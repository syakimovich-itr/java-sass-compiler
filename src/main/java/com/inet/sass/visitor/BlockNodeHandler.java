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
import com.inet.sass.selector.Selector;
import com.inet.sass.tree.BlockNode;
import com.inet.sass.tree.MediaNode;
import com.inet.sass.tree.Node;

/**
 * Handle nesting of blocks by moving child blocks to their parent, updating
 * their selector lists while doing so. Also parent selectors (&amp;) are
 * handled here.
 * 
 * Sample SASS code (from www.sass-lang.com):
 * 
 * <pre>
 * table.hl {
 *   margin: 2em 0;
 *   td.ln {
 *     text-align: right;
 *   }
 * }
 * </pre>
 * 
 * Note that nested properties are handled by {@link NestedNodeHandler}, not
 * here.
 */
public class BlockNodeHandler {

    public static Collection<Node> traverse( ScssContext context, BlockNode node ) {

        if( node.getChildren().size() == 0 ) {
            return Collections.emptyList();
        }

        ArrayList<Node> result = new ArrayList<Node>();
        boolean inContentNode = context.isInContentNode();
        if( !inContentNode ) {
            // the selector will be evaluate after the @content was replaced in the tree
            updateSelectors( node );
        }

        List<Node> children = node.getChildren();
        if( !children.isEmpty() ) {
            context.openVariableScope();
            BlockNode oldParent = context.getParentBlock();
            context.setParentBlock( node );
            try {
                ArrayList<Node> medias = null;
                ArrayList<Node> newChildren = null;
                for( Node child : children ) {
                    if( child.getClass() == BlockNode.class ) {
                        ((BlockNode)child).setParentSelectors( node.getSelectorList() );
                        result.addAll( child.traverse( context ) );
                    } else if( child.getClass() == MediaNode.class ) {
                        medias = bubbleMedia( medias, context, node, (MediaNode)child );
                    } else {
                        Collection<Node> childTraversed = child.traverse( context );
                        for( Node n : childTraversed ) {
                            if( n.getClass() == BlockNode.class ) {
                                // already traversed
                                result.add( n );
                            } else if( n.getClass() == MediaNode.class ) {
                                medias = bubbleMedia( medias, context, node, (MediaNode)n );
                            } else {
                                if( newChildren == null ) {
                                    newChildren = new ArrayList<Node>();
                                }
                                newChildren.add( n );
                            }
                        }
                    }
                }
                if( medias != null ) {
                    result = medias;
                }
                // add the node with the remaining non-block children at the
                // beginning
                if( newChildren != null ) {
                    BlockNode newNode = new BlockNode( node, newChildren );
                    newNode.setParentSelectors( node.getParentSelectors() );
                    result.add( 0, newNode );
                }
            } finally {
                context.closeVariableScope();
                context.setParentBlock( oldParent );
            }
        }

        if( inContentNode ) {
            return Collections.singletonList( node );
        }
        return result;
    }

    /**
     * Reorder the @media rule on top
     * @param medias container for the medias, will be null on first call
     * @param context current context
     * @param node the parent node of the MediaNode
     * @param child the media node which is child
     * @return the container, never null
     */
    static ArrayList<Node> bubbleMedia( ArrayList<Node> medias, ScssContext context, BlockNode node, MediaNode child ) {
        for( Selector selector : node.getSelectorList() ) {
            if( selector.isPlaceholder() ) {
                //TODO placeholder selectors must handle other
                return medias;
            }
        }
        if( medias == null ) {
            medias = new ArrayList<>();
        }
        MediaNode media = new MediaNode( child.getMedia() );
        node = new BlockNode( node, child.getChildren() );
        for( Node n : node.traverse( context ) ) {
            media.appendChild( n );
        }
        medias.add( media );
        return medias;
    }

    private static void updateSelectors( BlockNode node) {
        Node parentBlock = node.getNormalParentNode();
        if( parentBlock instanceof BlockNode ) {
            replaceParentSelectors( (BlockNode)parentBlock, node );

        } else if( node.getSelectors().contains( "&" ) ) {
            ScssStylesheet.warning("Base-level rule contains"
                    + " the parent-selector-referencing character '&';"
                    + " the character will be removed:\n" + node);
            removeParentReference(node);
        }
    }

    /**
     * Goes through the selector list of the given BlockNode and removes the '&'
     * character from the selectors.
     * 
     * @param node
     */
    private static void removeParentReference(BlockNode node) {
        ArrayList<Selector> newSelectors = new ArrayList<Selector>();

        for( Selector sel : node.getSelectorList() ) {
            newSelectors.add( sel.replaceParentReference( null, node ) );
        }

        node.setSelectorList(newSelectors);
    }

    private static void replaceParentSelectors( BlockNode parentBlock, BlockNode node ) {
        ArrayList<Selector> newSelectors = new ArrayList<Selector>();

        for (Selector parentSel : parentBlock.getSelectorList()) {
            for( Selector sel : node.getSelectorList() ) {
                newSelectors.add( sel.replaceParentReference( parentSel, node ) );
            }
        }

        node.setSelectorList(newSelectors);

    }
}
