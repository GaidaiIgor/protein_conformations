package core;

public abstract class AbstractMappedNode<T extends AbstractMappedNode> extends AbstractNode<T> {
    private final int oldId;

    public AbstractMappedNode(int id) {
        this(id, id);
    }

    public AbstractMappedNode(int id, int oldId) {
        super(id);
        this.oldId = oldId;
    }

    @Override
    public String toString() {
        return String.format("id: %d, old id: %d", getId(), getOldId());
    }

//    public static Node readNextNode(Scanner in, int nodeId) {
//        int oldId = in.nextInt();
//        return new Node(nodeId, oldId);
//    }

    public int getOldId() {
        return oldId;
    }
}
