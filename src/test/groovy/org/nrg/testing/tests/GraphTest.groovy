package org.nrg.testing.tests

import org.nrg.testing.util.GraphUtils
import org.testng.annotations.Test

import static org.testng.AssertJUnit.*

class GraphTest {

    @Test
    void testDegenerateGraphs() {
        assertEquals(GraphUtils.findMaximalConnectedComponent([:]), 0)

        final Map<Integer, Collection<Integer>> oneNode = [:]
        oneNode.put(1, [])
        assertEquals(GraphUtils.findMaximalConnectedComponent(oneNode), 1)

        final Map<Integer, Collection<Integer>> twoNodes = [:]
        twoNodes.put(1, [])
        twoNodes.put(2, [])
        assertEquals(GraphUtils.findMaximalConnectedComponent(twoNodes), 1)
    }

    @Test
    void testConnectedComponentExample1() {
        final String zero = '0'
        final String one = '1'
        final String two = '2'
        final String three = '3'
        final String four = '4'
        final String five = '5'
        final String six = '6'
        final String seven = '7'

        final Map<String, Collection<String>> graph = [:]
        graph.put(zero, [one, four])
        graph.put(one, [zero, three, four])
        graph.put(two, [four])
        graph.put(three, [one])
        graph.put(four, [zero, one, two])
        graph.put(five, [six])
        graph.put(six, [five])
        graph.put(seven, [])

        final Set<String> cc1 = [zero, one, two, three, four]
        final Set<String> cc2 = [five, six]
        final Set<String> cc3 = [seven]

        final Set<Set<String>> ccs = GraphUtils.findConnectedComponents(graph)
        assertEquals([cc1, cc2, cc3] as Set, ccs)
        assertEquals(cc1.size(), GraphUtils.findMaximalConnectedComponent(graph))
    }

    @Test
    void testConnectedComponentExample2() {
        // graph example from : https://i.stack.imgur.com/A6kof.png
        final Integer five = 5
        final Integer nine = 9
        final Integer eleven = 11
        final Integer thirteen = 13
        final Integer seventeen = 17
        final Integer eighteen = 18
        final Integer nineteen = 19
        final Integer twentyThree = 23
        final Integer twentyFour = 24
        final Integer twentySix = 26

        final Map<Integer, Collection<Integer>> graph = [:]
        graph.put(seventeen, [])
        graph.put(twentyThree, [twentyFour])
        graph.put(twentyFour, [twentyThree])
        graph.put(nine, [twentySix, eighteen, nineteen])
        graph.put(thirteen, [nineteen, five])
        graph.put(twentySix, [nine, eleven, eighteen])
        graph.put(nineteen, [nine, thirteen])
        graph.put(five, [thirteen])
        graph.put(eleven, [twentySix])
        graph.put(eighteen, [twentySix, nine])

        final Set<Integer> cc1 = [seventeen]
        final Set<Integer> cc2 = [twentyThree, twentyFour]
        final Set<Integer> cc3 = [nine, thirteen, twentySix, nineteen, five, eleven, eighteen]

        final Set<Set<Integer>> ccs = GraphUtils.findConnectedComponents(graph)
        assertEquals(ccs, [cc1, cc2, cc3] as Set)
        assertEquals(GraphUtils.findMaximalConnectedComponent(graph), cc3.size())
    }

    @Test
    void test1Cycle() {
        final String cat = 'CAT'
        try {
            GraphUtils.topologicalSort([(cat) : [cat]])
            fail('No CyclicGraphException was thrown for a 1-cycle')
        } catch (GraphUtils.CyclicGraphException cge) {
            assertEquals(cge.getCycle(), [cat]) // TestNG assertEquals does the right thing with lists
        }
    }

    @Test
    void test1Noncycle() {
        final String dog = 'DOG'
        assertEquals([dog], GraphUtils.topologicalSort([('DOG') : []]))
    }

    @Test
    void test2Cycle() {
        // 1 -> 3 -> 1
        final Integer one = 1
        final Integer two = 2
        final Integer three = 3
        try {
            GraphUtils.topologicalSort([
                    (one) : [three],
                    (two) : [],
                    (three) : [one]
            ])
            fail('No CyclicGraphException was thrown for a 2-cycle')
        } catch (GraphUtils.CyclicGraphException cge) {
            assertEquals(cge.getCycle(), [one, three])
        }
    }

    @Test
    void test3NonCycle() {
        // 1 -> 2, 2 -> 3, 1 -> 3
        final Integer one = 1
        final Integer two = 2
        final Integer three = 3
        GraphUtils.topologicalSort([
                (one) : [],
                (two) : [one],
                (three) : [one, two]
        ])
    }

}
