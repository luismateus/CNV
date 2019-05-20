import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Date;
import com.amazonaws.services.ec2.model.Instance;


// Create a class extends with TimerTask
public class ScallingTask extends TimerTask {

    private int running;
    private int pending;
    private ArrayList<Instance> runningInstances;
    private ArrayList<Instance> occupiedInstances;
    private double MAX_CPU_USAGE = 100;
    private double MAX_CPU_USAGE_BEFORE_LAUNCH = 0.75f;
    private double MIN_CPU_USAGE_BEFORE_TERMINATE = 0.25f;

    // Add your task here
	public void run() {
        System.out.println("SCALLING! :)");
        update();
        decide();
    }

    public void update(){
        ArrayList<Instance> runningInstances = AutoScaler.getAutoScaler().getInstances();
        ArrayList<Instance> occupiedInstances = LoadBalancer.getLoadBalancer().getOccupiedInstances();
        this.running = runningInstances.size();
        this.pending = AutoScaler.getAutoScaler().getPendingInstances();
    }

    public void decide(){
        if(this.running == 0 && this.pending == 0){
            AutoScaler.getAutoScaler().launch();
            return;
        }
        if(this.pending != 0 && this.running == 1)
            return;
        if(getSystemCPUUsage() > MAX_CPU_USAGE * MAX_CPU_USAGE_BEFORE_LAUNCH){
            AutoScaler.getAutoScaler().launch();
            return;
        }
        if(getSystemCPUUsage() < MAX_CPU_USAGE * MIN_CPU_USAGE_BEFORE_TERMINATE){
            terminateAnInstance();
            return;
        }
    }

    public double getSystemCPUUsage(){
        return AutoScaler.getAutoScaler().getSystemCPUUsage();
    }

    public double getInstanceCPUUsage(Instance instance){
        return AutoScaler.getAutoScaler().getInstanceCPUUsage(instance);
    }

    public void terminateAnInstance(){
        boolean found;
        for(Instance instanceR : runningInstances){
            found = false;
            for(Instance instanceO : occupiedInstances){
                if((instanceR.getInstanceId()).equals(instanceO.getInstanceId())){
                    found = true;
                    break;
                }
            }
            if(found){
                continue;
            }
            else{
                AutoScaler.getAutoScaler().terminate(instanceR);
                return;
            }
        }
        AutoScaler.getAutoScaler().setTerminateNextFlag(true);
    }
}