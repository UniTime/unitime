/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.localization.impl;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Properties;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

/**
 * @author Tomas Muller
 */
public class ImportTranslations {
	
	private static String array2string(String[] value) {
		String ret = "";
		for (String s: value) {
			if (!ret.isEmpty()) ret += ",";
			ret += s.replace(",", "\\,");
		}
		return ret;
	}
	
    private static final char[] hexChar = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    private static String unicodeEscape(String s, boolean includeColon) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < s.length(); i++) {
    		char c = s.charAt(i);
    	    if ((c >> 7) > 0) {
        		sb.append("\\u");
        		sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
        		sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
        		sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
        		sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
    	    } else if (c == ':' && includeColon) {
    	    	sb.append("\\:");
    	    } else if (c == '\n') {
    	    	sb.append("\\n");
    	    } else {
    	    	sb.append(c);
    	    }
    	}
    	return sb.toString();
    }	

	public static void main(String[] args) {
		try {
			String bundles = System.getProperty("bundle", "CourseMessages,ConstantsMessages,ExaminationMessages,SecurityMessages,GwtConstants,GwtAriaMessages,GwtMessages,StudentSectioningConstants,StudentSectioningMessages");
			String locales = System.getProperty("locale", "cs");
			String source = System.getProperty("source", "/Users/muller/git/unitime");
			File translations = new File(new File(source), "Documentation/Translations");
			
			for (String bundle: bundles.split(",")) {
				System.out.println("Loading " + bundle);
				Class clazz = null;
				File folder = null;
				try {
					clazz = Class.forName(Localization.ROOT + bundle);
					folder = new File(new File(source), "JavaSource" + File.separator + Localization.ROOT.replace('.', File.separatorChar));
				} catch (ClassNotFoundException e) {}
				try {
					if (clazz == null) {
						clazz = Class.forName(Localization.GWTROOT + bundle);
						folder = new File(new File(source), "JavaSource" + File.separator + Localization.GWTROOT.replace('.', File.separatorChar));
					}
				} catch (ClassNotFoundException e) {}
				if (clazz == null) {
					System.err.println("Bundle " + bundle + " not found.");
					continue;
				}
				boolean constants = Constants.class.isAssignableFrom(clazz);
				
				for (String locale: locales.split(",")) {
					System.out.println(" -- " + locale);
					
					File input = new File(translations, bundle + "_" + locale + ".properties");
					if (!input.exists()) continue;
					
					Properties translation = new Properties();
					translation.load(new FileReader(input));
					
					File output = new File(folder, bundle + "_" + locale + ".properties");
					
					Properties old = new Properties();
					if (output.exists()) old.load(new FileReader(output));
					
					PrintStream out = new PrintStream(output);
					
					out.println("# Licensed to The Apereo Foundation under one or more contributor license");
					out.println("# agreements. See the NOTICE file distributed with this work for");
					out.println("# additional information regarding copyright ownership.");
					out.println("#");
					out.println("# The Apereo Foundation licenses this file to you under the Apache License,");
					out.println("# Version 2.0 (the \"License\"); you may not use this file except in");
					out.println("# compliance with the License. You may obtain a copy of the License at:");
					out.println("#");
					out.println("# http://www.apache.org/licenses/LICENSE-2.0");
					out.println("#");
					out.println("# Unless required by applicable law or agreed to in writing, software");
					out.println("# distributed under the License is distributed on an \"AS IS\" BASIS,");
					out.println("# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
					out.println("#");
					out.println("# See the License for the specific language governing permissions and");
					out.println("# limitations under the License.");
					out.println("#");
					
					for (Method method: clazz.getMethods()) {
						String value = null;
						DefaultMessage dm = method.getAnnotation(DefaultMessage.class);
						if (dm != null)
							value = dm.value();
						Constants.DefaultBooleanValue db = method.getAnnotation(Constants.DefaultBooleanValue.class);
						if (db != null)
							value = (db.value() ? "true" : "false");
						Constants.DefaultDoubleValue dd = method.getAnnotation(Constants.DefaultDoubleValue.class);
						if (dd != null)
							value = String.valueOf(dd.value());
						Constants.DefaultFloatValue df = method.getAnnotation(Constants.DefaultFloatValue.class);
						if (df != null)
							value = String.valueOf(df.value());
						Constants.DefaultIntValue di = method.getAnnotation(Constants.DefaultIntValue.class);
						if (di != null)
							value = String.valueOf(di.value());					
						Constants.DefaultStringValue ds = method.getAnnotation(Constants.DefaultStringValue.class);
						if (ds != null)
							value = ds.value();
						Constants.DefaultStringArrayValue dsa = method.getAnnotation(Constants.DefaultStringArrayValue.class);
						if (dsa != null)
							value = array2string(dsa.value());
						Constants.DefaultStringMapValue dsm = method.getAnnotation(Constants.DefaultStringMapValue.class);
						if (dsm != null)
							value = array2string(dsm.value());
						if ("translateMessage".equals(method.getName())) continue;

						String text = translation.getProperty(method.getName(), old.getProperty(method.getName()));
						if (text == null && constants) continue;

						out.println();
						if (value != null)
							out.println("# Default: " + unicodeEscape(value, false).trim());
						if (text == null) {
							if (value != null)
								out.println("# FIXME: Translate \"" + unicodeEscape(value, false) + "\"");
							else
								out.println("# FIXME: Translate " + method.getName());
							out.println("# " + method.getName() + "=");
						} else {
							out.println(method.getName() + "=" + unicodeEscape(text, true));
						}
					}
					
					out.flush();
					out.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
