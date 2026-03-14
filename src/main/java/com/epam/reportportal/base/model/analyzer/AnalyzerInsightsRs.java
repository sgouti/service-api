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

package com.epam.reportportal.base.model.analyzer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzerInsightsRs {

  @JsonProperty("launchId")
  private Long launchId;

  @JsonProperty("launchName")
  private String launchName;

  @JsonProperty("launchNumber")
  private Long launchNumber;

  @JsonProperty("recentLaunches")
  private List<LaunchRef> recentLaunches = new ArrayList<>();

  @JsonProperty("coverage")
  private Coverage coverage;

  @JsonProperty("triageAging")
  private List<TriageBucket> triageAging = new ArrayList<>();

  @JsonProperty("releaseAggregate")
  private List<ReleaseLaunch> releaseAggregate = new ArrayList<>();

  @JsonProperty("comparison")
  private Comparison comparison;

  @JsonProperty("quarantine")
  private List<QuarantineItem> quarantine = new ArrayList<>();

  public Long getLaunchId() {
    return launchId;
  }

  public void setLaunchId(Long launchId) {
    this.launchId = launchId;
  }

  public String getLaunchName() {
    return launchName;
  }

  public void setLaunchName(String launchName) {
    this.launchName = launchName;
  }

  public Long getLaunchNumber() {
    return launchNumber;
  }

  public void setLaunchNumber(Long launchNumber) {
    this.launchNumber = launchNumber;
  }

  public List<LaunchRef> getRecentLaunches() {
    return recentLaunches;
  }

  public void setRecentLaunches(List<LaunchRef> recentLaunches) {
    this.recentLaunches = recentLaunches;
  }

  public Coverage getCoverage() {
    return coverage;
  }

  public void setCoverage(Coverage coverage) {
    this.coverage = coverage;
  }

  public List<TriageBucket> getTriageAging() {
    return triageAging;
  }

  public void setTriageAging(List<TriageBucket> triageAging) {
    this.triageAging = triageAging;
  }

  public List<ReleaseLaunch> getReleaseAggregate() {
    return releaseAggregate;
  }

  public void setReleaseAggregate(List<ReleaseLaunch> releaseAggregate) {
    this.releaseAggregate = releaseAggregate;
  }

  public Comparison getComparison() {
    return comparison;
  }

  public void setComparison(Comparison comparison) {
    this.comparison = comparison;
  }

  public List<QuarantineItem> getQuarantine() {
    return quarantine;
  }

  public void setQuarantine(List<QuarantineItem> quarantine) {
    this.quarantine = quarantine;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class LaunchRef {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("number")
    private Long number;

    @JsonProperty("startTime")
    private Instant startTime;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Long getNumber() {
      return number;
    }

    public void setNumber(Long number) {
      this.number = number;
    }

    public Instant getStartTime() {
      return startTime;
    }

    public void setStartTime(Instant startTime) {
      this.startTime = startTime;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Coverage {

    @JsonProperty("totalItems")
    private int totalItems;

    @JsonProperty("nonPassedItems")
    private int nonPassedItems;

    @JsonProperty("autoAnalyzedItems")
    private int autoAnalyzedItems;

    @JsonProperty("coveragePercent")
    private int coveragePercent;

    public int getTotalItems() {
      return totalItems;
    }

    public void setTotalItems(int totalItems) {
      this.totalItems = totalItems;
    }

    public int getNonPassedItems() {
      return nonPassedItems;
    }

    public void setNonPassedItems(int nonPassedItems) {
      this.nonPassedItems = nonPassedItems;
    }

    public int getAutoAnalyzedItems() {
      return autoAnalyzedItems;
    }

    public void setAutoAnalyzedItems(int autoAnalyzedItems) {
      this.autoAnalyzedItems = autoAnalyzedItems;
    }

    public int getCoveragePercent() {
      return coveragePercent;
    }

    public void setCoveragePercent(int coveragePercent) {
      this.coveragePercent = coveragePercent;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class TriageBucket {

    @JsonProperty("label")
    private String label;

    @JsonProperty("minHours")
    private long minHours;

    @JsonProperty("maxHours")
    private long maxHours;

    @JsonProperty("count")
    private int count;

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public long getMinHours() {
      return minHours;
    }

    public void setMinHours(long minHours) {
      this.minHours = minHours;
    }

    public long getMaxHours() {
      return maxHours;
    }

    public void setMaxHours(long maxHours) {
      this.maxHours = maxHours;
    }

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ReleaseLaunch {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("number")
    private Integer number;

    @JsonProperty("startTime")
    private Instant startTime;

    @JsonProperty("values")
    private Map<String, Integer> values = new LinkedHashMap<>();

    @JsonProperty("qualityGateStatus")
    private String qualityGateStatus;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getNumber() {
      return number;
    }

    public void setNumber(Integer number) {
      this.number = number;
    }

    public Instant getStartTime() {
      return startTime;
    }

    public void setStartTime(Instant startTime) {
      this.startTime = startTime;
    }

    public Map<String, Integer> getValues() {
      return values;
    }

    public void setValues(Map<String, Integer> values) {
      this.values = values;
    }

    public String getQualityGateStatus() {
      return qualityGateStatus;
    }

    public void setQualityGateStatus(String qualityGateStatus) {
      this.qualityGateStatus = qualityGateStatus;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Comparison {

    @JsonProperty("currentLaunchId")
    private Long currentLaunchId;

    @JsonProperty("baselineLaunchId")
    private Long baselineLaunchId;

    @JsonProperty("metrics")
    private List<MetricDelta> metrics = new ArrayList<>();

    public Long getCurrentLaunchId() {
      return currentLaunchId;
    }

    public void setCurrentLaunchId(Long currentLaunchId) {
      this.currentLaunchId = currentLaunchId;
    }

    public Long getBaselineLaunchId() {
      return baselineLaunchId;
    }

    public void setBaselineLaunchId(Long baselineLaunchId) {
      this.baselineLaunchId = baselineLaunchId;
    }

    public List<MetricDelta> getMetrics() {
      return metrics;
    }

    public void setMetrics(List<MetricDelta> metrics) {
      this.metrics = metrics;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MetricDelta {

    @JsonProperty("label")
    private String label;

    @JsonProperty("field")
    private String field;

    @JsonProperty("baseline")
    private int baseline;

    @JsonProperty("current")
    private int current;

    @JsonProperty("delta")
    private int delta;

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public int getBaseline() {
      return baseline;
    }

    public void setBaseline(int baseline) {
      this.baseline = baseline;
    }

    public int getCurrent() {
      return current;
    }

    public void setCurrent(int current) {
      this.current = current;
    }

    public int getDelta() {
      return delta;
    }

    public void setDelta(int delta) {
      this.delta = delta;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class QuarantineItem {

    @JsonProperty("itemId")
    private Long itemId;

    @JsonProperty("launchId")
    private Long launchId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("uniqueId")
    private String uniqueId;

    @JsonProperty("currentStatus")
    private String currentStatus;

    @JsonProperty("totalRuns")
    private int totalRuns;

    @JsonProperty("flakyTransitions")
    private int flakyTransitions;

    @JsonProperty("flakyRate")
    private int flakyRate;

    @JsonProperty("quarantined")
    private boolean quarantined;

    @JsonProperty("statusHistory")
    private List<String> statusHistory = new ArrayList<>();

    public Long getItemId() {
      return itemId;
    }

    public void setItemId(Long itemId) {
      this.itemId = itemId;
    }

    public Long getLaunchId() {
      return launchId;
    }

    public void setLaunchId(Long launchId) {
      this.launchId = launchId;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getUniqueId() {
      return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
      this.uniqueId = uniqueId;
    }

    public String getCurrentStatus() {
      return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
      this.currentStatus = currentStatus;
    }

    public int getTotalRuns() {
      return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
      this.totalRuns = totalRuns;
    }

    public int getFlakyTransitions() {
      return flakyTransitions;
    }

    public void setFlakyTransitions(int flakyTransitions) {
      this.flakyTransitions = flakyTransitions;
    }

    public int getFlakyRate() {
      return flakyRate;
    }

    public void setFlakyRate(int flakyRate) {
      this.flakyRate = flakyRate;
    }

    public boolean isQuarantined() {
      return quarantined;
    }

    public void setQuarantined(boolean quarantined) {
      this.quarantined = quarantined;
    }

    public List<String> getStatusHistory() {
      return statusHistory;
    }

    public void setStatusHistory(List<String> statusHistory) {
      this.statusHistory = statusHistory;
    }
  }
}