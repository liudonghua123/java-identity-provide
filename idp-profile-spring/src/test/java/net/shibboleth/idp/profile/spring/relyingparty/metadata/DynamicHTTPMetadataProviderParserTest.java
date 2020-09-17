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

package net.shibboleth.idp.profile.spring.relyingparty.metadata;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.persist.FilesystemLoadSaveManager;
import org.opensaml.core.xml.persist.XMLObjectLoadSaveManager;
import org.opensaml.saml.common.binding.artifact.SAMLSourceIDArtifact;
import org.opensaml.saml.criterion.ArtifactCriterion;
import org.opensaml.saml.metadata.resolver.impl.AbstractDynamicMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.HTTPEntityIDRequestURLBuilder;
import org.opensaml.saml.saml2.binding.artifact.SAML2ArtifactType0004;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.crypto.JCAConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockPropertySource;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.test.repository.RepositorySupport;

@SuppressWarnings("javadoc")
public class DynamicHTTPMetadataProviderParserTest extends AbstractMetadataParserTest {
    
    private static final String PROP_MDURL = "metadataURL";
    
    private static final String REPO_IDP = "java-identity-provider";
    
    private static final String REPO_OPENSAML = "java-opensaml";
    
    private static final String TEMPLATE_URL = "opensaml-saml-impl/src/test/resources/org/opensaml/saml/metadata/resolver/impl/${entityID}.xml";
    
    @Test
    public void testDefaults() throws Exception {
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicDefaults.xml", "beans.xml");
        
        Assert.assertTrue(resolver.isInitialized());
        Assert.assertTrue(resolver.isFailFastInitialization());
        Assert.assertTrue(resolver.isRequireValidMetadata());
        Assert.assertNull(resolver.getMetadataFilter());
        Assert.assertNotNull(resolver.getParserPool());
        Assert.assertTrue(resolver.getIndexes().isEmpty());
        
        Assert.assertEquals(resolver.getNegativeLookupCacheDuration(), Duration.ofMinutes(10));
        Assert.assertEquals(resolver.getRefreshDelayFactor().floatValue(), 0.75f);
        Assert.assertEquals(resolver.getMinCacheDuration(), Duration.ofMinutes(10));
        Assert.assertEquals(resolver.getMaxCacheDuration(), Duration.ofHours(8));
        Assert.assertEquals(resolver.getMaxIdleEntityData(), Duration.ofHours(8));
        Assert.assertTrue(resolver.isRemoveIdleEntityData());
        Assert.assertEquals(resolver.getCleanupTaskInterval(), Duration.ofMinutes(30));
        Assert.assertEquals(resolver.getExpirationWarningThreshold(), Duration.ZERO);
        
        Assert.assertEquals(resolver.getSupportedContentTypes(), 
                Arrays.asList("application/samlmetadata+xml", "application/xml", "text/xml"));
        
        Assert.assertFalse(resolver.isPersistentCachingEnabled());
        
        Assert.assertNull(resolver.getPersistentCacheManager());
        
        Assert.assertNotNull(resolver.getPersistentCacheKeyGenerator());
        Assert.assertTrue(resolver.getPersistentCacheKeyGenerator() instanceof AbstractDynamicMetadataResolver.DefaultCacheKeyGenerator);
        
        Assert.assertNotNull(resolver.getInitializationFromCachePredicate());
        Assert.assertTrue(resolver.getInitializationFromCachePredicate().test(null));  // always true predicate
        
        Assert.assertTrue(resolver.isInitializeFromPersistentCacheInBackground());
        
        Assert.assertEquals(resolver.getBackgroundInitializationFromCacheDelay(), Duration.ofSeconds(2));
        
        Assert.assertEquals(resolver.getRequestURLBuilder().getClass(), HTTPEntityIDRequestURLBuilder.class);
    }
    
    @Test
    public void testIndexes() throws Exception {
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicIndexes.xml", "beans.xml");
        
        Assert.assertFalse(resolver.getIndexes().isEmpty());
        Assert.assertEquals(resolver.getIndexes().size(), 3);
    }

    @Test
    public void testClientSecurityParamsParams() throws Exception {
        getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicClientSecurityParams.xml", "beans.xml", "httpClient.xml");
        
        // We can't really test the actual timeout values, this is just to test that parser, factory bean, etc are ok.
    }
    
    @Test
    public void testTimeoutParams() throws Exception {
        getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicTimeouts.xml", "beans.xml", "httpClient.xml");
        
        // We can't really test the actual timeout values, this is just to test that parser, factory bean, etc are ok.
    }
    
