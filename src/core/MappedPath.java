package core;

import java.io.IOException;
import java.io.PrintWriter;

public class MappedPath<T extends AbstractMappedNode<T>> {
    private Path<T> path;

    public MappedPath(Path<T> path) {
        this.path = path;
    }

    public String toStringOldId() {
        StringBuilder builder = new StringBuilder();
        builder.append("void");
        path.getEdges().forEach(e -> builder.append(" -> ").append(e.getSecond().getOldId()));
        builder.append("; total nodes: ").append(path.getTotalNodes());
        builder.append(String.format("; total weight: %.0f", path.getTotalWeight()));
        return builder.toString();
    }

    public void export(String filename) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < path.getEdges().size(); ++i) {
            Edge<T> e = path.getEdges().get(i);
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
