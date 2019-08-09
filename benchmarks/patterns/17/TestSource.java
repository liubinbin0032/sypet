public static boolean test0() throws Throwable{
	if(string2Time("HH:mm:ss", "09:33:00").toString().equals("09:33:00"))
		return true;
	else 
		return false;
}
public static boolean test1() throws Throwable{
	if(string2Time("yyyy-MM-dd", "2018-08-16").toString().equals("00:00:00"))
		return true;
	else 
		return false;
}
public static boolean test() throws Throwable{
    return test0() && test1();
}
