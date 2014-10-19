import java.util.ArrayList;
import java.util.Arrays;

// This class takes a transition matrix and applies constraints on it,
// it returns a transition matrix that is more constrained

public class MatrixConstraints {
	double[][] newMatrix;
	private final int SLUTSYMBOL = 4;
	private final int ALLA_SYMBOLER = 5;
	
	public MatrixConstraints(double[][] matrix, int maxDepth) {
		newMatrix = constrainMatrix(0, matrix, maxDepth);
	}
	
	public double[][] getConstrainedMatrix() {
		return newMatrix;
	}
	
	public double[][] constrainMatrix(int startIndex, double[][] matrix, int maxDepth) {
		TreeNode root = new TreeNode(null, startIndex, 1);
		double prob = fillTree(root, startIndex, matrix, maxDepth-1);
		prob = 1/prob;
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = matrix[i][j] * prob;
			}
		}
		
		return matrix;
	}
	
	public double fillTree(TreeNode parent, int index, double[][] matrix, int maxDepth) {		
		if(maxDepth <= 0) {
			if(index == SLUTSYMBOL) {
				return parent.getCurry();
			}
			return 0;
		}
		double sum = 0d;
		for(int i = 0; i < ALLA_SYMBOLER; i++) {
			if(matrix[index][i] > 0) {
				TreeNode node = new TreeNode(parent, i, matrix[index][i]);
				sum += fillTree(node, i, matrix, maxDepth-1);
			}
		}

		return sum;
	}
	
	public static void main(String[] args) {
		/*
		 * Clay loves Mary Paul Today
		 */
		double[][] ts = new double[][]{
				{0, 1, 0, 0, 0},
				{0.25, 0, 0.5, 0.25, 0},
				{0, 0.67, 0, 0, 0.33},
				{0, 0, 0, 0, 1}
		};
		MatrixConstraints mc = new MatrixConstraints(ts, 4);
		for(double[] d : mc.getConstrainedMatrix()) {
			System.out.println(Arrays.toString(d));
		}
	}
}


class TreeNode {
	ArrayList<TreeNode> children;
	TreeNode parent;
	double probability;
	double curry; // Current probability (Product of probabilities until this part in tree)
	int wordType;
	public TreeNode(TreeNode parent, int wT, double probability) {
		this.parent = parent;
		this.wordType = wT;
		this.probability = probability;
		if(parent != null) {
			this.curry = probability * parent.getCurry();
		} else {
			this.curry = probability * 1;
		}
		
		children = new ArrayList<TreeNode>();
	}

	public TreeNode getParent() { return parent; }
	public int getWordType() { return wordType;	}
	public double getCurry() { return curry; }
	
	public void newChild(TreeNode child) {
		children.add(child);
	}
}
