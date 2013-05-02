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

package net.shibboleth.idp.saml.impl.attribute.encoding;

import java.util.Collection;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.AttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.core.impl.NameIdentifierBuilder;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/**
 * {@link Saml1ByteAttributeEncoder} Unit test.
 * 
 * Identical code to the {@link Saml1XmlObjectSubjectNameIdentifierEncoder} except that the type of assertion and
 * encoder is changed.
 */
public class Saml1XmlObjectSubjectNameIdentifierEncoderTest extends OpenSAMLInitBaseTestCase {

    /** The name we give the test attribute. */
    private final static String ATTR_NAME = "foo";

    private static NameIdentifierBuilder saml1Builder;

    private static NameIDBuilder saml2Builder;

    /** test values. */
    private final static String NAME_1 = "NameId1";

    private final static String NAME_2 = "NameId2";

    private final static String OTHERID = "NameOtherProtocol";

    private final static String QUALIFIER = "Qualifier";

    private static AttributeValue saml1NameIdFor(final String ident) {
        NameIdentifier id = saml1Builder.buildObject();

        id.setNameIdentifier(ident);
        id.setNameQualifier(QUALIFIER);
        return new XMLObjectAttributeValue(id);
    }

    private static AttributeValue saml2NameIdFor(final String ident) {
        NameID id = saml2Builder.buildObject();

        id.setValue(ident);
        id.setNameQualifier(QUALIFIER);
        return new XMLObjectAttributeValue(id);
    }

    private Saml1XmlObjectSubjectNameIdentifierEncoder encoder;

    @BeforeClass public void initTest() throws ComponentInitializationException {

        encoder = new Saml1XmlObjectSubjectNameIdentifierEncoder();
        saml1Builder = new NameIdentifierBuilder();
        saml2Builder = new NameIDBuilder();
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void testEmpty() throws Exception {
        final net.shibboleth.idp.attribute.Attribute inputAttribute;

        inputAttribute = new net.shibboleth.idp.attribute.Attribute(ATTR_NAME);

        encoder.encode(inputAttribute);
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void testInappropriate() throws Exception {
        final int[] intArray = {1, 2, 3, 4};
        final Collection<AttributeValue> values =
                Lists.newArrayList((AttributeValue) new StringAttributeValue("foo"), new ScopedStringAttributeValue(
                        "foo", "bar"), new AttributeValue() {
                    public Object getValue() {
                        return intArray;
                    }
                }, saml2NameIdFor(OTHERID));

        net.shibboleth.idp.attribute.Attribute inputAttribute = new net.shibboleth.idp.attribute.Attribute(ATTR_NAME);
        inputAttribute.setValues(values);

        encoder.encode(inputAttribute);
    }

    @Test public void testSingle() throws Exception {
        final Collection<AttributeValue> values =
                Lists.newArrayList((AttributeValue) saml2NameIdFor(OTHERID), new StringAttributeValue("foo"),
                        saml1NameIdFor(NAME_1), saml2NameIdFor(NAME_2));
        final net.shibboleth.idp.attribute.Attribute inputAttribute;

        inputAttribute = new net.shibboleth.idp.attribute.Attribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final NameIdentifier outputNameId = encoder.encode(inputAttribute);

        Assert.assertNotNull(outputNameId);
        Assert.assertEquals(outputNameId.getNameIdentifier(), NAME_1);
        Assert.assertEquals(outputNameId.getNameQualifier(), QUALIFIER);

    }

    @Test public void testMulti() throws Exception {
        final Collection<AttributeValue> values =
                Lists.newArrayList((AttributeValue) saml2NameIdFor(OTHERID), saml1NameIdFor(NAME_1),
                        saml1NameIdFor(NAME_1));

        final net.shibboleth.idp.attribute.Attribute inputAttribute;
        inputAttribute = new net.shibboleth.idp.attribute.Attribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final NameIdentifier outputNameId = encoder.encode(inputAttribute);

        Assert.assertNotNull(outputNameId);
        Assert.assertEquals(outputNameId.getNameQualifier(), QUALIFIER);
        String nameName = outputNameId.getNameIdentifier();
        Assert.assertTrue(nameName.equals(NAME_1) || nameName.equals(NAME_2));

    }

}
