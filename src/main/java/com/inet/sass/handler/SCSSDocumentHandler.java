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

import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;

import com.inet.sass.ScssStylesheet;
import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.MediaList;
import com.inet.sass.parser.SCSSLexicalUnit;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.StringInterpolationSequence;
import com.inet.sass.parser.Variable;
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
import com.inet.sass.tree.MediaNode;
import com.inet.sass.tree.MessageNode;
import com.inet.sass.tree.MixinDefNode;
import com.inet.sass.tree.MixinNode;
import com.inet.sass.tree.NestPropertiesNode;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.ReturnNode;
import com.inet.sass.tree.RuleNode;
import com.inet.sass.tree.SimpleNode;
import com.inet.sass.tree.VariableNode;
import com.inet.sass.tree.MessageNode.MessageLevel;
import com.inet.sass.tree.controldirective.EachDefNode;
import com.inet.sass.tree.controldirective.ElseNode;
import com.inet.sass.tree.controldirective.ForNode;
import com.inet.sass.tree.controldirective.IfElseDefNode;
import com.inet.sass.tree.controldirective.IfNode;
import com.inet.sass.tree.controldirective.WhileNode;

public class SCSSDocumentHandler {

    private final ScssStylesheet styleSheet;
    Stack<Node> nodeStack = new Stack<Node>();

    public SCSSDocumentHandler() {
        this(new ScssStylesheet());
    }

    public SCSSDocumentHandler(ScssStylesheet styleSheet) {
        this.styleSheet = styleSheet;
        nodeStack.push(styleSheet);
    }

    public ScssStylesheet getStyleSheet() {
        return styleSheet;
    }

    public void startDocument(InputSource source) throws CSSException {
        nodeStack.push(styleSheet);
    }

    public void endDocument(InputSource source) throws CSSException {
    }

    public void variable(String name, SassListItem value, boolean guarded) {
        VariableNode node = new VariableNode(name, value, guarded);
        nodeStack.peek().appendChild(node);
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

    public void startForDirective(String var, SassListItem from,
            SassListItem to, boolean exclusive) {
        ForNode node = new ForNode(var, from, to, exclusive);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void endForDirective() {
        nodeStack.pop();
    }

    public void startEachDirective( List<String> variables, SassListItem list ) {
        EachDefNode node = new EachDefNode( variables, list );
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void endEachDirective() {
        nodeStack.pop();
    }

    public void startWhileDirective(SassListItem condition) {
        WhileNode node = new WhileNode(condition);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void endWhileDirective() {
        nodeStack.pop();
    }

    public void comment(String text) throws CSSException {
        CommentNode node = new CommentNode(text);
        nodeStack.peek().appendChild(node);
    }

    public void ignorableAtRule(String atRule) throws CSSException {
        log("ignorableAtRule(String atRule): " + atRule);
    }

    public void namespaceDeclaration(String prefix, String uri)
            throws CSSException {
        log("namespaceDeclaration(String prefix, String uri): " + prefix + ", "
                + uri);
    }

    public void importStyle( String uri, MediaList media, String defaultNamespaceURI ) throws CSSException {
    }

    public void startMedia( MediaList media ) throws CSSException {
        MediaNode node = new MediaNode( media );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endMedia( MediaList media ) throws CSSException {
        nodeStack.pop();
    }

    public void startPage(String name, String pseudo_page) throws CSSException {
        log("startPage(String name, String pseudo_page): " + name + ", "
                + pseudo_page);
    }

    public void endPage(String name, String pseudo_page) throws CSSException {
        log("endPage(String name, String pseudo_page): " + name + ", "
                + pseudo_page);
    }

    public void startFontFace() throws CSSException {
        FontFaceNode node = new FontFaceNode();
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void endFontFace() throws CSSException {
        nodeStack.pop();
    }

    public void startSelector( String uri, int line, int column, List<Selector> selectors ) throws CSSException {
        BlockNode node = new BlockNode( uri, line, column, selectors );
        nodeStack.peek().appendChild( node );
        nodeStack.push( node );
    }

    public void endSelector() throws CSSException {
        nodeStack.pop();
    }

    public void property(StringInterpolationSequence name, SassListItem value,
            boolean important) throws CSSException {
        property(name, value, important, null);
    }

    public void property(StringInterpolationSequence name, SassListItem value,
            boolean important, String comment) {
        RuleNode node = new RuleNode(name, value, important, comment);
        nodeStack.peek().appendChild(node);
    }

    public void extendDirective(List<Selector> list, boolean optional) {
        ExtendNode node = new ExtendNode(list, optional);
        nodeStack.peek().appendChild(node);
    }

    public void startNestedProperties(StringInterpolationSequence name) {
        NestPropertiesNode node = new NestPropertiesNode(name);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void endNestedProperties(StringInterpolationSequence name) {
        nodeStack.pop();
    }

    public void startMixinDirective(String name, Collection<Variable> args,
            boolean hasVariableArgs) {
        MixinDefNode node = new MixinDefNode(name.trim(), args, hasVariableArgs);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void endMixinDirective() {
        nodeStack.pop();
    }

    public void startFunctionDirective(String name, Collection<Variable> args,
            boolean hasVariableArgs) {
        FunctionDefNode node = new FunctionDefNode(name.trim(), args,
                hasVariableArgs);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
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
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void ifDirective(SassListItem evaluator) {
        if (nodeStack.peek() instanceof IfNode) {
            nodeStack.pop();
        }
        IfNode node = new IfNode(evaluator);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void elseDirective() {
        if (nodeStack.peek() instanceof IfNode) {
            nodeStack.pop();
        }
        ElseNode node = new ElseNode();
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);
    }

    public void endIfElseDirective() {
        if ((nodeStack.peek() instanceof ElseNode)
                || (nodeStack.peek() instanceof IfNode)) {
            nodeStack.pop();
        }
        nodeStack.pop();
    }

    // rule that is passed to the output as-is (except variable value
    // substitution) - no children
    public void unrecognizedRule(String text) {
        SimpleNode node = new SimpleNode(text);
        nodeStack.peek().appendChild(node);
    }

    public void startKeyFrames(String keyframeName,
            StringInterpolationSequence animationName) {
        KeyframesNode node = new KeyframesNode(keyframeName, animationName);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);

    }

    public void endKeyFrames() {
        nodeStack.pop();

    }

    public void startKeyframeSelector(String selector) {
        KeyframeSelectorNode node = new KeyframeSelectorNode(selector);
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);

    }

    public void endKeyframeSelector() {
        nodeStack.pop();
    }

    public void contentDirective() {
        ContentNode node = new ContentNode();
        nodeStack.peek().appendChild(node);
    }

    public void returnDirective(SassListItem expr) {
        ReturnNode node = new ReturnNode(expr);
        nodeStack.peek().appendChild(node);
    }

    public void startInclude(String name, ActualArgumentList args ) {
        MixinNode node = new MixinNode(name, args );
        nodeStack.peek().appendChild(node);
        nodeStack.push(node);

    }

    public void endInclude() {
        nodeStack.pop();
    }

    private void log(String msg) {
        SCSSErrorHandler.get().debug( msg );
    }

    public void property(String name, SCSSLexicalUnit value, boolean important)
            throws CSSException {
        // This method needs to be here due to an implemented interface.
        throw new CSSException("Unsupported call: property(" + name + ", "
                + value + ", important: " + important + ")");
    }

}
