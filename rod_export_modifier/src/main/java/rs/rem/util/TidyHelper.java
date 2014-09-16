package rs.rem.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.tidy.Tidy;

/**
 * Utils for working with Tidy
 * 
 * @author V3400877
 *
 */
public class TidyHelper {
	
	private TidyHelper(){}
	
	public static String convertToXhtml(File fileInput, String encoding) throws IOException {
		List<String> inputLines = FileUtils.readLines(fileInput, encoding);
		//je treba manualne smazat prvni radek s DOCTYPE - je tam uveden podivne a rozbiji to pak tidy
		if (inputLines.get(0).contains("DOCTYPE"))
			inputLines.remove(0);
		String input = StringUtils.join(inputLines, "");
		
		//moznost 'polygon' v shape neexistuje -> spravne je 'poly'
		input = input.replace("<area shape=\"polygon\"", "<area shape=\"poly\"");
		
		//zahozeni komentare v JS protoze ten se smrskne na jeden radek a kod za komentarem by byl v komentari
		input = input.replace("//aby se správně nastavila velikost iframe", "");
		
		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		tidy.setDocType("omit");
		tidy.setTidyMark(false);
		tidy.setXmlTags(false);
		tidy.setInputEncoding("UTF-8");
		tidy.setOutputEncoding("UTF-8");
		tidy.setXHTML(true);
		tidy.setJoinClasses(true);
		tidy.setJoinStyles(false);
		tidy.setIndentCdata(true);
		
		StringWriter output = new StringWriter();
		tidy.parse(new StringReader(input), output);

		return output.getBuffer().toString();
	}
}
