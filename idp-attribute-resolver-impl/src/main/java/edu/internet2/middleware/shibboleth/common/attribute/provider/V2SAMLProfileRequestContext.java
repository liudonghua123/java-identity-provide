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

package edu.internet2.middleware.shibboleth.common.attribute.provider;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * Emulation code for Scripted Attributes.
 */
public class V2SAMLProfileRequestContext {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(V2SAMLProfileRequestContext.class);

    /** The Attribute Resolution Context, used to local the Principal. */
    @Nonnull private final AttributeResolutionContext resolutionContext;

    /** Attribute Id being resolved, if any. */
    @Nullable private final String id;

    /**
     * Constructor.
     * 
     * @param attributeResolutionContext the resolution context.
     * @param attributeId the id of the attribute being resolved.
     */
    public V2SAMLProfileRequestContext(@Nonnull final AttributeResolutionContext attributeResolutionContext,
            @Nullable final String attributeId) {

        resolutionContext = Constraint.isNotNull(attributeResolutionContext, "Attribute Resolution Context was null");
        id = StringSupport.trimOrNull(attributeId);
    }

    /**
     * Get the attribute ID being resolved, if available.
     * 
     * @return attribute ID
     */
    @Nullable protected String getId() {
        // Deprecation is NEW in V3.4.4
        DeprecationSupport.warnOnce(ObjectType.METHOD, "requestContext.getId()", null, null);
        return id;
    }

    /**
     * Get the name of the principal associated with this resolution.
     * 
     * @return the Principal.
     */
    public String getPrincipalName() {
        // Deprecation is NEW in V3.4.4
        DeprecationSupport.warnOnce(ObjectType.METHOD, "requestContext.getPrincipalName()", null,
                "resolutionContext.getPrincipal()");
        return resolutionContext.getPrincipal();
    }

    /**
     * Get the Entity Id associate with this attribute issuer.
     * 
     * @return the entityId.
     */
    public String getPeerEntityId() {
        // Deprecation is NEW in V3.4.4
        DeprecationSupport.warnOnce(ObjectType.METHOD, "requestContext.getPeerEntityId()",
                null,
                "resolutionContext.getAttributeRecipientID()");
        return resolutionContext.getAttributeRecipientID();
    }

    /**
     * Get the Entity Id associate with this attribute consumer.
     * 
     * @return the entityId.
     */
    public String getLocalEntityId() {
        // Deprecation is NEW in V4.0
        DeprecationSupport.warnOnce(ObjectType.METHOD,
                "requestContext.getLocalEntityId()", null,
                "resolutionContext.getAttributeIssuerID()");
        return resolutionContext.getAttributeIssuerID();
    }

