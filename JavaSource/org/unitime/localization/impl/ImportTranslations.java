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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.unitime.localization.impl.ExportTranslations.Bundle;
import org.unitime.localization.impl.ExportTranslations.Locale;
import org.unitime.localization.messages.PageNames;
import org.unitime.timetable.gwt.resources.Constants;
import org.unitime.timetable.gwt.resources.Messages;

/**
 * @author Tomas Muller
 */
public class ImportTranslations {
	private List<Bundle> iBundles = new ArrayList<Bundle>();
	private List<Locale> iLocales = new ArrayList<Locale>();
	private Project iProject;
	private File iBaseDir;
	private File iSource;
	private String iTranslations = "Documentation/Translations";
	private boolean iGeneratePageNames = false;

	public ImportTranslations() {}
	
	public void setProject(Project project) {
		iProject = project;
		iBaseDir = project.getBaseDir();
	}
	
	public void setBaseDir(String baseDir) {
		iBaseDir = new File(baseDir);
	}
	
	public void setSource(String source) {
		iSource = new File(source);
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

	public void setGeneratePageNames(boolean generatePageNames) {
		iGeneratePageNames = generatePageNames;
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

    public void execute() throws BuildException {
		try {
			File translations = new File(iBaseDir, iTranslations);
			info("Importing translations from: " + translations);
			
			for (Bundle bundle: iBundles) {
    			info("Loading " + bundle);
				Class clazz = null;
				File folder = null;
    			if (bundle.hasPackage()) {
    				try {
    					clazz =  Class.forName(bundle.getPackage() + "." + bundle.getName());
    					folder = new File(iSource, bundle.getPackage().replace('.', File.separatorChar));
    				} catch (ClassNotFoundException e) {}
    			}
				try {
					clazz = Class.forName(Localization.ROOT + bundle);
					folder = new File(iSource, Localization.ROOT.replace('.', File.separatorChar));
				} catch (ClassNotFoundException e) {}
				try {
					if (clazz == null) {
						clazz = Class.forName(Localization.GWTROOT + bundle);
						folder = new File(iSource, Localization.GWTROOT.replace('.', File.separatorChar));
					}
				} catch (ClassNotFoundException e) {}
				if (clazz == null) {
					error("Bundle " + bundle + " not found.");
					continue;
				}
				boolean constants = Constants.class.isAssignableFrom(clazz);
				
				for (Locale locale: iLocales) {
					debug("Locale " + locale);
					boolean empty = true;
					
					File input = new File(translations, bundle.getName() + "_" + locale.getValue() + ".properties");
					if (!input.exists()) continue;
					
					Properties translation = new Properties();
					translation.load(new FileReader(input));
					
					File output = new File(folder, bundle.getName() + "_" + locale.getValue() + ".properties");
					
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
					
					if (PageNames.class.equals(clazz) && iGeneratePageNames) {
						TreeSet<String> names = new TreeSet<String>();
						for (Object o: translation.keySet())
							names.add((String)o);
						Properties defaults = new Properties();
						defaults.load(new FileReader(new File(translations, bundle.getName() + ".properties")));
						for (Object o: defaults.keySet())
							names.add((String)o);
						for (String name: names) {
							String value = defaults.getProperty(name);
							out.println();
							if (value != null)
								out.println("# Default: " + unicodeEscape(value, false).trim());
							String text = translation.getProperty(name);
							if (text == null) {
								if (value != null)
									out.println("# FIXME: Translate \"" + unicodeEscape(value, false) + "\"");
								else
									out.println("# FIXME: Translate " + name);
								out.println("# " + name + "=");
							} else {
								out.println(name + "=" + unicodeEscape(text, true));
								empty = false;
							}
						}
					} else {
						TreeSet<Method> methods = new TreeSet<Method>(new Comparator<Method>() {
							@Override
							public int compare(Method m1, Method m2) {
								return m1.getName().compareTo(m2.getName());
							}
						});
						for (Method method: clazz.getMethods()) methods.add(method);
						for (Method method: methods) {
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

							String text = translation.getProperty(method.getName(), old.getProperty(method.getName()));
							boolean doNotTranslate = (method.getAnnotation(Messages.DoNotTranslate.class) != null) || (method.getAnnotation(Constants.DoNotTranslate.class) != null);
							if (text == null && (constants || doNotTranslate)) continue;
							
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
								empty = false;
							}
						}
					}
					
					out.flush();
					out.close();
					if (empty) output.delete();
				}
			}
		} catch (Exception e) {
			throw new BuildException("Import failed: " + e.getMessage(), e);
		}
	}
    
	public static void main(String[] args) {
		try {
			ImportTranslations task = new ImportTranslations();
			task.setBaseDir(System.getProperty("source", "/Users/muller/git/unitime"));
			task.setSource(System.getProperty("source", "/Users/muller/git/unitime") + File.separator + "JavaSource");
			task.setBundles(System.getProperty("bundle", "CourseMessages,ConstantsMessages,ExaminationMessages,SecurityMessages,GwtConstants,GwtAriaMessages,GwtMessages,StudentSectioningConstants,StudentSectioningMessages"));
			task.setLocales(System.getProperty("locale", "cs"));
			task.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}    
}
