package core;

import java.util.Scanner;

public final class CommonNode extends AbstractMappedNode<CommonNode> {
    public CommonNode(int id) {
        this(id, id);
    }

    public CommonNode(int id, int oldId) {
        super(id, oldId);
    }

    public static CommonNode readNode(Scanner in, int id) {
        int oldId = in.nextInt();
        return new CommonNode(id, oldId);
    }
}
