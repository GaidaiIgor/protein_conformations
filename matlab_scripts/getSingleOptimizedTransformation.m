function [pdbTransformation optimizedTrm] = getSingleOptimizedTransformation(...
    pdbBegin, pdbEnd, conformationsCount, trmAnglesCountToOptimize,...
    trmCostFunction, trmCostFunctionAdditionalParameters, ...
    maxIter, tolx, ...
    outputPdbPath)
%GETSINGLEOPTIMIZEDTRANSFORMATION finds optimal transformation between 2 conformations
%	of one protein.
%	Function create initial transformation. Received transformation have undergone
%   optimization to find final result.
%	pdbBegin - begin pdb conformation.
%	pdbEnd - end pdb conformation.
%	conformationsCount - count of conformations in final transformation.
%	trmAnglesCountToOptimize,trmCostFunction, trmCostFunctionAdditionalParameters, ...
%	maxIter, tolx, outputPdbPath - see trmOptimize for details.
%
% By Sergey Knyazev, 2014.
% sergey.n.knyazev@gmail.com

initTrm = trmcreate(pdbBegin, pdbEnd,conformationsCount);
optimizedTrm = trmOptimize(initTrm, trmAnglesCountToOptimize,...
    trmCostFunction, trmCostFunctionAdditionalParameters, ...
    'interior-point',maxIter, tolx);
pdbTransformation = trm2pdb(optimizedTrm,pdbBegin);
pdbwrite([outputPdbPath 'result.pdb'], pdbTransformation);
end

