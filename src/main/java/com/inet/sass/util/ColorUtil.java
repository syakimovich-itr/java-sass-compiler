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

package com.inet.sass.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SCSSLexicalUnit;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassList.Separator;

public class ColorUtil {

    private static Map<String, String> colorNameToHex = new HashMap<String, String>();
    private static Map<String, String> hexToColorName = new HashMap<String, String>();

    static {
        colorNameToHex.put("aliceblue", "#f0f8ff");
        colorNameToHex.put("antiquewhite", "#faebd7");
        colorNameToHex.put("aqua", "#00ffff");
        colorNameToHex.put("aquamarine", "#7fffd4");
        colorNameToHex.put("azure", "#f0ffff");
        colorNameToHex.put("beige", "#f5f5dc");
        colorNameToHex.put("bisque", "#ffe4c4");
        colorNameToHex.put("black", "#000000");
        colorNameToHex.put("blanchedalmond", "#ffebcd");
        colorNameToHex.put("blue", "#0000ff");
        colorNameToHex.put("blueviolet", "#8a2be2");
        colorNameToHex.put("brown", "#a52a2a");
        colorNameToHex.put("burlywood", "#deb887");
        colorNameToHex.put("cadetblue", "#5f9ea0");
        colorNameToHex.put("chartreuse", "#7fff00");
        colorNameToHex.put("chocolate", "#d2691e");
        colorNameToHex.put("coral", "#ff7f50");
        colorNameToHex.put("cornflowerblue", "#6495ed");
        colorNameToHex.put("cornsilk", "#fff8dc");
        colorNameToHex.put("crimson", "#dc143c");
        colorNameToHex.put("cyan", "#00ffff");
        colorNameToHex.put("darkblue", "#00008b");
        colorNameToHex.put("darkcyan", "#008b8b");
        colorNameToHex.put("darkgoldenrod", "#b8860b");
        colorNameToHex.put("darkgray", "#a9a9a9");
        colorNameToHex.put("darkgreen", "#006400");
        colorNameToHex.put("darkkhaki", "#bdb76b");
        colorNameToHex.put("darkmagenta", "#8b008b");
        colorNameToHex.put("darkolivegreen", "#556b2f");
        colorNameToHex.put("darkorange", "#ff8c00");
        colorNameToHex.put("darkorchid", "#9932cc");
        colorNameToHex.put("darkred", "#8b0000");
        colorNameToHex.put("darksalmon", "#e9967a");
        colorNameToHex.put("darkseagreen", "#8fbc8f");
        colorNameToHex.put("darkslateblue", "#483d8b");
        colorNameToHex.put("darkslategray", "#2f4f4f");
        colorNameToHex.put("darkturquoise", "#00ced1");
        colorNameToHex.put("darkviolet", "#9400d3");
        colorNameToHex.put("deeppink", "#ff1493");
        colorNameToHex.put("deepskyblue", "#00bfff");
        colorNameToHex.put("dimgray", "#696969");
        colorNameToHex.put("dodgerblue", "#1e90ff");
        colorNameToHex.put("firebrick", "#b22222");
        colorNameToHex.put("floralwhite", "#fffaf0");
        colorNameToHex.put("forestgreen", "#228b22");
        colorNameToHex.put("fuchsia", "#ff00ff");
        colorNameToHex.put("gainsboro", "#dcdcdc");
        colorNameToHex.put("ghostwhite", "#f8f8ff");
        colorNameToHex.put("gold", "#ffd700");
        colorNameToHex.put("goldenrod", "#daa520");
        colorNameToHex.put("gray", "#808080");
        colorNameToHex.put("green", "#008000");
        colorNameToHex.put("greenyellow", "#adff2f");
        colorNameToHex.put("honeydew", "#f0fff0");
        colorNameToHex.put("hotpink", "#ff69b4");
        colorNameToHex.put("indianred", "#cd5c5c");
        colorNameToHex.put("indigo", "#4b0082");
        colorNameToHex.put("ivory", "#fffff0");
        colorNameToHex.put("khaki", "#f0e68c");
        colorNameToHex.put("lavender", "#e6e6fa");
        colorNameToHex.put("lavenderblush", "#fff0f5");
        colorNameToHex.put("lawngreen", "#7cfc00");
        colorNameToHex.put("lemonchiffon", "#fffacd");
        colorNameToHex.put("lightblue", "#add8e6");
        colorNameToHex.put("lightcoral", "#f08080");
        colorNameToHex.put("lightcyan", "#e0ffff");
        colorNameToHex.put("lightgoldenrodyellow", "#fafad2");
        colorNameToHex.put("lightgray", "#d3d3d3");
        colorNameToHex.put("lightgreen", "#90ee90");
        colorNameToHex.put("lightpink", "#ffb6c1");
        colorNameToHex.put("lightsalmon", "#ffa07a");
        colorNameToHex.put("lightseagreen", "#20b2aa");
        colorNameToHex.put("lightskyblue", "#87cefa");
        colorNameToHex.put("lightslategray", "#778899");
        colorNameToHex.put("lightsteelblue", "#b0c4de");
        colorNameToHex.put("lightyellow", "#ffffe0");
        colorNameToHex.put("lime", "#00ff00");
        colorNameToHex.put("limegreen", "#32cd32");
        colorNameToHex.put("linen", "#faf0e6");
        colorNameToHex.put("magenta", "#ff00ff");
        colorNameToHex.put("maroon", "#800000");
        colorNameToHex.put("mediumaquamarine", "#66cdaa");
        colorNameToHex.put("mediumblue", "#0000cd");
        colorNameToHex.put("mediumorchid", "#ba55d3");
        colorNameToHex.put("mediumpurple", "#9370db");
        colorNameToHex.put("mediumseagreen", "#3cb371");
        colorNameToHex.put("mediumslateblue", "#7b68ee");
        colorNameToHex.put("mediumspringgreen", "#00fa9a");
        colorNameToHex.put("mediumturquoise", "#48d1cc");
        colorNameToHex.put("mediumvioletred", "#c71585");
        colorNameToHex.put("midnightblue", "#191970");
        colorNameToHex.put("mintcream", "#f5fffa");
        colorNameToHex.put("mistyrose", "#ffe4e1");
        colorNameToHex.put("moccasin", "#ffe4b5");
        colorNameToHex.put("navajowhite", "#ffdead");
        colorNameToHex.put("navy", "#000080");
        colorNameToHex.put("oldlace", "#fdf5e6");
        colorNameToHex.put("olive", "#808000");
        colorNameToHex.put("olivedrab", "#6b8e23");
        colorNameToHex.put("orange", "#ffa500");
        colorNameToHex.put("orangered", "#ff4500");
        colorNameToHex.put("orchid", "#da70d6");
        colorNameToHex.put("palegoldenrod", "#eee8aa");
        colorNameToHex.put("palegreen", "#98fb98");
        colorNameToHex.put("paleturquoise", "#afeeee");
        colorNameToHex.put("palevioletred", "#db7093");
        colorNameToHex.put("papayawhip", "#ffefd5");
        colorNameToHex.put("peachpuff", "#ffdab9");
        colorNameToHex.put("peru", "#cd853f");
        colorNameToHex.put("pink", "#ffc0cb");
        colorNameToHex.put("plum", "#dda0dd");
        colorNameToHex.put("powderblue", "#b0e0e6");
        colorNameToHex.put("purple", "#800080");
        colorNameToHex.put("rebeccapurple", "#663399");
        colorNameToHex.put("red", "#ff0000");
        colorNameToHex.put("rosybrown", "#bc8f8f");
        colorNameToHex.put("royalblue", "#4169e1");
        colorNameToHex.put("saddlebrown", "#8b4513");
        colorNameToHex.put("salmon", "#fa8072");
        colorNameToHex.put("sandybrown", "#f4a460");
        colorNameToHex.put("seagreen", "#2e8b57");
        colorNameToHex.put("seashell", "#fff5ee");
        colorNameToHex.put("sienna", "#a0522d");
        colorNameToHex.put("silver", "#c0c0c0");
        colorNameToHex.put("skyblue", "#87ceeb");
        colorNameToHex.put("slateblue", "#6a5acd");
        colorNameToHex.put("slategray", "#708090");
        colorNameToHex.put("snow", "#fffafa");
        colorNameToHex.put("springgreen", "#00ff7f");
        colorNameToHex.put("steelblue", "#4682b4");
        colorNameToHex.put("tan", "#d2b48c");
        colorNameToHex.put("teal", "#008080");
        colorNameToHex.put("thistle", "#d8bfd8");
        colorNameToHex.put("tomato", "#ff6347");
        colorNameToHex.put("turquoise", "#40e0d0");
        colorNameToHex.put("violet", "#ee82ee");
        colorNameToHex.put("wheat", "#f5deb3");
        colorNameToHex.put("white", "#ffffff");
        colorNameToHex.put("whitesmoke", "#f5f5f5");
        colorNameToHex.put("yellow", "#ffff00");
        colorNameToHex.put("yellowgreen", "#9acd32");

        for (Entry<String, String> entry : colorNameToHex.entrySet()) {
            hexToColorName.put(entry.getValue(), entry.getKey());
        }

        //British
        colorNameToHex.put("darkgrey", "#a9a9a9");
        colorNameToHex.put("darkslategrey", "#2f4f4f");
        colorNameToHex.put("dimgrey", "#696969");
        colorNameToHex.put("grey", "#808080");
        colorNameToHex.put("lightgrey", "#d3d3d3");
        colorNameToHex.put("lightslategrey", "#778899");
        colorNameToHex.put("slategrey", "#708090");
    }

