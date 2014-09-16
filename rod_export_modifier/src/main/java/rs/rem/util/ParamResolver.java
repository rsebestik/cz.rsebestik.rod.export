package rs.rem.util;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Helper for resolving and verifying input params
 * 
 * @author V3400877
 *
 */
public class ParamResolver {
	
	private static final Logger log = LogManager.getLogger(ParamResolver.class);
	
	private ParamResolver(){}
	
	public static File resolveAndVerifySourceDir(String[] args) {
		String dirName = ParamHelper.getParameterValue(args, "dir");
		
		if (isEmpty(dirName)) {
			log.error("No '-dir <directory>' parameter specified");
			return null;
		}
		
		File sourceDir = new File(dirName);
		if (!sourceDir.exists()) {
			log.error(String.format("Directory '%s' doesnt exist.", sourceDir.getAbsolutePath()));
			return null;
		}
		if (!sourceDir.isDirectory()) {
			log.error(String.format("'%s' is not a directory.", sourceDir.getAbsolutePath()));
			return null;
		}
		
		return sourceDir;
	}
	
	public static File resolveAndVerifyMainFile(File sourceDir, String fileName) {
		File indexFile = new File(sourceDir, fileName);
		if (!indexFile.exists()) {
			log.error(String.format("Index file '%s' doesnt exist.", indexFile.getAbsolutePath()));
			return null;
		}
		if (!indexFile.isFile()) {
			log.error(String.format("'%s' is not a file.", indexFile.getAbsolutePath()));
			return null;
		}
		
		return indexFile;
	}
	
	public static File resolveAndVerifyFileFromParam(String[] args, String paramName, String defaultFileName) {
		String fileName = ParamHelper.getParameterValue(args, paramName);
		
		if (isEmpty(fileName)) {
			log.info(String.format("No '-%1$s <%1$sFile>' parameter specified. '"+defaultFileName+"' will be searched.", paramName));
			fileName = defaultFileName;
		}
		
		File file = new File(fileName);
		if (!file.exists()) {
			log.error(String.format("File '%s' doesnt exist.", file.getAbsolutePath()));
			return null;
		}
		if (!file.isFile()) {
			log.error(String.format("'%s' is not a file.", file.getAbsolutePath()));
			return null;
		}
		
		return file;
	}
	
	public static boolean isXhtmlRequested(String[] args) {
		boolean xhtml = ParamHelper.isParameterSet(args, "xhtml");
		
		log.info("Html converted to xhtml will be written: "+xhtml);
		
		return xhtml;
	}
	
	public static boolean isCompressSuppressed(String[] args) {
		boolean notCompress = ParamHelper.isParameterSet(args, "nc");
		
		log.info("Compression of result dir is suppressed: "+notCompress);
		
		return notCompress;
	}
}
