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
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzerItemFlakinessRs {

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

  @JsonProperty("flaky")
  private boolean flaky;

  @JsonProperty("quarantined")
  private boolean quarantined;

  @JsonProperty("totalRuns")
  private int totalRuns;

  @JsonProperty("flakyTransitions")
  private int flakyTransitions;

  @JsonProperty("flakyRate")
  private int flakyRate;

  @JsonProperty("flakinessScore")
  private Integer flakinessScore;

  @JsonProperty("label")
  private String label;

  @JsonProperty("lastStatusChange")
  private Instant lastStatusChange;

  @JsonProperty("history")
  private List<HistoryEntry> history = new ArrayList<>();

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

  public boolean isFlaky() {
    return flaky;
  }

  public void setFlaky(boolean flaky) {
    this.flaky = flaky;
  }

  public boolean isQuarantined() {
    return quarantined;
  }

  public void setQuarantined(boolean quarantined) {
    this.quarantined = quarantined;
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

  public Integer getFlakinessScore() {
    return flakinessScore;
  }

  public void setFlakinessScore(Integer flakinessScore) {
    this.flakinessScore = flakinessScore;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Instant getLastStatusChange() {
    return lastStatusChange;
  }

  public void setLastStatusChange(Instant lastStatusChange) {
    this.lastStatusChange = lastStatusChange;
  }

  public List<HistoryEntry> getHistory() {
    return history;
  }

  public void setHistory(List<HistoryEntry> history) {
    this.history = history;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class HistoryEntry {

    @JsonProperty("itemId")
    private Long itemId;

    @JsonProperty("launchId")
    private Long launchId;

    @JsonProperty("launchName")
    private String launchName;

    @JsonProperty("launchNumber")
    private Long launchNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("startTime")
    private Instant startTime;

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

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public Instant getStartTime() {
      return startTime;
    }

    public void setStartTime(Instant startTime) {
      this.startTime = startTime;
    }
  }
}