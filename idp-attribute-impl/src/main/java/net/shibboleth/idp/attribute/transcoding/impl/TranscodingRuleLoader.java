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

package net.shibboleth.idp.attribute.transcoding.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.google.common.base.Predicates;

import net.shibboleth.idp.attribute.transcoding.TranscodingRule;
import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A mechanism for loading a set of {@link TranscodingRule} objects from sources such as maps
 * or directories of property files.
 */
public class TranscodingRuleLoader {
    
    /** Class logger. */
    @Nonnull private Logger log = LoggerFactory.getLogger(TranscodingRuleLoader.class);
    
    /** Rules loaded. */
    private @Nonnull @NonnullElements final Collection<TranscodingRule> rules;
    
    /**
     * Load rules from all files found below a directory root.
     * 
     * <p>Files are assumed to be Java property files in text format.</p>
     * 
     * <p>Individual rules that fail to load will be skipped.</p>
     * 
     * @param dir root to search
     * 
     * @throws IOException if an error occurs
     */
    public TranscodingRuleLoader(@Nonnull @ParameterName(name="dir") final Path dir) throws IOException {

        log.debug("Loading rules from directory ({})", dir);
        rules = new ArrayList<>();
        
        try (final DirectoryStream<Path> dirstream  = Files.newDirectoryStream(dir)) {
            for (final Path child : dirstream) {
                final File file =  child.toFile();
                if (file.isDirectory()) {
                    try {
                        rules.addAll(new TranscodingRuleLoader(child).getRules());
                    } catch (final IOException e) {
                        log.error("Failed to load rules from directory ({})", file, e);
                    }
                } else {
                    log.debug("Loading rule from property set in file ({})", file);
                    try {
                        rules.add(TranscodingRule.fromResource(new FileSystemResource(file)));
                    } catch (final IOException e) {
                        log.error("Failed to load rule from file ({})", file, e);
                    }
                }
            }
        }
    }
    
    /**
     * Constructor.
     *
     * @param maps a collection of maps to build rules around directly.
     */
    public TranscodingRuleLoader(
            @Nonnull @NonnullElements @ParameterName(name="maps") final Collection<Map<String,Object>> maps) {
        Constraint.isNotNull(maps, "Input collection cannot be null");
        
        rules = maps
                .stream()
                .filter(Predicates.notNull())
                .map(m -> {
                    return new TranscodingRule(m);
                    })
                .collect(Collectors.toList());
    }

    /**
     * Get the rules loaded by this object.
     * 
     * @return collection of rules
     */
    @Nonnull @NonnullElements public Collection<TranscodingRule> getRules() {
        return rules;
    }
    
}