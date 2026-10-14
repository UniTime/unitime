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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.restlet.Client;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.localization.impl.ExportTranslations.Locale;
import org.unitime.localization.impl.POHelper.Bundle;
import org.unitime.localization.messages.PageNames;
import org.xml.sax.SAXException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Tomas Muller
 */
public class ImportTranslations {
	private List<Locale> iLocales = new ArrayList<Locale>();
	private Project iProject;
	private File iBaseDir;
	private File iSource;
	private String iTranslations = "Documentation/Translations";
	private String iToken = null;
	private boolean iGeneratePageNames = false;
	private boolean iFixGwtConfig = true;
	private boolean iDownload = true;

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
	
	public void setFixGwtConfig(boolean fixGwtConfig) {
		iFixGwtConfig = fixGwtConfig;
	}
	
	public void setDownload(boolean download) {
		iDownload = download;
	}
	
	public void setToken(String token) {
		iToken = token;
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
			Map<String, byte[]> downloads = new HashMap<String, byte[]>();
			if (iDownload) {
				info("Downloading translations to: " + translations);
				
				List<Protocol> protocols = new ArrayList<Protocol>();
				protocols.add(Protocol.HTTP);
				protocols.add(Protocol.HTTPS);
				Client client = new Client(protocols);
				
				for (Locale locale: iLocales) {
					info("Locale " + locale.getValue().replace('_', '-').toLowerCase());
					ClientResource resource = new ClientResource("https://api.poeditor.com/v2/projects/export");
					resource.setNext(client);
					Form form = new Form();
					form.add("api_token", iToken);
					form.add("id", "568029");
					form.add("language", locale.getValue().replace('_', '-').toLowerCase());
					form.add("type", "po");
					String response = resource.post(form.getWebRepresentation(CharacterSet.UTF_8)).getText();
					JsonObject json = JsonParser.parseString(response).getAsJsonObject();
					info("Response: " + json);
					String url = json.getAsJsonObject("result").get("url").getAsString();
					debug("URL: " + url);
					resource.release();
					resource = new ClientResource(url);
					info("Downloading " + url);
					InputStream in = resource.get().getStream();
					byte[] buffer = new byte[10240];
					File file = new File(translations, "UniTime" + org.unitime.timetable.util.Constants.VERSION + "_" + locale.getValue() + ".po");
					debug("Writing " + file);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					FileOutputStream out = new FileOutputStream(file);
					int read = 0;
					while ((read = in.read(buffer)) > 0) {
						out.write(buffer, 0, read);
						bos.write(buffer, 0, read);
					}
					out.flush();
					out.close();
					bos.flush();
					bos.close();
					downloads.put(locale.getValue(), bos.toByteArray());
					resource.release();
				}
				client.stop();
			}
			
			if (downloads.isEmpty())
				info("Importing translations from: " + translations);
			else
				info("Importing translations");
			
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

				byte[] data = downloads.get(locale.getValue());
				if (data == null) {
					File input = new File(translations, "UniTime" + org.unitime.timetable.util.Constants.VERSION + "_" + locale.getValue() + ".po");
					if (!input.exists()) {
						error("Input file " + input + " does not exist.");
						continue;
					}
					helper.readPOFile(null, new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8));
				} else {
					helper.readPOFile(null, new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8));
				}
				
				for (Bundle bundle: Bundle.values())
					helper.writePropertiesFile(iSource, bundle);
			}
			
			Set<String> locales = new HashSet<String>();
			if (iFixGwtConfig) {
				info("Updating GWT configuration, if needed.");
				File config = new File(iSource,
						"org" + File.separator + "unitime" + File.separator + "timetable" + File.separator + "gwt"+  File.separator + "UniTime.gwt.xml");
				SAXReader reader = new SAXReader();
				try {
					reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
					reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
					reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
				} catch (SAXException e) {}
				Document document = reader.read(config);
				for (Iterator<Element> i = document.getRootElement().elementIterator("extend-property"); i.hasNext(); ) {
					Element e = i.next();
					if ("locale".equals(e.attributeValue("name")))
						locales.add(e.attributeValue("values"));
				}
				debug("Existing locales: " + locales);
				boolean changed = false;
				for (Locale locale: iLocales) {
					if (!locales.contains(locale.getValue())) {
						info("added " + locale);
						changed = true;
						document.getRootElement().addElement("extend-property")
							.addAttribute("name", "locale")
							.addAttribute("values", locale.getValue());
					}
				}
				if (changed) {
					FileOutputStream out = new FileOutputStream(config);
		            (new XMLWriter(out, OutputFormat.createPrettyPrint())).write(document);
		            out.flush(); out.close();
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
			task.setLocales(System.getProperty("locale", "cs"));
			task.setToken(System.getProperty("token", "b191dd443ab1800fc1e09ef23e50cdb0"));
			task.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}    
}
