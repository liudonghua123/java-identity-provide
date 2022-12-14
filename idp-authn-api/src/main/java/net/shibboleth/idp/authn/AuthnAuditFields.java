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

package net.shibboleth.idp.authn;

import javax.annotation.Nonnull;

import net.shibboleth.shared.annotation.constraint.NotEmpty;

/**
 * Constants to use for audit logging fields stored in an {@link net.shibboleth.idp.profile.context.AuditContext}.
 */
public final class AuthnAuditFields {

    /** Requested principal(s) operator field. @since 4.0.0 */
    @Nonnull @NotEmpty public static final String REQ_PRINC_OP = "ROP";

    /** Requested principal(s) field. @since 4.0.0 */
    @Nonnull @NotEmpty public static final String REQ_PRINC = "RPRIN";

    /** Authentication flow ID field. */
    @Nonnull @NotEmpty public static final String AUTHN_FLOW_ID = "AF";

    /** SSO indicator signaling authentication was not "freshly" performed. */
    @Nonnull @NotEmpty public static final String SSO = "SSO";
    
    /** Constructor. */
    private AuthnAuditFields() {

    }

}