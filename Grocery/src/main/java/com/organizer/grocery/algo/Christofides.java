package com.organizer.grocery.algo;

import com.organizer.grocery.model.Coordinate;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.HierholzerEulerianCycle;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovWeightedPerfectMatching;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.AsSubgraph;

import java.util.*;
import java.util.stream.Collectors;

public class Christofides{

    public static int manhattanDistance(Coordinate p1, Coordinate p2) {
        return Math.abs(p1.x() - p2.x()) + Math.abs(p1.y() - p2.y());
    }

    public static List<Coordinate> calculateOrderRoute(List<Coordinate> locations) {
        if (locations.size() < 3) {
            List<Coordinate> tour = new ArrayList<>(locations);
            tour.add(locations.get(0));
            return tour;
        }
        Graph<Coordinate, DefaultWeightedEdge> completeGraph = createCompleteGraph(locations);

        SpanningTreeAlgorithm<DefaultWeightedEdge> mstAlgorithm = new KruskalMinimumSpanningTree<>(completeGraph);
        SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> mst = mstAlgorithm.getSpanningTree();

        Graph<Coordinate, DefaultWeightedEdge> mstGraph = new AsSubgraph<>(completeGraph, null, mst.getEdges());
        Set<Coordinate> oddDegreeVertices = mstGraph.vertexSet().stream()
                .filter(v -> mstGraph.degreeOf(v) % 2!= 0)
                .collect(Collectors.toSet());

        Graph<Coordinate, DefaultWeightedEdge> oddSubgraph = new AsSubgraph<>(completeGraph, oddDegreeVertices);
        KolmogorovWeightedPerfectMatching<Coordinate, DefaultWeightedEdge> matchingAlgorithm = new KolmogorovWeightedPerfectMatching<>(oddSubgraph);
        MatchingAlgorithm.Matching<Coordinate, DefaultWeightedEdge> perfectMatching = matchingAlgorithm.getMatching();

        Graph<Coordinate, DefaultWeightedEdge> multigraph = new Pseudograph<>(DefaultWeightedEdge.class);
        locations.forEach(multigraph::addVertex);
        mst.getEdges().forEach(edge -> multigraph.addEdge(
                completeGraph.getEdgeSource(edge), completeGraph.getEdgeTarget(edge), edge
        ));
        perfectMatching.getEdges().forEach(edge -> multigraph.addEdge(
                completeGraph.getEdgeSource(edge), completeGraph.getEdgeTarget(edge), edge
        ));

        HierholzerEulerianCycle<Coordinate, DefaultWeightedEdge> eulerianCycleAlgorithm = new HierholzerEulerianCycle<>();
        GraphPath<Coordinate, DefaultWeightedEdge> eulerianCircuit = eulerianCycleAlgorithm.getEulerianCycle(multigraph);

        List<Coordinate> tour = new ArrayList<>();
        Set<Coordinate> visited = new HashSet<>();
        for (Coordinate v : eulerianCircuit.getVertexList()) {
            if (!visited.contains(v)) {
                tour.add(v);
                visited.add(v);
            }
        }
        tour.add(tour.get(0));
        return tour;
    }

    private static Graph<Coordinate, DefaultWeightedEdge> createCompleteGraph(List<Coordinate> locations) {
        Graph<Coordinate, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        locations.forEach(g::addVertex);
        for (int i = 0; i < locations.size(); i++) {
            for (int j = i + 1; j < locations.size(); j++) {
                Coordinate p1 = locations.get(i);
                Coordinate p2 = locations.get(j);
                DefaultWeightedEdge edge = g.addEdge(p1, p2);
                g.setEdgeWeight(edge, manhattanDistance(p1, p2));
            }
        }
        return g;
    }
}

