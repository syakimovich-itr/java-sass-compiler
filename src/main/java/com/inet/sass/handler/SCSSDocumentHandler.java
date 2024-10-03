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

package com.inet.sass.handler;

import java.util.List;
import java.util.Stack;

import com.inet.sass.ScssStylesheet;
import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.MediaList;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.StringInterpolationSequence;
import com.inet.sass.selector.Selector;
import com.inet.sass.tree.BlockNode;
import com.inet.sass.tree.CommentNode;
import com.inet.sass.tree.ContentNode;
import com.inet.sass.tree.ExtendNode;
import com.inet.sass.tree.FontFaceNode;
import com.inet.sass.tree.FunctionDefNode;
import com.inet.sass.tree.ImportNode;
import com.inet.sass.tree.KeyframeSelectorNode;
import com.inet.sass.tree.KeyframesNode;
import com.inet.sass.tree.LayerNode;
import com.inet.sass.tree.MediaNode;
import com.inet.sass.tree.MessageNode;
import com.inet.sass.tree.MessageNode.MessageLevel;
import com.inet.sass.tree.MixinDefNode;
import com.inet.sass.tree.MixinNode;
import com.inet.sass.tree.NestPropertiesNode;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.ReturnNode;
import com.inet.sass.tree.RuleNode;
import com.inet.sass.tree.SimpleNode;
import com.inet.sass.tree.VariableNode;
import com.inet.sass.tree.controldirective.EachDefNode;
import com.inet.sass.tree.controldirective.ElseNode;
import com.inet.sass.tree.controldirective.ForNode;
import com.inet.sass.tree.controldirective.IfElseDefNode;
import com.inet.sass.tree.controldirective.IfNode;
import com.inet.sass.tree.controldirective.WhileNode;

public class SCSSDocumentHandler {

    private final ScssStylesheet styleSheet;
    Stack<Node>                  nodeStack = new Stack<Node>();

    public SCSSDocumentHandler( ScssStylesheet styleSheet ) {
        this.styleSheet = styleSheet;
        nodeStack.push( styleSheet );
    }

    public ScssStylesheet getStyleSheet() {
        return styleSheet;
    }

    public void variable( String name, SassListItem value, boolean guarded ) {
        VariableNode node = new VariableNode( name, value, guarded );
        nodeStack.peek().appendChild( node );
    }

    public void debugDirective( SassListItem message ) {
        MessageNode node = new MessageNode( message, MessageLevel.debug );
        nodeStack.peek().appendChild( node );
    }

    public void warnDirective( SassListItem message ) {
        MessageNode node = new MessageNode( message, MessageLevel.warn );
        nodeStack.peek().appendChild( node );
    }

    public void errorDirective( SassListItem message ) {
        MessageNode node = new MessageNode( message, MessageLevel.error );
        nodeStack.peek().appendChild( node );
    }

    public void startForDirective( String var, SassListItem from, SassListItem to, boolean exclusive ) {
        ForNode node = new ForNode( var, from, to, exclusive );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endForDirective() {
        nodeStack.pop();
    }

    public void startEachDirective( List<String> variables, SassListItem list ) {
        EachDefNode node = new EachDefNode( variables, list );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endEachDirective() {
        nodeStack.pop();
    }

    public void startWhileDirective( SassListItem condition ) {
        WhileNode node = new WhileNode( condition );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endWhileDirective() {
        nodeStack.pop();
    }

    public void comment( String text ) {
        CommentNode node = new CommentNode( text );
        nodeStack.peek().appendChild( node );
    }

    public void startMedia( String uri, int line, int column, MediaList media ) {
        MediaNode node = new MediaNode( uri, line, column, media );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endMedia()  {
        nodeStack.pop();
    }

    public void startLayer( StringInterpolationSequence layerName ) {
        LayerNode node = new LayerNode(layerName);
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endLayer()  {
        nodeStack.pop();
    }

    public void startFontFace() {
        FontFaceNode node = new FontFaceNode();
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endFontFace() {
        nodeStack.pop();
    }

    public void startSelector( String uri, int line, int column, List<Selector> selectors ) {
        BlockNode node = new BlockNode( uri, line, column, selectors );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endSelector() {
        nodeStack.pop();
    }

    public void property( StringInterpolationSequence name, SassListItem value, boolean important, String comment ) {
        RuleNode node = new RuleNode( name, value, important, comment );
        nodeStack.peek().appendChild( node );
    }

    public void extendDirective( List<Selector> list, boolean optional ) {
        ExtendNode node = new ExtendNode( list, optional );
        nodeStack.peek().appendChild( node );
    }

    public void startNestedProperties( StringInterpolationSequence name ) {
        NestPropertiesNode node = new NestPropertiesNode( name );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endNestedProperties() {
        nodeStack.pop();
    }

    public void startMixinDirective( String name, FormalArgumentList args ) {
        MixinDefNode node = new MixinDefNode( name.trim(), args );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endMixinDirective() {
        nodeStack.pop();
    }

    public void startFunctionDirective( String name, FormalArgumentList args ) {
        FunctionDefNode node = new FunctionDefNode( name.trim(), args );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endFunctionDirective() {
        nodeStack.pop();
    }

    public void importStyle( String uri, MediaList media, boolean isURL ) {
        ImportNode node = new ImportNode( uri, media, isURL );
        nodeStack.peek().appendChild( node );
    }

    public void startIfElseDirective() {
        final IfElseDefNode node = new IfElseDefNode();
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void ifDirective( SassListItem evaluator ) {
        if( nodeStack.peek() instanceof IfNode ) {
            nodeStack.pop();
        }
        IfNode node = new IfNode( evaluator );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void elseDirective() {
        if( nodeStack.peek() instanceof IfNode ) {
            nodeStack.pop();
        }
        ElseNode node = new ElseNode();
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endIfElseDirective() {
        if( (nodeStack.peek() instanceof ElseNode) || (nodeStack.peek() instanceof IfNode) ) {
            nodeStack.pop();
        }
        nodeStack.pop();
    }

    // rule that is passed to the output as-is (except variable value
    // substitution) - no children
    public void unrecognizedRule( String text ) {
        SimpleNode node = new SimpleNode( text );
        nodeStack.peek().appendChild( node );
    }

    public void startKeyFrames( String keyframeName, StringInterpolationSequence animationName ) {
        KeyframesNode node = new KeyframesNode( keyframeName, animationName );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );

    }

    public void endKeyFrames() {
        nodeStack.pop();

    }

    public void startKeyframeSelector( String selector ) {
        KeyframeSelectorNode node = new KeyframeSelectorNode( selector );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );

    }

    public void endKeyframeSelector() {
        nodeStack.pop();
    }

    public void contentDirective() {
        ContentNode node = new ContentNode();
        nodeStack.peek().appendChild( node );
    }

    public void returnDirective( SassListItem expr ) {
        ReturnNode node = new ReturnNode( expr );
        nodeStack.peek().appendChild( node );
    }

    public void startInclude( String uri, int line, int column, String name, ActualArgumentList args ) {
        MixinNode node = new MixinNode( uri, line, column, name, args );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );

    }

    public void endInclude() {
        nodeStack.pop();
    }
}
