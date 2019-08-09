public class Solution {

    public static int readFile(String fileName) throws Throwable {
		InputStream in = new FileInputStream(fileName);
        int tempbyte = in.read();
        return tempbyte;
	}
}