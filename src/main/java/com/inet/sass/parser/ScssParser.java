/*
 * Copyright 2023 i-net software
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
package com.inet.sass.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.css.sac.InputSource;

import com.inet.sass.handler.SCSSDocumentHandler;
import com.inet.sass.parser.SassList.Separator;
import com.inet.sass.selector.AttributeSelector;
import com.inet.sass.selector.AttributeSelector.MatchRelation;
import com.inet.sass.selector.ClassSelector;
import com.inet.sass.selector.Combinator;
import com.inet.sass.selector.IdSelector;
import com.inet.sass.selector.ParentSelector;
import com.inet.sass.selector.PlaceholderSelector;
import com.inet.sass.selector.PseudoClassSelector;
import com.inet.sass.selector.PseudoElementSelector;
import com.inet.sass.selector.Selector;
import com.inet.sass.selector.SimpleSelector;
import com.inet.sass.selector.SimpleSelectorSequence;
import com.inet.sass.selector.TypeSelector;

public final class ScssParser {

    private SCSSDocumentHandler documentHandler;
    private InputSource         source;
    private String              uri;

    private ScssLookAheadReader reader;

    private boolean             wasDefault;
    private boolean             wasOptional;

    /**
     * A StringBuilder which can reused inside one method. Do not call another method that also use it.
     */
    private final StringBuilder cachesBuilder = new StringBuilder();

    /**
     * A accumulate String Sequence
     */
    private List<SassListItem>  stringSequence;

    /**
     * Start to parse a single file
     * @param documentHandler the reference to the model
     * @param source the file source
     * @throws IOException if any error on reading the source occur
     */
    public void parseStyleSheet( SCSSDocumentHandler documentHandler, InputSource source ) throws IOException {
        this.documentHandler = documentHandler;
        this.source = source;
        this.uri = source.getURI();

        Reader stream = source.getCharacterStream();
        if( stream == null ) {
            stream = new InputStreamReader( source.getByteStream() );
        }
        try {
            reader = new ScssLookAheadReader( stream, source.getURI() );
            parse( true );
        } catch( ParseException ex ) {
            throw ex;
        } catch( Throwable th ) {
            throw reader.createException( th );
        } finally {
            stream.close();
        }
    }

    private void parse( boolean isRoot ) {
        for( ;; ) {
            int ch = reader.nextBlockMarker();
            switch( ch ) {
                case -1:
                    if( isRoot ) {
                        return;
                    } else {
                        throw reader.createException( "Unexpected end of Scss data" );
                    }
                case ';':
                    parseSemicolon();
                    break;
                case ':':
                    parseNestedProperties();
                    break;
                case '{':
                    parseBlock();
                    break;
                case '}':
                    if( !isRoot ) {
                        parseSemicolon();
                        consumeMarker( '}' );
                        return;
                    }
                    //$FALL-THROUGH$
                default:
                    throw reader.createException( "Unrecognized input: '" + reader.getLookAhead() + "'" );
            }
        }
    }

    /**
     * Parse an expression which ends with an semicolon.
     */
    private void parseSemicolon() {
        boolean isVariable = false;;
        StringBuilder builder = cachesBuilder;

        LOOP: for( ;; ) {
            char ch;
            try {
                ch = reader.read();
            } catch( Exception e ) {
                ch = ';'; // a not terminated line is like a lime with semicolon
            }

            if( isWhitespace( ch ) ) {
                if( builder.length() == 0 ) {
                    continue LOOP;
                }
                if( builder.charAt( 0 ) == '@' ) {
                    parseAtRule( trim( builder ) );
                    return;
                }
                builder.append( ch );
                continue LOOP;
            }

            switch( ch ) {
                case ':':
                    if( isVariable ) {
                        String name = trim( builder );
                        // check if the last item is "!default"
                        wasDefault = false;
                        SassListItem exp = parseExpressionOrList();
                        documentHandler.variable( name, exp, wasDefault );
                    } else {
                        StringInterpolationSequence name = createStringInterpolationSequence( trim( builder ) );
                        SassListItem exp = parseExpressionOrList();
                        boolean important = false; //the old JavaCC based parser has also use ever false
                        documentHandler.property( name, exp, important, null );
                    }
                    if( consumeMarkers( ';', '}' ) == '}' ) { //last line in a block does not need a semicolon
                        reader.back( '}' );
                    }
                    return;
                case '$':
                    isVariable = true;
                    continue LOOP;
                case '}':
                    reader.back( ch );
                    //$FALL-THROUGH$
                case ';':
                    if( builder.length() == 0 ) {
                        return;
                    }
                    if( builder.charAt( 0 ) == '@' ) {
                        reader.back( ch );
                        parseAtRule( trim( builder ) );
                        return;
                    }
                    throw reader.createException( "Unrecognized input: '" + builder + "'" );
                case '/':
                    if( comment() ) {
                        continue LOOP;
                    }
                    break;
                case '#':
                    if( !parseInterpolation( builder ) ) {
                        break;
                    }
                    continue LOOP;

                default:
            }
            builder.append( ch );
        }
    }

    /**
     * Parse block like:
     * 
     * <pre>
     * selector {
     *      ...
     * }
     * </pre>
     */
    private void parseBlock() {
        LOOP: for( ;; ) {
            char ch = readNonWhitespace();
            switch( ch ) {
                case '/':
                    if( comment() ) {
                        continue LOOP;
                    }
                    break;

                case '@':
                    parseAtRule( '@' + parseName( false ) );
                    return;
            }
            reader.back( ch );
            break;
        }
        List<Selector> selectorList = parseSelectorList( false );
        consumeMarker( '{' );
        documentHandler.startSelector( source.getURI(), reader.getLine(), reader.getColumn(), selectorList );
        parse( false );
        documentHandler.endSelector();
    }

    /**
     * Parse block like:
     * 
     * <pre>
     * property: {
     *      ...
     * }
     * </pre>
     */
    private void parseNestedProperties() {
        StringInterpolationSequence name = parseStringInterpolationSequence();
        consumeMarker( ':' );
        consumeMarker( '{' );
        documentHandler.startNestedProperties( name );
        parse( false );
        documentHandler.endNestedProperties();
    }

    /**
     * Parse a selector list
     * @param extendRule true, Selector for an @extend rule
     * @return the list
     */
    private List<Selector> parseSelectorList( boolean extendRule ) {
        StringBuilder builder = cachesBuilder;

        List<Selector> selList = new ArrayList<>();
        char type = ' ';
        Combinator combiner = null;
        Selector selector = null;
        List<SimpleSelector> list = new ArrayList<>();
        LOOP: for( ;; ) {
            char ch = reader.read();

            if( isWhitespace( ch ) ) {
                appendSelector( list, type, builder, extendRule );
                type = ' ';
                if( list.size() > 0 ) {
                    selector = appendSelector( selector, combiner, list );
                    combiner = Combinator.DESCENDANT;
                }
                continue LOOP;
            }

            switch( ch ) {
                case '>':
                case '+':
                case '~':
                    appendSelector( list, type, builder, extendRule );
                    type = ' ';
                    if( list.size() > 0 ) {
                        selector = appendSelector( selector, combiner, list );
                    }
                    switch( ch ) {
                        case '>':
                            combiner = Combinator.CHILD;
                            break;
                        case '+':
                            combiner = Combinator.SIBLING;
                            break;
                        case '~':
                            combiner = Combinator.GENERAL_SIBLING;
                            break;
                    }
                    continue LOOP;
                case '{':
                case ';':
                case '}':
                    appendSelector( list, type, builder, extendRule );
                    if( list.size() > 0 || selector == null ) {
                        selector = appendSelector( selector, combiner, list );
                    }
                    selList.add( selector );
                    reader.back( ch );
                    return selList;
                case ',':
                    appendSelector( list, type, builder, extendRule );
                    type = ' ';
                    if( list.size() > 0 || selector == null ) {
                        selector = appendSelector( selector, combiner, list );
                    }
                    selList.add( selector );
                    selector = null;
                    combiner = null;
                    continue LOOP;
                case '/':
                    comment();
                    continue LOOP;
                case '&':
                    appendSelector( list, type, builder, extendRule );
                    type = ' ';
                    list.add( ParentSelector.it );
                    continue LOOP;
                case '#':
                    if( parseInterpolation( builder ) ) {
                        continue LOOP;
                    }
                    //$FALL-THROUGH$
                case '%':
                case '.':
                    if( isNextIdentifierPart() ) {
                        appendSelector( list, type, builder, extendRule );
                        type = ch;
                        continue LOOP;
                    }
                    break;
                case '[':
                    appendSelector( list, type, builder, extendRule );
                    type = ' ';
                    list.add( parseAttributeSelector() );
                    continue LOOP;
                case ':':
                    appendSelector( list, type, builder, extendRule );
                    if( ':' == (ch = reader.read()) ) {
                        type = ch;
                    } else {
                        reader.back( ch );
                        type = ' ';
                        list.add( parsePseudoClassSelector() );
                    }
                    continue LOOP;
            }
            builder.append( ch );
        }
    }

    /**
     * Append a selector to the list if there content.
     * @param list the target
     * @param type the type of the selector
     * @param builder the builder with content
     * @param extendRule true, Selector for an @extend rule
     */
    private void appendSelector( List<SimpleSelector> list, int type, StringBuilder builder, boolean extendRule ) {
        if( extendRule && "!optional".contentEquals( builder ) ) {
            wasOptional = true;
            builder.setLength( 0 );
        }
        if( stringSequence != null || builder.length() > 0 || type != ' ' ) {
            SimpleSelector sel;
            StringInterpolationSequence value = createStringInterpolationSequence( builder );
            switch( type ) {
                case '%':
                    sel = new PlaceholderSelector( value );
                    break;
                case '.':
                    sel = new ClassSelector( value );
                    break;
                case '#':
                    sel = new IdSelector( value );
                    break;
                case ':':
                    sel = new PseudoElementSelector( value );
                    break;
                default:
                    sel = new TypeSelector( value );
            }
            list.add( sel );
        }
    }

    /**
     * Append a selector or create the first. After it delete the list of consumed simple selectors.
     * @param selector previous selector
     * @param combiner the combiner
     * @param list a list with simple selectors to append.
     * @return the new selector, never null
     */
    private Selector appendSelector( Selector selector, Combinator combiner, List<SimpleSelector> list ) {
        SimpleSelectorSequence selSequence = new SimpleSelectorSequence( list );
        list.clear();
        if( selector == null ) {
            return combiner == null ? new Selector( selSequence ) : new Selector( combiner, selSequence );
        } else {
            if( combiner == null ) {
                combiner = Combinator.DESCENDANT;
            }
            return selector.createNested( combiner, selSequence );
        }
    }

    /**
     * Parse an AttributeSelector like <code>[attr=value]</code>
     * @return the selector
     */
    private AttributeSelector parseAttributeSelector() {
        StringInterpolationSequence attribute = parseStringInterpolationSequence();
        char ch = reader.read();
        MatchRelation matchRelation;
        switch( ch ) {
            case '=':
                matchRelation = MatchRelation.EQUALS;
                break;
            case '|':
                matchRelation = MatchRelation.DASHMATCH;
                consumeMarker( '=' );
                break;
            case '^':
                matchRelation = MatchRelation.PREFIXMATCH;
                consumeMarker( '=' );
                break;
            case '$':
                matchRelation = MatchRelation.SUFFIXMATCH;
                consumeMarker( '=' );
                break;
            case '*':
                matchRelation = MatchRelation.SUBSTRINGMATCH;
                consumeMarker( '=' );
                break;
            case '~':
                matchRelation = MatchRelation.INCLUDES;
                consumeMarker( '=' );
                break;
            case ']':
                matchRelation = null;
                reader.back( ch );
                break;
            default:
                throw reader.createException( "Unrecognized input: '" + ch + "'" );
        }
        StringInterpolationSequence value = matchRelation == null ? null : parseStringInterpolationSequence();
        consumeMarker( ']' );
        return new AttributeSelector( attribute, matchRelation, value );
    }

    /**
     * Parse pseudo class selector like <code>:foo(2n+1)</code>
     * @return the selector
     */
    private PseudoClassSelector parsePseudoClassSelector() {
        StringInterpolationSequence pseudoClass = parseStringInterpolationSequence();
        String argument;
        char ch = reader.read();
        if( ch == '(' ) {
            argument = parseUnquotedString();
        } else {
            argument = null;
            reader.back( ch );
        }
        return new PseudoClassSelector( pseudoClass, argument );
    }

    private MediaList mediaStatement() {
        StringBuilder builder = cachesBuilder;

        MediaList ml = new MediaList();
        LOOP: for( ;; ) {
            char ch = reader.read();
            switch( ch ) {
                case ';':
                case '{':
                    if( stringSequence == null ) {
                        ml.addItem( trim( builder ) );
                    } else {
                        ml.addItem( createStringInterpolationSequence( builder ) );
                    }
                    reader.back( ch );
                    break LOOP;
                case '#':
                    if( !parseInterpolation( builder ) ) {
                        break;
                    }
                    continue LOOP;
                case '$':
                    List<SassListItem> sequence = stringSequence;
                    if( sequence == null ) {
                        sequence = stringSequence = new ArrayList<>();
                    }
                    if( builder.length() > 0 ) {
                        sequence.add( new StringItem( str( builder ) ) );
                    }
                    int line = reader.getLine();
                    int column = reader.getColumn();
                    SassListItem item = LexicalUnitImpl.createVariable( uri, line, column, parseName( false ) );
                    sequence.add( new Interpolation( item, line, column ) );
                    continue LOOP;

                default:
            }
            builder.append( ch );
        }
        return ml;
    }

    /**
     * Parse the rules starts with an @ character
     * @param rule the name of the rule
     */
    private void parseAtRule( String rule ) {
        char ch;

        SWITCH: for( ;; ) {
            switch( rule ) {
                case "@charset":
                    String encoding = parseQuotedString( consumeMarkers( '\'', '\"' ) );
                    consumeMarker( ';' );
                    source.setEncoding( encoding );
                    return;

                case "@import":
                    ch = readNonWhitespace();
                    String uri;
                    MediaList ml;
                    boolean isURL;
                    switch( ch ) {
                        case '\'':
                        case '\"':
                            uri = parseQuotedString( ch );
                            isURL = false;
                            ml = null;
                            break;
                        case 'u':
                            char ch2 = reader.read();
                            char ch3 = reader.read();
                            if( ch2 != 'r' || ch3 != 'l' ) {
                                throw reader.createException( "Unrecognized input: '" + ch + ch2 + ch3 + "'" );
                            }
                            consumeMarker( '(' );
                            uri = parseUnquotedString();
                            isURL = true;
                            ml = mediaStatement();
                            if( ml.getLength() == 0 ) {
                                // see section 6.3 of the CSS2 recommandation.
                                ml.addItem( "all" );
                            }
                            break;
                        default:
                            throw reader.createException( "Unrecognized input: '" + ch + "'" );
                    }
                    consumeMarker( ';' );
                    documentHandler.importStyle( uri, ml, isURL );
                    return;

                case "@include":
                    ActualArgumentList args;
                    String name = parseName( true );
                    ch = readNonWhitespace();
                    switch( ch ) {
                        default:
                            reader.back( ch );
                            //$FALL-THROUGH$
                        case ';':
                        case '{':
                            args = new ActualArgumentList( Separator.COMMA, Collections.emptyList() );
                            break;
                        case '(':
                            args = argValuelist( false );
                            ch = readNonWhitespace();
                            switch( ch ) {
                                default:
                                    reader.back( ch );
                                    //$FALL-THROUGH$
                                case ';':
                                case '{':
                                    break;
                            }
                            break;

                    }
                    documentHandler.startInclude( name, args );
                    if( ch == '{' ) {
                        parse( false );
                    }
                    documentHandler.endInclude();
                    return;

                case "@extend":
                    wasOptional = false;
                    List<Selector> selectorList = parseSelectorList( true );
                    documentHandler.extendDirective( selectorList, wasOptional );
                    return;

                case "@mixin":
                    parseFunctionMixin( true );
                    return;

                case "@if":
                    SassListItem evaluator = parseExpression( true );
                    consumeMarker( '{' );
                    documentHandler.startIfElseDirective();
                    documentHandler.ifDirective( evaluator );
                    parse( false );

                    // Look Ahead for @else
                    ELSE: for( ;; ) {
                        try {
                            ch = readNonWhitespace();
                        } catch( ParseException e ) {
                            // End of file?
                            ch = ' ';
                        }
                        if( ch != '@' ) {
                            reader.back( ch ); // '@'
                        } else {
                            rule = '@' + parseName( false );
                            if( "@else".equals( rule ) ) {
                                ch = consumeMarkers( '{', 'i' );
                                if( 'i' == ch ) {
                                    consumeMarker( 'f' );
                                    evaluator = parseExpression( true );
                                    consumeMarker( '{' );
                                    documentHandler.ifDirective( evaluator );
                                    parse( false );
                                    continue ELSE;
                                }
                                documentHandler.elseDirective();
                                parse( false );
                            } else if( "@elseif".equals( rule ) ) {
                                evaluator = parseExpression( true );
                                consumeMarker( '{' );
                                documentHandler.ifDirective( evaluator );
                                parse( false );
                                continue ELSE;
                            } else {
                                documentHandler.endIfElseDirective();
                                // continue with the next rule
                                continue SWITCH;
                            }
                        }
                        break ELSE;
                    }
                    documentHandler.endIfElseDirective();
                    return;

                case "@else":
                    throw reader.createException( "Unrecognized input: '" + rule + "'" );

                case "@font-face":
                    consumeMarker( '{' );
                    documentHandler.startFontFace();
                    parse( false );
                    documentHandler.endFontFace();
                    return;

                case "@function":
                    parseFunctionMixin( false );
                    return;

                case "@return":
                    documentHandler.returnDirective( parseExpressionOrList() );
                    return;

                case "@content":
                    documentHandler.contentDirective();
                    return;

                case "@each":
                    parseEach();
                    return;

                case "@for":
                    parseFor();
                    return;

                case "@while":
                    parseWhile();
                    return;

                case "@media":
                    media();
                    return;

                case "@keyframes":
                case "@-moz-keyframes":
                case "@-o-keyframes":
                case "@-webkit-keyframes":
                case "@-ms-keyframes":
                    parseKeyframes( rule );
                    return;

                case "@debug":
                    documentHandler.debugDirective( parseExpressionOrList() );
                    consumeMarker( ';' );
                    return;

                case "@warn":
                    documentHandler.warnDirective( parseExpressionOrList() );
                    consumeMarker( ';' );
                    return;

                case "@error":
                    documentHandler.errorDirective( parseExpressionOrList() );
                    consumeMarker( ';' );
                    return;

                default:
                    parseUnrecognizedAtRule( rule );
                    return;
            }
        }
    }

    /**
     * parse the rule @mixin and @function.
     * @param mixin true, if mixin
     */
    private void parseFunctionMixin( boolean mixin ) {
        FormalArgumentList args;
        String name = parseName( true );

        char ch = readNonWhitespace();
        switch( ch ) {
            case '{':
                args = new FormalArgumentList( null, false );
                break;
            case '(':
                args = argValuelist( true );
                consumeMarker( '{' );
                break;
            default:
                throw reader.createException( "Unrecognized input: '" + ch + "'" );

        }
        if( mixin ) {
            documentHandler.startMixinDirective( name, args );
        } else {
            documentHandler.startFunctionDirective( name, args );
        }
        parse( false );
        if( mixin ) {
            documentHandler.endMixinDirective();
        } else {
            documentHandler.endFunctionDirective();
        }
    }

    /**
     * Parse the @each rule
     */
    private void parseEach() {
        List<String> variables = new ArrayList<>();
        StringBuilder builder = cachesBuilder;
        boolean wasName = false;

        LOOP: for( ;; ) {
            char ch = reader.read();

            if( isWhitespace( ch ) ) {
                if( builder.length() == 0 ) {
                    continue LOOP;
                }
                String in = trim( builder );
                if( !"in".equals( in ) ) {
                    throw reader.createException( "Unrecognized input: '" + in + "'" );
                }
                break LOOP;
            }

            switch( ch ) {
                case '$':
                    if( wasName || builder.length() > 0 ) {
                        throw reader.createException( "Unrecognized input: '" + ch + "'" );
                    }
                    variables.add( parseName( false ) );
                    wasName = true;
                    continue LOOP;
                case ',':
                    if( !wasName || builder.length() > 0 ) {
                        throw reader.createException( "Unrecognized input: '" + ch + "'" );
                    }
                    wasName = false;
                    continue LOOP;
                default:
                    builder.append( ch );
                    continue LOOP;
            }
        }
        SassListItem list = parseExpressionOrList();
        consumeMarker( '{' );
        documentHandler.startEachDirective( variables, list );
        parse( false );
        documentHandler.endEachDirective();

    }

    /**
     * Parse @while rule
     */
    private void parseFor() {
        consumeMarker( '$' );
        String var = parseName( false );
        String key = parseName( true );
        if( !"from".equals( key ) ) {
            throw reader.createException( "Unrecognized input: '" + key + "'" );
        }
        SassListItem from = parseExpression( true );
        key = parseName( true );
        boolean exclusive;
        if( "through".equals( key ) ) {
            exclusive = false;
        } else if( "to".equals( key ) ) {
            exclusive = true;
        } else {
            throw reader.createException( "Unrecognized input: '" + key + "'" );
        }
        SassListItem to = parseExpression( true );
        consumeMarker( '{' );
        documentHandler.startForDirective( var, from, to, exclusive );
        parse( false );
        documentHandler.endForDirective();
    }

    /**
     * Parse @while rule
     */
    private void parseWhile() {
        SassListItem condition = parseExpressionOrList();
        consumeMarker( '{' );
        documentHandler.startWhileDirective( condition );
        parse( false );
        documentHandler.endWhileDirective();
    }

    /**
     * Parse @media rule
     */
    private void media() {
        MediaList media = mediaStatement();
        consumeMarker( '{' );
        documentHandler.startMedia( media );
        parse( false );
        documentHandler.endMedia();
    }

    /**
     * Parse @keyframe rules
     * @param keyframeName the specific keyframe rule name
     */
    private void parseKeyframes( String keyframeName ) {
        StringInterpolationSequence animationName = parseStringInterpolationSequence();
        documentHandler.startKeyFrames( keyframeName, animationName );
        consumeMarker( '{' );

        StringBuilder builder = cachesBuilder;
        SELECTOR: for( ;; ) {
            LOOP: for( ;; ) {
                char ch = reader.read();
                switch( ch ) {
                    case '{':
                        break LOOP;
                    case '}':
                        break SELECTOR;
                }
                if( !isWhitespace( ch ) || builder.length() > 0 ) {
                    builder.append( ch );
                }
            }
            String selector = trim( builder );
            documentHandler.startKeyframeSelector( selector );
            parse( false );
            documentHandler.endKeyframeSelector();
        }
        if( builder.length() > 0 ) {
            // if the KeyframeSelector logic is not needed then this is the only what we need to call
            reader.back( '}' );
            reader.back( trim( builder ) );
            parse( false );
        }

        documentHandler.endKeyFrames();
    }

    /**
     * Parse any unknown rule 1:1 as CSS rule.
     * @param rule the rule name
     */
    private void parseUnrecognizedAtRule( String rule ) {
        StringBuilder builder = cachesBuilder;
        builder.append( rule );

        int braceCount = 0;
        LOOP: for( ;; ) {
            char ch;
            try {
                ch = reader.read();
            } catch( Exception e ) {
                break;
            }
            switch( ch ) {
                case '}':
                    builder.append( ch );
                    if( --braceCount == 0 ) {
                        break LOOP;
                    }
                    continue LOOP;
                case '{':
                    braceCount++;
                    break;
                default:
            }
            builder.append( ch );
        }
        documentHandler.unrecognizedRule( trim( builder ) );
    }

    /**
     * Create a single SassListItem or an List
     * @return the list
     */
    private SassListItem parseExpressionOrList() {
        SassListItem first = parseExpression( true );
        return parseExpressionOrList( first, false );
    }

    private SassListItem parseExpressionOrList( SassListItem first, boolean hasParentWithComma ) {
        SassListItem next = null;
        Object list = first;
        Separator sep = null;
        LOOP: for( ;; ) {
            char ch = reader.read();
            switch( ch ) {
                case ',':
                    list = concat( list, next );
                    if( sep == Separator.SPACE || hasParentWithComma ) {
                        reader.back( ch );
                        if( next == null ) {
                            return first;
                        }
                        next = new SassList( sep, (ArrayList<SassListItem>)list );
                        return hasParentWithComma ? next : parseExpressionOrList( next, false );
                    } else if( sep == null ) {
                        sep = Separator.COMMA;
                    } else if( next == null ) {
                        // more as one comma at end
                        throw reader.createException( "Unrecognized input: '" + ch + "'" );
                    }
                    next = parseExpression( false );
                    continue LOOP;
                case ';':
                case ')':
                case '}':
                case '{':
                    reader.back( ch );
                    break LOOP;
                default:
                    reader.back( ch );
                    //$FALL-THROUGH$
                case ' ':
                    if( sep == Separator.COMMA ) {
                        reader.back( ch );
                        next = parseExpressionOrList( next, true );
                        continue LOOP;
                    }
                    sep = Separator.SPACE;
                    list = concat( list, next );
                    next = parseExpression( false );
                    if( next == null ) {
                        break LOOP;
                    }
                    continue LOOP;
            }
        }
        if( next == null && list == first ) {
            return first;
        }
        list = concat( list, next );
        return new SassList( sep, (ArrayList<SassListItem>)list );
    }

    /**
     * Parse an expression
     * @param first true, if first and should not be null
     * @return the expression
     */
    private SassListItem parseExpression( boolean first ) {
        char ch2;
        StringBuilder builder = cachesBuilder;
        Object left = null;
        SassListItem right;
        boolean wasWhite = false;
        boolean wasOperation = false;
        LOOP: for( ;; ) {
            char ch = reader.read();

            if( isWhitespace( ch ) ) {
                left = concatIfnotEmpty( left, builder );
                if( left != null ) {
                    if( !wasOperation ) {
                        wasWhite = true;
                    }
                    // for extra spaces in the output, required for the tests
                    left = concat( left, LexicalUnitImpl.createSpace( uri, reader.getLine(), reader.getColumn() ) );
                }
                continue LOOP;
            }

            switch( ch ) {
                case '.':
                    if( !isNextDigit( false ) ) {
                        reader.back( ch );
                        break LOOP;
                    }
                    //$FALL-THROUGH$
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if( builder.length() == 0 ) {
                        if( wasWhite ) {
                            break;
                        }
                        wasOperation = false;
                        left = concat( left, parseNumber( ch ) );
                        continue LOOP;
                    }
                    break;
                case '+':
                    left = concatIfnotEmpty( left, builder );
                    //$FALL-THROUGH$
                case '-':
                    if( builder.length() == 0 && stringSequence == null ) {
                        if( left == null || wasWhite || wasOperation ) {
                            // unary operator
                            if( isNextDigit( true ) ) {
                                if( wasWhite ) {
                                    break;
                                }
                                wasOperation = false;
                                left = concat( left, parseNumber( ch ) );
                                continue LOOP;
                            }
                            if( isNextIdentifierPart() ) {
                                break;
                            }
                            wasWhite = false;
                            if( left == null || wasOperation ) {
                                // add zero as operand to covert to a binary operator
                                left = LexicalUnitImpl.createNumber( uri, reader.getLine(), reader.getColumn(), 0 );
                            }
                        }
                        wasOperation = true;
                        left = concat( left, ch == '+' ? //
                            LexicalUnitImpl.createAdd( uri, reader.getLine(), reader.getColumn() ) : //
                            LexicalUnitImpl.createMinus( uri, reader.getLine(), reader.getColumn() ) );
                        continue LOOP;
                    }
                    break;
                case '/':
                    if( comment() ) {
                        continue LOOP;
                    }
                    wasOperation = true;
                    wasWhite = false;
                    left = concat( concatIfnotEmpty( left, builder ), LexicalUnitImpl.createSlash( uri, reader.getLine(), reader.getColumn() ) );
                    continue LOOP;
                case '*':
                    wasOperation = true;
                    wasWhite = false;
                    left = concat( concatIfnotEmpty( left, builder ), LexicalUnitImpl.createMultiply( uri, reader.getLine(), reader.getColumn() ) );
                    continue LOOP;
                case '%':
                    wasOperation = true;
                    wasWhite = false;
                    left = concat( concatIfnotEmpty( left, builder ), LexicalUnitImpl.createModulo( uri, reader.getLine(), reader.getColumn() ) );
                    continue LOOP;
                case '>':
                    wasOperation = true;
                    wasWhite = false;
                    ch2 = reader.read();
                    if( ch2 == '=' ) {
                        right = LexicalUnitImpl.createGreaterThanOrEqualTo( uri, reader.getLine(), reader.getColumn() );
                    } else {
                        reader.back( ch2 );
                        right = LexicalUnitImpl.createGreaterThan( uri, reader.getLine(), reader.getColumn() );
                    }
                    left = concat( concatIfnotEmpty( left, builder ), right );
                    continue LOOP;
                case '<':
                    wasOperation = true;
                    wasWhite = false;
                    ch2 = reader.read();
                    if( '=' == ch2 ) {
                        right = LexicalUnitImpl.createLessThanOrEqualTo( uri, reader.getLine(), reader.getColumn() );
                    } else {
                        reader.back( ch2 );
                        right = LexicalUnitImpl.createLessThan( uri, reader.getLine(), reader.getColumn() );
                    }
                    left = concat( concatIfnotEmpty( left, builder ), right );
                    continue LOOP;
                case '=':
                    ch2 = reader.read();
                    if( '=' == ch2 ) {
                        wasOperation = true;
                        wasWhite = false;
                        left = concat( concatIfnotEmpty( left, builder ), LexicalUnitImpl.createEquals( uri, reader.getLine(), reader.getColumn() ) );
                        continue LOOP;
                    } else {
                        reader.back( ch2 );
                        break;
                    }
                case '!':
                    ch2 = reader.read();
                    if( '=' == ch2 ) {
                        wasOperation = true;
                        wasWhite = false;
                        left = concat( concatIfnotEmpty( left, builder ), LexicalUnitImpl.createNotEqual( uri, reader.getLine(), reader.getColumn() ) );
                        continue LOOP;
                    } else {
                        reader.back( ch2 );
                        break;
                    }
                case '&':
                    left = concatIfnotEmpty( left, builder );
                    if( left != null ) {
                        reader.back( ' ' );
                        break LOOP;
                    }
                    return LexicalUnitImpl.createParent( uri, reader.getLine(), reader.getColumn() );
                case '{':
                    if( builder.length() > 0 && builder.charAt( builder.length() - 1 ) == '#' ) {
                        // interpolation
                        for( ;; ) {
                            builder.append( ch );
                            ch = reader.read();
                            if( ch == '}' ) {
                                break;
                            }
                        }
                        break;
                    }
                    //$FALL-THROUGH$
                case ';':
                case ')':
                case '}':
                case ',':
                    reader.back( ch );
                    break LOOP;
                case ':':
                    // map
                    left = concatIfnotEmpty( left, builder );
                    right = parseExpression( first );
                    return new SassList( Separator.COLON, toSassListItem( left, first ), right );
                case '"':
                case '\'':
                    if( builder.length() > 0 ) {
                        throw reader.createException( "Unrecognized input: '" + builder + ch + "'" );
                    }
                    if( wasWhite ) {
                        break;
                    }
                    wasOperation = false;
                    left = concat( left, LexicalUnitImpl.createString( uri, reader.getLine(), reader.getColumn(), parseQuotedStringInterpolationSequence( ch ) ) );
                    continue LOOP;
                case '(':
                    wasOperation = false;
                    if( wasWhite ) {
                        break;
                    }
                    String fname = trim( builder );
                    switch( fname ) {
                        case "url":
                            left = concat( left, parseUrlFunction() );
                            break;
                        case "expression":
                            left = LexicalUnitImpl.createIdent( uri, reader.getLine(), reader.getColumn(), fname + '(' + parseUnquotedString() + ')' );
                            break;
                        default:
                            ActualArgumentList params = argValuelist( false );
                            left = concat( left, LexicalUnitImpl.createFunction( uri, reader.getLine(), reader.getColumn(), fname, params ) );
                            break;
                        case "":
                            right = parseExpressionOrList();
                            if( right.getClass() == SassList.class && ((SassList)right).getSeparator() == Separator.COLON ) {
                                // map item required a Comma separated list on top
                                right = new SassList( Separator.COMMA, right );
                            }
                            left = concat( left, right );
                            consumeMarker( ')' );
                    }
                    continue LOOP;
                case '#':
                    if( wasWhite ) {
                        break;
                    }
                    if( parseInterpolation( builder ) ) {
                        continue LOOP;
                    }
                    break;
                case 'a':
                case 'o':
                case 'n':
                    if( builder.length() == 0 ) {
                        builder.append( ch );
                        String name = parseName( false ); // use the builder
                        switch( name ) {
                            case "and":
                                left = concat( left, LexicalUnitImpl.createAnd( uri, reader.getLine(), reader.getColumn() ) );
                                wasOperation = true;
                                wasWhite = false;
                                continue LOOP;
                            case "or":
                                left = concat( left, LexicalUnitImpl.createOr( uri, reader.getLine(), reader.getColumn() ) );
                                wasOperation = true;
                                wasWhite = false;
                                continue LOOP;
                            case "not":
                                left = concat( left, LexicalUnitImpl.createIdent( uri, reader.getLine(), reader.getColumn(), "false" ) );
                                left = concat( left, LexicalUnitImpl.createEquals( uri, reader.getLine(), reader.getColumn() ) );
                                wasOperation = true;
                                wasWhite = false;
                                continue LOOP;
                            default:
                                if( wasWhite ) {
                                    reader.back( name );
                                    reader.back( ' ' );
                                    return toSassListItem( left, first );
                                }
                                builder.append( name );
                                continue LOOP;
                        }
                    }
                    break;
                default:
            }
            if( wasWhite ) {
                reader.back( ch );
                reader.back( ' ' );
                return toSassListItem( left, first );
            }
            builder.append( ch );
            wasOperation = false;
        }

        left = concatIfnotEmpty( left, builder );
        return toSassListItem( left, first );
    }

    /**
     * Concatenate the builder if not empty and clear it after it.
     * @param left null, SassListItem or list of SassListItem
     * @param builder the builder
     * @return
     */
    private Object concatIfnotEmpty( Object left, StringBuilder builder ) {
        SassListItem right;
        if( stringSequence != null ) {
            right = LexicalUnitImpl.createIdent( uri, reader.getLine(), reader.getColumn(), createStringInterpolationSequence( builder ) );
            return concat( left, right );
        }
        if( builder.length() == 0 ) {
            return left;
        }
        String trim = trim( builder );
        if( trim.isEmpty() ) {
            return left;
        }
        if( trim.startsWith( "$" ) ) {
            right = LexicalUnitImpl.createVariable( uri, reader.getLine(), reader.getColumn() - trim.length(), trim.substring( 1 ) );
        } else {
            switch( trim ) {
                case "!default":
                    wasDefault = true;
                    return left;
            }
            right = LexicalUnitImpl.createIdent( uri, reader.getLine(), reader.getColumn() - trim.length(), trim );
        }
        return concat( left, right );
    }

    /**
     * Concatenate two or more items
     * @param left null, SassListItem or list of SassListItem
     * @param right the SassListItem that should be added
     * @return the new left
     */
    private Object concat( Object left, SassListItem right ) {
        if( left == null ) {
            return right;
        }
        if( right == null ) {
            return left;
        }
        ArrayList<SassListItem> list;
        if( left.getClass() == ArrayList.class ) {
            list = (ArrayList<SassListItem>)left;
        } else {
            list = new ArrayList<>();
            list.add( (SassListItem)left );
        }
        list.add( right );
        return list;
    }

    /**
     * Convert the object to an SassListItem. If it is an list then it will create an expression.
     * @param left the object
     * @param first true, if first and should not be null
     * @return the casted item
     */
    SassListItem toSassListItem( Object left, boolean first ) {
        if( left == null ) {
            if( wasDefault || !first ) {
                return null;
            }
            char ch = reader.read();
            if( ch == ')' ) {
                // "()" -> empty list
                reader.back( ')' );
                return first ? new SassList() : null;
            }
            throw reader.createException( "Unrecognized input: '" + ch + "'" );
        }
        if( left.getClass() == ArrayList.class ) {
            return SassExpression.createExpression( (ArrayList)left );
        }
        return (SassListItem)left;
    }

    /**
     * If the next character is a digit
     * @param orPoint true, if point '.' is also valid
     * @return true, if a digit
     */
    private boolean isNextDigit( boolean orPoint ) {
        char ch = reader.read();
        reader.back( ch );
        switch( ch ) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
            default:
                return orPoint ? ch == '.' : false;
        }
    }

    /**
     * If the next character can be part of an identifier
     * @return true, if a digit
     */
    private boolean isNextIdentifierPart() {
        char ch = reader.read();
        reader.back( ch );
        switch( ch ) {
            case '-':
            case '#': // content of interpolation can also be an identifier
                return true;
            default:
                return Character.isLetter( ch );
        }
    }

    /**
     * Parse an quoted string
     * @param quote the quote character, already consumed
     * @return the string without the quotes
     */
    private String parseQuotedString( char quote ) {
        StringBuilder builder = cachesBuilder;
        boolean isSlash = false;
        for( ;; ) {
            char ch = reader.read();
            if( ch == quote && !isSlash ) {
                return str( builder );
            }
            builder.append( ch );
            isSlash = ch == '\\' && !isSlash;
        }
    }

    /**
     * Parse an quoted string
     * @param quote the quote character, already consumed
     * @return the string without the quotes
     */
    private StringInterpolationSequence parseQuotedStringInterpolationSequence( char quote ) {
        StringBuilder builder = cachesBuilder;
        boolean isSlash = false;
        LOOP: for( ;; ) {
            char ch = reader.read();
            switch( ch ) {
                case '\\':
                    isSlash = !isSlash;
                    break;
                case '#':
                    if( parseInterpolation( builder ) ) {
                        continue LOOP;
                    }
                    break;
                default:
                    if( ch == quote && !isSlash ) {
                        return createStringInterpolationSequence( builder );
                    }
                    isSlash = false;
            }
            builder.append( ch );
        }
    }

    /**
     * Parse a number item
     * @param ch the first digit
     * @return the item
     */
    private SassListItem parseNumber( char ch ) {
        StringBuilder builder = cachesBuilder;

        // parse number value
        builder.append( ch );
        LOOP: for( ;; ) {
            ch = reader.read();
            switch( ch ) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    builder.append( ch );
                    break;
                default:
                    break LOOP;
            }
        }
        float val = Float.parseFloat( trim( builder ) );

        // parse the unit value
        LOOP: for( ;; ) {
            if( Character.isLetter( ch ) || ch == '%' || ch == '\ufffd' ) { //TODO the '\ufffd' is a hack for the valo test because it is read as ASCII
                builder.append( ch );
            } else {
                reader.back( ch );
                break LOOP;
            }
            ch = reader.read();
        }

        // create the numeric item
        String unit = trim( builder );
        switch( unit ) {
            case "":
                return LexicalUnitImpl.createNumber( uri, reader.getLine(), reader.getColumn(), val );
            case "%":
                return LexicalUnitImpl.createPercentage( uri, reader.getLine(), reader.getColumn(), val );
            case "pt":
                return LexicalUnitImpl.createPT( uri, reader.getLine(), reader.getColumn(), val );
            case "mm":
                return LexicalUnitImpl.createMM( uri, reader.getLine(), reader.getColumn(), val );
            case "cm":
                return LexicalUnitImpl.createCM( uri, reader.getLine(), reader.getColumn(), val );
            case "pc":
                return LexicalUnitImpl.createPC( uri, reader.getLine(), reader.getColumn(), val );
            case "in":
                return LexicalUnitImpl.createIN( uri, reader.getLine(), reader.getColumn(), val );
            case "px":
                return LexicalUnitImpl.createPX( uri, reader.getLine(), reader.getColumn(), val );
            case "em":
                return LexicalUnitImpl.createEMS( uri, reader.getLine(), reader.getColumn(), val );
            case "lem":
                return LexicalUnitImpl.createLEM( uri, reader.getLine(), reader.getColumn(), val );
            case "rem":
                return LexicalUnitImpl.createREM( uri, reader.getLine(), reader.getColumn(), val );
            case "exs":
                return LexicalUnitImpl.createEXS( uri, reader.getLine(), reader.getColumn(), val );
            case "deg":
                return LexicalUnitImpl.createDEG( uri, reader.getLine(), reader.getColumn(), val );
            case "rad":
                return LexicalUnitImpl.createRAD( uri, reader.getLine(), reader.getColumn(), val );
            case "grad":
                return LexicalUnitImpl.createGRAD( uri, reader.getLine(), reader.getColumn(), val );
            case "ms":
                return LexicalUnitImpl.createMS( uri, reader.getLine(), reader.getColumn(), val );
            case "s":
                return LexicalUnitImpl.createS( uri, reader.getLine(), reader.getColumn(), val );
            case "Hz":
                return LexicalUnitImpl.createHZ( uri, reader.getLine(), reader.getColumn(), val );
            case "kHz":
                return LexicalUnitImpl.createKHZ( uri, reader.getLine(), reader.getColumn(), val );
            default:
                return LexicalUnitImpl.createDimen( uri, reader.getLine(), reader.getColumn(), val, unit );
        }
    }

    /**
     * Parse an ident/name
     * @param skipWhitespaces true, skip leading spaces
     * @return the name
     */
    private String parseName( boolean skipWhitespaces ) {

        StringBuilder builder = cachesBuilder;

        LOOP: for( ;; ) {
            char ch = reader.read();

            if( isWhitespace( ch ) ) {
                if( skipWhitespaces && builder.length() == 0 ) {
                    continue LOOP;
                }
                reader.back( ch );
                break LOOP;
            }

            switch( ch ) {
                case '-':
                case '_':
                    builder.append( ch );
                    continue LOOP;
                default:
                    if( Character.isLetterOrDigit( ch ) ) {
                        builder.append( ch );
                    } else {
                        reader.back( ch );
                        break LOOP;
                    }
            }
        }
        return trim( builder );
    }

    /**
     * Parse any characters as string until the closing parenthesis <code>)</code>.
     * @return the the string value
     */
    private String parseUnquotedString() {
        StringBuilder builder = cachesBuilder;

        LOOP: for( ;; ) {
            char ch = reader.read();
            if( ch == ')' ) {
                return trim( builder );
            }
            builder.append( ch );
        }
    }

    /**
     * Parse the parameters of the url(x) function
     * @return the expression
     */
    private LexicalUnitImpl parseUrlFunction() {
        char ch = readNonWhitespace();
        switch( ch ) {
            case '\'':
            case '"':
                char ch2 = reader.read();
                reader.back( ch2 );
                switch( ch2 ) {
                    case '#':
                    case '$':
                        break;
                    default:
                        LexicalUnitImpl left = LexicalUnitImpl.createURL( uri, reader.getLine(), reader.getColumn(), ch + parseQuotedString( ch ) + ch );
                        consumeMarker( ')' );
                        return left;
                }
                //$FALL-THROUGH$
            case '$':
                // if the parameter is a variable reference then parse it as simple function
                reader.back( ch );
                ActualArgumentList params = argValuelist( false );
                return LexicalUnitImpl.createFunction( uri, reader.getLine(), reader.getColumn(), "url", params );
            default:
                reader.back( ch );
                return LexicalUnitImpl.createURL( uri, reader.getLine(), reader.getColumn(), parseUnquotedString() );
        }
    }

    /**
     * Parse sequence of strings and interpolation
     * @return a StringInterpolationSequence
     */
    private StringInterpolationSequence parseStringInterpolationSequence() {
        StringBuilder builder = cachesBuilder;

        LOOP: for( ;; ) {
            char ch = reader.read();

            if( isWhitespace( ch ) ) {
                if( builder.length() == 0 ) {
                    continue LOOP;
                }
                reader.back( ch );
                break LOOP;
            }

            switch( ch ) {
                case '#':
                    if( parseInterpolation( builder ) ) {
                        continue LOOP;
                    }
                    break;
                case '"':
                case '\'':
                    List<SassListItem> sequence = stringSequence;
                    if( sequence == null ) {
                        sequence = stringSequence = new ArrayList<>();
                    }
                    if( builder.length() > 0 ) {
                        String trim = trim( builder );
                        if( !trim.isEmpty() ) {
                            sequence.add( new StringItem( trim ) );
                        }
                    }
                    sequence.add( LexicalUnitImpl.createString( uri, reader.getLine(), reader.getColumn(), parseQuotedString( ch ) ) );
                    continue LOOP;
                case ':':
                case '{':
                case '}':
                case '(':
                case '=':
                case ']':
                case '.':
                case ',':
                    reader.back( ch );
                    break LOOP;
            }
            builder.append( ch );
        }
        return createStringInterpolationSequence( builder );
    }

    /**
     * Concatenate the sequence and the string to one StringInterpolationSequence,
     * @param str possible empty string
     * @return the StringInterpolationSequence, never null
     */
    private StringInterpolationSequence createStringInterpolationSequence( String str ) {
        List<SassListItem> sequence = stringSequence;
        if( sequence != null ) {
            stringSequence = null;
            if( !str.isEmpty() ) {
                sequence.add( new StringItem( str ) );
            }
            return new StringInterpolationSequence( sequence );
        }
        return new StringInterpolationSequence( str );
    }

    /**
     * Concatenate the sequence and the builder to one StringInterpolationSequence,
     * @param builder possible empty builder
     * @return the StringInterpolationSequence, never null
     */
    private StringInterpolationSequence createStringInterpolationSequence( StringBuilder builder ) {
        return createStringInterpolationSequence( str( builder ) );
    }

    /**
     * Parse a interpolation like #{xxxx}. The '#' character must already consumed from the caller.
     * <p>
     * Set the flag <code>wasInterpolation</code>. If there is a interpolation. If not then the caller must handle the '#' character self.
     * @param sequence a possible previous parsed sequence, can be null
     * @param builder before consumed characters. Must add to the sequence first. Is empty if flag <code>wasInterpolation</code> is true.
     * @return the list, create if needed
     */
    private boolean parseInterpolation( StringBuilder builder ) {
        char ch = reader.read();
        if( ch != '{' ) {
            reader.back( ch );
            return false;
        }

        List<SassListItem> sequence = stringSequence;
        if( sequence == null ) {
            sequence = new ArrayList<>();
        }
        stringSequence = null; // for calling parseExpression()
        if( builder.length() > 0 ) {
            sequence.add( new StringItem( str( builder ) ) );
        }

        int line = reader.getLine();
        int column = reader.getColumn();
        SassListItem item = parseExpression( true );
        sequence.add( new Interpolation( item, line, column ) );
        consumeMarker( '}' );
        stringSequence = sequence;
        return true;
    }

    /**
     * Read a argument list
     * @return the list
     */
    @SuppressWarnings( "unchecked" )
    private <T> T argValuelist( boolean formal ) {
        ArrayList<Variable> args = new ArrayList<>();
        int varArgumentPos = -1;
        String name = null;
        SassListItem exp = null;
        boolean wasSpace = false;

        LOOP: for( ;; ) {
            char ch = reader.read();

            if( isWhitespace( ch ) ) {
                wasSpace = true;
                continue LOOP;
            }

            EXP: {
                switch( ch ) {
                    case '$':
                        if( name != null ) {
                            break;
                        }
                        name = parseName( false );
                        wasSpace = false;
                        continue LOOP;
                    case ')':
                        if( name == null ) {
                            break LOOP;
                        }
                        if( !formal ) {
                            exp = LexicalUnitImpl.createVariable( uri, reader.getLine(), reader.getColumn(), name );
                            name = null;
                        }
                        args.add( new Variable( name, exp ) );
                        break LOOP;
                    case ',':
                        if( name == null ) {
                            continue LOOP;
                        }
                        if( !formal ) {
                            exp = LexicalUnitImpl.createVariable( uri, reader.getLine(), reader.getColumn(), name );
                            name = null;
                        }
                        break EXP;
                    case ':':
                        exp = parseExpressionOrList( parseExpression( true ), true );
                        break EXP;
                    case '.':
                        if( varArgumentPos < 0 ) {
                            char ch2, ch3;
                            if( '.' == (ch2 = reader.read()) ) {
                                if( '.' == (ch3 = reader.read()) ) {
                                    varArgumentPos = args.size();
                                    continue LOOP;
                                } else {
                                    reader.back( ch3 );
                                }
                            } else {
                                reader.back( ch2 );
                            }
                        }
                        break;
                }
                reader.back( ch );
                if( name != null ) {
                    if( wasSpace ) {
                        reader.back( ' ' );
                    }
                    // seems an expression and not a parameter name
                    reader.back( name );
                    reader.back( '$' );
                    name = null;
                }
                exp = parseExpressionOrList( parseExpression( true ), true );
            }
            args.add( new Variable( name, exp ) );
            name = null;
            wasSpace = false;
        }

        boolean hasVariableArguments;
        if( varArgumentPos < 0 ) {
            hasVariableArguments = false;
        } else {
            hasVariableArguments = true;
            if( varArgumentPos != args.size() - 1 ) {
                reader.createException( "Varargs must on last parameter" );
            }
        }

        return (T)(formal ? //
            new FormalArgumentList( args, hasVariableArguments ) : //
            new ActualArgumentList( Separator.COMMA, args, hasVariableArguments ) //
        );
    }

    /**
     * Parse comments
     * @param container optional container for the parsed comments
     * @return return true, if a comment was parsed, false if the slash must be parse anywhere else
     */
    private boolean comment() {
        char ch = reader.read();
        switch( ch ) {
            case '*': // block comments
                StringBuilder builder = new StringBuilder();
                builder.append( "/*" );
                boolean wasAsterix = false;
                for( ;; ) {
                    ch = reader.read();
                    builder.append( ch );
                    if( ch == '/' && wasAsterix ) {
                        String comment = trim( builder );
                        if( comment.startsWith( "/**" ) ) {
                            documentHandler.comment( comment );
                        }
                        return true;
                    }
                    wasAsterix = ch == '*';
                }
            default:
                reader.back( ch );
                return false;
        }
    }

    /**
     * Get a trim string from the builder and clear the builder.
     * @param builder the builder.
     * @return a trim string
     */
    private static String trim( StringBuilder builder ) {
        return str( builder ).trim();
    }

    /**
     * Get a string from the builder with spaces and clear the builder.
     * @param builder the builder.
     * @return a trim string
     */
    private static String str( StringBuilder builder ) {
        String str = builder.toString();
        builder.setLength( 0 );
        return str;
    }

    /**
     * Consume and check for a end marker. Throw an exception if there is not the expected marker.
     * @param marker1 accepted marker
     * @param marker2 accepted marker
     */
    private void consumeMarker( char marker ) {
        char ch = readNonWhitespace();
        if( ch == marker ) {
            return;
        }
        throw reader.createException( "Unrecognized input: '" + ch + "' expected: '" + marker + "'" );
    }

    /**
     * Consume and check for a end marker. Throw an exception if there is not the expected marker.
     * @param marker1 accepted marker
     * @param marker2 accepted marker
     */
    private char consumeMarkers( char marker1, char marker2 ) {
        char ch = readNonWhitespace();
        if( ch == marker1 || ch == marker2 ) {
            return ch;
        }
        throw reader.createException( "Unrecognized input: '" + ch + "' expected: '" + marker1 + "' or '" + marker2 + "'" );
    }

    /**
     * Skip all whitespace and read the next character which is not a whitespace.
     * @return the read character
     */
    private char readNonWhitespace() {
        for( ;; ) {
            char ch = reader.read();
            if( isWhitespace( ch ) ) {
                continue;
            }
            if( ch == '/' && comment() ) {
                continue;
            }
            return ch;
        }
    }

    /**
     * Fast check if a character is a whitespace
     * @param ch the character
     * @return true, if whitespace
     */
    private static boolean isWhitespace( char ch ) {
        if( ch <= ' ' ) {
            return true;
        }
        switch( ch ) {
            case '\u00A0':
                return true;
        }
        return false;
    }
}
