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

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Christofides {
    public static List<Coordinate> calculateOrderRoute(List<Coordinate> locations) {
        System.out.println("Locations to visit before processing: " + locations);
        if (locations.size() < 3) {
            List<Coordinate> tour = new ArrayList<>(locations);
            tour.add(0, new Coordinate(0, 0));
            return tour;
        }

        Graph<Coordinate, DefaultWeightedEdge> completeGraph = createCompleteGraph(locations);

        SpanningTreeAlgorithm<DefaultWeightedEdge> mstAlgorithm = new KruskalMinimumSpanningTree<>(completeGraph);
        Set<DefaultWeightedEdge> mstEdges = mstAlgorithm.getSpanningTree().getEdges();
        Graph<Coordinate, DefaultWeightedEdge> mstGraph = new AsSubgraph<>(completeGraph, null, mstEdges);
        Set<Coordinate> oddDegreeVertices = mstGraph.vertexSet().stream()
                .filter(v -> mstGraph.degreeOf(v) % 2 != 0)
                .collect(Collectors.toSet());

        Graph<Coordinate, DefaultWeightedEdge> oddSubgraph = new AsSubgraph<>(completeGraph, oddDegreeVertices);
        KolmogorovWeightedPerfectMatching<Coordinate, DefaultWeightedEdge> matchingAlgorithm = new KolmogorovWeightedPerfectMatching<>(oddSubgraph);
        MatchingAlgorithm.Matching<Coordinate, DefaultWeightedEdge> perfectMatching = matchingAlgorithm.getMatching();

        Graph<Coordinate, DefaultWeightedEdge> multigraph = new Pseudograph<>(DefaultWeightedEdge.class);
        for (Coordinate coord : locations) {
            multigraph.addVertex(coord);
        }

        System.out.println("Adding MST edges to multigraph...");
        for (DefaultWeightedEdge edge : mstEdges) {
            System.out.println("Adding mst edge: " + edge);
            Coordinate v1 = completeGraph.getEdgeSource(edge);
            Coordinate v2 = completeGraph.getEdgeTarget(edge);
            multigraph.addEdge(v1, v2);
        }

        for (DefaultWeightedEdge edge : perfectMatching.getEdges()) {
            System.out.println("Adding matching edge: " + edge);
            Coordinate v1 = completeGraph.getEdgeSource(edge);
            Coordinate v2 = completeGraph.getEdgeTarget(edge);
            multigraph.addEdge(v1, v2);
        }

        HierholzerEulerianCycle<Coordinate, DefaultWeightedEdge> eulerianCycleAlgorithm = new HierholzerEulerianCycle<>();
        GraphPath<Coordinate, DefaultWeightedEdge> eulerianCircuit = eulerianCycleAlgorithm.getEulerianCycle(multigraph);
        System.out.println("Successful cycle found!");
        for (Coordinate coord : multigraph.vertexSet()) {
            System.out.println("Vertex: " + coord + ", Degree: " + multigraph.degreeOf(coord));
        }

        List<Coordinate> tour = new ArrayList<>();
        for (int i = 0; i < eulerianCircuit.getVertexList().size(); i++) {
            if (!tour.contains(eulerianCircuit.getVertexList().get(i))) {
                tour.add(eulerianCircuit.getVertexList().get(i));
            }
        }

        if (tour.size() >= 2 && !tour.get(0).equals(tour.get(tour.size() - 1))) {
            tour.add(tour.get(0));
        }
        System.out.println("Tour: " + tour);
        return tour;
    }

    private static Graph<Coordinate, DefaultWeightedEdge> createCompleteGraph(List<Coordinate> locations) {
        Graph<Coordinate, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        locations.forEach(g::addVertex);
        for (int i = 0; i < locations.size(); i++) {
            Coordinate p1 = locations.get(i);
            for (int j = i + 1; j < locations.size(); j++) {
                Coordinate p2 = locations.get(j);
                DefaultWeightedEdge edge = g.addEdge(p1, p2);
                g.setEdgeWeight(edge, HelperFunctions.calculateDistance(p1, p2));
            }
        }
        return g;
    }
}

