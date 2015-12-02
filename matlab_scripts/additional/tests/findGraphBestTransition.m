function result = findGraphBestTransition(maxId, edgePath)
result = zeros(maxId * (maxId - 1) * (maxId - 2) / 2, 4);
rowCount = 1;
for i = 1:maxId
    for j = 1:maxId
        if i == j
            continue
        end
        edgeij = getOrientedPdbForEdge([i j], edgePath);
        for k = i+1:maxId
            if k == j
                continue
            end
            fprintf('%d %d %d\n', i, j, k);
            edgejk = getOrientedPdbForEdge([j k], edgePath);
            score = conformationsTransitionScore(edgeij, edgejk);
            result(rowCount, :) = [i, j, k, score];
            rowCount = rowCount + 1;
        end
    end
end