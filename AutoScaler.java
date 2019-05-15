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

    private static EC2 manager = new EC2();

    // Starts the dynamic and automatic process of managing the number of running instances
	public AutoScaler(){
        //dumbAuto();
        //launch();
        //launch();
    }

    // To launch a new instance
    public void launch(){
        try{
            manager.launchInstance();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    // To terminate an especific instance
    public void terminate(Instance instance){
        manager.terminateInstance(instance);
    }

    // To terminate all the instances that are running
    // Be carefull, since pending instances are not running yet they [pending] will not be terminated
    public void terminateAllInstances(){
        manager.terminateInstances(getInstances());
        printInstancesReport();
    }

    // Prints a report about the instances, their states, IDs and IP addresses
    // Returns an array list with the [RUNNING] instances
    public ArrayList<Instance> getInstances(){
        return manager.getInstances();
    }

    public void dumbAuto(){
        try{
            launch();
            launch();
            printInstancesReport();
            Timer time = new Timer(); // Instantiate Timer Object
            TerminateTask terminate = new TerminateTask();
		    LaunchTask launch = new LaunchTask(); // Instantiate SheduledTask class
            terminate.setAutoScaler(this);
            launch.setAutoScaler(this);
            time.schedule(terminate, 30000, 60000); // Create Repetitively task for every 1 secs
            time.schedule(launch, 60000, 60000);    
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void printInstancesReport(){
        manager.printInstancesReport();
    }

    public EC2 getManager(){
        return manager;
    }
}
