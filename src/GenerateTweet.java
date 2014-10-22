import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class GenerateTweet {
	private int maxLength = 30;
	private int minLength = 25;
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
		tM = new TransitionMatrix();
		tg = new TweetGenerator(tM, maxLength, nGramLength, minLength);
		
		System.out.println("Starting to generate tweets");
		Scanner sc = new Scanner(System.in);
		System.out.println("\nPress ENTER to proceed (q + ENTER to quit).\n");
        while (sc.hasNextLine()) {
            if (sc.nextLine().equals("q")) {
                break;
            }
			String tweet;
			tweet = getTweetNGram();
			System.out.println("With NGram: " + tweet);
			
			tweet = getTweetTM();
			System.out.println("With TM: " + tweet);	
			
			System.out.println("\nPress ENTER to proceed (q + ENTER to quit).\n");
        }
		sc.close();
	}
	
	public static void main(String[] args) {
		new GenerateTweet();
	}
}