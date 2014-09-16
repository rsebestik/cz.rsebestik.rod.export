package rs.rem.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class BundleHelper {
	
	private static final Locale LOCALE = new Locale("cs", "CZ");
	private static final Logger log = LogManager.getLogger(BundleHelper.class);
	
	private BundleHelper(){}

	public static String getMessageIfNotReturnKey(Bundle bundle, String key) {
		try {
			return getBundle(bundle).getString(key);
		} catch (MissingResourceException mre) {
			if (log.isTraceEnabled())
				log.trace("Key: '"+key+"' not found in bundle: '"+bundle.getName());
			return key;
		}
	}
	
	public static String getMessageIfNotReturnNull(Bundle bundle, String key) {
		try {
			return getBundle(bundle).getString(key);
		} catch (MissingResourceException mre) {
			if (log.isTraceEnabled())
				log.trace("Key: '"+key+"' not found in bundle: '"+bundle.getName());
			return null;
		}
	}
	
	public static ResourceBundle getBundle(Bundle bundle) {
		return ResourceBundle.getBundle(bundle.getName(), LOCALE);
	}
}
