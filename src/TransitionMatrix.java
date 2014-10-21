import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class TransitionMatrix {
	private double[]   initialSigns;
	private double[][] transitionMatrix;
	private HashMap<String, ArrayList<String>> dictionary;
	private String[] tags;
	private int terminalTagIndex;
	public MaxentTagger tagger;
	
	public static void main(String[] args) throws IOException { 
		new TransitionMatrix(); 
	}
	
	public TransitionMatrix() throws IOException {
		tagger = new MaxentTagger(Constants.taggerPath);
		terminalTagIndex = tagger.getTagIndex(".");
		
		initialSigns = new double[tagger.numTags()];
		
		// Create
		fillTransitionMatrix();
		//mapWords(tagger);
		fillTags();
		
		// Normalize
		normalizeTransitionMatrix();
		initialSigns = normalizeArray(initialSigns);
		
		/*
		for (int i = 0; i < transitionMatrix.length; i++) {
			for (int j = 0; j < transitionMatrix.length; j++) {
				System.out.print(transitionMatrix[i][j] + "\t");
			}
			System.out.println();
		}
		*/
		System.out.println(tagger.getTag(tagger.numTags()-3));
		System.out.println(tagger.getTag(43));
		System.out.println(tagger.getTag(45));
	}
	
	/*
	 * Creates a new transition matrix that is not normalized.
	 * @param tagger the tagger...
	 * 
	 * +
	 * 
	 * Creates HashMap...
	 */
	private void fillTransitionMatrix() throws IOException {
		transitionMatrix = new double[tagger.numTags()][tagger.numTags()];
		dictionary = new HashMap<String, ArrayList<String>>();
		
		BufferedReader in = new BufferedReader(new FileReader(Constants.corpusPath));
		
		String[] splitSign = new String[]{".", "!", "?", ","};
		
		int previousTag = -1;							// There is no previous tag yet.
		String line;
		while ((line = in.readLine()) != null) { 	// Read all lines from corpus.
			//if (line.equals(""))
			//	continue;
			String[] words;
			StringBuilder sb;
			String endSymbol = line.substring(line.length()-1);
			boolean terminalSign = false;
			line = line.substring(0, line.length());
			for (String sign : splitSign) {
				if (sign.equals(endSymbol)) {
					terminalSign = true;
				}
				words = line.split("\\" + sign);
				sb = new StringBuilder();
				for (int i = 0; i < words.length; i++) {
					if (i == words.length-1) {
						sb.append(words[i]);
						break;
					}
					sb.append(words[i] + " " + sign);
				}
				line = sb.toString().trim();
			}
			if (terminalSign) {
				line += " " + endSymbol;
			}
			
			line = tagger.tagString(line);					// Get tags for each word in line.
			words = line.split("\\s");
			boolean newSentence = false;
			for (String word : words) {						// For ever word in word...
				if (previousTag == -1) {				// Go in here if it is the first word of the line.
					previousTag = tagger.getTagIndex(word.split("_")[1]);
					if (previousTag != -1) {
						initialSigns[previousTag]++;	
					}
				} else {								// Go here if it is not the first word of the line.
					int currentTag = tagger.getTagIndex(word.split("_")[1]);
					if (currentTag != -1) {
						transitionMatrix[previousTag][currentTag]++;
						previousTag = currentTag;
						if (newSentence) {
							initialSigns[currentTag]++;
							newSentence = false;
						}
					}
				}
				// Add word to dictionary.
				String[] tempWord = split(word);
				addToDictionary(dictionary, tempWord[1], tempWord[0]);
				
				newSentence = tempWord[0].matches(Constants.terminalSign);
			}
			// Last tag goes to terminal sign.
			if (previousTag != -1) {
				transitionMatrix[previousTag][terminalTagIndex]++;
			}
		}
		in.close();
	}
	
	private String[] split(String word) {
		String[] s = new String[2];
		int i = word.lastIndexOf("_");
		s[0] = word.substring(0, i);
		s[1] = word.substring(i + 1, word.length());
		return s;
	}
	
	private void fillTags() {
		tags = new String[tagger.numTags()];
		for (int i = 0; i < tagger.numTags(); i++) {
			tags[i] = tagger.getTag(i);
		}
	}
	
	/*
	 * Normalize a matrix. If the sum of a row is 0 then do nothing with that row.
	 * @param matrix the matrix to be normalized.
	 */
	private void normalizeTransitionMatrix() {
		for (int i = 0; i < transitionMatrix.length; i++) {
			double c = 1 / arraySum(transitionMatrix[i]);
			if (c == Double.POSITIVE_INFINITY || c == Double.NEGATIVE_INFINITY || c == Double.NaN) { // Vad ska vi göra om hela raden är 0?
				/*
				for (int j = 0; j < matrix[i].length; j++) {
					if (j == i) {
						transitionMatrix[i][j] = 1; 	// Låta den gå till sig själv endast?
					} else {
						transitionMatrix[i][j] = 0;
					}
				}
				*/
			} else {
				for (int j = 0; j < transitionMatrix[i].length; j++) {
					transitionMatrix[i][j] = c * transitionMatrix[i][j];
				}
			}
		}
	}
	
	/*
	 * Normalize an array. If the sum of the array is 0 then do nothing, just return the array.
	 * @param array the array to be normalized.
	 * @return the normalized array.
	 */
	public double[] normalizeArray(double[] array) {
		double c = 1 / arraySum(array);
		for (int i = 0; i < array.length; i++) {
			array[i] = c * array[i];
		}
		return array;
	}
	
	/*
	 * @param dArray the array to sum.
	 * @return the sum of all elements in dArray.
	 */
	public double arraySum(double[] dArray) {
		double sum = 0.0;
		for (double d : dArray) {
			sum += d;
		}
		return sum;
	}
	
	/*
	 * Add a word to the dictionary with key == tag
	 * @param dictionary the dictionary
	 * @param tag the tag
	 * @param word the word
	 */
	private void addToDictionary(HashMap<String, ArrayList<String>> dictionary, String tag, String word) {
		ArrayList<String> list = dictionary.get(tag);
		if(list == null) list = new ArrayList<String>();
		
		//System.out.println(word + "\t" + tag);
		list.add(word);
		dictionary.put(tag, list);
	}
	
	public String getTag(String word) {
		String[] s = tagger.tagString(word).split("_");
//		System.out.println(s[s.length - 1]);
		return s[s.length - 1].trim();
	}
	
	public double[][] getMatrix() {
		return transitionMatrix;
	}
	
	public HashMap<String, ArrayList<String>> getDictionary() {
		return dictionary;
	}
	
	public String[] getTags() {
		return tags;
	}
	
	public double[] getInitialDistribution() {
		return initialSigns;
	}
	
	public int getTagIndex(String tag) {
		return tagger.getTagIndex(tag);
	}
}