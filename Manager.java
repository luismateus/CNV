import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.Scanner;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;
import com.amazonaws.services.ec2.model.Instance;
import java.util.Set;
import java.util.HashSet;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.lang.StringBuffer;
import java.net.URL;
import java.net.HttpURLConnection;


import javax.imageio.ImageIO;

public class Manager {

    private static AutoScaler autoScaler = new AutoScaler();
    private static LoadBalancer loadBalancer = new LoadBalancer();

	public static void main(final String[] args) throws Exception {
		// LOCAL
		//final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8000), 0);
		// REMOTE
		final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        
		server.createContext("/climb", new HandleRequest(autoScaler, loadBalancer));

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[0m" + "===== MANAGER IS LISTENING AT: " + server.getAddress().toString() + " =====");
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[0m" + "==========================================================");

        while(true){
            System.out.println();
            System.out.println("\u001B[0m" + "========================== MENU ==========================");
            System.out.println("\u001B[0m" + "1 - Launch an instance");
            System.out.println("\u001B[0m" + "2 - Print instances report");
            System.out.println("\u001B[0m" + "3 - Terminate one instance");
            System.out.println("\u001B[0m" + "4 - Terminate all instances");
            System.out.println("\u001B[0m" + "==========================================================");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    autoScaler.launch();
                    break;
                case 2:
                    autoScaler.printInstancesReport();;
                    break;
                case 3:
                    ArrayList<Instance> instances = autoScaler.getInstances();
                    System.out.println("Write the IP of the instance you want to terminate:");
                    Scanner scanner2 = new Scanner(System.in);
                    String ip = scanner2.nextLine();
                    boolean found = false;
                    for(Instance instance : instances){
                        if(instance.getPublicIpAddress().equals(ip)){
                            autoScaler.terminate(instance);
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                        System.out.println("IP not found!");
                    break;
                case 4:
                    String[] a = new String[0];
                    CleanInstances.main(a);
                    break;
                default:
                System.out.println("\u001B[0m" + "Invalid Input");
                    continue;
            }
        }
    }
	

	static class HandleRequest implements HttpHandler {

        private  AutoScaler autoScaler;
        private  LoadBalancer loadBalancer;

        public HandleRequest(AutoScaler autoScaler, LoadBalancer loadBalancer){
            this.autoScaler = autoScaler;
            this.loadBalancer = loadBalancer;
        }

		@Override
		public void handle(final HttpExchange request) throws IOException {
            autoScaler.printInstancesReport();
            byte[] response = loadBalancer.handleRequest(request, autoScaler.getInstances());
            request.sendResponseHeaders(200, response.length);
            OutputStream os = request.getResponseBody();
            os.write(response);
            os.close();
        
            System.out.println("\u001B[0m" + "==========================================================");
            System.out.println("\u001B[0m" + "RESPONDING");
            //System.out.println("\u001B[0m" + "Instance " + chosenInstance.getInstanceId() + " with the IP " + chosenInstance.getPublicIpAddress() + " responded to  it's request :)");
            System.out.println("\u001B[0m" + "==========================================================");
        }
	}
}
