import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Statistics {
	private static final int tweets = 1000;
	private final int maxLength = 90;
	private final int minLength = 30;
	private final int nGramLength = 2;
	MaxentTagger tagger = new MaxentTagger(Constants.taggerPath);
	
	public static void main(String[] args) {
		new Statistics();
	}

	public Statistics() {
		GenerateTweet gt = new GenerateTweet(maxLength, minLength, nGramLength);
		int acceptedLength = nGramLength + 1;
		DecimalFormat df = new DecimalFormat("####0.00");
		
		System.err.print("Getting text from " + Constants.corpusPath + " ... ");
		long timeNow = System.currentTimeMillis();
		String text = getText();
		long endTime  = System.currentTimeMillis();
		System.err.println("done [" + df.format(((double) (endTime - timeNow) / 100)) + " sec]");
		
		
		double[] NGramScore = new double[tweets];
		double[] tMScore = new double[tweets];
		
		System.out.println("Starting statistical analysis");
		timeNow = System.currentTimeMillis();
		String tweet;
		int maxLength = 0;
		int minLength = Integer.MAX_VALUE;
		int over = 0;
		int under = 0;
        for (int i = 0; i < tweets; i++) {
        	//System.out.print(df.format(((double) (i) / tweets) * 100) + "% done...\r"); // \r does not work in Eclipse (bug from 2004).
        	tweet = gt.getTweetNGram();
        	maxLength = Math.max(tweet.length(), maxLength);
        	minLength = Math.min(tweet.length(), minLength);
        	if (tweet.length() <= this.maxLength) {
        		under++;
        	} else if (tweet.length() > this.maxLength) {
        		over++;
        	}
        	//System.out.println("With NGram:\t" + tweet);
			double tweetScoreNGram = analyseTweet(tweet, text, acceptedLength);
			//System.out.println("Score NGram: " + df.format(tweetScoreNGram * 100) + "%");
			
			tweet = gt.getTweetTM();
			maxLength = Math.max(tweet.length(), maxLength);
        	minLength = Math.min(tweet.length(), minLength);
        	if (tweet.length() <= this.maxLength) {
        		under++;
        	} else if (tweet.length() > this.maxLength) {
        		over++;
        	}
			//System.out.println("With TM:\t" + tweet);
			double tweetScoreTM = analyseTweet(tweet, text, acceptedLength);
			//System.out.println("Score TM: " + df.format(tweetScoreTM * 100) + "%");
			
			NGramScore[i] = tweetScoreNGram;
			tMScore[i] = tweetScoreTM;
        }
        endTime  = System.currentTimeMillis();
        System.out.println("Analyzed " + tweets + " tweets [" + df.format(((double) (endTime - timeNow) / 1000)) + " sec]");
        
        System.out.println("Longest tweet:\t" + maxLength + " characters");
        System.out.println("Shortest tweet:\t" + minLength + " characters");
        System.out.println("Number of tweets that is longer than " + this.maxLength + " characters:\t" + over);
        System.out.println("Number of tweets that is shorter than " + this.maxLength + " characters:\t" + under);
        System.out.println("NGram average score:\t" + df.format((sumArray(NGramScore) / tweets) * 100) + "%");
        System.out.println("TM average score:\t" + df.format((sumArray(tMScore) / tweets) * 100) + "%");
	}
	
	private double sumArray(double[] array) {
		double sum = 0d;
		for (double d : array) {
			sum += d;
		}
		return sum;
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
		
		String[] tempSentences = sb.toString().split("(?<=[.!?])");
		sb = new StringBuilder();
		for (String sentence : tempSentences) {
			sentence = sentence.replace("_", " ").trim();
			if(sentence.equals("")) continue;
			sentence = tagger.tagString(sentence);  // Get tags for each word in line.
			sentence = removeTags(sentence); // Remove all of the tags.
			sb.append(sentence + " ");
		}
		return humanifyText(sb.toString());
	}
	
	private String removeTags(String string) {
		StringBuilder sb = new StringBuilder();
		
		String[] words = string.split("\\s");
		for (String word : words) {
			word = word.split("_")[0];
			sb.append(word + " ");
		}
		return sb.toString().trim();
	}
	
	private String humanifyText(String text) {
        String[] tweetWords = text.split("\\s");
        StringBuilder sb = new StringBuilder();
        boolean newSentence = true;
        for (String word : tweetWords) {
            word = word.split("_")[0] + " ";
            if (newSentence) {
                sb.append(Character.toUpperCase(word.charAt(0)) + word.substring(1));
                newSentence = false;
            } else {
                sb.append(word);
            }
            if (word.trim().matches(Constants.terminalSign)) {
                newSentence = true;
            }
        }
        text = sb.toString();
             
        String[] specialChars = new String[]{".", "!", "?", ",", "'", ":", ";"};
        for (String specialChar : specialChars) {
        	text = text.replaceAll(" [" + specialChar + "]", specialChar);
        }
        //text = text.replaceAll("$ ", "$");
        return text;
    }
	
	private double analyseTweet(String tweet, String text, int acceptedLength) {
		double reuse = 0d;
		String[] tweetSentence = tweet.split(Constants.terminalSign);
		
		for (String sentence : tweetSentence) {							// For each sentence in tweet 
			double numWords = (double) sentence.split("\\s").length;	// Get number of words in sentence
			
			if (numWords == 0) {
				continue;
			}
			double sum = analyseSentence(sentence, text, acceptedLength);		
			reuse += sum;
		}
		// Average score per sentence in tweet
		reuse /= tweet.split("\\s").length;
		
		return 1d - reuse;
	}
	
	private double analyseSentence(String sentence, String text, int acceptedLength) {
		double score = 0d;
		String[] words = sentence.split("\\s");
		if (words.length < acceptedLength || (words.length == 1 && words[0].equals(""))) {
			return score;
		}

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
		
		int maxMatch = 0;
		while (combinations.size() > 0) {
			String s = combinations.get(0);
			
//			System.out.println(combinations.size());
//			System.out.println(s);
//			try {
//			    Thread.sleep(1000);                 //1000 milliseconds is one second.
//			} catch(InterruptedException ex) {
//			    Thread.currentThread().interrupt();
//			}
			
			double tempScore = 0d;
			if (s.split("\\s").length < maxMatch) {
				break;
			}
			if(text.contains(s)) {
				maxMatch = s.split("\\s").length;
				tempScore += maxMatch - acceptedLength;
				s = s.replace("$", "\\$");
				for (String newSentence : sentence.split(s, 2)) {
					tempScore += analyseSentence(newSentence, text, acceptedLength);
				}
				combinations.remove(0);
			} else {
				combinations.remove(0);
			}
			score = Math.max(score, tempScore);
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