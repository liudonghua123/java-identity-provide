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

package net.shibboleth.idp.saml.profile.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.ProfileIdLookup;
import org.opensaml.saml.common.messaging.context.navigate.EntityDescriptorLookupFunction;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.soap.client.security.SOAPClientSecurityProfileIdLookupFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;

import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A strategy function that examines SAML metadata associated with a relying party and derives configuration
 * settings based on EntityAttribute extension tags.
 * 
 * <p>The function is tailored with properties that determine what tag it looks for, with subclasses
 * handling the specific type conversion logic.</p>
 * 
 * <p>If a specific property is unavailable, then null is returned.</p>
 * 
 * @param <T> type of property being returned
 * 
 * @since 3.4.0
 */
public abstract class AbstractMetadataDrivenConfigurationLookupStrategy<T> extends AbstractInitializableComponent
        implements Function<BaseContext,T> {

    /** Default metadata lookup for PRC-based usage. */
    @Nonnull private static final Function<ProfileRequestContext,EntityDescriptor> DEFAULT_PRC_METADATA_LOOKUP;

    /** Default profile ID lookup for PRC-based usage. */
    @Nonnull private static final Function<ProfileRequestContext,String> DEFAULT_PRC_PROFILE_ID_LOOKUP;

    /** Default metadata lookup for MC-based usage. */
    @Nonnull private static final Function<MessageContext,EntityDescriptor> DEFAULT_MC_METADATA_LOOKUP;

    /** Default profile ID lookup for MC-based usage. */
    @Nonnull private static final Function<MessageContext,String> DEFAULT_MC_PROFILE_ID_LOOKUP;

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AbstractMetadataDrivenConfigurationLookupStrategy.class);
    
    /** Require use of URI attribute name format. */
    private boolean strictNameFormat;
    
    /** Cache the lookup in the context tree. */
    private boolean enableCaching;
    
    /** Base name of property to produce. */
    @NonnullAfterInit @NotEmpty private String propertyName;
    
    /** Alternative "full" property identifiers to support. */
    @NonnullAfterInit @NonnullElements private Collection<String> propertyAliases;
        
    /** Strategy for obtaining metadata to check. */
    @Nullable private Function<BaseContext,EntityDescriptor> metadataLookupStrategy;

    /** Strategy for obtaining profile ID for property naming. */
    @Nullable @NotEmpty private Function<BaseContext,String> profileIdLookupStrategy;
        
    /** Constructor. */
    public AbstractMetadataDrivenConfigurationLookupStrategy() {
        enableCaching = true;
    }
    
    /**
     * Set whether tag matching should examine and require an Attribute NameFormat of the URI type.
     * 
     * <p>Default is false.</p>
     * 
     * @param flag flag to set
     */
    public void setStrictNameFormat(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        strictNameFormat = flag;
    }
    
    /**
     * Set whether property lookup should be cached in the profile context tree.
     * 
     * <p>Default is true.</p>
     * 
     * @param flag flag to set
     */
    public void setEnableCaching(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        enableCaching = flag;
    }
    
    /**
     * Set the "base" name of the property/setting to derive.
     * 
     * @param name base property name
     */
    public void setPropertyName(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        propertyName = Constraint.isNotNull(StringSupport.trimOrNull(name), "Property name cannot be null or empty");
    }
    
    /**
     * Set profile ID aliases to include when checking for metadata tags (the property name is suffixed to the
     * aliases).
     * 
     * <p>This allows alternative tag names to be checked.</p>
     * 
     * @param aliases alternative profile IDs
     */
    public void setProfileAliases(@Nonnull @NonnullElements final Collection<String> aliases) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(aliases, "Alias collection cannot be null");
        
        propertyAliases = new ArrayList(StringSupport.normalizeStringCollection(aliases));        
    }
    
    /**
     * Set lookup strategy for metadata to examine.
     * 
     * @param strategy  lookup strategy
     */
    public void setMetadataLookupStrategy(@Nonnull final Function<BaseContext,EntityDescriptor> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        metadataLookupStrategy = Constraint.isNotNull(strategy, "Metadata lookup strategy cannot be null");
    }

    /**
     * Set lookup strategy for profile ID to base property names on.
     * 
     * @param strategy  lookup strategy
     */
    public void setProfileIdLookupStrategy(@Nonnull final Function<BaseContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        profileIdLookupStrategy = Constraint.isNotNull(strategy, "Profile ID lookup strategy cannot be null");
    }

