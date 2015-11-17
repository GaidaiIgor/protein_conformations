function result = generatePathsNames(startId, endId, ellipseParam, proteinName, suffixes)
names = cell(length(suffixes), 1);
for i = 1:length(suffixes)
    names(i) = cellstr(['path_', num2str(startId), '_', num2str(endId), '_ellipse', num2str(ellipseParam), ...
        '_', proteinName, '_', char(suffixes(i))]);
end
result = names;