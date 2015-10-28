package io;

import core.Edge;
import core.Path;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdbWorker {
    public static void pdbForPath(Path graphPath, java.nio.file.Path edgesPath, String outputFileName, String proteinName) {
        try (PrintWriter writer = new PrintWriter(outputFileName)) {
            List<Edge> pathEdges = graphPath.getEdges();
            int modelNumber = 1;
            for (int i = 1; i < pathEdges.size(); ++i) {
                Edge edge = pathEdges.get(i);
                String edgeFileName = correspondingFileName(edge, proteinName);
                java.nio.file.Path edgeFullPath = edgesPath.resolve(edgeFileName);
                try (FileInputStream edgeInputStream = new FileInputStream(edgeFullPath.toString())) {
                    Scanner in = new Scanner(edgeInputStream).useDelimiter("\\Z");
                    String content = in.next();
                    Pattern header = Pattern.compile("(?ms)(.*?)^MODEL");
                    Pattern model = Pattern.compile("(?ms)^MODEL.*?^(.*?^ENDMDL.*?$)");
                    if (i == 1) {
                        Matcher headerMatcher = header.matcher(content);
                        if (headerMatcher.find()) {
                            writer.print(headerMatcher.group(1));
                        } else {
                            throw new RuntimeException(edgeFullPath + " doesn't match header pattern");
                        }
                    }
                    Matcher modelMatcher = model.matcher(content);
                    while (modelMatcher.find()) {
                        writer.format("MODEL%9d%n", modelNumber);
                        modelNumber += 1;
                        writer.println(modelMatcher.group(1));
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String correspondingFileName(Edge edge, String proteinName) {
        int firstId = edge.getFirst().getOldId();
        int secondId = edge.getSecond().getOldId();
        return proteinName + "_" + Integer.min(firstId, secondId) + "_" + Integer.max(firstId, secondId) + "_1.pdb";
    }
}
