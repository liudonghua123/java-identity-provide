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

package net.shibboleth.idp.attribute.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Support class for working with {@link ResolverPluginDependency}. */
public final class PluginDependencySupport {

    /** Log. */
    private static final Logger LOG = LoggerFactory.getLogger(PluginDependencySupport.class);

    /** Constructor. */
    private PluginDependencySupport() {

    }

    /**
     * Gets the values, as a single list, from all dependencies. This method only supports dependencies which contain an
     * attribute specifier (i.e. {@link ResolverPluginDependency#getDependencyAttributeId()} does not equal null). It is
     * therefore used inside Attribute definitions which only process a single attribute as input.
     * 
     * <p>
     * <strong>NOTE</strong>, this method does *not* actually trigger any attribute definition or data connector
     * resolution, it only looks for the cached results of previously resolved plugins within the current work context.
     * </p>
     * 
     * @param workContext current attribute resolver work context
     * @param dependencies set of dependencies
     * @deprecated use  {@link PluginDependencySupport#getMergedAttributeValues(
     *                      AttributeResolverWorkContext, Collection, String)}
     * @return the merged value set. Returns an empty set if we were given a DataConnector as a dependency, but not
     *         attribute name
     */
    @Deprecated @Nonnull @NonnullElements public static List<IdPAttributeValue<?>> getMergedAttributeValues(
            @Nonnull final AttributeResolverWorkContext workContext,
            @Nonnull @NonnullElements final Collection<ResolverPluginDependency> dependencies) {
        Constraint.isNotNull(workContext, "Attribute resolution context cannot be null");
        Constraint.isNotNull(dependencies, "Resolver dependency collection cannot be null");
        
        return getMergedAttributeValues(workContext, dependencies, "<unknown>");
    }

    /**
     * Gets the values, as a single list, from all dependencies. This method only supports dependencies which contain an
     * attribute specifier (i.e. {@link ResolverPluginDependency#getDependencyAttributeId()} does not equal null). It is
     * therefore used inside Attribute definitions which only process a single attribute as input.
     * 
     * <p>
     * <strong>NOTE</strong>, this method does *not* actually trigger any attribute definition or data connector
     * resolution, it only looks for the cached results of previously resolved plugins within the current work context.
     * </p>
     * 
     * @param workContext current attribute resolver work context
     * @param dependencies set of dependencies
     * @param attributeDefinitionId the attributeID that these values will be associated with.
     * @return the merged value set. Returns an empty set if we were given a DataConnector as a dependency, but not
     *         attribute name
     */
    @Nonnull @NonnullElements public static List<IdPAttributeValue<?>> getMergedAttributeValues(
            @Nonnull final AttributeResolverWorkContext workContext,
            @Nonnull @NonnullElements final Collection<ResolverPluginDependency> dependencies,
            @Nonnull final String attributeDefinitionId) {
        Constraint.isNotNull(workContext, "Attribute resolution context cannot be null");
        Constraint.isNotNull(dependencies, "Resolver dependency collection cannot be null");

        final List<IdPAttributeValue<?>> values = new ArrayList<>();

        for (final ResolverPluginDependency dependency : dependencies) {
            Constraint.isNotNull(dependency, "Resolver dependency cannot be null");

            final String pluginId = dependency.getDependencyPluginId();

            if (dependency instanceof ResolverAttributeDefinitionDependency) {
                final ResolvedAttributeDefinition attributeDefinition =
                        workContext.getResolvedIdPAttributeDefinitions().get(pluginId);
                final IdPAttribute resolvedAttribute = attributeDefinition.getResolvedAttribute();
                mergeAttributeValues(resolvedAttribute, values);
            } else if (dependency instanceof ResolverDataConnectorDependency) {
                // Merge all specified attribute values.
                final ResolverDataConnectorDependency dataConnectorDependency = (ResolverDataConnectorDependency) dependency;
                final ResolvedDataConnector dataConnector =
                        workContext.getResolvedDataConnectors().get(dependency.getDependencyPluginId());
                if (dataConnector != null) { 
                    final Map<String, IdPAttribute> resolvedAttrs = dataConnector.getResolvedAttributes();
                    if (null != resolvedAttrs) {
                        for (final Entry<String, IdPAttribute> entry : resolvedAttrs.entrySet()) {
                            if (dataConnectorDependency.isAllAttributes() || dataConnectorDependency.getAttributeNames().contains(entry.getKey())) {
                                mergeAttributeValues(entry.getValue(), values);
                            }
                        }
                    }
                }
            } else {
                final ResolvedAttributeDefinition attributeDefinition =
                        workContext.getResolvedIdPAttributeDefinitions().get(pluginId);
                final String dependencyAttributeId = dependency.getDependencyAttributeId();
                if (attributeDefinition != null) {
                    if (null == dependencyAttributeId) {
                        LOG.warn("Plugin '{}' was defined without a sourceAttributeID,  but attribute '{}', specified "
                                + "" + "as a <Dependency> will be used.", attributeDefinitionId, pluginId);
                    } else if (!dependencyAttributeId.equals(pluginId)) {
                        LOG.warn("Plugin '{}' was defined with a sourceAttributeID '{}',"
                                + " but the attribute definition '{}', specified as a <Dependency> will be used as well.",
                                attributeDefinitionId, dependencyAttributeId, pluginId);
                    }
    
                    final IdPAttribute resolvedAttribute = attributeDefinition.getResolvedAttribute();
                    mergeAttributeValues(resolvedAttribute, values);
                    continue;
                }
    
                final ResolvedDataConnector dataConnector =
                        workContext.getResolvedDataConnectors().get(dependency.getDependencyPluginId());
                if (dataConnector != null) {
                    if (dependency.getDependencyAttributeId() == null) {
                        LOG.error("Attribute definition '{}' has a data connector"
                                + " dependency '{}' but no sourceAttributeID.", attributeDefinitionId,
                                dataConnector.getId());
                        return Collections.EMPTY_LIST;
                    }
    
                    if (null != dataConnector.getResolvedAttributes()) {
                        final IdPAttribute resolvedAttribute =
                                dataConnector.getResolvedAttributes().get(dependency.getDependencyAttributeId());
                        mergeAttributeValues(resolvedAttribute, values);
                        continue;
                    }
                }
            }
        }

        return values;
    }

