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

package com.epam.reportportal.base.core.widget.content.loader;

import static com.epam.reportportal.base.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;

import com.epam.reportportal.base.core.analyzer.insights.AnalyzerProjectMetricsProvider;
import com.epam.reportportal.base.core.widget.content.LoadContentStrategy;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.model.analyzer.AnalyzerTriageAgingRs;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TriageAgingHeatmapContentLoader implements LoadContentStrategy {

  private final AnalyzerProjectMetricsProvider analyzerProjectMetricsProvider;

  public TriageAgingHeatmapContentLoader(
      AnalyzerProjectMetricsProvider analyzerProjectMetricsProvider) {
    this.analyzerProjectMetricsProvider = analyzerProjectMetricsProvider;
  }

  @Override
  public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping,
      WidgetOptions widgetOptions, int limit) {
    Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());
    AnalyzerTriageAgingRs response = analyzerProjectMetricsProvider.getProjectTriageAging(
        extractProjectId(filter));

    Map<String, Object> content = new LinkedHashMap<>();
    content.put("buckets", response.getBuckets());
    content.put("totalToInvestigate", response.getTotalToInvestigate());
    return content;
  }

  private Long extractProjectId(Filter filter) {
    return filter.getFilterConditions().stream()
        .filter(FilterCondition.class::isInstance)
        .map(FilterCondition.class::cast)
        .filter(condition -> CRITERIA_PROJECT_ID.equals(condition.getSearchCriteria()))
        .map(FilterCondition::getValue)
        .map(Long::valueOf)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Project scope is required for triage aging widget"));
  }
}