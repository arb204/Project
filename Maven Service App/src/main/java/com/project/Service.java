package com.project;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.gax.paging.Page;
import com.google.api.services.dataproc.Dataproc;
import com.google.api.services.dataproc.Dataproc.Builder;
import com.google.api.services.dataproc.model.HadoopJob;
import com.google.api.services.dataproc.model.Job;
import com.google.api.services.dataproc.model.JobPlacement;
import com.google.api.services.dataproc.model.JobReference;
import com.google.api.services.dataproc.model.SubmitJobRequest;
import com.google.auth.appengine.AppEngineCredentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Storage.ComposeRequest;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFileChooser;



/**
 * Demonstrate various ways to authenticate requests using Cloud Storage as an example call.
 */
public class Service {
	
	public static String projectID = "vaulted-broker-332902";
	public static String bucketID = "dataproc-staging-us-central1-610391583003-dqujerpe";
	public static String region = "us-central1";
	public static GoogleCredentials credentials;
	public static Storage storage;
	public static Dataproc dataproc;
	
	public static final ImmutableSet<String> JOB_STATE = ImmutableSet.of("CANCELLED", "ERROR", "DONE");
	
	public static void main(String[] args) throws Exception {
		//Authenticate with GCP
		try {
			storage = StorageOptions.getDefaultInstance().getService();
			credentials = GoogleCredentials.getApplicationDefault();
			System.out.println("Cluster Storage Accessed Sucessfully");
		} catch(Exception e) {
			System.out.println("Cluster Storage Could not be Accessed. Check Environment Variables");
			System.exit(0);
		}
		
		
		
		System.out.println("Welcome to Austin Brother's Search Engine");
		
		Scanner userInput = new Scanner(System.in);
		
		boolean flag = true;
		while(flag) {
			System.out.println();
			System.out.println("Please Pick a Numeric Value That Corresponds to the Operation You'd Like to Perform.");
			System.out.println("[1]\tUpload Files");
			System.out.println("[2]\tConstruct Inverted Indices");
			System.out.println("[0]\tExit");
			
			int input = userInput.nextInt();
			switch(input) {
				case 1: 
					File upload = getFiles();
					uploadFile(upload);
					System.out.println(upload.getAbsolutePath() + " Uploaded");
					break;
				case 2:
					String ii_job = runJob();
					merge(ii_job);
					flag = false;
					break;
				case 0: 
					System.out.println("Exiting");
					System.exit(0);
					break;
				default: 
					System.out.println("Bad Input! Try Again.");
					break;
			}
		}
		flag = true;
		System.out.println("Files Inputted and Merged: Options Updated");
		while(flag) {
			System.out.println("[1]\tDownload Output");
			System.out.println("[0]\tExit");
			
			int input = userInput.nextInt();
			switch(input) {
				case 1: 
					downloadOutput();
					break;
				case 0:
					System.out.println("Exiting");
					System.exit(0);
					break;
				default:
					System.out.println("Bad Input! Try Again.");
					break;
			}
	
		}
	}
	
	public static void downloadOutput() {
		Blob blob = storage.get(bucketID, "output");
		blob.downloadTo(Paths.get("C:/Users/blott/Onedrive/Desktop/Project/output.txt"));
	}
	
	public static void waitForJob(String jobId) throws IOException, InterruptedException {
		Job job = dataproc.projects().regions().jobs().get(projectID, region, jobId).execute();
		while(!JOB_STATE.contains(job.getStatus().getState())) {
			
			System.out.println("Job not finished yet. Job State: " + job.getStatus().getState());
			Thread.sleep(5000);
			job = dataproc.projects().regions().jobs().get(projectID, region, jobId).execute();
		}
		System.out.println("Job finished with status: " + job.getStatus().getState());
		System.out.println();
	}
	
	public static void merge(String jobId) {	
		String prefix = "output_" + jobId + "/";
		final Page<Blob> blobs = 
				storage.list(bucketID, BlobListOption.prefix(prefix), BlobListOption.currentDirectory());
		List<String> blobNames = new ArrayList<String>();
		System.out.println("Finding Files to Merge");
		for(Blob blob: blobs.iterateAll()) {
			System.out.println("Found: " + blob.getName());
			blobNames.add(blob.getName());
		}
		
		BlobId blobId = BlobId.of(bucketID, "output");
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
		ComposeRequest req =
				ComposeRequest.newBuilder().addSource(blobNames).setTarget(blobInfo).build();
		Blob blob = storage.compose(req);	
		System.out.println("Files Merged: output.txt");
	}
	
	public static String runJob() throws InterruptedException, IOException {
		System.out.println();
		dataproc = new Dataproc.Builder(new NetHttpTransport(), new GsonFactory(), new HttpCredentialsAdapter(credentials))
				.setApplicationName("ArbProj").build();
		
		String jobId = "InvertedIndex" + UUID.randomUUID().toString();
		try {
			dataproc.projects().regions().jobs().submit(projectID, region, new SubmitJobRequest()
					.setJob(new Job()
							.setReference(new JobReference()
									.setJobId(jobId))
							.setPlacement(new JobPlacement()
									.setClusterName("cluster-0d74"))
							.setHadoopJob(new HadoopJob()
									.setMainClass("InvertedIndex")
									.setJarFileUris(ImmutableList.of("gs://dataproc-staging-us-central1-610391583003-dqujerpe/II.jar"))
									.setArgs(ImmutableList.of("gs://dataproc-staging-us-central1-610391583003-dqujerpe/input_data/", "gs://dataproc-staging-us-central1-610391583003-dqujerpe/output_" + jobId)))))
			.execute();
			System.out.println("Job Submitted Successfully");
			
		} catch (IOException e) {
			System.out.println("Problem Submitting Job");
		}
		waitForJob(jobId);
		return jobId;
	}
	
	public static void uploadFile(File upload) throws IOException {
		String objectName = upload.getName();
		String folder = "input_data";
		String filePath = upload.getAbsolutePath();
		
		Storage storage = StorageOptions.newBuilder().setProjectId(projectID).build().getService();
		BlobId blobID = BlobId.of(bucketID, folder + "/" + objectName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobID).build();
		storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));
	}
	
	public static File getFiles() throws Exception {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose this file: " + fileChooser.getSelectedFile());
			File upload = fileChooser.getSelectedFile();
			return upload;
		} else {
			throw new FileNotFoundException();
		}
	}
}
