/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dom4j.Attribute;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Lower case table, column and sequence names, and foreign keys in .hbm.xml files.
 * This should negate the need to set MySQL to case insensitive mode on Linux based systems.
 * 
 * @author Tomas Muller
 *
 */
public class LowercaseTableNames extends Task {
	private SAXReader iSAXReader = null;
	private String iSource = null;
	private String iConfig = "hibernate.cfg.xml";
	
	public LowercaseTableNames() throws DocumentException, SAXException {
		iSAXReader = new SAXReader();
		iSAXReader.setEntityResolver(iEntityResolver);
		iSAXReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	}
	
	private EntityResolver iEntityResolver = new EntityResolver() {
	    public InputSource resolveEntity(String publicId, String systemId) {
	        if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
	        	InputStream stream = null;
	        	if (iSource == null) {
		            stream = getClass().getClassLoader().getResourceAsStream("hibernate-mapping-3.0.dtd");
	        	} else {
	        		try {
	        			stream = new FileInputStream(iSource + File.separator + "hibernate-mapping-3.0.dtd");
	        		} catch (FileNotFoundException e) {}
	        	}
	        	return (stream == null ? null : new InputSource(stream));
	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
	        	InputStream stream = null;
	        	if (iSource == null) {
		            stream = getClass().getClassLoader().getResourceAsStream("hibernate-configuration-3.0.dtd");
	        	} else {
	        		try {
	        			stream = new FileInputStream(iSource + File.separator + "hibernate-configuration-3.0.dtd");
	        		} catch (FileNotFoundException e) {}
	        	}
	        	return (stream == null ? null : new InputSource(stream));
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
			info("  -- reading " + resource + " ...");
			return iSAXReader.read(getClass().getClassLoader().getResourceAsStream(resource));
		} else {
			info("  -- reading " + iSource + File.separator + resource + " ...");
			return iSAXReader.read(new File(iSource + File.separator + resource));
		}
	}

