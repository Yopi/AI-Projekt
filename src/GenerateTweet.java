import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class GenerateTweet {
	private int maxLength = 130;
	private int minLength = 30;
	private int nGramLength = 2;
	
	static String[] tags;
	static HashMap<String, ArrayList<String>> dictionary;
	static double[] initialDistribution;
	static TransitionMatrix tM;
	TweetGenerator tg;
	
	public GenerateTweet(int maxL, int minL, int nGL) {
		maxLength = maxL;
		minLength = minL;
		nGramLength = nGL;
		
		tM = new TransitionMatrix();
		tg = new TweetGenerator(tM, maxLength, nGramLength, minLength);
	}
	
	public String getTweetNGram() {
		return tg.generate(null);
	}
	
	public String getTweetTM() {
		return tg.generate(tM.getMatrix());
	}
	
	public GenerateTweet() {
		System.out.println("Building the transition matrix.");
		long timeNow = System.currentTimeMillis();
		tM = new TransitionMatrix();
		long endTime = System.currentTimeMillis();
		System.out.println("Transition matrix complete [" + (endTime - timeNow) + " ms]");
		
		System.out.println("Initializing the tweet generator.");
		timeNow = System.currentTimeMillis();
		tg = new TweetGenerator(tM, maxLength, nGramLength, minLength);
		endTime = System.currentTimeMillis();
		System.out.println("Tweet generator initialized [" + (endTime - timeNow) + " ms]");
		
		System.out.println("Starting to generate tweets");
		Scanner sc = new Scanner(System.in);
		System.out.println("\nPress ENTER to proceed (q + ENTER to quit).\n");
        while (sc.hasNextLine()) {
            if (sc.nextLine().equals("q")) {
                break;
            }
			String tweet;
			
			timeNow = System.currentTimeMillis();
			tweet = getTweetNGram();
			endTime = System.currentTimeMillis();
			System.out.println("With NGram [" + (endTime - timeNow) + " ms]:\t" + tweet);
			
			timeNow = System.currentTimeMillis();
			tweet = getTweetTM();
			endTime = System.currentTimeMillis();
			System.out.println("With TM [" + (endTime - timeNow) + " ms]:\t" + tweet);	
			
			System.out.println("\nPress ENTER to proceed (q + ENTER to quit).\n");
        }
		sc.close();
	}
	
	public static void main(String[] args) {
		new GenerateTweet();
	}
}