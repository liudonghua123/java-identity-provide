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

package net.shibboleth.idp.attribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

/** A {@link XMLObjectAttributeValue} value for an {@link net.shibboleth.idp.attribute.IdPAttribute}. */
public final class XMLObjectAttributeValue implements IdPAttributeValue {

    /** Log. */
    private static final Logger LOG = LoggerFactory.getLogger(XMLObjectAttributeValue.class);

    /** Value of the attribute. */
    private final XMLObject value;

    /**
     * Constructor.
     * 
     * @param attributeValue value of the attribute
     */
    public XMLObjectAttributeValue(@Nonnull @ParameterName(name="attributeValue") final XMLObject attributeValue) {
        value = Constraint.isNotNull(attributeValue, "Attribute value cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    public Object getNativeValue() {
        return value;
    }

    /** Return the value.
     * @return the value
     */
    public final XMLObject getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String getDisplayValue() {
        if (value instanceof NameIDType) {
            final NameIDType valAsNameId = (NameIDType) value;
            return valAsNameId.getValue();
        }
        try {
            return SerializeSupport.nodeToString(XMLObjectSupport.marshall(value));
        } catch (final MarshallingException e) {
            LOG.error("Error while marshalling XMLObject value", e);
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof XMLObjectAttributeValue)) {
            return false;
        }

        final XMLObjectAttributeValue other = (XMLObjectAttributeValue) obj;
        return value.equals(other.value);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("value", value).toString();
    }
}