function pathNames = getPathsFullNamesAt(path)
files = dir(path);
pathNames = {};
for file = files'
    extension = getExtension(file.name);
    if strcmp(extension, 'path')
        pathNames(end + 1) = cellstr(fullfile(path, file.name));
    end
end

function result = getExtension(filename)
lastDotIndex = find(filename == '.', 1, 'last');
result = filename(lastDotIndex+1:end);