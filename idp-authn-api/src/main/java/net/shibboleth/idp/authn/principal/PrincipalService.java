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

package net.shibboleth.idp.authn.principal;

import java.security.Principal;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

/**
 * Interface that provides services for a {@link Principal} of a given type.
 * 
 * @param <T> principal type
 * 
 * @since 4.1.0
 */
public interface PrincipalService<T extends Principal> extends IdentifiedComponent {

    /**
     * Get the type of object supported.
     * 
     * @return supported type
     */
    @Nonnull Class<T> getType();
    
    /**
     * Create a new instance of the appropriate type.
     * 
     * @param name principal name
     * 
     * @return new instance
     */
    @Nonnull T newInstance(@Nonnull @NotEmpty final String name);
}