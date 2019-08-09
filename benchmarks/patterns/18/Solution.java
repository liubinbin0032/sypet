import java.text.SimpleDateFormat; 
import java.text.DateFormat;

public static String changeDateFormat(String date, String oldFormat, String newFormat) throws Throwable{     
	DateFormat input = new SimpleDateFormat(oldFormat); 
	DateFormat output = new SimpleDateFormat(newFormat);
	java.util.Date date2 = input.parse(date);
	String result = output.format(date2);
	return result;
}