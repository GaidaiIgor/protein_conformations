function [costs] = trmsGetCosts(trms, costFunction, additionalParameters)
%TRMSGETCOSTS return costs of transformations.
%	trms - vector of transformations.
%	costFunction - function to calculate cost of transformations.
%	additionalParameters - cost function additional parameters.
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com
n = length(trms);
costs = zeros(n,1);
for i=1:n
    costs(i) = costFunction(trms(i),[],trms(i).psi(:,2:end-1) ...
        ,additionalParameters);
end

