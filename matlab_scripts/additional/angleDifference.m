function result = angleDifference(a, b)
answers = [b - a, b - (a - sign(a) * 2 * pi)];
[~, index] = min(abs(answers));
result = answers(index);