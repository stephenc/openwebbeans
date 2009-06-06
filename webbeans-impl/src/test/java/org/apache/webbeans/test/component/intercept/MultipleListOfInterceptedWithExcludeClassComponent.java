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
package org.apache.webbeans.test.component.intercept;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.deployment.Production;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

@Production
@RequestScoped
@Interceptors(value = { Interceptor1.class })
public class MultipleListOfInterceptedWithExcludeClassComponent
{

    String s = null;

    @Interceptors(value = { Interceptor2.class })
    @ExcludeClassInterceptors
    public Object intercepted()
    {
        return s;
    }

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception
    {
        this.s = context.getContextData().get("key2").toString();

        return context.proceed();
    }

}
