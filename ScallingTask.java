import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Date;
import com.amazonaws.services.ec2.model.Instance;


// Create a class extends with TimerTask
public class ScallingTask extends TimerTask {

    private int running;
    private int pending;
    private ArrayList<Instance> runningInstances;
    

    // Add your task here
	public void run() {
        System.out.println("SCALLING! :)");
        updateMetrics();
        decide();
    }

    public void updateMetrics(){
        // READ FROM DB
        ArrayList<Instance> runningInstances = AutoScaler.getAutoScaler().getInstances();
        this.running = runningInstances.size();
        this.pending = AutoScaler.getAutoScaler().getPendingInstances();
    }

    public void decide(){
        if(running == 0 && pending == 0)
            AutoScaler.getAutoScaler().launch();
    }
}