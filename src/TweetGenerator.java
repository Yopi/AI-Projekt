import java.util.ArrayList;
import java.util.Iterator;


public class TweetGenerator {
	private int tweetLength, nGramLength, minLength;
	private NGram nGram;
	private TransitionMatrix tM;
	public TweetGenerator(TransitionMatrix transitionMatrix, int tL, int nGL, int mL) {
		tM = transitionMatrix;
		tweetLength = tL;
		nGramLength = nGL;
		minLength = mL;
		nGram = new NGram(nGramLength, tM.sentences);
	}
	
	public String generate(double[][] rules) {
		StringBuilder tweet = new StringBuilder();
		int wordsLeft = tweetLength;
		
		while (wordsLeft > minLength) {
			String sentence;
			if (rules == null) {
				sentence = nGramSentence(wordsLeft);
			} else {
				sentence = generateSentence(rules, wordsLeft);
			}
			if (sentence.split("\\s").length >= minLength) {
				tweet.append(sentence);
				wordsLeft -= sentence.split("\\s").length;
			}
		}
		
		return humanifyTweet(tweet.toString());
	}
	
	private String humanifyTweet(String tweet) {
        String[] tweetWords = tweet.split("\\s");
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
        tweet = sb.toString();
             
        String[] specialChars = new String[]{".", "!", "?", ",", "'", ":", ";"};
        for (String specialChar : specialChars) {
            tweet = tweet.replaceAll(" [" + specialChar + "]", specialChar);
        }
        return tweet;
    }
	
	// Loops through possible words and returns an arraylist with all terminals
	public ArrayList<String> terminalsFromPos(ArrayList<String> posWords) {
		ArrayList<String> posTerminals = new ArrayList<String>();
		for (String word : posWords) {
			if(split(word)[0].matches(Constants.terminalSign)) {
				posTerminals.add(word);
			}
		}
		
		return posTerminals;
	}
	
	// Splits a word into [0] = word and [1] = tag
	private String[] split(String word) {
		String[] s = new String[]{"", ""};
		int i = word.lastIndexOf("_");
		if (i < 0) {
			return s;
		}
		s[0] = word.substring(0, i);
		s[1] = word.substring(i + 1, word.length());
		return s;
	}
	
	public String generateSentence(double[][] matrix, int maxLength) {
		StringBuilder sentence = new StringBuilder();

		int index = 0;
		String lastWord = "";
		String[] tags = tM.getTags();
		int i = 0;
		while(true) {
			if(split(lastWord)[0].matches(Constants.terminalSign)) {
				return sentence.toString();
			}
			
			// Get all the next possible words from n-gram
			ArrayList<String> posWords = nGram.getNextWord(sentence.toString());
			if(posWords.size() == 0) break;
			
			if(i >= maxLength) {
				ArrayList<String> posTerminalSigns = terminalsFromPos(posWords);				
				if (posTerminalSigns.size() > 0) {
					posWords = posTerminalSigns;
				}
			}
			
			// Get the tags for all of the possible words
			ArrayList<String> posWordsTags = new ArrayList<String>();
			for (int j = 0; j < posWords.size(); j++) {
				String tag = split(posWords.get(j))[1];
				if (!posWordsTags.contains(tag)) {
					posWordsTags.add(tag);
				}
			}

			if(posWordsTags.size() == 0) {
				System.err.println("There are no possible word tags");
				break;
			}
			
			// Get the possible tags from the correct source
			double[] posTags;
			if (i != 0) {
				posTags = matrix[index].clone();
			} else {
				posTags = tM.getInitialDistribution().clone();
			}
			
			// Get out the tags that are among the possible words
			int tagResetCount = 0;
			for (int j = 0; j < posTags.length; j++) {
				boolean keepTag = false;
				for(String w : posWordsTags) {
					if (j == tM.getTagIndex(w)) {
						keepTag = true;
					}
				}
				// Set the probability to zero if it is not a tag among our possible ones
				if (!keepTag) {
					tagResetCount++;
					posTags[j] = 0;
				}
			}
			
			if(tagResetCount == posTags.length) 
				System.err.println("Number of reset tags = all tags");

			// Normalize
			posTags = tM.normalizeArray(posTags);
			index = probabilisticIndex(posTags);
			String type = tags[index];
			
			// Remove those words from the possible words that do not have the same type 
			// as the one we have gotten from probabilisticIndex
			Iterator<String> iterator = posWords.iterator();
			while(iterator.hasNext()) {
				String word = iterator.next();
				if(word.split("_")[1].contains(type) == false) {
					iterator.remove();
				}
			}

			if(posWords.size() == 0) {
				System.err.println("There are no possible words");
				break;
			}

			lastWord = posWords.get((int) (Math.random() * posWords.size()));
			sentence.append(lastWord + " ");
			i++;
		}
		
		return "";
	}
	
	// Generates a sentence with the n-gram
	public String nGramSentence(int maxLength) {
		StringBuilder sentence = new StringBuilder();
		
		// nGram is completely unrestrained
		String lastWord = "";
		int i = 0;
		while(true) {
			if(split(lastWord)[0].matches(Constants.terminalSign)) break;
			
			ArrayList<String> posWords = nGram.getNextWord(sentence.toString());
			if (posWords.size() == 0) break;
			
			if (i >= maxLength) {
				ArrayList<String> posTerminalSigns = terminalsFromPos(posWords);				
				if (posTerminalSigns.size() > 0) {
					posWords = posTerminalSigns;
				}
			}
			
			int index = (int)(Math.random() * posWords.size());
			lastWord = posWords.get(index);
			sentence.append(lastWord + " ");
			i++;
		}
		
		return sentence.toString();
	}

	// Returns the index of a number
	public int probabilisticIndex(double[] intervals) {
		//System.out.println(Arrays.toString(intervals));
		double prob = Math.random();
		double sum = 0d;
		for(int i = 0; i < intervals.length; i++) {
			sum += intervals[i];
			if(prob < sum) {
				return i;
			}
		}
		
		return 10;
	}
}