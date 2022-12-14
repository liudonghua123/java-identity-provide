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

package net.shibboleth.idp.profile.config;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.NotLive;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.component.IdentifiedComponent;

/** Represents the configuration of a particular communication profile. */
public interface ProfileConfiguration extends IdentifiedComponent {

    /**
     * Get an ordered list of interceptor flows to run for this profile after an inbound message is
     * decoded.
     * 
     * <p>The flow IDs returned MUST NOT contain the
     * {@link net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor#FLOW_ID_PREFIX}
     * prefix common to all interceptor flows.</p>
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return  a set of interceptor flow IDs to enable
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable List<String> getInboundInterceptorFlows(
            @Nullable final ProfileRequestContext profileRequestContext);

    /**
     * Get an ordered list of interceptor flows to run for this profile before a final outbound
     * message is generated.
     * 
     * <p>The flow IDs returned MUST NOT contain the
     * {@link net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor#FLOW_ID_PREFIX}
     * prefix common to all interceptor flows.</p>
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return  a set of interceptor flow IDs to enable
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable List<String> getOutboundInterceptorFlows(
            @Nullable final ProfileRequestContext profileRequestContext);
    
    /**
     * Get the {@link SecurityConfiguration} to use with this profile.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return security configuration to use with this profile
     */
    @Nullable SecurityConfiguration getSecurityConfiguration(
            @Nullable final ProfileRequestContext profileRequestContext);
    
}