import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class GenerateTweet {
	private final static int tweetLength = 2;
	
	public static void main(String[] args) throws IOException {
		TransitionMatrix tm = new TransitionMatrix();
		System.out.println("Created new TM");
		MatrixConstraints constrainedTM = new MatrixConstraints(tm.getMatrix(), tweetLength);
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
			
		}
		
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
	
	public static String getRandomWord(String type, HashMap<String, ArrayList<String>> dictionary) {
		ArrayList<String> words = dictionary.get(type);
		return words.get((int) (Math.random() * words.size()));
	}
}
