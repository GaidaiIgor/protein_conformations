function result = interpcirc(start, finish, M, longArc)
%INTERPCIRC Circular interpolation of angles.
%   interpcirc(start, finish, M) returns the matrix that contains circular
%   interpolations between the angles of start and finish vectors. M is the
%   number of interpolated values.
%
% Reference:
%   P. Berens, CircStat: A Matlab Toolbox for Circular Statistics, 
%   Journal of Statistical Software, Volume 31, Issue 10, 2009.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

% Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com

if nargin < 4
    longArc = false;
end

if (~isvector(start) || ~isvector(finish))
    error('circ_interp error: a vector of angles must be specified');
end

result = ...
    repmat((M:-1:1)/(M+1), length(start), 1).*repmat(start, 1, M) + ...
    repmat((1:M)/(M+1), length(finish), 1).*repmat(finish, 1, M);

if ~longArc
% angles that differ more than pi are interpolated in a special way
    I = find(abs(finish - start) > pi);
    if ~isempty(I)
        result(I,:) = repmat(start(I), 1, M) + ...
            repmat((1:M)/(M+1), length(I), 1) .* ...
            repmat(circ_dist(finish(I), start(I)), 1, M);
    end

else
% angles that differ less than pi are interpolated in a special way
    I = find(finish - start <= 0 & finish - start > -pi);
    if ~isempty(I)
        result(I,:) = repmat(start(I), 1, M) + ...
            repmat((1:M)/(M+1), length(I), 1) .* ...
            repmat(circ_dist(finish(I), start(I))+2*pi, 1, M);
    end
    I = find(finish - start > 0 & finish - start < pi);
    if ~isempty(I)
        result(I,:) = repmat(start(I), 1, M) + ...
            repmat((1:M)/(M+1), length(I), 1) .* ...
            repmat(circ_dist(finish(I), start(I))-2*pi, 1, M);
    end
end

result(result < -pi) = result(result < -pi) + 2*pi;
result(result > pi) =  result(result > pi) - 2*pi;

end

