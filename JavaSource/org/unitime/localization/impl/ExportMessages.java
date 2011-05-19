package org.unitime.localization.impl;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import com.google.gwt.i18n.client.Messages.DefaultMessage;

public class ExportMessages {

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
				if (dm != null) {
					String text = properties.getProperty(method.getName());
					System.out.println("\"" + method.getName() + "\",\"" + dm.value() + "\",\"" + (text == null ? "" : text) + "\"");
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
