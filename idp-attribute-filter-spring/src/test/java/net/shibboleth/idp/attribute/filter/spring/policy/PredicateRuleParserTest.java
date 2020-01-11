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

package net.shibboleth.idp.attribute.filter.spring.policy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import net.shibboleth.ext.spring.util.SchemaTypeAwareXMLBeanDefinitionReader;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl.PredicatePolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

@SuppressWarnings("javadoc")
public class PredicateRuleParserTest extends BaseAttributeFilterParserTest {

    @Test public void policy() throws ComponentInitializationException {
        GenericApplicationContext ctx = new GenericApplicationContext();
        setTestContext(ctx);
        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader = new SchemaTypeAwareXMLBeanDefinitionReader(ctx);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH 
                + "predicate1.xml"), new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH
                + "predicateBeans.xml"));

        ctx.refresh();

        final PredicatePolicyRule rule = (PredicatePolicyRule) getPolicyRuleFromAttributeFilterPolicy(ctx);

        assertEquals(rule.getRulePredicate().getClass(), Foo.class);
        assertNull(rule.getProfileContextStrategy().apply(new AttributeFilterContext()));
        ProfileRequestContext pc = new ProfileRequestContext();
        assertSame(
                rule.getProfileContextStrategy().apply(
                        pc.getSubcontext(RelyingPartyContext.class, true).getSubcontext(AttributeFilterContext.class,
                                true)), pc);
    }

    @Test public void strategy() throws ComponentInitializationException {
        GenericApplicationContext ctx = new GenericApplicationContext();
        setTestContext(ctx);
        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader = new SchemaTypeAwareXMLBeanDefinitionReader(ctx);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH 
                + "predicateBeans.xml"), new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH 
                + "predicate2.xml"));

        ctx.refresh();

        final PredicatePolicyRule rule = (PredicatePolicyRule) getPolicyRuleFromAttributeFilterPolicy(ctx);

        assertEquals(rule.getRulePredicate().getClass(), Foo.class);
        assertNotNull(rule.getProfileContextStrategy().apply(new AttributeFilterContext()));
        assertEquals(rule.getProfileContextStrategy().getClass(), Func.class);
    }

    private AttributeFilterContext prcFor(String sp) {

        final ProfileRequestContext prc = new ProfileRequestContext();
        final  RelyingPartyContext rpc = prc.getSubcontext(RelyingPartyContext.class, true);rpc.setRelyingPartyId(sp);
        return rpc.getSubcontext(AttributeFilterContext.class, true);
    }
    

    @Test public void rp() throws Exception {
        GenericApplicationContext ctx = new GenericApplicationContext();
        setTestContext(ctx);
        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader = new SchemaTypeAwareXMLBeanDefinitionReader(ctx);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH
                + "predicateBeans.xml"), new ClassPathResource(BaseAttributeFilterParserTest.POLICY_RULE_PATH
                + "predicateRp.xml"));

        ctx.refresh();

        final PredicatePolicyRule rule = (PredicatePolicyRule) getPolicyRuleFromAttributeFilterPolicy(ctx);
        assertEquals(rule.matches(prcFor("https://example.org")), Tristate.FALSE);
        assertEquals(rule.matches(prcFor("https://sp.example.org")), Tristate.TRUE);
        assertEquals(rule.matches(prcFor("https://sp2.example.org")), Tristate.TRUE);

    }

    static class Foo implements Predicate<AttributeFilterContext> {

        /** {@inheritDoc} */
        public boolean test(@Nullable AttributeFilterContext input) {
            return false;
        }

    }

    static class Func implements Function<AttributeFilterContext, ProfileRequestContext> {

        /** {@inheritDoc} */
        @Override public ProfileRequestContext apply(@Nullable AttributeFilterContext input) {
            return new ProfileRequestContext();
        }

    }

}
