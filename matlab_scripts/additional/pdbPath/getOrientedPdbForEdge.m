function result = getOrientedPdbForEdge(edge, proteinName, edgePath)
result = getPdbForEdge(edge, proteinName, edgePath);
if edge(1) > edge(2)
    result.Model = fliplr(result.Model);
end