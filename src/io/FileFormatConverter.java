package io;

import core.Edge;
import core.Node;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class FileFormatConverter {
    public static void convert_from_pseudo_csv_to_adequate_graph_description(InputStream pseudo_csv, OutputStream graph_description) {
        Scanner in = new Scanner(pseudo_csv);
        in.nextLine();
        List<Edge> edges = new ArrayList<>();
        Map<Integer, Integer> id_map = new HashMap<>();
        while (in.hasNext()) {
            String next_line = in.nextLine();
            String[] tokens = next_line.split("(,|_)");
            int node1_id = Integer.parseInt(tokens[1]);
            int node2_id = Integer.parseInt(tokens[2]);
            id_map.putIfAbsent(node1_id, id_map.size());
            id_map.putIfAbsent(node2_id, id_map.size());
            double edge_weight = Double.parseDouble(tokens[4]);
            edges.add(new Edge(new Node(id_map.get(node1_id)), new Node(id_map.get(node2_id)), edge_weight));
        }

        PrintWriter out = new PrintWriter(graph_description);
        out.println(id_map.size());
        out.println(edges.size());
        edges.forEach(e -> out.format("%d %d %f%n", e.first.id, e.second.id, e.weight));
        out.flush();
    }
}
