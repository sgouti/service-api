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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzerCoverageRs {

  @JsonProperty("currentSprint")
  private SprintCoverage currentSprint;

  @JsonProperty("previousSprint")
  private SprintCoverage previousSprint;

  @JsonProperty("trend")
  private String trend;

  @JsonProperty("missingSprintAttribute")
  private boolean missingSprintAttribute;

  public SprintCoverage getCurrentSprint() {
    return currentSprint;
  }

  public void setCurrentSprint(SprintCoverage currentSprint) {
    this.currentSprint = currentSprint;
  }

  public SprintCoverage getPreviousSprint() {
    return previousSprint;
  }

  public void setPreviousSprint(SprintCoverage previousSprint) {
    this.previousSprint = previousSprint;
  }

  public String getTrend() {
    return trend;
  }

  public void setTrend(String trend) {
    this.trend = trend;
  }

  public boolean isMissingSprintAttribute() {
    return missingSprintAttribute;
  }

  public void setMissingSprintAttribute(boolean missingSprintAttribute) {
    this.missingSprintAttribute = missingSprintAttribute;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SprintCoverage {

    @JsonProperty("sprintName")
    private String sprintName;

    @JsonProperty("coveragePercent")
    private float coveragePercent;

    @JsonProperty("avgConfidence")
    private Float avgConfidence;

    @JsonProperty("manualTriagePercent")
    private float manualTriagePercent;

    @JsonProperty("totalItems")
    private int totalItems;

    @JsonProperty("autoClassified")
    private int autoClassified;

    public String getSprintName() {
      return sprintName;
    }

    public void setSprintName(String sprintName) {
      this.sprintName = sprintName;
    }

    public float getCoveragePercent() {
      return coveragePercent;
    }

    public void setCoveragePercent(float coveragePercent) {
      this.coveragePercent = coveragePercent;
    }

    public Float getAvgConfidence() {
      return avgConfidence;
    }

    public void setAvgConfidence(Float avgConfidence) {
      this.avgConfidence = avgConfidence;
    }

    public float getManualTriagePercent() {
      return manualTriagePercent;
    }

    public void setManualTriagePercent(float manualTriagePercent) {
      this.manualTriagePercent = manualTriagePercent;
    }

    public int getTotalItems() {
      return totalItems;
    }

    public void setTotalItems(int totalItems) {
      this.totalItems = totalItems;
    }

    public int getAutoClassified() {
      return autoClassified;
    }

    public void setAutoClassified(int autoClassified) {
      this.autoClassified = autoClassified;
    }
  }
}