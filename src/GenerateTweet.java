import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class GenerateTweet {
	private final static int tweetLength = 15;
	private final static int nGramLength = 2;
	private final static int minLength = 5;
	
	static String[] tags;
	static HashMap<String, ArrayList<String>> dictionary;
	static double[] initialDistribution;
	static TransitionMatrix tM;
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		long indTime = System.currentTimeMillis();

		tM = new TransitionMatrix();
		System.out.println("Created new TM [" + (System.currentTimeMillis() - indTime) + " ms]");
		
		TweetGenerator tg = new TweetGenerator(tM, tweetLength, nGramLength, minLength);
		indTime = System.currentTimeMillis();
		
		System.out.println("Starting to generate tweets");
		String tweet;
		indTime = System.currentTimeMillis();
		tweet = tg.generate(null);
		System.out.println("With NGram [" + (System.currentTimeMillis() - indTime) + " ms]:\t" + tweet);
		
		indTime = System.currentTimeMillis();
		tweet = tg.generate(tM.getMatrix());

		System.out.println("With TM [" + (System.currentTimeMillis() - indTime) + " ms]:\t" + tweet);	
		System.out.println("Total time taken: " + (System.currentTimeMillis() - startTime) + " ms");
	}
	
	public static String getRandomWord(String type, HashMap<String, ArrayList<String>> dictionary) {
		ArrayList<String> words = dictionary.get(type);
		return words.get((int) (Math.random() * words.size()));
	}
}