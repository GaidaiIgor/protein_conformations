function value = smaxmin(x, alpha)
%SMAXMIN The smooth approximation of the maximum or minimum.
%   smaxmin(x, alpha) is the smooth approximation of the maximum or 
%   minimum of x values. As alpha approaches to positive infinity the
%   function value approaches to the maximum of x. As alpha approaches
%   to negative infinity the value approaches to the minumum of x.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

value = sum(x.*exp(alpha.*x))/sum(exp(alpha.*x));

end