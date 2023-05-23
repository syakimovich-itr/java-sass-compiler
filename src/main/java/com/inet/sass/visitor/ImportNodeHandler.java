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

package com.inet.sass.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.handler.SCSSDocumentHandler;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.tree.ImportNode;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.NodeWithUrlContent;
import com.inet.sass.tree.RuleNode;
import com.inet.sass.tree.controldirective.TemporaryNode;

public class ImportNodeHandler {

    public static Collection<Node> traverse(ScssContext context,
            ImportNode importNode) {
        ScssStylesheet styleSheet = importNode.getStylesheet();
        // top-level case
        if (styleSheet == null) {
            // iterate to parents of node, find ScssStylesheet
            Node parent = importNode.getParentNode();
            while (parent != null && !(parent instanceof ScssStylesheet)) {
                parent = parent.getParentNode();
            }
            if (parent instanceof ScssStylesheet) {
                styleSheet = (ScssStylesheet) parent;
            }
        }
        if (styleSheet == null) {
            SCSSErrorHandler.get().error( "Nested import in an invalid context" );
            return Collections.emptyList();
        }
        if (!importNode.isPureCssImport()) {
            List<Node> importedChildren = Collections.emptyList();
            ScssStylesheet imported = null;
            try {
                // set parent's charset to imported node.

                imported = ScssStylesheet.get(importNode.getUri(), styleSheet,
                        new SCSSDocumentHandler(), SCSSErrorHandler.get());
                if (imported == null) {
                    SCSSErrorHandler.get().error( "Import '" + importNode.getUri() + "' in '" + styleSheet.getUri() + "' could not be found" );
                    return Collections.emptyList();
                }

                String prefix = styleSheet.getPrefix()
                        + getUrlPrefix(importNode.getUri());
                if (!"".equals(prefix)) {
                    // support resolving nested imports relative to prefix
                    imported.setPrefix(prefix);
                    updateUrlInImportedSheet(imported, prefix, imported,
                            context);
                }

                importedChildren = new ArrayList<Node>(imported.getChildren());
            } catch (Exception e) {
                SCSSErrorHandler.get().error( e );
                return Collections.emptyList();
            }

            if (imported != null) {
                context.setStylesheet( imported );
                // traverse the imported nodes normally in the correct context
                Node tempParent = new TemporaryNode(importNode.getParentNode(),
                        importedChildren);
                Collection<Node> result = tempParent.traverseChildren(context);

                styleSheet.addSourceUris(imported.getSourceUris());
                context.setStylesheet( styleSheet );
                return result;
            }
        } else {
            if (styleSheet != importNode.getParentNode()) {
                SCSSErrorHandler.get().error( "CSS imports can only be used at the top level, not as nested imports. Within style rules, use SCSS imports." );
                return Collections.emptyList();

            }
        }
        return Collections.singleton((Node) importNode);
    }

    private static String getUrlPrefix(String url) {
        if (url == null) {
            return "";
        }
        int pos = url.lastIndexOf('/');
        if (pos == -1) {
            return "";
        }
        return url.substring(0, pos + 1);
    }

    private static void updateUrlInImportedSheet(Node node, String prefix,
            ScssStylesheet styleSheet, ScssContext context) {
        ScssContext.UrlMode urlMode = context.getUrlMode();
        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            Node newChild = child;
            if (child instanceof NodeWithUrlContent
                    && (urlMode.equals(ScssContext.UrlMode.RELATIVE) || (urlMode
                            .equals(ScssContext.UrlMode.MIXED) && child instanceof RuleNode))) {
                newChild = (Node) ((NodeWithUrlContent) child)
                        .updateUrl(prefix);
                node.replaceNodeAt(i, newChild);
            } else if (child instanceof ImportNode) {
                ((ImportNode) child).setStylesheet(styleSheet);
            }
            updateUrlInImportedSheet(newChild, prefix, styleSheet, context);
        }
    }
}
