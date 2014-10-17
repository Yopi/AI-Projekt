import java.util.ArrayList;

// This class takes a transition matrix and applies constraints on it,
// it returns a transition matrix that is more constrained

public class MatrixConstraints {
	double[][] newMatrix;
	private final int SLUTSYMBOL = 0;
	private final int ALLA_SYMBOLER = 47;
	
	public MatrixConstraints(double[][] matrix) {
		int maxDepth = 10; // Godtyckligt
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
		this.curry = probability * parent.getCurry();
		
		children = new ArrayList<TreeNode>();
	}

	public TreeNode getParent() { return parent; }
	public int getWordType() { return wordType;	}
	public double getCurry() { return curry; }
	
	public void newChild(TreeNode child) {
		children.add(child);
	}
}
