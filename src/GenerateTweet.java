import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class GenerateTweet {
	private final static int tweetLength = 12;
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		TransitionMatrix tm = new TransitionMatrix();
		System.out.println("Created new TM");
		double[][] constrainedTM = getConstrainedMatrix(tm.getMatrix(), tweetLength); 
		System.out.println("Created new cTM");
		HashMap<String, ArrayList<String>> dictionary = tm.getDictionary();
		String[] tags = tm.getTags();
		StringBuilder tweet = new StringBuilder();
		System.out.println("Starting to generate tweets");
		
		// 
		int index = probabilisticIndex(tm.getInitialDistribution());
		tweet.append(getRandomWord(tags[index], dictionary));
		tweet.append(" ");
		for(int i = 0; i < tweetLength; i++) {
			index = probabilisticIndex(tm.getMatrix()[index]);
			tweet.append(getRandomWord(tags[index], dictionary));
			tweet.append(" ");	
		}
		tweet.delete(tweet.length()-1, tweet.length());
		long endTime = System.currentTimeMillis();
		System.out.println("Time taken: " + (endTime - startTime) + "ms");
		System.out.println(tweet.toString());
		// Generate tweet with regular transitionmatrix
		// Constrain transition matrix
		// Generate new tweet
	}
	
	// Returns the index of a number
	public static int probabilisticIndex(double[] intervals) {
		double prob = Math.random();
		double sum = 0d;
		for(int i = 0; i < intervals.length; i++) {
			sum += intervals[i];
			if(prob < sum) {
				return i;
			}
		}
		
		return intervals.length-1;
	}
	
	public static double[][] getConstrainedMatrix(double[][] matrix, int tweetLength) throws IOException {
		double[][] newMatrix;
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("data/constrainedMatrix.bin"));
			int sizeOfMatrix = Integer.parseInt(br.readLine());
			newMatrix = new double[sizeOfMatrix][sizeOfMatrix];
			String partMatrix = "";
			int i = 0;
			while ((partMatrix = br.readLine()) != null) {
				String[] lineList = partMatrix.split(",");
				for (int j = 0; j < sizeOfMatrix; j++) {
					newMatrix[i][j] = Double.parseDouble(lineList[j]);
				}
				i++;
			}
			br.close();
		} catch(IOException e) {
			MatrixConstraints mc = new MatrixConstraints(matrix, tweetLength);
			newMatrix = mc.getConstrainedMatrix();
			// Save matrix to file
			File theFile = new File("data/constrainedMatrix.bin");
			theFile.createNewFile();
		    FileWriter fw = new FileWriter("data/constrainedMatrix.bin");
		    
		    fw.write("" + newMatrix.length);
		    for (int i = 0; i < newMatrix.length; i++) {
		    	fw.write("\n");
		    	for (int j = 0; j < newMatrix[0].length; j++) {
		    		fw.write(newMatrix[i][j] + ",");
		    	}
		    }
		    fw.close();
		}
		return newMatrix;
	}
	
	public static String getRandomWord(String type, HashMap<String, ArrayList<String>> dictionary) {
		ArrayList<String> words = dictionary.get(type);
		return words.get((int) (Math.random() * words.size()));
	}
}
