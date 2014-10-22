import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Statistics {
	private static final int tweets = 100;
	private String text;
	
	public static void main(String[] args) {
		new Statistics();
	}

//	public Statistics(TransitionMatrix tM, TweetGenerator tg, int acceptedLength) {
	public Statistics() {
		text = getText();
		System.out.println("Starting statistical analysis");
		analyseSentence("Hej jag heter Daniel", 0);
        for (int i = 0; i < tweets; i++) {
//			double tweetScoreNGram = analyseTweet(tg.generate(null), acceptedLength);
			//System.out.println("With NGram [" + (System.currentTimeMillis() - indTime) + " ms]:\t" + tweet);
			
//			double tweetScoreTM = analyseTweet(tg.generate(tM.getMatrix()), acceptedLength);
			//System.out.println("With TM [" + (System.currentTimeMillis() - indTime) + " ms]:\t" + tweet);
        }
        System.out.println("Analyzed " + tweets + " tweets");
	}
	
	private String getText() {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Constants.corpusPath));
			
			String line;
			while ((line = in.readLine()) != null) { 	// Read all lines from corpus.
				if (line.equals(""))
					continue;
				sb.append(line.trim() + " ");
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Fail reading error.");
			System.exit(1);
		}
		return sb.toString();
	}
	
	private double analyseTweet(String tweet, int acceptedLength) {
		double reuse = 0d;
		String[] tweetSentence = tweet.split(Constants.terminalSign);
		
		for (String sentence : tweetSentence) {							// For each sentence in tweet 
			double numWords = (double) sentence.split("\\s").length;	// Get number of words in sentence
			
			double sum = analyseSentence(sentence, acceptedLength);						// Score for this sentence
			
			reuse += sum / numWords;
		}
		
		// Average score per sentence in tweet
		reuse /= tweetSentence.length;
		
		return 1d - reuse;
	}
	
	private double analyseSentence(String sentence, int acceptedLength) {
		double score = 0d;
		String[] words = sentence.split("\\s");

		ArrayList<String> combinations = stringCombinations(words, acceptedLength);
		Collections.sort(combinations, new Comparator<String>() {
		    public int compare(String x, String y) {
		        if(x.length() > y.length()) {
		            return -1;
		        } else if(x.length() == y.length()) {
		            return 0;
		        } else {
		            return 1;
		        }
		    }
		});
		
		while (combinations.size() > 0) {
			if(text.contains(combinations.get(0))) {
				score += combinations.get(0).split("\\s").length - acceptedLength;
				
			} else {
				combinations.remove(0);
			}
		}
		
		return score;
	}
	
	private ArrayList<String> stringCombinations(String[] words, int acceptedLength) {
		int numWords = words.length;
		ArrayList<String> combinations = new ArrayList<String>();
		for(int c = 0; c < numWords; c++) {
			for(int i = 1; i <= numWords - c; i++) {
				if (i >= acceptedLength) {
					String[] s = new String[i];
					System.arraycopy(words, c, s, 0, i);
					combinations.add(arrayToString(s));
				}
			}
		}
		
		return combinations;
	}
	
	private String arrayToString(String[] a) {
		StringBuilder sb = new StringBuilder();
		for (String s : a) {
			sb.append(s);
			sb.append(" ");
		}
		
		return sb.toString().trim();
	}
}