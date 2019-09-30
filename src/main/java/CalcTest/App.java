package CalcTest;

import CalcTest.parser.util.*;
import CalcTest.parser.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // Silly s=new Silly();
        // System.out.println(s.silly() );
        String f_xs = "(x+y)*2+(x+y)*1.2";
    
        // final Point xo = new Point("x", new Double(2));
        // final Point zo = new Point("y", new Double(1));
        // final List<Point> foo = new ArrayList<Point>();
        // foo.add(xo);
        // foo.add(zo);
        String[] ps=new String[] {"y","x"};
        Double[] vs=new Double[] {3.0,3.0};
        Double result = Parser.eval(f_xs,ps,vs);
        System.out.println(result);
    }
}
