function result = conformationVicinity(conformation, referencePath)
result = min(arrayfun(@(model) modelRmsd(conformation, model), referencePath));