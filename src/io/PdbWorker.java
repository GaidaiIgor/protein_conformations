//package io;
//
//import core.Edge;
//import core.Path;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Scanner;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class PdbWorker {
//    public static void pdbForPath(Path graphPath, java.nio.file.Path edgesPath, String outputFileName, String proteinName) {
//        try (PrintWriter writer = new PrintWriter(outputFileName)) {
//            List<Edge> pathEdges = graphPath.getEdges();
//            int modelNumber = 1;
//            for (int i = 1; i < pathEdges.size(); ++i) {
//                Edge edge = pathEdges.get(i);
//                String edgeFileName = correspondingFileName(edge, proteinName);
//                java.nio.file.Path edgeFullPath = edgesPath.resolve(edgeFileName);
//                try (FileInputStream edgeInputStream = new FileInputStream(edgeFullPath.toString())) {
//                    Scanner in = new Scanner(edgeInputStream).useDelimiter("\\Z");
//                    String content = in.next();
//                    Pattern headerPattern = Pattern.compile("(?ms)(.*?)^MODEL");
//                    Pattern modelPattern = Pattern.compile("(?ms)^MODEL.*?^(.*?^ENDMDL.*?$)");
//                    List<String> models = new ArrayList<>(10);
//
//                    Matcher modelMatcher = modelPattern.matcher(content);
//                    String firstModel;
//                    if (modelMatcher.find()) {
//                        firstModel = modelMatcher.group(1);
//                    } else {
//                        throw new RuntimeException(edgeFullPath + " doesn't match model pattern");
//                    }
//
//                    if (i == 1) {
//                        Matcher headerMatcher = headerPattern.matcher(content);
//                        if (headerMatcher.find()) {
//                            writer.print(headerMatcher.group(1));
//                        } else {
//                            throw new RuntimeException(edgeFullPath + " doesn't match header pattern");
//                        }
//                        models.add(firstModel);
//                    }
//
//                    while (modelMatcher.find()) {
//                        models.add(modelMatcher.group(1));
//                    }
//                    // reverse model sequence if we walked through edge in the opposite direction
//                    if (edge.getFirst().getOldId() > edge.getSecond().getOldId()) {
//                        Collections.reverse(models);
//                    }
//                    for (String model : models) {
//                        writer.format("MODEL%9d%n", modelNumber);
//                        modelNumber += 1;
//                        writer.println(model);
//                    }
//                }
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String correspondingFileName(Edge edge, String proteinName) {
//        int firstId = edge.getFirst().getOldId();
//        int secondId = edge.getSecond().getOldId();
//        return proteinName + "_" + Integer.min(firstId, secondId) + "_" + Integer.max(firstId, secondId) + "_1.pdb";
//    }
//}
