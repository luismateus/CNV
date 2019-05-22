package pt.ulisboa.tecnico.cnv.server;

import java.io.*;
import java.lang.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import BIT.highBIT.*;



public class InstrumentationMetrics {

    static AmazonDynamoDB dynamoDB;

    private static ConcurrentHashMap<Long, Metrics> metricsPerThread = new ConcurrentHashMap<Long, Metrics>();
    static String query;

    static class Metrics{
        public int bb_count;
        public int alloc_count;

        public Metrics(int bb_count, int alloc_count){
            this.bb_count = bb_count;
            this.alloc_count = alloc_count;
        }
    }

    private static void init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        /*ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }*/
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion("us-east-1")
            .build();

    }

    private static void createTable() throws Exception {
        try {
            String tableName = "cnv-metrics";

            System.out.println("Creating "+tableName + " table");

            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(dynamoDB, tableName);

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);
            

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public static void main(String argv[]) throws Exception {
        File file = new File(argv[0]);
        String fileName = file.toString(); //name of strategy
        
        init();
        createTable();



        if(fileName.endsWith(".class")){
            init_metrics();
            ClassInfo ci = new ClassInfo(fileName);
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement(); 
                InstructionArray instructions = routine.getInstructionArray();

                for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                    BasicBlock bb = (BasicBlock) b.nextElement();
                    bb.addBefore("InstrumentationMetrics", "basicBlocks", new Integer(bb.size()));
                }
                for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
                    Instruction instr = (Instruction) instrs.nextElement();
                    int opcode=instr.getOpcode();
                    if ((opcode==InstructionTable.NEW) ||
                        (opcode==InstructionTable.newarray) ||
                        (opcode==InstructionTable.anewarray) ||
                        (opcode==InstructionTable.multianewarray)) {
                        instr.addBefore("InstrumentationMetrics", "allocCount", new Integer(opcode));
                    }    
                }
            }
            ci.addAfter("InstrumentationMetrics", "storeMetrics", "null");
            ci.write("metrics-output");
        }
        Metrics metric = metricsPerThread.get(Thread.currentThread().getId());
        metric.bb_count = 0;
        metric.alloc_count = 0;

    }


    public static synchronized void init_metrics() {
        Metrics metric = new Metrics(0,0);
        metricsPerThread.put(Thread.currentThread().getId(), metric);

	}

    public static synchronized void basicBlocks(int count){
        Long tID = Thread.currentThread().getId();
        Metrics metric = metricsPerThread.get(tID);
        metric.bb_count += count;
        metricsPerThread.put(tID, metric);
    }

    public static synchronized void allocCount(int count){
        Long tID = Thread.currentThread().getId();
        Metrics metric = metricsPerThread.get(tID);
        metric.alloc_count += count;
        metricsPerThread.put(tID, metric);
    }


    public static synchronized void set_params(String received_query){
        query = received_query;
    }


    public static synchronized void storeMetrics(){
        String tableName = "cnv-metrics";
        Metrics metric = metricsPerThread.get(Thread.currentThread().getId());
        try{
            Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
            item.put("query", new AttributeValue(query));
            item.put("bb", new AttributeValue(String.valueOf(metric.bb_count)));
            item.put("allocs", new AttributeValue(String.valueOf(metric.alloc_count)));
            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);

            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
        }catch (Exception e) {
            //TODO: handle exception
        }

    }
}