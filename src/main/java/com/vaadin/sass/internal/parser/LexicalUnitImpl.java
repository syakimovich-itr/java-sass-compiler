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
 *
 * $Id: LexicalUnitImpl.java,v 1.3 2000/02/15 02:08:19 plehegar Exp $
 */
package com.vaadin.sass.internal.parser;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.expression.exception.IncompatibleUnitsException;
import com.vaadin.sass.internal.parser.function.AbsFunctionGenerator;
import com.vaadin.sass.internal.parser.function.AdjustColorFunctionGenerator;
import com.vaadin.sass.internal.parser.function.AlphaFunctionGenerator;
import com.vaadin.sass.internal.parser.function.CeilFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ColorComponentFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ComparableFunctionGenerator;
import com.vaadin.sass.internal.parser.function.DarkenFunctionGenerator;
import com.vaadin.sass.internal.parser.function.DefaultFunctionGenerator;
import com.vaadin.sass.internal.parser.function.FloorFunctionGenerator;
import com.vaadin.sass.internal.parser.function.GrayscaleFunctionGenerator;
import com.vaadin.sass.internal.parser.function.IfFunctionGenerator;
import com.vaadin.sass.internal.parser.function.LightenFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListAppendFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListIndexFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListJoinFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListLengthFunctionGenerator;
import com.vaadin.sass.internal.parser.function.ListNthFunctionGenerator;
import com.vaadin.sass.internal.parser.function.MapMergeFunctionGenerator;
import com.vaadin.sass.internal.parser.function.MinMaxFunctionGenerator;
import com.vaadin.sass.internal.parser.function.MixFunctionGenerator;
import com.vaadin.sass.internal.parser.function.PercentageFunctionGenerator;
import com.vaadin.sass.internal.parser.function.QuoteUnquoteFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RGBFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RectFunctionGenerator;
import com.vaadin.sass.internal.parser.function.RoundFunctionGenerator;
import com.vaadin.sass.internal.parser.function.SCSSFunctionGenerator;
import com.vaadin.sass.internal.parser.function.SaturationModificationFunctionGenerator;
import com.vaadin.sass.internal.parser.function.TransparencyModificationFunctionGenerator;
import com.vaadin.sass.internal.parser.function.TypeOfFunctionGenerator;
import com.vaadin.sass.internal.parser.function.UnitFunctionGenerator;
import com.vaadin.sass.internal.parser.function.UnitlessFunctionGenerator;
import com.vaadin.sass.internal.tree.FunctionCall;
import com.vaadin.sass.internal.tree.FunctionDefNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;
import com.vaadin.sass.internal.util.ColorUtil;
import com.vaadin.sass.internal.util.StringUtil;

/**
 * @version $Revision: 1.3 $
 * @author Philippe Le Hegaret
 * 
 * @modified Sebastian Nyholm @ Vaadin Ltd
 */
