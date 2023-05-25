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

package com.inet.sass.tree;

import java.util.Collection;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.MediaList;
import com.inet.sass.util.StringUtil;
import com.inet.sass.visitor.ImportNodeHandler;

public class ImportNode extends Node implements NodeWithUrlContent {

    private String uri;
    private MediaList ml;
    private boolean isURL;

    public ImportNode( String uri, MediaList ml, boolean isURL ) {
        super();
        this.uri = uri;
        this.ml = ml;
        this.isURL = isURL;
    }

    private ImportNode(ImportNode nodeToCopy) {
        super(nodeToCopy);
        uri = nodeToCopy.uri;
        ml = nodeToCopy.ml;
        isURL = nodeToCopy.isURL;
    }

    public boolean isPureCssImport() {
        return (isURL || uri.endsWith(".css") || uri.startsWith("http://") || hasMediaQueries());
    }

    private boolean hasMediaQueries() {
        return (ml != null && ml.getLength() >= 1 && !"all".equals(ml.item(0)));
    }

    @Override
    public String printState() {
        StringBuilder builder = new StringBuilder("@import ");
        if (isURL) {
            builder.append("url(").append(uri).append(")");
        } else {
            builder.append("\"").append(uri).append("\"");
        }
        if (hasMediaQueries()) {
            for (int i = 0; i < ml.getLength(); i++) {
                builder.append(" ").append(ml.item(i));
            }
        }
        builder.append(";");
        return builder.toString();
    }

    @Override
    public String toString() {
        return "Import node [" + printState() + "]";
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public MediaList getMl() {
        return ml;
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        return ImportNodeHandler.traverse(context, this);
    }

    @Override
    public ImportNode copy() {
        return new ImportNode(this);
    }

    @Override
    public ImportNode updateUrl(String prefix) {
        if (isURL) {
            String newUri = getUri().replaceAll("^\"|\"$", "").replaceAll(
                    "^'|'$", "");
            if (!newUri.startsWith("/") && !newUri.contains(":")) {
                newUri = prefix + newUri;
                newUri = StringUtil.cleanPath(newUri);
            }
            ImportNode copy = copy();
            copy.setUri(newUri);
            return copy;
        }
        return this;
    }
}
