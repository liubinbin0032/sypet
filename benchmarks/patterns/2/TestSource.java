public static boolean test() throws Throwable {
    double [][] mat1 = new double[][]{{1,2,3},{4,5,6}};
    double [][] mat2 = new double[][]{{-1.6666666666666665,0.6666666666666666},{1.333333333333333,-0.33333333333333326},{0.0,0.0}};
    
    double[][] result = invert(mat1);
    for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 2; ++j) {
            if (Math.abs(mat2[i][j] - result[i][j]) > 1e-6) 
                return false;
        }
    }
    return true;
}
