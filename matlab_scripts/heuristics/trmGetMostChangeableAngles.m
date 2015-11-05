function mostChangeableAngles = trmGetMostChangeableAngles( trm, ...
    consideredAngles, minAngleChangeValue, info)
%TRMGETMOSTCHANGEBLEANGLES returns numbers of angles which change value more
%   than minAngleChangeValue value during transformation
%
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com


if nargin < 3
    minAngleChangeValue = 0;
end

if nargin < 4
    info = false;
else
    info = true;
end

if isempty(consideredAngles)
    consideredAngles = setdiff(1:size(trm.psi, 1), 2:3:size(trm.psi, 1));
end

diff = trm.psi(:,end) - trm.psi(:,1);
diff(diff < -pi) = diff(diff < -pi) + 2*pi;
diff(diff > pi) =  diff(diff > pi) - 2*pi;
diff = abs(diff);
diff = [diff (1:length(diff))'];
diff = flipdim(sortrows(diff),1);
mostChangeableAngles = diff(diff(:,1) > minAngleChangeValue, 2)';
mostChangeableAngles = mostChangeableAngles(...
    ismember(mostChangeableAngles(:),consideredAngles));
if info
    fprintf('Angles that greater than %1.2f\n',minAngleChangeValue);
    diff = diff(ismember(diff(:,2),mostChangeableAngles),:);
    for i = 1:length(diff)
        fprintf('%d.\t%1.2f\t%d\n',i,diff(i,1),diff(i,2))
    end
    fprintf('Initial points count: %d\n',2^length(diff));

end

