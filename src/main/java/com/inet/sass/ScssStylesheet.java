/*
 * Copyright 2023 i-net software
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.inet.sass;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.inet.sass.handler.SCSSDocumentHandler;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.parser.ScssParser;
import com.inet.sass.resolver.ScssStylesheetResolver;
import com.inet.sass.tree.Node;
import com.inet.sass.visitor.ExtendNodeHandler;

public class ScssStylesheet extends Node {

    private String uri;

    private String charset;

    private ScssStylesheetResolver resolver;

    // relative path to use when importing files etc.
    private String prefix = "";

    private List<String> sourceUris = new ArrayList<String>();

    /**
     * Read in a file SCSS and parse it into a ScssStylesheet
     * 
     * @param file
     * @throws IOException
     */
    private ScssStylesheet() {
        super();
    }

    /**
     * Main entry point for the SASS compiler. Takes in a file, an optional
     * parent stylesheet, and document and error handlers. Then builds up a
     * ScssStylesheet tree out of it. Calling compile() on it will transform
     * SASS into CSS. Calling printState() will print out the SCSS/CSS.
     * 
     * @param identifier
     *            The file path. If null then null is returned.
     * @param parentStylesheet
     *            Style sheet from which to inherit resolvers and encoding. May
     *            be null.
     * @param documentHandler
     *            Instance of document handler. May not be null.
     * @param errorHandler
     *            Instance of error handler. May not be null.
     * @param resolver the used resolver
     * @return
     * @throws IOException
     */
    public static ScssStylesheet get( String identifier, SCSSErrorHandler errorHandler, ScssStylesheetResolver resolver ) throws IOException {
        SCSSErrorHandler.set( errorHandler );
        return load( identifier, null, resolver );
    }

    public ScssStylesheet importStylesheet( String identifier ) throws IOException {
        return load( identifier, this, resolver );
    }

    /**
     * Main entry point for the SASS compiler. Takes in a file, an optional
     * parent stylesheet, and document and error handlers. Then builds up a
     * ScssStylesheet tree out of it. Calling compile() on it will transform
     * SASS into CSS. Calling printState() will print out the SCSS/CSS.
     * 
     * @param identifier
     *            The file path. If null then null is returned.
     * @param parentStylesheet
     *            Style sheet from which to inherit resolvers and encoding. May
     *            be null.
     * @param documentHandler
     *            Instance of document handler. May not be null.
     * @return
     * @throws IOException
     */
    private static ScssStylesheet load( String identifier, ScssStylesheet parentStylesheet, ScssStylesheetResolver resolver ) throws IOException {
        /*
         * The encoding to be used is passed through "encoding" parameter. the
         * imported children scss node will have the same encoding as their
         * parent, ultimately the root scss file. The root scss node has this
         * "encoding" parameter to be null. Its encoding is determined by the
         * 
         * @charset declaration, the default one is ASCII.
         */

        if (identifier == null) {
            return null;
        }

        ScssStylesheet stylesheet = new ScssStylesheet();
        stylesheet.resolver = resolver;
        SCSSDocumentHandler documentHandler = new SCSSDocumentHandler( stylesheet );

        InputSource source = stylesheet.resolveSource( identifier, parentStylesheet );
        if( source == null ) {
            return null;
        }
        stylesheet.uri = source.getURI();

        if (parentStylesheet != null) {
            stylesheet.setCharset(parentStylesheet.getCharset());
        }
        ScssParser parser = new ScssParser();
        parser.parseStyleSheet(documentHandler,source);

        stylesheet.sourceUris.add(source.getURI());

        return stylesheet;
    }

    public InputSource resolveSource( String identifier, ScssStylesheet parentStylesheet ) {
        if( resolver != null ) {
            return resolver.resolve( parentStylesheet, identifier );
        }

        return null;
    }

    public List<String> getSourceUris() {
        return Collections.unmodifiableList(sourceUris);
    }

    public void addSourceUris(Collection<String> uris) {
        sourceUris.addAll(uris);
    }

    /**
     * Applies all the visitors and compiles SCSS into Css.
     * 
     * @throws Exception
     */
    public void compile() throws Exception {
        compile(ScssContext.UrlMode.MIXED);
    }

    /**
     * Applies all the visitors and compiles SCSS into Css.
     * 
     * @param urlMode
     *            Specifies whether urls appearing in an scss style sheet are
     *            taken to be absolute or relative. It is also possible to use
     *            old Vaadin style urls (ScssContext.UrlMode.MIXED), where urls
     *            are relative only when they appear in simple properties such
     *            as "background-image:url(image.png)".
     * 
     *            Absolute url mode means that urls appear in the output css in
     *            the same form as they are in the source scss files. Relative
     *            urls take into account the locations of imported scss files.
     *            For instance, if a style sheets imports foo/bar.scss and
     *            bar.scss contains url(baz.png), it will be output as
     *            url(foo/baz.png) in relative mode and as url(baz.png) in
     *            absolute mode.
     * 
     * @throws Exception
     */
    public void compile(ScssContext.UrlMode urlMode) throws Exception {
        ScssContext context = new ScssContext( urlMode, this );
        traverse(context);
        ExtendNodeHandler.modifyTree(context, this);
    }

    /**
     * Prints out the current state of the node tree. Will return SCSS before
     * compile and CSS after.
     * 
     * For now this is an own method with it's own implementation that most node
     * types will implement themselves.
     */
    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "Stylesheet node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    /**
     * Traverses a node and its children recursively, calling all the
     * appropriate handlers via {@link Node#traverse()}.
     * 
     * The node itself may be removed during the traversal and replaced with
     * other nodes at the same position or later on the child list of its
     * parent.
     */
    @Override
    public Collection<Node> traverse(ScssContext context) {
        traverseChildren(context);
        return Collections.singleton((Node) this);
    }

    /**
     * Returns the directory containing this style sheet
     * 
     * @return The directory containing this style sheet
     */
    public String getDirectory() {
        int idx = uri.lastIndexOf( '/' );
        return idx < 0 ? "" : uri.substring( 0, idx );
    }

    /**
     * Returns the full file name for this style sheet
     * 
     * @return The full file name for this style sheet
     */
    @Override
    public String getUri() {
        return uri;
    }

    public static final void warning(String msg) {
        SCSSErrorHandler.get().warning( msg );
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String buildString(BuildStringStrategy strategy) {
        StringBuilder string = new StringBuilder("");
        String delimeter = "\n\n";
        // add charset declaration, if it is not default "ASCII".
        if( charset != null && !"ASCII".equals( charset ) ) {
            string.append( "@charset \"" ).append( charset ).append( "\";" ).append( delimeter );
        }
        List<Node> children = getChildren();
        if (children.size() > 0) {
            string.append(strategy.build(children.get(0)));
        }
        if (children.size() > 1) {
            for (int i = 1; i < children.size(); i++) {
                String childString = strategy.build(children.get(i));
                if (childString != null) {
                    string.append(delimeter).append(childString);
                }
            }
        }
        String output = string.toString();
        return output;
    }

    @Override
    public Node copy() {
        throw new UnsupportedOperationException(
                "ScssStylesheet cannot be copied");
    }

    public void write(Writer writer) throws IOException {
        writer.write(printState());
    }
}
