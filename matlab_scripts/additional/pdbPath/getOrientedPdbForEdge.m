function result = getOrientedPdbForEdge(edge, edgePath)
result = getPdbForEdge(edge, edgePath);
if edge(1) > edge(2)
    result.Model = fliplr(result.Model);
end