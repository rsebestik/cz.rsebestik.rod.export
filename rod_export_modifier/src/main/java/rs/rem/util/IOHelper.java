package rs.rem.util;

import static org.apache.commons.io.IOCase.INSENSITIVE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import rs.rem.Modifier;

/**
 * IO Helper
 * 
 * @author V3400877
 *
 */
public class IOHelper {
	
	private static final Logger log = LogManager.getLogger(IOHelper.class);
	
	private IOHelper(){}
	
	public static void copyFiles(File resultDir, List<File> files) throws IOException{
		for (File jqueryFile : files) {
			log.info(String.format("Copying %s to %s", jqueryFile.getName(), resultDir));
			FileUtils.copyFileToDirectory(jqueryFile, resultDir);
		}
	}
	
	public static File getFileInDir(File dir, File file) {
		String resultFilePath = dir.getAbsolutePath() + "/" + file.getName();
		return new File(resultFilePath);
	}
	
	public static File createResultDir(File sourceDir) throws IOException {
		File resultDir = new File(sourceDir.getAbsolutePath()+"_RESULT");
		if (resultDir.exists())
			FileUtils.deleteDirectory(resultDir);
		resultDir.mkdir();
		return resultDir;
	}
	
	@SuppressWarnings("serial")
	public static IOFileFilter getMaskFilter(final String mask) {
		return new FileFileFilter() {
			@Override public boolean accept(File dir, String name) {
				return accept(new File(dir, name));
			}
			
			@Override public boolean accept(File file) {
				return super.accept(file) && FilenameUtils.wildcardMatch(file.getName(), mask, INSENSITIVE);
			}
		};
	}
	
	public static String getFilenamesCSV(List<File> jqueryFiles) {
		Collection<String> jqueryFileNames = new ArrayList<String>();
		for (File f : jqueryFiles) {
			jqueryFileNames.add(FilenameUtils.getName(f.getName()));
		}
		return StringUtils.join(jqueryFileNames, ",");
	}
	
	public static Properties getProperties(String fileName) {
		Properties p = new Properties();
		try {
			p.load(Modifier.class.getResourceAsStream("/"+fileName));
		} catch (IOException e) {
			log.error(fileName + " properties wasnt loaded", e);
		}
		return p;
	}
}
