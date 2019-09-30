package CalcTest;

import org.junit.Rule;
import org.junit.Test;

import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.harness.junit.Neo4jRule;
import static org.junit.Assert.*;

public class CalcUTest
{
    // This rule starts a Neo4j instance
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()

            // This is the function we want to test
            .withFunction( Calc.class );

    @Test
    public void testSum() throws Throwable
    {
        // This is in a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase
                .driver( neo4j.boltURI() , Config.build().toConfig() ) )
        {
            Session session = driver.session();

            // And given I have a node in the database
            session.run("MERGE (salary:Salary {name:'Salary',type:'sum'})"+
                                        "MERGE (salary)-[:HAS]->(rate:Rate {name: 'rate',type:'data',value:90})"+
                                        "MERGE (salary)-[:HAS]->(value:Hour {name: 'hour', type:'data',value:8})");

             // When
             long result = session.run( "match (salary:Salary) return CalcTest.calc(salary) as result").single().get("result").asLong();

             // Then
             assertEquals( result, 98 );

            session.close();
        }
    }

    @Test
    public void testSumRecursive() throws Throwable
    {
        // This is in a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase
                .driver( neo4j.boltURI() , Config.build().toConfig() ) )
        {
            Session session = driver.session();

            // And given I have a node in the database
            session.run("MERGE (total:Total {name:'Total',type:'sum'})"+
                                        "MERGE (total)-[:HAS]->(salary:Salary {name: 'salary',type:'sum'})"+
                                        "MERGE (total)-[:HAS]->(tax:Tax {name: 'tax', type:'sum'})"+
                                        "MERGE (salary)-[:HAS]->(bonus:Bonus {name: 'bonux',type:'data',value:1000})"+
                                        "MERGE (salary)-[:HAS]->(base:BaseSalary {name: 'base', type:'data',value:2000})"+
                                        "MERGE (tax)-[:HAS]->(cTax:CTax {name: 'CTax',type:'data',value:500})"+
                                        "MERGE (tax)-[:HAS]->(pTax:Ptax {name: 'PTax', type:'data',value:400})");
           
             // When
             long result = session.run( "match (salary:Salary) return CalcTest.calc(salary) as result").single().get("result").asLong();

             // Then
             assertEquals( result, 3000 );

              // When
              result = session.run( "match (salary:Tax) return CalcTest.calc(salary) as result").single().get("result").asLong();

              // Then
              assertEquals( result, 900 );

              result = session.run( "match (salary:Total) return CalcTest.calc(salary) as result").single().get("result").asLong();

              // Then
              assertEquals( result, 3900 );
            session.close();
        }
    }

    @Test
    public void testFormula() throws Throwable
    {
        // This is in a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase
                .driver( neo4j.boltURI() , Config.build().toConfig() ) )
        {
            Session session = driver.session();

            // And given I have a node in the database
            session.run("merge (f:FormulaNode {name:'formula',type:'formula',formula:'(a+b)*2+(c+d)*1.2',parameters:'a:PTax,b:CTax,c:base,d:bonus'})"+
                                        "merge (f)-[:HAS]->(p:Ptax {name:'PTax',type:'data',value:10})"+
                                        "merge (f)-[:HAS]->(c:CTax {name:'CTax',type:'data',value:10})"+
                                        "merge (f)-[:HAS]->(b:BaseSalary {name:'base',type:'data',value:10})"+
                                        "merge (f)-[:HAS]->(bb:Bonus {name:'bonus',type:'data',value:10})");
           
             // When
             Double result = session.run( "match (salary:FormulaNode) return CalcTest.calc(salary) as result").single().get("result").asDouble();

             // Then
             assertEquals( result, Double.parseDouble("64"),0 );

            session.close();
        }
    }

    @Test
    public void testFormula_Parameter_not_match() throws Throwable
    {
        // This is in a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase
                .driver( neo4j.boltURI() , Config.build().toConfig() ) )
        {
            Session session = driver.session();

            // And given I have a node in the database
            session.run("merge (f:FormulaNode {name:'formula',type:'formula',formula:'(a+b)*2+c',parameters:'a:PTax,b:CTax,c:base'})"+
                                        "merge (f)-[:HAS]->(p:Ptax {name:'PTax',type:'data',value:10})"+
                                        "merge (f)-[:HAS]->(c:CTax {name:'CTax',type:'data',value:10})"+
                                        "merge (f)-[:HAS]->(b:BaseSalary {name:'base',type:'data',value:10})"+
                                        "merge (f)-[:HAS]->(bb:Bonus {name:'bonus',type:'data',value:10})");
           
             // When
             Double result = session.run( "match (salary:FormulaNode) return CalcTest.calc(salary) as result").single().get("result").asDouble();

             // Then
             assertEquals( result, Double.parseDouble("50"),0 );

            session.close();
        }
    }

    @Test
    public void test_Formula_and_sum() throws Throwable
    {
        // This is in a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase
                .driver( neo4j.boltURI() , Config.build().toConfig() ) )
        {
            Session session = driver.session();

            // And given I have a node in the database
            session.run("merge (f:FormulaNode {name:'calculation-1',type:'formula',formula:'a+b*2',parameters:'a:Salary,b:Tax'})"+
                            "merge (f)-[:HAS]->(s:Salary {name:'Salary',type:'sum'})"+
                            "merge (f)-[:HAS]->(t:Tax {name:'Tax',type:'sum'})"+
                            "merge (t)-[:HAS]->(p:Ptax {name:'PTax',type:'data',value:10})"+
                            "merge (t)-[:HAS]->(c:CTax {name:'CTax',type:'data',value:10})"+
                            "merge (s)-[:HAS]->(b:BaseSalary {name:'base',type:'data',value:1000})"+
                            "merge (s)-[:HAS]->(bb:Bonus {name:'bonus',type:'data',value:500})");
           
             // When
             Double result = session.run( "match (salary:FormulaNode) return CalcTest.calc(salary) as result").single().get("result").asDouble();

             // Then
             assertEquals( result, Double.parseDouble("1540"),0 );

            session.close();
        }
    }
}