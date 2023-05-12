package com.vaadin.sass.testcases.scss;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.sass.AbstractTestBase;
import com.vaadin.sass.internal.ScssStylesheet;

public class MixinExtraParameters extends AbstractTestBase {
    String scss = "/scss/mixin-extra-params.scss";

    @Test
    public void testCompiler() throws Exception {
        ScssStylesheet sheet;
        try {
            sheet = getStyleSheet(scss);
            sheet.compile();
            Assert.fail();
        } catch (AssertionError e) {
            Throwable th = e.getCause();
            Assert.assertEquals(
                    "Substitution error: some actual parameters were not used. Formal parameters: FormalArgumentList[$p1: null], actual parameters: Actual argument list [ArgumentList [foo, bar]] at line 4, column 15, in file mixin-extra-params.scss",
                    th.getMessage());
        }
    }
}
