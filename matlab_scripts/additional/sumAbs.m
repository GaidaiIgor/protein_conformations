function result = sumAbs(coordscur, coordsprev, m)
result = m .* sum(abs(coordscur - coordsprev), 2);
end