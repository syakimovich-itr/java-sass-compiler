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

package com.inet.sass.tree.controldirective;

import java.util.Collection;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.tree.IVariableNode;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.NodeWithUrlContent;
import com.inet.sass.visitor.EachNodeHandler;

public class EachDefNode extends Node implements IVariableNode,
        NodeWithUrlContent {

    private List<String> variableNames;
    private SassListItem list;

    public EachDefNode( List<String> variableNames, SassListItem list) {
        super();
        this.variableNames = variableNames;
        this.list = list;
    }

    private EachDefNode(EachDefNode nodeToCopy) {
        super(nodeToCopy);
        variableNames = nodeToCopy.variableNames;
        list = nodeToCopy.list;
    }

    public SassListItem getVariables() {
        return list;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    @Override
    public String toString() {
        return "Each Definition Node: {variable : " + variableNames + ", "
                + "children : " + list + "}";
    }

    @Override
    public void replaceVariables(ScssContext context) {
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        return EachNodeHandler.traverse(context, this);
    }

    @Override
    public EachDefNode copy() {
        return new EachDefNode(this);
    }

    @Override
    public NodeWithUrlContent updateUrl( String prefix ) {
        SassListItem list = this.list;
        SassListItem newList = list.updateUrl( prefix );
        if( list != newList ) {
            EachDefNode copy = copy();
            copy.list = newList;
            return copy;
        }
        return this;
    }
}
