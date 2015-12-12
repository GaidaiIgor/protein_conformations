function result = makePlotData(prefix, suffix, MAPPathPdbPath, calculations, grid, conformationsPerEdge)
assert(calculations(1) == grid(1), 'first grid element must match first calculation point');
result = zeros(length(grid), 3);
result(:, 1) = grid;
MAPPathPdb = pdbread(MAPPathPdbPath);
calcIndex = 1;
rowNum = 0;
for i = grid
    rowNum = rowNum + 1;
    if calcIndex > length(calculations) || i < calculations(calcIndex)
        result(rowNum, 2:end) = result(rowNum - 1, 2:end);
        continue;
    end
    fullName = strjoin({prefix, num2str(i, '%.1f'), suffix}, '_');
    pathPdb = pdbread(fullName);
    result(rowNum, 2:end) = [estimatePathsSimilarity(getPathsAlignMatrix(pathPdb, MAPPathPdb)), ...
        (length(pathPdb.Model) - 1) / (conformationsPerEdge - 1) - 1];
    calcIndex = calcIndex + 1;
end