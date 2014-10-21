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
	private final static int minLength = 3;
	
	static String[] tags;
	static HashMap<String, ArrayList<String>> dictionary;
	static double[] initialDistribution;
	static NGram nGram;
	static TransitionMatrix tM;
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		long indTime = System.currentTimeMillis();
		
		tM = new TransitionMatrix();
		System.out.println("Created new TM [" + (System.currentTimeMillis() - indTime) + " ms]");
		indTime = System.currentTimeMillis();
		
		//double[][] constrainedTM = getConstrainedMatrix(tM.getMatrix(), tweetLength); 
		//System.out.println("Created new cTM [" + (System.currentTimeMillis() - indTime) + " ms]");
		//indTime = System.currentTimeMillis();
		
		nGram = new NGram(nGramLength);
		System.out.println("Created NGram of length " + nGramLength + " [" + (System.currentTimeMillis() - indTime) + " ms]");
		
		dictionary = tM.getDictionary();
		tags = tM.getTags();
		initialDistribution = tM.getInitialDistribution();
		
		System.out.println("Starting to generate tweets");
		String tweet;
		indTime = System.currentTimeMillis();
		tweet = generateTweet(null, false);
		System.out.println("With NGram [" + (System.currentTimeMillis() - indTime) + " ms]:\t" + tweet);
		
		indTime = System.currentTimeMillis();
		tweet = generateTweet(tM.getMatrix(), false);
		System.out.println("Without constraints [" + (System.currentTimeMillis() - indTime) + " ms]:\t" + tweet);
		
		//indTime = System.currentTimeMillis();
		//tweet = generateTweet(constrainedTM, true);
		//System.out.println("With constraints [" + (System.currentTimeMillis() - indTime) + " ms]:\t" + tweet);
		
		long endTime = System.currentTimeMillis();
		System.out.println("Total time taken: " + (endTime - startTime) + " ms");
		// Generate tweet with regular transitionmatrix
		// Constrain transition matrix
		// Generate new tweet
	}
	
	public static String generateTweet(double[][] rules, boolean constrained) throws IOException {
		StringBuilder tweet = new StringBuilder();
		int wordsLeft = tweetLength;
		
		while (wordsLeft > minLength) {
			String sentence = generateSentence(rules, wordsLeft, constrained);
			//System.out.println(sentence);
			if (sentence.split("\\s").length >= minLength) {
				tweet.append(sentence);
				tweet.replace(tweet.length()-1, tweet.length(), ". ");
				wordsLeft -= sentence.split("\\s").length;
			}
		}
		
		return tweet.toString();
	}
	
	public static String generateSentence(double[][] matrix, int maxLength, boolean constrained)  throws IOException {
		StringBuilder sentence = new StringBuilder();
		
		// nGram is completely unrestrained
		if (matrix == null) {
			for (int i = 0; i < maxLength; i++) {
				ArrayList<String> posWords = nGram.getNextWord(sentence.toString());
				if (posWords.size() > 0) {
					int index = (int)(Math.random() * posWords.size());
					sentence.append(posWords.get(index));
					sentence.append(" ");
				} else
					break;
			}
			return sentence.toString();
		}
		
		int retries = 0;
		int maxRetries = 15;
		int index = probabilisticIndex(initialDistribution);
		for (int i = 0; i < maxLength && retries < maxRetries; i++) {	
			//System.out.println(sentence.toString());
			if (index == 10) { // If Terminal Sign
				if (i+1 >= minLength) {
					return sentence.toString();
				} else {
					index = probabilisticIndex(initialDistribution);
				}
			} else if (index == 1) { // If ','
				if (sentence.length() == 0) {
					i--;
					continue;
				}
				sentence.replace(sentence.length()-1, sentence.length(), ", "); // Tar död på heapen
				i--;
				index = probabilisticIndex(matrix[index]);
				continue;
			}

			// Selected word-type
			String type = tags[index];
			
			// Get all possible next words, remove words of wrong type
			ArrayList<String> posWords = nGram.getNextWord(sentence.toString());
			for (int j = 0; j < posWords.size(); j++) {
				//System.out.println("Tag: " + type + ", posTag: " + tM.getTag(posWords.get(j)) + ", equals: " + tM.getTag(posWords.get(j)).equals(type));
				if (!tM.getTag(posWords.get(j)).equals(type)) {
					posWords.remove(j);
					j--;
				}
			}
			// If there are no possible words left, try again
			if (posWords.size() == 0) {
				retries++;
				i--;
				continue;
			}
			// Pick one acceptable word randomly and append to sentence
			String sWord = posWords.get((int) (Math.random() * posWords.size()));
			sentence.append(sWord);
			sentence.append(" ");
			
			retries = 0;
			index = probabilisticIndex(matrix[index]);
		}
		
		if (retries == maxRetries || constrained)
			return ""; //throw new Exception("HÄR BLEV DET FEL :(");
		return sentence.toString();
	}
	
