public static boolean test1() throws Throwable {
	java.lang.String REGEX = "dog";
    java.lang.String INPUT = "The dog says meow.";
    java.lang.String REPLACE = "cat";
    java.lang.String OUTPUT = repalceWithRegex(INPUT, REGEX, REPLACE);
    if (OUTPUT.equals("The cat says meow."))
        return true;
    else
        return false;
}

public static boolean test2() throws Throwable {
	java.lang.String REGEX = "meow";
    java.lang.String INPUT = "The dog says meow meow.";
    java.lang.String REPLACE = "bark";
    java.lang.String OUTPUT = repalceWithRegex(INPUT, REGEX, REPLACE);
    if (OUTPUT.equals("The dog says bark bark."))
        return true;
    else
        return false;
}

public static boolean test() throws Throwable {
	
    return test1() && test2();
}