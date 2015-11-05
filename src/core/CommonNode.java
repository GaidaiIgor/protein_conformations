package core;

public final class CommonNode extends AbstractMappedNode<CommonNode> {
    public CommonNode(int id) {
        this(id, id);
    }

    public CommonNode(int id, int oldId) {
        super(id, oldId);
    }
}
