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

package net.shibboleth.idp.attribute.filter.complex.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import net.shibboleth.ext.spring.testing.MockApplicationContext;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.filter.AttributeFilterException;
import net.shibboleth.idp.attribute.filter.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filter.AttributeRule;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.MatcherFromPolicy;
import net.shibboleth.idp.attribute.filter.PolicyFromMatcherId;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.impl.AttributeFilterImpl;
import net.shibboleth.idp.attribute.filter.matcher.impl.AttributeValueStringMatcher;
import net.shibboleth.idp.attribute.filter.matcher.logic.impl.NotMatcher;
import net.shibboleth.idp.attribute.filter.matcher.logic.impl.NotMatcherTest;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

/**
 * Complex test for AttributeRuleFilters when the rule is targeted
 */
public class TargettedAttributeValueFilterTest extends BaseComplexAttributeFilterTestCase {
    
    /*
     * We will test this rule: xsi:type="basic:AttributeValueString" value="jsmith" attributeId="uid" ignoreCase="true"
     */
    private Matcher valueMatcher() {
        AttributeValueStringMatcher retVal = new AttributeValueStringMatcher();

        retVal.setCaseSensitive(true);
        retVal.setMatchString("jsmith");
        retVal.setId("Test");
        try {
            retVal.initialize();
        } catch (final ComponentInitializationException e) {
            retVal = null;
        }

        return retVal;
    }

    /**
     * Test the following policy.
     * 
     * <pre>
     * &lt;AttributeFilterPolicy id="targettedValueInEPA"&gt;
     *     &lt;PolicyRequirementRule xsi:type="basic:ANY" /&gt;
     *     &lt;AttributeRule attributeID="eduPersonAffiliation"&gt;
     *         &lt;PermitValueRule xsi:type="basic:AttributeValueString" value="jsmith" attributeId="uid" ignoreCase="true"/&gt;
     *     &lt;/AttributeRule&gt;
     * &lt;/AttributeFilterPolicy&gt;
     * </pre>
     * 
     * which should return all values of eduPersonAffiliation when uid has a "jsmith"
     *  
     * @throws ComponentInitializationException ...
     * @throws ResolutionException ...
     * @throws AttributeFilterException ...
     */
    @Test public void testTargettedPolicyRequirement() throws ComponentInitializationException, ResolutionException,
            AttributeFilterException {

        final AttributeRule attributeValueFilterPolicy = new AttributeRule();
        attributeValueFilterPolicy.setId("test");
        attributeValueFilterPolicy.setAttributeId("eduPersonAffiliation");
        final PolicyFromMatcherId pfm = new PolicyFromMatcherId(valueMatcher(), "uid");
        pfm.setId("pfm");
        final MatcherFromPolicy mfp = new MatcherFromPolicy(pfm);
        mfp.setId("mfp");
        
        attributeValueFilterPolicy.setMatcher(mfp);
        attributeValueFilterPolicy.setIsDenyRule(false);

        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("targettedAtPermit", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singleton(attributeValueFilterPolicy));

        final AttributeFilterImpl engine = new AttributeFilterImpl("engine", Collections.singleton(policy));
        engine.setApplicationContext(new MockApplicationContext());

        ComponentSupport.initialize(attributeValueFilterPolicy);
        ComponentSupport.initialize(policy);
        ComponentSupport.initialize(engine);

        AttributeFilterContext context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("epa-uidwithjsmith.xml").values());
        engine.filterAttributes(context);
        Map<String, IdPAttribute> attributes = context.getFilteredIdPAttributes();
        final IdPAttribute attribute = attributes.get("eduPersonAffiliation");
        assertEquals(attribute.getValues().size(), 3);

