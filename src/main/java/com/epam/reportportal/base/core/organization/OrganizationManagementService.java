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

package com.epam.reportportal.base.core.organization;

import com.epam.reportportal.api.model.CreateOrganizationRequest;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.UpdateOrganizationRequest;
import com.epam.reportportal.base.core.organization.settings.OrganizationSettingsHandler;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import com.epam.reportportal.base.util.SlugUtils;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves organization operations either through an installed plugin or a built-in fallback.
 */
@Service
@RequiredArgsConstructor
public class OrganizationManagementService {

  private final Pf4jPluginBox pluginBox;
  private final GetOrganizationHandler getOrganizationHandler;
  private final OrganizationRepository organizationRepository;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;
  private final OrganizationSettingsRepository organizationSettingsRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final OrganizationUserService organizationUserService;
  private final OrganizationSettingsHandler organizationSettingsHandler;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final PersonalProjectService personalProjectService;

  @Transactional
  public OrganizationInfo createOrganization(CreateOrganizationRequest request, ReportPortalUser principal) {
    return getOrgExtension()
        .map(ext -> ext.createOrganization(request, principal))
        .orElseGet(() -> createOrganizationFallback(request, principal));
  }

  @Transactional
  public OrganizationInfo createPersonalOrganization(long userId) {
    return getOrgExtension()
        .map(ext -> ext.createPersonalOrganization(userId))
        .orElseGet(() -> createPersonalOrganizationFallback(userId));
  }

  @Transactional
  public void updateOrganization(Long organizationId, UpdateOrganizationRequest request,
      ReportPortalUser principal) {
    getOrgExtension()
        .ifPresentOrElse(
            ext -> ext.updateOrganization(organizationId, request, principal),
            () -> updateOrganizationFallback(organizationId, request)
        );
  }

  @Transactional
  public void deleteOrganization(Long organizationId, ReportPortalUser principal) {
    getOrgExtension()
        .ifPresentOrElse(
            ext -> ext.deleteOrganization(organizationId, principal),
            () -> deleteOrganizationFallback(organizationId)
        );
  }

  private OrganizationInfo createOrganizationFallback(CreateOrganizationRequest request,
      ReportPortalUser principal) {
    verifyOrganizationNameIsAvailable(request.getName(), null);

    var slug = resolveRequestedSlug(request.getSlug(), request.getName());
    verifyOrganizationSlugIsAvailable(slug, null);

    var owner = userRepository.findById(principal.getUserId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, principal.getUserId()));

    var now = Instant.now();
    var organization = new Organization();
    organization.setCreatedAt(now);
    organization.setUpdatedAt(now);
    organization.setName(request.getName());
    organization.setSlug(slug);
    organization.setOwnerId(owner.getId());
    organization.setOrganizationType(resolveOrganizationType(request.getType()));

    var savedOrganization = organizationRepository.save(organization);
    organizationUserService.saveOrganizationUser(savedOrganization, owner, OrganizationRole.MANAGER.name());

