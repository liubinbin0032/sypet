import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public static Time string2Time(String arg0, String arg1) throws ParseException {
	SimpleDateFormat sdf = new SimpleDateFormat(arg0);
	Date date = sdf.parse(arg1);
	long ms = date.getTime();
	Time t = new Time(ms);
	return t;
}