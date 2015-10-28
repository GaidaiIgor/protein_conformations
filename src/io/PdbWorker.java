package io;

import core.Edge;
import core.Path;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class PdbWorker {
//    private static String correspondingFileName(Edge edge) {
//        return "1CFC_" + edge.
//    }

    public static void pdbForPath(Path graphPath) {
        pdbForPath(graphPath, "edges\\", "out.pdb");
    }

    public static void pdbForPath(Path graphPath, String edgesPdbPath, String outputFileName) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outputFileName);
            List<Edge> pathEdges = graphPath.getEdges();
            for (int i = 1; i < pathEdges.size(); ++i) {
                Edge edge = pathEdges.get(i);

            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
