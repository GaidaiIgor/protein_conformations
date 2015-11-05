package core;

public final class HierarchicalNode extends AbstractMappedNode<HierarchicalNode> {
    private final HierarchicalGraph underlyingGraph;

    public HierarchicalNode(int id) {
        this(id, null, id);
    }

    public HierarchicalNode(int id, HierarchicalGraph underlyingGraph, int oldId) {
        super(id, oldId);
        this.underlyingGraph = underlyingGraph;
    }

    public static HierarchicalNode forkOther(int id, HierarchicalNode other) {
        return new HierarchicalNode(id, other.getUnderlyingGraph(), other.getId());
    }

    public HierarchicalGraph getUnderlyingGraph() {
        return underlyingGraph;
    }

    @Override
    public String toString() {
        return String.format("id: %d, old id: %d, size: %d", getId(), getOldId(), getSize());
    }

    public int getSize() {
        if (underlyingGraph == null) {
            return 1;
        }
        return underlyingGraph.getTotalSize();
    }
}