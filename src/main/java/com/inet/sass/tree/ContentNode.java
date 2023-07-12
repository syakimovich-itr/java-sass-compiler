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

import com.inet.sass.Scope;
import com.inet.sass.ScssContext;
import com.inet.sass.tree.controldirective.TemporaryNode;

/**
 * ContentNode represents a {@literal @}content in a SCSS tree. 
 */
public class ContentNode extends Node {

    MixinNode     mixinNode;
    private Scope scope;

    public ContentNode() {
    }

    private ContentNode( ContentNode nodeToCopy ) {
        super( nodeToCopy );
        this.mixinNode = nodeToCopy.mixinNode;
        this.scope = nodeToCopy.scope;
    }

    /**
     * Bind this placeholder (@content rule) with the block and scope of the caller (@include rule).
     * @param mixinNode the caller (@include rule)
     * @param scope the state of the variables of the caller
     */
    void bind( MixinNode mixinNode, Scope scope ) {
        this.mixinNode = mixinNode;
        this.scope = scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Node> traverse( ScssContext context ) {
        // evaluate the @content rule with the variables of the @include rule
        Scope previousScope = context.openVariableScope( scope );
        try {
            Node tempParent = new TemporaryNode( getParentNode(), mixinNode.copyChildren() );
            return tempParent.traverse( context );
        } finally {
            context.closeVariableScope( previousScope );
        }
    }

    @Override
    public ContentNode copy() {
        return new ContentNode( this );
    }
}
