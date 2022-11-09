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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.unitime.localization.impl.POHelper.Bundle;
import org.unitime.localization.messages.PageNames;

/**
 * @author Tomas Muller
 */
public class ExportTranslations {
	private List<Locale> iLocales = new ArrayList<Locale>();
	private File iBaseDir;
	private String iTranslations = "Documentation/Translations";
	private File iSource;
	private Project iProject = null;
	private boolean iGeneratePageNames = false;
	
	public ExportTranslations() {}
	
	public void setProject(Project project) {
		iProject = project;
		iBaseDir = project.getBaseDir();
	}
	
	public void setSource(String source) {
		iSource = new File(source);
	}
	
	public void setBaseDir(String baseDir) {
		iBaseDir = new File(baseDir);
	}
	
	public void setGeneratePageNames(boolean generatePageNames) {
		iGeneratePageNames = generatePageNames;
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
    		Map<String, String> pageNames = null;
    		if (iGeneratePageNames) {
    			PageNameGenerator gen = new PageNameGenerator();
    			gen.setSource(iSource);
    			gen.execute();
    			pageNames = gen.getPageNames();
    		} else {
    			Properties p = new Properties();
    			p.load(new FileInputStream(new File(iSource, PageNames.class.getName().replace('.', File.separatorChar) + ".properties")));
    			pageNames = new HashMap<String, String>();
    			for (Map.Entry e: p.entrySet())
    				pageNames.put((String)e.getKey(), (String)e.getValue());
    		}
    		
			for (Locale locale: iLocales) {
				debug("Locale " + locale);
				
				POHelper helper = new POHelper(locale.getValue(), pageNames);
				for (Bundle bundle: Bundle.values())
					helper.readProperties(bundle, iSource, locale.getValue());
				
				helper.writePOFile(new File(translations, "UniTime" + org.unitime.timetable.util.Constants.VERSION + "_" + locale.getValue() + ".po"));
				helper.writePOTFile(new File(translations, "UniTime" + org.unitime.timetable.util.Constants.VERSION + ".pot"));
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new BuildException("Export failed: " + e.getMessage(), e);
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
			task.setSource(System.getProperty("source", "/Users/muller/git/unitime") + File.separator + "JavaSource");
			task.setLocales(System.getProperty("locale", "cs"));
			task.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
