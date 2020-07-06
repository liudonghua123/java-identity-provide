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

package net.shibboleth.idp.attribute.filter.policyrule.saml.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.policyrule.impl.AbstractPolicyRule;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Checks if the attribute issuer supports the required NameID format. */
public abstract class AbstractNameIDFormatExactPolicyRule extends AbstractPolicyRule {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractNameIDFormatExactPolicyRule.class);

    /** The NameID format that needs to be supported by the entity. */
    @NonnullAfterInit @NotEmpty private String nameIdFormat;

    /**
     * Get the NameID format that needs to be supported by the entity.
     * 
     * @return NameID format that needs to be supported by the entity
     */
    @NonnullAfterInit @NotEmpty public String getNameIdFormat() {
        return nameIdFormat;
    }

    /**
     * Sets the NameID format that needs to be supported by the entity.
     * 
     * @param format NameID format that needs to be supported by the entity
     */
    public void setNameIdFormat(@Nullable final String format) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        nameIdFormat = StringSupport.trimOrNull(format);
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (null == nameIdFormat) {
            throw new ComponentInitializationException(getLogPrefix() + " No NameID format specified");
        }
    }

    /**
     * Gets the SSO role descriptor for the entity to be checked.
     * 
     * @param filterContext current filtering context
     * 
     * @return the SSO role descriptor of the entity or null if the entity does not have such a descriptor
     */
    @Nullable protected abstract SSODescriptor getEntitySSODescriptor(
            @Nonnull final AttributeFilterContext filterContext);

    /**
     * Checks to see if the metadata for the entity supports the required NameID format.
     * 
     * @param filterContext current filter context
     * 
     * @return true if the entity supports the required NameID format, false otherwise
     *         {@inheritDoc}
     */
    @Override
    public Tristate matches(@Nonnull final AttributeFilterContext filterContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        final SSODescriptor role = getEntitySSODescriptor(filterContext);
        if (role == null) {
            // logged in above
            return Tristate.FALSE;
        }

        final List<NameIDFormat> supportedFormats = role.getNameIDFormats();
        if (supportedFormats == null || supportedFormats.isEmpty()) {
            log.debug("{} Entity SSO role descriptor does not list any supported NameID formats", getLogPrefix());
            return Tristate.FALSE;
        }

        for (final NameIDFormat supportedFormat : supportedFormats) {
            if (nameIdFormat.equals(supportedFormat.getURI())) {
                log.debug("{} Entity does support the NameID format '{}'", getLogPrefix(), nameIdFormat);
                return Tristate.TRUE;
            }
        }

        log.debug("{} Entity does not support the NameID format '{}'", getLogPrefix(), nameIdFormat);
        return Tristate.FALSE;
    }

}