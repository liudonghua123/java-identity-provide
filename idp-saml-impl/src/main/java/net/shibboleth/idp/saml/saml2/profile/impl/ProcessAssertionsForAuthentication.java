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

package net.shibboleth.idp.saml.saml2.profile.impl;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.common.assertion.ValidationProcessingData;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.opensaml.saml.saml2.assertion.SAML2AssertionValidationParameters;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Perform processing of SAML 2 Assertions that have been validated by earlier actions
 * for use in finalization of SAML-based authentication by later actions. 
 */
public class ProcessAssertionsForAuthentication extends AbstractAuthenticationAction {
    
    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(ProcessAssertionsForAuthentication.class);
    
    /** The resolver for the list of assertions to be processed. */
    @Nonnull private Function<ProfileRequestContext, List<Assertion>> assertionResolver;
    
    /** Lookup strategy to locate the SAML context. */
    @Nonnull private Function<ProfileRequestContext,SAMLAuthnContext> samlContextLookupStrategy;
    
    /** Selection strategy for multiple valid authn Assertions. */
    @Nonnull private Function<List<Assertion>,Assertion> authnAssertionSelectionStrategy;
    
    /** Selection strategy for multiple AuthnStatements. */
    @Nonnull private Function<Assertion,AuthnStatement> authnStatementSelectionStrategy;
    
    /** The list of initial candidate Assertions to process. */
    private List<Assertion> candidates;
    
    /** The SAML authentication context. */
    private SAMLAuthnContext samlAuthnContext;
    
    /**
     * Constructor.
     */
    public ProcessAssertionsForAuthentication() {
        super();
        
        assertionResolver = new DefaultAssertionResolver().compose(
                new ChildContextLookup<>(ProfileRequestContext.class).compose(
                        new ChildContextLookup<>(AuthenticationContext.class)));
        
        // PRC -> AC -> SAMLAuthnContext
        samlContextLookupStrategy = new ChildContextLookup<>(SAMLAuthnContext.class).compose(
                new ChildContextLookup<>(AuthenticationContext.class));
        
        //TODO replace with better default logic based on Scott review and SP behavior
        authnAssertionSelectionStrategy = assertions -> {
            return assertions.get(0);
        };
            
        //TODO replace with better default logic based on Scott review and SP behavior 
        authnStatementSelectionStrategy = assertion -> {
            return assertion.getAuthnStatements().get(0);
        };
    }

    /**
     * Set the strategy function for selecting which of multiple valid Assertions to use.
     * 
     * @param strategy the new strategy function
     */
    public void setAuthnAssertionSelectionStrategy(@Nonnull final Function<List<Assertion>, Assertion> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        authnAssertionSelectionStrategy = Constraint.isNotNull(strategy, 
                "The Assertion selection strategy may not be null");
    }
    
    /**
     * Set the strategy function for selecting which of multiple AuthnStatements to use.
     * 
     * @param strategy the new strategy function
     */
    public void setAuthnStatementSelectionStrategy(@Nonnull final Function<Assertion, AuthnStatement> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        authnStatementSelectionStrategy = Constraint.isNotNull(strategy, 
                "The AuthnStatement selection strategy may not be null");
    }
    
    /**
     * Set the strategy function which resolves the list of assertions to process.
     * 
     * @param strategy the new strategy function
     */
    public void setAssertionResolver(@Nonnull final Function<ProfileRequestContext, List<Assertion>> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        assertionResolver = Constraint.isNotNull(strategy, "The Assertion resolver strategy may not be null");
    }
    
