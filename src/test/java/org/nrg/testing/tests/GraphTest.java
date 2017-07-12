package org.nrg.testing.tests;

import org.nrg.testing.CommonUtils;
import org.nrg.testing.util.GraphUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class GraphTest {

    @Test
    public void testDegenerateGraphs() {
        Assert.assertEquals(GraphUtils.findMaximalConnectedComponent(new HashMap<Integer, Collection<Integer>>()), 0);

        final Map<Integer, Collection<Integer>> oneNode = new HashMap<>();
        oneNode.put(1, new HashSet<Integer>());
        Assert.assertEquals(GraphUtils.findMaximalConnectedComponent(oneNode), 1);

        final Map<Integer, Collection<Integer>> twoNodes = new HashMap<>();
        twoNodes.put(1, new HashSet<Integer>());
        twoNodes.put(2, new HashSet<Integer>());
        Assert.assertEquals(GraphUtils.findMaximalConnectedComponent(twoNodes), 1);
    }

    @Test
    public void testConnectedComponentExample1() {
        // Graph example from: http://www.ida.liu.se/opendsa/OpenDSA/Books/TDDD86_2014/html/_images/ConCom.png
        final String zero = "0";
        final String one = "1";
        final String two = "2";
        final String three = "3";
        final String four = "4";
        final String five = "5";
        final String six = "6";
        final String seven = "7";

        final Map<String, Collection<String>> graph = new HashMap<>();
        graph.put(zero, CommonUtils.newHashSet(one, four));
        graph.put(one, CommonUtils.newHashSet(zero, three, four));
        graph.put(two, CommonUtils.newHashSet(four));
        graph.put(three, CommonUtils.newHashSet(one));
        graph.put(four, CommonUtils.newHashSet(zero, one, two));
        graph.put(five, CommonUtils.newHashSet(six));
        graph.put(six, CommonUtils.newHashSet(five));
        graph.put(seven, new HashSet<String>());

        final Set<String> cc1 = CommonUtils.newHashSet(zero, one, two, three, four);
        final Set<String> cc2 = CommonUtils.newHashSet(five, six);
        final Set<String> cc3 = CommonUtils.newHashSet(seven);

        final Set<Set<String>> ccs = GraphUtils.findConnectedComponents(graph);
        Assert.assertEquals(ccs, CommonUtils.newHashSet(cc1, cc2, cc3));
        Assert.assertEquals(GraphUtils.findMaximalConnectedComponent(graph), cc1.size());
    }

    @Test
    public void testConnectedComponentExample2() {
        // graph example from : https://i.stack.imgur.com/A6kof.png
        final Integer five = 5;
        final Integer nine = 9;
        final Integer eleven = 11;
        final Integer thirteen = 13;
        final Integer seventeen = 17;
        final Integer eighteen = 18;
        final Integer nineteen = 19;
        final Integer twentyThree = 23;
        final Integer twentyFour = 24;
        final Integer twentySix = 26;

        final Map<Integer, Collection<Integer>> graph = new HashMap<>();
        graph.put(seventeen, new HashSet<Integer>());
        graph.put(twentyThree, CommonUtils.newHashSet(twentyFour));
        graph.put(twentyFour, CommonUtils.newHashSet(twentyThree));
        graph.put(nine, CommonUtils.newHashSet(twentySix, eighteen, nineteen));
        graph.put(thirteen, CommonUtils.newHashSet(nineteen, five));
        graph.put(twentySix, CommonUtils.newHashSet(nine, eleven, eighteen));
        graph.put(nineteen, CommonUtils.newHashSet(nine, thirteen));
        graph.put(five, CommonUtils.newHashSet(thirteen));
        graph.put(eleven, CommonUtils.newHashSet(twentySix));
        graph.put(eighteen, CommonUtils.newHashSet(twentySix, nine));

        final Set<Integer> cc1 = CommonUtils.newHashSet(seventeen);
        final Set<Integer> cc2 = CommonUtils.newHashSet(twentyThree, twentyFour);
        final Set<Integer> cc3 = CommonUtils.newHashSet(nine, thirteen, twentySix, nineteen, five, eleven, eighteen);

        final Set<Set<Integer>> ccs = GraphUtils.findConnectedComponents(graph);
        Assert.assertEquals(ccs, CommonUtils.newHashSet(cc1, cc2, cc3));
        Assert.assertEquals(GraphUtils.findMaximalConnectedComponent(graph), cc3.size());
    }

}
