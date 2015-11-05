function C = cross3d(A, B)
%CROSS3D Simple evalulation of a vector cross product for 3D vectors.
%   C = cross3d(A,B) returns the vector cross product of three-dimensional
%   vectors A and B. This implementation is faster than the standard cross
%   routine.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

C = [A(2)*B(3) - A(3)*B(2), ...
    A(3)*B(1) - A(1)*B(3), ...
	A(1)*B(2) - A(2)*B(1)];

end

