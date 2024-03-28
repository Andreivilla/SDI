package interfaces;
import java.rmi.Remote;
import java.rmi.RemoteException;
 
/*
 * A matriz é o objeto alvo que será manipulado
 */

public interface IMatrix extends Remote {
    public double[][] sum(double[][] a, double[][] b) throws RemoteException;
    public double[][] mult(double[][] a, double[][] b) throws RemoteException;
    public double[][] randfill(int rows, int cols) throws RemoteException;
}
