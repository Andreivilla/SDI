import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import interfaces.IDatabase;

public class ServerDatabase extends UnicastRemoteObject implements IDatabase {

    public ServerDatabase() throws RemoteException {
        super();
    }

    @Override
    public void save(double[][] a, String name) throws RemoteException {
        String filePath = "data/" + name + ".csv";
        try (
            FileOutputStream fos = new FileOutputStream(filePath);
            PrintWriter writer = new PrintWriter(new File(filePath));
        ) {
            for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                writer.print(a[i][j] + ",");
            }
            writer.println();
            }
        } catch (IOException e) {
            throw new RemoteException("Erro ao salvar a matriz", e);
        }
    }

    @Override
    public double[][] load(String filename) throws RemoteException {
        double[][] matriz = null;
        String filePath = "data/" + filename + ".csv";

        try (
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);
        ) {
            List<double[]> rows = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");  
                double[] row = Arrays.stream(values)
                                    .mapToDouble(Double::parseDouble)
                                    .toArray();
                rows.add(row);
            }
            matriz = rows.toArray(new double[rows.size()][]);
        } catch (IOException e) {
            throw new RemoteException("Erro ao carregar a matriz", e);
        }

        return matriz;
    }


    @Override
    public void remove(String filename) throws RemoteException {
        String filePath = "data/" + filename + ".csv";
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (SecurityException e) {
            throw new RemoteException("Erro ao remover o arquivo", e);
        }
    }

        public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(2099); // Porta padrão do RMI
            registry.rebind("rmi://localhost:2099/Database", new ServerDatabase());
            System.out.println("Servidor RMI iniciado!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
