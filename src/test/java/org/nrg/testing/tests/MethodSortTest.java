package org.nrg.testing.tests;

import org.nrg.testing.CommonUtils;
import org.nrg.testing.util.GraphUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class MethodSortTest {

    @Test
    public void test1Cycle() {
        final String cat = "CAT";
        try {
            GraphUtils.topologicalSort(Collections.singletonMap(cat, (Collection<String>)Collections.singleton(cat)));
            Assert.fail("No CyclicGraphException was thrown for a 1-cycle");
        } catch (GraphUtils.CyclicGraphException cge) {
            Assert.assertEquals(cge.getCycle(), Collections.singleton(cat)); // TestNG assertEquals does the right thing with lists
        }
    }

    @Test
    public void test1Noncycle() {
        GraphUtils.topologicalSort(Collections.singletonMap("DOG", (Collection<String>)new HashSet<String>()));
    }

    @Test
    public void test2Cycle() {
        // 1 -> 3 -> 1

        final Integer one = 1;
        final Integer two = 2;
        final Integer three = 3;

        final Map<Integer, Collection<Integer>> twoCycle = new HashMap<>();
        twoCycle.put(one, Collections.singleton(three));
        twoCycle.put(two, new HashSet<Integer>());
        twoCycle.put(three, Collections.singleton(one));
        try {
            GraphUtils.topologicalSort(twoCycle);
            Assert.fail("No CyclicGraphException was thrown for a 2-cycle");
        } catch (GraphUtils.CyclicGraphException cge) {
            Assert.assertEquals(cge.getCycle(), Arrays.asList(one, three));
        }
    }

    @Test
    public void test3NonCycle() {
        // 1 -> 2, 2 -> 3, 1 -> 3

        final Integer one = 1;
        final Integer two = 2;
        final Integer three = 3;
        final Map<Integer, Collection<Integer>> nonCycle = new HashMap<>();
        nonCycle.put(one, new HashSet<Integer>());
        nonCycle.put(two, CommonUtils.newHashSet(one));
        nonCycle.put(three, CommonUtils.newHashSet(one, two));
        GraphUtils.topologicalSort(nonCycle);
    }

}
