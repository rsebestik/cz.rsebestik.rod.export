package rs.rem.util;

import static org.apache.commons.compress.archivers.ArchiveStreamFactory.TAR;
import static org.apache.commons.io.filefilter.FileFilterUtils.nameFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Compress Helper
 * 
 * @author V3400877
 *
 */
public class CompressHelper {
	
	private static final Logger log = LogManager.getLogger(CompressHelper.class);
	
	private CompressHelper(){}
	
	public static void compress(File inputDir) throws IOException, ArchiveException {
		File tarFile = IOHelper.getFileInDir(inputDir, new File("_RESULT.tar"));
		File gzFile = new File(tarFile.getAbsolutePath()+".gz");
		log.info("Compressing result TARing... "+tarFile);
		CompressHelper.tarDir(inputDir, tarFile);
		log.info("Compressing result GZIPing... "+gzFile);
		CompressHelper.gzipFile(tarFile, gzFile);
		log.info("Compressing DONE");
	}
	
	public static void tarDir(File inputDir, File resultFile) throws IOException, ArchiveException {
		OutputStream out = new FileOutputStream(resultFile);
		TarArchiveOutputStream taos = (TarArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream(TAR, out);

		for(File file : FileUtils.listFiles(inputDir, notFileFilter(nameFileFilter(resultFile.getName())), TrueFileFilter.INSTANCE)){
//			System.out.println(file);
//			System.out.println(inputDir);
//			System.out.println(inputDir.toURI().relativize(file.toURI()).getPath());

			TarArchiveEntry entry = new TarArchiveEntry(file, inputDir.toURI().relativize(file.toURI()).getPath());
			entry.setSize(file.length());
			taos.putArchiveEntry(entry);
			FileInputStream fis = new FileInputStream(file);
			IOUtils.copy(fis, taos);
			fis.close();
			taos.closeArchiveEntry();
		}
		taos.finish();
		taos.close();
		out.close();
	}
	
	public static void gzipFile(File inputFile, File resultFile) throws IOException {
		FileInputStream fis = new FileInputStream(inputFile);
		FileOutputStream fos = new FileOutputStream(resultFile);
		GZIPOutputStream gos = new GZIPOutputStream(fos);

		// copy and compress
		IOUtils.copy(fis, gos);
		
		gos.close();
		fis.close();
	}
}