    // All other methods are stubs

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public SAMLObject getInboundSAMLMessage() {
        unsupportedMethod("getInboundSAMLMessage");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getInboundSAMLMessageId() {
        unsupportedMethod("getInboundSAMLMessageId");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public DateTime getInboundSAMLMessageIssueInstant() {
        unsupportedMethod("getInboundSAMLMessageIssueInstant");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getInboundSAMLProtocol() {
        unsupportedMethod("getInboundSAMLProtocol");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public EntityDescriptor getLocalEntityMetadata() {
        unsupportedMethod("getLocalEntityMetadata");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public QName getLocalEntityRole() {
        unsupportedMethod("getLocalEntityRole");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public RoleDescriptor getLocalEntityRoleMetadata() {
        unsupportedMethod("getLocalEntityRoleMetadata");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public MetadataResolver getMetadataResolver() {
        unsupportedMethod("getMetadataResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getOutboundSAMLMessageSigningCredential() {
        unsupportedMethod("getOutboundSAMLMessageSigningCredential");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public byte[] getOutboundMessageArtifactType() {
        unsupportedMethod("getOutboundMessageArtifactType");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public SAMLObject getOutboundSAMLMessage() {
        unsupportedMethod("getOutboundSAMLMessage");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getOutboundSAMLMessageId() {
        unsupportedMethod("getOutboundSAMLMessageId");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public DateTime getOutboundSAMLMessageIssueInstant() {
        unsupportedMethod("getOutboundSAMLMessageIssueInstant");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getOutboundSAMLProtocol() {
        unsupportedMethod("getOutboundSAMLProtocol");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Endpoint getPeerEntityEndpoint() {
        unsupportedMethod("getPeerEntityEndpoint");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public EntityDescriptor getPeerEntityMetadata() {
        unsupportedMethod("getPeerEntityMetadata");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public QName getPeerEntityRole() {
        unsupportedMethod("getPeerEntityRole");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public RoleDescriptor getPeerEntityRoleMetadata() {
        unsupportedMethod("getPeerEntityRoleMetadata");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getRelayState() {
        unsupportedMethod("getRelayState");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public SAMLObject getSubjectNameIdentifier() {
        unsupportedMethod("getSubjectNameIdentifier");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public boolean isInboundSAMLMessageAuthenticated() {
        unsupportedMethod("isInboundSAMLMessageAuthenticated");
        return false;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLMessage(final SAMLObject param) {
        unsupportedMethod("setInboundSAMLMessage");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLMessageAuthenticated(final boolean param) {
        unsupportedMethod("setInboundSAMLMessageAuthenticated");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLMessageId(final String param) {
        unsupportedMethod("setInboundSAMLMessageId");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLMessageIssueInstant(final DateTime param) {
        unsupportedMethod("setInboundSAMLMessageIssueInstant");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundSAMLProtocol(final String param) {
        unsupportedMethod("setInboundSAMLProtocol");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setLocalEntityId(final String param) {
        unsupportedMethod("setLocalEntityId");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setLocalEntityMetadata(final EntityDescriptor param) {
        unsupportedMethod("setLocalEntityMetadata");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setLocalEntityRole(final QName param) {
        unsupportedMethod("setLocalEntityRole");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setLocalEntityRoleMetadata(final RoleDescriptor param) {
        unsupportedMethod("setLocalEntityRoleMetadata");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setMetadataResolver(final Object param) {
        unsupportedMethod("setMetadataResolver");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundMessageArtifactType(final byte[] param) {
        unsupportedMethod("setOutboundMessageArtifactType");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLMessage(final SAMLObject param) {
        unsupportedMethod("setOutboundSAMLMessage");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLMessageId(final String param) {
        unsupportedMethod("setOutboundSAMLMessageId");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLMessageIssueInstant(final DateTime param) {
        unsupportedMethod("setOutboundSAMLMessageIssueInstant");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLMessageSigningCredential(final Object param) {
        unsupportedMethod("setOutboundSAMLMessageSigningCredential");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundSAMLProtocol(final String param) {
        unsupportedMethod("setOutboundSAMLProtocol");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityEndpoint(final Endpoint param) {
        unsupportedMethod("setPeerEntityEndpoint");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityId(final String param) {
        unsupportedMethod("setPeerEntityId");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityMetadata(final EntityDescriptor param) {
        unsupportedMethod("setPeerEntityMetadata");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityRole(final QName param) {
        unsupportedMethod("setPeerEntityRole");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPeerEntityRoleMetadata(final RoleDescriptor param) {
        unsupportedMethod("setPeerEntityRoleMetadata");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setRelayState(final String param) {
        unsupportedMethod("setRelayState");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setSubjectNameIdentifier(final SAMLObject param) {
        unsupportedMethod("setSubjectNameIdentifier");

    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getCommunicationProfileId() {
        unsupportedMethod("getCommunicationProfileId");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public XMLObject getInboundMessage() {
        unsupportedMethod("getInboundMessage");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getInboundMessageIssuer() {
        unsupportedMethod("getInboundMessageIssuer");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getInboundMessageTransport() {
        unsupportedMethod("getInboundMessageTransport");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public XMLObject getOutboundMessage() {
        unsupportedMethod("getOutboundMessage");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getOutboundMessageIssuer() {
        unsupportedMethod("getOutboundMessageIssuer");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getOutboundMessageTransport() {
        unsupportedMethod("getOutboundMessageTransport");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getSecurityPolicyResolver() {
        unsupportedMethod("getSecurityPolicyResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public boolean isIssuerAuthenticated() {
        unsupportedMethod("isIssuerAuthenticated");
        return false;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setCommunicationProfileId(final String param) {
        unsupportedMethod("setCommunicationProfileId");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundMessage(final XMLObject param) {
        unsupportedMethod("setInboundMessage");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundMessageIssuer(final String param) {
        unsupportedMethod("setInboundMessageIssuer");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setInboundMessageTransport(final Object param) {
        unsupportedMethod("setInboundMessageTransport");

    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundMessage(final XMLObject param) {
        unsupportedMethod("setOutboundMessage");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundMessageIssuer(final String param) {
        unsupportedMethod("setOutboundMessageIssuer");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundMessageTransport(final Object param) {
        unsupportedMethod("setOutboundMessageTransport");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setSecurityPolicyResolver(final Object param) {
        unsupportedMethod("setSecurityPolicyResolver");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getPreSecurityInboundHandlerChainResolver() {
        unsupportedMethod("getPreSecurityInboundHandlerChainResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPreSecurityInboundHandlerChainResolver(final Object param) {
        unsupportedMethod("setPreSecurityInboundHandlerChainResolver");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getPostSecurityInboundHandlerChainResolver() {
        unsupportedMethod("getPostSecurityInboundHandlerChainResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPostSecurityInboundHandlerChainResolver(final Object param) {
        unsupportedMethod("setPostSecurityInboundHandlerChainResolver");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getOutboundHandlerChainResolver() {
        unsupportedMethod("getOutboundHandlerChainResolver");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setOutboundHandlerChainResolver(final Object param) {
        unsupportedMethod("setOutboundHandlerChainResolver");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getProfileConfiguration() {
        unsupportedMethod("getProfileConfiguration");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getRelyingPartyConfiguration() {
        unsupportedMethod("getRelyingPartyConfiguration");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Object getUserSession() {
        unsupportedMethod("getUserSession");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setProfileConfiguration(final Object param) {
        unsupportedMethod("setProfileConfiguration");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setRelyingPartyConfiguration(final Object param) {
        unsupportedMethod("setRelyingPartyConfiguration");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setUserSession(final Object param) {
        unsupportedMethod("setUserSession");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Collection getReleasedAttributes() {
        unsupportedMethod("getReleasedAttributes");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setReleasedAttributes(final Collection param) {
        unsupportedMethod("setReleasedAttributes");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Collection<String> getRequestedAttributesIds() {
        unsupportedMethod("getRequestedAttributesIds");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setRequestedAttributes(final Collection<String> param) {
        unsupportedMethod("setRequestedAttributes");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public Map<String, Object> getAttributes() {
        unsupportedMethod("getAttributes");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setAttributes(final Map<String, Object> param) {
        unsupportedMethod("setAttributes");
    }

    /**
     * Stubbed failing function.
     * 
     * @return null
     */
    public String getPrincipalAuthenticationMethod() {
        unsupportedMethod("getPrincipalAuthenticationMethod");
        return null;
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPrincipalAuthenticationMethod(final String param) {
        unsupportedMethod("setPrincipalAuthenticationMethod");
    }

    /**
     * Stubbed failing function.
     * 
     * @param param ignored.
     */
    public void setPrincipalName(final String param) {
        unsupportedMethod("setPrincipalName");

    }

    /**
     * Emit an appropriate message when an unsupported method is called.
     * 
     * @param method the method
     */
    protected void unsupportedMethod(@Nonnull final String method) {
        if (null == getId()) {
            log.error("Template definition referenced unsupported method {}", method);
        } else {
            log.error("AttributeDefinition: '{}' called unsupported method {}", getId(), method);
        }
    }

    /** {@inheritDoc}. */
    @Override public String toString() {
        return MoreObjects.toStringHelper(V2SAMLProfileRequestContext.class).add("Id", getId())
                .add("PrincipalName", getPrincipalName()).add("PeerEntityId", getPeerEntityId())
                .add("LocalEntityId", getLocalEntityId()).toString();
    }
}
