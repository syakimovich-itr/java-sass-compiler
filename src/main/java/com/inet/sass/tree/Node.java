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
import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.tree.controldirective.TemporaryNode;

public abstract class Node implements SourceLocation {

    public static BuildStringStrategy PRINT_STRATEGY = new PrintStrategy();

    public static BuildStringStrategy TO_STRING_STRATEGY = new ToStringStrategy();

    private ArrayList<Node> children;

    private Node parentNode;

    private int line;
    private int column;
    private String uri;

    protected Node() {
    }

    protected Node( String uri, int line, int column ) {
        this.uri = uri;
        this.line = line;
        this.column = column;
    }

    protected Node( Node nodeToCopy ) {
        if( nodeToCopy != null ) {
            if( nodeToCopy.children != null ) {
                setChildren( nodeToCopy.copyChildren() );
            }
            this.uri = nodeToCopy.uri;
            this.line = nodeToCopy.line;
            this.column = nodeToCopy.column;
        }
    }

    /**
     * Replace the child at the given position with another node.
     * 
     * @param index
     *            the position of the old node that is to be replaced
     * @param newChild
     *            replacing node
     */
    public void replaceNodeAt(int index, Node newChild) {
        newChild.removeFromParent();
        newChild.parentNode = this;
        children.set(index, newChild);
    }

    /**
     * Replace the child oldChild with a collection of nodes.
     * 
     * @param oldChild
     *            child to replace
     * @param newNodes
     *            replacing nodes, can be an empty collection
     */
    public void replaceNode(Node oldChild, Collection<? extends Node> newNodes) {
        appendAfterNode(oldChild, newNodes);
        if (!newNodes.contains(oldChild)) {
            oldChild.removeFromParent();
        }
    }

    private void appendAfterNode( Node after, Collection<? extends Node> newNodes ) {
        if( newNodes != null && !newNodes.isEmpty() ) {
            // try to find last node with "after" as its original node and
            // append after it
            ArrayList<Node> children = this.children;
            if( children != null ) {
                for( int i = children.size() - 1; i >= 0; --i ) {
                    Node node = children.get( i );
                    if( node == after ) {
                        children.addAll( i + 1, newNodes );
                        for( final Node child : newNodes ) {
                            child.removeFromParent();
                            child.parentNode = this;
                        }
                        return;
                    }
                }
            }
            throw new ParseException( "after-node was not found", after );
        }
    }

    /**
     * Append a new child node to the end of the child list. This method should
     * only be used when constructing the Node tree, not when modifying it.
     * 
     * @param node
     *            new child to append
     */
    // TODO this should be avoided except when constructing the node tree
    public void appendChild(Node node) {
        if (node != null) {
            getChildren(true).add(node);
            node.removeFromParent();
            node.parentNode = this;
        }
    }

    /**
     * Remove this node from its parent (if any).
     */
    private void removeFromParent() {
        if (getParentNode() != null) {
            getParentNode().getChildren(true).remove(this);
            parentNode = null;
        }
    }

    public List<Node> getChildren() {
        return getChildren( false );
    }

    // avoid calling this method whenever possible
    @Deprecated
    protected void setChildren(Collection<Node> newChildren) {
        children = new ArrayList<Node>(newChildren);
        // add new children
        for (Node child : newChildren) {
            child.parentNode = this;
        }
    }

    private List<Node> getChildren(boolean create) {
        if (children == null && create) {
            children = new ArrayList<Node>();
        }
        if (children != null) {
            return children;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Method for manipulating the data contained within the {@link Node}.
     * 
     * Traversing a node is allowed to modify the node, replace it with one or
     * more nodes at the same or later position in its parent and modify the
     * children of the node, but not modify or remove preceding nodes in its
     * parent. Traversing a node is also allowed to modify the definitions
     * currently in scope as its side-effect.
     * 
     * @param context
     *            current compilation context
     * @return nodes replacing the current node
     */
    public abstract Collection<Node> traverse(ScssContext context);

    /**
     * Prints out the current state of the node tree. Will return SCSS before
     * compile and CSS after.
     * 
     * Result value could be null.
     * 
     * @return State as a string
     */
    public String printState() {
        return null;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public Node getNormalParentNode() {
        Node parent = getParentNode();
        while (parent instanceof TemporaryNode) {
            parent = parent.getParentNode();
        }
        return parent;
    }

    /**
     * Copy a node (deep copy including children).
     * 
     * The copy is detached from the original tree, with null as parent, and
     * data that is not relevant to handling of function or mixin expansion is
     * not copied.
     * 
     * @return copy of the node
     */
    public abstract Node copy();

    // to be used primarily from inside the class Node
    protected Collection<Node> copyChildren() {
        ArrayList<Node> children = this.children;
        if( children == null || children.isEmpty() ) {
            return Collections.emptyList();
        }
        ArrayList<Node> result = new ArrayList<Node>();
        for( Node child : children ) {
            result.add( child.copy() );
        }
        return result;
    }

    public Collection<Node> traverseChildren(ScssContext context) {
        return traverseChildren(context, true);
    }

    protected Collection<Node> traverseChildren(ScssContext context,
            boolean newScope) {
        List<Node> children = getChildren();
        if (!children.isEmpty()) {
            if (newScope) {
                context.openVariableScope();
            }
            try {
                ArrayList<Node> result = new ArrayList<Node>();
                for (Node child : children) {
                    result.addAll(child.traverse(context));
                }
                // TODO this ugly but hard to eliminate as long as some classes
                // use traverseChildren() for its side-effects
                setChildren(result);
                return result;
            } finally {
                if (newScope) {
                    context.closeVariableScope();
                }
            }
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineNumber() {
        return line;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnNumber() {
        return column;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUri() {
        return uri;
    }

    public static interface BuildStringStrategy {

        String build(Node node);

        String build(SassListItem expr);

        String build(ActualArgumentList expr);
    }

    public static class PrintStrategy implements BuildStringStrategy {

        @Override
        public String build(Node node) {
            return node.printState();
        }

        @Override
        public String build(SassListItem expr) {
            return expr.printState();
        }

        @Override
        public String build(ActualArgumentList expr) {
            return expr.printState();
        }

    }

    public static class ToStringStrategy implements BuildStringStrategy {

        @Override
        public String build(Node node) {
            return node.toString();
        }

        @Override
        public String build(SassListItem expr) {
            return expr.toString();
        }

        @Override
        public String build(ActualArgumentList expr) {
            return expr.toString();
        }

    }

}
