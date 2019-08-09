
public class Solution {

    public static RealMatrix invert(RealMatrix arg0) {
    	RealMatrix v0 = MatrixUtils.createRealMatrix(arg0);
        SingularValueDecomposition v1 = new SingularValueDecomposition(v0);
        DecompositionSolver v2 = v1.getSolver();
        RealMatrix v3 = v2.getInverse();
         double[][] v4 = v3.getData();
        return v4;
    }
}