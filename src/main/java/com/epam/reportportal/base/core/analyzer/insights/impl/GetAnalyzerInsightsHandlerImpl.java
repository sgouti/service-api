/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.analyzer.insights.impl;

import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_AUTOMATION_BUG_TOTAL;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_PRODUCT_BUG_TOTAL;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_SYSTEM_ISSUE_TOTAL;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DEFECTS_TO_INVESTIGATE_TOTAL;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_FAILED;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_PASSED;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_SKIPPED;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.TEST_ITEM_NOT_FOUND;

import com.epam.reportportal.base.core.analyzer.insights.GetAnalyzerInsightsHandler;
import com.epam.reportportal.base.core.launch.GetLaunchHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.ChartStatisticsContent;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.analyzer.AnalyzerInsightsRs;
import com.epam.reportportal.base.model.analyzer.AnalyzerItemFlakinessRs;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAnalyzerInsightsHandlerImpl implements GetAnalyzerInsightsHandler {

  private static final int DEFAULT_HISTORY_DEPTH = 10;
  private static final int RECENT_LAUNCHES_LIMIT = 12;
  private static final List<String> COMPARISON_FIELDS = List.of(
      EXECUTIONS_PASSED,
      EXECUTIONS_FAILED,
      EXECUTIONS_SKIPPED,
      DEFECTS_AUTOMATION_BUG_TOTAL,
      DEFECTS_PRODUCT_BUG_TOTAL,
      DEFECTS_SYSTEM_ISSUE_TOTAL,
      DEFECTS_TO_INVESTIGATE_TOTAL
  );

  private final LaunchRepository launchRepository;
  private final TestItemRepository testItemRepository;
  private final GetLaunchHandler getLaunchHandler;

  @Override
  @Transactional(readOnly = true)
  public AnalyzerInsightsRs getLaunchInsights(MembershipDetails membershipDetails, Long launchId,
      Long compareToId, int historyDepth) {
    final Launch launch = resolveLaunch(membershipDetails, launchId);
    final int normalizedHistoryDepth = normalizeHistoryDepth(historyDepth);
    final List<Launch> historyLaunches = loadHistoryLaunches(launch, membershipDetails,
        normalizedHistoryDepth);
    final Map<Long, Launch> launchById = historyLaunches.stream()
        .collect(Collectors.toMap(Launch::getId, Function.identity()));
    final List<TestItem> historyItems = loadEligibleItems(historyLaunches);
    final Map<String, List<TestItem>> historyItemsByUniqueId = historyItems.stream()
        .collect(Collectors.groupingBy(TestItem::getUniqueId));
    final List<TestItem> currentItems = historyItems.stream()
        .filter(item -> Objects.equals(item.getLaunchId(), launch.getId()))
        .sorted(Comparator.comparing(TestItem::getStartTime))
        .toList();

    AnalyzerInsightsRs response = new AnalyzerInsightsRs();
    response.setLaunchId(launch.getId());
    response.setLaunchName(launch.getName());
    response.setLaunchNumber(launch.getNumber());
    response.setRecentLaunches(loadRecentLaunches(membershipDetails.getProjectId()));
    response.setCoverage(buildCoverage(currentItems));
    response.setTriageAging(buildTriageAging(currentItems));
    response.setReleaseAggregate(buildReleaseAggregate(membershipDetails, historyLaunches));
    response.setComparison(buildComparison(membershipDetails, launch, compareToId, historyLaunches));
    response.setQuarantine(buildQuarantineItems(currentItems, historyItemsByUniqueId, launchById));
    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public AnalyzerItemFlakinessRs getItemFlakiness(MembershipDetails membershipDetails, Long itemId,
      int historyDepth) {
    TestItem item = testItemRepository.findById(itemId)
        .orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, itemId));
    Launch launch = launchRepository.findByIdAndProjectId(item.getLaunchId(), membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, item.getLaunchId()));
    List<Launch> historyLaunches = loadHistoryLaunches(launch, membershipDetails,
        normalizeHistoryDepth(historyDepth));
    Map<Long, Launch> launchById = historyLaunches.stream()
        .collect(Collectors.toMap(Launch::getId, Function.identity()));
    List<TestItem> historyItems = loadEligibleItems(historyLaunches)
        .stream()
        .filter(candidate -> Objects.equals(candidate.getUniqueId(), item.getUniqueId()))
        .sorted(Comparator.comparing(TestItem::getStartTime))
        .toList();

    return buildItemFlakiness(item, historyItems, launchById);
  }

  private Launch resolveLaunch(MembershipDetails membershipDetails, Long launchId) {
    if (launchId != null) {
      return launchRepository.findByIdAndProjectId(launchId, membershipDetails.getProjectId())
          .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
    }

    return launchRepository.findTop20ByProjectIdAndModeNotOrderByStartTimeDescNumberDesc(
            membershipDetails.getProjectId(), LaunchModeEnum.DEBUG)
        .stream()
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND,
            "No launches available for analyzer insights"));
  }

  private List<Launch> loadHistoryLaunches(Launch launch, MembershipDetails membershipDetails,
      int historyDepth) {
    return launchRepository.findLaunchesHistory(historyDepth, launch.getId(), launch.getName(),
        membershipDetails.getProjectId());
  }

  private List<TestItem> loadEligibleItems(Collection<Launch> launches) {
    if (launches.isEmpty()) {
      return List.of();
    }

    Set<Long> launchIds = launches.stream().map(Launch::getId).collect(Collectors.toSet());
    return testItemRepository.findTestItemsByLaunchIdInOrderByStartTimeAsc(launchIds)
        .stream()
        .filter(this::isEligibleItem)
        .toList();
  }

  private boolean isEligibleItem(TestItem item) {
    return item.isHasStats() && !item.isHasChildren() && item.getRetryOf() == null
        && item.getItemResults() != null && item.getItemResults().getStatus() != null;
  }

  private List<AnalyzerInsightsRs.LaunchRef> loadRecentLaunches(Long projectId) {
    return launchRepository.findTop20ByProjectIdAndModeNotOrderByStartTimeDescNumberDesc(projectId,
            LaunchModeEnum.DEBUG)
        .stream()
        .limit(RECENT_LAUNCHES_LIMIT)
        .map(this::toLaunchRef)
        .toList();
  }

  private AnalyzerInsightsRs.LaunchRef toLaunchRef(Launch launch) {
    AnalyzerInsightsRs.LaunchRef ref = new AnalyzerInsightsRs.LaunchRef();
    ref.setId(launch.getId());
    ref.setName(launch.getName());
    ref.setNumber(launch.getNumber());
    ref.setStartTime(launch.getStartTime());
    return ref;
  }

  private AnalyzerInsightsRs.Coverage buildCoverage(List<TestItem> currentItems) {
    int nonPassedItems = (int) currentItems.stream().filter(this::isNonPassed).count();
    int autoAnalyzedItems = (int) currentItems.stream()
        .filter(this::isNonPassed)
        .filter(item -> Optional.ofNullable(item.getItemResults().getIssue())
            .map(IssueEntity::getAutoAnalyzed)
            .orElse(false))
        .count();

    AnalyzerInsightsRs.Coverage coverage = new AnalyzerInsightsRs.Coverage();
    coverage.setTotalItems(currentItems.size());
    coverage.setNonPassedItems(nonPassedItems);
    coverage.setAutoAnalyzedItems(autoAnalyzedItems);
    coverage.setCoveragePercent(nonPassedItems == 0 ? 0 : Math.round(autoAnalyzedItems * 100.0f / nonPassedItems));
    return coverage;
  }

  private List<AnalyzerInsightsRs.TriageBucket> buildTriageAging(List<TestItem> currentItems) {
    List<BucketRange> ranges = List.of(
        new BucketRange("0-24h", 0, 24),
        new BucketRange("1-3d", 24, 72),
        new BucketRange("3-7d", 72, 168),
        new BucketRange("7d+", 168, Long.MAX_VALUE)
    );
    Instant now = Instant.now();

    return ranges.stream().map(range -> {
      AnalyzerInsightsRs.TriageBucket bucket = new AnalyzerInsightsRs.TriageBucket();
      bucket.setLabel(range.label());
      bucket.setMinHours(range.minHours());
      bucket.setMaxHours(range.maxHours() == Long.MAX_VALUE ? -1 : range.maxHours());
      bucket.setCount((int) currentItems.stream()
          .filter(this::isNonPassed)
          .filter(item -> {
            long ageHours = Duration.between(item.getStartTime(), now).toHours();
            return ageHours >= range.minHours() && ageHours < range.maxHours();
          })
          .count());
      return bucket;
    }).toList();
  }

  private List<AnalyzerInsightsRs.ReleaseLaunch> buildReleaseAggregate(
      MembershipDetails membershipDetails, List<Launch> historyLaunches) {
    if (historyLaunches.isEmpty()) {
      return List.of();
    }

    Long[] ids = historyLaunches.stream().map(Launch::getId).toArray(Long[]::new);
    Map<Long, ChartStatisticsContent> contentById = getLaunchHandler.getLaunchesComparisonInfo(
            membershipDetails, ids)
        .getOrDefault("result", List.of())
        .stream()
        .collect(Collectors.toMap(ChartStatisticsContent::getId, Function.identity()));

    return historyLaunches.stream().map(launch -> {
      AnalyzerInsightsRs.ReleaseLaunch entry = new AnalyzerInsightsRs.ReleaseLaunch();
      entry.setId(launch.getId());
      entry.setName(launch.getName());
      entry.setNumber(launch.getNumber().intValue());
      entry.setStartTime(launch.getStartTime());
      ChartStatisticsContent content = contentById.get(launch.getId());
      entry.setValues(COMPARISON_FIELDS.stream().collect(Collectors.toMap(
          Function.identity(),
          field -> parseMetric(content, field),
          (left, right) -> right,
          LinkedHashMap::new
      )));
      entry.setQualityGateStatus(computeQualityGate(content));
      return entry;
    }).toList();
  }

  private String computeQualityGate(ChartStatisticsContent content) {
    int passed = parseMetric(content, EXECUTIONS_PASSED);
    int failed = parseMetric(content, EXECUTIONS_FAILED);
    int total = passed + failed + parseMetric(content, EXECUTIONS_SKIPPED);
    if (total == 0) {
      return "PASS";
    }
    double passRate = (double) passed / total * 100;
    int productBugs = parseMetric(content, DEFECTS_PRODUCT_BUG_TOTAL);
    if (passRate < 85 || productBugs >= 2) {
      return "BLOCK";
    }
    if (passRate < 95 || productBugs >= 1) {
      return "WARN";
    }
    return "PASS";
  }

  private AnalyzerInsightsRs.Comparison buildComparison(MembershipDetails membershipDetails,
      Launch currentLaunch, Long compareToId, List<Launch> historyLaunches) {
    Launch baseline = resolveBaselineLaunch(currentLaunch, compareToId, historyLaunches,
        membershipDetails.getProjectId());
    if (baseline == null) {
      return null;
    }

    Map<Long, ChartStatisticsContent> contentById = getLaunchHandler.getLaunchesComparisonInfo(
            membershipDetails, new Long[]{baseline.getId(), currentLaunch.getId()})
        .getOrDefault("result", List.of())
        .stream()
        .collect(Collectors.toMap(ChartStatisticsContent::getId, Function.identity()));

    ChartStatisticsContent current = contentById.get(currentLaunch.getId());
    ChartStatisticsContent baselineContent = contentById.get(baseline.getId());

    AnalyzerInsightsRs.Comparison comparison = new AnalyzerInsightsRs.Comparison();
    comparison.setCurrentLaunchId(currentLaunch.getId());
    comparison.setBaselineLaunchId(baseline.getId());
    comparison.setMetrics(Arrays.asList(
        buildMetricDelta("Passed", EXECUTIONS_PASSED, baselineContent, current),
        buildMetricDelta("Failed", EXECUTIONS_FAILED, baselineContent, current),
        buildMetricDelta("Skipped", EXECUTIONS_SKIPPED, baselineContent, current),
        buildMetricDelta("Auto Bug", DEFECTS_AUTOMATION_BUG_TOTAL, baselineContent, current),
        buildMetricDelta("Product Bug", DEFECTS_PRODUCT_BUG_TOTAL, baselineContent, current),
        buildMetricDelta("System Issue", DEFECTS_SYSTEM_ISSUE_TOTAL, baselineContent, current),
        buildMetricDelta("To Investigate", DEFECTS_TO_INVESTIGATE_TOTAL, baselineContent,
            current)
    ));
    return comparison;
  }

  private AnalyzerInsightsRs.MetricDelta buildMetricDelta(String label, String field,
      ChartStatisticsContent baseline, ChartStatisticsContent current) {
    int baselineValue = parseMetric(baseline, field);
    int currentValue = parseMetric(current, field);

    AnalyzerInsightsRs.MetricDelta metric = new AnalyzerInsightsRs.MetricDelta();
    metric.setLabel(label);
    metric.setField(field);
    metric.setBaseline(baselineValue);
    metric.setCurrent(currentValue);
    metric.setDelta(currentValue - baselineValue);
    return metric;
  }

  private Launch resolveBaselineLaunch(Launch currentLaunch, Long compareToId,
      List<Launch> historyLaunches, Long projectId) {
    if (compareToId != null && !Objects.equals(compareToId, currentLaunch.getId())) {
      return launchRepository.findByIdAndProjectId(compareToId, projectId)
          .orElse(null);
    }

    return historyLaunches.stream()
        .filter(launch -> !Objects.equals(launch.getId(), currentLaunch.getId()))
        .findFirst()
        .orElse(null);
  }

  private List<AnalyzerInsightsRs.QuarantineItem> buildQuarantineItems(List<TestItem> currentItems,
      Map<String, List<TestItem>> historyItemsByUniqueId, Map<Long, Launch> launchById) {
    return currentItems.stream()
        .filter(this::isNonPassed)
        .map(item -> buildQuarantineItem(item, historyItemsByUniqueId.get(item.getUniqueId()),
            launchById))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparingInt(AnalyzerInsightsRs.QuarantineItem::getFlakyRate)
            .reversed()
            .thenComparingInt(AnalyzerInsightsRs.QuarantineItem::getFlakyTransitions).reversed())
        .limit(25)
        .toList();
  }

  private AnalyzerInsightsRs.QuarantineItem buildQuarantineItem(TestItem item,
      List<TestItem> historyItems, Map<Long, Launch> launchById) {
    if (historyItems == null || historyItems.isEmpty()) {
      return null;
    }
    AnalyzerItemFlakinessRs flakiness = buildItemFlakiness(item, historyItems, launchById);
    AnalyzerInsightsRs.QuarantineItem quarantineItem = new AnalyzerInsightsRs.QuarantineItem();
    quarantineItem.setItemId(item.getItemId());
    quarantineItem.setLaunchId(item.getLaunchId());
    quarantineItem.setName(item.getName());
    quarantineItem.setUniqueId(item.getUniqueId());
    quarantineItem.setCurrentStatus(flakiness.getCurrentStatus());
    quarantineItem.setTotalRuns(flakiness.getTotalRuns());
    quarantineItem.setFlakyTransitions(flakiness.getFlakyTransitions());
    quarantineItem.setFlakyRate(flakiness.getFlakyRate());
    quarantineItem.setQuarantined(flakiness.isQuarantined());
    quarantineItem.setStatusHistory(flakiness.getHistory().stream()
        .map(AnalyzerItemFlakinessRs.HistoryEntry::getStatus)
        .toList());
    return quarantineItem;
  }

  private AnalyzerItemFlakinessRs buildItemFlakiness(TestItem item, List<TestItem> historyItems,
      Map<Long, Launch> launchById) {
    List<TestItem> sortedHistory = historyItems.stream()
        .sorted(Comparator.comparing(TestItem::getStartTime))
        .toList();

    List<StatusEnum> historyStatuses = sortedHistory.stream()
        .map(historyItem -> historyItem.getItemResults().getStatus())
        .toList();

    int transitions = 0;
    Instant lastStatusChange = null;
    StatusEnum previousStatus = null;
    for (StatusEnum currentStatus : historyStatuses) {
      if (previousStatus != null && previousStatus != currentStatus) {
        transitions++;
        lastStatusChange = sortedHistory.get(historyStatuses.indexOf(currentStatus)).getStartTime();
      }
      previousStatus = currentStatus;
    }

    AnalyzerFlakinessSupport.FlakinessDecision decision = AnalyzerFlakinessSupport.analyze(
        historyStatuses);

    AnalyzerItemFlakinessRs response = new AnalyzerItemFlakinessRs();
    response.setItemId(item.getItemId());
    response.setLaunchId(item.getLaunchId());
    response.setName(item.getName());
    response.setUniqueId(item.getUniqueId());
    response.setCurrentStatus(item.getItemResults().getStatus().name());
    response.setTotalRuns(sortedHistory.size());
    response.setFlakyTransitions(decision.transitions());
    response.setFlakyRate(decision.flakyRate());
    response.setFlakinessScore(decision.flakinessScore());
    response.setLabel(decision.label());
    response.setFlaky(decision.flaky());
    response.setQuarantined(decision.quarantined());
    response.setLastStatusChange(lastStatusChange);
    response.setHistory(sortedHistory.stream().map(historyItem -> {
      AnalyzerItemFlakinessRs.HistoryEntry entry = new AnalyzerItemFlakinessRs.HistoryEntry();
      Launch launch = launchById.get(historyItem.getLaunchId());
      entry.setItemId(historyItem.getItemId());
      entry.setLaunchId(historyItem.getLaunchId());
      entry.setLaunchName(launch != null ? launch.getName() : null);
      entry.setLaunchNumber(launch != null ? launch.getNumber() : null);
      entry.setStatus(historyItem.getItemResults().getStatus().name());
      entry.setStartTime(historyItem.getStartTime());
      return entry;
    }).toList());
    return response;
  }

  private boolean isNonPassed(TestItem item) {
    StatusEnum status = item.getItemResults().getStatus();
    return status != StatusEnum.PASSED && status != StatusEnum.SKIPPED;
  }

  private int normalizeHistoryDepth(int historyDepth) {
    return historyDepth > 0 ? historyDepth : DEFAULT_HISTORY_DEPTH;
  }

  private int parseMetric(ChartStatisticsContent content, String field) {
    if (content == null || content.getValues() == null) {
      return 0;
    }

    return Optional.ofNullable(content.getValues().get(field))
        .map(value -> {
          try {
            return Integer.parseInt(value);
          } catch (NumberFormatException ignore) {
            return 0;
          }
        })
        .orElse(0);
  }

  private record BucketRange(String label, long minHours, long maxHours) {
  }
}