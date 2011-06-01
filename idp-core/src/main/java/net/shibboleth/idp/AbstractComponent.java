/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
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

package net.shibboleth.idp;

import org.opensaml.util.Assert;
import org.opensaml.util.ObjectSupport;
import org.opensaml.util.StringSupport;

/**
 * Simple base class for {@link Component} implementations. Note, this class provides a no-op implementation of the
 * {@link Component#validate()} method.
 */
public abstract class AbstractComponent implements Component {

    /** Identifier for this component. */
    private final String id;

    /**
     * Constructor.
     * 
     * @param componentId identifier for the component, never null or empty
     */
    public AbstractComponent(final String componentId) {
        id = StringSupport.trimOrNull(componentId);
        Assert.isNotNull(id, "Component ID may not be null or empty");
    }

    /** {@inheritDoc} */
    public String getId() {
        return id;
    }

    /**
     * The default implementation of this implementation is a no-op.
     * 
     * {@inheritDoc}
     */
    public void validate() throws ComponentValidationException {
        // no-op implementation
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AbstractComponent)) {
            return false;
        }

        AbstractComponent other = (AbstractComponent) obj;
        return ObjectSupport.equals(getId(), other.getId());
    }
}