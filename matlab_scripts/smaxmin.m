function value = smaxmin(x, kParam)
%SMAXMIN The smooth approximation of the maximum or minimum.
%   smaxmin(x, kParam) is the smooth approximation of the maximum or
%   minimum of x values. As kParam approaches to positive infinity the
%   function value approaches to the maximum of x. As kParam approaches
%   to negative infinity the value approaches to the minumum of x.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

value = sum(x.*exp(kParam.*x))/sum(exp(kParam.*x));

end