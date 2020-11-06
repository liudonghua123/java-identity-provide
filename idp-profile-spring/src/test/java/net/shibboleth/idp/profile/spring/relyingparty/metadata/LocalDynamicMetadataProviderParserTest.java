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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.persist.XMLObjectLoadSaveManager;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.metadata.resolver.impl.AbstractDynamicMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.LocalDynamicMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.crypto.JCAConstants;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.util.concurrent.Uninterruptibles;

import net.shibboleth.utilities.java.support.codec.StringDigester;
import net.shibboleth.utilities.java.support.codec.StringDigester.OutputFormat;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

/**
 *
 */
@SuppressWarnings("javadoc")
public class LocalDynamicMetadataProviderParserTest extends AbstractMetadataParserTest {
    
    private File sourceDirectory;
    
    private final int TIME_GRANULARITY_MS = 25; // Dither for Windows clocks
    
    @BeforeMethod
    public void setUp() throws IOException {
        sourceDirectory = new File(System.getProperty("java.io.tmpdir"), "localDynamicMD");
        resetSourceDirectory();
    }
    
    @AfterMethod
    public void tearDown() throws IOException {
        resetSourceDirectory();
    }
    
    @Test
    public void testDefaults() throws Exception {
        LocalDynamicMetadataResolver resolver = getBean(LocalDynamicMetadataResolver.class, 
                "localDynamicDefaults.xml", "beans.xml");
        
        Assert.assertTrue(resolver.isInitialized());
        Assert.assertTrue(resolver.isFailFastInitialization());
        Assert.assertTrue(resolver.isRequireValidMetadata());
        Assert.assertNull(resolver.getMetadataFilter());
        Assert.assertNotNull(resolver.getParserPool());
        
        Assert.assertEquals(resolver.getNegativeLookupCacheDuration(), Duration.ofMinutes(10));
        Assert.assertEquals(resolver.getRefreshDelayFactor().floatValue(), 0.75f);
        Assert.assertEquals(resolver.getMinCacheDuration(), Duration.ofMinutes(10));
        Assert.assertEquals(resolver.getMaxCacheDuration(), Duration.ofHours(8));
        Assert.assertEquals(resolver.getMaxIdleEntityData(), Duration.ofHours(8));
        Assert.assertTrue(resolver.isRemoveIdleEntityData());
        Assert.assertEquals(resolver.getCleanupTaskInterval(), Duration.ofMinutes(30));
        Assert.assertEquals(resolver.getExpirationWarningThreshold(), Duration.ZERO);
        
        Assert.assertFalse(resolver.isPersistentCachingEnabled());
        
        Assert.assertNull(resolver.getPersistentCacheManager());
        
        Assert.assertNotNull(resolver.getPersistentCacheKeyGenerator());
        Assert.assertTrue(resolver.getPersistentCacheKeyGenerator() instanceof AbstractDynamicMetadataResolver.DefaultCacheKeyGenerator);
        
        Assert.assertNotNull(resolver.getInitializationFromCachePredicate());
        Assert.assertTrue(resolver.getInitializationFromCachePredicate().test(null));  // always true predicate
        
        Assert.assertTrue(resolver.isInitializeFromPersistentCacheInBackground());
        
        Assert.assertEquals(resolver.getBackgroundInitializationFromCacheDelay(), Duration.ofSeconds(2));
        
    }
        
    @Test
    public void testSourceManagerAndGenerator() throws Exception {
        // Test that correctly use the specified source manager and key generator
        String entityID = "urn:test:entity1";
        EntityDescriptor entity = (EntityDescriptor) XMLObjectSupport.buildXMLObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        entity.setEntityID(entityID);
        
        ApplicationContext appContext = getApplicationContext("LocalDynamic", 
                "localDynamicWithManagerAndGenerator.xml", "beans.xml");

        XMLObjectLoadSaveManager<XMLObject> sourceManager = 
                appContext.getBean("metadata.LocalDynamicSourceManager", XMLObjectLoadSaveManager.class);

        MetadataProviderContainer rpProvider = appContext.getBean("localDynamicWithManagerAndGenerator", MetadataProviderContainer.class);
        LocalDynamicMetadataResolver resolver = (LocalDynamicMetadataResolver) rpProvider.getEmbeddedResolver(); 
        
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(entityID));
        
