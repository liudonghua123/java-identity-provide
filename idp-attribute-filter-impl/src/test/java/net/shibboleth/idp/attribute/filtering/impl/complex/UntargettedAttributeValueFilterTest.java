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

package net.shibboleth.idp.attribute.filtering.impl.complex;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.Attribute;
import net.shibboleth.idp.attribute.filtering.AttributeFilterContext;
import net.shibboleth.idp.attribute.filtering.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filtering.AttributeFilteringEngine;
import net.shibboleth.idp.attribute.filtering.AttributeFilteringException;
import net.shibboleth.idp.attribute.filtering.AttributeValueFilterPolicy;
import net.shibboleth.idp.attribute.filtering.MatchFunctor;
import net.shibboleth.idp.attribute.filtering.impl.matcher.attributevalue.AttributeValueStringMatcher;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Complex test for AttributeRuleFilters when the rule is targeted
 */
public class UntargettedAttributeValueFilterTest extends BaseComplexAttributeFilterTestCase {

    /*
     * We will test this rule: xsi:type="basic:AttributeValueString" value="jsmith" ignoreCase="true"
     */
    private MatchFunctor valueMatcher() {
        AttributeValueStringMatcher retVal = new AttributeValueStringMatcher();

        retVal.setCaseSensitive(true);
        retVal.setMatchString("jsmith");
        
        retVal.setId("valueMatcher");
        try {
            retVal.initialize();
        } catch (ComponentInitializationException e) {
            retVal = null;
        }

        return retVal;
    }

    /**
     * test the following policy.
     * 
     <code>
      <AttributeFilterPolicy id="targettedValueInEPA">
          <PolicyRequirementRule xsi:type="basic:ANY" /> 
          <AttributeRule attributeID="eduPersonAffiliation">
              <PermitValueRule xsi:type="basic:AttributeValueString" value="jsmith" ignoreCase="true"/>
          </AttributeRule>
      <AttributeFilterPolicy/>
      </code> which should return One values of eduPersonAffiliation when eduPersonAffiliation attribute has a value of "jsmith"
     * 
     */
    @Test public void testTargettedPolicyRequirement() throws ComponentInitializationException, ResolutionException,
            AttributeFilteringException {

        final AttributeValueFilterPolicy attributeValueFilterPolicy = new AttributeValueFilterPolicy();
        attributeValueFilterPolicy.setAttributeId("eduPersonAffiliation");
        attributeValueFilterPolicy.setMatchingPermittedValues(true);
        attributeValueFilterPolicy.setValueMatcher(valueMatcher());

        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("targettedAtPermit", Predicates.alwaysTrue(),
                        Collections.singleton(attributeValueFilterPolicy));

        final AttributeFilteringEngine engine = new AttributeFilteringEngine("engine", Collections.singleton(policy));

        engine.initialize();

        AttributeFilterContext context = new AttributeFilterContext();
        context.setPrefilteredAttributes(getAttributes("epa-uidwithjsmith.xml").values());
        engine.filterAttributes(context);
        Map<String, Attribute> attributes = context.getFilteredAttributes();
        Assert.assertNull(attributes.get("eduPersonAffiliation"));

        context = new AttributeFilterContext();
        context.setPrefilteredAttributes(getAttributes("uid-epawithjsmith.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredAttributes();
        Attribute attribute = attributes.get("eduPersonAffiliation");
        Assert.assertEquals(attribute.getValues().size(), 1);

        context = new AttributeFilterContext();
        context.setPrefilteredAttributes(getAttributes("epa-uid.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredAttributes();
        Assert.assertNull(attributes.get("eduPersonAffiliation"));
    }

    /**
     * test the following policy.
     * 
     <code>
        <AttributeFilterPolicy id="targettedValueInEPA">
        <PolicyRequirementRule xsi:type="basic:AttributeValueString" value="jsmith" ignoreCase="true"/>
        <AttributeRule attributeID="eduPersonAffiliation">
          <PermitValueRule xsi:type="basic:ANY" />
        </AttributeRul>
      <AttributeFilterPolicy/>
     </code>

    * which should return all values of eduPersonAffiliation when any attribute has a value of "jsmith"
     * 
     */
    @Test public void testTargettedPolicyValue() throws ComponentInitializationException, ResolutionException,
            AttributeFilteringException {

        final AttributeValueFilterPolicy attributeValueFilterPolicy = new AttributeValueFilterPolicy();
        attributeValueFilterPolicy.setAttributeId("eduPersonAffiliation");
        attributeValueFilterPolicy.setMatchingPermittedValues(true);
        attributeValueFilterPolicy.setValueMatcher(MatchFunctor.MATCHES_ALL);
        final Predicate<AttributeFilterContext> pred = new Predicate<AttributeFilterContext>() {
            
            public boolean apply(@Nullable AttributeFilterContext input) {
                try {
                    return valueMatcher().evaluatePolicyRule(input);
                } catch (AttributeFilteringException e) {
                    return false;
                }
            }
        };

        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("targettedAtPermit", pred,  Collections.singleton(attributeValueFilterPolicy));

        final AttributeFilteringEngine engine = new AttributeFilteringEngine("engine", Collections.singleton(policy));

        engine.initialize();

        AttributeFilterContext context = new AttributeFilterContext();
        context.setPrefilteredAttributes(getAttributes("epa-uidwithjsmith.xml").values());
        engine.filterAttributes(context);
        Map<String, Attribute> attributes = context.getFilteredAttributes();
        Attribute attribute = attributes.get("eduPersonAffiliation");
        Assert.assertEquals(attribute.getValues().size(), 3);

        context = new AttributeFilterContext();
        context.setPrefilteredAttributes(getAttributes("uid-epawithjsmith.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredAttributes();
        attribute = attributes.get("eduPersonAffiliation");
        Assert.assertEquals(attribute.getValues().size(), 3);

        context = new AttributeFilterContext();
        context.setPrefilteredAttributes(getAttributes("epa-uid.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredAttributes();
        Assert.assertNull(attributes.get("eduPersonAffiliation"));
    }

}
