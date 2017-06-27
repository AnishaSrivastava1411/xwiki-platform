/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.notifications.internal.rss;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import javax.script.ScriptContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.eventstream.Event;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.notifications.CompositeEvent;
import org.xwiki.notifications.NotificationException;
import org.xwiki.notifications.internal.ModelBridge;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateManager;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultNotificationRSSRenderer}.
 *
 * @version $Id$
 * @since 9.6RC1
 */
public class DefaultNotificationRSSRendererTest
{
    @Rule
    public final MockitoComponentMockingRule<DefaultNotificationRSSRenderer> mocker =
            new MockitoComponentMockingRule<>(DefaultNotificationRSSRenderer.class);

    private ContextualLocalizationManager contextualLocalizationManager;

    private ModelBridge modelBridge;

    private DocumentReferenceResolver<String> documentReferenceResolver;

    private TemplateManager templateManager;

    private ScriptContextManager scriptContextManager;

    private BlockRenderer blockRenderer;

    @Before
    public void setUp() throws Exception
    {
        this.contextualLocalizationManager = this.mocker.registerMockComponent(ContextualLocalizationManager.class);

        this.modelBridge = this.mocker.registerMockComponent(ModelBridge.class);

        this.documentReferenceResolver = this.mocker.registerMockComponent(DocumentReferenceResolver.class);

        this.templateManager = this.mocker.registerMockComponent(TemplateManager.class);

        this.scriptContextManager = this.mocker.registerMockComponent(ScriptContextManager.class);
        ScriptContext scriptContext = mock(ScriptContext.class);
        when(this.scriptContextManager.getCurrentScriptContext()).thenReturn(scriptContext);

        this.blockRenderer = this.mocker.registerMockComponent(BlockRenderer.class, "html/5.0");
    }

    private void mockEvent(CompositeEvent testCompositeEvent) throws Exception
    {
        Event testEvent1 = mock(Event.class);
        Date testEventDate = mock(Date.class);
        when(testEvent1.getTitle()).thenReturn("EventTitle");
        when(testEvent1.getDocumentTitle()).thenReturn("EventDocumentTitle");
        when(this.contextualLocalizationManager.getTranslationPlain("EventTitle", "EventDocumentTitle"))
                .thenReturn("TranslatedEventTitle");

        DocumentReference testEventAuthor1 = new DocumentReference("xwiki", "XWiki", "AuthorName");

        when(this.templateManager.getTemplate(any())).thenReturn(mock(Template.class));

        when(testCompositeEvent.getEvents()).thenReturn(Arrays.asList(testEvent1));
        when(testCompositeEvent.getUsers()).thenReturn(new HashSet<>(Arrays.asList(testEventAuthor1)));
        when(testCompositeEvent.getEventIds()).thenReturn(Arrays.asList("id1"));
        when(testCompositeEvent.getDates()).thenReturn(Arrays.asList(testEventDate));

    }

    @Test
    public void testCompositeEventRendering() throws Exception
    {
        CompositeEvent testCompositeEvent = mock(CompositeEvent.class);

        this.mockEvent(testCompositeEvent);

        SyndEntry resultEntry = this.mocker.getComponentUnderTest().renderNotification(testCompositeEvent);

        assertEquals("TranslatedEventTitle", resultEntry.getTitle());
        assertEquals(1, resultEntry.getAuthors().size());
        assertEquals("id1", resultEntry.getUri());
    }

    @Test
    public void renderFeed() throws Exception
    {
        when(this.contextualLocalizationManager.getTranslationPlain("notifications.rss.feedTitle"))
                .thenReturn("FeedTitle");
        when(this.modelBridge.getDocumentURL(
                any(), any(), any())).thenReturn("url");
        when(this.contextualLocalizationManager.getTranslationPlain("notifications.rss.feedDescription"))
                .thenReturn("FeedDescription");

        SyndFeed feed = this.mocker.getComponentUnderTest().renderFeed(Collections.EMPTY_LIST);

        assertEquals("FeedTitle", feed.getTitle());
        assertEquals("FeedDescription", feed.getDescription());
        assertEquals("url", feed.getLink());
        assertEquals(0, feed.getEntries().size());
    }
}
