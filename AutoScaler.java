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
import java.util.Timer;



import javax.imageio.ImageIO;

public class AutoScaler {

    private static AutoScaler autoScaler; 

    private static EC2 ec2;

    // Starts the dynamic and automatic process of managing the number of running instances
	private AutoScaler(){
        ec2 = new EC2();
        autoScalling();
    }

    public static AutoScaler getAutoScaler() { 
        if (autoScaler == null) 
            autoScaler = new AutoScaler(); 
        return autoScaler; 
    } 

    // To launch a new instance
    public void launch(){
        try{
            ec2.launchInstance();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    // To terminate an especific instance
    public void terminate(Instance instance){
        ec2.terminateInstance(instance);
    }

    // To terminate all the instances that are running
    // Be carefull, since pending instances are not running yet they [pending] will not be terminated
    public void terminateAllInstances(){
        ec2.terminateInstances(getInstances());
        printInstancesReport();
    }

    // Prints a report about the instances, their states, IDs and IP addresses
    // Returns an array list with the [RUNNING] instances
    public ArrayList<Instance> getInstances(){
        return ec2.getInstances();
    }

    public void autoScalling(){
        try{
            Timer time = new Timer(); 
            ScallingTask scallingTask = new ScallingTask();
            time.schedule(scallingTask, 0, 5000);    
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void printInstancesReport(){
        ec2.printInstancesReport();
    }

    public EC2 getManager(){
        return ec2;
    }

    public int getPendingInstances(){
        return ec2.getPendingInstances();
    }
}
