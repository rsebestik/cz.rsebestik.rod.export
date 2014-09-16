package rs.rem.util;

import static org.apache.commons.lang.StringUtils.defaultString;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XmlHelper {
	
	private XmlHelper(){}
	
	public static boolean isSpecialVztah(Node node) {
		return nodeStartsWith(node, "%S%");
	}
	
	public static boolean isDatumVzniku(Node node) {
		return nodeStartsWith(node, "%D%");
	}
	
	public static boolean nodeStartsWith(Node node, String startString) {
		return defaultString(node.getTextContent()).startsWith(startString);
	}
	
	public static boolean isImageRow(Node node) {
		return (node instanceof Element) ? ((Element) node).getElementsByTagName("a").getLength()>0 : false;
	}
	
//	public static String getJmeno(XPath xPath, Document vazbaDetail, String shortCut) throws XPathExpressionException {
//		String jmeno = StringUtils.substring(xPath.compile("//tr[td[contains(.,'"+shortCut+"')]]").evaluate(vazbaDetail), 2);
//		return jmeno;
//	}
	
	public static Document loadXml(String xml) throws RuntimeException {
		try {
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return dBuilder.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			throw new RuntimeException("Unable to load configuration file", e);
		}
	}
}
