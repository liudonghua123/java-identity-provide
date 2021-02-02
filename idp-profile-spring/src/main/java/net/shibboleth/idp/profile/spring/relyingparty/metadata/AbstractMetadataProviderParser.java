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

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.shibboleth.ext.spring.util.AbstractCustomBeanDefinitionParser;
import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/**
 * Parser for the MetadataProviderType in the <code>urn:mace:shibboleth:2.0:metadata</code> namespace.
 * 
 *  If we are the top most element then we need to summon up a
 * {@link MetadataProviderContainer} and inject what we would usually create into that.
 */
public abstract class AbstractMetadataProviderParser extends AbstractCustomBeanDefinitionParser {

    /** Namespace for Security. */
    @Nonnull @NotEmpty public static final String SECURITY_NAMESPACE = "urn:mace:shibboleth:2.0:security";
    
    /** Namespace for Metadata. */
    @Nonnull @NotEmpty public static final String METADATA_NAMESPACE = "urn:mace:shibboleth:2.0:metadata";
    
    /** MetadataFilter Element name. */
    @Nonnull public static final QName METADATA_FILTER_ELEMENT_NAME = new QName(METADATA_NAMESPACE, "MetadataFilter");
    
    /** ChainingMetadataProviderElement name. */
    @Nonnull public static final QName CHAINING_PROVIDER_ELEMENT_NAME = 
            new QName(METADATA_NAMESPACE, "ChainingMetadataProvider");
    
    /** RelyingPartyGroup Element name. */
    @Nonnull public static final QName TRUST_ENGINE_ELEMENT_NAME = new QName(SECURITY_NAMESPACE, "TrustEngine");

    /** Logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractMetadataProviderParser.class);

    /**
     * Handle attributes which are inappropriate for specific implementations. The chaining metadata provider cannot
     * have "requireValidMetadata" or "failFastInitialization" set, even though they are present in the schema.
     * 
     * <p>
     * This method detects whether these elements are present and if the element is not a chaining provider returns
     * true, otherwise it returns false and emits a warning.
     * </p>
     * 
     * @param element the element
     * @param attribute the attribute
     * @return true iff this is not a chaining resolver and the attribute is present
     */
    private boolean isPresentNotChaining(@Nonnull final Element element, @Nonnull final String attribute) {

        if (!element.hasAttributeNS(null, attribute)) {
            return false;
        } else if (isChaining(element)) {
            log.warn("{} is not valid for {}", attribute, CHAINING_PROVIDER_ELEMENT_NAME.getLocalPart());
            return false;
        }
        return true;
    }
    
    /**
     * Is this a chaining resolver?
     * 
     * @param element root element of resolver
     * 
     * @return whether the type is Chaining
     */
    private boolean isChaining(@Nonnull final Element element) {
        return CHAINING_PROVIDER_ELEMENT_NAME.equals(DOMTypeSupport.getXSIType(element));
    }

    /**
     * Is this the element at the top of the file? Yes, if it has no parent. In
     * this situation we need to wrap the element in a {@link MetadataProviderContainer}.
     * 
     * @param element the element.
     * @return whether it is the outmost element.
     */
    private boolean isTopMost(@Nonnull final Element element) {
        final Node parent = element.getParentNode();

        return parent.getNodeType() == Node.DOCUMENT_NODE;
    }

    /**
     * Return the real class implement by this type. This has the same function as the more usual
     * {@link AbstractSingleBeanDefinitionParser#getBeanClass(Element)} but it may need to be shimmed in
     * {@link AbstractMetadataProviderParser} which may need to insert an extra bean.
     * 
     * @param element the {@code Element} that is being parsed
     * @return the {@link Class} of the bean that is being defined via parsing the supplied {@code Element}, or
     *         {@code null} if none
     * @see #getBeanClassName
     */
    protected abstract Class<? extends MetadataResolver> getNativeBeanClass(Element element);

    /** {@inheritDoc} */
    @Override protected final Class<?> getBeanClass(final Element element) {
        if (isTopMost(element)) {
            return MetadataProviderContainer.class;
        }
        return getNativeBeanClass(element);
    }

