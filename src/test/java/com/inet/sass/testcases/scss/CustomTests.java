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
package com.inet.sass.testcases.scss;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.junit.runner.RunWith;

import com.inet.sass.InputSource;
import com.inet.sass.ScssContext;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.function.AbstractFunctionGenerator;
import com.inet.sass.function.SCSSFunctionGenerator;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.StringInterpolationSequence;
import com.inet.sass.testcases.scss.SassTestRunner.TestFactory;
import com.inet.sass.util.ColorUtil;

@RunWith( SassTestRunner.class )
public class CustomTests extends AbstractDirectoryScanningSassTests {

    @Override
    protected URL getResourceURL( String path ) {
        return getResourceURLInternal( path );
    }

    private static URL getResourceURLInternal( String path ) {
        return CustomTests.class.getResource( "/custom" + path );
    }

    @TestFactory
    public static Collection<String> getScssResourceNames() throws URISyntaxException, IOException {
        SCSSFunctionGenerator.registerCustomFunction( new CustomFunctionColorizeImage() );
        return getScssResourceNames( getResourceURLInternal( "" ) );
    }

     @Override
    protected File getCssFile( File scssFile ) throws IOException {
        File cssFile = super.getCssFile( scssFile );
        String javaVersion = System.getProperty( "java.vm.specification.version" );
        if( "1.8".equals( javaVersion ) ) {
            cssFile = new File( cssFile.getPath().replace( ".css", "_1-8.css" ) );
        }
        return cssFile;
    }

    /**
     * Colorize an image and inline it as base64.
     */
    private static class CustomFunctionColorizeImage extends AbstractFunctionGenerator {

        CustomFunctionColorizeImage() {
            super( createArgumentList( new String[] { "url", "main_color", "contrast_color" }, false ), "colorize-image" );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
            try {
                ScssStylesheet stylesheet = context.getStylesheet();
                InputSource source = stylesheet.resolveSource( getParam( actualArguments, "url" ).unquotedString(), stylesheet );
                BufferedImage loadedImage = ImageIO.read( source.getByteStream() );

                // convert the image in a standard color model
                int width = loadedImage.getWidth( null );
                int height = loadedImage.getHeight( null );
                BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
                Graphics2D bGr = image.createGraphics();
                bGr.drawImage( loadedImage, 0, 0, null );
                bGr.dispose();

                SassListItem param = getParam( actualArguments, "main_color" );
                int[] mainColor = ColorUtil.colorToRgb( param.getContainedValue() );
                if( mainColor == null ) {
                    throw new ParseException( "The parameter main_color of the function " + function.getFunctionName() + " must be a valid color: " + param.printState(), param );
                }

                param = getParam( actualArguments, "contrast_color" );
                int[] contrastColor = ColorUtil.colorToRgb( param.getContainedValue() );
                if( contrastColor == null ) {
                    throw new ParseException( "The parameter contrast_color of the function " + function.getFunctionName() + " must be a valid color: " + param.printState(), param );
                }

                final float[] mainColorHsb = Color.RGBtoHSB( mainColor[0], mainColor[1], mainColor[2], null );
                final float[] contrastColorHsb = Color.RGBtoHSB( contrastColor[0], contrastColor[1], contrastColor[2], null );

                // get the pixel data
                WritableRaster raster = image.getRaster();
                DataBufferInt buffer = (DataBufferInt)raster.getDataBuffer();
                int[] data = buffer.getData();

                float[] hsb = new float[3];
                int hsbColor = 0;
                int lastRgb = data[0] + 1;
                for( int i = 0; i < data.length; i++ ) {
                    int rgb = data[i];
                    if( rgb == lastRgb ) {
                        data[i] = hsbColor;
                        continue;
                    }
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    Color.RGBtoHSB( r, g, b, hsb );

                    float[] hsbColorize;
                    if( hsb[1] == 1.0f ) {
                        hsbColorize = hsb;
                        hsb[0] = hsb[0] * 3f / 4f + mainColorHsb[0] / 4f;
                        hsb[1] = hsb[1] * 3f / 4f + mainColorHsb[1] / 4f;
                        hsb[2] = hsb[2] * 3f / 4f + mainColorHsb[2] / 4f;
                    } else {
                        if( hsb[2] == 1.0f ) {
                            hsbColorize = contrastColorHsb;
                        } else {
                            hsbColorize = mainColorHsb;
                        }
                    }
                    lastRgb = rgb;
                    hsbColor = Color.HSBtoRGB( hsbColorize[0], hsbColorize[1], hsbColorize[2] );
                    hsbColor = (rgb & 0xFF000000) | (hsbColor & 0xFFFFFF);
                    data[i] = hsbColor;
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write( image, "PNG", out );

                String base64url = "\"data:image/png;base64," + Base64.getEncoder().encodeToString( out.toByteArray() ) + '\"';
                return LexicalUnitImpl.createURL( function.getUri(), function.getLineNumber(), function.getColumnNumber(), new StringInterpolationSequence( base64url ) );
            } catch( IOException ex ) {
                throw new RuntimeException( ex );
            }
        }
    }
}
