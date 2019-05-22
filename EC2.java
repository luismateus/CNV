/* 2016-18 Extended by Luis Veiga and Joao Garcia */
/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*; 
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;


import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.Region;

import java.util.*;
import java.io.*;

public class EC2 {

    static AmazonEC2      ec2;
    static AmazonCloudWatch cloudWatch;
    private ArrayList<Instance> instances;
    private Map<Instance, Double> instancesCPUUsage;
    private int pending;
    private int running;
    private int shuttingDown;
    private int terminated;

    private String ami;
    private String key_file;
    private String security_group;

    public EC2(){
        try{
            init();
        }catch(Exception e){
            System.out.println("Caught Exception: " + e.getMessage());
        }
    }

    public EC2(String manager){
        try{
            initManager();
        }catch(Exception e){
            System.out.println("Caught Exception: " + e.getMessage());
        }
    }

    public int getPendingInstances(){
        updateInstances();
        return this.pending;
    }

    // MUDAR AS KEYS E CENAS
    private void initManager() throws Exception {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();

            //load ec2 properties from file
            FileReader reader=new FileReader("config.cfg");
            Properties p=new Properties();
            p.load(reader);

            /*ami = p.getProperty("ami"); ISTO TEM QUE MUDAR PARA LANÇAR O MANAGER PARA A IMAGEM DO MANAGER */
            key_file = p.getProperty("key_file");
            security_group = p.getProperty("security_group");


        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        if(ec2==null){
            ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        }
    }

    private void init() throws Exception {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();

            //load ec2 properties from file
            FileReader reader=new FileReader("config.cfg");
            Properties p=new Properties();
            p.load(reader);

            ami = p.getProperty("ami");
            key_file = p.getProperty("key_file");
            security_group = p.getProperty("security_group");


        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        if(ec2==null){
            ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
            cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        }
    }
    
    public ArrayList<Instance> getInstances(){
        updateInstances();
        return this.instances;
    }

