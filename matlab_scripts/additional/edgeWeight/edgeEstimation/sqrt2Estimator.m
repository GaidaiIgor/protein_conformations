function result = sqrt2Estimator()
result = @(x) (pdbTransformationCost(x, @sumSqrt, @(x) (x .^ 2)));