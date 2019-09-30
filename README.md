# neo4j-udf
 An UDF for formula calculations and sum aggregations
 
 using cypher to do the calculations
 
 match (salary:FormulaNode) return CalcTest.calc(salary) as result
 
 Using sbesada/java.math.expression.parser to parse formula, it's my first java project, dont know how import the lib, sorry to copy the src into my project
