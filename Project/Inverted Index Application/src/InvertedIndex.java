import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class InvertedIndex {

  public static class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, Text> {
	  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		  
		  List<String> stopList = Arrays.asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now");
		  
		  //Get the file names in order to map them to the words
		  String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
		  
		  //Get only alphabetic and numeric values and spaces and puts them in a string
		  String line = value.toString().replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
		  
		  //Breaks the line up into tokens
		  StringTokenizer tokenizer = new StringTokenizer(line);
		  
		  
		  //While there are more tokens
		  while(tokenizer.hasMoreTokens()) {
			  
			  //Get the next token
			  String token = tokenizer.nextToken();
			  if(stopList.contains(token)) {
				  continue;
			  }
			  
			  //Store key value pair of word, and filename
			  context.write(new Text(token), new Text(fileName));
		  }
	  }
  }

  public static class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {
	  
	  public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		  HashMap<String, Integer> out = new HashMap<String, Integer>();
		  
		  for(Text value : values) {
			  String word = value.toString();
			  if(out.containsKey(word) == false) {
				  out.put(word, 1);
			  } else {
				  out.put(word, out.get(word) + 1);
			  }
		  }
		  StringBuilder listing = new StringBuilder();
		  for(String doc : out.keySet()) {
			  listing.append(doc + ":" + out.get(doc) + " ");
		  }
		  context.write(key, new Text(listing.toString()));
	  }
      
  }

  public static void main(String[] args) throws Exception {
	//In case wrong input was provided.
	if(args.length != 2) {
		 System.err.println("Usage: Inverted Index <input path> <output path>");
		 System.exit(0);
	}
	
	Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Inverted Index");
    job.setJarByClass(InvertedIndex.class);
    
    job.setMapperClass(InvertedIndexMapper.class);
    job.setReducerClass(InvertedIndexReducer.class);
    
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}