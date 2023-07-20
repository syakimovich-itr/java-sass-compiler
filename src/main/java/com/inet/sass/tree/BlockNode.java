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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.selector.Selector;
import com.inet.sass.visitor.BlockNodeHandler;

public class BlockNode extends Node {

    private List<Selector> selectorList;

    // combined selectors of the parent node, used to handle @extends
    private List<Selector> parentSelectors;

    public BlockNode( String uri, int line, int column, List<Selector> selectorList ) {
        super( uri, line, column );
        this.selectorList = selectorList;
    }

    /**
     * Create a copy with different children
     */
    public BlockNode( BlockNode original, Collection<Node> children) {
        this( original.getUri(), original.getLineNumber(), original.getColumnNumber(), original.getSelectorList() );
        setChildren(children);
    }

    // for use by copy() only
    private BlockNode(BlockNode blockNode) {
        super(blockNode);
        selectorList = blockNode.selectorList;
        parentSelectors = blockNode.parentSelectors;
    }

    /**
     * Returns unmodifiable selector list of the block
     * 
     * @return selector list
     */
    public List<Selector> getSelectorList() {
        return selectorList;
    }

    /**
     * Sets the selector list for the node.
     * 
     * The selector list instance must not be modified after being given as a
     * parameter here.
     * 
     * @param selectorList
     *            new selector list
     */
    public void setSelectorList(List<Selector> selectorList) {
        this.selectorList = selectorList;
    }

    public String buildString(boolean indent) {
        return buildString(indent, PRINT_STRATEGY);
    }

    @Override
    public String printState() {
        return buildString(false);
    }

    @Override
    public String toString() {
        return "BlockNode [" + buildString(true, TO_STRING_STRATEGY) + "]";
    }

    private void replaceVariables(ScssContext context) {
        if (selectorList == null || selectorList.isEmpty()) {
            return;
        }

        ArrayList<Selector> newSelectorList = new ArrayList<Selector>();
        for (Selector s : selectorList) {
            newSelectorList.add(s.replaceVariables(context));
        }
        setSelectorList(newSelectorList);
    }

    public String getSelectors() {
        StringBuilder b = new StringBuilder();
        for (final Selector s : selectorList) {
            b.append(s);
        }

        return b.toString();
    }

    @Override
    public Collection<Node> traverse( ScssContext context ) {
        try {
            replaceVariables( context );
            return BlockNodeHandler.traverse( context, this );
        } catch( Exception e ) {
            SCSSErrorHandler.get().error( e );
        }
        return Collections.emptyList();
    }

    private String buildString(boolean indent, BuildStringStrategy strategy) {
        StringBuilder string = new StringBuilder();
        int i = 0;
        for (final Selector s : selectorList) {
            string.append(s);
            if (i != selectorList.size() - 1) {
                string.append(", ");
            }
            i++;
        }
        string.append(" {\n");
        for (Node child : getChildren()) {
            if (indent) {
                string.append("\t");
            }
            string.append("\t" + strategy.build(child) + "\n");
        }
        if (indent) {
            string.append("\t");
        }
        string.append("}");
        return string.toString();
    }

    @Override
    public BlockNode copy() {
        return new BlockNode(this);
    }

    /**
     * Returns the parent selector list.
     * 
     * @return parent selector list or null
     */
    public List<Selector> getParentSelectors() {
        return parentSelectors;
    }

    /**
     * Sets the selector list of the parent node. The list must not be modified
     * after giving it as a parameter here.
     * 
     * @param parentSelectors
     *            parent selector list or null
     */
    public void setParentSelectors(List<Selector> parentSelectors) {
        this.parentSelectors = parentSelectors;
    }

}
