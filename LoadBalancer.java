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

public class LoadBalancer {

    private boolean toogle = true;
    private static LoadBalancer loadBalancer = null;
    private ArrayList<Instance> ocupiedInstances = new ArrayList<Instance>();

    //METRICS

    private LoadBalancer(){}

    public static LoadBalancer getLoadBalancer(){ 
        if (loadBalancer == null) 
            loadBalancer = new LoadBalancer(); 
        return loadBalancer; 
    } 

	public byte[] handleRequest(final HttpExchange request, ArrayList<Instance> instances) throws IOException {
        System.out.println();
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[93m" + "==========================================================");
        Instance chosenInstance = chooseInstance(instances);
        System.out.println("\u001B[93m" + "Forwarding request to instance " + chosenInstance.getInstanceId() + " with the IP " + chosenInstance.getPublicIpAddress());
        System.out.println("\u001B[93m" + "==========================================================");
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println();
        return forwardRequest(request, chosenInstance);
    }

    public byte[] forwardRequest(HttpExchange request, Instance instance){
        try{
            ocupiedInstances.add(instance);
            URL url = new URL("http://" + instance.getPublicIpAddress() + ":8000/climb?" + request.getRequestURI().getQuery());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            byte[] response = new byte[connection.getContentLength()];
            DataInputStream rd = new DataInputStream(connection.getInputStream());    
            int readBytes = 0;
            while(readBytes < connection.getContentLength()){
                readBytes += rd.read(response, readBytes, response.length - readBytes);
            }
            if(connection != null){
                rd.close();
                connection.disconnect();
            }
            System.out.println();
            System.out.println("\u001B[0m" + "==========================================================");
            System.out.println("\u001B[93m" + "==========================================================");
            System.out.println("\u001B[93m" + "Response from Instance " + instance.getInstanceId() + " with the IP " + instance.getPublicIpAddress() + " responded to  it's request :)");
            System.out.println("\u001B[93m" + "==========================================================");
            System.out.println("\u001B[0m" + "==========================================================");
            System.out.println();
            ocupiedInstances.remove(instance);
            AutoScaler.getAutoScaler().checkIfTerminateAndTerminate(instance);
            return response;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Instance> getOccupiedInstances(){
        return this.ocupiedInstances;
    }

    public Instance chooseInstance(ArrayList<Instance> instances){
        Instance instance = AutoScaler.getAutoScaler().getEC2().getMinimalCPUUsageInstance();
        if(instance == null)
            instance = instances.get(0);
        return instance;
    }
}
