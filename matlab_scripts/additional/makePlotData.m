function result = makePlotData(prefix, parameters, suffix, MAPPathPdb)
i = 1;
result = zeros(length(parameters), 2);
for p = parameters
    fullName = strjoin({prefix, char(p), suffix}, '_');
    pathPdb = pdbread(fullName);
    result(i, :) = [str2double(char(p)), estimatePathsSimilarity(getPathsAlignMatrix(pathPdb, MAPPathPdb))];
    i = i + 1;
end