    /**
     * Returns true if the lexical unit represents a valid color (hexadecimal,
     * rgb(), color name etc.), false otherwise.
     * 
     * Note that rgba() and hsla() are not considered to be normal colors. To
     * detect it, call isRgba() or isHsla().
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents a color
     */
    public static boolean isColor(LexicalUnitImpl unit) {
        return isHexColor(unit) || isHslColor(unit) || isRgbFunction(unit)
                || isColorName(unit);
    }

    /**
     * Returns true if the lexical unit represents a valid rgb() method call ,
     * false otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents an RGB method call
     */
    public static boolean isRgbFunction(LexicalUnitImpl unit) {
        if( !LexicalUnitImpl.checkLexicalUnitType( unit, SCSSLexicalUnit.SAC_FUNCTION, SCSSLexicalUnit.SAC_RGBCOLOR ) ) {
            return false;
        }
        if (unit.getParameterList().size() != 3
                || !"rgb".equals(unit.getFunctionName())) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the lexical unit represents a valid RGBA value, false
     * otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents an RGBA value
     */
    public static boolean isRgba(LexicalUnitImpl unit) {
        return unit.getItemType() == SCSSLexicalUnit.SAC_FUNCTION
                && "rgba".equals(unit.getFunctionName())
                && (unit.getParameterList().size() == 2 || unit
                        .getParameterList().size() == 4);
    }

    /**
     * Returns true if the lexical unit represents a valid HSLA value, false
     * otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents an HSLA value
     */
    public static boolean isHsla(LexicalUnitImpl unit) {
        return unit.getItemType() == SCSSLexicalUnit.SAC_FUNCTION
                && "hsla".equals(unit.getFunctionName())
                && unit.getParameterList().size() == 4;
    }

    /**
     * Returns true if the lexical unit represents a valid color in the
     * hexadecimal form (three or six digits), false otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents a hexadecimal color
     */
    public static boolean isHexColor(LexicalUnitImpl unit) {
        if( unit.getItemType() != SCSSLexicalUnit.SAC_IDENT ) {
            return false;
        }
        return isHexColor( unit.getStringValue() );
    }

    public static boolean isHexColor( String str ) {
        int length = str.length();
        switch( length ) {
            case 4:
            case 7:
                if( str.charAt( 0 ) != '#' ) {
                    return false;
                }
                for( int i = 1; i < length; i++ ) {
                    char ch = str.charAt( i );
                    if( ch >= '0' && ch <='9' ) {
                        continue;
                    }
                    if( ch >= 'a' && ch <='f' ) {
                        continue;
                    }
                    if( ch >= 'A' && ch <='F' ) {
                        continue;
                    }
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns true if the lexical unit represents a valid color name, false
     * otherwise.
     * 
     * Currently, the 17 standard color names from the HTML and CSS color
     * specification are supported.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents a color name
     */
    public static boolean isColorName(LexicalUnitImpl unit) {
        return unit.getItemType() == SCSSLexicalUnit.SAC_IDENT
                && colorNameToHex.containsKey(unit.getStringValue());
    }

    public static boolean isColorName( String str ) {
        return colorNameToHex.containsKey( str );
    }

    /**
     * Returns true if the lexical unit represents a valid hsl() color function
     * call, false otherwise.
     * 
     * @param unit
     *            lexical unit
     * @return true if unit represents as HSL color
     */
    public static boolean isHslColor(LexicalUnitImpl unit) {
        return unit.getItemType() == SCSSLexicalUnit.SAC_FUNCTION
                && "hsl".equals(unit.getFunctionName())
                && unit.getParameterList().size() == 3;
    }

    /**
     * Returns the alpha component of the color. For colors that do not have an
     * explicit alpha component, returns 1.
     * 
     * @param color
     *            An object representing a valid color.
     * @return The alpha component of color.
     */
    public static double getAlpha(LexicalUnitImpl color) {
        if (isHsla(color) || isRgba(color)) {
            ActualArgumentList params = color.getParameterList();
            return params.get(params.size() - 1).getContainedValue()
                    .getDoubleValue();
        } else if (isColor(color)) {
            return 1;
        }
        throw new ParseException("The parameter is not a valid color: "
                + color.toString(), color);
    }

    /**
     * Converts a color into an array of its RGB components.
     * 
     * In the case of an RGBA value, the alpha channel is ignored.
     * 
     * @param color
     *            a lexical unit that represents a color
     * @return RGB components or null if not a color
     */
    public static int[] colorToRgb(LexicalUnitImpl color) {
        if (isRgba(color)) {
            if (color.getParameterList().size() == 2
                    && color.getParameterList().get(0) instanceof LexicalUnitImpl) {
                return colorToRgb((LexicalUnitImpl) color.getParameterList()
                        .get(0));
            } else {
                int red = color.getParameterList().get(0).getContainedValue()
                        .getIntegerValue();
                int green = color.getParameterList().get(1).getContainedValue()
                        .getIntegerValue();
                int blue = color.getParameterList().get(2).getContainedValue()
                        .getIntegerValue();
                return new int[] { red, green, blue };
            }
        } else if (isHsla(color)) {
            return hslToRgb(color);
        } else if (isHexColor(color)) {
            return hexColorToRgb(color);
        } else if (isHslColor(color)) {
            return hslToRgb(color);
        } else if (isRgbFunction(color)) {
            return rgbFunctionToRgb(color);
        } else if (isColorName(color)) {
            return colorNameToRgb(color);
        }
        return null;
    }

    /**
     * Converts an array of RGB components to a string representing the color.
     * 
     * @param rgb
     *            the RGB components of a color
     * @return a valid string representation of the color
     */
    public static String rgbToColorString(int[] rgb) {
        String colorString = rgbToHexColor(rgb, 6);
        if (hexToColorName.containsKey(colorString)) {
            colorString = hexToColorName.get(colorString);
        }
        return colorString;
    }

    /**
     * Converts an array of HSL components to a string representing the color.
     * 
     * @param hsl
     *            the HSL components of a color
     * @return a valid string representation of the color
     */
    public static int[] hslToRgb(double[] hsl) {
        double h = ((hsl[0] % 360) + 360) % 360 / 360.0;
        double s = hsl[1] / 100;
        double l = hsl[2] / 100;
        double m2, m1;
        int[] rgb = new int[3];
        m2 = l <= 0.5 ? l * (s + 1) : l + s - l * s;
        m1 = l * 2 - m2;
        rgb[0] = (int)Math.round(hueToRgb(m1, m2, h + 1f / 3) * 255);
        rgb[1] = (int)Math.round(hueToRgb(m1, m2, h) * 255);
        rgb[2] = (int)Math.round(hueToRgb(m1, m2, h - 1f / 3) * 255);
        return rgb;
    }

    /**
     * Converts a color into an array of its HSL (hue, saturation, lightness)
     * components.
     * 
     * @param color
     *            a lexical unit that represents a color
     * @return HSL components or null if not a color
     */
    public static double[] colorToHsl(LexicalUnitImpl color) {
        if (isHslColor(color) || isHsla(color)) {
            ActualArgumentList params = color.getParameterList();
            double hue = params.get( 0 ).getContainedValue().getDoubleValue();
            double saturation = params.get( 1 ).getContainedValue().getDoubleValue();
            double lightness = params.get( 2 ).getContainedValue().getDoubleValue();
            return new double[] { hue, saturation, lightness };
        }
        // TODO shortcut path for hsl()? need to take percent vs. integer vs.
        // real into account
        // TODO int[] loses precision
        int[] rgb = colorToRgb(color);
        if (rgb == null) {
            return null;
        }
        double hsl[] = calculateHsl(rgb[0], rgb[1], rgb[2]);
        return hsl;
    }

    /**
     * Converts an rgb() function call into an array of its RGB components. The
     * caller must ensure that isRgbFunction(color) returns true for the
     * parameter.
     * 
     * @param color
     *            a lexical unit that represents a function call for rgb()
     * @return RGB components
     */
    private static int[] rgbFunctionToRgb(LexicalUnitImpl color) {
        int red = color.getParameterList().get(0).getContainedValue()
                .getIntegerValue();
        int green = color.getParameterList().get(1).getContainedValue()
                .getIntegerValue();
        int blue = color.getParameterList().get(2).getContainedValue()
                .getIntegerValue();
        return new int[] { red, green, blue };
    }

    /**
     * Converts a hex color (with three or six hex characters) into an array of
     * its RGB components. The caller must ensure that isHexColor(hexColor)
     * returns true for the parameter.
     * 
     * @param hexColor
     *            a hexadecimal representation for a color (in CSS and HTML
     *            color specification format)
     * @return RGB components
     */
    private static int[] hexColorToRgb(String hexColor) {
        String s = hexColor.substring(1);
        int r = 0, g = 0, b = 0;
        if (s.length() == 3) {
            String sh = s.substring(0, 1);
            r = Integer.parseInt(sh + sh, 16);
            sh = s.substring(1, 2);
            g = Integer.parseInt(sh + sh, 16);
            sh = s.substring(2, 3);
            b = Integer.parseInt(sh + sh, 16);
        } else if (s.length() == 6) {
            r = Integer.parseInt(s.substring(0, 2), 16);
            g = Integer.parseInt(s.substring(2, 4), 16);
            b = Integer.parseInt(s.substring(4, 6), 16);
        }
        return new int[] { r, g, b };

    }

    /**
     * Converts a hex color (with three or six hex characters) into an array of
     * its RGB components. The caller must ensure that isHexColor(hexColor)
     * returns true for the parameter.
     * 
     * @param hexColor
     *            a lexical unit containing an identifier that represents a
     *            hexadecimal representation for a color (in CSS and HTML color
     *            specification format)
     * @return RGB components
     */
    private static int[] hexColorToRgb(LexicalUnitImpl hexColor) {
        return hexColorToRgb(hexColor.getStringValue());
    }

    /**
     * Converts a color name into an array of its RGB components. The caller
     * must ensure that isColorName(color) returns true for the parameter.
     * 
     * @param color
     *            a lexical unit that represents a color
     * @return RGB components
     */
    private static int[] colorNameToRgb(LexicalUnitImpl color) {
        return hexColorToRgb(colorNameToHex.get(color.getStringValue()));
    }

    private static String rgbToHexColor(int[] rgb, int length) {
        StringBuilder builder = new StringBuilder("#");
        for (int i = 0; i < 3; i++) {
            String color = Integer.toHexString(rgb[i]);
            if (length == 6) {
                if (color.length() == 1) {
                    color = "0" + color;
                }
            }
            if (length == 3) {
                color = color.substring(0, 1);
            }
            builder.append(color);
        }
        return builder.toString();
    }

    private static int[] hslToRgb(LexicalUnitImpl hsl) {
        ActualArgumentList hslParam = hsl.getParameterList();
        if (hslParam.size() != 3 && hslParam.size() != 4) {
            throw new ParseException(
                    "The function hsl() requires exactly three parameters", hsl);
        }

        double hue = hslParam.get(0).getContainedValue().getDoubleValue();
        double saturation = hslParam.get(1).getContainedValue().getDoubleValue();
        double lightness = hslParam.get(2).getContainedValue().getDoubleValue();

        return hslToRgb(new double[] { hue, saturation, lightness });
    }

    private static double[] calculateHsl(int red, int green, int blue) {
        double[] hsl = new double[3];

        double r = red / 255.0;
        double g = green / 255.0;
        double b = blue / 255.0;

        double max = Math.max(Math.max(r, g), b);
        double min = Math.min(Math.min(r, g), b);
        double d = max - min;

        double h = 0, s = 0, l = 0;

        if (max == min) {
            h = 0;
        }
        if (max == r) {
            h = 60 * (g - b) / d;
        } else if (max == g) {
            h = 60 * (b - r) / d + 120;
        } else if (max == b) {
            h = 60 * (r - g) / d + 240;
        }

        l = (max + min) / 2.0;

        if (max == min) {
            s = 0;
        } else if (l < 0.5) {
            s = d / (2 * l);
        } else {
            s = d / (2 - 2 * l);
        }

        hsl[0] = h % 360;
        hsl[1] = s * 100;
        hsl[2] = l * 100;
        // If saturation is 0, the hue is not well defined. Use hue 0 in this
        // case.
        if (hsl[1] == 0) {
            hsl[0] = 0;
        }

        return hsl;
    }

    private static double hueToRgb(double m1, double m2, double h) {
        if (h < 0) {
            h = h + 1;
        }
        if (h > 1) {
            h = h - 1;
        }
        if (h * 6 < 1) {
            return m1 + (m2 - m1) * h * 6;
        }
        if (h * 2 < 1) {
            return m2;
        }
        if (h * 3 < 2) {
            return m1 + (m2 - m1) * (2f / 3 - h) * 6;
        }
        return m1;
    }

    public static LexicalUnitImpl createHexColor( String uri, int line, int column, int[] rgb ) {
        return LexicalUnitImpl.createIdent( uri, line, column, rgbToColorString( rgb ) );
    }

    public static LexicalUnitImpl createRgbaColor( String uri, int line, int column, int red, int green, int blue, double alpha ) {
        LexicalUnitImpl redUnit = LexicalUnitImpl.createNumber( null, line, column, red );
        LexicalUnitImpl greenUnit = LexicalUnitImpl.createNumber( null, line, column, green );
        LexicalUnitImpl blueUnit = LexicalUnitImpl.createNumber( null, line, column, blue );
        LexicalUnitImpl alphaUnit = LexicalUnitImpl.createNumber( null, line, column, alpha );
        ActualArgumentList args = new ActualArgumentList( SassList.Separator.COMMA, redUnit, greenUnit, blueUnit, alphaUnit );
        return LexicalUnitImpl.createFunction( uri, line, column, "rgba", args );
    }

    public static LexicalUnitImpl createHslaColor( double hue, double saturation, double lightness, double alpha, int line, int column ) {
        LexicalUnitImpl hueUnit = LexicalUnitImpl.createNumber( null, line, column, hue );
        LexicalUnitImpl saturationUnit = LexicalUnitImpl.createPercentage( null, line, column, saturation );
        LexicalUnitImpl lightnessUnit = LexicalUnitImpl.createPercentage( null, line, column, lightness );
        LexicalUnitImpl alphaUnit = LexicalUnitImpl.createNumber( null, line, column, alpha );
        ActualArgumentList args = new ActualArgumentList( SassList.Separator.COMMA, hueUnit, saturationUnit, lightnessUnit, alphaUnit );
        return LexicalUnitImpl.createFunction( null, line, column, "hsla", args );
    }

    public static LexicalUnitImpl createHslaOrHslColor( double[] hsl, double alpha, int line, int column ) {
        if (alpha < 1.0f) {
            return createHslaColor(hsl[0], hsl[1], hsl[2], alpha, line, column);
        } else {
            return createHslFunction(hsl[0], hsl[1], hsl[2], line, column);
        }
    }

    /**
     * Creates a hex color if alpha is equal to one. Otherwise creates an RGBA
     * color.
     * 
     * @return An object representing a color.
     */
    public static LexicalUnitImpl createRgbaOrHexColor( int[] rgb, double alpha, int line, int column ) {
        if( alpha < 1.0f ) {
            return createRgbaColor( null, line, column, rgb[0], rgb[1], rgb[2], alpha );
        } else {
            return createHexColor( null, line, column, rgb );
        }
    }

    private static LexicalUnitImpl createHslFunction( double hue, double saturation, double lightness, int ln, int cn ) {
        LexicalUnitImpl hueUnit = LexicalUnitImpl.createNumber( null, ln, cn, hue );
        LexicalUnitImpl saturationUnit = LexicalUnitImpl.createPercentage( null, ln, cn, saturation );
        LexicalUnitImpl lightnessUnit = LexicalUnitImpl.createPercentage( null, ln, cn, lightness );
        ActualArgumentList hslParams = new ActualArgumentList( Separator.COMMA, hueUnit, saturationUnit, lightnessUnit );
        return LexicalUnitImpl.createFunction( null, ln, cn, "hsl", hslParams );
    }

    private static LexicalUnitImpl adjust(LexicalUnitImpl color, double amountByPercent, ColorOperation op) {

        double[] hsl = colorToHsl(color);
        if( hsl == null ) {
            throw new ParseException( "The function adjust requires a color", color );
        }
        if (op == ColorOperation.Darken) {
            hsl[2] = hsl[2] - amountByPercent;
            hsl[2] = hsl[2] < 0 ? 0 : hsl[2];
        } else if (op == ColorOperation.Lighten) {
            hsl[2] = hsl[2] + amountByPercent;
            hsl[2] = hsl[2] > 100 ? 100 : hsl[2];
        }
        double alpha = getAlpha(color);
        return createHslaOrHslColor( hsl, alpha, color.getLineNumber(), color.getColumnNumber() );
    }

    public static LexicalUnitImpl darken(LexicalUnitImpl color, double amount) {
        return adjust(color, amount, ColorOperation.Darken);
    }

    public static LexicalUnitImpl lighten(LexicalUnitImpl color, double amount) {
        return adjust(color, amount, ColorOperation.Lighten);
    }

    private enum ColorOperation {
        Darken, Lighten
    }
}