    public void updateInstances(){
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> aux = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            aux.addAll(reservation.getInstances());
        }
        this.instances = new ArrayList<Instance>();
        this.pending = 0;
        this.running = 0;
        this.shuttingDown = 0;
        this.terminated = 0;
        int stateAUX = -1;
        for(Instance instance : aux){                
            stateAUX = instance.getState().getCode();
            switch(stateAUX) {
                case 0:
                    this.pending++;
                    break;
                case 16:
                    this.running++;
                    this.instances.add(instance);
                    break;
                case 32:
                    this.shuttingDown++;
                    break;
                case 48:
                    this.terminated++;
                    break;              
                default:
                    System.out.println("\u001B[0m" + "Unkown Code: " + stateAUX);
              }
        }
    }

    public void printInstancesReport(){
        updateInstances();
        System.out.println();
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[96m" + "==========================================================");
        System.out.println("\u001B[96m" + "================= INSTANCES STATE REPORT =================");
        System.out.println("\u001B[96m" + "==========================================================");
        System.out.println("\u001B[96m" + "You have " + this.shuttingDown + " SHUTTING DOWN instances");
        System.out.println("\u001B[96m" + "You have " + this.terminated + " TERMINATED instances");
        System.out.println("\u001B[96m" + "You have " + this.pending + " PENDING instances");
        System.out.println("\u001B[96m" + "You have " + this.running + " RUNNING instances");
        if(running == 0){
            System.out.println("\u001B[0m" + "==========================================================");
            return;
        }
        System.out.println("\u001B[96m" + "=================== Running Instances ====================");
        int i=0;
        for(Instance instance : instances){
            System.out.println("\u001B[96m" + "============================ " + i + " ===========================");
            System.out.println("\u001B[96m" + "Instance ID: " + instance.getInstanceId());
            System.out.println("\u001B[96m" + "Instance IP: " + instance.getPublicIpAddress());
            i++;
        }
        System.out.println("\u001B[96m" + "==========================================================");
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println();
    }


    public void instanceMetricsReport(){
        this.instancesCPUUsage = new HashMap<Instance,Double>();
        ArrayList<Instance> instances = getInstances();

        System.out.println("total instances = " + instances.size());

        long offsetInMilliseconds = 1000 * 60 * 10;

        Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        List<Dimension> dims = new ArrayList<Dimension>();
        dims.add(instanceDimension);

        for (Instance instance : instances) {
            String name = instance.getInstanceId();
            String state = instance.getState().getName();
            if (state.equals("running")) { 
                //System.out.println("running instance id = " + name);
                instanceDimension.setValue(name);
        System.out.println("HERE1!");
        GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
            .withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
            .withNamespace("AWS/EC2")
            .withPeriod(60)
            .withMetricName("CPUUtilization")
            .withStatistics("Average")
            .withDimensions(instanceDimension)
            .withEndTime(new Date());
            GetMetricStatisticsResult getMetricStatisticsResult = cloudWatch.getMetricStatistics(request);
                List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
                for (Datapoint dp : datapoints) {
                System.out.println(" CPU utilization for instance " + name + " = " + dp.getAverage());
                double cpu = dp.getAverage();
                this.instancesCPUUsage.put(instance,cpu);
                }
            }else{
                //System.out.println("instance id = " + name);
            }
        //System.out.println("Instance State : " + state +".");
        }
    }

    public void launchInstance() throws Exception {
        System.out.println();
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[92m" + "================= LAUNCHING NEW INSTANCE =================");
        try {

            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            RunInstancesRequest runInstancesRequest =
               new RunInstancesRequest();

            runInstancesRequest.withImageId(ami)
                               .withInstanceType("t2.micro")
                               .withMinCount(1)
                               .withMaxCount(1)
                               .withKeyName(key_file)
                               .withSecurityGroups(security_group);
            RunInstancesResult runInstancesResult =
               ec2.runInstances(runInstancesRequest);
            String newInstanceId = runInstancesResult.getReservation().getInstances()
                                      .get(0).getInstanceId();
            System.out.println("\u001B[92m" + "Instance launched :)");
            System.out.println("\u001B[92m" + "==========================================================");
            System.out.println("\u001B[0m" + "==========================================================");
            System.out.println();
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
    }
    
    public void terminateInstances(ArrayList<Instance> instances){
        for(Instance instance : instances){
            terminateInstance(instance);
        }
    }

    public void terminateInstance(Instance instance){
        System.out.println();
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println("\u001B[91m" + "================== TERMINATING INSTANCE ==================");
        System.out.println("\u001B[91m" + "ID: " + instance.getInstanceId() + "      IP: " + instance.getPublicIpAddress());
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instance.getInstanceId());
        ec2.terminateInstances(termInstanceReq);
        System.out.println("\u001B[91m" + "Instance terminated :)");
        System.out.println("\u001B[91m" + "==========================================================");
        System.out.println("\u001B[0m" + "==========================================================");
        System.out.println();
    }

    //RETURN 
    public double getSystemCPUUsage(){
        double CPUUsage = 0;
        //instanceMetricsReport();
        ArrayList<Instance> instances = getInstances();
        //System.out.println(this.instances.get(0).getInstanceId());
        for(Instance instance : instances){

            String name = instance.getInstanceId();
            String state = instance.getState().getName();
            if (state.equals("running")) { 
                CPUUsage += getInstanceCPUUsage(instance);
            }
        }
        return (CPUUsage/instances.size());
    }

    public double getInstanceCPUUsage(Instance instance){
        //System.out.println(instancesCPUUsage.get(instance)*100);
        //return instancesCPUUsage.get(instance)*100; //to percentage?
        double dummy = 10;
        return dummy;
    }

    public Instance chooseMinInstance(){
        ArrayList<Instance> instances = getInstances();
        Instance chosen = null;
        double min_cpu = 101; //100 is max
        //instanceMetricsReport();
        for(Instance instance : instances){
            double cpuUsage = getInstanceCPUUsage(instance);
            if(cpuUsage < min_cpu){
                min_cpu = cpuUsage;
                chosen = instance;
            }
        }
        return chosen;
    }
}
