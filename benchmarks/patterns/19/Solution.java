
public class Solution {

    public static String readFile(String fileName) throws Throwable {
		File file = new File(fileName);
		FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        String tempString = reader.readLine();
        return tempString;
	}
}