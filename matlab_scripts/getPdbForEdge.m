function result = getPdbForEdge(edge, proteinName, edgePath)
firstId = min(edge(1), edge(2));
secondId = max(edge(1), edge(2));
edgeFileName = fullfile(edgePath, [proteinName, '_', num2str(firstId), '_', num2str(secondId), '_', num2str(edge(3)), '.pdb']);
result = pdbread(edgeFileName);