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

package net.shibboleth.idp.attribute.filter.spring.mapper;

import java.util.Collection;

import net.shibboleth.idp.attribute.filter.impl.policyrule.saml.attributemapper.RequestedAttributesMapper;
import net.shibboleth.idp.spring.SchemaTypeAwareXMLBeanDefinitionReader;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * kicks the wheels tests.
 */
public class NativeSpringTest {

    public static final String FILE_PATH = "net/shibboleth/idp/attribute/mapper/"; 

    protected <Type> Type getBean(String fileName, Class<Type> claz, GenericApplicationContext context) {

        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader =
                new SchemaTypeAwareXMLBeanDefinitionReader(context);

        beanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        beanDefinitionReader.loadBeanDefinitions(fileName);

        Collection<Type> beans = context.getBeansOfType(claz).values();
        Assert.assertEquals(beans.size(), 1);

        return (Type) beans.iterator().next();
    }

    
    @Test
    public void stringAttrValue() {
        RequestedAttributesMapper map = getBean(FILE_PATH + "attributesMapper.xml", RequestedAttributesMapper.class, new GenericApplicationContext());
        
        LoggerFactory.getLogger(NativeSpringTest.class).debug(map.toString());
    }
}
