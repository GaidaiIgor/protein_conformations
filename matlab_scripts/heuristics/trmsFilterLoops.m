function filteredLoopTrms = ...
    trmsFilterLoops(loopsTrms, minInteratomicDist)
%TRMSFILTERLOOPS pass loops without interatomic distances
% greater than minInteratomicDist value.
%	loopsTrms - loops transformations vector.
%	minInteratomicDist - minimal interatomic distance.
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com
filteredLoopTrms = repmat(struct('trms',[]),length(loopsTrms),1);
for i = 1:length(loopsTrms)
    filteredLoopTrms(i).trms = ...
        trmsFilter(loopsTrms(i).trms,minInteratomicDist);
end

end