    /**
     * Gets the values from all dependencies. Attributes, with the same identifier but from different resolver plugins,
     * will have their values merged into a single list within this method's returned map. This method is the equivalent
     * of calling {@link #getMergedAttributeValues(AttributeResolverWorkContext, Collection)} for all attributes
     * resolved by all the given dependencies. This is therefore used when an attribute definition may have multiple
     * input attributes (for instance scripted or templated definitions).
     * 
     * <p>
     * <strong>NOTE</strong>, this method does *not* actually trigger any attribute definition or data connector
     * resolution, it only looks for the cached results of previously resolved plugins within the current work context.
     * </p>
     * 
     * @param workContext current attribute resolver work context
     * @param dependencies set of dependencies
     * 
     * @return the merged value set
     */
    public static Map<String, List<IdPAttributeValue<?>>> getAllAttributeValues(
            @Nonnull final AttributeResolverWorkContext workContext,
            @Nonnull final Collection<ResolverPluginDependency> dependencies) {

        final HashMap<String, List<IdPAttributeValue<?>>> result = new HashMap<>();

        for (final ResolverPluginDependency dependency : dependencies) {
            Constraint.isNotNull(dependency, "Resolver dependency cannot be null");

            if (dependency instanceof ResolverAttributeDefinitionDependency) {
                final ResolvedAttributeDefinition attributeDefinition =
                        workContext.getResolvedIdPAttributeDefinitions().get(dependency.getDependencyPluginId());
                if (attributeDefinition != null) {
                    addAttribute(attributeDefinition.getResolvedAttribute(), result);
                }
            } else if (dependency instanceof ResolverDataConnectorDependency) {
                // Just add those attributes specified
                final ResolverDataConnectorDependency dataConnectorDependency = (ResolverDataConnectorDependency) dependency;
                final ResolvedDataConnector dataConnector =
                        workContext.getResolvedDataConnectors().get(dependency.getDependencyPluginId());
                if (dataConnector != null) { 
                    final Map<String, IdPAttribute> resolvedAttrs = dataConnector.getResolvedAttributes();
                    if (null != resolvedAttrs) {
                        for (final Entry<String, IdPAttribute> entry : resolvedAttrs.entrySet()) {
                            if (dataConnectorDependency.isAllAttributes() || dataConnectorDependency.getAttributeNames().contains(entry.getKey())) {
                                addAttribute(entry.getValue(), result);
                            }
                        }
                    }
                }
            } else {
                // deprecated behavior
                final ResolvedAttributeDefinition attributeDefinition =
                        workContext.getResolvedIdPAttributeDefinitions().get(dependency.getDependencyPluginId());
                if (attributeDefinition != null) {
                    addAttribute(attributeDefinition.getResolvedAttribute(), result);
                    continue;
                }
    
                final ResolvedDataConnector dataConnector =
                        workContext.getResolvedDataConnectors().get(dependency.getDependencyPluginId());
                if (dataConnector != null) {
                    if (null != dataConnector.getResolvedAttributes()) {
                        mergeAttributes(dataConnector.getResolvedAttributes(), result);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Adds the values of the attributes to the target collection of attribute values indexes by attribute ID.
     * 
     * @param sources the source attributes
     * @param target current set attribute values
     */
    @Nonnull private static void mergeAttributes(@Nonnull final Map<String, IdPAttribute> sources,
            @Nullable final Map<String, List<IdPAttributeValue<?>>> target) {
        for (final IdPAttribute source : sources.values()) {
            if (source == null) {
                continue;
            }

            addAttribute(source, target);
        }
    }

    /**
     * Adds the values of the given attribute to the target collection of attribute values.
     * 
     * @param source the source attribute
     * @param target current set attribute values
     */
    @Nonnull private static void addAttribute(@Nullable final IdPAttribute source,
            @Nullable final Map<String, List<IdPAttributeValue<?>>> target) {
        if (source == null) {
            return;
        }
        List<IdPAttributeValue<?>> attributeValues = target.get(source.getId());
        if (attributeValues == null) {
            attributeValues = new ArrayList<>();
            target.put(source.getId(), attributeValues);
        }

        mergeAttributeValues(source, attributeValues);
    }

    /**
     * Adds the values of the given attribute to the set of attribute values.
     * 
     * @param source the source attribute
     * @param target current set attribute values
     */
    @Nonnull private static void mergeAttributeValues(@Nullable final IdPAttribute source,
            @Nonnull final List<IdPAttributeValue<?>> target) {
        if (source != null) {
            target.addAll(source.getValues());
        }
    }
}