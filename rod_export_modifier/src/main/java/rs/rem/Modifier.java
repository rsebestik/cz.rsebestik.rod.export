package rs.rem;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.io.filefilter.FileFilterUtils.and;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import static rs.rem.util.IOHelper.getMaskFilter;
import static rs.rem.util.IOHelper.getProperties;
import static rs.rem.util.XmlHelper.isDatumVzniku;
import static rs.rem.util.XmlHelper.isImageRow;
import static rs.rem.util.XmlHelper.isSpecialVztah;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import rs.rem.util.CompressHelper;
import rs.rem.util.CopyHelper;
import rs.rem.util.IOHelper;
import rs.rem.util.ParamResolver;
import rs.rem.util.StringHelper;
import rs.rem.util.TidyHelper;
import rs.rem.util.XmlHelper;

/**
 * 
 * @author rsebestik
 *
 */
public class Modifier {

	private static final Logger log = LogManager.getLogger(Modifier.class);

	public static final String CHAR_SET = "UTF-8";

	private static final String SUBDIR_NAME_SOURCE = "index_soubory";
	private static final String SUBDIR_NAME_RESULT = "img";
	private static final String MAIN_HTML_FILE = "index.html";
	private static final String DEFAULT_CSS_FILE = "style.css";
	private static final String DEFAULT_JS_FILE = "script.js";
	private static final String DEFAULT_CHANGES_FILE = "changes.txt";
	private static final String RESULT_JQUERY_FILE = "jquery-all.js";

	private static final Map<Object, Object> LABELS_PROPERTIES;
	private static final String LABELS_MAP_JS_VARIABLE;
	
	private static final Map<Object, Object> TITLES_PROPERTIES;
	private static final String TITLES_MAP_JS_VARIABLE;
	
	static {
		LABELS_PROPERTIES = unmodifiableMap(new LinkedHashMap<Object, Object>(getProperties("labelShortcuts.properties")));
		LABELS_MAP_JS_VARIABLE = StringHelper.convertMapToJsVar("labelsMap", LABELS_PROPERTIES);
		
		TITLES_PROPERTIES = unmodifiableMap(new LinkedHashMap<Object, Object>(getProperties("titleShortcuts.properties")));
		TITLES_MAP_JS_VARIABLE = StringHelper.convertMapToJsVar("titlesMap", TITLES_PROPERTIES);
	}

	public static void main(String[] args) throws Exception {
		log.info("--------START--------");

		//process input params
		File sourceDir = ParamResolver.resolveAndVerifySourceDir(args);
		if (sourceDir == null)
			return;

		log.info("source dir: "+sourceDir);

		File indexFile = ParamResolver.resolveAndVerifyMainFile(sourceDir, MAIN_HTML_FILE);
		File cssFile = ParamResolver.resolveAndVerifyFileFromParam(args, "css", DEFAULT_CSS_FILE);
		File jsFile = ParamResolver.resolveAndVerifyFileFromParam(args, "js", DEFAULT_JS_FILE);
		File changesFile = ParamResolver.resolveAndVerifyFileFromParam(args, "changes", DEFAULT_CHANGES_FILE);

		if (indexFile == null || cssFile == null || jsFile == null || changesFile == null)
			return;

		transform(args, sourceDir, indexFile, cssFile, jsFile, changesFile);
		log.info("--------END--------");
	}
	
	private static void transform(String[] args, File sourceDir, File indexFile, File cssFile, File jsFile, File changesFile) 
			throws TransformerException, Exception {
		File resultDir = IOHelper.createResultDir(sourceDir);
		log.info("result dir created: "+resultDir);

		//find jquery files and copy them to RESULT dir
		File resultJQuerySubdir = new File(resultDir.getAbsolutePath()+"/jquery");
		File imgSubDir = new File(resultDir.getAbsolutePath()+"/"+SUBDIR_NAME_RESULT);
		
		mergeJqueryFilesToResultDir(resultJQuerySubdir, RESULT_JQUERY_FILE);

		//convert html to xhtml
		String xhtmlInput = TidyHelper.convertToXhtml(indexFile, CHAR_SET);
		log.info("html converted to proper xhtml");
		if (ParamResolver.isXhtmlRequested(args)) {
			File xhtmlFile = new File(resultDir.getAbsolutePath() + "/" + indexFile.getName()+"-xhtml.html");
			FileUtils.write(xhtmlFile, xhtmlInput, CHAR_SET);
			log.info("XHTML written: "+xhtmlFile);
		}

		File sourceSubdir = new File(sourceDir.getAbsolutePath()+"/"+SUBDIR_NAME_SOURCE);

		log.info("Grabbing html files with person details from subdir...");
		Collection<File> personDetails = FileUtils.listFiles(sourceSubdir, and(getMaskFilter("*.html"), notFileFilter(getMaskFilter("*max*"))), null);
		log.info("Grabbing html files with person details from subdir DONE count: "+personDetails.size());

		//make XSLT transformation and write result to new file
		String result = transformIndex(xhtmlInput, cssFile, jsFile, changesFile, RESULT_JQUERY_FILE, personDetails);
		File resultFile = IOHelper.getFileInDir(resultDir, indexFile);
		FileUtils.write(resultFile, result, CHAR_SET);
		log.info("result file: "+resultFile);

		//copy resources to result
		CopyHelper.copyResources(sourceSubdir, resultDir, imgSubDir, resultJQuerySubdir);

		if (!ParamResolver.isCompressSuppressed(args)) {
			CompressHelper.compress(resultDir);
		}
	}
	