    /** {@inheritDoc} */
    @Override protected final void doParse(final Element element, final ParserContext parserContext, 
            final BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        if (isTopMost(element)) {
            builder.setInitMethodName("initialize");
            builder.setDestroyMethodName("destroy");
            builder.setLazyInit(true);
            final BeanDefinitionBuilder childBeanDefinitionBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(getNativeBeanClass(element));
            doNativeParse(element, parserContext, childBeanDefinitionBuilder);

            builder.addPropertyValue("embeddedResolver", childBeanDefinitionBuilder.getBeanDefinition());

            if (element.hasAttributeNS(null, "sortKey")) {
                builder.addPropertyValue("sortKey", StringSupport.trimOrNull(element.getAttributeNS(null, "sortKey")));
            }
        } else {
            if (element.hasAttributeNS(null, "sortKey")) {
                log.warn("{} sortKey is only valid on 'top level' MetadataProviders", parserContext.getReaderContext()
                        .getResource().getDescription());
            }
            doNativeParse(element, parserContext, builder);
        }
    }

    /**
     * Parse the element into the provider builder. This has the same function as the more usual
     * {@link AbstractSingleBeanDefinitionParser#doParse(Element, ParserContext, BeanDefinitionBuilder)} but it may need
     * to be shimmed in this class which may need to insert an extra bean.
     * 
     * @param element the XML element being parsed
     * @param parserContext the object encapsulating the current state of the parsing process
     * @param builder used to define the {@code BeanDefinition}
     * @see #doParse(Element, BeanDefinitionBuilder)
     */
    protected void doNativeParse(final Element element, final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {

        builder.setInitMethodName("initialize");
        builder.setDestroyMethodName("destroy");
        builder.setLazyInit(true);

        builder.addPropertyValue("id", StringSupport.trimOrNull(element.getAttributeNS(null, "id")));

        if (isPresentNotChaining(element, "failFastInitialization")) {
            builder.addPropertyValue("failFastInitialization",
                    SpringSupport.getStringValueAsBoolean(element.getAttributeNS(null, "failFastInitialization")));
        }

        if (isPresentNotChaining(element, "requireValidMetadata")) {
            builder.addPropertyValue("requireValidMetadata",
                    SpringSupport.getStringValueAsBoolean(element.getAttributeNS(null, "requireValidMetadata")));
        }

        processPredicateOptions(element, parserContext, builder);

        final List<Element> filters =
                ElementSupport.getChildElements(element, METADATA_FILTER_ELEMENT_NAME);
        if (null != filters && !filters.isEmpty()) {
            if (!isChaining(element)) {
                if (filters.size() == 1) {
                    // Install directly.
                    builder.addPropertyValue("metadataFilter",
                            SpringSupport.parseCustomElement(filters.get(0), parserContext, builder, false));
                } else if (filters.size() > 1) {
                    // Wrap in a chaining filter.
                    final BeanDefinitionBuilder chainBuilder =
                            BeanDefinitionBuilder.genericBeanDefinition(MetadataFilterChain.class);
                    chainBuilder.addPropertyValue("filters", SpringSupport.parseCustomElements(filters, parserContext,
                            chainBuilder));
                    builder.addPropertyValue("metadataFilter", chainBuilder.getBeanDefinition());
                }
            } else {
                log.warn("MetadataFilter is not valid for {}", CHAINING_PROVIDER_ELEMENT_NAME.getLocalPart());
            }
        }
    }

    /**
     * Process predicate-related options.
     * 
     * @param element the current element being processed
     * @param parserContext the current parser context
     * @param builder the current bean definition builder
     */
    private void processPredicateOptions(final Element element, final ParserContext parserContext, 
            final BeanDefinitionBuilder builder) {
        
        if (isPresentNotChaining(element, "satisfyAnyPredicates")) {
            builder.addPropertyValue("satisfyAnyPredicates",
                    SpringSupport.getStringValueAsBoolean(element.getAttributeNS(null, "satisfyAnyPredicates")));
        }

        if (isPresentNotChaining(element, "useDefaultPredicateRegistry")) {
            builder.addPropertyValue("useDefaultPredicateRegistry",
                    SpringSupport.getStringValueAsBoolean(element.getAttributeNS(null, "useDefaultPredicateRegistry")));
        }

        if (isPresentNotChaining(element, "criterionPredicateRegistryRef")) {
            builder.addPropertyReference("criterionPredicateRegistry",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "criterionPredicateRegistryRef")));
        }
    }
}
