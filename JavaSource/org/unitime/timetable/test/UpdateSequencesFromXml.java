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
package org.unitime.timetable.test;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Tomas Muller
 *
 */
public class UpdateSequencesFromXml {
	private Hashtable<String, TreeSet<String>> iSequences = new Hashtable<String, TreeSet<String>>();
	private Hashtable<String, String> iIdColumns = new Hashtable<String, String>();
	
	private SAXReader iSAXReader = null;
	private String iSource = null;
	private String iConfig = "hibernate.cfg.xml";
	
	public UpdateSequencesFromXml() throws DocumentException {
		iSAXReader = new SAXReader();
		iSAXReader.setEntityResolver(iEntityResolver);
	}
	
	private EntityResolver iEntityResolver = new EntityResolver() {
	    public InputSource resolveEntity(String publicId, String systemId) {
	        if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
	        	return new InputSource(getClass().getClassLoader().getResourceAsStream("org/hibernate/hibernate-mapping-3.0.dtd"));
	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
	        	return new InputSource(getClass().getClassLoader().getResourceAsStream("org/hibernate/hibernate-configuration-3.0.dtd"));
	        }
	        return null;
	    }
	};
	
	public void setSource(String source) {
		iSource = source;
	}
	
	public void setConfig(String config) {
		iConfig = config;
	}
	
	protected Document read(String resource) throws IOException, DocumentException {
		if (iSource == null) {
			return iSAXReader.read(getClass().getClassLoader().getResourceAsStream(resource));
		} else {
			return iSAXReader.read(new File(iSource + File.separator + resource));
		}
	}
	
	public void load() throws IOException, DocumentException {
		info("Config: " + (iSource == null ? getClass().getClassLoader().getResource(iConfig) : iSource + File.separator + iConfig));
		info("Reading hibernate.cfg.xml ...");
		Document document = read(iConfig);
		Element root = document.getRootElement();
		Element sessionFactoryElement = root.element("session-factory");
		for (Iterator<Element> i = sessionFactoryElement.elementIterator("mapping"); i.hasNext(); ) {
			Element m = i.next();
			String resource = m.attributeValue("resource");
			if (resource == null) continue;
			info("Processing " + resource + " ...");
			Document resDoc = read(resource);
			Element resRoot = resDoc.getRootElement();
			for (Iterator<Element> j = resRoot.elementIterator("class");j.hasNext(); ) {
				Element classEl = j.next();
				checkSequences(classEl, null, null, null);
			}
		}
	}
	
	public void print() throws IOException, DocumentException {
		String ret = "";
		for (String sequence: new TreeSet<String>(iSequences.keySet())) {
			if (!ret.isEmpty())
				ret += "\nunion all ";
			ret += "select 'DROP SEQUENCE " + sequence.toUpperCase() + "; CREATE SEQUENCE " + sequence.toUpperCase() + " MINVALUE 1 MAXVALUE 99999999999999999999 INCREMENT BY 1 START WITH ' ||";
			TreeSet<String> tables = iSequences.get(sequence);
			if (tables.size() == 1) {
				String table = tables.first(); String idColumn = iIdColumns.get(table);
				ret += " case when max(" + idColumn + ") is not null then max(" + idColumn + " + 1) else 1 end ";
				ret += "|| ' CACHE 20 NOORDER NOCYCLE; GRANT SELECT ON "+sequence.toUpperCase()+" TO webuser;' from " + table;
			} else {
				ret += " case when max(uniqueid) is not null then max(uniqueid + 1) else 1 end ";
				ret += "|| ' CACHE 20 NOORDER NOCYCLE; GRANT SELECT ON "+sequence.toUpperCase()+" TO webuser;' from (";
				boolean first = true;
				for (String table: tables) {
					String idColumn = iIdColumns.get(table);
					if (first) {
						ret += "\nselect max(" + idColumn + " + 1) as uniqueid from " + table;
						first = false;
					} else {
						ret += "\nunion all select max(" + idColumn + " + 1) as uniqueid from " + table;
					}
				}
				ret += "\n)";
			}
			info(sequence + ": " + iSequences.get(sequence));
		}
		info(ret);
		info("All done.");
	}

	@SuppressWarnings("unchecked")
	private void checkSequences(Element classEl, String parentTable, String parentIdColumn, String parentSequence) throws IOException {
		String table = classEl.attributeValue("table");
		if (table == null) table = parentTable;
		
		String sequence = null, idColumn = null;
		for (Iterator<Element> i = classEl.elementIterator("id"); i.hasNext();) {
			Element el = i.next();
			idColumn = el.attributeValue("column").toLowerCase();
			if (el.element("generator") != null)
				for (Iterator<Element> j = el.element("generator").elementIterator("param"); j.hasNext();) {
					Element p = j.next();
					if ("sequence".equals(p.attributeValue("name")))
						sequence = p.getTextTrim();
				}
		}
		if (sequence == null) sequence = parentSequence;
		if (idColumn == null) idColumn = parentIdColumn;
		
		if (sequence != null && table != null) {
			info("  " + table + "." + idColumn + ": " + sequence);
			TreeSet<String> tables = iSequences.get(sequence);
			if (tables == null) {
				tables = new TreeSet<String>();
				iSequences.put(sequence, tables);
			}
			tables.add(table);
			iIdColumns.put(table, idColumn);
		}
		
		for (Iterator<Element> i=classEl.elementIterator("union-subclass");i.hasNext();) {
			checkSequences(i.next(), table, idColumn, sequence);
		}
		for (Iterator<Element> i=classEl.elementIterator("subclass");i.hasNext();) {
			checkSequences(i.next(), table, idColumn, sequence);
		}
	}
	
	public void info(String message) {
		System.out.println(message);
	}
	
	public void warn(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) {
		try {
			UpdateSequencesFromXml update = new UpdateSequencesFromXml();
			update.load();
			if (new File("../unitime-addons").exists()) {
				update.setSource("../unitime-addons/BannerAddOn/JavaSource");
				update.load();
			}
			update.print();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