    /**
     * Set the lookup strategy used to locate the {@link SAMLAuthnContext}.
     * 
     * @param strategy the new strategy function
     */
    public void setSAMLAuthnContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,SAMLAuthnContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        samlContextLookupStrategy = Constraint.isNotNull(strategy, "SAMLAuthnContext lookup strategy may not be null");
    }

    /** {@inheritDoc} */
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            return false;
        }

        candidates = assertionResolver.apply(profileRequestContext);
        if (candidates == null || candidates.isEmpty()) {
            log.info("{} Profile context contained no candidate Assertions to process. Skipping further processing",
                    getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
            return false;
        }
        
        samlAuthnContext = samlContextLookupStrategy.apply(profileRequestContext);
        if (samlAuthnContext == null) {
            log.debug("{} No SAMLAuthnContext available within authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        // Select only valid Assertions which contain at least 1 AuthnStatement and a confirmed Subject
        final Predicate<Assertion> selector = new AssertionIsValid()
                .and(new AssertionContainsAuthenticationStatement())
                .and(new AssertionContainsConfirmedSubject());
        
        final List<Assertion> assertions = candidates.stream().filter(selector).collect(Collectors.toList());
        if (assertions.isEmpty()) {
            log.debug("{} No valid SAML Assertions meeting the criteria for authentication were found", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
            return;
        }
        
        Assertion authnAssertion = null;
        if (assertions.size() == 1) {
            authnAssertion = assertions.get(0);
            log.debug("{} Saw single valid SAML Assertion, selecting for authentication", getLogPrefix());
        } else {
            log.debug("{} Attempting to select from multiple valid SAML Assertions for authentication", getLogPrefix());
            authnAssertion = authnAssertionSelectionStrategy.apply(assertions);
        }
        if (authnAssertion == null) {
            log.debug("{} Could not select a single valid SAML Assertion for authentication", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
            return;
        }
        
        log.debug("{} Selected valid SAML Assertion for authentication: {}", getLogPrefix(), authnAssertion.getID());
        
        AuthnStatement authnStatement = null;
        if (authnAssertion.getAuthnStatements().size() == 1) {
            authnStatement = authnAssertion.getAuthnStatements().get(0);
            log.debug("{} Saw single AuthnStatement, selecting for authentication", getLogPrefix());
        } else {
            log.debug("{} Attempting to select from multiple AuthnStatements for authentication", getLogPrefix());
            authnStatement = authnStatementSelectionStrategy.apply(authnAssertion);
            if (authnStatement == null) {
                log.debug("{} Could not select a single AuthnStatement for authentication", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
                return;
            }
        }
        
        samlAuthnContext.setAuthnStatement(authnStatement);
        samlAuthnContext.setSubject(authnAssertion.getSubject());
    }

    /**
     * The default assertion resolver function. NOTE: this is relative to the nested profile request context.
     * Need to compose with other lookup function against the main/outer profile request context.
     */
    private class DefaultAssertionResolver implements Function<ProfileRequestContext, List<Assertion>> {

        /** {@inheritDoc} */
        public List<Assertion> apply(@Nonnull final ProfileRequestContext profileContext) {
            final SAMLObject message = (SAMLObject) profileContext.getInboundMessageContext().getMessage();
            if (message instanceof Response) {
                return ((Response) message).getAssertions();
            }
            
            return null;
        }
        
    }
    
    /**
     * Predicate for valid assertions.
     */
    private class AssertionIsValid implements Predicate<Assertion> {

        /** {@inheritDoc} */
        public boolean test(@Nullable final Assertion assertion) {
            if (assertion == null) {
                return false;
            }
            
            final Optional<ValidationProcessingData> validationData = assertion.getObjectMetadata()
                    .get(ValidationProcessingData.class).stream().findFirst();
            if (validationData.isEmpty()) {
                return false;
            }
            
            return validationData.get().getResult() == ValidationResult.VALID;
        }
        
    }
    
    /**
     * Predicate for assertions containing at least 1 AuthenticationStatement.
     */
    private class AssertionContainsAuthenticationStatement implements Predicate<Assertion> {

        /** {@inheritDoc} */
        public boolean test(@Nullable final Assertion assertion) {
            if (assertion == null) {
                return false;
            }
            
            return ! assertion.getAuthnStatements().isEmpty();
        }
        
    }

    /**
     * Predicate for assertions which have been validated and have a confirmed Subject.
     */
    private class AssertionContainsConfirmedSubject implements Predicate<Assertion> {

        /** {@inheritDoc} */
        public boolean test(@Nullable final Assertion assertion) {
            if (assertion == null) {
                return false;
            }
            
            final Optional<ValidationProcessingData> validationData = assertion.getObjectMetadata()
                    .get(ValidationProcessingData.class).stream().findFirst();
            if (validationData.isEmpty()) {
                return false;
            }
            
            final ValidationContext validationContext = validationData.get().getContext();
            if (validationContext == null) {
                return false;
            }
            
            return validationContext.getDynamicParameters()
                    .get(SAML2AssertionValidationParameters.CONFIRMED_SUBJECT_CONFIRMATION) != null;
        }
        
    }

}
