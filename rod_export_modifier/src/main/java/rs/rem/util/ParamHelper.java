package rs.rem.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Utils for working with input params
 * 
 * @author V3400877
 *
 */
public class ParamHelper {
	
	private ParamHelper(){}
	
	public static String getParameterValue(String[] args, String name) {
		List<String> values = getParameterValues(args, name);
		return values.isEmpty() ? null : values.get(0);
	}
	
	public static List<String> getParameterValues(String[] args, String name) {
		Iterator<String> it = Arrays.asList(args).iterator();
		List<String> result = new ArrayList<String>();
		outer:
		while (it.hasNext()) {
			String arg = it.next();
			if (("-"+name).equalsIgnoreCase(arg)) {
				while (it.hasNext()) {
					String value = it.next();
					if (!value.startsWith("-")) {
						result.add(StringUtils.strip(value, "'"));
					} else {
						break outer;
					}
				}
			}
		}
		return result;
	}
	
	public static boolean isParameterSet(String[] args, String name) {
		for (String arg : args) {
			if (("-"+name).equalsIgnoreCase(arg)) {
				return true;
			}
		}
		return false;
	}
}
