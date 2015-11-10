function result = getEdgesNames(edgeFilePath)
edgeFile = fopen(edgeFilePath);
fgetl(edgeFile);
result = {};
while true
    nextLine = fgetl(edgeFile);
    if ~ischar(nextLine)
        break
    end
    lineTokens = strsplit(nextLine, ',');
    fileName = lineTokens(1);
    result(end + 1) = fileName;
end
fclose(edgeFile);