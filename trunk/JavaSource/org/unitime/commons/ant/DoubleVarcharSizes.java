/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.commons.ant;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

/**
 * The following class will go through all hbm.xml files and generate (Oracle) SQL commands
 * that will double the size of all varchars2. This is needed for Oracle when non-english characters
 * are used as these may take up to two spaces each.
 * 
 * @author Tomas Muller
 *
 */
public class DoubleVarcharSizes extends Task {
	private SAXReader iSAXReader = null;
	private String iSource = null;
	private String iConfig = "hibernate.cfg.xml";
	
	public DoubleVarcharSizes() throws DocumentException, SAXException {
		iSAXReader = new SAXReader();
		iSAXReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	}
	
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
	
	protected void pretty(File f) {
	}
	
	public void execute() throws BuildException {
		try {
			generate();
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
	
	public void info(String message) {
		try {
			log(message);
		} catch (Exception e) {
			System.out.println(message);
		}
	}
	
	public void warn(String message) {
		try {
			log(message, Project.MSG_WARN);
		} catch (Exception e) {
			System.out.println(message);
		}
	}

	public void generate() throws IOException, DocumentException {
		Document document = read(iConfig);
		Element root = document.getRootElement();
		Element sessionFactoryElement = root.element("session-factory");
		for (Iterator<Element> i = sessionFactoryElement.elementIterator("mapping"); i.hasNext(); ) {
			Element m = i.next();
			String resource = m.attributeValue("resource");
			if (resource == null) continue;
			generate(read(resource).getRootElement(), null);
		}
	}
	
	private void generate(Element element, String table) {
		table = element.attributeValue("table", table);
		if (table != null && "property".equals(element.getName())) {
			String column = element.attributeValue("column");
			String type = element.attributeValue("type");
			String length = element.attributeValue("length");
			if (("String".equals(type) || "java.lang.String".equals(type)) && length != null && !length.isEmpty() && column != null && !column.isEmpty()) {
				int size = Integer.parseInt(length);
				info("alter table " + table + " modify " + column + " varchar2(" + Math.min(4000, 2 * size) + ");");
			}
		}
		for (Iterator<Element> i = element.elementIterator(); i.hasNext(); ) {
			generate(i.next(), table);
		}
	}

	public static void main(String[] args) {
		try {
			DoubleVarcharSizes dvs = new DoubleVarcharSizes();
			dvs.generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
