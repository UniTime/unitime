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
