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
package com.inet.sass.expression;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.inet.sass.ScssContext;
import com.inet.sass.ScssContext.UrlMode;
import com.inet.sass.expression.ArithmeticExpressionEvaluator;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SCSSLexicalUnit;
import com.inet.sass.parser.SassListItem;

public class ArithmeticExpressionEvaluatorTest {
    private ArithmeticExpressionEvaluator evaluator = new ArithmeticExpressionEvaluator();

    private LexicalUnitImpl evaluate(SassListItem... terms) {
        // clean context every time - no variables etc.
        ScssContext context = new ScssContext( UrlMode.MIXED, null );
        return (LexicalUnitImpl) evaluator.evaluate(context,
                Arrays.asList(terms));
    }

    private final LexicalUnitImpl operand2         = LexicalUnitImpl.createInteger( null, 0, 0, 2 );
    private final LexicalUnitImpl operatorMultiply = LexicalUnitImpl.createMultiply( null, 0, 0 );
    private final LexicalUnitImpl operand3         = LexicalUnitImpl.createInteger( null, 0, 0, 3 );
    private final LexicalUnitImpl operatorMinus    = LexicalUnitImpl.createMinus( null, 0, 0 );
    private final LexicalUnitImpl operand4         = LexicalUnitImpl.createInteger( null, 0, 0, 4 );
    private final LexicalUnitImpl operand2cm       = LexicalUnitImpl.createCM( null, 0, 0, 2 );
    private final LexicalUnitImpl operand3px       = LexicalUnitImpl.createPX( null, 0, 0, 3 );
    private final LexicalUnitImpl operand3cm       = LexicalUnitImpl.createCM( null, 0, 0, 3 );
    private final LexicalUnitImpl operand4cm       = LexicalUnitImpl.createCM( null, 0, 0, 4 );
    private final LexicalUnitImpl operatorDivide   = LexicalUnitImpl.createSlash( null, 0, 0 );
    private final LexicalUnitImpl operatorComma    = LexicalUnitImpl.createComma( null, 2, 3 );

    @Test
    public void testPrecedenceSameAsAppearOrder() {
        // 2 * 3 - 4 = 2
        LexicalUnitImpl result = evaluate(operand2, operatorMultiply, operand3,
                operatorMinus, operand4);
        Assert.assertEquals(2, result.getIntegerValue());
    }

    @Test
    public void testPrecedenceDifferFromAppearOrder() {
        // 2 - 3 * 4 = -10
        LexicalUnitImpl result = evaluate(operand2, operatorMinus, operand3,
                operatorMultiply, operand4);
        Assert.assertEquals(-10, result.getIntegerValue());
    }

    @Test(expected = ParseException.class)
    public void testIncompatibleUnit() {
        // 2cm - 3px
        evaluate(operand2cm, operatorMinus, operand3px);
    }

    @Test
    public void testMultiplyWithUnitInfirstOperand() {
        // 2cm * 3 = 6cm
        LexicalUnitImpl result = evaluate(operand2cm, operatorMultiply,
                operand3);
        Assert.assertEquals(6, result.getIntegerValue());
        Assert.assertEquals(SCSSLexicalUnit.SAC_CENTIMETER,
                result.getLexicalUnitType());
    }

    @Test
    public void testMultiplyWithUnitInSecondOperand() {
        // 2 * 3cm = 6cm
        LexicalUnitImpl result = evaluate(operand2, operatorMultiply,
                operand3cm);
        Assert.assertEquals(6, result.getIntegerValue());
        Assert.assertEquals(SCSSLexicalUnit.SAC_CENTIMETER,
                result.getLexicalUnitType());
    }

    @Test
    public void testDivideWithSameUnit() {
        // 4cm / 2cm = 2
        LexicalUnitImpl result = evaluate(operand4cm, operatorDivide,
                operand2cm);
        Assert.assertEquals(2, result.getIntegerValue());
        Assert.assertEquals(SCSSLexicalUnit.SAC_REAL, result.getLexicalUnitType());
    }

    @Test
    public void testDivideDenominatorWithoutUnit() {
        // 4cm / 2 = 2cm
        LexicalUnitImpl result = evaluate(operand4cm, operatorDivide, operand2);
        Assert.assertEquals(2, result.getIntegerValue());
        Assert.assertEquals(SCSSLexicalUnit.SAC_CENTIMETER,
                result.getLexicalUnitType());
    }

    @Test(expected = ParseException.class)
    public void testNonExistingSignal() {
        LexicalUnitImpl result = evaluate(operand2, operatorComma, operand3);
    }
}
