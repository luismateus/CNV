import BIT.BIT.highBIT.*;
import java.io.*;
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
import AmazonDynamoDBHandler;



public class InstrumentationMetrics {
    private static ConcurrentHashMap<Long, Metrics> metricsPerThread = new ConcurrentHashMap<Long, Metrics>();

    public class Metrics{
        public int bb_count;

        public void Metrics(int bb_count){
            this.bb_count = bb_count;
        }
    }

    public static void main(final String[] args) throws Exception {
        File file = new File(argv[0]);
        String filename = file.toString(); //name of strategy
        
        //dk if this works
        AmazonDynamoDBHandler.init();
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBHandler.getDB();

        if(filename.endsWidth(".class")){
            ClassInfo ci = new ClassInfo(filename);
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement(); 
                for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                    BasicBlock bb = (BasicBlock) b.nextElement();
                    bb.addBefore("InstrumentationMetrics", "basicBlocks", new Integer(bb.size()));
                }
            }
            ci.addAfter("InstrumentationMetrics", "printMetrics", "null");
            ci.write("metricsOutput");
        }

    }

    //do a init to call somewhere tbd
    public static synchronized void init_metric() {
        Metrics metric = new Metrics(0);
        long threadId = Thread.currentThread().getId();
        metricsPerThread.put(threadId, metric);

	}

    public static synchronized void basicBlocks(int count){
        Metrics metric = metricsPerThread.get(Thread.currentThread().getId()); //create hashamap in webserver
        metric.bb_count++;
        metricsPerThread.put(threadId, metric);
    }


    //dk if works
    public static storeMetrics(AmazonDynamoDB amazonDynamoDB){
        String tableName = "cnv-metrics";
        try{
            Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
            item.put("bb", new AttributeValue(String.valueOf(metric.bb_count)));
            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);

            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
        }

    }
}