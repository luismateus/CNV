import java.util.TimerTask;
import java.util.Date;

// Create a class extends with TimerTask
public class TerminateTask extends TimerTask {

    private AutoScaler autoScaler;
    
    public void setAutoScaler(AutoScaler as){
        this.autoScaler = as;
    }

	public void run() {
        this.autoScaler.terminate(this.autoScaler.getInstances().get(0));
	}
}