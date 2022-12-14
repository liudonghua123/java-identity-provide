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

package net.shibboleth.idp.authn.spnego.impl;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.shibboleth.shared.component.ComponentInitializationException;

public class KerberosSettingsTest {

    protected static String SERVICE_PRINCIPAL = "HTTP/aai-logon.domain_a.com@DOMAIN_A.COM";

    protected static String KEYTAB = "/opt/kerberos/http_domainA.keytab";

    protected List<KerberosRealmSettings> realms;

    private KerberosSettings settings;

    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        settings = new KerberosSettings();
        KerberosRealmSettings realm1 = new KerberosRealmSettings();
        realm1.setServicePrincipal(SERVICE_PRINCIPAL);
        realm1.setKeytab(KEYTAB);
        realm1.initialize();

        realms = new ArrayList<>();
        realms.add(realm1);
    }

    @Test
    public void testRequiredValues() {
        settings.setRealms(realms);

        assertEquals(settings.getRealms(), realms);
    }
    
}