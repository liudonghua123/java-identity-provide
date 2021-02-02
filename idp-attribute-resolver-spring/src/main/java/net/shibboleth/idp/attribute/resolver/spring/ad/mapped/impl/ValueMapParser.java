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

package net.shibboleth.idp.attribute.resolver.spring.ad.mapped.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import net.shibboleth.ext.spring.util.AbstractCustomBeanDefinitionParser;
import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.resolver.ad.mapped.impl.ValueMap;
import net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverNamespaceHandler;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/** Bean definition parser for a {@link ValueMap}. */
public class ValueMapParser extends AbstractCustomBeanDefinitionParser {

    /** Schema type name - resolver: . */
    @Nonnull public static final QName TYPE_NAME_RESOLVER =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "ValueMap");

    /** return Value element name. */
    @Nonnull public static final QName RETURN_VALUE_ELEMENT_NAME_RESOLVER =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "ReturnValue");

    /** Class logger. */
    @Nonnull private Logger log = LoggerFactory.getLogger(ValueMapParser.class);

    /** {@inheritDoc} */
    @Override protected Class<ValueMap> getBeanClass(@Nullable final Element element) {
        return ValueMap.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        final List<Element> returnElems = ElementSupport.getChildElements(config, RETURN_VALUE_ELEMENT_NAME_RESOLVER);
        String returnValue = null;

        if (null != returnElems && returnElems.size() > 0) {
            returnValue = StringSupport.trimOrNull(returnElems.get(0).getTextContent());
        }

        if (null == returnValue) {
            throw new BeanCreationException("Attribute Definition: ValueMap must have a ReturnValue");
        }

        final List<Element> sourceValueElements =
                ElementSupport.getChildElements(config, SourceValueParser.TYPE_NAME_RESOLVER);
        if (null == sourceValueElements || sourceValueElements.size() == 0) {
            throw new BeanCreationException("Attribute Definition: ValueMap must have at least one SourceValue");
        }

        final ManagedList<BeanDefinition> sourceValues =
                SpringSupport.parseCustomElements(sourceValueElements, parserContext, builder);

        log.debug("AttributeDefinition(ValueMap): return value: {}, {} source values ", returnValue,
                sourceValues.size());
        log.trace("AttributeDefinition(ValueMap): source values: {}", sourceValues);

        builder.addPropertyValue("sourceValues", sourceValues);
        builder.addPropertyValue("returnValue", returnValue);
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }

}