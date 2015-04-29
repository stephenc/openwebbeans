/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.conversation;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.webbeans.context.ConversationContext;

/**
 * Bean used to create the map of conversations in a session
 */
public class ConversationStorageBean implements Bean<Set<ConversationContext>>, PassivationCapable
{
    public static final ConversationStorageBean INSTANCE = new ConversationStorageBean();

    @Override
    public Set<ConversationContext> create(CreationalContext<Set<ConversationContext>> creationalContext)
    {
        return Collections.newSetFromMap(new ConcurrentHashMap<ConversationContext, Boolean>());
    }

    @Override
    public void destroy(Set<ConversationContext> instance, CreationalContext<Set<ConversationContext>> context)
    {
        for (ConversationContext conversationContext : instance)
        {
            conversationContext.destroy();
        }
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return Collections.EMPTY_SET;
    }

    @Override
    public Class<?> getBeanClass()
    {
        return Map.class;
    }

    @Override
    public boolean isNullable()
    {
        return false;
    }

    @Override
    public Set<Type> getTypes()
    {
        return Collections.EMPTY_SET; // this bean is only used manually
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        return Collections.EMPTY_SET; // this bean is only used manually
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return SessionScoped.class;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        return null;
    }

    @Override
    public boolean isAlternative()
    {
        return false;
    }

    @Override
    public String getId()
    {
        return "OwbInternalConversationStorageBean";
    }
}