	protected void write(String resource, Document document) throws IOException, DocumentException {
		File file = null;
		if (iSource == null) {
			file = new File(getClass().getClassLoader().getResource(resource).getFile());
		} else {
			file = new File(iSource + File.separator + resource);
		}
		info("  -- writing " + file + " ...");
		FileOutputStream fos = new FileOutputStream(file);
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setIndentSize(4);
			format.setPadText(false);
			new MyXMLWriter(fos, format).write(document);
		} finally {
			fos.flush(); fos.close();
		}
	}
	
	protected void pretty(File f) {
	}
	
	public void execute() throws BuildException {
		try {
			convert();
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

	public void convert() throws IOException, DocumentException {
		info("Config: " + (iSource == null ? getClass().getClassLoader().getResource(iConfig) : iSource + File.separator + iConfig));
		File workDir = null;
		if (iSource == null) {
			workDir = new File(getClass().getClassLoader().getResource(iConfig).getFile());
			while (workDir.getParentFile() != null && !"WebContent".equals(workDir.getName()))
				workDir = workDir.getParentFile();
			workDir = new File(workDir.getParentFile(), "JavaSource");
			workDir.mkdirs();
		} else {
			workDir = new File(iSource);
		}
		info("Working directory: " + workDir);
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
			convert(resDoc.getRootElement());
			write(resource, resDoc);
		}
	}
	
	private String[] sAttributes = new String[] {
			"table", "sequence", "column", "foreign-key", "order-by"
	};
	
	private String[] sFormulas = new String[] {
			"formula", "where"
	};

	private String lowerFormula(String formula) {
		boolean quot = false;
		String ret = "";
		for (int i = 0; i < formula.length(); i++) {
			char ch = formula.charAt(i);
			if (ch == '\'') quot = !quot;
			ret += (quot ? ch : Character.toLowerCase(ch));
		}
		return ret.replace("%schema%", "%SCHEMA%").replace("/*+ rule */", "/*+ RULE */");
	}

	private void convert(Element element) {
		for (Iterator<Attribute> i = element.attributeIterator(); i.hasNext(); ) {
			Attribute attribute = i.next();
			for (String name: sAttributes) {
				if (name.equals(attribute.getName())) {
					if (!attribute.getValue().equals(attribute.getValue().toLowerCase())) {
						info("  -- converting " + name + " " + attribute.getValue() + " to " + attribute.getValue().toLowerCase());
					}
					attribute.setValue(attribute.getValue().toLowerCase());
				}
			}
			for (String name: sFormulas) {
				if (name.equals(attribute.getName())) {
					if (!lowerFormula(attribute.getValue()).equals(attribute.getValue())) {
						info("  -- converting "+name+": " + attribute.getValue());
						info("  -- into : " + lowerFormula(attribute.getValue()));
					}
					attribute.setValue(lowerFormula(attribute.getValue()));
				}
			}
		}
		if (element.getName().equals("param")) {
			for (String name: sAttributes) {
				if (name.equals(element.attributeValue("name", ""))) {
					if (!element.getText().equals(element.getText().toLowerCase())) {
						info("  -- converting " + name + " " + element.getText() + " to " + element.getText().toLowerCase());
					}
					element.setText(element.getText().toLowerCase());
				}
			}
		}
		for (Iterator<Element> i = element.elementIterator(); i.hasNext(); ) {
			convert(i.next());
		}
	}

	public static void main(String[] args) {
		try {
			LowercaseTableNames ltn = new LowercaseTableNames();
			ltn.setSource("/Users/muller/Sources/UniTime/JavaSource");
			ltn.convert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class MyXMLWriter extends XMLWriter {
		private OutputFormat format;
	    private int indentLevel = 0;
		
		public MyXMLWriter(OutputStream out, OutputFormat format) throws UnsupportedEncodingException {
			super(out, format);
			this.format = format;
		}
	
	    protected void indent() throws IOException {
	        String indent = format.getIndent();

	        if ((indent != null) && (indent.length() > 0)) {
	            for (int i = 0; i < indentLevel; i++) {
	                writer.write(indent);
	            }
	        }
	    }

	    protected void writeAttributes(Element element) throws IOException {
	        for (int i = 0, size = element.attributeCount(); i < size; i++) {
	            Attribute attribute = element.attribute(i);
                char quote = format.getAttributeQuoteCharacter();
                if (element.attributeCount() > 2) {
                    writePrintln();
                    indent();
                    writer.write(format.getIndent());
                } else {
                	writer.write(" ");
                }
                writer.write(attribute.getQualifiedName());
                writer.write("=");
                writer.write(quote);
                writeEscapeAttributeEntities(attribute.getValue());
                writer.write(quote);
	        }
	    }
	    
	    protected void writeElement(Element element) throws IOException {
	        int size = element.nodeCount();
	        String qualifiedName = element.getQualifiedName();

	        writePrintln();
	        indent();

	        writer.write("<");
	        writer.write(qualifiedName);

	        boolean textOnly = true;

	        for (int i = 0; i < size; i++) {
	            Node node = element.node(i);
	            if (node instanceof Element) {
	                textOnly = false;
	            } else if (node instanceof Comment) {
	                textOnly = false;
	            }
	        }

	        writeAttributes(element);

	        lastOutputNodeType = Node.ELEMENT_NODE;

	        if (size <= 0) {
	            writeEmptyElementClose(qualifiedName);
	        } else {
	            writer.write(">");

	            if (textOnly) {
	                // we have at least one text node so lets assume
	                // that its non-empty
	                writeElementContent(element);
	            } else {
	            	if (element.attributeCount() > 3)
	            		writePrintln();
	                // we know it's not null or empty from above
	                ++indentLevel;

	                writeElementContent(element);

	                --indentLevel;

	                writePrintln();
	                indent();
	            }

	            writer.write("</");
	            writer.write(qualifiedName);
	            writer.write(">");
	        }
        	if (element.attributeCount() > 2 && indentLevel > 0)
        		writePrintln();

	        lastOutputNodeType = Node.ELEMENT_NODE;
	    }

	}
}