    @Test
    public void testMaxConnectionsParams() throws Exception {
        getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicMaxConnections.xml", "beans.xml", "httpClient.xml");
        
        // We can't really test the actual max values, this is just to test that parser, factory bean, etc are ok.
    }
    
    @Test
    public void testPersistentCacheParamsViaDirectory() throws Exception {
        final ApplicationContext appContext = getApplicationContext("dynamicResolverContext",
                "dynamicPersistentCacheDirectory.xml", "beans.xml", "httpClient.xml");
        
        final MetadataProviderContainer rpProvider = 
                appContext.getBean("dynamicPersistentCacheParamsDirectory", MetadataProviderContainer.class);
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = 
                FunctionDrivenDynamicHTTPMetadataResolver.class.cast(rpProvider.getEmbeddedResolver());
        Assert.assertNotNull(resolver);
        
        Assert.assertTrue(resolver.isInitialized());
        
        Assert.assertTrue(resolver.isPersistentCachingEnabled());
        
        Assert.assertNotNull(resolver.getPersistentCacheManager());
        Assert.assertTrue(resolver.getPersistentCacheManager() instanceof FilesystemLoadSaveManager);
        
        Assert.assertNotNull(resolver.getPersistentCacheKeyGenerator());
        Assert.assertSame(resolver.getPersistentCacheKeyGenerator(), appContext.getBean("digester.SHA1HexLower", Function.class));
        
        Assert.assertNotNull(resolver.getInitializationFromCachePredicate());
        Assert.assertSame(resolver.getInitializationFromCachePredicate(), appContext.getBean("predicate.AlwaysFalse", Predicate.class));
        
        Assert.assertFalse(resolver.isInitializeFromPersistentCacheInBackground());
        
        Assert.assertEquals(resolver.getBackgroundInitializationFromCacheDelay(), Duration.ofSeconds(30));
    }
    
    @Test
    public void testPersistentCacheParamsViaManagerBeanRef() throws Exception {
        final ApplicationContext appContext = getApplicationContext("dynamicResolverContext",
                "dynamicPersistentCacheBean.xml", "beans.xml", "httpClient.xml");
        
        final MetadataProviderContainer rpProvider = 
                appContext.getBean("dynamicPersistentCacheParamsBean", MetadataProviderContainer.class);
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = 
                FunctionDrivenDynamicHTTPMetadataResolver.class.cast(rpProvider.getEmbeddedResolver());
        Assert.assertNotNull(resolver);
        
        Assert.assertTrue(resolver.isInitialized());
        
        Assert.assertTrue(resolver.isPersistentCachingEnabled());
        
        Assert.assertNotNull(resolver.getPersistentCacheManager());
        Assert.assertSame(resolver.getPersistentCacheManager(), appContext.getBean("metadata.persistentCacheManager", XMLObjectLoadSaveManager.class));
        
        Assert.assertNotNull(resolver.getPersistentCacheKeyGenerator());
        Assert.assertSame(resolver.getPersistentCacheKeyGenerator(), appContext.getBean("digester.SHA1HexLower", Function.class));
        
        Assert.assertNotNull(resolver.getInitializationFromCachePredicate());
        Assert.assertSame(resolver.getInitializationFromCachePredicate(), appContext.getBean("predicate.AlwaysFalse", Predicate.class));
        
        Assert.assertFalse(resolver.isInitializeFromPersistentCacheInBackground());
        
        Assert.assertEquals(resolver.getBackgroundInitializationFromCacheDelay(), Duration.ofSeconds(30));
    }
    
    @Test
    public void testBasicParams() throws Exception {
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicBasicParams.xml", "beans.xml", "httpClient.xml");
        
        Assert.assertTrue(resolver.isInitialized());
        Assert.assertFalse(resolver.isFailFastInitialization());
        Assert.assertFalse(resolver.isRequireValidMetadata());
        Assert.assertNull(resolver.getMetadataFilter());
        Assert.assertNotNull(resolver.getParserPool());
        
        Assert.assertEquals(resolver.getNegativeLookupCacheDuration(), Duration.ofMinutes(5));
        Assert.assertEquals(resolver.getRefreshDelayFactor().floatValue(), 0.50f);
        Assert.assertEquals(resolver.getMinCacheDuration(), Duration.ofMinutes(5));
        Assert.assertEquals(resolver.getMaxCacheDuration(), Duration.ofHours(4));
        Assert.assertEquals(resolver.getMaxIdleEntityData(), Duration.ofHours(2));
        Assert.assertFalse(resolver.isRemoveIdleEntityData());
        Assert.assertEquals(resolver.getCleanupTaskInterval(), Duration.ofMinutes(20));
        Assert.assertEquals(resolver.getExpirationWarningThreshold(), Duration.ofHours(3));
        
        Assert.assertEquals(resolver.getSupportedContentTypes(), Collections.singletonList("text/xml"));
        
        Assert.assertEquals(resolver.getRequestURLBuilder().getClass(), HTTPEntityIDRequestURLBuilder.class);
    }
    
