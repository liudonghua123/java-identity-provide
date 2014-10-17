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

package net.shibboleth.idp.attribute.resolver.dc.rdbms.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.dc.AbstractMappingStrategy;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A simple {@link ResultMappingStrategy} that assumes all columns in the result set should be mapped and that all
 * values are strings.
 */
public class StringResultMappingStrategy extends AbstractMappingStrategy<ResultSet> implements ResultMappingStrategy {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(StringResultMappingStrategy.class);

// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    @Nullable public Map<String,IdPAttribute> map(@Nonnull final ResultSet results) throws ResolutionException {
        Constraint.isNotNull(results, "Result set can not be null");

        try {
            if (!results.next()) {
                log.debug("Result set did not contain any rows, nothing to map");
                return null;
            }
            
            final ResultSetMetaData resultMetadata = results.getMetaData();

            final Map<String,IdPAttribute> attributes =
                    Maps.newHashMapWithExpectedSize(resultMetadata.getColumnCount());
            
            final Map<String,String> aliases = getResultRenamingMap();

            do {
                for (int i = 1; i <= resultMetadata.getColumnCount(); i++) {
                    
                    final String originalId = resultMetadata.getColumnName(i);
                    final String effectiveId = aliases.containsKey(originalId) ? aliases.get(originalId) : originalId;
                    if (log.isDebugEnabled()) {
                        if (!effectiveId.equals(originalId)) {
                            log.debug("Remapping column {} to {}", originalId, effectiveId);
                        }
                    }
                    
                    IdPAttribute attribute = attributes.get(effectiveId);
                    if (attribute == null) {
                        attribute = new IdPAttribute(effectiveId);
                        attributes.put(effectiveId, attribute);
                    }
                    
                    if (attribute.getValues().isEmpty()) {
                        attribute.setValues(Collections.singletonList(new StringAttributeValue(results.getString(i))));
                    } else {
                        final List<IdPAttributeValue<?>> values = Lists.newArrayList(attribute.getValues());
                        values.add(new StringAttributeValue(results.getString(i)));
                        attribute.setValues(values);
                    }
                }
            } while (results.next());

            if (attributes.isEmpty()) {
                return null;
            } else {
                return attributes;
            }
        } catch (final SQLException e) {
            throw new ResolutionException("Error reading data from result set", e);
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
}