function result = groupEdges(edgesPath)
result = containers.Map();
files = dir(edgesPath);
for file = files'
    fileKey = getEdgeNameKey(file.name);
    if (isempty(fileKey))
        % file name doesn't correspond format
        continue
    end
    if result.isKey(fileKey)
        oldValue = result(fileKey);
        oldValue(end + 1) = cellstr(file.name);
        result(fileKey) = oldValue;
    else
        result(fileKey) = cellstr(file.name);
    end
end