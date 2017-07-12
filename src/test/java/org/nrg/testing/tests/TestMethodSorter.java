package org.nrg.testing.tests;

import org.nrg.testing.UnitTestFilter;
import org.nrg.testing.listeners.interceptors.sorters.DefaultMethodSorter;
import org.testng.*;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.AssertJUnit.*;

@Listeners(UnitTestFilter.class)
public class TestMethodSorter {

    private static final DefaultMethodSorter INTERCEPTOR = new DefaultMethodSorter();

    @Test(dependsOnGroups = "dummy")
    public void testSingleMethod() {
        final int testId = 1;

        final IMethodInstance singleInstance = UnitTestFilter.getDummyTests(testId).get(0);
        assertEquals(Collections.singletonList(singleInstance), INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId)));
    }

    @Test(dependsOnGroups = "dummy")
    public void testClassSort() {
        final int testId = 2;

        final IMethodInstance testA1 = UnitTestFilter.getInstance("testDummyA1");
        final IMethodInstance testA2 = UnitTestFilter.getInstance("testDummyA2");
        final IMethodInstance testA3 = UnitTestFilter.getInstance("testDummyA3");
        final IMethodInstance testB1 = UnitTestFilter.getInstance("testDummyB1");
        final IMethodInstance testB2 = UnitTestFilter.getInstance("testDummyB2");
        final List<IMethodInstance> possibleSort = Arrays.asList(testA1, testA2, testA3, testB1, testB2);
        final List<IMethodInstance> otherPossibleSort = Arrays.asList(testB1, testB2, testA1, testA2, testA3);
        final List<IMethodInstance> actualSort = INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId));

        Assert.assertTrue(actualSort.equals(possibleSort) || actualSort.equals(otherPossibleSort));
    }

    @Test(dependsOnGroups = "dummy")
    public void test1Cycle() {
        final int testId = 3;

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId));
            Assert.fail("1-cycle did not fail.");
        } catch (RuntimeException re) {
            Assert.assertTrue(re.getMessage().contains("circular dependency"));
        }
    }

    @Test(dependsOnGroups = "dummy")
    public void test2Cycle() {
        final int testId = 4;

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId));
            Assert.fail("2-cycle did not fail.");
        } catch (RuntimeException re) {
            Assert.assertTrue(re.getMessage().contains("circular dependency"));
        }
    }

    @Test(dependsOnGroups = "dummy")
    public void testLargerCycle() {
        // This example: https://qph.ec.quoracdn.net/main-qimg-d575a41ce0ca5f8e07d417e3e733a310
        final int testId = 5;

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId));
            Assert.fail("4-cycle did not fail.");
        } catch (RuntimeException re) {
            Assert.assertTrue(re.getMessage().contains("circular dependency"));
        }
    }

    @Test(dependsOnGroups = "dummy")
    public void testLargerNonCycle() {
        // This example: https://www.cs.hmc.edu/~keller/courses/cs60/s98/examples/acyclic/image%20500.gif (plus some disconnected vertices)
        // Sorted order of the above is unique: 1, 2, 4, 5, 6, 3

        final int testId = 6;

        final IMethodInstance test1 = UnitTestFilter.getInstance("testDummyNoncycle1");
        final IMethodInstance test2 = UnitTestFilter.getInstance("testDummyNoncycle2");
        final IMethodInstance test3 = UnitTestFilter.getInstance("testDummyNoncycle3");
        final IMethodInstance test4 = UnitTestFilter.getInstance("testDummyNoncycle4");
        final IMethodInstance test5 = UnitTestFilter.getInstance("testDummyNoncycle5");
        final IMethodInstance test6 = UnitTestFilter.getInstance("testDummyNoncycle6");
        final IMethodInstance testA = UnitTestFilter.getInstance("testDummyNoncycleA");
        final IMethodInstance testB = UnitTestFilter.getInstance("testDummyNoncycleB");
        final IMethodInstance testC = UnitTestFilter.getInstance("testDummyNoncycleC");
        final IMethodInstance testD = UnitTestFilter.getInstance("testDummyNoncycleD");

        final List<IMethodInstance> possibleResult1 = Arrays.asList(test1, test2, test4, test5, test6, test3, testA, testD, testC, testB);
        final List<IMethodInstance> possibleResult2 = Arrays.asList(testA, testD, testC, testB, test1, test2, test4, test5, test6, test3);
        final List<IMethodInstance> actualResult = INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId));

        Assert.assertTrue(actualResult.equals(possibleResult1) || actualResult.equals(possibleResult2));
    }

    @Test(dependsOnGroups = "dummy")
    public void testClass1Cycle() {
        final int testId = 7;

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId));
            Assert.fail("Test class 1-cycle did not fail");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("cyclic dependency"));
        }
    }

    @Test(dependsOnGroups = "dummy")
    public void testLargerClassNoncycle() {
        // example from http://www.codediesel.com/wp-content/uploads/2012/02/d-graph4.gif
        // Topological sort is unique: A -> B -> C -> E -> D

        final int testId = 8;

        final IMethodInstance testA = UnitTestFilter.getInstance("testDummyNoncycleClassA");
        final IMethodInstance testB = UnitTestFilter.getInstance("testDummyNoncycleClassB");
        final IMethodInstance testC = UnitTestFilter.getInstance("testDummyNoncycleClassC");
        final IMethodInstance testD = UnitTestFilter.getInstance("testDummyNoncycleClassD");
        final IMethodInstance testE = UnitTestFilter.getInstance("testDummyNoncycleClassE");

        final List<IMethodInstance> wellSorted = Arrays.asList(testA, testB, testC, testE, testD);
        assertEquals(wellSorted, INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId)));
    }

    @Test(dependsOnGroups = "dummy")
    public void testClassCycle() {
        final int testId = 9;

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId));
            Assert.fail("Class cycle did not fail to sort.");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("cyclic dependency"));
        }
    }

}
