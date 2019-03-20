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

package net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import net.shibboleth.idp.profile.spring.factory.BasicX509CredentialFactoryBean;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;

/**
 * Parser for X509Filesystem credentials.
 */
public class X509ResourceCredentialParser extends AbstractX509CredentialParser {

    /** Element Name for Filesystem. */
    public static final QName TYPE_NAME_FILESYSTEM = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "X509Filesystem");

    /** Type for X509 credentials. */
    public static final QName TYPE_NAME_RESOURCE = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "X509ResourceBacked");

    @Override protected Class<?> getBeanClass(final Element element) {
        return BasicX509CredentialFactoryBean.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(final Element element, final BeanDefinitionBuilder builder) {
        if (TYPE_NAME_FILESYSTEM.equals(DOMTypeSupport.getXSIType(element))) {
            DeprecationSupport.warnOnce(
                    ObjectType.ELEMENT,
                    TYPE_NAME_FILESYSTEM.getLocalPart(),
                    null,
                    TYPE_NAME_RESOURCE.getLocalPart());

        }
        super.doParse(element, builder);
    }

}
