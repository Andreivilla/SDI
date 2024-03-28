package interfaces;
//package interfaces
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * A base de dados deve armazenar o conteúdo da matriz no arquivo informado. Além disso, deve recuperar a matriz quando for solicitado.
 * Por fim, a base de dados deve remover o arquivo quando solicitado.
 */

public interface IDatabase extends Remote {
    public void save(double[][] a, String filename) throws RemoteException;
    public double[][] load(String filename) throws RemoteException;
    public void remove(String filename) throws RemoteException;
}
