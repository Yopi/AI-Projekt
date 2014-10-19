import java.io.BufferedReader;
import java.io.FileReader;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class TransitionMatrix {
	private final String taggerPath	  = "data/models/english-left3words-distsim.tagger";
	private final String corpusPath	  = "data/corpus/cb.txt";
	private final String terminalSign = "[.?!]";
	private final String unknownTag	  = "?";
	
	private double[]   initialSigns;
	private double[][] transitionMatrix;

	public static void main(String[] args) throws Exception { new TransitionMatrix(); }
	
	public TransitionMatrix() throws Exception {
		MaxentTagger tagger = new MaxentTagger(taggerPath);
		BufferedReader in = new BufferedReader(new FileReader(corpusPath));
		int maxTags = tagger.numTags() + 1;
		initialSigns = new double[maxTags];
		double[][] tM = new double[maxTags][maxTags];			// Temporary transition matrix
		
		String line;
		while ((line = in.readLine()) != null) {				// Read each line in corpus
			String[] sentences = line.split(terminalSign);		// Split row into sentences
			for (String sentence : sentences) {
				sentence = tagger.tagString(sentence);			// Add tags to each word
				
				String[] words = sentence.split("\\s");			// Split sentence into words
				String[] tag = new String[words.length];
				for (int i = 0; i < tag.length; i++)
					try { tag[i] = words[i].split("_")[1]; }	// Get tag of each word
					catch (Exception e) {
						tag[i] = unknownTag;					// If word isn't tagged, put unknownTag
					}
				
				// Increase tag of first word in initialSigns array
				int i = getTagIndex(tagger, tag[0]);
				initialSigns[i]++;
				
				for (int k = 1; k < tag.length; k++) {			// Iterate through each tag in the sentence
					int j = getTagIndex(tagger, tag[k]);

					tM[i][j]++;									// Increase how many times tag i goes to tag j
					i = j;
				}
				
				tM[i][getTagIndex(tagger, ".")]++;				// Last tag goes to terminal sign
			}
		}
		in.close();
		
		double[] c = new double[maxTags];
		transitionMatrix = new double[maxTags][maxTags];		// Transition matrix with non-zero rows/columns
		for (int i = 0; i < tM.length; i++) {
			for (int j = 0; j < tM.length; j++) {
				transitionMatrix[j][i] = tM[i][j];		// Transposes the temporary matrix
				c[j] += transitionMatrix[j][i];
			}
		}
		
		// Normalize initialSigns
		double c0 = 0;
		for (int i = 0; i < initialSigns.length; i++)
			c0 += initialSigns[i];
		for (int i = 0; i < initialSigns.length; i++)
			initialSigns[i] *= c0;
		
		// Normalize transition matrix
		for (int i = 0; i < maxTags; i++)
			if (c[i] != 0) {
				c[i] = 1 / c[i];
				for (int j = 0; j < maxTags; j++)
					transitionMatrix[i][j] *= c[i];
			}
	}
	
	private static int getTagIndex(MaxentTagger t, String s) {
		int i = t.getTagIndex(s);
		if (i < 0)
			return t.numTags();
		return i;
	}
	
	public double[] getInitialDistribution() {
		return initialSigns;
	}
	
	public double[][] getMatrix() {
		return transitionMatrix;
	}
	
	public void printTM() {
		System.out.print("\t");
		System.out.println();
		for (int i = 0; i < transitionMatrix.length; i++) {
			for (int j = 0; j < transitionMatrix[i].length; j++)
				System.out.print(transitionMatrix[i][j] + "\t");
			System.out.println();
		}
	}
}