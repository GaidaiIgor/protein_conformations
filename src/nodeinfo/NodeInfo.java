package nodeinfo;

import core.AbstractNode;

public interface NodeInfo<T extends AbstractNode<T>> {
    T getNode();
}
