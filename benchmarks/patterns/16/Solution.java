import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public static String repalceWithRegex(String input, String regex, String replace) {
    Pattern p = Pattern.compile(regex);
    // get a matcher object
    Matcher m = p.matcher(input); 
    input = m.replaceAll(replace);
    return input;
}