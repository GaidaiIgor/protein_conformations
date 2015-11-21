function result = getEdgeNameKey(edgeName)
lastIndex = find(edgeName == '_', 1, 'last');
result = edgeName(1:lastIndex - 1);