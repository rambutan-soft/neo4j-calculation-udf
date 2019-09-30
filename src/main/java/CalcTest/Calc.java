package CalcTest;

// import apoc.create.Create;
// import apoc.refactor.util.PropertiesManager;
// import apoc.refactor.util.RefactorConfig;
// import apoc.result.*;
// import apoc.util.Util;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.internal.kernel.api.*;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.procedure.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// import static apoc.path.RelationshipTypeAndDirections.format;
// import static apoc.path.RelationshipTypeAndDirections.parse;
// import static apoc.refactor.util.RefactorUtil.copyProperties;
// import static apoc.util.Util.map;
import CalcTest.parser.util.*;
import CalcTest.parser.*;


/**
 * This is an example how you can create a simple user-defined function for Neo4j.
 */
public class Calc
{
    

    @UserFunction
    @Description("CalcTest.Calc(Node) - UDF for calculation-sum.")
    public long calc(@Name("Node") Node node) {    

        if (!node.hasProperty("type")) 
        {
            return -1;  //throw exception
        }
        switch (node.getProperty("type").toString())
        {
            case "sum":
                return sum_node(node);
            case "formula":
                return formula_node(node);
            default:
                return -1;
        }
        

        //Map<String, Object> nodeProperties = node.getAllProperties();
        
        //if (node == null) return 0;
        // String q="With {node} as n"+
        //     "match (n)-[*1]->(children) return children";
        // Map<String, Object> params = new HashMap<>(1);
        // params.put("node", node);
       
        // Result result = node.getGraphDatabase().execute(q, params);
        // long sum=0;
        // while (result.hasNext()) {
        //     Node child=(Node)result.next().get("children");
        //     sum=sum+(long)child.getProperty("value",0);
        // }
        // return sum;

        // Read dataRead = ktx.dataRead();
        // //TokenRead tokenRead = ktx.tokenRead();
        // CursorFactory cursors = ktx.cursors();

        // try (NodeCursor startNodeCursor = cursors.allocateNodeCursor()) 
        // {
        //     dataRead.singleNode(node.getId(), startNodeCursor);
        //     if (startNodeCursor.next()) {
        //         //boolean startDense = startNodeCursor.isDense();
        //         //startNodeCursor.
        //     }
            
        // }
        //return 0;
    }
    private long sum_node(Node node)
    {
        if (node.hasProperty("type") && node.getProperty("type").equals("data")) 
        return (long)node.getProperty("value");

        if (node.hasRelationship(Direction.OUTGOING))
        {
            List<Relationship> rel= Iterables.asList(node.getRelationships(Direction.OUTGOING));
            long sum=0;
            for(Relationship r: rel)
            {
                if (r.getOtherNode(node).hasRelationship(Direction.OUTGOING))
                {
                    sum=sum+calc(r.getOtherNode(node));
                }
                else
                {
                    sum=sum+(long)r.getOtherNode(node).getProperty("value");
                }
                
            }
            return sum;
        }
        return -1;
    }

    private long formula_node(Node node)
    {
        if (node.hasProperty("type") && node.getProperty("type").equals("data")) 
            return (long)node.getProperty("value");

        if (node.hasRelationship(Direction.OUTGOING))
        {
            String f_xs = node.getProperty("formula").toString();
            String[] ps = node.getProperty("parameters").toString().toLowerCase().split(",");
            String[] pList=new String[ps.length];
            Double[] valueList=new Double[ps.length];
            int i=0;
            List<Relationship> rel= Iterables.asList(node.getRelationships(Direction.OUTGOING));
            if (pList.length>rel.size())
            {
                return -1;
            }

            for(Relationship r: rel)
            {
                Node childNode=r.getOtherNode(node);
                String nodeName=childNode.getProperty("name").toString();
                for(String s: ps)
                {
                    if(nodeName.toLowerCase().equals(s.split(":")[1]))
                    {
                        pList[i]=s.split(":")[0];
                        if (childNode.hasRelationship(Direction.OUTGOING))
                        {
                            valueList[i]= Double.parseDouble(calc(childNode)+"");
                        }
                        else
                        {
                            valueList[i]=Double.parseDouble(childNode.getProperty("value").toString());
                        }
                        i++;
                        break;
                    }
                }
            }

            //Todo: compare 2 string list : ps and plist; if not find parameter, put 0 as default value
            
            Double result = Parser.eval(f_xs, pList, valueList);
            return Math.round(result);
        }
        return -1;
    }
}