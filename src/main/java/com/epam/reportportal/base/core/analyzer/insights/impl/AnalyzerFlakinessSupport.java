/*
 * Copyright 2026 EPAM Systems
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

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import java.util.List;

final class AnalyzerFlakinessSupport {

  static final int MIN_HISTORY_FOR_STATISTICAL_SCORING = 10;

  private static final int STABLE_MAX_SCORE = 20;

  private static final int UNSTABLE_MAX_SCORE = 50;

  private static final int FLAKY_MAX_SCORE = 75;

  private AnalyzerFlakinessSupport() {
  }

  static FlakinessDecision analyze(List<StatusEnum> statuses) {
    int totalRuns = statuses.size();
    int transitions = calculateTransitions(statuses);
    int flakyRate = totalRuns <= 1 ? 0 : Math.round(transitions * 100.0f / (totalRuns - 1));
    boolean mixedPassAndFailure = hasMixedPassAndFailure(statuses);

    if (totalRuns < MIN_HISTORY_FOR_STATISTICAL_SCORING) {
      return new FlakinessDecision(transitions, flakyRate, mixedPassAndFailure, false, null,
          mixedPassAndFailure ? "UNSTABLE" : null);
    }

    String label = getBadgeLabel(flakyRate);
    boolean flaky = label != null;
    return new FlakinessDecision(transitions, flakyRate, flaky, flaky && flakyRate >= UNSTABLE_MAX_SCORE,
        flaky ? flakyRate : null, label);
  }

  static String getBadgeLabel(int flakinessScore) {
    if (flakinessScore <= STABLE_MAX_SCORE) {
      return null;
    }
    if (flakinessScore <= UNSTABLE_MAX_SCORE) {
      return "UNSTABLE";
    }
    if (flakinessScore <= FLAKY_MAX_SCORE) {
      return "FLAKY";
    }
    return "CRITICAL";
  }

  private static int calculateTransitions(List<StatusEnum> statuses) {
    int transitions = 0;
    StatusEnum previousStatus = null;
    for (StatusEnum currentStatus : statuses) {
      if (previousStatus != null && previousStatus != currentStatus) {
        transitions++;
      }
      previousStatus = currentStatus;
    }
    return transitions;
  }

  private static boolean hasMixedPassAndFailure(List<StatusEnum> statuses) {
    boolean hasPass = statuses.stream().anyMatch(StatusEnum.PASSED::equals);
    boolean hasFailure = statuses.stream().anyMatch(AnalyzerFlakinessSupport::isFailureStatus);
    return hasPass && hasFailure;
  }

  private static boolean isFailureStatus(StatusEnum status) {
    return status != null && status != StatusEnum.PASSED && status != StatusEnum.SKIPPED;
  }

  record FlakinessDecision(int transitions, int flakyRate, boolean flaky, boolean quarantined,
                           Integer flakinessScore, String label) {
  }
}