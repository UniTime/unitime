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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.localization.messages.PageNames;
import org.unitime.timetable.gwt.resources.Constants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.Messages;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.google.gwt.i18n.client.Messages.DefaultMessage;

public class PageNameGenerator {
	private Map<String, String> iPageNames = new HashMap<String, String>();
	private File iSource;
	
	public PageNameGenerator() {
	}
	
	public void setSource(File source) {
		iSource = source;
	}
	
	protected boolean addValue(String name) {
		String key = name2property(name);
		if (iPageNames.containsKey(key)) {
			return false;
		} else {
			iPageNames.put(key, name);
			return true;
		}
	}
	
	protected boolean addKey(String key) {
		if (iPageNames.containsKey(key)) {
			return false;
		} else {
			iPageNames.put(key, property2name(key));
			return true;
		}
	}
	
	protected boolean addPair(String key, String name) {
		if (iPageNames.containsKey(key)) {
			return false;
		} else {
			iPageNames.put(key, name);
			return true;
		}
	}
	
	public void checkPageNamecClass() {
		for (Method method: PageNames.class.getMethods()) {
			DefaultMessage dm = method.getAnnotation(DefaultMessage.class);
			if (dm != null)
				if (addValue(dm.value()))
					System.out.println("[names] " + dm.value());
		}
	}
	
	public void checkMenuXML() throws DocumentException, IOException {
        SAXReader sax = new SAXReader();
        Document document = null;
        InputStream is = PageNameGenerator.class.getClassLoader().getResourceAsStream("menu.xml");
        if (is != null) {
            sax.setEntityResolver(new EntityResolver() {
            	public InputSource resolveEntity(String publicId, String systemId) {
            		if (publicId.equals("-//UniTime//UniTime Menu DTD/EN")) {
            			return new InputSource(PageNameGenerator.class.getClassLoader().getResourceAsStream("menu.dtd"));
            		}
            		return null;
            	}
            });
        	document = sax.read(is);
        } else if (iSource != null) {
        	File file = new File(iSource, "menu.xml");
    		sax.setEntityResolver(new EntityResolver() {
            	public InputSource resolveEntity(String publicId, String systemId) {
            		if (publicId.equals("-//UniTime//UniTime Menu DTD/EN")) {
            			try {
            				return new InputSource(new FileInputStream(new File(iSource, "menu.dtd")));
            			} catch (FileNotFoundException e) {}
            		}
            		return null;
            	}
            });
    		System.out.println(" [menu] Using " + new File(iSource, "menu.xml"));
    		document = sax.read(file);
        }
        if (document != null)
        	parseMenu(document.getRootElement());
	}
	
	protected void parseMenu(Element element) {
		for (Iterator i = element.elementIterator("item"); i.hasNext(); ) {
			Element item = (Element)i.next();
			String name = item.attributeValue("name");			
			if (name != null && !name.isEmpty()) {
				if (addValue(name))
					System.out.println(" [menu] " + name);
			}
		}
		for (Iterator i = element.elementIterator("menu"); i.hasNext(); ) {
			Element menu = (Element)i.next();
			String name = menu.attributeValue("name");			
			if (name != null && !name.isEmpty()) {
				if (addValue(name))
					System.out.println(" [menu] " + name);
			}
			parseMenu(menu);
		}
	}
	
	public void checkProperties() throws IOException {
		Properties properties = new Properties();
		InputStream is = PageNameGenerator.class.getClassLoader().getResourceAsStream("org/unitime/localization/messages/PageNames.properties");
		if (is == null && iSource != null) {
			is = new FileInputStream(new File(iSource, "org" + File.separator + "unitime" + File.separator + "localization" + File.separator + "messages" + File.separator + "PageNames" + ".properties"));
		}
		if (is == null) return;
		try {
			properties.load(is);
		} finally {
			is.close();
		}
		for (Map.Entry<Object, Object> e: properties.entrySet()) {
			String key = (String)e.getKey();
			String name = (String)e.getValue();
			if (addPair(key, name))
				System.out.println("   [default] " + name);
		}
	}
	
