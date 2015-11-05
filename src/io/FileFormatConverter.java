package io;

import core.Edge;
import core.HierarchicalNode;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Supplier;

public class FileFormatConverter {
    public static void convertFromPseudoCsv(InputStream pseudoCsv, OutputStream graphDescription) {
        Scanner in = new Scanner(pseudoCsv);
        in.nextLine();
        List<Edge> edges = new ArrayList<>();
        Map<Integer, Integer> oldIdToId = new HashMap<>();
        while (in.hasNext()) {
            String nextLine = in.nextLine();
            String[] tokens = nextLine.split(",|_");
            int node1Id = Integer.parseInt(tokens[1]);
            int node2Id = Integer.parseInt(tokens[2]);
            int edgeId = Integer.parseInt(tokens[3].split("\\.")[0]);
            oldIdToId.putIfAbsent(node1Id, oldIdToId.size());
            oldIdToId.putIfAbsent(node2Id, oldIdToId.size());
            double edgeWeight = Double.parseDouble(tokens[4]);
            edges.add(new Edge<>(new HierarchicalNode(oldIdToId.get(node1Id)),
                    new HierarchicalNode(oldIdToId.get(node2Id)), edgeWeight, edgeId));
        }
        Map<Integer, Integer> idToOldId = inverseMap(oldIdToId, TreeMap::new);

        PrintWriter out = new PrintWriter(graphDescription);
        out.println(oldIdToId.size());
        out.println(edges.size());
        idToOldId.values().forEach(out::println);
        edges.forEach(e -> out.format("%d %d %f %d%n", e.getFirst().getId(), e.getSecond().getId(), e.getWeight(), e.getId()));
        out.flush();
    }

    private static <T> Map<T, T> inverseMap(Map<T, T> original, Supplier<Map<T, T>> newMapSupplier) {
        Map<T, T> result = newMapSupplier.get();
        for (Map.Entry<T, T> entry : original.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }
        return result;
    }
}
