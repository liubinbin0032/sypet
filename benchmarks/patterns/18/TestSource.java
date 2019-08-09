public static boolean test() throws Throwable {
	java.lang.String date = "20181207";
    java.lang.String oldFormat = "yyyyMMdd";
    java.lang.String newFormat = "dd-MM-yyyy";
    java.lang.String res = changeDateFormat(date, oldFormat, newFormat);
    if(res.equals("07-12-2018"))
        return true;
    else
        return false;
}