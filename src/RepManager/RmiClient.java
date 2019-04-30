//NOTE: USED START UP CODE FROM: https://en.wikipedia.org/wiki/Java_remote_method_invocation
import java.rmi.Naming;

public class RmiClient { 
    public static void main(String args[]) throws Exception {
        RmiServerIntf obj = (RmiServerIntf)Naming.lookup("//localhost/RmiServer");
        System.out.println(obj.getMessage()); 
        System.out.println(obj.getIP("/doc2.html"));
    }
}