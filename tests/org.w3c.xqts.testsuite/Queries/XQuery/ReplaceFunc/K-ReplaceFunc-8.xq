(:*******************************************************:)
(: Test: K-ReplaceFunc-8                                 :)
(: Written by: Frans Englich                             :)
(: Date: 2006-10-05T18:29:40+02:00                       :)
(: Purpose: A '\' cannot be used to escape whitespace.   :)
(:*******************************************************:)
replace("input", "in", "thisIsInvalid\ ")