    return getOrganizationHandler.getOrganizationById(savedOrganization.getId());
  }

  private OrganizationInfo createPersonalOrganizationFallback(long userId) {
    var existingOrganization = findExistingPersonalOrganization(userId);
    if (existingOrganization.isPresent()) {
      return getOrganizationHandler.getOrganizationById(existingOrganization.get().getId());
    }

    var owner = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));

    var baseName = personalProjectService.getProjectPrefix(owner.getLogin());
    var uniqueNameAndSlug = generateUniquePersonalOrganizationIdentity(baseName);

    var now = Instant.now();
    var organization = new Organization();
    organization.setCreatedAt(now);
    organization.setUpdatedAt(now);
    organization.setName(uniqueNameAndSlug.name());
    organization.setSlug(uniqueNameAndSlug.slug());
    organization.setOwnerId(owner.getId());
    organization.setOrganizationType(OrganizationType.PERSONAL);

    var savedOrganization = organizationRepository.save(organization);
    organizationUserService.saveOrganizationUser(savedOrganization, owner, OrganizationRole.MANAGER.name());

    return getOrganizationHandler.getOrganizationById(savedOrganization.getId());
  }

  private void updateOrganizationFallback(Long organizationId, UpdateOrganizationRequest request) {
    var organization = getOrganization(organizationId);
    var changed = false;

    if (StringUtils.isNotBlank(request.getName()) && !Objects.equals(request.getName(), organization.getName())) {
      verifyOrganizationNameIsAvailable(request.getName(), organizationId);
      organization.setName(request.getName());
      changed = true;
    }

    if (StringUtils.isNotBlank(request.getSlug()) && !Objects.equals(request.getSlug(), organization.getSlug())) {
      verifyOrganizationSlugIsAvailable(request.getSlug(), organizationId);
      organization.setSlug(request.getSlug());
      changed = true;
    }

    if (changed) {
      organization.setUpdatedAt(Instant.now());
      organizationRepository.save(organization);
      organizationSettingsHandler.getOrganizationSettings(organizationId);
    }
  }

  private void deleteOrganizationFallback(Long organizationId) {
    var organization = getOrganization(organizationId);

    if (!projectRepository.findAllByOrganizationId(organizationId).isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Organization contains projects and cannot be deleted.");
    }

    organizationSettingsRepository.deleteAll(organizationSettingsRepository.findByOrganizationId(organizationId));
    organizationUserRepository.deleteAllByOrganizationId(organizationId);
    organizationRepository.delete(organization);
  }

  private Organization getOrganization(Long organizationId) {
    return organizationRepositoryCustom.findById(organizationId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, organizationId));
  }

  private Optional<Organization> findExistingPersonalOrganization(long userId) {
    return organizationUserRepository.findOrganizationIdsByUserId(userId)
        .stream()
        .map(organizationRepositoryCustom::findById)
        .flatMap(Optional::stream)
        .filter(org -> OrganizationType.PERSONAL.equals(org.getOrganizationType()))
        .filter(org -> Objects.equals(org.getOwnerId(), userId))
        .findFirst();
  }

  private NameAndSlug generateUniquePersonalOrganizationIdentity(String baseName) {
    var baseSlug = SlugUtils.slug(baseName);
    var candidateName = baseName;
    var candidateSlug = baseSlug;

    for (int suffix = 1;
         organizationRepositoryCustom.findOrganizationByName(candidateName).isPresent()
             || organizationRepositoryCustom.findOrganizationBySlug(candidateSlug).isPresent();
         suffix++) {
      candidateName = baseName + "_" + suffix;
      candidateSlug = baseSlug + "-" + suffix;
    }

    return new NameAndSlug(candidateName, candidateSlug);
  }

  private void verifyOrganizationNameIsAvailable(String name, Long currentOrganizationId) {
    organizationRepositoryCustom.findOrganizationByName(name)
        .filter(organization -> !organization.getId().equals(currentOrganizationId))
        .ifPresent(organization -> {
          throw new ReportPortalException(ErrorType.RESOURCE_ALREADY_EXISTS, "organization name");
        });
  }

  private void verifyOrganizationSlugIsAvailable(String slug, Long currentOrganizationId) {
    organizationRepositoryCustom.findOrganizationBySlug(slug)
        .filter(organization -> !organization.getId().equals(currentOrganizationId))
        .ifPresent(organization -> {
          throw new ReportPortalException(ErrorType.RESOURCE_ALREADY_EXISTS, "organization slug");
        });
  }

  private String resolveRequestedSlug(String requestedSlug, String name) {
    return StringUtils.isBlank(requestedSlug) ? SlugUtils.slug(name) : requestedSlug;
  }

  private OrganizationType resolveOrganizationType(CreateOrganizationRequest.TypeEnum type) {
    var requestedType = Optional.ofNullable(type).orElse(CreateOrganizationRequest.TypeEnum.INTERNAL);
    return OrganizationType.valueOf(requestedType.getValue());
  }

  private Optional<OrganizationExtensionPoint> getOrgExtension() {
    return pluginBox.getInstance(OrganizationExtensionPoint.class);
  }

  private record NameAndSlug(String name, String slug) {
  }
}