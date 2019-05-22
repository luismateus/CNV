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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;


import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.Region;

import java.util.*;
import java.io.*;

public class LaunchManagerEC2 {


    static AmazonEC2      ec2;

    private static String ami;
    private static String key_file;
    private static String security_group;

    private static void init() throws Exception {

        /*

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
            */
            FileReader reader=new FileReader("config.cfg");
            Properties p=new Properties();
            p.load(reader);
            ami = p.getProperty("manager_ami");
            key_file = p.getProperty("key_file");
            security_group = p.getProperty("security_group");
/*
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }*/
    ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").build();
}


    public static void main(String[] args) throws Exception {

        System.out.println("===========================================");
        System.out.println("Welcome to the AWS Java SDK!");
        System.out.println("===========================================");

       
        init();
        try {
            RunInstancesRequest runInstancesRequest =
               new RunInstancesRequest();

            IamInstanceProfileSpecification profile = new IamInstanceProfileSpecification();
            
            // TODO: copy-paste the ARN you copied from the role you created.
            profile.setArn("arn:aws:iam::240593726392:instance-profile/cnv-project-role");

            runInstancesRequest.withImageId(ami)
                               .withInstanceType("t2.micro")
                               .withMinCount(1)
                               .withMaxCount(1)
                               .withKeyName(key_file)
                               .withSecurityGroups(security_group)
                               .withIamInstanceProfile(profile);
            RunInstancesResult runInstancesResult =
               ec2.runInstances(runInstancesRequest);
            String newInstanceId = runInstancesResult.getReservation().getInstances()
                                      .get(0).getInstanceId();
            System.out.println("\u001B[92m" + "Manager launched :)");
            System.out.println("\u001B[92m" + "==========================================================");
            System.out.println("\u001B[0m" + "==========================================================");
            System.out.println();
            System.out.println("Type '1' to terminate manager!");

            while (true) {
                Scanner scanner = new Scanner(System.in);
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        System.out.println("Terminating the instance.");
                        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
                        termInstanceReq.withInstanceIds(newInstanceId);
                        ec2.terminateInstances(termInstanceReq);
                        return;
                    default:
                        System.out.println("\u001B[0m" + "Invalid Input");
                        continue;
    
                }
                
            }
            
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
       
    }
}


