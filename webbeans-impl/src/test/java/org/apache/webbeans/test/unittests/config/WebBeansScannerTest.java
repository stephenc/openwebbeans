/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.webbeans.test.unittests.config;

import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Named;
import javax.servlet.ServletContext;

import junit.framework.Assert;

import org.apache.webbeans.spi.ServiceLoader;
import org.apache.webbeans.spi.deployer.MetaDataDiscoveryService;
import org.apache.webbeans.test.mock.MockServletContext;
import org.apache.webbeans.test.servlet.TestContext;
import org.apache.webbeans.util.ArrayUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link WebBeansScanner}.
 */
public class WebBeansScannerTest extends TestContext
{

    public WebBeansScannerTest()
    {
        super(WebBeansScannerTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testWebBeansScanner() throws Exception
    {
        MetaDataDiscoveryService scanner = ServiceLoader.getService(MetaDataDiscoveryService.class);

        ServletContext servletContext = new MockServletContext();
        scanner.init(servletContext);

        // try to re-run the scan
        scanner.scan();

        Map<String, Set<String>> classMap = scanner.getClassIndex();
        Assert.assertNotNull(classMap);
        Assert.assertFalse(classMap.isEmpty());
        Set<String> testBeanAnnotations = classMap.get(ScannerTestBean.class.getName());

        String[] expectedAnnotations = new String[] { RequestScoped.class.getName(), Named.class.getName() };

        Assert.assertTrue(ArrayUtil.equalsIgnorePosition(testBeanAnnotations.toArray(), expectedAnnotations));
    }
}
