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

package net.shibboleth.idp.attribute.resolver.impl.dc;

import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.Attribute;
import net.shibboleth.idp.attribute.resolver.AttributeResolutionException;

import com.google.common.base.Optional;

//TODO(lajoie): I wonder if there should be an abstract class impl of this that has some properties like name and data type mappings, name lower/uppercasing, etc.

/** Strategy for mapping from an arbitrary result type to a collection of {@link Attribute}s. */
public interface MappingStrategy<T> {

    /**
     * Maps the given results to a collection of {@link Attribute} indexed by the attribute's ID.
     * 
     * @param results to map
     * 
     * @return the mapped attributes
     * 
     * @throws AttributeResolutionException thrown if there is a problem reading data or mapping it
     */
    @Nonnull public Optional<Map<String, Attribute>> map(@Nonnull T results)
            throws AttributeResolutionException;
}