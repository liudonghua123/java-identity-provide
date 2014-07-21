/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.relyingparty.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.ext.spring.service.AbstractServiceableComponent;
import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.idp.profile.logic.impl.AnonymousProfilePredicate;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.IdentifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Retrieves a per-relying party configuration for a given profile request based on the request context.
 * 
 * <p>
 * Note that this resolver does not permit more than one {@link RelyingPartyConfiguration} with the same ID.
 * </p>
 */
public class DefaultRelyingPartyConfigurationResolver extends
        AbstractServiceableComponent<RelyingPartyConfigurationResolver> implements RelyingPartyConfigurationResolver,
        IdentifiableComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(DefaultRelyingPartyConfigurationResolver.class);

    /** Registered relying party configurations. */
    @Nonnull private List<RelyingPartyConfiguration> rpConfigurations;

    /** Default relying party configurations, called if no other configuration matches. */
    @NonnullAfterInit private RelyingPartyConfiguration defaultConfiguration;

    /** Anonymous relying party configurations, called if the profile is Anonymous. */
    @Nullable private RelyingPartyConfiguration anonymousConfiguration;

    /** The predicate which decides if this context is "Anonymous". */
    @NonnullAfterInit private Predicate<ProfileRequestContext> isAnonymousPredicate;

    /** The map from profile ID to {@link SecurityConfiguration}. */
    @Nonnull @NonnullElements private Map<String,SecurityConfiguration> securityConfigurationMap;
    
    /** A global default security configuration. */
    @Nullable private SecurityConfiguration defaultSecurityConfiguration;

    /** Constructor. */
    public DefaultRelyingPartyConfigurationResolver() {
        rpConfigurations = Collections.emptyList();
        isAnonymousPredicate = new AnonymousProfilePredicate();
        securityConfigurationMap = Collections.emptyMap();
    }

    /**
     * Get an unmodifiable list of registered relying party configurations.
     * 
     * @return unmodifiable list of registered relying party configurations
     */
    @Nonnull @NonnullElements @Unmodifiable @NotLive public List<RelyingPartyConfiguration>
            getRelyingPartyConfigurations() {
        return ImmutableList.copyOf(rpConfigurations);
    }

    /**
     * Set the registered relying party configurations.
     * 
     * This property may not be changed after the resolver is initialized.
     * 
     * @param configs list of registered relying party configurations
     */
    public void setRelyingPartyConfigurations(@Nonnull @NonnullElements final List<RelyingPartyConfiguration> configs) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(configs, "RelyingPartyConfiguration collection cannot be null");

        rpConfigurations = Lists.newArrayList(Collections2.filter(configs, Predicates.notNull()));
    }

    /**
     * Get the {@link RelyingPartyConfiguration} to use if no other configuration is acceptable.
     * 
     * @return Returns the defaultConfiguration.
     */
    @NonnullAfterInit public RelyingPartyConfiguration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    /**
     * Set the {@link RelyingPartyConfiguration} to use if no other configuration is acceptable.
     * 
     * @param configuration The defaultConfiguration to set.
     */
    public void setDefaultConfiguration(@Nonnull final RelyingPartyConfiguration configuration) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        defaultConfiguration = Constraint.isNotNull(configuration, "Default RP configuration cannot be null");
    }

    /**
     * Get the {@link RelyingPartyConfiguration} to use if the configuration is found to be "anonymous" (via the call to
     * the {@link #isAnonymousPredicate}.
     * 
     * @return Returns the anonymousConfiguration.
     */
    @NonnullAfterInit public RelyingPartyConfiguration getAnonymousConfiguration() {
        return anonymousConfiguration;
    }

    /**
     * Set the {@link RelyingPartyConfiguration} to use if the configuration is found to be "anonymous" (via the call to
     * the {@link #isAnonymousPredicate}.
     * 
     * @param configuration The anonymousConfiguration to set.
     */
    public void setAnonymousConfiguration(@Nonnull final RelyingPartyConfiguration configuration) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        anonymousConfiguration = Constraint.isNotNull(configuration, "Anonymous RP configuration cannot be null");
    }

    /**
     * Get the definition of what an anonymous Profile is.
     * 
     * @return the Ppredicate
     */
    @Nonnull public Predicate<ProfileRequestContext> isAnonymousPredicate() {
        return isAnonymousPredicate;
    }

    /**
     * Set the definition of what an anonymous Profile is.
     * 
     * @param predicate the predicate to set
     */
    public void setIsAnonymousPredicate(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        isAnonymousPredicate = Constraint.isNotNull(predicate, "Anonymous profile predicate cannot be null");
    }

    /**
     * Get the map we use to look up default configuration.
     * 
     * @return Returns the Map.
     */
    @Nonnull @NonnullElements @Unmodifiable @NotLive public Map<String,SecurityConfiguration>
            getSecurityConfigurationMap() {
        return ImmutableMap.copyOf(securityConfigurationMap);
    }

    /**
     * Set the map we use to look up default configuration.
     * 
     * @param map what to set.
     */
    public void setSecurityConfigurationMap(@Nonnull @NonnullElements final Map<String,SecurityConfiguration> map) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(map, "SecurityConfiguration map cannot be null");
        
        securityConfigurationMap = Maps.newHashMapWithExpectedSize(map.size());
        for (final Map.Entry<String,SecurityConfiguration> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                final String trimmed = StringSupport.trimOrNull(entry.getKey());
                if (trimmed != null) {
                    securityConfigurationMap.put(trimmed, entry.getValue());
                }
            }
        }
    }
    
    /**
     * Set the global default {@link SecurityConfiguration}.
     * 
     * @param config  global default
     */
    public void setDefaultSecurityConfiguration(@Nullable final SecurityConfiguration config) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        defaultSecurityConfiguration = config;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        final HashSet<String> configIds = Sets.newHashSetWithExpectedSize(rpConfigurations.size());
        for (final RelyingPartyConfiguration config : rpConfigurations) {
            if (configIds.contains(config.getId())) {
                throw new ComponentInitializationException("Multiple replying party configurations with ID "
                        + config.getId() + " detected. Configuration IDs must be unique.");
            }
            configIds.add(config.getId());
        }
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Iterable<RelyingPartyConfiguration> resolve(
            @Nullable final ProfileRequestContext context) throws ResolverException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if (context == null) {
            return Collections.emptyList();
        }

        log.debug("Resolving relying party configurations");
        if (isAnonymousPredicate.apply(context) && null != getAnonymousConfiguration()) {
            log.debug("Profile Request is anonymous: returning configuration {} only", getAnonymousConfiguration()
                    .getId());
            return Collections.singleton(getAnonymousConfiguration());
        }

        final ArrayList<RelyingPartyConfiguration> matches = Lists.newArrayList();

        for (final RelyingPartyConfiguration configuration : rpConfigurations) {
            log.debug("Checking if relying party configuration {} is applicable", configuration.getId());
            if (configuration.apply(context)) {
                log.debug("Relying party configuration {} is applicable", configuration.getId());
                matches.add(configuration);
            } else {
                log.debug("Relying party configuration {} is not applicable", configuration.getId());
            }
        }

        if (matches.isEmpty()) {
            log.debug("No matching Relying Party Configuration found, returning the default configuration {}",
                    getDefaultConfiguration().getId());
            return Collections.singleton(getDefaultConfiguration());
        }
        return matches;
    }

    /** {@inheritDoc} */
    @Override @Nullable public RelyingPartyConfiguration resolveSingle(@Nullable final ProfileRequestContext context)
            throws ResolverException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if (context == null) {
            return null;
        }
        if (isAnonymousPredicate.apply(context) && null != getAnonymousConfiguration()) {
            log.debug("Profile Request is anonymous: returning configuration {} only", getAnonymousConfiguration()
                    .getId());
            return getAnonymousConfiguration();
        }

        log.debug("Resolving relying party configuration");
        for (RelyingPartyConfiguration configuration : rpConfigurations) {
            log.debug("Checking if relying party configuration {} is applicable", configuration.getId());
            if (configuration.apply(context)) {
                log.debug("Relying party configuration {} is applicable", configuration.getId());
                return configuration;
            } else {
                log.debug("Relying party configuration {} is not applicable", configuration.getId());
            }
        }

        log.debug("No relying party configurations are applicable, returning the default configuration {}",
                getDefaultConfiguration().getId());
        return getDefaultConfiguration();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public SecurityConfiguration getDefaultSecurityConfiguration(@Nonnull @NotEmpty String profileId) {
        final SecurityConfiguration config = securityConfigurationMap.get(profileId);
        return config != null ? config : defaultSecurityConfiguration;
    }

    /**
     * {@inheritDoc}. This is an {@link IdentifiableComponent).
     */
    @Override public void setId(@Nonnull String componentId) {
        super.setId(componentId);
    }

    /** {@inheritDoc}. This is service is a {@link net.shibboleth.utilities.java.support.service.ServiceableComponent}. */
    @Override @Nonnull public RelyingPartyConfigurationResolver getComponent() {
        return this;
    }
}