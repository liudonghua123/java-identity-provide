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

package net.shibboleth.idp.saml.impl.profile.config.navigate;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.profile.config.SAMLProfileConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableList;

/**
 * A function that returns the effective audience restrictions to include in assertions,
 * based on combining a relying party's entityID with the result of 
 * {@link SAMLProfileConfiguration#getAdditionalAudiencesForAssertion()},
 * if such a profile is available from a {@link RelyingPartyContext} obtained via a lookup function,
 * by default a child of the {@link ProfileRequestContext}.
 * 
 * <p>If a specific setting is unavailable, no values are returned.</p>
 */
public class AudienceRestrictionsLookupFunction implements Function<ProfileRequestContext, Collection<String>> {

    /**
     * Strategy used to locate the {@link RelyingPartyContext} associated with a given {@link ProfileRequestContext}.
     */
    @Nonnull private Function<ProfileRequestContext, RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** Constructor. */
    public AudienceRestrictionsLookupFunction() {
        relyingPartyContextLookupStrategy =
                new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class);
    }

    /**
     * Sets the strategy used to locate the {@link RelyingPartyContext} associated with a given
     * {@link ProfileRequestContext}.
     * 
     * @param strategy strategy used to locate the {@link RelyingPartyContext} associated with a given
     *            {@link ProfileRequestContext}
     */
    public synchronized void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, RelyingPartyContext> strategy) {
        relyingPartyContextLookupStrategy =
                Constraint.isNotNull(strategy, "RelyingPartyContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nullable @NonnullElements @NotLive @Unmodifiable public Collection<String> apply(
            @Nullable final ProfileRequestContext input) {
        if (input != null) {
            final RelyingPartyContext rpc = relyingPartyContextLookupStrategy.apply(input);
            if (rpc != null) {
                final String id = rpc.getRelyingPartyId();
                final ProfileConfiguration pc = rpc.getProfileConfig();
                if (pc != null && pc instanceof SAMLProfileConfiguration
                        && !((SAMLProfileConfiguration) pc).getAdditionalAudiencesForAssertion().isEmpty()) {
                    final Builder builder = ImmutableList.builder();
                    if (id != null) {
                        builder.add(rpc.getRelyingPartyId());
                    }
                    builder.add(((SAMLProfileConfiguration) pc).getAdditionalAudiencesForAssertion());
                    return builder.build();
                } else if (id != null) {
                    return ImmutableList.<String>of(rpc.getRelyingPartyId());
                }
            }
        }
        
        return Collections.emptyList();
    }

}