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

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.annotation.DestroyedLiteral;
import org.apache.webbeans.annotation.InitializedLiteral;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.ConversationService;

/**
 * Manager for the conversations.
 * Each conversation is related with conversation id and session id.
 *
 *
 * @version $Rev$ $Date$
 *
 */
public class ConversationManager
{
    private final static Logger logger = WebBeansLoggerFacade.getLogger(ConversationManager.class);


    private final WebBeansContext webBeansContext;
    private final ContextsService contextsService;

    /**
     * Creates new conversation manager
     */
    public ConversationManager(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        this.contextsService = webBeansContext.getContextsService();

        // we need to register this for serialisation in clusters
        webBeansContext.getBeanManagerImpl().addInternalBean(ConversationStorageBean.INSTANCE);
    }


    /**
     * This method shall only get called from the ContextsService.
     * It will create a new ConversationContext if there is no long running Conversation already running.
     * @return the ConversationContext which is valid for the whole request.
     */
    public ConversationContext getConversationContext()
    {
        ConversationService conversationService = webBeansContext.getConversationService();

        String conversationId = conversationService.getConversationId();
        if (conversationId != null && conversationId.length() > 0)
        {
            Set<ConversationContext> conversationContexts = getConversations(false);
            if (conversationContexts != null)
            {
                for (ConversationContext conversationContext : conversationContexts)
                {
                    if (conversationId.equals(conversationContext.getConversation().getId()))
                    {
                        return conversationContext;
                    }
                }
            }
        }

        ConversationContext conversationContext = new ConversationContext(webBeansContext);
        conversationContext.setActive(true);

        webBeansContext.getBeanManagerImpl().fireEvent(getLifecycleEventPayload(conversationContext), InitializedLiteral.INSTANCE_CONVERSATION_SCOPED);

        return conversationContext;
    }

    /**
     * Check if a conversation with the given id exists in the session context.
     * @param conversationId conversation id
     * @return true if this conversation exist
     */
    public boolean isConversationExistWithGivenId(String conversationId)
    {
        if (conversationId == null)
        {
            return false;
        }

        Set<ConversationContext> conversationContexts = getConversations(false);
        if (conversationContexts == null)
        {
            return false;
        }

        for (ConversationContext conversationContext : conversationContexts)
        {
            if (conversationId.equals(conversationContext.getConversation().getId()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets conversation instance from conversation bean.
     * @return conversation instance
     */
    @SuppressWarnings("unchecked")
    public Conversation getConversationBeanReference()
    {
        BeanManager beanManager = webBeansContext.getBeanManagerImpl();
        Bean<Conversation> bean = (Bean<Conversation>)beanManager.getBeans(Conversation.class, DefaultLiteral.INSTANCE).iterator().next();
        Conversation conversation =(Conversation) beanManager.getReference(bean, Conversation.class, beanManager.createCreationalContext(bean));

        return conversation;
    }

    /**
     * Destroy inactive (timed out) and transient conversations.
     */
    public void destroyUnrequiredConversations()
    {
        Set<ConversationContext> conversationContexts = getConversations(false);
        if (conversationContexts == null)
        {
            return;
        }

        Iterator<ConversationContext> convIt = conversationContexts.iterator();
        while (convIt.hasNext())
        {
            ConversationContext conversationContext = convIt.next();

            ConversationImpl conv = conversationContext.getConversation();
            if (conv.isTransient() || conversationTimedOut(conv))
            {
                destroyConversationContext(conversationContext);
                convIt.remove();
            }
        }
    }

    private boolean conversationTimedOut(ConversationImpl conv)
    {
        long timeout = conv.getTimeout();
        if (timeout != 0L && (System.currentTimeMillis() - conv.getLastAccessTime()) > timeout)
        {
            logger.log(Level.FINE, OWBLogConst.INFO_0011, conv.getId());
            return true;
        }

        return false;
    }

    /**
     * Destroy the given ConversationContext and fire the proper
     * &#064;Destroyed event with the correct payload.
     */
    public void destroyConversationContext(ConversationContext ctx)
    {
        ctx.destroy();
        webBeansContext.getBeanManagerImpl().fireEvent(getLifecycleEventPayload(ctx), DestroyedLiteral.INSTANCE_CONVERSATION_SCOPED);
    }

    private Object getLifecycleEventPayload(ConversationContext ctx)
    {
        Object payLoad = null;
        if (ctx.getConversation().getId() != null)
        {
            payLoad = ctx.getConversation().getId();
        }

        if (payLoad == null)
        {
            RequestContext requestContext = (RequestContext) contextsService.getCurrentContext(RequestScoped.class);
            if (requestContext != null)
            {
                payLoad = requestContext.getRequestObject();
            }
        }

        if (payLoad == null)
        {
            payLoad = new Object();
        }
        return payLoad;
    }


    /**
     * @param create whether a session and the map in there shall get created or not
     * @return the conversation Map from the current session
     */
    private Set<ConversationContext> getConversations(boolean create)
    {
        Set<ConversationContext> conversationContexts = null;
        Context sessionContext = contextsService.getCurrentContext(SessionScoped.class);
        if (sessionContext != null)
        {
            if (!create)
            {
                conversationContexts = sessionContext.get(ConversationStorageBean.INSTANCE);
            }
            else
            {
                CreationalContextImpl<Set<ConversationContext>> creationalContext
                        = webBeansContext.getBeanManagerImpl().createCreationalContext(ConversationStorageBean.INSTANCE);

                conversationContexts = sessionContext.get(ConversationStorageBean.INSTANCE, creationalContext);
            }
        }

        return conversationContexts;
    }

}
