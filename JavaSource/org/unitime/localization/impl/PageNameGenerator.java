package org.unitime.localization.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
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
	private TreeSet<String> iPageNames = new TreeSet<String>();
	private File iSource;
	
	public PageNameGenerator() {
	}
	
	public void setSource(File source) {
		iSource = source;
	}
	
	public void checkPageNamecClass() {
		for (Method method: PageNames.class.getMethods()) {
			DefaultMessage dm = method.getAnnotation(DefaultMessage.class);
			if (dm != null)
				if (iPageNames.add(dm.value()))
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
				if (iPageNames.add(name))
					System.out.println(" [menu] " + name);
			}
		}
		for (Iterator i = element.elementIterator("menu"); i.hasNext(); ) {
			Element menu = (Element)i.next();
			String name = menu.attributeValue("name");			
			if (name != null && !name.isEmpty()) {
				if (iPageNames.add(name))
					System.out.println(" [menu] " + name);
			}
			parseMenu(menu);
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
			String name = property2name((String)o);
			if (iPageNames.add(name))
				System.out.println("   [" + locale + "] " + name);
		}
	}
	
	public void checkGwtMessages() {
		for (Method method: GwtMessages.class.getMethods()) {
			if (!method.getName().startsWith("page")) continue;
			boolean doNotTranslate = (method.getAnnotation(Messages.DoNotTranslate.class) != null) || (method.getAnnotation(Constants.DoNotTranslate.class) != null);
			if (!doNotTranslate) continue;
			DefaultMessage dm = method.getAnnotation(DefaultMessage.class);
			if (dm != null && !dm.value().contains("{0}"))
				if (iPageNames.add(dm.value()))
					System.out.println("  [gwt] " + dm.value());
		}
	}
	
	public void checkOnlineHelp() throws IOException, DocumentException {
		URL url = new URL("https://sites.google.com/feeds/content/unitime.org/help41?kind=webpage");
		while (url != null) {
			Element feed = readHelpContentFeed(url).getRootElement();
			url = null;
			for (Iterator i = feed.elementIterator("entry"); i.hasNext(); ) {
				Element entry = (Element)i.next();
				String name = entry.element("pageName").getTextTrim();
				String title = entry.element("title").getTextTrim();
				if (name2property(title).equals(name) && iPageNames.add(title))
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
	
	public String name2property(String name) {
		return name.trim().replace(' ', '_').replace("(", "").replace(")", "").replace(':', '_');
	}
	
	public String property2name(String property) {
		return property.trim().replace('_', ' ').replace('_', ':');
	}
	
	public Properties getProperties() {
		Properties properties = new Properties();
		for (String name: iPageNames) {
			properties.put(name2property(name), name);
		}
		return properties;
	}
	
	public Collection<String> execute() throws Exception {
		checkPageNamecClass();
		checkMenuXML();
		checkGwtMessages();
		checkOnlineHelp();
		checkLocale("cs");
		return iPageNames;
	}
	
	public static void main(String[] args) {
		try {
			PageNameGenerator task = new PageNameGenerator();
			task.execute();
			task.getProperties().store(System.out, null);
		} catch (Exception e) {}
	}

}
