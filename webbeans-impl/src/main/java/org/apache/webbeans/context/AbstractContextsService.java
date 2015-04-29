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
package org.apache.webbeans.context;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextException;
import javax.enterprise.context.spi.Context;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.conversation.ConversationImpl;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.spi.ContextsService;

public abstract class AbstractContextsService implements ContextsService
{
    protected final WebBeansContext webBeansContext;

    protected AbstractContextsService(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    @Override
    public void destroy(Object destroyObject)
    {
        //Default no-op
    }

    @Override
    public void endContext(Class<? extends Annotation> scopeType, Object endParameters)
    {
        //Default no-op
    }

    @Override
    public Context getCurrentContext(Class<? extends Annotation> scopeType)
    {
        
        return null;
    }

    @Override
    public void init(Object initializeObject)
    {
        //Default no-op        
    }

    @Override
    public void startContext(Class<? extends Annotation> scopeType, Object startParameter) throws ContextException
    {
        //Default no-op        
    }

    @Override
    public boolean supportsContext(Class<? extends Annotation> scopeType)
    {        
        return false;
    }
    
    @Override
    public void activateContext(Class<? extends Annotation> scopeType)
    {
        if(supportsContext(scopeType))
        {
            Context context = getCurrentContext(scopeType);
            if(context instanceof AbstractContext)
            {
                ((AbstractContext)context).setActive(true);
            }
        }
    }
    
    @Override
    public void deActivateContext(Class<? extends Annotation> scopeType)
    {
        if(supportsContext(scopeType))
        {
            Context context = getCurrentContext(scopeType);
            if(context instanceof AbstractContext)
            {
                ((AbstractContext)context).setActive(false);
            }
        }        
    }

    protected void cleanupConversations(ConversationContext conversationCtx)
    {
        if (conversationCtx == null)
        {
            return;
        }

        ConversationManager conversationManager = webBeansContext.getConversationManager();

        ConversationImpl conversation = conversationCtx.getConversation();
        if (!conversation.isTransient())
        {
            //Conversation must be used by one thread at a time
            conversation.updateLastAccessTime();
            //Other threads can now access propagated conversation.
            conversation.iDontUseItAnymore();
        }

        // and now destroy all timed-out and transient conversations
        conversationManager.destroyUnrequiredConversations();
    }


}
