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
package com.inet.sass.function;

import static com.inet.sass.parser.SCSSLexicalUnit.SAC_CENTIMETER;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_DEGREE;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_DIMENSION;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_EM;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_EX;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_FUNCTION;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_GRADIAN;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_HERTZ;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_IDENT;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_INCH;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_INTEGER;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_KILOHERTZ;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_LEM;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_MILLIMETER;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_MILLISECOND;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_PERCENTAGE;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_PICA;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_PIXEL;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_POINT;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_RADIAN;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_REAL;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_REM;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_RGBCOLOR;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_SECOND;
import static com.inet.sass.parser.SCSSLexicalUnit.SCSS_LIST;
import static com.inet.sass.parser.SCSSLexicalUnit.SCSS_NULL;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassList.Separator;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.util.ColorUtil;

class TypeOfFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "value" };

    TypeOfFunctionGenerator() {
        super( createArgumentList( argumentNames, false ), "type-of" );
    }

    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassListItem param = getParam( actualArguments, 0 );
        String type = "string";

        switch( param.getItemType() ) {
            case SCSS_LIST:
                type = "list";
                SassList list = (SassList)param;
                if( list.size() > 0 ) {
                    // we need only to check the first entry, the other has the parser already checked
                    SassListItem first = list.get( 0 );
                    if( first.getClass() == SassList.class && ((SassList)first).getSeparator() == Separator.COLON ) {
                        type = "map";
                    }
                }
                break;
            case SCSS_NULL:
                type = "null";
                break;
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
                type = "number";
                break;
            case SAC_IDENT:
                String str = ((LexicalUnitImpl)param).getStringValue();
                if( "true".equals( str ) || "false".equals( str ) ) {
                    type = "bool";
                    break;
                }
                if( ColorUtil.isColorName( str ) || ColorUtil.isHexColor( str ) ) {
                    type = "color";
                    break;
                }
                break;
            case SAC_RGBCOLOR:
                type = "color";
                break;
            case SAC_FUNCTION:
                str = ((LexicalUnitImpl)param).getFunctionName();
                switch( str ) {
                    case "rgb":
                    case "rgba":
                    case "hsl":
                    case "hsla":
                        type = "color";
                        break;
                }
                break;
        }

        return LexicalUnitImpl.createRawIdent( function.getUri(), function.getLineNumber(), function.getColumnNumber(), type );
    }
}
