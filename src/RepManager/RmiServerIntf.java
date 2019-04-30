//NOTE: USED START UP CODE FROM: https://en.wikipedia.org/wiki/Java_remote_method_invocation
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiServerIntf extends Remote {
    public String getMessage() throws RemoteException;
    public String getIP(String doc) throws RemoteException;
}