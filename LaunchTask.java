import java.util.TimerTask;
import java.util.Date;
/**
 * 
 * @author Dhinakaran P.
 */
// Create a class extends with TimerTask
public class LaunchTask extends TimerTask {

    private AutoScaler autoScaler;
    
    public void setAutoScaler(AutoScaler as){
        this.autoScaler = as;
    }

	// Add your task here
	public void run() {
        this.autoScaler.printInstancesReport();
        this.autoScaler.launch();
        this.autoScaler.printInstancesReport();
        System.out.println("30 SECONDS....");
	}
}