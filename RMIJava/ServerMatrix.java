import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import interfaces.IMatrix;

public class ServerMatrix extends UnicastRemoteObject implements IMatrix {

    protected ServerMatrix() throws RemoteException {
        super();
    }

    @Override
    public double[][] sum(double[][] a, double[][] b) throws RemoteException {
        if (a.length != b.length || a[0].length != b[0].length) {
            throw new IllegalArgumentException("Matrices must have the same dimensions for addition.");
        }

        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }

        return result;
    }

    @Override
    public double[][] mult(double[][] a, double[][] b) throws RemoteException {
        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Matrix A columns must equal Matrix B rows for multiplication.");
        }

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                result[i][j] = 0;
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return result;
    }

    @Override
    public double[][] randfill(int rows, int cols) throws RemoteException {
        double[][] matrix = new double[rows][cols];
        Random random = new Random();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble();
            }
        }

        return matrix;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando o servidor Matrix...");

        // Criando a instância do servidor
        IMatrix server = new ServerMatrix();

        // Registrando o servidor no RMI Registry
        // "rmi://localhost:1099/MatrixServer" é o nome do serviço RMI
        LocateRegistry.createRegistry(1099);
        Naming.rebind("rmi://localhost:1099/MatrixServer", server);

        System.out.println("Servidor Matrix pronto!");

        // Mantendo o servidor em execução
        while (true) {
            Thread.sleep(1000);
        }
    }
}