        Assert.assertNull(resolver.resolveSingle(criteria));
            
        sourceManager.save(entityID, entity);
        
        // Configured negative lookup cache should still be in effect
        Assert.assertNull(resolver.resolveSingle(criteria));
        
        // Sleep past the negative lookup cache expiration
        Uninterruptibles.sleepUninterruptibly(resolver.getNegativeLookupCacheDuration().toMillis()+TIME_GRANULARITY_MS, TimeUnit.MILLISECONDS);
        
        // In this case, will be the same instance since using in-memory map-based store.
        Assert.assertSame(resolver.resolveSingle(criteria), entity);
    }

    @Test
    public void testSourceDirectory() throws Exception {
        // Test that can correctly resolve when sourceDirectory is supplied
        String entityID = "urn:test:entity1";
        EntityDescriptor entity = (EntityDescriptor) XMLObjectSupport.buildXMLObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        entity.setEntityID(entityID);
        
        if (!sourceDirectory.exists()) {
            Assert.assertTrue(sourceDirectory.mkdirs());
        }
        StringDigester digester = new StringDigester(JCAConstants.DIGEST_SHA1, OutputFormat.HEX_LOWER);
        String sourceKey = digester.apply(entityID) + ".xml";
        File sourceFile = new File(sourceDirectory, sourceKey);
        
        // First clear anything from previous test, and sanity check the setup
        if (sourceFile.exists()) {
            sourceFile.delete();
            Assert.assertFalse(sourceFile.exists());
        }
        
        LocalDynamicMetadataResolver resolver = getBean(LocalDynamicMetadataResolver.class, 
                "localDynamicWithSourceDirectory.xml", "beans.xml");
        
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(entityID));
        
        Assert.assertNull(resolver.resolveSingle(criteria));
        
        XMLObjectSupport.marshallToOutputStream(entity, new FileOutputStream(sourceFile));
        Assert.assertTrue(sourceFile.exists());
        
        // Configured negative lookup cache should still be in effect
        Assert.assertNull(resolver.resolveSingle(criteria));
        
        // Sleep past the negative lookup cache expiration
        Uninterruptibles.sleepUninterruptibly(resolver.getNegativeLookupCacheDuration().toMillis()+TIME_GRANULARITY_MS, TimeUnit.MILLISECONDS);
        
        EntityDescriptor resolved = resolver.resolveSingle(criteria);
        Assert.assertNotNull(resolved);
        Assert.assertEquals(resolved.getEntityID(), entityID);
    }
    
    @Test
    public void testSourceDirectoryWithIntermediateNumberAndLength() throws Exception {
        // Test that can correctly resolve when sourceDirectory is supplied with intermediate dir segment number and length
        // SHA-1 hashes to: 9339685fdddbf8e45faac94340a3260468aa65bd
        String entityID = "urn:test:entity1";
        EntityDescriptor entity = (EntityDescriptor) XMLObjectSupport.buildXMLObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        entity.setEntityID(entityID);
        
        if (!sourceDirectory.exists()) {
            Assert.assertTrue(sourceDirectory.mkdirs());
        }
        StringDigester digester = new StringDigester(JCAConstants.DIGEST_SHA1, OutputFormat.HEX_LOWER);
        String sourceKey = digester.apply(entityID) + ".xml";
        // Using segment number=1 and length=2
        File sourceFile = Path.of(sourceDirectory.getAbsolutePath(), "93", sourceKey).toFile();
        
        // First clear anything from previous test, and sanity check the setup
        if (sourceFile.exists()) {
            sourceFile.delete();
            Assert.assertFalse(sourceFile.exists());
        }
        
        LocalDynamicMetadataResolver resolver = getBean(LocalDynamicMetadataResolver.class, 
                "localDynamicWithSourceDirectoryAndIntermediateNumberAndLength.xml", "beans.xml");
        
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(entityID));
        
        Assert.assertNull(resolver.resolveSingle(criteria));
        
        sourceFile.getParentFile().mkdirs();
        XMLObjectSupport.marshallToOutputStream(entity, new FileOutputStream(sourceFile));
        Assert.assertTrue(sourceFile.exists());
        
        // Configured negative lookup cache should still be in effect
        Assert.assertNull(resolver.resolveSingle(criteria));
        
        // Sleep past the negative lookup cache expiration
        Uninterruptibles.sleepUninterruptibly(resolver.getNegativeLookupCacheDuration().toMillis()+TIME_GRANULARITY_MS, TimeUnit.MILLISECONDS);
        
        EntityDescriptor resolved = resolver.resolveSingle(criteria);
        Assert.assertNotNull(resolved);
        Assert.assertEquals(resolved.getEntityID(), entityID);
    }
    
    @Test
    public void testSourceDirectoryWithIntermediateStrategyRef() throws Exception {
        // Test that can correctly resolve when sourceDirectory is supplied with intermediate dir strategy ref
        // SHA-1 hashes to: 9339685fdddbf8e45faac94340a3260468aa65bd
        String entityID = "urn:test:entity1";
        EntityDescriptor entity = (EntityDescriptor) XMLObjectSupport.buildXMLObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        entity.setEntityID(entityID);
        
        if (!sourceDirectory.exists()) {
            Assert.assertTrue(sourceDirectory.mkdirs());
        }
        StringDigester digester = new StringDigester(JCAConstants.DIGEST_SHA1, OutputFormat.HEX_LOWER);
        String sourceKey = digester.apply(entityID) + ".xml";
        // Using segment number=2 and length=2
        File sourceFile = Path.of(sourceDirectory.getAbsolutePath(), "93", "39", sourceKey).toFile();
        
        // First clear anything from previous test, and sanity check the setup
        if (sourceFile.exists()) {
            sourceFile.delete();
            Assert.assertFalse(sourceFile.exists());
        }
        
        LocalDynamicMetadataResolver resolver = getBean(LocalDynamicMetadataResolver.class, 
                "localDynamicWithSourceDirectoryAndIntermediateStrategyRef.xml", "beans.xml");
        
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(entityID));
        
        Assert.assertNull(resolver.resolveSingle(criteria));
        
        sourceFile.getParentFile().mkdirs();
        XMLObjectSupport.marshallToOutputStream(entity, new FileOutputStream(sourceFile));
        Assert.assertTrue(sourceFile.exists());
        
        // Configured negative lookup cache should still be in effect
        Assert.assertNull(resolver.resolveSingle(criteria));
        
        // Sleep past the negative lookup cache expiration
        Uninterruptibles.sleepUninterruptibly(resolver.getNegativeLookupCacheDuration().toMillis()+TIME_GRANULARITY_MS, TimeUnit.MILLISECONDS);
        
        EntityDescriptor resolved = resolver.resolveSingle(criteria);
        Assert.assertNotNull(resolved);
        Assert.assertEquals(resolved.getEntityID(), entityID);
    }
    
    @Test(expectedExceptions=BeanDefinitionParsingException.class)
    public void testSourceDirectoryIntermediateNumberWithoutLength() throws Exception {
        getBean(LocalDynamicMetadataResolver.class, "localDynamicWithSourceDirectoryAndIntermediateNumberWithoutLength.xml", "beans.xml");
    }
    
    @Test(expectedExceptions=BeanDefinitionParsingException.class)
    public void testSourceDirectoryIntermediateLengthWithoutNumber() throws Exception {
        getBean(LocalDynamicMetadataResolver.class, "localDynamicWithSourceDirectoryAndIntermediateLengthWithoutNumber.xml", "beans.xml");
    }
    
    @Test(expectedExceptions=BeanDefinitionParsingException.class)
    public void testMissingSourceDirectoryAndSourceManager() throws Exception {
        getBean(LocalDynamicMetadataResolver.class, "localDynamicMissingSourceManagerAndSourceDirectory.xml", "beans.xml");
    }
    
    // Helpers

    private void resetSourceDirectory() throws IOException {
        if (sourceDirectory.exists()) {
            if (sourceDirectory.isDirectory()) {
                Files.walk(sourceDirectory.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
            } else {
                sourceDirectory.delete();
            }
        }
    }
    
}