	private static String transformIndex(String xhtmlInput, File cssFile, File jsFile, File changesFile, String jqueryFilesName, Collection<File> personDetails) 
			throws TransformerException, IOException, XPathExpressionException {
		log.info("INDEX loading XSLT");
		InputStream indexXslt = Modifier.class.getResourceAsStream("/index.xslt");

		StringWriter resultWriter = new StringWriter();
		Transformer t = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null).newTransformer(new StreamSource(indexXslt));
		t.setParameter("cssContent", FileUtils.readFileToString(cssFile, CHAR_SET));
		t.setParameter("jsContent", FileUtils.readFileToString(jsFile, CHAR_SET).replaceAll("[\r\n]\\s+", ""));
		t.setParameter("jsLabelsVar", LABELS_MAP_JS_VARIABLE);
		t.setParameter("jsTitlesVar", TITLES_MAP_JS_VARIABLE);
		List<String> changesLines = FileUtils.readLines(changesFile, CHAR_SET);
		String lastChangeDate = changesLines.get(0);
		t.setParameter("changesContent", getChangesContent(changesLines));
		t.setParameter("lastChangeDate", lastChangeDate);
		t.setParameter("jqueryFileName", "jquery/"+jqueryFilesName);
		Map<String, String> details = getDetails(personDetails, lastChangeDate);
		Map<String, String> mapaVztahu = getMapaVztahu(details);
		t.setParameter("jsVztahyVar", StringHelper.convertMapToJsVar("vztahyMap", mapaVztahu));
		t.setParameter("personDetails", details.values());
		t.setParameter("subdirSource", SUBDIR_NAME_SOURCE);
		t.setParameter("subdirResult", SUBDIR_NAME_RESULT);
		t.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "{http://www.w3.org/1999/xhtml}script");
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.setOutputProperty(OutputKeys.INDENT, "no");

		log.info("INDEX transforming...");
		t.transform(new StreamSource(new StringReader(xhtmlInput)), new StreamResult(resultWriter));
		log.info("INDEX transforming DONE");
		
		String result = resultWriter.getBuffer().toString();
		
		//additional output enhancements
		result = result.replace("\" />", "\"/>");

		return result;
	}
	
	private static String getChangesContent(List<String> changesLines) {
		final String dateRegexp = "((0?[1-9]|[12][0-9]|3[01]).(0?[1-9]|1[012]).((19|20)\\d\\d))";
		
		//join lines with <br/>
		String result = StringUtils.join(changesLines, "<br/>\n");
		//simulate tabs
		result = result.replace("\t", "&#160;&#160;");
		//highlight dates
		result = result.replaceAll(dateRegexp, "<strong>$1</strong>");
		
		return result;
	}

	/**
	 * Nacte detaily stranek, vysledek je linked mapa, kde: key = id; value = xml
	 */
	private static Map<String, String> getDetails(Collection<File> personDetails, String lastChangeDate) throws IOException, TransformerException, XPathExpressionException {
		log.info("DETAILS loading XSLT");
		String detailXslt = IOUtils.toString(Modifier.class.getResourceAsStream("/detail.xslt"), CHAR_SET);
		log.info(String.format("DETAILS transforming %s detail pages...", personDetails.size()));

		Map<String, String> details = new LinkedHashMap<String, String>();
		for (File f : personDetails) {
//			if (getBaseName(f.getName()).equals("448") || getBaseName(f.getName()).equals("469")){
				String id = getBaseName(f.getName());
				details.put(id, transformDetail(TidyHelper.convertToXhtml(f, CHAR_SET), detailXslt, id, lastChangeDate));
//				System.out.println(details);
//			}
//			break;
		}
		log.info("DETAILS transforming DONE");

		return details;
	}
	
	/**
	 * zjistime k jakym osobam se vztahuje kazda osoba (vysledek je ... mapaVztahu František  Glozyga%D%22.11.1864=431, Rozálie Kučíková Dudíková%D%13.6.1848=193, ...} )
	 * v klici mapy je uvedeno jednak jmeno a pak taky za oddelovacem '%D%' take datum vzniku vztahu
	 * vychazi se z predpokladu, kdyz mam dokument svatby, tak vim datum vzniku
	 * mit v klici i tento datum je dulezite aby nedochazelo k mylnym kombinacim napr pri shode jmen 
	 */
	private static Map<String, String> getMapaVztahu(Map<String, String> details) throws IOException, TransformerException, XPathExpressionException {
		Map<String, String> mapaVztahu = new HashMap<String, String>();//key = jmeno vztahu+datumVzniku; value = id partnera

		XPath xPath =  XPathFactory.newInstance().newXPath();
		for (Entry<String, String> detail : details.entrySet()) {
			String xml = detail.getValue(); 
			if (xml.contains("¨S") && xml.contains("%W")) {
				Document docDetail = XmlHelper.loadXml(xml);
				
				//najdu TR element standardnich vztahu
				Element standVztahyTR = (Element) xPath.compile("//tr[td[contains(.,'%W')]]").evaluate(docDetail, XPathConstants.NODE);
				if (standVztahyTR != null) {
					//postupne se budu koukat na nasledujici TR vztahu (koncim bud na konci nebo kdyz se jedna o specialni vztah nebo obrazky)
					Node nextTR = standVztahyTR.getNextSibling();
					while (nextTR != null && !isSpecialVztah(nextTR) &&!isImageRow(nextTR)) {
						String content = nextTR.getTextContent();
						if (content.startsWith("%X")){
							//nasel jsem zacatek standardniho vztahu - TR s textem Partner:
							//vztah -> jmeno osoby, se kterou ma tato osoba vztah
							String vztah = content.substring(2).replace('\u00A0', ' ');
							//System.out.println(detail.getKey()+"-'"+vztah+"'");
							
							//v nasledujicich TR najdu datumVzniku a pridam hodnotu do mapyVztahy
							Node datumVzniku = nextTR.getNextSibling();
							hledaniDatumuVzniku:
							while (datumVzniku != null && !isSpecialVztah(datumVzniku) && !isImageRow(datumVzniku)) {
								if (isDatumVzniku(datumVzniku)) {
									mapaVztahu.put(vztah+datumVzniku.getTextContent(), detail.getKey());
									break hledaniDatumuVzniku;
								}
								datumVzniku = datumVzniku.getNextSibling();
							}
							
							//String jmeno = getMessageIfNotReturnKey(LABEL_SHORTCUTS, getJmeno(xPath, docDetail, "%J"));
							//String rodnePrij = getMessageIfNotReturnKey(LABEL_SHORTCUTS, getJmeno(xPath, docDetail, "%R"));
							//String prijmeni = getMessageIfNotReturnKey(LABEL_SHORTCUTS, getJmeno(xPath, docDetail, "%B"));
							//System.out.println("+-"+vztah+" === "+detail.getKey()+ " === "+jmeno+rodnePrij+prijmeni);
						}
						
						nextTR = nextTR.getNextSibling();
					}
				}
			}
		}

		log.info("DETAILS mapa vztahu DONE: "+mapaVztahu);
		return mapaVztahu;
	}
	
	private static String transformDetail(String xhtmlInput, String detailXslt, String id, String lastChangeDate) throws TransformerException, IOException {
		StringWriter resultWriter = new StringWriter();
		Transformer t = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null).newTransformer(new StreamSource(new StringReader(detailXslt)));
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.setOutputProperty(OutputKeys.INDENT, "no");
		t.setParameter("id", id);
		t.setParameter("lastChangeDate", lastChangeDate);
		t.setParameter("labelTexts", StringUtils.join(LABELS_PROPERTIES.values(), ";"));
		t.setParameter("labelShortcuts", StringUtils.join(LABELS_PROPERTIES.keySet(), ";"));
		t.setParameter("titleTexts", StringUtils.join(TITLES_PROPERTIES.values(), ";"));
		t.setParameter("titleShortcuts", StringUtils.join(TITLES_PROPERTIES.keySet(), ";"));

		t.transform(new StreamSource(new StringReader(xhtmlInput)), new StreamResult(resultWriter));
		String result = resultWriter.getBuffer().toString();
		
		//additional output enhancements
		result = result.replace(" xmlns=\"http://www.w3.org/1999/xhtml\"", "");
		result = result.replace(">\n", ">");
		result = result.replace("<tr><td colspan=\"2\"></td></tr>", "");
		//skryti **secret** informaci (non-greedy matching)
		result = result.replaceAll("\\*\\*.+?\\*\\*", "***");
		return result;
	}

	private static void mergeJqueryFilesToResultDir(File resultDir, String resultFileName) throws IOException {
		String[] jqueryFiles = {"1jquery-1.11.0.min.js", "2jquery.cookie.js", "3jQuery.splitter.js", 
				"4jquery-ui-1.10.4.custom.min.js", "5jquery.fancybox.pack.js", "6jquery.linkify-1.1.sh-min.js"};

		File resultJquery = new File(resultFileName);
		log.info(format("%s jquery files (%s) -> will be merged&copied to resultDir: %s", jqueryFiles.length, ArrayUtils.toString(jqueryFiles), resultJquery));

		for (String f : jqueryFiles) {
			FileUtils.writeByteArrayToFile(IOHelper.getFileInDir(resultDir, resultJquery), toByteArray(Modifier.class.getResourceAsStream("/jquery/"+f)), true);
		}
	}
	
}