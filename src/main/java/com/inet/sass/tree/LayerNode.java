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

import com.inet.sass.ScssContext;
import com.inet.sass.parser.StringInterpolationSequence;

import java.util.Collection;
import java.util.Collections;

public class LayerNode extends Node {

    private StringInterpolationSequence layerName;

    public LayerNode(StringInterpolationSequence layerName) {
        this.layerName = layerName;
    }

    private LayerNode(LayerNode nodeToCopy) {
        super(nodeToCopy);
        layerName = nodeToCopy.layerName;
    }

    public StringInterpolationSequence getLayerName() {
        return layerName;
    }

    public void setLayerName(final StringInterpolationSequence layerName) {
        this.layerName = layerName;
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY, true);
    }

    @Override
    public String toString() {
        return buildString(TO_STRING_STRATEGY, true);
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        layerName = layerName.replaceVariables(context);
        traverseChildren(context);
        return Collections.singleton((Node) this);
    }

    private String buildString(BuildStringStrategy strategy, boolean indent) {
        StringBuilder builder = new StringBuilder("@layer ")
                .append(layerName)
                .append(" {\n");
        for (Node child : getChildren()) {
            builder.append('\t');
            if (child instanceof BlockNode) {
                if (PRINT_STRATEGY.equals(strategy)) {
                    builder.append(((BlockNode) child).buildString(indent));
                } else {
                    builder.append(strategy.build(child));

                }
            } else {
                builder.append(strategy.build(child));
            }
            builder.append('\n');
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public LayerNode copy() {
        return new LayerNode(this);
    }
}
