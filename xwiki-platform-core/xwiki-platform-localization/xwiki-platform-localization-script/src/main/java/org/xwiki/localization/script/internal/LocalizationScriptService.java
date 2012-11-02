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
package org.xwiki.localization.script.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.ArrayUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.localization.LocalizationContext;
import org.xwiki.localization.LocalizationManager;
import org.xwiki.localization.Translation;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.script.service.ScriptService;

/**
 * Provides Component-specific Scripting APIs.
 * 
 * @version $Id$
 * @since 4.3M2
 */
@Component
@Named("l10n")
@Singleton
public class LocalizationScriptService implements ScriptService
{
    @Inject
    private LocalizationManager localization;

    @Inject
    private LocalizationContext locallizationContext;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManager;

    /**
     * @param key the translation key
     * @return the translation, null if none can be found
     */
    public Translation get(String key)
    {
        return this.localization.getTranslation(key, this.locallizationContext.getCurrentLocale());
    }

    // Helpers

    /**
     * @param key the translation key
     * @return the rendered translation message
     */
    public String render(String key)
    {
        return render(key, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * @param key the translation key
     * @param parameters the translation parameters
     * @return the rendered translation message
     */
    public String render(String key, Object[] parameters)
    {
        return render(key, Syntax.PLAIN_1_0, parameters);
    }

    /**
     * @param key the translation key
     * @param syntax the syntax in which to render the translation message
     * @param parameters the translation parameters
     * @return the rendered translation message, the key if no translation can be found and null if the rendering failed
     */
    public String render(String key, Syntax syntax, Object[] parameters)
    {
        String result = null;

        Translation translation = this.localization.getTranslation(key, this.locallizationContext.getCurrentLocale());

        if (translation != null) {
            Block block = translation.render(parameters);

            // Render the block

            try {
                BlockRenderer renderer =
                    this.componentManager.get().getInstance(BlockRenderer.class, syntax.toIdString());

                DefaultWikiPrinter wikiPrinter = new DefaultWikiPrinter();
                renderer.render(block, wikiPrinter);

                result = wikiPrinter.toString();
            } catch (ComponentLookupException e) {
                // TODO set current error
            }
        } else {
            result = key;
        }

        return result;
    }
}
