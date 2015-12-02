function result = getPdbForEdge(edge, edgePath)
if edge(1) > edge(2)
    temp = edge(1);
    edge(1) = edge(2);
    edge(2) = temp;
end
edgeFileName = fullfile(edgePath, [strjoin(arrayfun(@(i) cellstr(num2str(i)), edge), '_') '.pdb']);
result = pdbread(edgeFileName);