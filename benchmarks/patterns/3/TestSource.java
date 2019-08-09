public static boolean test() throws Throwable {
    double [][] mat1 = new double[][]{{1,2,3},{4,5,6}};
    double [][] mat2 = new double[][]{{-0.944444,0.444444},{-0.111111,0.111111},{0.722222,-0.222222}};
    double [][] result = invert(mat1);
    for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 2; ++j) {
            if (Math.abs(mat2[i][j] - result[i][j]) > 1e-6)
                return false;
        }
    }
    return true;
}
