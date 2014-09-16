package rs.rem.util;

import static java.lang.String.format;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class StringHelper {

	private StringHelper(){}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static String convertMapToJsVar(String jsVarName, Map map) {
		StringBuilder jsVar = new StringBuilder("var ").append(jsVarName).append("={");
		for (Iterator<Entry> iter = map.entrySet().iterator();iter.hasNext();) {
			Entry e = iter.next();
			jsVar.append(format("'%s':'%s'", e.getKey(), e.getValue()));
			if (iter.hasNext())
				jsVar.append(',');
		}
		jsVar.append("};");
		return jsVar.toString();
	}
}
