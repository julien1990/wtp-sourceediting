(:*******************************************************:)
(: Test: K-SeqExprCast-1247                              :)
(: Written by: Frans Englich                             :)
(: Date: 2006-10-05T18:29:39+02:00                       :)
(: Purpose: 'castable as' involving xs:boolean as source type and xs:gYear as target type should always evaluate to false. :)
(:*******************************************************:)
not(xs:boolean("true") castable as xs:gYear)