        context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("uid-epawithjsmith.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredIdPAttributes();
        assertNull(attributes.get("eduPersonAffiliation"));

        context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("epa-uid.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredIdPAttributes();
        assertNull(attributes.get("eduPersonAffiliation"));
    }

    /**
     * Test the following policy.
     * 
     * <pre>
     * &lt;AttributeFilterPolicy id="targettedValueInEPA"&gt;
     *     &lt;PolicyRequirementRule xsi:type="basic:ANY" /&gt; 
     *     &lt;AttributeRule attributeID="eduPersonAffiliation"&gt;
     *         &lt;PermitValueRule xsi:type="basic:Not"&gt;
     *             &lt;basic:Rule xsi:type="basic:AttributeValueString" value="jsmith" attributeId="uid" ignoreCase="true"/&gt;
     *         &lt;/PermitValueRule&gt;
     *     &lt;/AttributeRule&gt;
     * &lt;/AttributeFilterPolicy&gt;
     * </pre>
     * 
     * which should return all values of eduPersonAffiliation when uid has a "jsmith"
     * 
     * @throws ComponentInitializationException ...
     * @throws ResolutionException ...
     * @throws AttributeFilterException ...
     */
    @Test public void testTargettedNotPolicyRequirement() throws ComponentInitializationException, ResolutionException,
            AttributeFilterException {

        final AttributeRule attributeValueFilterPolicy = new AttributeRule();
        attributeValueFilterPolicy.setId("test");
        attributeValueFilterPolicy.setAttributeId("eduPersonAffiliation");
        final NotMatcher notM = NotMatcherTest.newNotMatcher(valueMatcher());
        notM.setId("notM");
        notM.initialize();
        final PolicyFromMatcherId pfm = new PolicyFromMatcherId(notM, "uid");
        pfm.setId("pfm");
        final MatcherFromPolicy mfp = new MatcherFromPolicy(pfm);
        mfp.setId("mfp");
        
        attributeValueFilterPolicy.setMatcher(mfp);
        attributeValueFilterPolicy.setIsDenyRule(false);

        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("targettedAtPermit", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singleton(attributeValueFilterPolicy));

        final AttributeFilterImpl engine = new AttributeFilterImpl("engine", Collections.singleton(policy));
        engine.setApplicationContext(new MockApplicationContext());


        ComponentSupport.initialize(attributeValueFilterPolicy);
        ComponentSupport.initialize(policy);
        ComponentSupport.initialize(engine);

        AttributeFilterContext context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("epa-uidwithjsmith.xml").values());
        engine.filterAttributes(context);
        Map<String, IdPAttribute> attributes = context.getFilteredIdPAttributes();
        final IdPAttribute attribute = attributes.get("eduPersonAffiliation");
        assertEquals(attribute.getValues().size(), 3);

        context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("uid-epawithjsmith.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredIdPAttributes();
        assertEquals(attribute.getValues().size(), 3);

        context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("epa-uid.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredIdPAttributes();
        assertEquals(attribute.getValues().size(), 3);
    }

    /**
     * Test the following policy.
     * 
     * <pre>
     * &lt;AttributeFilterPolicy id="targettedValueInEPA"&gt;
     *   &lt;PolicyRequirementRule xsi:type="basic:AttributeValueString" value="jsmith" attributeId="uid" ignoreCase="true"/&gt;
     *   &lt;AttributeRule attributeID="eduPersonAffiliation"&gt;
     *     &lt;PermitValueRule xsi:type="basic:ANY" /&gt;
     *   &lt;/AttributeRule&gt;
     * &lt;AttributeFilterPolicy/&gt;
     * </pre>
     * 
     * which should return all values of eduPersonAffiliation when uid has a "jsmith"
     * 
     * @throws ComponentInitializationException ...
     * @throws ResolutionException ...
     * @throws AttributeFilterException ...
     */
    @Test public void testTargettedPolicyValue() throws ComponentInitializationException, ResolutionException,
            AttributeFilterException {

        final AttributeRule attributeValueFilterPolicy = new AttributeRule();
        attributeValueFilterPolicy.setId("test");
        attributeValueFilterPolicy.setAttributeId("eduPersonAffiliation");
        attributeValueFilterPolicy.setMatcher(Matcher.MATCHES_ALL);
        attributeValueFilterPolicy.setIsDenyRule(false);

        final PolicyFromMatcherId rule = new PolicyFromMatcherId(valueMatcher(), "uid");
        rule.setId("rule");
        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("targettedAtPermit", rule,  Collections.singleton(attributeValueFilterPolicy));

        final AttributeFilterImpl engine = new AttributeFilterImpl("engine", Collections.singleton(policy));
        engine.setApplicationContext(new MockApplicationContext());

        ComponentSupport.initialize(attributeValueFilterPolicy);
        ComponentSupport.initialize(policy);
        ComponentSupport.initialize(engine);

        AttributeFilterContext context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("epa-uidwithjsmith.xml").values());
        engine.filterAttributes(context);
        Map<String, IdPAttribute> attributes = context.getFilteredIdPAttributes();
        final IdPAttribute attribute = attributes.get("eduPersonAffiliation");
        assertEquals(attribute.getValues().size(), 3);

        context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("uid-epawithjsmith.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredIdPAttributes();
        assertNull(attributes.get("eduPersonAffiliation"));

        context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(getIdPAttributes("epa-uid.xml").values());
        engine.filterAttributes(context);
        attributes = context.getFilteredIdPAttributes();
        assertNull(attributes.get("eduPersonAffiliation"));
    }

}
