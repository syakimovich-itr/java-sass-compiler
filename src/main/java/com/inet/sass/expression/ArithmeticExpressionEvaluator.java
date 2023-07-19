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

import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_GE;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_GT;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_LE;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_LT;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_MINUS;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_MOD;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_MULTIPLY;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_PLUS;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_OPERATOR_SLASH;
import static com.inet.sass.parser.SCSSLexicalUnit.SCSS_OPERATOR_AND;
import static com.inet.sass.parser.SCSSLexicalUnit.SCSS_OPERATOR_EQUALS;
import static com.inet.sass.parser.SCSSLexicalUnit.SCSS_OPERATOR_NOT_EQUAL;
import static com.inet.sass.parser.SCSSLexicalUnit.SCSS_OPERATOR_OR;

import java.util.List;
import java.util.Stack;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassList.Separator;
import com.inet.sass.parser.SassListItem;

public class ArithmeticExpressionEvaluator {

    private static void createNewOperand( BinaryOperator operator, Stack<Object> operands ) {
        Object rightOperand = operands.pop();
        operands.push( new BinaryExpression( operands.pop(), operator, rightOperand ) );
    }

    /**
     * If the operation can be evaluated without evaluation the right operand
     * @param operator the operation
     * @param operands all operands
     * @return true, if the right operand should not be evaluated
     */
    private static boolean isShortCircuitEvaluation( BinaryOperator operator, Stack<Object> operands ) {
        boolean value;
        switch( operator ) {
            case OR:
                value = true;
                break;
            case AND:
                value = false;
                break;
            default:
                return false;
        }
        Object expr = operands.peek();
        SassListItem left = expr instanceof BinaryExpression ? ((BinaryExpression)expr).eval() : (SassListItem)expr;
        return value == BinaryOperator.isTrue( left );
    }

    private static Object createExpression( ScssContext context, List<SassListItem> terms ) {
        SassListItem current = null;
        boolean afterOperand = false;
        Stack<Object> operands = new Stack<Object>();
        Stack<Object> operators = new Stack<Object>();
        int termCount = terms.size();
        inputTermLoop: for( int i = 0; i < termCount; ++i ) {
            current = terms.get( i );
            if( current == LexicalUnitImpl.WHITESPACE ) {
                continue;
            }
            if( afterOperand ) {
                afterOperand = false;
                BinaryOperator operator;
                switch( current.getItemType() ) {
                    case SCSS_OPERATOR_OR:
                        operator = BinaryOperator.OR;
                        break;
                    case SCSS_OPERATOR_AND:
                        operator = BinaryOperator.AND;
                        break;
                    case SCSS_OPERATOR_EQUALS:
                        operator = BinaryOperator.EQUALS;
                        break;
                    case SCSS_OPERATOR_NOT_EQUAL:
                        operator = BinaryOperator.NOT_EQUAL;
                        break;
                    case SAC_OPERATOR_LT:
                        operator = BinaryOperator.LESS_THAN;
                        break;
                    case SAC_OPERATOR_GT:
                        operator = BinaryOperator.GREATER_THAN;
                        break;
                    case SAC_OPERATOR_LE:
                        operator = BinaryOperator.LESS_THAN_OR_EQUALS;
                        break;
                    case SAC_OPERATOR_GE:
                        operator = BinaryOperator.GREATER_THAN_OR_EQUALS;
                        break;
                    case SAC_OPERATOR_PLUS:
                        operator = BinaryOperator.ADD;
                        break;
                    case SAC_OPERATOR_MINUS:
                        operator = BinaryOperator.MINUS;
                        break;
                    case SAC_OPERATOR_MULTIPLY:
                        operator = BinaryOperator.MUL;
                        break;
                    case SAC_OPERATOR_SLASH:
                        operator = BinaryOperator.DIV;
                        break;
                    case SAC_OPERATOR_MOD:
                        operator = BinaryOperator.MOD;
                        break;
                    default:
                        throw new ParseException( "Illegal arithmetic expression: " + new SassList( Separator.SPACE, terms ).printState(), current );
                }
                while( !operators.isEmpty() ) {
                    Object previous = operators.peek();
                    if( previous == Parentheses.LEFT || ((BinaryOperator)previous).precedence < operator.precedence ) {
                        break;
                    }
                    createNewOperand( (BinaryOperator)operators.pop(), operands );
                }

                if( isShortCircuitEvaluation( operator, operands ) ) {
                    break inputTermLoop;
                }
                operators.push( operator );

                continue inputTermLoop;
            }
            afterOperand = true;

            operands.push( current.evaluateFunctionsAndExpressions( context, true ) );
        }

        while( !operators.isEmpty() ) {
            Object operator = operators.pop();
            if( operator == Parentheses.LEFT ) {
                throw new ParseException( "Unexpected \"(\" found", current );
            }
            createNewOperand( (BinaryOperator)operator, operands );
        }
        Object expression = operands.pop();
        if( !operands.isEmpty() ) {
            LexicalUnitImpl operand = (LexicalUnitImpl)operands.peek();
            throw new ParseException( "Unexpected operand " + operand.toString() + " found", current );
        }
        return expression;
    }

    public static SassListItem evaluate( ScssContext context, List<SassListItem> terms ) {
        Object result = createExpression( context, terms );
        if( result instanceof BinaryExpression ) {
            return ((BinaryExpression)result).eval();
        }
        // createExpression returns either a BinaryExpression or a
        // SassListItem
        return (SassListItem)result;
    }
}