public class LexicalUnitImpl implements SCSSLexicalUnit,
        SassListItem, Serializable {
    private static final long serialVersionUID = -6649833716809789399L;

    public static final long PRECISION = 100000L;

    private static final DecimalFormat CSS_FLOAT_FORMAT = new DecimalFormat(
            "0.0####");

    private short type;
    private int line;
    private int column;

    private int i;
    private float f;
    private String sdimension;
    private StringInterpolationSequence s;
    private String fname;
    private ActualArgumentList params;
    private String uri;

    private String printState;

    LexicalUnitImpl( String uri, int line, int column, short type ) {
        this.uri = uri;
        this.line = line;
        this.column = column;
        this.type = type;
    }

    LexicalUnitImpl( String uri, int line, int column, short type, float f ) {
        this( uri, line, column, type );
        this.f = f;
        i = (int) f;
    }

    LexicalUnitImpl( String uri, int line, int column, short type, int i ) {
        this( uri, line, column, type );
        this.i = i;
        f = i;
    }

    LexicalUnitImpl( String uri, int line, int column, short type, String sdimension, float f ) {
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

    public int getLineNumber() {
        return line;
    }

    public int getColumnNumber() {
        return column;
    }

    public short getLexicalUnitType() {
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
        short type = getLexicalUnitType();
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
        return i;
    }

    private void setIntegerValue(int i) {
        this.i = i;
        f = i;
    }

    public float getFloatValue() {
        return f;
    }

    /**
     * Returns the float value as a string unless the value is an integer. In
     * that case returns the integer value as a string.
     * 
     * @return a string representing the value, either with or without decimals
     */
    public String getFloatOrInteger() {
        float f = getFloatValue();
        int i = (int) f;
        if (i == f) {
            return Integer.toString(i);
        } else {
            return CSS_FLOAT_FORMAT.format(f);
        }
    }

    private void setFloatValue(float f) {
        this.f = f;
        i = (int) f;
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

    public LexicalUnitImpl divide(LexicalUnitImpl denominator) {
        if (denominator.getLexicalUnitType() != SAC_INTEGER
                && denominator.getLexicalUnitType() != SAC_REAL
                && getLexicalUnitType() != denominator.getLexicalUnitType()) {
            throw new IncompatibleUnitsException(printState());
        }
        LexicalUnitImpl copy = copyWithValue(getFloatValue()
                / denominator.getFloatValue());
        if (getLexicalUnitType() == denominator.getLexicalUnitType()) {
            copy.setLexicalUnitType(SAC_REAL);
        }
        return copy;
    }

    public LexicalUnitImpl add(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getFloatValue()
                + another.getFloatValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
    }

    public LexicalUnitImpl minus(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getFloatValue()
                - another.getFloatValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
    }

    public LexicalUnitImpl multiply(LexicalUnitImpl another) {
        LexicalUnitImpl copy = copyWithValue(getFloatValue()
                * another.getFloatValue());
        copy.setLexicalUnitType(checkAndGetUnit(another));
        return copy;
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
                return thisType;

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
        throw new IncompatibleUnitsException( printState() + " <> " + another.printState() );
    }

    public LexicalUnitImpl modulo(LexicalUnitImpl another) {
        if (!checkLexicalUnitType(another, getLexicalUnitType(), SAC_INTEGER,
                SAC_REAL)) {
            throw new IncompatibleUnitsException(printState());
        }
        LexicalUnitImpl copy = copy();
        copy.setIntegerValue(getIntegerValue() % another.getIntegerValue());
        return copy;
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
        copy.i = i;
        copy.f = f;
        copy.s = s;
        copy.fname = fname;
        copy.sdimension = sdimension;
        copy.params = params;
        return copy;
    }

    public LexicalUnitImpl copyWithValue(float value) {
        LexicalUnitImpl result = copy();
        result.setFloatValue(value);
        return result;
    }

    private void setParameterList(ActualArgumentList params) {
        this.params = params;
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

    public static LexicalUnitImpl createNumber( String uri, int line, int column, float v ) {
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

    public static LexicalUnitImpl createPercentage( String uri, int line, int column, float v ) {
        LexicalUnitImpl result = new LexicalUnitImpl( uri, line, column, SAC_PERCENTAGE, v );

        if( Math.round( v * 100 * PRECISION ) == (((int)v) * 100 * PRECISION) ) {
            result.setIntegerValue( (int)v );
        }

        return result;
    }

    static LexicalUnitImpl createEMS( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_EM, v );
    }

    static LexicalUnitImpl createLEM( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SCSSLexicalUnit.SAC_LEM, v );
    }

    static LexicalUnitImpl createREM( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SCSSLexicalUnit.SAC_REM, v );
    }

    static LexicalUnitImpl createEXS( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_EX, v );
    }

    public static LexicalUnitImpl createPX( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_PIXEL, v );
    }

    public static LexicalUnitImpl createCM( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_CENTIMETER, v );
    }

    static LexicalUnitImpl createMM( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_MILLIMETER, v );
    }

    static LexicalUnitImpl createIN( String uri,int line, int column, float v) {
        return new LexicalUnitImpl(uri, line, column, SAC_INCH, v);
    }

    static LexicalUnitImpl createPT( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_POINT, v );
    }

    static LexicalUnitImpl createPC( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_PICA, v );
    }

    public static LexicalUnitImpl createDEG( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_DEGREE, v );
    }

    static LexicalUnitImpl createRAD( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_RADIAN, v );
    }

    static LexicalUnitImpl createGRAD( String uri, int line, int column, float v ) {
        return new LexicalUnitImpl( uri, line, column, SAC_GRADIAN, v );
    }

    static LexicalUnitImpl createMS( String uri, int line, int column, float v ) {
        if( v < 0 ) {
            throw new ParseException( "Time values may not be negative", line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_MILLISECOND, v );
    }

    static LexicalUnitImpl createS( String uri, int line, int column, float v ) {
        if( v < 0 ) {
            throw new ParseException( "Time values may not be negative", line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_SECOND, v );
    }

    static LexicalUnitImpl createHZ( String uri, int line, int column, float v ) {
        if( v < 0 ) {
            throw new ParseException( "Frequency values may not be negative", line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_HERTZ, v );
    }

    static LexicalUnitImpl createKHZ( String uri, int line, int column, float v ) {
        if( v < 0 ) {
            throw new ParseException( "Frequency values may not be negative", line, column );
        }
        return new LexicalUnitImpl( uri, line, column, SAC_KILOHERTZ, v );
    }

    static LexicalUnitImpl createDimen( String uri, int line, int column, float v, String s ) {
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

    public static LexicalUnitImpl createString( String s ) {
        return new LexicalUnitImpl( null, 0, 0, SAC_STRING_VALUE, s );
    }

    public static LexicalUnitImpl createString( String uri, int line, int column, String s ) {
        return new LexicalUnitImpl( uri, line, column, SAC_STRING_VALUE, s );
    }

    public static LexicalUnitImpl createURL( String uri, int line, int column, String s ) {
        return new LexicalUnitImpl( uri, line, column, SAC_URI, s );
    }

    public static LexicalUnitImpl createAttr( String uri, int line, int column, String s ) {
        return new LexicalUnitImpl( uri, line, column, SAC_ATTR, s );
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

    public static boolean checkLexicalUnitType(SassListItem item,
            short... lexicalUnitTypes) {
        if (!(item instanceof LexicalUnitImpl)) {
            return false;
        }
        short itemType = ((LexicalUnitImpl)item).getLexicalUnitType();
        for (short s : lexicalUnitTypes) {
            if (itemType == s) {
                return true;
            }
        }
        return false;
    }

    public static LexicalUnitImpl createUnicodeRange( String uri, int line, int column, SassList params ) {
        // @@ return new LexicalUnitImpl( uri, line, column, previous, null,
        // SAC_UNICODERANGE, params);
        return null;
    }

    public static LexicalUnitImpl createComma( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_OPERATOR_COMMA );
    }

    public static LexicalUnitImpl createSpace( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SAC_IDENT, " " );
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

    public static LexicalUnitImpl createLeftParenthesis( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SCSS_OPERATOR_LEFT_PAREN );
    }

    public static LexicalUnitImpl createRightParenthesis( String uri, int line, int column ) {
        return new LexicalUnitImpl( uri, line, column, SCSS_OPERATOR_RIGHT_PAREN );
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
    public SassListItem replaceVariables(ScssContext context) {
        LexicalUnitImpl lui = this;

        // replace function parameters (if any)
        lui = lui.replaceParams(context);

        // replace parameters in string value
        if (lui.getLexicalUnitType() == LexicalUnitImpl.SCSS_VARIABLE) {
            return lui.replaceSimpleVariable(context);
        } else if (containsInterpolation()) {
            return lui.replaceInterpolation(context);
        }
        return lui;
    }

    private LexicalUnitImpl replaceParams(ScssContext context) {
        ActualArgumentList params = getParameterList();
        if (params != null) {
            LexicalUnitImpl copy = copy();
            copy.setParameterList(params.replaceVariables(context));
            return copy;
        } else {
            return this;
        }
    }

    private SassListItem replaceSimpleVariable(ScssContext context) {
        if (getLexicalUnitType() == LexicalUnitImpl.SCSS_VARIABLE) {
            // replace simple variable
            String stringValue = getStringValue();
            Variable var = context.getVariable(stringValue);
            if (var != null) {
                return var.getExpr().replaceVariables(context);
            }
            throw new ParseException( "Variable was not resolved: " + simpleAsString(), line, column );
        }
        return this;
    }

    private boolean containsInterpolation() {
        return s != null && s.containsInterpolation();
    }

    private SassListItem replaceInterpolation(ScssContext context) {
        // replace interpolation
        if (containsInterpolation()) {
            // handle Interpolation objects
            StringInterpolationSequence sis = s.replaceVariables(context);
            // handle strings with interpolation
            for (Variable var : context.getVariables()) {
                if (!sis.containsInterpolation()) {
                    break;
                }
                String interpolation = "#{$" + var.getName() + "}";
                String stringValue = sis.toString();
                SassListItem expr = var.getExpr();
                // strings should be unquoted
                if (stringValue.equals(interpolation)
                        && !checkLexicalUnitType(expr,
                                LexicalUnitImpl.SAC_STRING_VALUE)) {
                    // no more replacements needed, use data type of expr
                    return expr.replaceVariables(context);
                } else if (stringValue.contains(interpolation)) {
                    String replacementString = expr.replaceVariables(context)
                            .unquotedString();
                    sis = new StringInterpolationSequence(
                            stringValue.replaceAll(
                                    Pattern.quote(interpolation),
                                    Matcher.quoteReplacement(replacementString)));
                }
            }
            if (sis != s) {
                LexicalUnitImpl copy = copy();
                copy.s = sis;
                return copy;
            }
        }
        return this;
    }

    @Override
    public SassListItem evaluateFunctionsAndExpressions(ScssContext context,
            boolean evaluateArithmetics) {
        if (params != null && !"calc".equals(getFunctionName())) {
            SCSSFunctionGenerator generator = getGenerator(getFunctionName());
            LexicalUnitImpl copy = this;
            if (!"if".equals(getFunctionName())) {
                copy = createFunction( uri, line, column, fname, params.evaluateFunctionsAndExpressions( context, true ) );
            }
            if (generator == null) {
                SassListItem result = copy.replaceCustomFunctions(context);
                if (result != null) {
                    return result;
                }
            }
            if (generator == null) {
                generator = DEFAULT_SERIALIZER;
            }
            return generator.compute(context, copy);
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

    /**
     * Register a custom sass function.
     * @param generator the implementation of the custom function
     */
    public static void registerCustomFunction( SCSSFunctionGenerator generator ) {
        for (String functionName : generator.getFunctionNames()) {
            SERIALIZERS.put(functionName, generator );
        }
    }

    private static SCSSFunctionGenerator getGenerator(String funcName) {
        return SERIALIZERS.get(funcName);
    }

    private static List<SCSSFunctionGenerator> initSerializers() {
        List<SCSSFunctionGenerator> list = new LinkedList<SCSSFunctionGenerator>();
        list.add(new AbsFunctionGenerator());
        list.add(new AdjustColorFunctionGenerator());
        list.add(new CeilFunctionGenerator());
        list.add(new ComparableFunctionGenerator());
        list.add(new DarkenFunctionGenerator());
        list.add(new FloorFunctionGenerator());
        list.add(new GrayscaleFunctionGenerator());
        list.add(new IfFunctionGenerator());
        list.add(new LightenFunctionGenerator());
        list.add(new ListAppendFunctionGenerator());
        list.add(new ListIndexFunctionGenerator());
        list.add(new ListJoinFunctionGenerator());
        list.add(new ListLengthFunctionGenerator());
        list.add(new ListNthFunctionGenerator());
        list.add(new MapMergeFunctionGenerator());
        list.add(new MinMaxFunctionGenerator());
        list.add(new MixFunctionGenerator());
        list.add(new PercentageFunctionGenerator());
        list.add(new RectFunctionGenerator());
        list.add(new RGBFunctionGenerator());
        list.add(new RoundFunctionGenerator());
        list.add(new SaturationModificationFunctionGenerator());
        list.add(new TypeOfFunctionGenerator());
        list.add(new AlphaFunctionGenerator());
        list.add(new TransparencyModificationFunctionGenerator());
        list.add(new ColorComponentFunctionGenerator());
        list.add(new UnitFunctionGenerator());
        list.add(new UnitlessFunctionGenerator());
        list.add(new QuoteUnquoteFunctionGenerator());
        return list;
    }

    private static final Map<String, SCSSFunctionGenerator> SERIALIZERS = new HashMap<String, SCSSFunctionGenerator>();

    private static final SCSSFunctionGenerator DEFAULT_SERIALIZER = new DefaultFunctionGenerator();

    private String simpleAsString() {
        short type = getLexicalUnitType();
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
        case SCSS_OPERATOR_LEFT_PAREN:
            text = "(";
            break;
        case SCSS_OPERATOR_RIGHT_PAREN:
            text = ")";
            break;
        case SCSS_OPERATOR_EQUALS:
            text = "==";
            break;
        case SCSS_OPERATOR_NOT_EQUAL:
            text = "!=";
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
            text = Integer.toString(getIntegerValue());
            break;
        case SAC_REAL:
            text = getFloatOrInteger();
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
            text = getFloatOrInteger() + getDimensionUnitText();
            break;
        }
        return text;
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        short type = getLexicalUnitType();
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
                if (ColorUtil.isColor(this)) {
                    text = ColorUtil.rgbToColorString(ColorUtil
                            .colorToRgb(this));
                    break;
                } else if (ColorUtil.isRgba(this) || ColorUtil.isHsla(this)) {
                    float alpha = params.get(params.size() - 1)
                            .getContainedValue().getFloatValue();
                    rgb = ColorUtil.colorToRgb(this);
                    if (alpha == 0.0f && rgb[0] == 0 && rgb[1] == 0
                            && rgb[2] == 0) {
                        text = "transparent";
                        break;
                    } else if (alpha == 1.0f) {
                        text = ColorUtil.rgbToColorString(ColorUtil
                                .colorToRgb(this));
                        break;
                    } else if (params.size() == 2 || ColorUtil.isHsla(this)) {

                        String alphaText = alpha == 0.0f ? "0"
                                : CSS_FLOAT_FORMAT.format(alpha);
                        text = "rgba(" + rgb[0] + ", " + rgb[1] + ", " + rgb[2]
                                + ", " + alphaText + ")";
                        break;
                    }
                }
                text = fname + "(" + params.buildString(strategy) + ")";
                break;
            case SAC_IDENT:
                text = getStringValue();
                break;
            case SAC_STRING_VALUE:
                // @@SEEME. not exact
                text = "\"" + getStringValue() + "\"";
                break;
            case SAC_ATTR:
                text = "attr(" + getStringValue() + ")";
                break;
            case SAC_UNICODERANGE:
                text = "@@TODO";
                break;
            case SAC_SUB_EXPRESSION:
                text = strategy.build(getParameterList());
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

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        CSS_FLOAT_FORMAT.setDecimalFormatSymbols(symbols);
        for (SCSSFunctionGenerator serializer : initSerializers()) {
            registerCustomFunction( serializer );
        }
    }

    @Override
    public boolean containsArithmeticalOperator() {
        return false;
    }

    @Override
    public LexicalUnitImpl updateUrl(String prefix) {
        if (getLexicalUnitType() == SAC_URI) {
            String path = getStringValue().replaceAll("^\"|\"$", "")
                    .replaceAll("^'|'$", "");
            if (!path.startsWith("/") && !path.contains(":")) {
                path = prefix + path;
                path = StringUtil.cleanPath(path);
            }
            LexicalUnitImpl copy = copy();
            copy.setStringValue(path);
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
                if (getLexicalUnitType() != other.getLexicalUnitType()) {
                    return false;
                }
            }
            return getFloatValue() == other.getFloatValue()
                    && getIntegerValue() == other.getIntegerValue();
        } else if (getLexicalUnitType() != other.getLexicalUnitType()) {
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

    @Override
    public boolean containsVariable() {
        return getLexicalUnitType() == SCSS_VARIABLE;
    }
}
