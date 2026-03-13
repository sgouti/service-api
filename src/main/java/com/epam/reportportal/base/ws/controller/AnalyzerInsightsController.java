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

import static com.epam.reportportal.base.auth.AuthorizationProvider.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.analyzer.insights.GetAnalyzerInsightsHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.model.analyzer.AnalyzerInsightsRs;
import com.epam.reportportal.base.model.analyzer.AnalyzerItemFlakinessRs;
import com.epam.reportportal.base.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/{projectKey}/analyzer/insights")
public class AnalyzerInsightsController {

  private final GetAnalyzerInsightsHandler getAnalyzerInsightsHandler;
  private final ProjectExtractor projectExtractor;

  public AnalyzerInsightsController(GetAnalyzerInsightsHandler getAnalyzerInsightsHandler,
      ProjectExtractor projectExtractor) {
    this.getAnalyzerInsightsHandler = getAnalyzerInsightsHandler;
    this.projectExtractor = projectExtractor;
  }

  @Transactional(readOnly = true)
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get analyzer insights for a launch")
  public AnalyzerInsightsRs getLaunchInsights(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestParam(value = "launchId", required = false) Long launchId,
      @RequestParam(value = "compareToId", required = false) Long compareToId,
      @RequestParam(value = "historyDepth", required = false, defaultValue = "10")
      int historyDepth) {
    return getAnalyzerInsightsHandler.getLaunchInsights(
        projectExtractor.extractMembershipDetails(user, projectKey), launchId, compareToId,
        historyDepth);
  }

  @Transactional(readOnly = true)
  @GetMapping("/item/{itemId}/flakiness")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Get flakiness details for a test item")
  public AnalyzerItemFlakinessRs getItemFlakiness(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable Long itemId,
      @RequestParam(value = "historyDepth", required = false, defaultValue = "10")
      int historyDepth) {
    return getAnalyzerInsightsHandler.getItemFlakiness(
        projectExtractor.extractMembershipDetails(user, projectKey), itemId, historyDepth);
  }
}