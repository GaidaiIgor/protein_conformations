function filteredTrms = trmsFilter(trms, minInteratomicDist)
%TRMFILTER pass transformations without interatomic distances
% greater than minInteratomicDist value.
%	trms - transformations vector.
%	minInteratomicDist - minimal interatomic distance.
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com
n = length(trms);
dist = zeros(n,1);
for i = 1:n
    minconfs = inf;
    if ~mod(i,100)
        fprintf('%d\n',i);
    end
    coords = trmrestorecoords(trms(i));
    for j = 1:length(coords)
        minconfs = min(min(pdist(coords{j})),minconfs);
    end
    dist(i) = minconfs;
end
filteredTrms = trms(dist>minInteratomicDist);
end

