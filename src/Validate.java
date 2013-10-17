
public class Validate {
	public static boolean IsValidIP(String ip) {
		String word = "";
		for(int i = 0; i < ip.length(); i++) {
			// grab current char
			char current_char = ip.charAt(i);
			// if it isnt a number then crash out
			if((current_char-((int)'0') > 9 || current_char-((int)'0') < 0) && current_char != '.') {
				return false;
			}
			// if it is a period, then analyse the word
			if(current_char == '.') {
				// make it an integer
				int add = Integer.parseInt(word);
				// check the bounds
				if((add < 0 || add > 255)) {
					return false;
				}
				// clear the words
				word="";
			} else {
				// add the char to the word
				word+=current_char;
			}
		}
		// process the last word
		int add = Integer.parseInt(word);
		// check the bounds
		if((add < 0 || add > 255)) {
			return false;
		}
		// all is good
		return true;
	}
	public static boolean IsValidPort(String port) {
		// make sure everything is a number
		for(int i = 0; i < port.length(); i++) {
			// grab current char
			char current_char = port.charAt(i);
			// if it isnt a number then crash out
			if(current_char-((int)'0') > 9 || current_char-((int)'0') < 0) {
				return false;
			}
		}
		int p = Integer.parseInt(port);
		if(p < 0 || p > 65535)
			return false;
		return true;
	}
}
