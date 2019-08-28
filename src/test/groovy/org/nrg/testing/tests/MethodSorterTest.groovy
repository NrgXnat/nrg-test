package org.nrg.testing.tests

import org.nrg.testing.unit.UnitTestFilter
import org.nrg.testing.listeners.interceptors.sorters.DefaultMethodSorter
import org.testng.*
import org.testng.annotations.Listeners
import org.testng.annotations.Test

import static org.testng.AssertJUnit.*

@SuppressWarnings('UnnecessaryQualifiedReference') // qualified field required to get groovy to compile
@Listeners(UnitTestFilter)
class MethodSorterTest {

    private static final DefaultMethodSorter INTERCEPTOR = new DefaultMethodSorter()
    public static final String DUMMY = 'dummy'

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void testSingleMethod() {
        final int testId = 1

        assertEquals([UnitTestFilter.getDummyTests(testId).get(0)], INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId)))
    }

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void testClassSort() {
        final int testId = 2

        final IMethodInstance testA1 = UnitTestFilter.getInstance('testDummyA1')
        final IMethodInstance testA2 = UnitTestFilter.getInstance('testDummyA2')
        final IMethodInstance testA3 = UnitTestFilter.getInstance('testDummyA3')
        final IMethodInstance testB1 = UnitTestFilter.getInstance('testDummyB1')
        final IMethodInstance testB2 = UnitTestFilter.getInstance('testDummyB2')
        final List<IMethodInstance> actualSort = INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId))

        assertTrue(actualSort == [testA1, testA2, testA3, testB1, testB2] || actualSort == [testB1, testB2, testA1, testA2, testA3])
    }

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void test1Cycle() {
        final int testId = 3

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId))
            fail('1-cycle did not fail.')
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains('cyclic dependency'))
        }
    }

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void test2Cycle() {
        final int testId = 4

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId))
            fail('2-cycle did not fail.')
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains('cyclic dependency'))
        }
    }

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void testLargerCycle() {
        final int testId = 5

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId))
            fail('4-cycle did not fail.')
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().contains('cyclic dependency'))
        }
    }

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void testLargerNonCycle() {
        // This example: https://www.cs.hmc.edu/~keller/courses/cs60/s98/examples/acyclic/image%20500.gif (plus some disconnected vertices)
        // Sorted order of the above is unique: 1, 2, 4, 5, 6, 3

        final int testId = 6

        final IMethodInstance test1 = UnitTestFilter.getInstance('testDummyNoncycle1')
        final IMethodInstance test2 = UnitTestFilter.getInstance('testDummyNoncycle2')
        final IMethodInstance test3 = UnitTestFilter.getInstance('testDummyNoncycle3')
        final IMethodInstance test4 = UnitTestFilter.getInstance('testDummyNoncycle4')
        final IMethodInstance test5 = UnitTestFilter.getInstance('testDummyNoncycle5')
        final IMethodInstance test6 = UnitTestFilter.getInstance('testDummyNoncycle6')
        final IMethodInstance testA = UnitTestFilter.getInstance('testDummyNoncycleA')
        final IMethodInstance testB = UnitTestFilter.getInstance('testDummyNoncycleB')
        final IMethodInstance testC = UnitTestFilter.getInstance('testDummyNoncycleC')
        final IMethodInstance testD = UnitTestFilter.getInstance('testDummyNoncycleD')

        final List<IMethodInstance> sortedComponents1 = [test1, test2, test4, test5, test6, test3]
        final List<IMethodInstance> sortedComponents2 = [testA, testD, testC, testB]
        final List<IMethodInstance> actualResult = INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId))

        assertTrue(actualResult == sortedComponents1 + sortedComponents2 || actualResult == sortedComponents2 + sortedComponents1)
    }

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void testClass1Cycle() {
        final int testId = 7

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId))
            fail('Test class 1-cycle did not fail')
        } catch (Exception e) {
            assertTrue(e.getMessage().contains('cyclic dependency'))
        }
    }

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void testLargerClassNoncycle() {
        // Topological sort is unique: A -> B -> C -> E -> D
        final int testId = 8

        final IMethodInstance testA = UnitTestFilter.getInstance('testDummyNoncycleClassA')
        final IMethodInstance testB = UnitTestFilter.getInstance('testDummyNoncycleClassB')
        final IMethodInstance testC = UnitTestFilter.getInstance('testDummyNoncycleClassC')
        final IMethodInstance testD = UnitTestFilter.getInstance('testDummyNoncycleClassD')
        final IMethodInstance testE = UnitTestFilter.getInstance('testDummyNoncycleClassE')

        assertEquals([testA, testB, testC, testE, testD], INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId)))
    }

    @Test(dependsOnGroups = MethodSorterTest.DUMMY)
    void testClassCycle() {
        final int testId = 9

        try {
            INTERCEPTOR.orderMethods(UnitTestFilter.getDummyTests(testId))
            fail('Class cycle did not fail to sort.')
        } catch (Exception e) {
            assertTrue(e.getMessage().contains('cyclic dependency'))
        }
    }

}
