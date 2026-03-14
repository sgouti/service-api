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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.analyzer.insights.AnalyzerProjectMetricsProvider;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.model.analyzer.AnalyzerCoverageRs;
import com.epam.reportportal.base.model.analyzer.AnalyzerTriageAgingRs;
import com.epam.reportportal.base.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/{projectKey}")
public class AnalyzerProjectMetricsController {

  private final AnalyzerProjectMetricsProvider analyzerProjectMetricsProvider;
  private final ProjectExtractor projectExtractor;

  public AnalyzerProjectMetricsController(
      AnalyzerProjectMetricsProvider analyzerProjectMetricsProvider,
      ProjectExtractor projectExtractor) {
    this.analyzerProjectMetricsProvider = analyzerProjectMetricsProvider;
    this.projectExtractor = projectExtractor;
  }

  @Transactional(readOnly = true)
  @GetMapping("/triage/aging")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get project triage aging distribution")
  public AnalyzerTriageAgingRs getProjectTriageAging(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    Long projectId = projectExtractor.extractMembershipDetails(user, projectKey).getProjectId();
    return analyzerProjectMetricsProvider.getProjectTriageAging(projectId);
  }

  @Transactional(readOnly = true)
  @GetMapping("/analyzer/coverage")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get project auto-analysis coverage by sprint")
  public AnalyzerCoverageRs getProjectCoverage(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    Long projectId = projectExtractor.extractMembershipDetails(user, projectKey).getProjectId();
    return analyzerProjectMetricsProvider.getProjectCoverage(projectId);
  }
}