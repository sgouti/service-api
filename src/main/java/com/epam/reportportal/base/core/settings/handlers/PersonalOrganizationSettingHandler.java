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

package com.epam.reportportal.base.core.settings.handlers;

import com.epam.reportportal.base.core.settings.ServerSettingHandler;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handler for the "personal organization" server setting.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Slf4j
@Service
public class PersonalOrganizationSettingHandler implements ServerSettingHandler {

  public static final String PERSONAL_ORGANIZATION_SETTINGS_KEY = "server.features.personal-organization.enabled";

  @Override
  public void handle(String value) {
    var enabled = Optional.ofNullable(value)
        .filter(v -> "true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v))
        .map(Boolean::parseBoolean)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.BAD_REQUEST_ERROR,
            "Invalid boolean value for personal organization setting: " + value
        ));

    log.info("Personal organization setting is set to '{}'", enabled);
  }

  @Override
  public String getKey() {
    return PERSONAL_ORGANIZATION_SETTINGS_KEY;
  }
}
