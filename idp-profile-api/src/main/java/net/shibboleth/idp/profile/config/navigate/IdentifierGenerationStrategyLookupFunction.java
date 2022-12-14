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

package net.shibboleth.idp.profile.config.navigate;

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.AbstractRelyingPartyLookupFunction;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;
import net.shibboleth.shared.security.IdentifierGenerationStrategy;

import org.opensaml.profile.context.ProfileRequestContext;

/**
 * A function that returns an {@link IdentifierGenerationStrategy} by way of a {@link RelyingPartyContext}
 * obtained via a lookup function, by default a child of the {@link ProfileRequestContext}.
 * 
 * <p>If a specific setting is unavailable, a default generator can be returned.</p>
 */
public class IdentifierGenerationStrategyLookupFunction
        extends AbstractRelyingPartyLookupFunction<IdentifierGenerationStrategy> {
    
    /** A resolver for default security configurations. */
    @Nullable private RelyingPartyConfigurationResolver rpResolver;

    /**
     * Set the resolver for default security configurations.
     * 
     * @param resolver the resolver to use
     */
    public void setRelyingPartyConfigurationResolver(@Nullable final RelyingPartyConfigurationResolver resolver) {
        rpResolver = resolver;
    }

    /** Default strategy to return. */
    @Nullable private IdentifierGenerationStrategy defaultGenerator;
    
    /**
     * Set the default {@link IdentifierGenerationStrategy} to return.
     * 
     * @param strategy  default generation strategy;
     */
    public void setDefaultIdentifierGenerationStrategy(@Nullable final IdentifierGenerationStrategy strategy) {
        defaultGenerator = strategy;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public IdentifierGenerationStrategy apply(@Nullable final ProfileRequestContext input) {
        if (input != null) {
            final RelyingPartyContext rpc = getRelyingPartyContextLookupStrategy().apply(input);
            if (rpc != null) {
                final ProfileConfiguration pc = rpc.getProfileConfig();
                if (pc != null) {
                    final SecurityConfiguration sc = pc.getSecurityConfiguration(input);
                    if (sc != null) {
                        return sc.getIdGenerator();
                    }
                }
            }
        }

        // Check for a per-profile default (relying party independent) config.
        if (input != null && rpResolver != null) {
            final SecurityConfiguration defaultConfig =
                    rpResolver.getDefaultSecurityConfiguration(input.getProfileId());
            if (defaultConfig != null && defaultConfig.getIdGenerator() != null) {
                return defaultConfig.getIdGenerator();
            }
        }

        return defaultGenerator;
    }

}