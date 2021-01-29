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

package net.shibboleth.idp.attribute.filter.policyrule.logic.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.InitializableComponent;

/** unit tests for {@link AbstractComposedPolicyRule}. */
@SuppressWarnings("javadoc")
public class AbstractComposedPolicyRuleTest {

    @Test
    public void testInitDestroy() throws ComponentInitializationException {
        final List<PolicyRequirementRule> firstList = new ArrayList<>(2);
        ComposedPolicyRule rule = new ComposedPolicyRule(Collections.emptyList());
        
        for (int i = 0; i < 2;i++) {
            firstList.add(new TestMatcher());
        }
        
        rule.destroy();
        
        boolean thrown = false;
        try {
            rule.initialize();
        } catch (final DestroyedComponentException e) {
            thrown = true;
        }
        
        assertTrue(thrown, "Initialize after destroy");

        for (int i = 0; i < 2;i++) {
            firstList.add(new TestMatcher());
        }
        rule = new ComposedPolicyRule(firstList);
        
        assertEquals(firstList.size(), rule.getComposedRules().size());
        
        thrown = false;
        try {
            rule.getComposedRules().add(new TestMatcher());
        } catch (final UnsupportedOperationException e) {
            thrown = true;
        }
        assertTrue(thrown, "Set into the returned list");
        rule.setId("Test");
        
        rule.initialize();
        
        rule.destroy();
    }
    
    @Test
    public void testParams() throws ComponentInitializationException {
        ComposedPolicyRule rule = new ComposedPolicyRule(null);

        assertTrue(rule.getComposedRules().isEmpty(), "Initial state - no matchers");
        assertTrue(rule.getComposedRules().isEmpty(), "Add null - no matchers");
        
        final List<PolicyRequirementRule> list = new ArrayList<>();
        
        rule = new ComposedPolicyRule(list);
        assertTrue(rule.getComposedRules().isEmpty(), "Add List<null> - no matchers");
        
        list.add(new TestMatcher());
        list.add(new TestMatcher());
        list.add(new TestMatcher());
        list.add(new TestMatcher());
        list.add(new TestMatcher());
        list.add(new TestMatcher());
        list.add(new TestMatcher());
        list.add(new TestMatcher());
        list.add(new TestMatcher());
        assertTrue(rule.getComposedRules().isEmpty(), "Change to input list - no matchers");

        rule = new ComposedPolicyRule(list);
        assertEquals(rule.getComposedRules().size(), 9, "Add a List with nulls");
        
        list.clear();
        assertEquals(rule.getComposedRules().size(), 9, "Change to input list");

        rule = new ComposedPolicyRule(list);
        assertTrue(rule.getComposedRules().isEmpty(), "Empty list");

        LoggerFactory.getLogger(AbstractComposedPolicyRuleTest.class).debug(rule.toString());
    }
    
    
    private class ComposedPolicyRule extends AbstractComposedPolicyRule {

        /**
         * Constructor.
         *
         * @param composedMatchers ...
         */
        public ComposedPolicyRule(final List<PolicyRequirementRule> composedMatchers) {
            super();
            setSubsidiaries(composedMatchers);
        }

        @Override
        public Tristate matches(@Nullable final AttributeFilterContext arg0) {
            return Tristate.FALSE;
        }
    }
    
    public static class TestMatcher extends AbstractInitializableComponent implements  PolicyRequirementRule, DestructableComponent, InitializableComponent {

        @Override
        public Tristate matches(@Nullable final AttributeFilterContext arg0) {
            return Tristate.FALSE;
        }

        /** {@inheritDoc} */
        @Override
        @Nullable public String getId() {
            return "99";
        }
        
    }
}
