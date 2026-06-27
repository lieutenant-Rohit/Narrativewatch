package com.narrativewatch.detection;

import com.narrativewatch.entity.GraphEdgeEntity;
import com.narrativewatch.repository.GraphEdgeRepository;
import lombok.RequiredArgsConstructor;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class GraphScorer {

    private static final Logger log = LoggerFactory.getLogger(GraphScorer.class);
    private static final double COEFFICIENT_THRESHOLD = 0.7;
    private static final int MIN_SUSPICIOUS_ACCOUNTS = 5;

    private final GraphEdgeRepository graphEdgeRepository;

    public GraphClusterResult evaluate() {
        List<GraphEdgeEntity> edges = graphEdgeRepository.findAll();

        if (edges.isEmpty()) {
            log.info("Graph scorer: no edges in database");
            return new GraphClusterResult(0, 0, 0.0, List.of(), false);
        }

        Graph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        Set<String> vertices = new HashSet<>();

        for (GraphEdgeEntity edge : edges) {
            vertices.add(edge.getFromAccount());
            vertices.add(edge.getToAccount());
        }

        for (String v : vertices) {
            graph.addVertex(v);
        }

        for (GraphEdgeEntity edge : edges) {
            String from = edge.getFromAccount();
            String to = edge.getToAccount();
            if (!from.equals(to)) {
                graph.addEdge(from, to);
            }
        }

        List<String> flagged = new ArrayList<>();
        double totalCoefficient = 0;
        int scoredCount = 0;

        for (String v : vertices) {
            Set<DefaultEdge> outgoing = graph.outgoingEdgesOf(v);
            if (outgoing.size() < 2) continue;

            Set<String> neighbors = new HashSet<>();
            for (DefaultEdge e : outgoing) {
                neighbors.add(graph.getEdgeTarget(e));
            }

            long actualEdges = 0;
            List<String> neighborList = new ArrayList<>(neighbors);
            for (int i = 0; i < neighborList.size(); i++) {
                for (int j = i + 1; j < neighborList.size(); j++) {
                    String a = neighborList.get(i);
                    String b = neighborList.get(j);
                    if (graph.containsEdge(a, b) || graph.containsEdge(b, a)) {
                        actualEdges++;
                    }
                }
            }

            int n = neighbors.size();
            long possibleEdges = (long) n * (n - 1) / 2;
            double coefficient = possibleEdges > 0 ? (double) actualEdges / possibleEdges : 0;

            totalCoefficient += coefficient;
            scoredCount++;

            if (coefficient > COEFFICIENT_THRESHOLD) {
                flagged.add(v);
                log.debug("Graph scorer: flagged {} with coefficient {}", v, String.format("%.2f", coefficient));
            }
        }

        double avgCoefficient = scoredCount > 0 ? totalCoefficient / scoredCount : 0;
        boolean fired = flagged.size() >= MIN_SUSPICIOUS_ACCOUNTS;

        if (fired) {
            log.warn("Graph signal fired: {} suspicious accounts (threshold {})",
                    flagged.size(), MIN_SUSPICIOUS_ACCOUNTS);
        }

        log.info("Graph scorer: {} vertices, {} scored, {} flagged, avg coefficient {}",
                vertices.size(), scoredCount, flagged.size(), String.format("%.2f", avgCoefficient));

        return new GraphClusterResult(vertices.size(), flagged.size(), avgCoefficient, flagged, fired);
    }
}
