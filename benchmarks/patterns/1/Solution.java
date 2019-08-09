import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Solution {

    public static double[] solveLinear(double[][] arg0, double[] arg1) {
		RealVector sypet_var2538 = MatrixUtils.createRealVector(sypet_arg1);
		RealMatrix sypet_var2539 = MatrixUtils.createRealMatrix(sypet_arg0);
		LUDecomposition sypet_var2540 = new LUDecomposition(sypet_var2539);
		DecompositionSolver sypet_var2541 = sypet_var2540.getSolver();
		RealVector sypet_var2542 = sypet_var2541.solve(sypet_var2538);
		double[] sypet_var2543 = sypet_var2542.toArray();
		return sypet_var2543;
    }

}