	public void checkLocale(String locale) throws IOException {
		Properties properties = new Properties();
		InputStream is = PageNameGenerator.class.getClassLoader().getResourceAsStream("org/unitime/localization/messages/PageNames_" + locale + ".properties");
		if (is == null && iSource != null) {
			is = new FileInputStream(new File(iSource, "org" + File.separator + "unitime" + File.separator + "localization" + File.separator + "messages" + File.separator + "PageNames_" + locale + ".properties"));
		}
		if (is == null) return;
		try {
			properties.load(is);
		} finally {
			is.close();
		}
		for (Object o: properties.keySet()) {
			String key = (String)o;
			if (addKey(key))
				System.out.println("   [" + locale + "] " + property2name(key));
		}
	}
	
	public void checkGwtMessages() {
		for (Method method: GwtMessages.class.getMethods()) {
			if (!method.getName().startsWith("page")) continue;
			boolean doNotTranslate = (method.getAnnotation(Messages.DoNotTranslate.class) != null) || (method.getAnnotation(Constants.DoNotTranslate.class) != null);
			if (!doNotTranslate) continue;
			DefaultMessage dm = method.getAnnotation(DefaultMessage.class);
			if (dm != null && !dm.value().contains("{0}"))
				if (addValue(dm.value()))
					System.out.println("  [gwt] " + dm.value());
		}
	}
	
	public void checkOnlineHelp() throws IOException, DocumentException {
		URL url = new URL("https://sites.google.com/feeds/content/unitime.org/help45?kind=webpage");
		while (url != null) {
			Element feed = readHelpContentFeed(url).getRootElement();
			url = null;
			for (Iterator i = feed.elementIterator("entry"); i.hasNext(); ) {
				Element entry = (Element)i.next();
				String name = entry.element("pageName").getTextTrim();
				String title = entry.element("title").getTextTrim();
				if (name2old(title).equals(name) && addValue(title))
					System.out.println(" [help] " + title);
			}
			for (Iterator i = feed.elementIterator("link"); i.hasNext(); ) {
				Element link = (Element)i.next();
				if ("next".equals(link.attributeValue("rel"))) {
					System.out.println(" [next] " + link.attributeValue("href"));
					url = new URL(link.attributeValue("href"));
				}
			}
		}
	}
	
	protected Document readHelpContentFeed(URL url) throws IOException, DocumentException {
		InputStream in = url.openStream();
		try {
			return new SAXReader().read(in);
		} finally {
			in.close();
		}
	}
	
	public String name2old(String name) {
		return name.trim().replace(' ', '_').replace("(", "").replace(")", "").replace(':', '_');
	}
	
	public String name2property(String name) {
		return name.trim().replace(' ', '-').replace("(", "").replace(")", "").replace(':', '-').toLowerCase();
	}
	
	public String property2name(String property) {
		if (iPageNames.containsKey(property))
			return iPageNames.get(property);
		return org.unitime.timetable.util.Constants.toInitialCase(property.trim()).replace('-', ' ');
	}
	
	public Properties getProperties() {
		Properties properties = new Properties();
		for (Map.Entry<String, String> e: iPageNames.entrySet()) {
			properties.put(e.getKey(), e.getValue());
		}
		return properties;
	}
	
	public Collection<String> execute() throws Exception {
		checkPageNamecClass();
		checkMenuXML();
		checkGwtMessages();
		checkOnlineHelp();
		checkProperties();
		checkLocale("cs");
		return new TreeSet<String>(iPageNames.keySet());
	}
	
	public static void main(String[] args) {
		try {
			PageNameGenerator task = new PageNameGenerator();
			task.execute();
			task.getProperties().store(System.out, null);
		} catch (Exception e) {}
	}

}
