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
package com.inet.sass.parser;

public interface SCSSLexicalUnit {

    /**
     * ,
     */
    public static final short SAC_OPERATOR_COMMA        = 0;
    /**
     * +
     */
    public static final short SAC_OPERATOR_PLUS         = 1;
    /**
     * -
     */
    public static final short SAC_OPERATOR_MINUS        = 2;
    /**
     * *
     */
    public static final short SAC_OPERATOR_MULTIPLY     = 3;
    /**
     * /
     */
    public static final short SAC_OPERATOR_SLASH        = 4;
    /**
     * %
     */
    public static final short SAC_OPERATOR_MOD          = 5;
    /**
     * ^
     */
    public static final short SAC_OPERATOR_EXP          = 6;
    /**
     * <
     */
    public static final short SAC_OPERATOR_LT           = 7;
    /**
     * >
     */
    public static final short SAC_OPERATOR_GT           = 8;
    /**
     * <=
     */
    public static final short SAC_OPERATOR_LE           = 9;
    /**
     * >=
     */
    public static final short SAC_OPERATOR_GE           = 10;
    /**
     * ~
     */
    public static final short SAC_OPERATOR_TILDE        = 11;

    /**
     * identifier <code>inherit</code>.
     */
    public static final short SAC_INHERIT               = 12;
    /**
     * Integers.
     * @see #getIntegerValue
     */
    public static final short SAC_INTEGER               = 13;
    /**
     * reals.
     */
    public static final short SAC_REAL                  = 14;
    /**
     * Relative length<code>em</code>.
     */
    public static final short SAC_EM                    = 15;
    /**
     * Relative length<code>ex</code>.
     */
    public static final short SAC_EX                    = 16;
    /**
     * Relative length <code>px</code>.
     */
    public static final short SAC_PIXEL                 = 17;
    /**
     * Absolute length <code>in</code>.
     */
    public static final short SAC_INCH                  = 18;
    /**
     * Absolute length <code>cm</code>.
     */
    public static final short SAC_CENTIMETER            = 19;
    /**
     * Absolute length <code>mm</code>.
     */
    public static final short SAC_MILLIMETER            = 20;
    /**
     * Absolute length <code>pt</code>.
     */
    public static final short SAC_POINT                 = 21;
    /**
     * Absolute length <code>pc</code>.
     */
    public static final short SAC_PICA                  = 22;
    /**
     * Percentage.
     */
    public static final short SAC_PERCENTAGE            = 23;
    /**
     * URI: <code>uri(...)</code>.
     * @see #getStringValue
     */
    public static final short SAC_URI                   = 24;
    /**
     * function <code>counter</code>.
     */
    public static final short SAC_COUNTER_FUNCTION      = 25;
    /**
     * function <code>counters</code>.
     */
    public static final short SAC_COUNTERS_FUNCTION     = 26;
    /**
     * RGB Colors. <code>rgb(0, 0, 0)</code> and <code>#000</code>
     */
    public static final short SAC_RGBCOLOR              = 27;
    /**
     * Angle <code>deg</code>.
     */
    public static final short SAC_DEGREE                = 28;
    /**
     * Angle <code>grad</code>.
     */
    public static final short SAC_GRADIAN               = 29;
    /**
     * Angle <code>rad</code>.
     */
    public static final short SAC_RADIAN                = 30;
    /**
     * Time <code>ms</code>.
     */
    public static final short SAC_MILLISECOND           = 31;
    /**
     * Time <code>s</code>.
     */
    public static final short SAC_SECOND                = 32;
    /**
     * Frequency <code>Hz</code>.
     */
    public static final short SAC_HERTZ                 = 33;
    /**
     * Frequency <code>kHz</code>.
     */
    public static final short SAC_KILOHERTZ             = 34;

    /**
     * any identifier except <code>inherit</code>.
     */
    public static final short SAC_IDENT                 = 35;
    /**
     * A string.
     */
    public static final short SAC_STRING_VALUE          = 36;

    /**
     * function <code>rect</code>.
     * @see #getFunctionName
     * @see #getParameters
     */
    public static final short SAC_RECT_FUNCTION         = 38;

    /**
     * sub expressions <code>(a)</code> <code>(a + b)</code> <code>(normal/none)</code>
     * @see #getSubValues
     */
    public static final short SAC_SUB_EXPRESSION        = 40;

    /**
     * unknown function.
     * @see #getFunctionName
     * @see #getParameters
     */
    public static final short SAC_FUNCTION              = 41;
    /**
     * unknown dimension.
     */
    public static final short SAC_DIMENSION             = 42;

    static final short        SCSS_VARIABLE             = 100;
    static final short        SCSS_OPERATOR_EQUALS      = 103;
    static final short        SCSS_OPERATOR_NOT_EQUAL   = 104;
    static final short        SCSS_OPERATOR_AND         = 105;
    static final short        SCSS_OPERATOR_OR          = 106;
    static final short        SCSS_OPERATOR_NOT         = 107;
    static final short        SCSS_NULL                 = 110;

    /**
     * Result of SASS function get-function(x)
     */
    static final short        SCSS_GET_FUNCTION         = 111;

    static final short        SCSS_INTERPOLATION        = 112; 

    /**
     * "&" placeholder for the parent selector reference
     */
    static final short        SCSS_PARENT               = 113;

    static final short        SCSS_EXPRESSION           = 114; 

    static final short        SCSS_LIST                 = 115; 

    static final short        SCSS_STRING               = 116; 

    static final short        SAC_LEM                   = 200;
    static final short        SAC_REM                   = 201;

}
