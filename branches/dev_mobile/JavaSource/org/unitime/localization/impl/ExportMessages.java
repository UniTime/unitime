/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.localization.impl;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

/**
 * @author Tomas Muller
 */
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
