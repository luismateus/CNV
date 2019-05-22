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
import java.util.Map;
import java.util.concurrent.Executors;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

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
    static AmazonDynamoDB dynamoDB;

    //METRICS

    private LoadBalancer(){}

    public static LoadBalancer getLoadBalancer(){ 
        if (loadBalancer == null)
            loadBalancer = new LoadBalancer();
            //try{
            init();
            // }catch(Exception e){
            //     throw new AmazonClientException(
            //         "Cannot load the credentials from the credential profiles file. " +
            //         "Please make sure that your credentials file is at the correct " +
            //         "location (~/.aws/credentials), and is in valid format.",
            //         e);
            // }
        return loadBalancer; 
    }
    
    

    private static void init() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        // ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        // try {
        //     credentialsProvider.getCredentials();
        // } catch (Exception e) {
        //     throw new AmazonClientException(
        //             "Cannot load the credentials from the credential profiles file. " +
        //             "Please make sure that your credentials file is at the correct " +
        //             "location (~/.aws/credentials), and is in valid format.",
        //             e);
        // }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion("us-east-1")
            .build();

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
            System.out.println(request.getRequestURI().getQuery());
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
        Instance instance = AutoScaler.getAutoScaler().getEC2().chooseMinInstance();
        if(instance == null)
            instance = instances.get(0);
        return instance;
    }

}
