import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class GenerateTweet {
	private final static int tweetLength = 12;
	private final static int nGramLength = 2;
	private final static int minLength = 4;
	
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
			String sentence;
			if (rules == null) {
				sentence = nGramSentence(wordsLeft, "");
			} else {
				sentence = generateSentence(rules, wordsLeft, constrained, "");
			}
			if (sentence.split("\\s").length >= minLength) {
				tweet.append(sentence);
				wordsLeft -= sentence.split("\\s").length;
			}
		}
		
		return humanifyTweet(tweet.toString());
	}
	
	private static String humanifyTweet(String tweet) {
		String[] tweetWords = tweet.split("\\s");
		StringBuilder sb = new StringBuilder();
		for (String word : tweetWords) {
			sb.append(split(word)[0] + " ");
		}
		tweet = sb.toString();
			
		String[] specialChars = new String[]{".", "!", "?", ","};
		for (String specialChar : specialChars) {
			tweet = tweet.replaceAll(" [" + specialChar + "]", specialChar);
		}
		return tweet;
	}
	
	public static String nGramSentence(int maxLength, String currentSentence) throws IOException {
		StringBuilder sentence = new StringBuilder();
		
		// nGram is completely unrestrained
		String lastWord = "";
		for (int i = 0; i < maxLength || !lastWord.matches(Constants.terminalSign); i++) {
			//System.out.println(sentence.toString());
			ArrayList<String> posWords = nGram.getNextWord(currentSentence + sentence.toString());
			for (String s : posWords) {
				//System.out.print(s + ", ");
			}
			//System.out.println();
			if (posWords.size() > 0) {
				ArrayList<String> posTerminalSigns = new ArrayList<String>();
				if (i >= maxLength) {
					for (int j = 0; j < posWords.size(); j++) {
						if (split(posWords.get(j))[0].matches(Constants.terminalSign)) {
							posTerminalSigns.add(posWords.get(j));
						}
					}
				}
				if (posTerminalSigns.size() > 0) {
					posWords = posTerminalSigns;
				}
				int index = (int)(Math.random() * posWords.size());
				String word = posWords.get(index);
				sentence.append(word + " ");
				if (split(word)[0].matches(Constants.terminalSign)) {
					return sentence.toString();
				}
				
			} else
				break;
		}
		return sentence.toString();
	}
	
	private static String[] split(String word) {
		String[] s = new String[]{"", ""};
		int i = word.lastIndexOf("_");
		if (i < 0) {
			return s;
		}
		s[0] = word.substring(0, i);
		s[1] = word.substring(i + 1, word.length());
		return s;
	}
	
	
	public static String generateSentence(double[][] matrix, int maxLength, boolean constrained, String currentSentence)  throws IOException {
		StringBuilder sentence = new StringBuilder();

		int index = 0;
		String lastWord = "";
		for (int i = 0; i < maxLength || !split(lastWord)[0].matches(Constants.terminalSign); i++) {
			System.out.println(sentence.toString());
			ArrayList<String> posWords = nGram.getNextWord(currentSentence + sentence.toString());
			if (posWords.size() > 0) {
				ArrayList<String> posTerminalSigns = new ArrayList<String>();
				if (i >= maxLength) {
					for (int j = 0; j < posWords.size(); j++) {
						if (split(posWords.get(j))[0].matches(Constants.terminalSign)) {
							posTerminalSigns.add(split(posWords.get(j))[0]);
						}
					}
				}
				if (posTerminalSigns.size() > 0) {
					posWords = posTerminalSigns;
				}
				ArrayList<String> posWordsTags = new ArrayList<String>();
				for (int j = 0; j < posWords.size(); j++) {
					//System.out.println("Word: " + posWords.get(j) + ", Tag: " + tM.getTag(posWords.get(j)) + ", TagIndex: " + tM.getTagIndex(tM.getTag(posWords.get(j))));
					String tag = split(posWords.get(j))[1];
					if (!posWordsTags.contains(tag)) {
						posWordsTags.add(tag);
					}
				}
				if (posWordsTags.size() > 0) {
				
					double[] posTags;
					if (i != 0) {
						posTags = matrix[index].clone();
					} else {
						posTags = initialDistribution.clone();
					}
					
					//System.out.println("Första");
					//System.out.println(Arrays.toString(posTags));
					for (int j = 0; j < posTags.length; j++) {
						boolean keepTag = false;
						for (int k = 0; k < posWordsTags.size(); k++) {
							//System.out.println("j: " + j + ", pWT: " + tM.getTagIndex(posWordsTags.get(k)));
							if (j == tM.getTagIndex(posWordsTags.get(k))) {
								keepTag = true;
							}
						}
						if (!keepTag) {
							posTags[j] = 0;
						}
					}
					//System.out.println("Andra");
					//System.out.println(Arrays.toString(posTags));
					posTags = tM.normalizeArray(posTags);
					
					index = probabilisticIndex(posTags);
					String type = tags[index];
					
					for (int j = 0; j < posWords.size(); j++) {
						System.out.println("Tag: " + type + ", posTag: " + tM.getTag(posWords.get(j)) + ", equals: " + tM.getTag(posWords.get(j)).equals(type));
						if (!split(posWords.get(j))[1].equals(type)) {
							posWords.remove(j);
							j--;
						}
					}
					
					if (posWords.size() > 0) {
						String sWord = posWords.get((int) (Math.random() * posWords.size()));
						sentence.append(sWord + " ");
						if (split(sWord)[0].matches(Constants.terminalSign)) {
							System.out.println(sentence.toString());
							return sentence.toString();
						}
					} else {
						System.err.println("Poswords är tom i slutet");
						break;
					}
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return "";
	}
	
	// Returns the index of a number
	public static int probabilisticIndex(double[] intervals) {
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