// Checkstyle: CyclomaticComplexity OFF    
    /** {@inheritDoc} */
    @Nullable public T apply(@Nullable final BaseContext input) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        CachedConfigurationContext cacheContext = null;
        
        if (enableCaching && input != null) {
            cacheContext = input.getSubcontext(CachedConfigurationContext.class, true);
            if (cacheContext.getPropertyMap().containsKey(propertyName)) {
                log.debug("Returning cached property '{}'", propertyName);
                return (T) cacheContext.getPropertyMap().get(propertyName);
            }
        }
        
        final EntityDescriptor entity;
        final String profileId;
        
        if (metadataLookupStrategy != null) {
            entity = metadataLookupStrategy.apply(input);
        } else if (input instanceof ProfileRequestContext) {
            entity = DEFAULT_PRC_METADATA_LOOKUP.apply((ProfileRequestContext) input);
        } else if (input instanceof MessageContext) {
            entity = DEFAULT_MC_METADATA_LOOKUP.apply((MessageContext) input);
        } else {
            entity = null;
        }
            
        if (entity == null) {
            log.debug("No metadata available for relying party, no setting returned for '{}'", propertyName);
            return null;
        }
        
        if (profileIdLookupStrategy != null) {
            profileId = profileIdLookupStrategy.apply(input);
        } else if (input instanceof ProfileRequestContext) {
            profileId = DEFAULT_PRC_PROFILE_ID_LOOKUP.apply((ProfileRequestContext) input);
        } else if (input instanceof MessageContext) {
            profileId = DEFAULT_MC_PROFILE_ID_LOOKUP.apply((MessageContext) input);
        } else {
            profileId = "";
        }
        
        // Look for "primary" tag name based on profile/property.
        Attribute attribute = findMatchingTag(entity, profileId + '/' + propertyName);
        if (attribute != null) {
            log.debug("Found matching tag '{}' for property '{}'", attribute.getName(), propertyName);
            final T result = translate(attribute);
            if (enableCaching) {
                cacheContext.getPropertyMap().put(propertyName, result);
            }
            return result;
        }
        
        for (final String alias : propertyAliases) {
            attribute = findMatchingTag(entity, alias);
            if (attribute != null) {
                log.debug("Found matching tag '{}' for property '{}'", attribute.getName(), propertyName);
                final T result = translate(attribute);
                if (enableCaching) {
                    cacheContext.getPropertyMap().put(propertyName, result);
                }
                return result;
            }
        }
        
        log.debug("No applicable tag, no setting returned for '{}'", propertyName);
        if (enableCaching) {
            cacheContext.getPropertyMap().put(propertyName, null);
        }
        return null;
    }
