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

package net.shibboleth.idp.attribute.resolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;

import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class PluginDependencySupportTest {

    @Test public void getMergedAttributeValueWithAttributeDefinitionDependencyOld() {
        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildAttributeDefinition(
                        ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA1_VALUES));
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final List<IdPAttributeValue> result =
                PluginDependencySupport.getMergedAttributeValues(workContext,
                        Collections.singletonList(new ResolverAttributeDefinitionDependency(ResolverTestSupport.EPA_ATTRIB_ID)),
                        Collections.<ResolverDataConnectorDependency>emptyList(),
                        ResolverTestSupport.EPA_ATTRIB_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[1])));
    }
    
    @Test public void getMergedAttributeValueWithAttributeDefinitionDependencyNew() {
        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildAttributeDefinition(
                        ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA1_VALUES));
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final List<IdPAttributeValue> result =
                PluginDependencySupport.getMergedAttributeValues(workContext,
                        Collections.singletonList(new ResolverAttributeDefinitionDependency(ResolverTestSupport.EPA_ATTRIB_ID)),
                        Collections.<ResolverDataConnectorDependency>emptyList(),
                        ResolverTestSupport.EPA_ATTRIB_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[1])));
    }

    @Test public void getMergedAttributeValuesWithDataConnectorDependencyOld() {
        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildDataConnector("connector1",
                        ResolverTestSupport.buildAttribute(ResolverTestSupport.EPE_ATTRIB_ID,
                                ResolverTestSupport.EPE1_VALUES), ResolverTestSupport.buildAttribute(
                                ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA1_VALUES)));
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final ResolverDataConnectorDependency depend = new ResolverDataConnectorDependency("connector1");
        depend.setAttributeNames(Collections.singletonList(ResolverTestSupport.EPE_ATTRIB_ID));
        final List<IdPAttributeValue> result =
                PluginDependencySupport.getMergedAttributeValues(workContext,
                        Collections.<ResolverAttributeDefinitionDependency>emptyList(),
                        Collections.singletonList(depend),
                        ResolverTestSupport.EPE_ATTRIB_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[1])));

    }
    
    @Test public void getMergedAttributeValuesWithDataConnectorDependency() {
        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildDataConnector("connector1",
                        ResolverTestSupport.buildAttribute(ResolverTestSupport.EPE_ATTRIB_ID,
                                ResolverTestSupport.EPE1_VALUES), ResolverTestSupport.buildAttribute(
                                ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA1_VALUES)));
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final ResolverDataConnectorDependency depend = new ResolverDataConnectorDependency("connector1");
        depend.setAttributeNames(Collections.singleton(ResolverTestSupport.EPE_ATTRIB_ID));
        final List<IdPAttributeValue> result =
                PluginDependencySupport.getMergedAttributeValues(workContext,
                        Collections.<ResolverAttributeDefinitionDependency>emptyList(),
                        Collections.singletonList(depend),
                        ResolverTestSupport.EPE_ATTRIB_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), ResolverTestSupport.EPE1_VALUES.length);
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[1])));

    }
    
    @Test public void getMergedAttributeValuesWithDataConnectorDependencyAll() {
        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildDataConnector("connector1",
                        ResolverTestSupport.buildAttribute(ResolverTestSupport.EPE_ATTRIB_ID,
                                ResolverTestSupport.EPE1_VALUES), ResolverTestSupport.buildAttribute(
                                ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA1_VALUES)));
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final ResolverDataConnectorDependency depend = new ResolverDataConnectorDependency("connector1");
        depend.setAttributeNames(List.of(ResolverTestSupport.EPE_ATTRIB_ID, ResolverTestSupport.EPA_ATTRIB_ID));

        final List<IdPAttributeValue> result =
                PluginDependencySupport.getMergedAttributeValues(workContext,
                        Collections.<ResolverAttributeDefinitionDependency>emptyList(),
                        Collections.singletonList(depend),
                        ResolverTestSupport.EPE_ATTRIB_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), ResolverTestSupport.EPE1_VALUES.length + ResolverTestSupport.EPA1_VALUES.length);
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[1])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[1])));

    }

    @Test public void getMergedAttributeValuesWithDataConnectorDependencyBoth() {
        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildDataConnector("connector1",
                        ResolverTestSupport.buildAttribute(ResolverTestSupport.EPE_ATTRIB_ID,
                                ResolverTestSupport.EPE1_VALUES), ResolverTestSupport.buildAttribute(
                                ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA1_VALUES)));
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final ResolverDataConnectorDependency depend = new ResolverDataConnectorDependency("connector1");
        depend.setAllAttributes(true);
        final List<IdPAttributeValue> result =
                PluginDependencySupport.getMergedAttributeValues(workContext,
                        Collections.<ResolverAttributeDefinitionDependency>emptyList(),
                        Collections.singletonList(depend),
                        ResolverTestSupport.EPE_ATTRIB_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), ResolverTestSupport.EPE1_VALUES.length + ResolverTestSupport.EPA1_VALUES.length);
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[1])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[1])));

    }
    
    @Test public void getMergedAttributeValueWithMultipleDependencies() {
        final MockStaticDataConnector connector1 =
                ResolverTestSupport.buildDataConnector("connector1", ResolverTestSupport.buildAttribute(
                        ResolverTestSupport.EPE_ATTRIB_ID, ResolverTestSupport.EPE1_VALUES), ResolverTestSupport
                        .buildAttribute(ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA2_VALUES));

        final MockStaticAttributeDefinition definition1 =
                ResolverTestSupport.buildAttributeDefinition(ResolverTestSupport.EPA_ATTRIB_ID,
                        ResolverTestSupport.EPA1_VALUES);

        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(connector1, definition1);
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final ResolverDataConnectorDependency dependConnector = new ResolverDataConnectorDependency("connector1");
        
        dependConnector.setAttributeNames(Collections.singleton(ResolverTestSupport.EPA_ATTRIB_ID));
        final List<IdPAttributeValue> result =
                PluginDependencySupport.getMergedAttributeValues(workContext,
                        Collections.singletonList(new ResolverAttributeDefinitionDependency(ResolverTestSupport.EPA_ATTRIB_ID)),
                        Collections.singletonList(dependConnector),
                        ResolverTestSupport.EPE_ATTRIB_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 4);
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[0])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[1])));
        Assert.assertTrue(result.contains(new StringAttributeValue(ResolverTestSupport.EPA2_VALUES[1])));

    }

    @Test public void getAllAttributeValues() {
        final MockStaticDataConnector connector1 =
                ResolverTestSupport.buildDataConnector("connector1", ResolverTestSupport.buildAttribute(
                        ResolverTestSupport.EPE_ATTRIB_ID, ResolverTestSupport.EPE1_VALUES), ResolverTestSupport
                        .buildAttribute(ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA2_VALUES));

        final MockStaticAttributeDefinition definition1 =
                ResolverTestSupport.buildAttributeDefinition(ResolverTestSupport.EPA_ATTRIB_ID,
                        ResolverTestSupport.EPA1_VALUES);

        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(connector1, definition1);
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final ResolverDataConnectorDependency depend = new ResolverDataConnectorDependency("connector1");
        depend.setAllAttributes(true);

        final Map<String, List<IdPAttributeValue>> result =
                PluginDependencySupport.getAllAttributeValues(workContext,
                        Collections.singletonList(new ResolverAttributeDefinitionDependency(ResolverTestSupport.EPA_ATTRIB_ID)),
                        Collections.singletonList(depend));

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);

        List<IdPAttributeValue> values = result.get(ResolverTestSupport.EPE_ATTRIB_ID);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[0])));
        Assert.assertTrue(values.contains(new StringAttributeValue(ResolverTestSupport.EPE1_VALUES[1])));

        values = result.get(ResolverTestSupport.EPA_ATTRIB_ID);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 4);
        Assert.assertTrue(values.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[0])));
        Assert.assertTrue(values.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[1])));
        Assert.assertTrue(values.contains(new StringAttributeValue(ResolverTestSupport.EPA2_VALUES[1])));
    }

    @Test public void getAllAttributeValuesLimited() {
        final MockStaticDataConnector connector1 =
                ResolverTestSupport.buildDataConnector("connector1", ResolverTestSupport.buildAttribute(
                        ResolverTestSupport.EPE_ATTRIB_ID, ResolverTestSupport.EPE1_VALUES), ResolverTestSupport
                        .buildAttribute(ResolverTestSupport.EPA_ATTRIB_ID, ResolverTestSupport.EPA2_VALUES));

        final MockStaticAttributeDefinition definition1 =
                ResolverTestSupport.buildAttributeDefinition(ResolverTestSupport.EPA_ATTRIB_ID,
                        ResolverTestSupport.EPA1_VALUES);

        final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(connector1, definition1);
        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);

        final ResolverDataConnectorDependency depend = new ResolverDataConnectorDependency("connector1");

        depend.setAttributeNames(Collections.singleton(ResolverTestSupport.EPA_ATTRIB_ID));
        final Map<String, List<IdPAttributeValue>> result =
                PluginDependencySupport.getAllAttributeValues(workContext,
                        Collections.singletonList(new ResolverAttributeDefinitionDependency(ResolverTestSupport.EPA_ATTRIB_ID)),
                        Collections.singletonList(depend));

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);

        final List<IdPAttributeValue> values = result.get(ResolverTestSupport.EPA_ATTRIB_ID);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 4);
        Assert.assertTrue(values.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[0])));
        Assert.assertTrue(values.contains(new StringAttributeValue(ResolverTestSupport.EPA1_VALUES[1])));
        Assert.assertTrue(values.contains(new StringAttributeValue(ResolverTestSupport.EPA2_VALUES[1])));
    }
    
    @Test public void hashesAttributeDependency() {
        final ResolverAttributeDefinitionDependency ad1 = new ResolverAttributeDefinitionDependency("ra1");
        final ResolverAttributeDefinitionDependency ad2 = new ResolverAttributeDefinitionDependency("ra1");
        final ResolverAttributeDefinitionDependency ad3 = new ResolverAttributeDefinitionDependency("ra3");
        
        Assert.assertEquals(ad1, ad2);
        Assert.assertNotEquals(ad2, ad3);
        Assert.assertEquals(ad1.hashCode(), ad2.hashCode());
        Assert.assertNotEquals(ad2.hashCode(), ad3.hashCode());
        
    }

    @Test public void hashesDataConnectorDependency() {

        final ResolverDataConnectorDependency dc1 = new ResolverDataConnectorDependency("dc1");
        final ResolverDataConnectorDependency dc2 = new ResolverDataConnectorDependency("dc1");
        final ResolverDataConnectorDependency dc3 = new ResolverDataConnectorDependency("dc3");

        Assert.assertEquals(dc1, dc2);
        Assert.assertNotEquals(dc2, dc3);
        Assert.assertEquals(dc1.hashCode(), dc2.hashCode());
        Assert.assertNotEquals(dc2.hashCode(), dc3.hashCode());
        
        dc1.setAttributeNames(Arrays.asList("a", "b"));
        dc2.setAttributeNames(Arrays.asList("b", "a"));

        Assert.assertEquals(dc1, dc2);
        Assert.assertNotEquals(dc2, dc3);
        Assert.assertEquals(dc1.hashCode(), dc2.hashCode());
        Assert.assertNotEquals(dc2.hashCode(), dc3.hashCode());
        
        dc2.setAllAttributes(true);
        Assert.assertNotEquals(dc1, dc2);
        Assert.assertNotEquals(dc2, dc3);
        Assert.assertNotEquals(dc1.hashCode(), dc2.hashCode());
        Assert.assertNotEquals(dc2.hashCode(), dc3.hashCode());

        dc1.setAllAttributes(true);
        Assert.assertEquals(dc1, dc2);
        Assert.assertNotEquals(dc2, dc3);
        Assert.assertEquals(dc1.hashCode(), dc2.hashCode());
        Assert.assertNotEquals(dc2.hashCode(), dc3.hashCode());

        dc2.setAttributeNames(Arrays.asList("b", "a", "d"));
        Assert.assertNotEquals(dc1, dc2);
        Assert.assertNotEquals(dc2, dc3);
        Assert.assertNotEquals(dc1.hashCode(), dc2.hashCode());
        Assert.assertNotEquals(dc2.hashCode(), dc3.hashCode());
        
}
}
