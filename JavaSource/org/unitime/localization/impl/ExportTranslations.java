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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.unitime.timetable.gwt.resources.Constants;
import org.unitime.timetable.gwt.resources.Messages;

/**
 * @author Tomas Muller
 */
public class ExportTranslations {
	private List<Bundle> iBundles = new ArrayList<Bundle>();
	private List<Locale> iLocales = new ArrayList<Locale>();
	private File iBaseDir;
	private String iTranslations = "Documentation/Translations";
	private Project iProject = null;
	
	public ExportTranslations() {}
	
	public void setProject(Project project) {
		iProject = project;
		iBaseDir = project.getBaseDir();
	}
	
	public void setBaseDir(String baseDir) {
		iBaseDir = new File(baseDir);
	}
	
	public Bundle createBundle() {
		Bundle bundle = new Bundle();
		iBundles.add(bundle);
		return bundle;
	}
	
	public void addBundle(Bundle bundle) {
		iBundles.add(bundle);
	}
	
	public void setBundles(String bundles) {
		for (String name: bundles.split(",")) {
			addBundle(new Bundle(name));
		}
	}
	
	public Locale createLocale() {
		Locale locale = new Locale();
		iLocales.add(locale);
		return locale;
	}
	
	public void addLocale(Locale locale) {
		iLocales.add(locale);
	}
	
	public void setLocales(String locales) {
		for (String value: locales.split(",")) {
			addLocale(new Locale(value));
		}
	}

	public void setTranslations(String translations) {
		iTranslations = translations;
	}
	
	private static String array2string(String[] value) {
		String ret = "";
		for (String s: value) {
			if (!ret.isEmpty()) ret += ",";
			ret += s.replace(",", "\\\\,");
		}
		return ret;
	}
	
    private static final char[] hexChar = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    private static String unicodeEscape(String s) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < s.length(); i++) {
    	    char c = s.charAt(i);
    	    if ((c >> 7) > 0) {
        		sb.append("\\u");
        		sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
        		sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
        		sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
        		sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
    	    } else if (c == '\n') {
    	    	sb.append("\\n");
    	    } else {
    	    	sb.append(c);
    	    }
    	}
    	return sb.toString();
    }
    
    public void info(String message) {
    	if (iProject != null)
    		iProject.log(message);
    	else
    		System.out.println("     [info] " + message);
    }
    
    public void warn(String message) {
    	if (iProject != null)
    		iProject.log(message, Project.MSG_WARN);
    	else
    		System.out.println("  [warning] " +message);
    }
    
    public void debug(String message) {
    	if (iProject != null)
    		iProject.log(message, Project.MSG_DEBUG);
    	else
    		System.out.println("    [debug] " +message);
    }

    public void error(String message) {
    	if (iProject != null)
    		iProject.log(message, Project.MSG_ERR);
    	else
    		System.out.println("    [error] " +message);
    }

    public void execute() throws BuildException {
    	try {
    		File translations = new File(iBaseDir, iTranslations);
    		info("Exporting translations to: " + translations);
    		translations.mkdirs();
    		for (Bundle bundle: iBundles) {
    			info("Loading " + bundle);
    			Class clazz = null;
    			if (bundle.hasPackage()) {
    				try {
    					clazz =  Class.forName(bundle.getPackage() + "." + bundle.getName());
    				} catch (ClassNotFoundException e) {}
    			}
    			try {
    				clazz =  Class.forName(Localization.ROOT + bundle.getName());
    			} catch (ClassNotFoundException e) {}
    			try {
    				if (clazz == null)
    					clazz = Class.forName(Localization.GWTROOT + bundle.getName());
    			} catch (ClassNotFoundException e) {}
    			if (clazz == null) {
    				error("Bundle " + bundle + " not found.");
    				continue;
    			}
    			
    			PrintStream out = new PrintStream(new File(translations, bundle.getName() + ".properties"));
    			
    			for (Method method: clazz.getMethods()) {
    				String value = null;
    				Messages.DefaultMessage dm = method.getAnnotation(Messages.DefaultMessage.class);
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

    				boolean doNotTranslate = (method.getAnnotation(Messages.DoNotTranslate.class) != null) || (method.getAnnotation(Constants.DoNotTranslate.class) != null);
    				if (doNotTranslate) continue;
    				
    				if (value == null)
    					warn("Property " + method.getName() + " has no default value!");
    				
    				out.println("# " + method.getName());
    				out.println(method.getName() + "=" + unicodeEscape(value != null ? value : ""));
    			}
    			
    			out.flush();
    			out.close();
    			
    			for (Locale locale: iLocales) {
    				debug("Locale " + locale);
    				
    				Properties properties = new Properties();
    				InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + "_" + locale + ".properties");
    				if (is != null)
    					properties.load(is);

    				out = new PrintStream(new File(translations, bundle.getName() + "_" + locale.getValue() + ".properties"));
    				
    				for (Method method: clazz.getMethods()) {
    					String text = properties.getProperty(method.getName());
    					if (text != null)
    						out.println(method.getName() + "=" + unicodeEscape(text));
    				}
    				
    				out.flush();
    				out.close();
    			}
    		}
    	} catch (IOException e) {
    		throw new BuildException("Export failed: " + e.getMessage(), e);
    	}
    }

	public static class Bundle {
		String iPackage, iName;
		
		public Bundle(String pck, String name) {
			iPackage = pck; iName = name;
		}
		public Bundle() { this(null, null); }
		public Bundle(String name) { this(null, name); }
		
		public void setPackage(String pck) { iPackage = pck; }
		public boolean hasPackage() { return iPackage != null && !iPackage.isEmpty(); }
		public String getPackage() { return iPackage; }
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		@Override
		public String toString() {
			return (hasPackage() ? getPackage() + "." : "") + getName();
		}
	}
	
	public static class Locale {
		String iValue;
		
		public Locale(String value) { iValue = value; }
		public Locale() { this(null); }
		
		public void setValue(String value) { iValue = value; }
		public String getValue() { return iValue; }
		
		@Override
		public String toString() {
			return getValue();
		}
	}
	
	public static void main(String[] args) {
		try {
			ExportTranslations task = new ExportTranslations();
			task.setBaseDir(System.getProperty("source", "/Users/muller/git/unitime"));
			task.setBundles(System.getProperty("bundle", "CourseMessages,ConstantsMessages,ExaminationMessages,SecurityMessages,GwtConstants,GwtAriaMessages,GwtMessages,StudentSectioningConstants,StudentSectioningMessages"));
			task.setLocales(System.getProperty("locale", "cs"));
			task.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
