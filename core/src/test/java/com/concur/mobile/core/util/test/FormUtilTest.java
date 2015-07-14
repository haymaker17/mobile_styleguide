package com.concur.mobile.core.util.test;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.concur.mobile.core.util.FormUtil;

@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class FormUtilTest {

    @Test
    public void testRegexFoundInString() {
        class RegexTestStruct {

            public String regex;
            public String[] shouldPass;
            public String[] shouldFail;

            public RegexTestStruct(String regex, String[] shouldPass, String[] shouldFail) {
                this.regex = regex;
                this.shouldPass = shouldPass;
                this.shouldFail = shouldFail;
            }
        }

        ArrayList<RegexTestStruct> regexTests = new ArrayList<RegexTestStruct>();

        regexTests.add(new RegexTestStruct("^([C,c]|[E,e]){1}[R,r]{1}[ ]", new String[] { "CR test", "ER test",
                "CR CR", "ER ER" }, new String[] { "CR", "ER", "test CR test", "test ER test", "" }));

        regexTests.add(new RegexTestStruct("^.*[A-Z,a-z]{3}[ ]", new String[] { "Any d4t4, RAD yes!!!", "WiL pass" },
                new String[] { "Nope", "", "Non", "Won't pass", "won't pas" }));

        regexTests.add(new RegexTestStruct("^.*[S,s]", new String[] { "Matches" }, new String[] { "Won't match" }));

        regexTests.add(new RegexTestStruct("^([W,a-t]){4}[ ][i-s]{2}[ ][a-r]{3}", new String[] { "Walt is rad",
                "Wart on ear, yuck" }, new String[] { "" }));

        for (RegexTestStruct regexTest : regexTests) {
            for (String passValue : regexTest.shouldPass) {
                Assert.assertEquals(true, FormUtil.regexFoundInString(regexTest.regex, passValue));
            }
            for (String failValue : regexTest.shouldFail) {
                Assert.assertEquals(false, FormUtil.regexFoundInString(regexTest.regex, failValue));
            }
        }
    }

    @Test
    public void testEmailFormat() {
        Assert.assertEquals(false, FormUtil.isEmailValid(""));
        Assert.assertEquals(false, FormUtil.isEmailValid(" "));
        Assert.assertEquals(false, FormUtil.isEmailValid(null));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc"));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc@"));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc@d"));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc@de."));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc@de.c"));
        Assert.assertEquals(false, FormUtil.isEmailValid(" abc@de.co "));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc@de.co "));
        Assert.assertEquals(false, FormUtil.isEmailValid(" abc@de.co"));
        Assert.assertEquals(false, FormUtil.isEmailValid(" abc@de . co "));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc@de.c.i"));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc@de.co.i"));
        Assert.assertEquals(false, FormUtil.isEmailValid("abc@de#co$i.com"));
        Assert.assertEquals(false, FormUtil.isEmailValid("@something.com"));
        Assert.assertEquals(true, FormUtil.isEmailValid("abc@de.co"));
        Assert.assertEquals(true, FormUtil.isEmailValid("abc@de.c.in"));
        Assert.assertEquals(true, FormUtil.isEmailValid("abc@de.co.in"));
    }
}
