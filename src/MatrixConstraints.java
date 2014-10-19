import java.util.ArrayList;
import java.util.Arrays;

// This class takes a transition matrix and applies constraints on it,
// it returns a transition matrix that is more constrained

public class MatrixConstraints {
	double[][] newMatrix;
	int allWordClasses;
	int endWordClass;
	
	public MatrixConstraints(double[][] matrix, int maxDepth) {
		allWordClasses = matrix.length;
		endWordClass = 11;
		newMatrix = new double[matrix.length][matrix[0].length];
		constrainMatrix(0, matrix, maxDepth);
	}
	
	public double[][] getConstrainedMatrix() {
		return newMatrix;
	}
	
	public void constrainMatrix(int startIndex, double[][] matrix, int maxDepth) {
		TreeNode root = new TreeNode(null, startIndex, 1);
		fillTree(root, startIndex, matrix, maxDepth-1);
		System.out.println("Tree filled");
		
		// Calculate new probabilities
		reestimateMatrix(root);
		System.out.println("Matrix reestimated");
	}
	
	public void reestimateMatrix(TreeNode current) {
		double normalizer = 1/current.sumChildren();
		for(TreeNode n : current.getChildren()) {
			double n_newProb = (n.getProbability() + n.sumChildren()) * normalizer;
			n.setProbability(n_newProb);
			newMatrix[current.getWordType()][n.getWordType()] = n_newProb;
		}
		
		for(TreeNode c : current.getChildren()) {
			reestimateMatrix(c);
		}
	}
	
	public int fillTree(TreeNode parent, int index, double[][] matrix, int maxDepth) {		
		if(maxDepth <= 0 && index != endWordClass) {
			return 0;
		} else if(maxDepth <= 0) {
			return 1;
		}
		
		int sum = 0;
		int value = 0;
		for(int i = 0; i < allWordClasses; i++) {
			if(matrix[index][i] > 0) {
				TreeNode node = new TreeNode(parent, i, matrix[index][i]);
				value = fillTree(node, i, matrix, maxDepth-1);
				if(value > 0) parent.addChild(node);
				sum += value;
			}
		}
		
		return (sum > 0) ? 1 : 0;
	}
	
	public static void main(String[] args) {
		/*
		 * Clay loves Mary Paul Today
		 */
		double[][] ts = new double[][]{
				{0, 1, 0, 0, 0},
				{0.25, 0, 0.5, 0.25, 0},
				{0, 0.67, 0, 0, 0.33},
				{0, 0, 0, 0, 1},
				{0, 0, 0, 0, 1}
		};
		
		
		ts = new double[][] {
			//	 0		1		2		3		4		5		6		7		8		9		10
				{0, 	0.25,	0.75,	0,		0,		0,		0,		0,		0,		0,		0}, // 0
				{0,		0,		0,		0.1,	0.9,	0,		0, 		0, 		0,		0,		0}, // 1
				{0.2,	0,		0,		0,		0,		0.1,	0.5, 	0.2,	0,		0,		0}, // 2
				{0,		0,		0,		0,		0.2,	0,		0,		0,		0.8,	0,		0}, // 3 
				{0,		0,		0,		0,		0,		0,		0,		0,		1,		0,		0}, // 4 
				{0,		0.5,	0,		0,		0,		0,		0,		0,		0.5,	0,		0}, // 5 
				{0,		0,		0,		0,		0,		0,		0,		0,		0,		1,		0}, // 6 
				{0,		0,		0,		0,		0,		0,		0,		0,		0,		1,		0}, // 7 
				{0,		0,		0,		0,		0,		0,		0,		0,		0,		0,		1}, // 8 
				{0,		0,		0,		0,		0,		0,		0,		0,		0,		0,		1}, // 9 
				{0,		0,		0,		0,		0,		0,		0,		0,		0,		0,		1} // 10
		};
				
		MatrixConstraints mc = new MatrixConstraints(ts, 5);
		for(double[] d : mc.getConstrainedMatrix()) {
			System.out.println(Arrays.toString(d));
		}
	}
}


class TreeNode {
	ArrayList<TreeNode> children;
	TreeNode parent;
	double probability;
	int wordType;
	public TreeNode(TreeNode parent, int wT, double probability) {
		this.parent = parent;
		this.wordType = wT;
		this.probability = probability;
		
		children = new ArrayList<TreeNode>();
	}

	public TreeNode getParent() { return parent; }
	public int getWordType() { return wordType;	}
	public double getProbability() { return probability; }
	public void setProbability(double newProb) { probability = newProb; } 
	
	public void addChild(TreeNode child) {
		children.add(child);
	}
	
	public ArrayList<TreeNode> getChildren() {
		return children;
	}
	
	public double sumChildren() {
		return sum() - this.probability;
	}
	public double sum() {
		double sum = this.probability;
		for(TreeNode n : children) {
			sum += n.sum();
		}
		
		return sum;
	}
	
	public void print(String extra) {
		System.out.println(extra + this.wordType + "(" + this.probability + ")");
		
		extra = extra + " > ";
		for(TreeNode n : children) {
			n.print(extra);
		}
	
	}
}
