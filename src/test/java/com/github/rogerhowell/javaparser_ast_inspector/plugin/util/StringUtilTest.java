package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilTest {

    @Test
    public void padEndTest0() {
        String input = "ABC";
        String output = StringUtil.padEnd(input, 0);
        Assertions.assertEquals("ABC", output);
    }
    @Test
    public void padEndTest1() {
        String input = "ABC";
        String output = StringUtil.padEnd(input, 1);
        Assertions.assertEquals("ABC", output);
    }
    @Test
    public void padEndTest2() {
        String input = "ABC";
        String output = StringUtil.padEnd(input, 2);
        Assertions.assertEquals("ABC", output);
    }
    @Test
    public void padEndTest3() {
        String input = "ABC";
        String output = StringUtil.padEnd(input, 3);
        Assertions.assertEquals("ABC", output);
    }
    @Test
    public void padEndTest4() {
        String input = "ABC";
        String output = StringUtil.padEnd(input, 4);
        Assertions.assertEquals("ABC ", output);
    }
    @Test
    public void padEndTest5() {
        String input = "ABC";
        String output = StringUtil.padEnd(input, 5);
        Assertions.assertEquals("ABC  ", output);
    }
    @Test
    public void padEndTest10() {
        String input = "ABC";
        String output = StringUtil.padEnd(input, 10);
        Assertions.assertEquals("ABC       ", output);
    }
    @Test
    public void padStartTest0() {
        String input = "ABC";
        String output = StringUtil.padStart(input, 0);
        Assertions.assertEquals("ABC", output);
    }
    @Test
    public void padStartTest1() {
        String input = "ABC";
        String output = StringUtil.padStart(input, 1);
        Assertions.assertEquals("ABC", output);
    }
    @Test
    public void padStartTest2() {
        String input = "ABC";
        String output = StringUtil.padStart(input, 2);
        Assertions.assertEquals("ABC", output);
    }
    @Test
    public void padStartTest3() {
        String input = "ABC";
        String output = StringUtil.padStart(input, 3);
        Assertions.assertEquals("ABC", output);
    }
    @Test
    public void padStartTest4() {
        String input = "ABC";
        String output = StringUtil.padStart(input, 4);
        Assertions.assertEquals(" ABC", output);
    }
    @Test
    public void padStartTest5() {
        String input = "ABC";
        String output = StringUtil.padStart(input, 5);
        Assertions.assertEquals("  ABC", output);
    }
    @Test
    public void padStartTest10() {
        String input = "ABC";
        String output = StringUtil.padStart(input, 10);
        Assertions.assertEquals("       ABC", output);
    }
}
