package norswap.skelex.test;

import norswap.skelex.Branch;
import norswap.skelex.MatchTree;
import norswap.skelex.Runner;
import norswap.skelex.Regex;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static norswap.skelex.DSL.*;
import static norswap.utils.Vanilla.list;

public class SkelexTest
{
    // ---------------------------------------------------------------------------------------------

    private static void test_succeed (Regex regex, String input, Object expect)
    {
        Runner runner = new Runner();
        runner.add(regex);

        List<String> linput = input.isEmpty()
            ? Collections.emptyList()
            : Arrays.asList(input.split(""));

        runner.advance(linput);
        Assert.assertEquals(runner.pos(), input.length());

        MatchTree match = runner.matches().longest_tree();
        Assert.assertNotNull(match);
        Assert.assertEquals(match.value(), expect);
    }

    // ---------------------------------------------------------------------------------------------

    private static void test_fail (Regex regex, String input)
    {
        Runner runner = new Runner();
        runner.add(regex);

        List<String> linput = input.isEmpty()
                ? Collections.emptyList()
                : Arrays.asList(input.split(""));

        runner.advance(linput);
        Assert.assertEquals(runner.pos(), input.length());
        MatchTree match = runner.matches().longest_tree();
        Assert.assertNull(match);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_seq()
    {
        test_succeed (seq("a", "b"), "ab", list("a", "b"));
        test_fail    (seq("a", "b"), "");
        test_fail    (seq("a", "b"), "cc");
        test_fail    (seq("a", "b"), "c");
        test_fail    (seq("a", "b"), "ac");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_choice()
    {
        test_succeed (choice("a", "b"), "a", new Branch(0, "a"));
        test_succeed (choice("a", "b"), "b", new Branch(1, "b"));
        test_fail    (choice("a", "b"), "");
        test_fail    (choice("a", "b"), "c");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_maybe()
    {
        test_succeed (maybe("a"), "a", "a");
        test_succeed (maybe("a"), "",  null);
        test_fail    (maybe("a"), "c");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_zeromore()
    {
        test_succeed (zeromore("a"), "aaa", list("a", "a", "a"));
        test_succeed (zeromore("a"), "a",   list("a"));
        test_succeed (zeromore("a"), "",    list());
        test_fail    (zeromore("a"), "b");
        test_fail    (zeromore("a"), "ab");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_onemore()
    {
        test_succeed (onemore("a"), "aaa", list("a", "a", "a"));
        test_succeed (onemore("a"), "a",   list("a"));
        test_fail    (onemore("a"), "");
        test_fail    (onemore("a"), "ab");
    }

    // ---------------------------------------------------------------------------------------------
}
