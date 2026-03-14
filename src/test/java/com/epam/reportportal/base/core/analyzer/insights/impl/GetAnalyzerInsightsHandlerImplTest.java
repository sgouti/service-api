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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.launch.GetLaunchHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.analyzer.AnalyzerInsightsRs;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAnalyzerInsightsHandlerImplTest {

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private GetLaunchHandler getLaunchHandler;

  @InjectMocks
  private GetAnalyzerInsightsHandlerImpl handler;

  @Test
  void shouldReturnEmptyInsightsPayloadWhenProjectHasNoEligibleLaunches() {
    MembershipDetails membershipDetails = new MembershipDetails();
    membershipDetails.setProjectId(1L);
    when(launchRepository.findTop20ByProjectIdAndModeNotOrderByStartTimeDescNumberDesc(1L,
        LaunchModeEnum.DEBUG)).thenReturn(List.of());

    AnalyzerInsightsRs response = handler.getLaunchInsights(membershipDetails, null, null, 10);

    assertNotNull(response);
    assertNull(response.getLaunchId());
    assertTrue(response.getRecentLaunches().isEmpty());
    assertTrue(response.getTriageAging().isEmpty());
    assertTrue(response.getReleaseAggregate().isEmpty());
    assertTrue(response.getQuarantine().isEmpty());
  }
}