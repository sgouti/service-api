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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzerTriageAgingRs {

  @JsonProperty("buckets")
  private Map<String, Bucket> buckets = new LinkedHashMap<>();

  @JsonProperty("totalToInvestigate")
  private int totalToInvestigate;

  public Map<String, Bucket> getBuckets() {
    return buckets;
  }

  public void setBuckets(Map<String, Bucket> buckets) {
    this.buckets = buckets;
  }

  public int getTotalToInvestigate() {
    return totalToInvestigate;
  }

  public void setTotalToInvestigate(int totalToInvestigate) {
    this.totalToInvestigate = totalToInvestigate;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Bucket {

    @JsonProperty("label")
    private String label;

    @JsonProperty("count")
    private int count;

    @JsonProperty("items")
    private List<Item> items = new ArrayList<>();

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }

    public List<Item> getItems() {
      return items;
    }

    public void setItems(List<Item> items) {
      this.items = items;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Item {

    @JsonProperty("itemId")
    private Long itemId;

    @JsonProperty("launchId")
    private Long launchId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("uniqueId")
    private String uniqueId;

    @JsonProperty("path")
    private String path;

    @JsonProperty("ageHours")
    private long ageHours;

    @JsonProperty("analysisOwnerId")
    private Long analysisOwnerId;

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

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public long getAgeHours() {
      return ageHours;
    }

    public void setAgeHours(long ageHours) {
      this.ageHours = ageHours;
    }

    public Long getAnalysisOwnerId() {
      return analysisOwnerId;
    }

    public void setAnalysisOwnerId(Long analysisOwnerId) {
      this.analysisOwnerId = analysisOwnerId;
    }
  }
}