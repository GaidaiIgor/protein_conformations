package core;

import java.io.IOException;
import java.io.PrintWriter;

public class MappedPath<T extends AbstractMappedNode<T>> extends Path<T> {
    public static <T extends AbstractMappedNode<T>> MappedPath<T> fromPath(Path<T> path) {
        return (MappedPath<T>) path;
    }

    public String toStringOldId() {
        StringBuilder builder = new StringBuilder();
        builder.append("void");
        getEdges().forEach(e -> builder.append(" -> ").append(e.getSecond().getOldId()));
        builder.append("; total nodes: ").append(getTotalNodes());
        builder.append(String.format("; total weight: %.0f", getTotalWeight()));
        return builder.toString();
    }

    public void export(String filename) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < getEdges().size(); ++i) {
            Edge<T> e = getEdges().get(i);
            builder.append(String.format("%d %d %d%n", e.getFirst().getOldId(), e.getSecond().getOldId(), e.getId()));
        }
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.write(builder.toString());
        }
        catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
