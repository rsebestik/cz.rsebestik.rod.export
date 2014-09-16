package rs.rem.util;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import static rs.rem.util.IOHelper.getFileInDir;
import static rs.rem.util.IOHelper.getMaskFilter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import rs.rem.Modifier;

/**
 * Copy Helper
 * 
 * @author V3400877
 *
 */
public class CopyHelper {
	
	private static final Logger log = LogManager.getLogger(CopyHelper.class);
	
	private CopyHelper(){}
	
	public static void copyResources(File sourceSubdir, File resultDir, File resultSubdir, File resultJQuerySubdir) throws IOException, ArchiveException {
		log.info("Copying external subdir with images ...");
		FileUtils.copyDirectory(sourceSubdir, resultSubdir, notFileFilter(getMaskFilter("*.html")));
		log.info("Copying external subdir with images DONE result sub-dir: "+resultSubdir);

		log.info("Copying internal app resources...");
		copyFiles(resultSubdir, 
				"img/grab.gif",
				"img/external-link.png",
				"img/narozeni.png",
				"img/narozeni-w.png",
				"img/snatky.png",
				"img/snatky-w.png",
				"img/umrti.png",
				"img/umrti-w.png"
		);
		copyFiles(resultJQuerySubdir, 
				"jquery/jquery-ui-1.10.4.custom.css",
				"jquery/jquery.fancybox.css"
		);
		copyFiles(new File(resultJQuerySubdir.getAbsolutePath()+"/img"), 
				"jquery/img/blank.gif",
				"jquery/img/fancybox_loading.gif",
				"jquery/img/fancybox_loading@2x.gif",
				"jquery/img/fancybox_overlay.png",
				"jquery/img/fancybox_sprite.png",
				"jquery/img/fancybox_sprite@2x.png",
				"jquery/img/ui-bg_flat_0_aaaaaa_40x100.png",
				"jquery/img/ui-bg_flat_75_ffffff_40x100.png",
				"jquery/img/ui-bg_glass_55_fbf9ee_1x400.png",
				"jquery/img/ui-bg_glass_65_ffffff_1x400.png",
				"jquery/img/ui-bg_glass_75_f6f5f5_1x400.png",
				"jquery/img/ui-bg_glass_75_f9f8f8_1x400.png",
				"jquery/img/ui-bg_glass_95_fef1ec_1x400.png",
				"jquery/img/ui-bg_highlight-soft_75_cfb_1x100.png",
				"jquery/img/ui-icons_090808_256x240.png",
				"jquery/img/ui-icons_2e83ff_256x240.png",
				"jquery/img/ui-icons_454545_256x240.png",
				"jquery/img/ui-icons_4d4d4d_256x240.png",
				"jquery/img/ui-icons_888888_256x240.png",
				"jquery/img/ui-icons_cd0a0a_256x240.png"
		);
		log.info("Copying internal app resources DONE");
	}
	
	public static void copyFiles(File resultDir, String... fileNames) throws IOException {
		for (String fileName : fileNames) {
			writeByteArrayToFile(getFileInDir(resultDir, new File(fileName)), toByteArray(Modifier.class.getResourceAsStream("/"+fileName)));
		}
	}
}