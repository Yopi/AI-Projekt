import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class TransitionMatrix {
	private final String taggerPath	  = "data/models/english-left3words-distsim.tagger";
	private final String corpusPath	  = "data/corpus/cb.txt";
	
	private double[]   initialSigns;
	private double[][] transitionMatrix;
	private HashMap<String, ArrayList<String>> dictionary;
	private String[] tags;
	
	public static void main(String[] args) throws IOException { 
		new TransitionMatrix(); 
	}
	
	public TransitionMatrix() throws IOException {
		MaxentTagger tagger = new MaxentTagger(taggerPath);
		
		initialSigns = new double[tagger.numTags()];
		
		// Create
		transitionMatrix = makeTransitionMatrix(tagger);
		dictionary = mapWords(tagger);
		tags = fillTags(tagger);
		
		// Normalize
		transitionMatrix = normalizeMatrix(transitionMatrix);
		initialSigns = normalizeArray(initialSigns);
	}
	
	/*
	 * Creates a new transition matrix that is not normalized.
	 * @param tagger the tagger...
	 * @return A new transition matrix that is not normalized.
	 */
	private double[][] makeTransitionMatrix(MaxentTagger tagger) throws IOException {
		double[][] tM = new double[tagger.numTags()][tagger.numTags()];
		
		BufferedReader in = new BufferedReader(new FileReader(corpusPath));
		
		String line;
		while ((line = in.readLine()) != null) { 	// Read all lines from corpus.
			line = tagger.tagString(line);			// Get tags for each word in line.
			String[] words = line.split("\\s");		// Make a list of single words.
			int previousTag = -1;					// There is no previous tag yet.
			for (String word : words) {				// For ever word in word...
				if (previousTag == -1) {			// Go in here if it is the first word of the line.
					previousTag = tagger.getTagIndex(word.split("_")[1]);
					initialSigns[previousTag]++;	
				} else {							// Go here if it is not the first word of the line.
					int currentTag = tagger.getTagIndex(word.split("_")[1]);
					tM[previousTag][currentTag]++;
					previousTag = currentTag;	
				}
			}
		}
		
		in.close();

		return tM;
	}
	
	/*
	 * Map each word to a word type.
	 * @param tagger  the tagger...
	 * @return A new HashMap (key = tag, value = list of words).
	 */
	private HashMap<String, ArrayList<String>> mapWords(MaxentTagger tagger) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(corpusPath));
		
		HashMap<String, ArrayList<String>> dictionary = new HashMap<String, ArrayList<String>>();
		
		String line;
		while ((line = in.readLine()) != null) {	// Read all lines of corpus.
			line = tagger.tagString(line);
			String[] words = line.split("\\s");
			for (String word : words) {
				String[] tempWord = word.split("_");
				addToDictionary(dictionary, tempWord[1], tempWord[0]);
			}
		}
		
		in.close();
		return dictionary;
	}
	
	private String[] fillTags(MaxentTagger tagger) {
		String[] tags = new String[tagger.numTags()];
		for (int i = 0; i < tagger.numTags(); i++) {
			tags[i] = tagger.getTag(i);
		}
		return tags;
	}
	
	/*
	 * Normalize a matrix. If the sum of a row is 0 then do nothing with that row.
	 * @param matrix the matrix to be normalized.
	 * @return the normalized matrix.
	 */
	private double[][] normalizeMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			double c = 1 / arraySum(matrix[i]);
			if (c == Double.POSITIVE_INFINITY || c == Double.NEGATIVE_INFINITY || c == Double.NaN) { // Vad ska vi göra om hela raden är 0?
				/*
				for (int j = 0; j < matrix[i].length; j++) {
					if (j == i) {
						matrix[i][j] = 1; 	// Låta den gå till sig själv endast?
					} else {
						matrix[i][j] = 0;
					}
				}
				*/
			} else {
				for (int j = 0; j < matrix[i].length; j++) {
					matrix[i][j] = c * matrix[i][j];
				}
			}
		}
		return matrix;
	}
	
	/*
	 * Normalize an array. If the sum of the array is 0 then do nothing, just return the array.
	 * @param array the array to be normalized.
	 * @return the normalized array.
	 */
	private double[] normalizeArray(double[] array) {
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
		
		list.add(word);
		dictionary.put(tag, list);
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
	
	/*
	 * Print a matrix to stdout, containing doubles, with 2 decimals.
	 * @param matrix the matrix to be printed to stdout.
	 */
	private void printMatrix(double[][] matrix) {
		for (double[] d : matrix) {
			for (double dd : d)
				System.out.print(String.format("%.2f", dd) + "\t");
			System.out.println();
		}
	}
}