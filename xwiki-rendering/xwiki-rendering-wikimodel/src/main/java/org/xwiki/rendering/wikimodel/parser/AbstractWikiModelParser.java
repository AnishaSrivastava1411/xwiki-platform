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
package org.xwiki.rendering.wikimodel.parser;

import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.LinkParser;
import org.xwiki.rendering.block.DOM;
import org.xwiki.rendering.wikimodel.internal.DocumentGeneratorListener;
import org.wikimodel.wem.IWikiParser;

import java.io.Reader;

public abstract class AbstractWikiModelParser extends AbstractLogEnabled implements Parser
{
    private LinkParser linkParser;
    
    public abstract IWikiParser createWikiModelParser();

    public DOM parse(Reader source) throws ParseException
    {
        IWikiParser parser = createWikiModelParser();

        // We pass the LinkParser corresponding to the syntax.
        DocumentGeneratorListener listener = new DocumentGeneratorListener(this.linkParser);

        try {
            parser.parse(source, listener);
        } catch (Exception e) {
            throw new ParseException("Failed to parse input source", e);
        }
        return listener.getDocument();
    }
}
