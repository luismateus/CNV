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
import com.amazonaws.services.ec2.model.Instance;
import java.util.Timer;



import javax.imageio.ImageIO;

public class CleanInstances {

    private static EC2 manager = new EC2();

    public static void main(final String[] args) throws Exception {
        terminateAllInstances();
    }

    public static void terminateAllInstances(){
        manager.terminateInstances(getInstances());
        printInstancesReport();
    }

    public static ArrayList<Instance> getInstances(){
        return manager.getInstances();
    }

    public static void printInstancesReport(){
        manager.printInstancesReport();
    }
}