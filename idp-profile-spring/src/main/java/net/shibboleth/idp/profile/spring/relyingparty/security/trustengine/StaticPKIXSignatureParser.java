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

package net.shibboleth.idp.profile.spring.relyingparty.security.trustengine;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.security.SecurityNamespaceHandler;

import org.opensaml.xmlsec.keyinfo.impl.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DSAKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import org.opensaml.xmlsec.signature.support.impl.PKIXSignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for trust engines of type StaticPKIXKeySignature.
 */
public class StaticPKIXSignatureParser extends AbstractTrustEngineParser {
    
    /** Schema type. */
    public static final QName TYPE_NAME = new QName(SecurityNamespaceHandler.NAMESPACE, "StaticPKIXSignature");
    
    /** log.*/
    private final Logger log = LoggerFactory.getLogger(StaticPKIXSignatureParser.class);

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return PKIXSignatureTrustEngine.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        log.error("Legacy parsing of StaticPKIXSignature (located in {}) has not"
                + " been implemented, behavior will be undefined", parserContext.getReaderContext().getResource()
                .getDescription());

        List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>();
        keyInfoProviders.add(new DSAKeyValueProvider());
        keyInfoProviders.add(new RSAKeyValueProvider());
        keyInfoProviders.add(new InlineX509DataProvider());
        builder.addConstructorArgValue(new BasicProviderKeyInfoCredentialResolver(keyInfoProviders));
    }
}