    @Test
    public void testWellKnown() throws Exception {
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicWellKnown.xml", "beans.xml");
        
        //TODO update with permanent test target, if there is a better one.
        final String entityID = "https://issues.shibboleth.net/shibboleth";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }

    @Test
    public void testTemplate() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPResourceURL(REPO_OPENSAML, TEMPLATE_URL, false));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamicTemplate.xml", "beans.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }

    @Test
    public void testMDQ() throws Exception {
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicMetadataQueryProtocol.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getSupportedContentTypes(), Collections.singletonList("application/samlmetadata+xml"));

        final String entityID = "https://foo1.example.org/idp/shibboleth";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testMDQWithContentTypeOverride() throws Exception {
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class,
                "dynamicMetadataQueryProtocolWithContentTypeOverride.xml", "beans.xml");

        Assert.assertEquals(resolver.getSupportedContentTypes(), List.of("application/xml", "test/foo"));

        // Note we can't actually execute the request as the test MDQ server only supports application/samlmetadata+xml
    }

    @Test
    public void testMDQWithSecondaryURLBuilderForArtifact() throws Exception {
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicMetadataQueryProtocolWithSecondaryURLBuilders.xml", "beans.xml");
        
        final String entityID = "https://foo1.example.org/idp/shibboleth";
        MessageDigest sha1Digester = MessageDigest.getInstance(JCAConstants.DIGEST_SHA1);
        byte[] entityIDSourceID = sha1Digester.digest(entityID.getBytes("UTF-8"));
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] messageHandle = new byte[20];
        secureRandom.nextBytes(messageHandle);
        SAMLSourceIDArtifact sourceIDArtifact = new SAML2ArtifactType0004(new byte[] {0, 0} , entityIDSourceID, messageHandle);
        
        final CriteriaSet criteriaSet = new CriteriaSet( new ArtifactCriterion(sourceIDArtifact));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testRegex() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPResourceURL(REPO_IDP, "idp-profile-spring/src/test/resources/net/shibboleth/idp/profile/spring/relyingparty/metadata/$1.xml", false));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamicRegex.xml", "beans.xml");
        
        final String entityID = "https://idp.example.org/idp/shibboleth";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHttpCachingNone() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPResourceURL(REPO_OPENSAML, TEMPLATE_URL, false));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-httpCaching-none.xml", "beans.xml", "httpClient.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }

    @Test
    public void testHttpCachingMemory() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPResourceURL(REPO_OPENSAML, TEMPLATE_URL, false));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-httpCaching-memory.xml", "beans.xml", "httpClient.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }

    @Test
    public void testHttpCachingFile() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPResourceURL(REPO_OPENSAML, TEMPLATE_URL, false));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-httpCaching-file.xml", "beans.xml", "httpClient.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSNoTrustEngine() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPResourceURL(REPO_OPENSAML, TEMPLATE_URL, false));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-https-noTrustEngine.xml", "beans.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSTrustEngineExplicitKey() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPSResourceURL(REPO_OPENSAML, TEMPLATE_URL));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-https-trustEngine-explicitKey.xml", "beans.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSTrustEngineInvalidKey() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPSResourceURL(REPO_OPENSAML, TEMPLATE_URL));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-https-trustEngine-invalidKey.xml", "beans.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNull(ed);
    }
    
    @Test
    public void testHTTPSTrustEngineValidPKIX() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPSResourceURL(REPO_OPENSAML, TEMPLATE_URL));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-https-trustEngine-validPKIX.xml", "beans.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSTrustEngineValidPKIXExplicitTrustedName() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPSResourceURL(REPO_OPENSAML, TEMPLATE_URL));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-https-trustEngine-validPKIX-explicitTrustedName.xml", "beans.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSTrustEngineInvalidPKIX() throws Exception {
        MockPropertySource propSource = singletonPropertySource(PROP_MDURL, 
                RepositorySupport.buildHTTPSResourceURL(REPO_OPENSAML, TEMPLATE_URL));
        
        final FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                propSource, "dynamic-https-trustEngine-invalidPKIX.xml", "beans.xml");
        
        final String entityID = "https://www.example.org/sp";
        
        final CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        final EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNull(ed);
    }

}
