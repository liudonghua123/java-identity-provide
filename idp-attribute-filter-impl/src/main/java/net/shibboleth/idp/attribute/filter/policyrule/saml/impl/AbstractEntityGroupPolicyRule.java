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

import java.util.Collections;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.policyrule.impl.AbstractPolicyRule;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.common.profile.logic.EntityGroupNamePredicate;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * A matcher that evaluates to true if an entity's metadata matches the provided entity group name,
 * or a valid metadata-sourced affiliation of entities.
 * 
 * @since 4.0.0
 */
public abstract class AbstractEntityGroupPolicyRule extends AbstractPolicyRule {
    
    /** The entity group to match against. */
    @NonnullAfterInit @NotEmpty private String entityGroup;
    
    /** Whether to search metadata for AffiliationDescriptor membership. */
    private boolean checkAffiliations;
    
    /**
     * Gets the entity group to match against.
     * 
     * @return entity group to match against
     */
    @NonnullAfterInit @NotEmpty public String getEntityGroup() {
        return entityGroup;
    }

    /**
     * Sets the entity group to match against.
     * 
     * @param group entity group to match against
     */
    public void setEntityGroup(@Nullable final String group) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        entityGroup = StringSupport.trimOrNull(group);
    }
    
    /**
     * Set whether to check a supplied {@link MetadataResolver} for membership in an AffiliationDescriptor
     * as a form of group policy.
     * 
     * <p>Defaults to false.</p>
     * 
     * @param flag flag to set
     */
    public void setCheckAffiliations(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        checkAffiliations = flag;
    }


    /**
     * Returns whether we check a supplied {@link MetadataResolver} for membership in an AffiliationDescriptor
     * as a form of group policy.
     *
     * @return whether to check for AffiliationDescriptor membership
     *
     * @since 4.0.0
     */
    public boolean isCheckAffiliations() {
        return checkAffiliations;
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (entityGroup == null) {
            throw new ComponentInitializationException("entityGroup cannot be null");
        }
    }

    /**
     * Gets the entity descriptor for the entity to check.
     * 
     * @param filterContext current filter request context
     * 
     * @return entity descriptor for the entity to check
     */
    @Nullable protected abstract EntityDescriptor getEntityMetadata(
            @Nonnull final AttributeFilterContext filterContext);
    
    /**
     * Checks if the given entity is in the provided entity group.
     * 
     * @param input the context to look at
     * 
     * @return whether the entity is in the group
     *         {@inheritDoc}
     */
    @Override
    @Nonnull public Tristate matches(@Nonnull final AttributeFilterContext input) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        Constraint.isNotNull(input, "Context must be supplied");

        final EntityDescriptor entity = getEntityMetadata(input);
        if (entity == null) {
            return Tristate.FALSE;
        }

        final Predicate<EntityDescriptor> predicate =
                new EntityGroupNamePredicate(Collections.singleton(entityGroup),
                        checkAffiliations ? input.getMetadataResolver() : null);
        
        return predicate.test(entity) ? Tristate.TRUE : Tristate.FALSE;
    }

}