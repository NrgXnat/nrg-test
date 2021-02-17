package org.nrg.testing.util

import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.Graphs
import com.google.common.graph.MutableGraph

/**
 * @author Kevin A. Archie
 * @author Charlie Moore
 * Originally written by Kevin, taken from NRG framework and modified here.
 */
@Deprecated
final class GraphUtils {
    private GraphUtils() {} // prevent instantiation

    /**
     * Indicates that an algorithm that requires a directed acyclic graph (DAG)
     * was instead provided a cyclic graph.
     */
    static class CyclicGraphException extends IllegalArgumentException {
        private final List cycle

        CyclicGraphException(final String msg, final List cycle) {
            super(msg)
            this.cycle = cycle
        }

        /**
         * @return cycle causing the exception.
         */
        List getCycle() {
            cycle
        }
    }

    /**
     * Use Kahn's algorithm for topological sort.
     *
     * @param graph A map where each entry maps from a node to its incoming edge vertices
     * @param <X>   The type of object to be sorted.
     *
     * @return topologically sorted X
     *
     * @throws CyclicGraphException if graph is cyclic
     */
    static <X> List<X> topologicalSort(final Map<X, Collection<X>> graph) throws CyclicGraphException {
        topologicalSort(mapToGraph(true, graph))
    }

    /**
     * Use Kahn's algorithm for topological sort.
     *
     * @param graph Guava graph object
     * @param <X>   The type of object to be sorted.
     *
     * @return topologically sorted List<X>
     *
     * @throws UnsupportedOperationException if graph is not directed
     * @throws CyclicGraphException          if graph is cyclic
     */
    static <X> List<X> topologicalSort(Graph<X> graph) throws CyclicGraphException {
        if (!graph.isDirected()) {
            throw new UnsupportedOperationException('Graph must be directed to be topologically sorted.')
        }
        final MutableGraph<X> copy = Graphs.copyOf(graph)

        final Set<X> resolved = []

        copy.nodes().each { vertex ->
            if (copy.hasEdgeConnecting(vertex, vertex)) {
                throw new CyclicGraphException("Graph is cyclic, with an edge from ${vertex} to itself", [vertex])
            }
            if (copy.inDegree(vertex) == 0) {
                resolved << vertex // Nodes with no incoming edges are trivially resolved.
            }
        }

        final List<X> sorted = []
        while (!resolved.isEmpty()) {
            // Move one element (x) from the resolved bin to the final sorted list.
            final Iterator<X> iterator = resolved.iterator()
            final X x = iterator.next()
            iterator.remove()
            sorted << x

            // All of the elements pointed to by x now have that edge resolved.
            copy.successors(x).collect{ it }.each { successor -> // Make copy of successors Set so we can modify underlying graph without ConcurrentModificationException
                copy.removeEdge(x, successor)
                if (copy.inDegree(successor) == 0) {
                    resolved << successor
                }
            }
            copy.removeNode(x)
        }
        if (copy.nodes().isEmpty()) {
            sorted
        } else {
            throw new CyclicGraphException("Some nodes are in cyclic graph: ${copy.nodes()}", copy.nodes() as List<X>)
        }
    }

    /**
     * Uses DFS to find the connected components of an undirected graph
     * @param graph A map where each entry maps from a node to the set of nodes which form its edges
     * @param <X>   Type of vertices
     * @return      Set of connected components of graph
     */
    static <X> Set<Set<X>> findConnectedComponents(final Map<X, Collection<X>> graph) {
        findConnectedComponents(mapToGraph(false, graph))
    }

    /**
     * Uses DFS to find the connected components of an undirected graph
     * @param graph Guava graph object
     * @param <X>   Type of vertices
     * @return      Set of connected components of graph
     */
    static <X> Set<Set<X>> findConnectedComponents(Graph<X> graph) {
        final Set<X> visitedNodes = []
        graph.nodes().findResults { node ->
            if (node in visitedNodes) {
                null
            } else { // part of not-yet-visited connected component...
                final Set<X> connectedComponent = []
                connectedComponent.addAll(Graphs.reachableNodes(graph, node))
                visitedNodes.addAll(connectedComponent)
                connectedComponent
            }
        } as Set<Set<X>>
    }

    /**
     * Uses findConnectedComponents to find the size of the largest one
     * @param graph A map where each entry maps from a node to the set of nodes which form its edges
     * @param <X>   Type of vertices
     * @return      Largest connected component size
     */
    static <X> int findMaximalConnectedComponent(final Map<X, Collection<X>> graph) {
        findMaximalConnectedComponentSize(findConnectedComponents(graph))
    }

    /**
     * Uses findConnectedComponents to find the size of the largest one
     * @param graph Guava object for the graph
     * @param <X>   Type of vertices
     * @return      Largest connected component size
     */
    static <X> int findMaximalConnectedComponent(Graph<X> graph) {
        findMaximalConnectedComponentSize(findConnectedComponents(graph))
    }

    private static <X> int findMaximalConnectedComponentSize(Set<Set<X>> connectedComponents) {
        (connectedComponents.isEmpty()) ? 0 : connectedComponents.max { it.size() }.size()
    }

    private static <X> MutableGraph<X> mapToGraph(boolean directed, Map<X, Collection<X>> map) {
        final MutableGraph<X> graphObject = (directed ? GraphBuilder.directed() : GraphBuilder.undirected()).allowsSelfLoops(true).build()
        map.keySet().each { node ->
            graphObject.addNode(node)
        }
        map.each { vertex, incomingEdges ->
            incomingEdges.each { incomingEdge ->
                graphObject.putEdge(incomingEdge, vertex)
            }
        }
        graphObject
    }

}

