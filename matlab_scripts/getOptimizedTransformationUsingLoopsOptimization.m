function [pdbTransformation optimizedTrmWithLoops ...
    optimizedFilteredSortedLoopsTrms] = getOptimizedTransformationUsingLoopsOptimization(...
    pdbBegin, pdbEnd, ...
    conformationsCount, loopMinAngleDiff, loopVicinity, ...
    loopBorderAtomMass, ...
    minBidirectionAngleValue, ...
    minAngleValueChangeToBeOptimized, trmLoopCostFunction, ...
    trmLoopCostFunctionAdditionalParameters, ...
    loopMaxIter, loopTolx, ...
    trmAnglesCountToOptimize,...
    trmCostFunction, trmCostFunctionAdditionalParameters, ...
    maxIter, tolx, ...
    outputPdbPath)
%GETOPTIMIZEDTRANSFORMATIONUSINGLOOPSOPTIMIZATION finds optimal transformation between 2 
%   conformations of one protein.
%	Function create initial transformation. Then extracts loops
%	and optimize them separately. During loops optimization function generates
%	different initial point for every loop. After initial points have being optimized
%	the best ones have being choosen. Choosen loop transformations are inserting into
%	initial transformation. Received transformation have undergone optimization to find
%	final result.
%	pdbBegin - begin pdb conformation.
%	pdbEnd - end pdb conformation.
%	conformationsCount - count of conformations in final transformation.
%	loopMinAngleDiff, loopVicinity, loopBorderAtomMass, minBidirectionAngleValue, ...
%	minAngleValueChangeToBeOptimized, trmLoopCostFunction, ...
%	trmLoopCostFunctionAdditionalParameters, loopMaxIter, loopTolx - see
%		pdbExtractAndOptimizeLoops() function for details.
%	trmAnglesCountToOptimize,trmCostFunction, trmCostFunctionAdditionalParameters, ...
%	maxIter, tolx, outputPdbPath - see trmOptimize for details.
%
% By Sergey Knyazev, 2014.
% sergey.n.knyazev@gmail.com

[optimizedLoopsTrm loopsAnglesIntervals] = pdbExtractAndOptimizeLoops(...
    pdbBegin, pdbEnd, conformationsCount, loopMinAngleDiff,loopVicinity,...
    loopBorderAtomMass, minBidirectionAngleValue, ...
    minAngleValueChangeToBeOptimized, trmLoopCostFunction, ...
    trmLoopCostFunctionAdditionalParameters, 'interior-point', ...
    loopMaxIter, loopTolx, [outputPdbPath 'loops']);
optimizedFilteredLoopsTrms = trmsFilterLoops(optimizedLoopsTrm,1);
optimizedFilteredSortedLoopsTrms = trmsSortLoops(...
    optimizedFilteredLoopsTrms,trmLoopCostFunction,...
    trmLoopCostFunctionAdditionalParameters);
initTrm = trmcreate(pdbBegin, pdbEnd,conformationsCount);
initTrmWithLoops = trmInsertLoops(initTrm,...
    optimizedFilteredSortedLoopsTrms,loopsAnglesIntervals,...
    ones(size(loopsAnglesIntervals,1),1));
optimizedTrmWithLoops = trmOptimize(initTrmWithLoops,...
    trmAnglesCountToOptimize,...
    trmCostFunction, trmCostFunctionAdditionalParameters, ...
    'interior-point',maxIter, tolx);
pdbTransformation = trm2pdb(optimizedTrmWithLoops,pdbBegin);
pdbwrite([outputPdbPath 'result.pdb'], pdbTransformation);
end

