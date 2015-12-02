function edges = readAllEdges(edgesPath)
files = dir(edgesPath)';
edges = containers.Map();
for file = files(1)
    if strcmp(file.name, '.') || strcmp(file.name, '..')
        continue
    end
    edges(file.name(1:end-4)) = pdbread(fullfile(edgesPath, file.name));
end