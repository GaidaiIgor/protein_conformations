function trms = trmsOptimize(trms, modelNumber, anglesCount, trmFunction, ...
    trmFunctionAdditionalParameters, algorithm, MaxIter,tolx)
%TRMSOPTIMIZE optimizes all transformations in trms vector
%   trms - vector with transformations to be optimized.
%	anglesCount, trmFunction, trmFunctionAdditionalParameters,...
%	algorithm, MaxIter,tolx - see trmOptimize for details.
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com
if isempty(modelNumber)
for i=1:length(trms)
    fprintf('Model %d of %d.\n',i,length(trms));
    trms(i) = trmOptimize(trms(i), anglesCount, trmFunction, ...
    trmFunctionAdditionalParameters, algorithm, MaxIter,tolx);
end
else
    trms = trmOptimize(trms(modelNumber), anglesCount, trmFunction, ...
    trmFunctionAdditionalParameters, algorithm, MaxIter,tolx);
end

