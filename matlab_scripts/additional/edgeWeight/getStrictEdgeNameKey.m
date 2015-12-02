function result = getStrictEdgeNameKey(edgeName)
result = getEdgeNameKey(edgeName);
firstIndex = find(result == '_', 1);
result = result(firstIndex+1:end);