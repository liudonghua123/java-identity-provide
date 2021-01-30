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

package net.shibboleth.idp.attribute.filter.spring.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.filter.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/** Bean definition parser for an {@link AttributeFilterPolicy}. */
public class AttributeFilterPolicyParser extends BaseFilterParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(BaseFilterParser.NAMESPACE,
            "AttributeFilterPolicy");

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(BaseFilterParser.NAMESPACE,
            "AttributeFilterPolicyType");

    /** The AttributeRule QName. */
    private static final QName ATTRIBUTE_RULE = new QName(BaseFilterParser.NAMESPACE, "AttributeRule");

    /** Class logger. */
    private Logger log = LoggerFactory.getLogger(AttributeFilterPolicyParser.class);

    /** {@inheritDoc} */
    @Override
    protected Class<?> getBeanClass(final Element arg0) {
        return AttributeFilterPolicy.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        String policyId = StringSupport.trimOrNull(config.getAttributeNS(null, "id"));
        if (null == policyId) {
            policyId =  builder.getBeanDefinition().getAttribute(BaseFilterParser.QUALIFIED_ID).toString();
        }
        log.debug("Parsing configuration for attribute filter policy: {}", policyId);
        builder.addConstructorArgValue(policyId);

        // Get the policy requirement, either inline or referenced
        final List<Element> policyRequirements = ElementSupport.getChildElements(config,
                BaseFilterParser.POLICY_REQUIREMENT_RULE);
        if (policyRequirements != null && policyRequirements.size() > 0) {
            final ManagedList<BeanDefinition> requirements =
                    BaseFilterParser.parseCustomElements(policyRequirements, parserContext, builder);
            builder.addConstructorArgValue(requirements.get(0));
        }

        final List<Element> rules = ElementSupport.getChildElements(config, ATTRIBUTE_RULE);
        final ManagedList<BeanDefinition> attributeRules = 
                SpringSupport.parseCustomElements(rules, parserContext, builder);

        builder.addConstructorArgValue(attributeRules);
    }
}