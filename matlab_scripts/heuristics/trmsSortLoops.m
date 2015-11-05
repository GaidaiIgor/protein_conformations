function sortedLoopsTrms = trmsSortLoops(loopsTrms, costFunction, ...
    additionalParameters)
%TRMSSORTHINGES sorts loops transformations by costFunction value.
%   loopsTrms - loops transformations to be sorted.
%	costFunction - cost function is used for sorting.
%	additionalParameters - cost function additional parameters.
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com
sortedLoopsTrms = repmat(struct('trms',[]),length(loopsTrms),1);
for i = 1:length(loopsTrms)
    loopsCosts = zeros(length(loopsTrms(i).trms),1);
    for j = 1:length(loopsTrms(i).trms)
        loopsCosts(j) = costFunction(loopsTrms(i).trms(j),[],[], ...
            additionalParameters);
    end
    [a idx] = sort(loopsCosts);
    sortedLoopsTrms(i).trms = loopsTrms(i).trms(idx);
end
end

