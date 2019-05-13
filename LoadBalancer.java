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


import javax.imageio.ImageIO;

public class LoadBalancer {

    private static AutoScaler autoScaler = new AutoScaler();

	public static void main(final String[] args) throws Exception {
		// LOCAL
		final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8000), 0);
		// REMOTE
		//final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        
		server.createContext("/climb", new MyHandler(autoScaler.getManager()));

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[93m" + "==========================================================");
        System.out.println("\u001B[93m" + "==========================================================");
        System.out.println("\u001B[93m" + "===== LOAD BALANCER IS LISTENING AT: " + server.getAddress().toString() + " =====");
        System.out.println("\u001B[93m" + "==========================================================");
        System.out.println("\u001B[93m" + "==========================================================");
        System.out.println("\u001B[0m" + "==========================================================");

	}

	static class MyHandler implements HttpHandler {

        private  EC2Launch manager;

        public MyHandler(EC2Launch manager){
            this.manager = manager;
        }

		@Override
		public void handle(final HttpExchange t) throws IOException {

            //CHOOSE SERVER
            //REQ SERVER
            ArrayList<Instance> instances = manager.getInstances();
            System.out.println("HANDLING..." + instances.size() + " instances");
            for (Instance instance : instances){ 
                /*TARGET_IP =  instance.getPublicIpAddress();
                TARGET_PORT = 8000;
                url = "http://" + TARGET_IP + ":" + TARGET_PORT + "/climb?" + queryParams;*/
                System.out.println("SEND REQUEST TO " + instance.getPublicIpAddress());
            }
        }
	}
}
