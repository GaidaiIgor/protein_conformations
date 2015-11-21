function result = sumOriginal(coordscur, coordsprev, m)
result = m .* sum((coordscur - coordsprev).^2, 2);
end