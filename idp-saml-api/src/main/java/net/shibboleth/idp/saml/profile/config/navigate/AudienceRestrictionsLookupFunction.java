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

package net.shibboleth.idp.saml.profile.config.navigate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.AbstractRelyingPartyLookupFunction;
import net.shibboleth.idp.saml.profile.config.SAMLProfileConfiguration;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.NotLive;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableList;

/**
 * A function that returns the effective audience restrictions to include in assertions,
 * based on combining a relying party's entityID with the result of 
 * {@link SAMLProfileConfiguration#getAdditionalAudiencesForAssertion(ProfileRequestContext)},
 * if such a profile is available from a {@link RelyingPartyContext} obtained via a lookup function,
 * by default a child of the {@link ProfileRequestContext}.
 * 
 * <p>If a specific setting is unavailable, no values are returned.</p>
 */
public class AudienceRestrictionsLookupFunction extends AbstractRelyingPartyLookupFunction<Collection<String>> {

    /** {@inheritDoc} */
    @Nullable @NonnullElements @NotLive @Unmodifiable public Collection<String> apply(
            @Nullable final ProfileRequestContext input) {
        final RelyingPartyContext rpc = getRelyingPartyContextLookupStrategy().apply(input);
        if (rpc != null) {
            final String id = rpc.getRelyingPartyId();
            final ProfileConfiguration pc = rpc.getProfileConfig();
            if (pc != null && pc instanceof SAMLProfileConfiguration) {
                final Set<String> additional =
                        ((SAMLProfileConfiguration) pc).getAdditionalAudiencesForAssertion(input);
                if (!additional.isEmpty()) {
                    final Builder<String> builder = ImmutableList.builder();
                    if (id != null) {
                        builder.add(rpc.getRelyingPartyId());
                    }
                    builder.addAll(additional);
                    return builder.build();
                }
            }
            
            if (id != null) {
                return List.of(rpc.getRelyingPartyId());
            }
        }
        
        return Collections.emptyList();
    }

}