 public class Solution {

	public static double[][] invert(double [][] matrix) {
		RealMatrix v1 = MatrixUtils.createRealMatrix(matrix);
		QRDecomposition v2 = new QRDecomposition(v1);
		DecompositionSolver v3 = v2.getSolver();
		RealMatrix v4 = v3.getInverse();
		double[][] v5 = v4.getData();
		return v5;
	}
}