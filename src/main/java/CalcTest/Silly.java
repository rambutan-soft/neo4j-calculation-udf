package CalcTest;

import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
//import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.UserFunction;
/**
 * This is an example how you can create a simple user-defined function for Neo4j.
 */
public class Silly
{
    @UserFunction
    @Description("CalcTest.silly() - give some data from sql server")
    public String silly() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection("jdbc:sqlserver://;servername=awsbuild;databaseName=SITAudit;integratedSecurity=true");
            Statement statement = conn.createStatement();
            String queryString = "select top 1 BranchCoverage from BuildCodeCoverage order by tfsbuildid desc";
            ResultSet rs = statement.executeQuery(queryString);
            rs.next();
            return "The latest code coverage is: "+rs.getString(1);
            //Class.forName("com.mysql.jdbc.Driver");

         } catch (Exception e) {
            //e.printStackTrace();
            return e.getMessage();
         }
        //return "-10";
    }
}