//	public static String generateTweet(double[][] matrix) {
//		int index = probabilisticIndex(initialDistribution);
//		StringBuilder tweet = new StringBuilder();
//		tweet.append(getRandomWord(tags[index], dictionary));
//		tweet.append(" ");
//		for(int i = 0; i < tweetLength-1; i++) {
//			index = probabilisticIndex(matrix[index]);
//			if (index == 10) {
//				tweet.replace(tweet.length()-1, tweet.length(), ". ");
//				index = probabilisticIndex(initialDistribution);
//				if (i == tweetLength - 2)
//					break;
//			} else if (index == 1) {
//				tweet.replace(tweet.length()-1, tweet.length(), ", ");
//				i--;
//			}
//			tweet.append(getRandomWord(tags[index], dictionary));
//			tweet.append(" ");
//		}
//		tweet.delete(tweet.length()-1, tweet.length());
//		
//		return tweet.toString();
//	}
	
	public static String nGramTweet() throws IOException {
		StringBuilder tweet = new StringBuilder();
		
		for (int i = 0; i < tweetLength && tweet.length() < 120; i++) {
			ArrayList<String> potWords = nGram.getNextWord(tweet.toString());
			if (potWords.size() > 0) {
				int randIndex = (int)(Math.random() * potWords.size());
				tweet.append(potWords.get(randIndex) + " ");
			} else {
				tweet.replace(tweet.length()-1, tweet.length(), ". ");
				if (i == tweetLength-1) {
					break;
				}
			}
			
			// Remove strange things.
			String[] s = tweet.toString().split(Constants.terminalSign);
			tweet = new StringBuilder();
			i = 0;
			for (int j = 0; j < s.length; j++) {
				String t = s[j];
				if (t.split("\\s").length > 1 || (t.split("\\s").length == 1 && j == s.length-1)) {
					tweet.append(t + ". ");
					i++;
				}
			}
			if (i == tweetLength-1) {
				break;
			}
			tweet.replace(tweet.length()-2, tweet.length(), "");
		}
		
		return tweet.toString();
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
		
		return 10;
	}
	
	public static double[][] getConstrainedMatrix(double[][] matrix, int tweetLength) throws IOException {
		double[][] newMatrix;
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("data/constrainedMatrix.bin"));
			
			// So it creates a new matrix if the current is with wrong tweetLength.
			if (Integer.parseInt(br.readLine()) != tweetLength) {
				br.close();
				throw new IOException();
			}
			
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
		    
		    fw.write(tweetLength + "\n");		// Save tweetLength as the first line of the file.
		    fw.write(newMatrix.length + "\n");	// Save the newMatrix length as the second line of the file.
		    for (int i = 0; i < newMatrix.length; i++) {
		    	for (int j = 0; j < newMatrix[0].length; j++) {
		    		fw.write(newMatrix[i][j] + ",");
		    	}
		    	if (i+1 < newMatrix.length) {
		    		fw.write("\n");
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
