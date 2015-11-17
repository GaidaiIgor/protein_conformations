function result = sqrtEstimator()
result = @(x) (pdbTransformationCost(x, @sumSqrt, @(x) (x)));