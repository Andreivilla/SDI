import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import interfaces.IDatabase;
import interfaces.IMatrix;
public class Client {

    public static boolean isNumber(String str) {
        try {
          Integer.parseInt(str);
          return true;
        } catch (NumberFormatException e) {
          return false;
        }
      }

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        IMatrix matrix_server = (IMatrix) Naming.lookup("rmi://localhost:1099/MatrixServer");
        Registry registry = LocateRegistry.getRegistry("localhost", 2099);
        IDatabase database_server = (IDatabase) registry.lookup("rmi://localhost:2099/Database");
        Scanner scanner = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Gera matriz aleatoria");
            System.out.println("2. Soma matrizes");
            System.out.println("3. Multiplica matrizes");
            System.out.println("4. Delete matiz");
            System.out.println("0. Sair");
            System.out.print("Digite a opção desejada: ");
            
            String opcaostr = System.console().readLine();
            if(isNumber(opcaostr)){
                opcao = Integer.parseInt(opcaostr);
            }else{
                opcao = -1;
            }            

            switch (opcao) {
                case 1:{
                    // criar matrizes
                    System.out.println("<linhas> <colunas> <nome>:");
                    String input = scanner.nextLine();
                    String[] input_parts = input.split(" ");
                    if(!isNumber(input_parts[0]) || !isNumber(input_parts[1]))
                    break;
            
                    int rows = Integer.parseInt(input_parts[0]);
                    int cols = Integer.parseInt(input_parts[1]);
                    String name = input_parts[2];
            
                    double[][] matrix = matrix_server.randfill(rows, cols);
            
                    database_server.save(matrix, name);
                    break;
                }
                case 2:{
                    // Soma matrizes
                    System.out.println("<nome> + <nome> = <nome>");
                    String input = scanner.nextLine();
                    String[] input_parts = input.split(" ");
                    
                    String nome1 = input_parts[0];
                    String nome2 = input_parts[1];
                    String nome_resultado = input_parts[2];

                    double[][] matriz1 = database_server.load(nome1);
                    double[][] matriz2 = database_server.load(nome2);

                    double[][] matriz_result = matrix_server.sum(matriz1, matriz2);
                    
                    database_server.save(matriz_result, nome_resultado);
                    break;
                }
                case 3: {
                    //multiplica matrizes
                    System.out.println("<nome> x <nome> = <nome>");
                    String input = scanner.nextLine();
                    String[] input_parts = input.split(" ");                    
                    String nome1 = input_parts[0];
                    String nome2 = input_parts[1];
                    String nome_resultado = input_parts[2];

                    double[][] matriz1 = database_server.load(nome1);
                    double[][] matriz2 = database_server.load(nome2);

                    double[][] matriz_result = matrix_server.mult(matriz1, matriz2);
                    
                    database_server.save(matriz_result, nome_resultado);
                    break;
                    }    
                case 4:{
                    //remover matriz
                    System.out.println("remover: <nome>");
                    String input = scanner.nextLine();
                    database_server.remove(input);
                    break;
                }
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.err.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
}
}
