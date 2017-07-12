package org.nrg.testing.util;

import java.util.*;

/**
 * @author Kevin A. Archie
 * @author Charlie Moore
 * Originally written by Kevin, taken from NRG framework and modified here.
 */
public final class GraphUtils {
    private GraphUtils() {} // prevent instantiation

    /**
     * Indicates that an algorithm that requires a directed acyclic graph (DAG)
     * was instead provided a cyclic graph.
     */
    public static class CyclicGraphException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final List cycle;

        CyclicGraphException(final String msg, final List cycle) {
            super(msg);
            this.cycle = cycle;
        }

        /**
         * @return cycle causing the exception.
         */
        public List getCycle() {
            return cycle;
        }
    }


    /**
     * Use Kahn's algorithm for topological sort. Removes sorted copy of input graph.
     *
     * @param graph A map where each entry maps from a node to its incoming edges
     * @param <X>   The type of object to be sorted.
     *
     * @return topologically sorted X
     *
     * @throws CyclicGraphException if graph is cyclic
     */
    public static <X> List<X> topologicalSort(final Map<X, Collection<X>> graph) throws CyclicGraphException {
        // Nodes with no incoming edges are trivially resolved.
        final Map<X, Collection<X>> copy = new HashMap<>(graph);
        final Set<X> resolved = new LinkedHashSet<>();

        for (Map.Entry<X, Collection<X>> entry : copy.entrySet()) {
            final X vertex = entry.getKey();
            final Collection<X> edges = entry.getValue();
            if (edges.contains(vertex)) {
                throw new CyclicGraphException(String.format("Graph is cyclic, with an edge from %s to itself", vertex), Collections.singletonList(vertex));
            }
            if (edges.isEmpty()) {
                resolved.add(vertex);
            }
        }

        copy.keySet().removeAll(resolved);

        final List<X> sorted = new ArrayList<>(copy.size());
        while (!resolved.isEmpty()) {
            // Move one element (x) from the resolved bin to the final sorted list.
            final Iterator<X> iterator = resolved.iterator();
            final X x = iterator.next();
            iterator.remove();
            sorted.add(x);

            // All of the elements pointed to by x now have that edge resolved.
            for (final Iterator<Map.Entry<X, Collection<X>>> mei = copy.entrySet().iterator(); mei.hasNext(); ) {
                final Map.Entry<X, Collection<X>> me = mei.next();
                final Collection<X> incoming = me.getValue();
                incoming.remove(x);
                if (incoming.isEmpty()) {
                    resolved.add(me.getKey());
                    mei.remove();
                }
            }
        }

        if (copy.isEmpty()) {
            return sorted;
        } else {
            throw new CyclicGraphException("Some nodes are in cyclic graph: " + copy.keySet(), new ArrayList<>(copy.keySet()));
        }
    }

    /**
     * Uses DFS to find the connected components of an undirected graph
     * @param graph A map where each entry maps from a node to the set of nodes which form its edges
     * @param <X>   Type of vertices
     * @return Set of connected components of graph
     */
    public static <X> Set<Set<X>> findConnectedComponents(final Map<X, Collection<X>> graph) {
        final Set<Set<X>> connectedComponents = new HashSet<>();
        final Set<X> visitedNodes = new HashSet<>();
        for (X node : graph.keySet()) {
            if (!visitedNodes.contains(node)) { // part of not-yet-visited connected component...
                visitedNodes.add(node);
                final Set<X> connectedComponent = new HashSet<>();
                connectedComponent.add(node);
                connectedComponent.addAll(getAdjacentNodes(node, graph, visitedNodes));
                connectedComponents.add(connectedComponent);
            }
        }
        return connectedComponents;
    }

    /**
     * Uses findConnectedComponents to find the size of the largest one
     * @param graph A map where each entry maps from a node to its edges
     * @param <X>   Type of vertices
     * @return Largest connected component size
     */
    public static <X> int findMaximalConnectedComponent(final Map<X, Collection<X>> graph) {
        int max = 0;
        for (Set<X> connectedComponent : findConnectedComponents(graph)) {
            max = Math.max(max, connectedComponent.size());
        }
        return max;
    }

    private static <X> Set<X> getAdjacentNodes(X node, Map<X, Collection<X>> graph, Set<X> visited) {
        final Set<X> adjacentNodes = new HashSet<>();
        for (X adjacentNode : graph.get(node)) {
            if (!visited.contains(adjacentNode)) {
                visited.add(adjacentNode);
                adjacentNodes.add(adjacentNode);
                adjacentNodes.addAll(getAdjacentNodes(adjacentNode, graph, visited));
            }
        }
        return adjacentNodes;
    }

}

