function result = sumSqrt(coordscur, coordsprev, m)
result = m .* sqrt(sum((coordscur - coordsprev).^2, 2));
end