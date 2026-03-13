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

package com.epam.reportportal.base.core.analyzer.insights;

import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.model.analyzer.AnalyzerCoverageRs;
import com.epam.reportportal.base.model.analyzer.AnalyzerTriageAgingRs;
import java.time.Duration;
import java.time.Instant;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyzerProjectMetricsProvider {

  private static final String SPRINT_ATTRIBUTE_KEY = "sprint";

  private final LaunchRepository launchRepository;
  private final TestItemRepository testItemRepository;

  public AnalyzerProjectMetricsProvider(LaunchRepository launchRepository,
      TestItemRepository testItemRepository) {
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
  }

  @Transactional(readOnly = true)
  public AnalyzerTriageAgingRs getProjectTriageAging(Long projectId) {
    List<Launch> recentLaunches = loadRecentProjectLaunches(projectId);
    List<TestItem> latestItems = latestItemsByUniqueId(loadEligibleItems(recentLaunches)).values()
        .stream()
        .filter(this::isToInvestigateItem)
        .sorted(Comparator.comparing(TestItem::getStartTime))
        .toList();

    AnalyzerTriageAgingRs response = new AnalyzerTriageAgingRs();
    Map<String, AnalyzerTriageAgingRs.Bucket> buckets = new LinkedHashMap<>();
    buckets.put("fresh", buildBucket("0-24h", latestItems, 0, 24));
    buckets.put("aging", buildBucket("1-3 days", latestItems, 24, 72));
    buckets.put("stale", buildBucket("3-7 days", latestItems, 72, 168));
    buckets.put("breach", buildBucket("7+ days", latestItems, 168, Long.MAX_VALUE));
    response.setBuckets(buckets);
    response.setTotalToInvestigate(latestItems.size());
    return response;
  }

  @Transactional(readOnly = true)
  public AnalyzerCoverageRs getProjectCoverage(Long projectId) {
    List<Launch> recentLaunches = loadRecentProjectLaunches(projectId);
    List<Launch> sprintLaunches = recentLaunches.stream()
        .filter(launch -> findSprintName(launch) != null)
        .toList();

    AnalyzerCoverageRs response = new AnalyzerCoverageRs();
    if (sprintLaunches.isEmpty()) {
      response.setMissingSprintAttribute(true);
      response.setTrend("STABLE");
      return response;
    }

    Map<String, List<Launch>> launchesBySprint = sprintLaunches.stream()
        .collect(Collectors.groupingBy(this::findSprintName, LinkedHashMap::new,
            Collectors.toList()));
    List<Map.Entry<String, List<Launch>>> orderedSprints = launchesBySprint.entrySet().stream()
        .sorted(Comparator.comparing((Map.Entry<String, List<Launch>> entry) -> entry.getValue()
                .stream()
                .map(Launch::getStartTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(Instant.EPOCH))
            .reversed())
        .toList();

    if (orderedSprints.size() < 2) {
      response.setMissingSprintAttribute(true);
      response.setTrend("STABLE");
      return response;
    }

    AnalyzerCoverageRs.SprintCoverage current = buildSprintCoverage(orderedSprints.get(0));
    AnalyzerCoverageRs.SprintCoverage previous = buildSprintCoverage(orderedSprints.get(1));
    response.setCurrentSprint(current);
    response.setPreviousSprint(previous);
    response.setTrend(resolveTrend(current, previous));
    return response;
  }

  private List<Launch> loadRecentProjectLaunches(Long projectId) {
    return launchRepository.findTop200ByProjectIdAndModeNotOrderByStartTimeDescNumberDesc(projectId,
        LaunchModeEnum.DEBUG);
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

  private Map<String, TestItem> latestItemsByUniqueId(List<TestItem> items) {
    return items.stream()
        .filter(item -> item.getUniqueId() != null)
        .collect(Collectors.toMap(TestItem::getUniqueId, Function.identity(),
            (left, right) -> left.getStartTime().isAfter(right.getStartTime()) ? left : right));
  }

  private AnalyzerTriageAgingRs.Bucket buildBucket(String label, List<TestItem> items,
      long minHours, long maxHours) {
    Instant now = Instant.now();
    List<AnalyzerTriageAgingRs.Item> bucketItems = items.stream()
        .filter(item -> {
          long ageHours = Duration.between(item.getStartTime(), now).toHours();
          return ageHours >= minHours && ageHours < maxHours;
        })
        .sorted(Comparator.comparing(TestItem::getStartTime))
        .map(item -> toBucketItem(item, now))
        .toList();

    AnalyzerTriageAgingRs.Bucket bucket = new AnalyzerTriageAgingRs.Bucket();
    bucket.setLabel(label);
    bucket.setCount(bucketItems.size());
    bucket.setItems(bucketItems);
    return bucket;
  }

  private AnalyzerTriageAgingRs.Item toBucketItem(TestItem item, Instant now) {
    AnalyzerTriageAgingRs.Item entry = new AnalyzerTriageAgingRs.Item();
    entry.setItemId(item.getItemId());
    entry.setLaunchId(item.getLaunchId());
    entry.setName(item.getName());
    entry.setUniqueId(item.getUniqueId());
    entry.setPath(item.getPath());
    entry.setAnalysisOwnerId(item.getAnalysisOwnerId());
    entry.setAgeHours(Duration.between(item.getStartTime(), now).toHours());
    return entry;
  }

  private AnalyzerCoverageRs.SprintCoverage buildSprintCoverage(
      Map.Entry<String, List<Launch>> sprintEntry) {
    List<TestItem> items = loadEligibleItems(sprintEntry.getValue());
    int totalAnalyzed = (int) items.stream().filter(this::isNonPassed).count();
    int autoClassified = (int) items.stream().filter(this::isNonPassed)
        .filter(item -> Optional.ofNullable(item.getItemResults().getIssue())
            .map(IssueEntity::getAutoAnalyzed)
            .orElse(false))
        .count();

    AnalyzerCoverageRs.SprintCoverage coverage = new AnalyzerCoverageRs.SprintCoverage();
    coverage.setSprintName(sprintEntry.getKey());
    coverage.setTotalItems(totalAnalyzed);
    coverage.setAutoClassified(autoClassified);
    coverage.setCoveragePercent(totalAnalyzed == 0 ? 0
        : roundToSingleDecimal(autoClassified * 100.0f / totalAnalyzed));
    coverage.setManualTriagePercent(totalAnalyzed == 0 ? 0
        : roundToSingleDecimal((totalAnalyzed - autoClassified) * 100.0f / totalAnalyzed));
    coverage.setAvgConfidence(null);
    return coverage;
  }

  private String resolveTrend(AnalyzerCoverageRs.SprintCoverage current,
      AnalyzerCoverageRs.SprintCoverage previous) {
    float delta = current.getCoveragePercent() - previous.getCoveragePercent();
    if (delta > 3.0f) {
      return "IMPROVING";
    }
    if (delta < -3.0f) {
      return "DEGRADING";
    }
    return "STABLE";
  }

  private float roundToSingleDecimal(float value) {
    return Math.round(value * 10.0f) / 10.0f;
  }

  private String findSprintName(Launch launch) {
    return launch.getAttributes().stream()
        .filter(attribute -> attribute.getKey() != null)
        .filter(attribute -> SPRINT_ATTRIBUTE_KEY.equalsIgnoreCase(attribute.getKey()))
        .map(ItemAttribute::getValue)
        .filter(value -> value != null && !value.isBlank())
        .findFirst()
        .orElse(null);
  }

  private boolean isEligibleItem(TestItem item) {
    return item.isHasStats() && !item.isHasChildren() && item.getRetryOf() == null
        && item.getItemResults() != null && item.getItemResults().getStatus() != null;
  }

  private boolean isNonPassed(TestItem item) {
    StatusEnum status = item.getItemResults().getStatus();
    return status != StatusEnum.PASSED && status != StatusEnum.SKIPPED;
  }

  private boolean isToInvestigateItem(TestItem item) {
    return isNonPassed(item) && Optional.ofNullable(item.getItemResults().getIssue())
        .map(IssueEntity::getIssueType)
        .map(issueType -> TO_INVESTIGATE.getLocator().equalsIgnoreCase(issueType.getLocator()))
        .orElse(false);
  }
}