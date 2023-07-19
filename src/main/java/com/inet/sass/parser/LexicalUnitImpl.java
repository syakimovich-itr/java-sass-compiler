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
/*
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */
package com.inet.sass.parser;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.inet.sass.ScssContext;
import com.inet.sass.function.SCSSFunctionGenerator;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.tree.BlockNode;
import com.inet.sass.tree.FunctionCall;
import com.inet.sass.tree.FunctionDefNode;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.Node.BuildStringStrategy;
import com.inet.sass.util.ColorUtil;
import com.inet.sass.util.StringUtil;

/**
 * @author Philippe Le Hegaret
 * 
 * @modified Sebastian Nyholm @ Vaadin Ltd
 */
public class LexicalUnitImpl implements SCSSLexicalUnit, SassListItem {

    public static final long PRECISION = 100000L;

    private static final ThreadLocal<DecimalFormat> CSS_FLOAT_FORMAT = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("0.#####", DecimalFormatSymbols.getInstance(Locale.US) );
        }
    };

    public static final LexicalUnitImpl WHITESPACE = new LexicalUnitImpl( null, 0, 0, SAC_IDENT, " " );

    private short type;
    private int line;
    private int column;

    private double f;
    private String sdimension;
    private StringInterpolationSequence s;
    private String fname;
    private ActualArgumentList params;
    private String uri;

    private String printState;
    private boolean varNotResolved;

    LexicalUnitImpl( String uri, int line, int column, short type ) {
        this.uri = uri;
        this.line = line;
        this.column = column;
        this.type = type;
    }

    LexicalUnitImpl( String uri, int line, int column, short type, double f ) {
        this( uri, line, column, type );
        this.f = f;
    }

    LexicalUnitImpl( String uri, int line, int column, short type, String sdimension, double f ) {
        this( uri, line, column, type, f );
        this.sdimension = sdimension;
    }

    LexicalUnitImpl( String uri, int line, int column, short type, String s ) {
        this( uri, line, column, type, new StringInterpolationSequence( s ) );
    }

    LexicalUnitImpl( String uri, int line, int column, short type, StringInterpolationSequence s ) {
        this( uri, line, column, type );
        this.s = s;
    }

    LexicalUnitImpl( String uri, short type, int line, int column, String fname, ActualArgumentList params ) {
        this( uri, line, column, type );
        this.fname = fname;
        this.params = params;
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     */
    public int getLineNumber() {
        return line;
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnNumber() {
        return column;
    }

    public short getItemType() {
        return type;
    }

    private void setLexicalUnitType(short type) {
        this.type = type;
    }

    public boolean isUnitlessNumber() {
        switch (type) {
        case LexicalUnitImpl.SAC_INTEGER:
        case LexicalUnitImpl.SAC_REAL:
            return true;
        default:
            return false;
        }
    }

    public boolean isNumber() {
        short type = getItemType();
        switch (type) {
        case SAC_INTEGER:
        case SAC_REAL:
        case SAC_EM:
        case SAC_LEM:
        case SAC_REM:
        case SAC_EX:
        case SAC_PIXEL:
        case SAC_INCH:
        case SAC_CENTIMETER:
        case SAC_MILLIMETER:
        case SAC_POINT:
        case SAC_PICA:
        case SAC_PERCENTAGE:
        case SAC_DEGREE:
        case SAC_GRADIAN:
        case SAC_RADIAN:
        case SAC_MILLISECOND:
        case SAC_SECOND:
        case SAC_HERTZ:
        case SAC_KILOHERTZ:
        case SAC_DIMENSION:
            return true;
        default:
            return false;
        }
    }

    public int getIntegerValue() {
        return (int)f;
    }

    public double getDoubleValue() {
        return f;
    }

    /**
     * Returns the double value as a string unless the value is an integer. In
     * that case returns the integer value as a string.
     * 
     * @return a string representing the value, either with or without decimals
     */
    public String getDoubleOrInteger() {
        return CSS_FLOAT_FORMAT.get().format( f );
    }

    private void setDoubleValue( double f ) {
        this.f = f;
    }

    public String getDimensionUnitText() {
        switch (type) {
        case SAC_INTEGER:
        case SAC_REAL:
            return "";
        case SAC_PERCENTAGE:
            return "%";
        case SAC_EM:
            return "em";
        case SAC_LEM:
            return "lem";
        case SAC_REM:
            return "rem";
        case SAC_EX:
            return "ex";
        case SAC_PIXEL:
            return "px";
        case SAC_CENTIMETER:
            return "cm";
        case SAC_MILLIMETER:
            return "mm";
        case SAC_INCH:
            return "in";
        case SAC_POINT:
            return "pt";
        case SAC_PICA:
            return "pc";
        case SAC_DEGREE:
            return "deg";
        case SAC_RADIAN:
            return "rad";
        case SAC_GRADIAN:
            return "grad";
        case SAC_MILLISECOND:
            return "ms";
        case SAC_SECOND:
            return "s";
        case SAC_HERTZ:
            return "Hz";
        case SAC_KILOHERTZ:
            return "kHz";
        case SAC_DIMENSION:
            return sdimension;
        default:
            throw new IllegalStateException("invalid dimension " + type);
        }
    }

    public String getStringValue() {
        return s == null ? null : s.toString();
    }

    private void setStringValue(String str) {
        s = new StringInterpolationSequence(str);
    }

    public String getFunctionName() {
        return fname;
    }

    public ActualArgumentList getParameterList() {
        return params;
    }

    /**
     * Get the URI, where a function is define
     * @return the uri
     */
    @Override
    public String getUri() {
        return uri;
    }

    /**
     * Prints out the current state of the node tree. Will return SCSS before
     * compile and CSS after.
     * 
     * Result value could be null.
     * 
     * @return State as a string
     */
    public String printState() {
        if (printState == null) {
            if( varNotResolved ) {
                // throw this exception only if there was already a failing try to resolve this variable
                throw new ParseException( "Variable was not resolved: " + simpleAsString(), uri, line, column );
            }
            printState = buildString(Node.PRINT_STRATEGY);
        }
        return printState;
    }

    @Override
    public String toString() {
        String result = simpleAsString();
        if (result == null) {
            return "Lexical unit node [" + buildString(Node.TO_STRING_STRATEGY)
                    + "]";
        } else {
            return result;
        }
    }

    // A helper method for sass interpolation
    @Override
    public String unquotedString() {
        String result = printState();
        if (result.length() >= 2
                && ((result.charAt(0) == '"' && result
                        .charAt(result.length() - 1) == '"') || (result
                        .charAt(0) == '\'' && result
                        .charAt(result.length() - 1) == '\''))) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    public LexicalUnitImpl divide( LexicalUnitImpl denominator ) {
        if( denominator.type != SAC_INTEGER && denominator.type != SAC_REAL && type != denominator.type ) {
            // then this is not a numeric division
            return new LexicalUnitImpl( uri, line, column, SAC_IDENT, printState()+'/'+denominator.printState() );
        }
        LexicalUnitImpl copy = copyWithValue( getDoubleValue() / denominator.getDoubleValue() );
        if( type == denominator.type ) {
            copy.setLexicalUnitType( SAC_REAL );
        }
        return copy;
    }

    public LexicalUnitImpl add(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getDoubleValue()
                + another.getDoubleValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
    }

    public LexicalUnitImpl minus(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getDoubleValue()
                - another.getDoubleValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
    }

    public LexicalUnitImpl multiply(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getDoubleValue()
                * another.getDoubleValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
    }

    private ParseException createIncompatibleUnitsException( LexicalUnitImpl another ) {
        String msg = "Incompatible units found in: '" + printState() + "' <> '" + another.printState() + "'\n" //
            + new ParseException( "", this ).getMessage() + '\n';
        return new ParseException( msg, another );
    }

    public short checkAndGetUnit( LexicalUnitImpl another ) {
        short thisType = this.type;
        short otherType = another.type;
        if( thisType == otherType ) {
            return thisType;
        }
        switch( otherType ) {
            case SAC_INTEGER:
            case SAC_REAL:
                return thisType == SAC_INTEGER ? otherType : thisType ;

            case SAC_CENTIMETER:
            case SAC_MILLIMETER:
            case SAC_INCH:
                switch( thisType ) {
                    case SAC_CENTIMETER:
                    case SAC_MILLIMETER:
                    case SAC_INCH:
                        return thisType;
                }
        }
        switch( thisType ) {
            case SAC_INTEGER:
            case SAC_REAL:
                return otherType;
        }
        throw createIncompatibleUnitsException( another );
    }

    public LexicalUnitImpl modulo(LexicalUnitImpl another) {
        if( !checkLexicalUnitType( another, type, SAC_INTEGER, SAC_REAL ) ) {
            throw createIncompatibleUnitsException( another );
        }
        return copyWithValue( (int)getIntegerValue() % (int)another.getIntegerValue() );
    }

    /**
     * Returns a shallow copy of the {@link LexicalUnitImpl} with null as next
     * lexical unit pointer. Parameters are not copied but a reference to the
     * same parameter list is used.
     * 
     * @return copy of this without next
     */
    public LexicalUnitImpl copy() {
        LexicalUnitImpl copy = new LexicalUnitImpl( uri, line, column, type );
        copy.f = f;
        copy.s = s;
        copy.fname = fname;
        copy.sdimension = sdimension;
        copy.params = params;
        return copy;
    }

    public LexicalUnitImpl copyWithValue( double value ) {
        LexicalUnitImpl result = copy();
        result.setDoubleValue( value );
        return result;
    }

    public String getSdimension() {
        return sdimension;
    }

    // here some useful function for creation
    public static LexicalUnitImpl createVariable( String uri, int line, int column, String name ) {
        return new LexicalUnitImpl( uri, line, column, SCSS_VARIABLE, name );
    }

    public static LexicalUnitImpl createNull( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SCSS_NULL, "null" );
    }

    public static LexicalUnitImpl createNumber( String uri, int line, int column, double v ) {
        int i = (int)v;
        if( v == i ) {
            return new LexicalUnitImpl( uri, line, column, SAC_INTEGER, i );
        } else {
            return new LexicalUnitImpl( uri, line, column, SAC_REAL, v );
        }
    }

    public static LexicalUnitImpl createInteger( String uri, int line, int column, int i ) {
        return new LexicalUnitImpl( uri, line, column, SAC_INTEGER, i );
    }

    public static LexicalUnitImpl createPercentage( String uri, int line, int column, double v ) {
        if( Math.round( v * 100 * PRECISION ) == (((int)v) * 100 * PRECISION) ) {
            v = (int)v;
        }
        return new LexicalUnitImpl( uri, line, column, SAC_PERCENTAGE, v );
    }

    static LexicalUnitImpl createEMS( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_EM, v );
    }

    static LexicalUnitImpl createLEM( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SCSSLexicalUnit.SAC_LEM, v );
    }

    static LexicalUnitImpl createREM( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SCSSLexicalUnit.SAC_REM, v );
    }

    static LexicalUnitImpl createEXS( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_EX, v );
    }

    public static LexicalUnitImpl createPX( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_PIXEL, v );
    }

    public static LexicalUnitImpl createCM( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_CENTIMETER, v );
    }

    static LexicalUnitImpl createMM( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_MILLIMETER, v );
    }

    static LexicalUnitImpl createIN( String uri,int line, int column, double v) {
        return new LexicalUnitImpl(uri, line, column, SAC_INCH, v);
    }

    static LexicalUnitImpl createPT( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_POINT, v );
    }

    static LexicalUnitImpl createPC( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_PICA, v );
    }

    public static LexicalUnitImpl createDEG( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_DEGREE, v );
    }

    static LexicalUnitImpl createRAD( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_RADIAN, v );
    }

    static LexicalUnitImpl createGRAD( String uri, int line, int column, double v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_GRADIAN, v );
    }

    static LexicalUnitImpl createMS( String uri, int line, int column, double v ) {
        if( v < 0 ) {
            throw new ParseException( "Time values may not be negative", uri, line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_MILLISECOND, v );
    }

    static LexicalUnitImpl createS( String uri, int line, int column, double v ) {
        if( v < 0 ) {
            throw new ParseException( "Time values may not be negative", uri, line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_SECOND, v );
    }

    static LexicalUnitImpl createHZ( String uri, int line, int column, double v ) {
        if( v < 0 ) {
            throw new ParseException( "Frequency values may not be negative", uri, line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_HERTZ, v );
    }

    static LexicalUnitImpl createKHZ( String uri, int line, int column, double v ) {
        if( v < 0 ) {
            throw new ParseException( "Frequency values may not be negative", uri, line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_KILOHERTZ, v );
    }

    static LexicalUnitImpl createDimen( String uri, int line, int column, double v, String s ) {
        return new LexicalUnitImpl( uri, line, column, SAC_DIMENSION, s, v );
    }

    static LexicalUnitImpl createInherit( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_INHERIT, "inherit" );
    }

    public static LexicalUnitImpl createRawIdent( String uri, int line, int column, String s ) {
        return new LexicalUnitImpl( uri, line, column, SAC_IDENT, s );
    }

    public static LexicalUnitImpl createIdent( String uri, int line, int column, String s ) {
        return createIdent( uri, line, column, new StringInterpolationSequence( s ) );
    }

    public static LexicalUnitImpl createIdent( String uri, int line, int column, StringInterpolationSequence s ) {
        if( "null".equals( s.toString() ) ) {
            return createNull( uri, line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_IDENT, s );
    }

    public static LexicalUnitImpl createString( String uri, int line, int column, String s ) {
        return createString( uri, line, column, new StringInterpolationSequence( s ) );
    }

    public static LexicalUnitImpl createString( String uri, int line, int column, StringInterpolationSequence s ) {
        return new LexicalUnitImpl( uri, line, column, SAC_STRING_VALUE, s );
    }

    public static LexicalUnitImpl createURL( String uri, int line, int column, StringInterpolationSequence s ) {
        return new LexicalUnitImpl( uri, line, column, SAC_URI, s );
    }

    public static LexicalUnitImpl createRGBColor( String uri, int line, int column, ActualArgumentList params ) {
        return new LexicalUnitImpl( uri, SAC_RGBCOLOR, line, column, "rgb", params );
    }

    public static LexicalUnitImpl createRect( String uri, int line, int column, ActualArgumentList params ) {
        return new LexicalUnitImpl( uri, SAC_RECT_FUNCTION, line, column, "rect", params );
    }

    public static LexicalUnitImpl createFunction(  String uri, int line, int column, String fname, ActualArgumentList params ) {
        return new LexicalUnitImpl( uri, SAC_FUNCTION, line, column, fname, params );
    }

    public static LexicalUnitImpl createGetFunction(  String uri, int line, int column, String fname ) {
        return new LexicalUnitImpl( uri, SCSS_GET_FUNCTION, line, column, fname, null );
    }

    public static LexicalUnitImpl createParent(  String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SCSS_PARENT );
    }

    public static boolean checkLexicalUnitType(SassListItem item,
            short... lexicalUnitTypes) {
        if (!(item instanceof LexicalUnitImpl)) {
            return false;
        }
        short itemType = ((LexicalUnitImpl)item).type;
        for (short s : lexicalUnitTypes) {
            if (itemType == s) {
                return true;
            }
        }
        return false;
    }

    public static LexicalUnitImpl createComma( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_COMMA );
    }

    public static LexicalUnitImpl createSlash( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_SLASH );
    }

    public static LexicalUnitImpl createAdd( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_PLUS );
    }

    public static LexicalUnitImpl createMinus( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_MINUS );
    }

    public static LexicalUnitImpl createMultiply( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_MULTIPLY );
    }

    public static LexicalUnitImpl createModulo( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_MOD );
    }

    public static LexicalUnitImpl createIdent( String s ) {
        return new LexicalUnitImpl( null, 0, 0, SAC_IDENT, s );
    }

    public static LexicalUnitImpl createEquals( String uri, int line, int column) {
        return new LexicalUnitImpl(uri,line, column, SCSS_OPERATOR_EQUALS);
    }

    public static LexicalUnitImpl createNotEqual( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SCSS_OPERATOR_NOT_EQUAL );
    }

    public static LexicalUnitImpl createGreaterThan( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_GT );
    }

    public static LexicalUnitImpl createGreaterThanOrEqualTo( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_GE );
    }

    public static LexicalUnitImpl createLessThan( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_LT );
    }

    public static LexicalUnitImpl createLessThanOrEqualTo( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_LE );
    }

    public static LexicalUnitImpl createAnd( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SCSS_OPERATOR_AND );
    }

    public static LexicalUnitImpl createOr( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SCSS_OPERATOR_OR );
    }

    @Override
    public SassListItem replaceVariables( ScssContext context ) {
        // replace function parameters (if any)
        LexicalUnitImpl lui;
        ActualArgumentList params = this.params;
        if( params != null && (params != (params = params.replaceVariables( context ))) ) {
            lui = copy();
            lui.params = params;
        } else {
            lui = this;
        }

        // replace parameters in string value
        switch( type ) {
            case SCSS_VARIABLE:
                String stringValue = lui.getStringValue();
                Variable var = context.getVariable( stringValue );
                if( var != null ) {
                    return var.getExpr().replaceVariables( context );
                }
                varNotResolved = true;
                break;
            case SCSS_PARENT:
                BlockNode parentBlock = context.getParentBlock();
                return parentBlock != null ? new StringItem( parentBlock.getSelectors() ) : createNull( uri, line, column );
            default:
                StringInterpolationSequence s = this.s;
                if( s != null && s.containsInterpolation() ) {
                    StringInterpolationSequence sis = s.replaceVariables( context );
                    if( sis != s ) {
                        LexicalUnitImpl copy = lui.copy();
                        copy.s = sis;
                        return copy;
                    }
                }
        }
        return lui;
    }

    public boolean containsInterpolation() {
        return s != null && s.containsInterpolation();
    }

    @Override
    public SassListItem evaluateFunctionsAndExpressions( ScssContext context, boolean evaluateArithmetics ) {
        String functionName = getFunctionName();
        if( params != null && !"calc".equals( functionName ) ) {
            SCSSFunctionGenerator generator = SCSSFunctionGenerator.getGenerator( functionName );
            LexicalUnitImpl copy = this;
            if( !"if".equals( functionName ) ) {
                copy = createFunction( uri, line, column, fname, params.evaluateFunctionsAndExpressions( context, true ) );
            }
            if( generator == null ) {
                SassListItem result = copy.replaceCustomFunctions( context );
                if( result != null ) {
                    return result;
                }
            }
            if( generator == null ) {
                // log unknown functions
                switch( functionName.toLowerCase() ) {
                    case "brightness":
                    case "counters":
                    case "hsl":
                    case "hsla":
                    case "linear-gradient":
                    case "not ":
                    case "rgba":
                    case "rotate":
                    case "scale":
                    case "translate":
                    case "translatey":
                    case "translatex":
                    case "translatez":
                    case "url":
                    case "var":
                        // ignore well known CSS functions
                        break;
                    default:
                        SCSSErrorHandler.get().warning( "Unknown function: " + functionName );
                }
                return copy;
            }
            return generator.compute( context, copy );
        } else {
            return this;
        }
    }

    private SassListItem replaceCustomFunctions(ScssContext context) {
        FunctionDefNode functionDef = context
                .getFunctionDefinition(getFunctionName());
        if (functionDef != null) {
            return FunctionCall.evaluate(context, functionDef, this);
        }
        return null;
    }

    private String simpleAsString() {
        String text = null;
        switch (type) {
        case SCSS_VARIABLE:
            text = "$" + s;
            break;
        case SCSS_NULL:
            text = "";
            break;
        case SAC_OPERATOR_COMMA:
            text = ",";
            break;
        case SAC_OPERATOR_PLUS:
            text = "+";
            break;
        case SAC_OPERATOR_MINUS:
            text = "-";
            break;
        case SAC_OPERATOR_MULTIPLY:
            text = "*";
            break;
        case SAC_OPERATOR_SLASH:
            text = "/";
            break;
        case SAC_OPERATOR_MOD:
            text = "%";
            break;
        case SAC_OPERATOR_EXP:
            text = "^";
            break;
        case SCSS_OPERATOR_EQUALS:
            text = "==";
            break;
        case SCSS_OPERATOR_NOT_EQUAL:
            text = "!=";
            break;
        case SCSS_OPERATOR_NOT:
            text = "not";
            break;
        case SCSS_OPERATOR_AND:
            text = "and";
            break;
        case SCSS_OPERATOR_OR:
            text = "or";
            break;
        case SAC_OPERATOR_LT:
            text = "<";
            break;
        case SAC_OPERATOR_GT:
            text = ">";
            break;
        case SAC_OPERATOR_LE:
            text = "<=";
            break;
        case SAC_OPERATOR_GE:
            text = "=>";
            break;
        case SAC_OPERATOR_TILDE:
            text = "~";
            break;
        case SAC_INHERIT:
            text = "inherit";
            break;
        case SAC_INTEGER:
        case SAC_REAL:
            text = getDoubleOrInteger();
            break;
        case SAC_EM:
        case SAC_LEM:
        case SAC_REM:
        case SAC_EX:
        case SAC_PIXEL:
        case SAC_INCH:
        case SAC_CENTIMETER:
        case SAC_MILLIMETER:
        case SAC_POINT:
        case SAC_PICA:
        case SAC_PERCENTAGE:
        case SAC_DEGREE:
        case SAC_GRADIAN:
        case SAC_RADIAN:
        case SAC_MILLISECOND:
        case SAC_SECOND:
        case SAC_HERTZ:
        case SAC_KILOHERTZ:
        case SAC_DIMENSION:
            text = getDoubleOrInteger() + getDimensionUnitText();
            break;
        }
        return text;
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        String text = simpleAsString();
        if (text == null) {
        switch (type) {
            case SAC_URI:
                text = "url(" + getStringValue() + ")";
                break;
            case SAC_RGBCOLOR:
                int[] rgb = getRgb();
                if (rgb != null) {
                    text = ColorUtil.rgbToColorString(rgb);
                    break;
                }
                //$FALL-THROUGH$ else fall through to the function branch
            case SAC_COUNTER_FUNCTION:
            case SAC_COUNTERS_FUNCTION:
            case SAC_RECT_FUNCTION:
            case SAC_FUNCTION:
                if( ColorUtil.isColor( this ) ) {
                    text = ColorUtil.rgbToColorString( ColorUtil.colorToRgb( this ) );
                    break;
                } else if( ColorUtil.isRgba( this ) || ColorUtil.isHsla( this ) ) {
                    double alpha = params.get( params.size() - 1 ).getContainedValue().getDoubleValue();
                    rgb = ColorUtil.colorToRgb( this );
                    if( rgb != null ) {
                        if( alpha == 0.0f && rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0 ) {
                            text = "transparent";
                            break;
                        } else if( alpha == 1.0f ) {
                            text = ColorUtil.rgbToColorString( rgb );
                            break;
                        } else if( params.size() == 2 || ColorUtil.isHsla( this ) ) {
                            String alphaText = alpha == 0.0f ? "0" : CSS_FLOAT_FORMAT.get().format( alpha );
                            text = "rgba(" + rgb[0] + ", " + rgb[1] + ", " + rgb[2] + ", " + alphaText + ")";
                            break;
                        }
                    }
                }
                text = fname + "(" + params.buildString( strategy ) + ")";
                break;
            case SCSS_GET_FUNCTION:
                text = "get-function(" + fname + ")";
                break;
            case SAC_IDENT:
                text = getStringValue();
                break;
            case SAC_STRING_VALUE:
                // @@SEEME. not exact
                text = "\"" + getStringValue() + "\"";
                break;
            case SAC_SUB_EXPRESSION:
                text = strategy.build(getParameterList());
                break;
            case SCSS_PARENT:
                text = "&";
                break;
            default:
                text = "@unknown";
                break;
            }
        }
        return text;
    }

    private int[] getRgb() {
        if (params.size() != 3
                || !checkLexicalUnitType(params.get(0), SAC_INTEGER)
                || !checkLexicalUnitType(params.get(1), SAC_INTEGER)
                || !checkLexicalUnitType(params.get(2), SAC_INTEGER)) {
            return null;
        }
        int red = ((LexicalUnitImpl) params.get(0)).getIntegerValue();
        int green = ((LexicalUnitImpl) params.get(1)).getIntegerValue();
        int blue = ((LexicalUnitImpl) params.get(2)).getIntegerValue();
        return new int[] { red, green, blue };
    }

    @Override
    public boolean containsArithmeticalOperator() {
        return false;
    }

    @Override
    public LexicalUnitImpl updateUrl(String prefix) {
        if (getItemType() == SAC_URI) {
            LexicalUnitImpl copy = copy();
            if( s.containsInterpolation() ) {
                List<SassListItem> items = new ArrayList<>();
                items.add( new StringItem( prefix ) );
                items.addAll( s.getItems() );
                copy.s = new StringInterpolationSequence( items );
            } else {
                String path = getStringValue().replaceAll("^\"|\"$", "")
                        .replaceAll("^'|'$", "");
                if (!path.startsWith("/") && !path.contains(":")) {
                    path = prefix + path;
                    path = StringUtil.cleanPath(path);
                }
                copy.setStringValue(path);
            }
            return copy;
        } else if (containsInterpolation()) {
            // s might contain URLs in its Interpolation objects
            LexicalUnitImpl copy = copy();
            copy.s = s.updateUrl(prefix);
            return copy;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LexicalUnitImpl)) {
            return false;
        }
        LexicalUnitImpl other = (LexicalUnitImpl) o;
        if (isNumber() && other.isNumber()) {
            if (!isUnitlessNumber() && !other.isUnitlessNumber()) {
                if (getItemType() != other.getItemType()) {
                    return false;
                }
            }
            return getDoubleValue() == other.getDoubleValue()
                    && getIntegerValue() == other.getIntegerValue();
        } else if (getItemType() != other.getItemType()) {
            return false;
        } else {
            return printState().equals(other.printState());
        }
    }

    @Override
    public int hashCode() {
        return printState().hashCode();
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        return this;
    }
}
