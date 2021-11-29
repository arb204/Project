What's implemented:
  GCP cluster communication from base application.
  Inverted index
What's not implemented
  Service application is not dockerized
  Top N and Search functionality.
  
  Video: https://youtu.be/DviNJuy_m1U

How to Run Application (I go over most of this in my demo):
  
  NOTE: I do not fully understand how my code will be tested as I cannot have the cluster open so the grader can grade the project. I am going to try to make my video as detailed as possible so the Project does not need to be ran by the TA or Professor. If the TA or Professor wants to run my it is assumed they know how to get thier own json file for google cloud authentication, and will be able to upload my inverted index jave file to thier cluster to compile it and move the jar file back to their bucket so my application can call jobs on the jar file. 
  
  NOTE: I had an implementation with being able to choose what path you download files too, but it wasn't running properly so now it downloads to a folder specific to my local machine. You will want to change the code to download to a filepath on your local machine in the downloadOutput function in this line 
  blob.downloadTo(Paths.get("C:/Users/blott/Onedrive/Desktop/Project/output.txt")); "C:/Users/blott/Onedrive/Desktop/Project/output.txt" is what will be changed.

  1. It's a maven project so I have been running it with eclipse. You'll need to drag the content from the maven service app file and place it into your eclipse workspace, and run   it as a maven project.
  2. Right click on the pom.xml file in eclipse click on maven -> update project...
  3. Then run the project as maven install you should get no errors. 
  4. Next you want to click on the outermost file of the project, and select run configurations.
  5. Here your gonna update the environment variables for google authentication.
    GOOGLE_APPLICATION_CREDENTIALS = the path to the json file that authenicates your cluster. (I sent a json file for my cluster to the professor in an email, but figure it       probably won't be used.
  6. You can now run the project, and it should work provided that everything is properly set up with the cluster.
  7. Follow the prompts to the project, when uploading files you there will be a JFileChooser may appear behind the eclipse screen so you might have to minimize eclipse to get to it.
  8. If all the prompts are followed correctly one should be able to download the output of Inverted Index into the filepath referred to above.
  
  RESOURCES USED: 
    https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html
    https://cloud.google.com/docs/authentication/getting-started#windows
    https://stackoverflow.com/questions/35704048/what-is-the-best-way-to-wait-for-a-google-dataproc-sparkjob-in-java
    https://cloud.google.com/storage/docs/uploading-objects#storage-upload-object-code-sample
    https://stackoverflow.com/questions/35611770/how-do-you-use-the-google-dataproc-java-client-to-submit-spark-jobs-using-jar-fi
    https://stackoverflow.com/questions/24187243/how-to-use-compose-on-gcs-using-the-java-client
    https://github.com/GoogleCloudPlatform/java-docs-samples/blob/HEAD/auth/src/main/java/com/google/cloud/auth/samples/AuthExample.java
    https://stackoverflow.com/questions/35611770/how-do-you-use-the-google-dataproc-java-client-to-submit-spark-jobs-using-jar-fi
    https://stackoverflow.com/questions/66249053/com-google-api-client-json-jackson2-jacksonfactory-is-deprecated-what-are-my
    https://stackoverflow.com/questions/66226327/list-blobs-in-a-directory-on-google-cloud-storage-returns-wrong-result
  
    