// Checkstyle: CyclomaticComplexity ON
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (propertyName == null) {
            throw new ComponentInitializationException("Property name cannot be null or empty");
        } else if (propertyAliases == null) {
            propertyAliases = Collections.emptyList();
        } else {
            propertyAliases = Collections2.transform(propertyAliases,
                    new Function<String,String>() {
                        public String apply(final String input) {
                            return input + (input.endsWith("/") ? propertyName : '/' + propertyName);
                        }
                    });
        }
    }

    /**
     * Translate the value(s) into a setting of the appropriate type.
     * 
     * @param tag tag to translate
     * 
     * @return the setting derived from the tag's value(s)
     */
    @Nullable private T translate(@Nonnull final Attribute tag) {
        
        final List<XMLObject> values = tag.getAttributeValues();
        if (values == null || values.isEmpty()) {
            log.debug("Tag '{}' contained no values, no setting returned for '{}'", tag.getName(), propertyName);
            return null;
        }
        
        return doTranslate(tag);
    }
    
    /**
     * Translate the value(s) into a setting of the appropriate type.
     * 
     * <p>Overrides of this function can assume a non-zero collection of values.</p>
     * 
     * @param tag tag to translate
     * 
     * @return the setting derived from the tag's value(s)
     */
    @Nullable protected abstract T doTranslate(@Nonnull final Attribute tag); 
    
    /**
     * Find a matching entity attribute in the input metadata.
     * 
     * @param entity the metadata to examine
     * @param name the tag name to search for
     * 
     * @return matching attribute or null
     */
    @Nullable private Attribute findMatchingTag(@Nonnull final EntityDescriptor entity,
            @Nonnull @NotEmpty final String name) {
        
        // Check for a tag match in the EntityAttributes extension of the entity and its parent(s).
        Extensions exts = entity.getExtensions();
        if (exts != null) {
            final List<XMLObject> children = exts.getUnknownXMLObjects(EntityAttributes.DEFAULT_ELEMENT_NAME);
            if (!children.isEmpty() && children.get(0) instanceof EntityAttributes) {
                final Attribute tag = findMatchingTag((EntityAttributes) children.get(0), name);
                if (tag != null) {
                    return tag;
                }
            }
        }

        EntitiesDescriptor group = (EntitiesDescriptor) entity.getParent();
        while (group != null) {
            exts = group.getExtensions();
            if (exts != null) {
                final List<XMLObject> children = exts.getUnknownXMLObjects(EntityAttributes.DEFAULT_ELEMENT_NAME);
                if (!children.isEmpty() && children.get(0) instanceof EntityAttributes) {
                    final Attribute tag = findMatchingTag((EntityAttributes) children.get(0), name);
                    if (tag != null) {
                        return tag;
                    }
                }
            }
            group = (EntitiesDescriptor) group.getParent();
        }
        
        return null;
    }
    
    /**
     * Find a matching entity attribute in the input metadata.
     * 
     * @param entityAttributes the metadata to examine
     * @param name the tag name to search for
     * 
     * @return matching attribute or null
     */
    @Nullable private Attribute findMatchingTag(@Nonnull final EntityAttributes entityAttributes,
            @Nonnull @NotEmpty final String name) {
        
        for (final Attribute tag : entityAttributes.getAttributes()) {
            if (Objects.equals(tag.getName(), name)
                    && (!strictNameFormat || Objects.equals(tag.getNameFormat(), Attribute.URI_REFERENCE))) {
                return tag;
            }
        }

        return null;
    }
    
    /** A child context that caches derived configuration properties. */
    public static final class CachedConfigurationContext extends BaseContext {
        
        /** Cached property map. */
        @Nonnull private Map<String,Object> propertyMap;
        
        /** Constructor. */
        public CachedConfigurationContext() {
            propertyMap = new HashMap<>();
        }
        
        /**
         * Get cached property map.
         * 
         * @return cached property map
         */
        @Nonnull @Live Map<String,Object> getPropertyMap() {
            return propertyMap;
        }
    }
    
    static {
        // Init PRC defaults.
        
        DEFAULT_PRC_METADATA_LOOKUP = Functions.compose(new EntityDescriptorLookupFunction(),
                new net.shibboleth.idp.saml.profile.context.navigate.SAMLMetadataContextLookupFunction());
        
        DEFAULT_PRC_PROFILE_ID_LOOKUP = new ProfileIdLookup();

        // Init MC defaults.

        DEFAULT_MC_METADATA_LOOKUP = Functions.compose(new EntityDescriptorLookupFunction(),
                new net.shibboleth.idp.saml.profile.context.navigate.messaging.SAMLMetadataContextLookupFunction());
        
        DEFAULT_MC_PROFILE_ID_LOOKUP = new SOAPClientSecurityProfileIdLookupFunction();
    }
}