package org.unitime.localization.impl;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

public class ExportMessages {
	
	private static String array2string(String[] value) {
		String ret = "";
		for (String s: value) {
			if (!ret.isEmpty()) ret += ",";
			ret += s.replace(",", "\\,");
		}
		return ret;
	}

	public static void main(String[] args) {
		try {
			Class clazz = Class.forName(Localization.ROOT + System.getProperty("bundle", "CourseMessages"));
			String locale = System.getProperty("locale", "cs");
			
			Properties properties = new Properties();
			InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + "_" + locale + ".properties");
			if (is != null)
				properties.load(is);
			
			System.out.println("\"Key\",\"Default\",\"Value\"");
			
			for (Method method: clazz.getMethods()) {
				DefaultMessage dm = method.getAnnotation(DefaultMessage.class);
				String text = properties.getProperty(method.getName());
				if (dm != null)
					System.out.println("\"" + method.getName() + "\",\"" + dm.value() + "\",\"" + (text == null ? "" : text) + "\"");
				Constants.DefaultBooleanValue db = method.getAnnotation(Constants.DefaultBooleanValue.class);
				if (db != null)
					System.out.println("\"" + method.getName() + "\",\"" + (db.value() ? "true" : "false") + "\",\"" + (text == null ? "" : text) + "\"");
				Constants.DefaultDoubleValue dd = method.getAnnotation(Constants.DefaultDoubleValue.class);
				if (dd != null)
					System.out.println("\"" + method.getName() + "\",\"" + dd.value() + "\",\"" + (text == null ? "" : text) + "\"");
				Constants.DefaultFloatValue df = method.getAnnotation(Constants.DefaultFloatValue.class);
				if (df != null)
					System.out.println("\"" + method.getName() + "\",\"" + df.value() + "\",\"" + (text == null ? "" : text) + "\"");
				Constants.DefaultIntValue di = method.getAnnotation(Constants.DefaultIntValue.class);
				if (di != null)
					System.out.println("\"" + method.getName() + "\",\"" + di.value() + "\",\"" + (text == null ? "" : text) + "\"");
				Constants.DefaultStringValue ds = method.getAnnotation(Constants.DefaultStringValue.class);
				if (ds != null)
					System.out.println("\"" + method.getName() + "\",\"" + ds.value() + "\",\"" + (text == null ? "" : text) + "\"");
				Constants.DefaultStringArrayValue dsa = method.getAnnotation(Constants.DefaultStringArrayValue.class);
				if (dsa != null)
					System.out.println("\"" + method.getName() + "\",\"" + array2string(dsa.value()) + "\",\"" + (text == null ? "" : text) + "\"");
				Constants.DefaultStringMapValue dsm = method.getAnnotation(Constants.DefaultStringMapValue.class);
				if (dsm != null)
					System.out.println("\"" + method.getName() + "\",\"" + array2string(dsm.value()) + "\",\"" + (text == null ? "" : text) + "\"");
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
