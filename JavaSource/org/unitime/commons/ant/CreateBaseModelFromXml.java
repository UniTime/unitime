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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Creates base model from UniTime3 .hbm.xml files.
 * @author Tomas Muller
 *
 */
public class CreateBaseModelFromXml extends Task {
	private Hashtable<String, String> iParent = new Hashtable<String, String>();
	private Hashtable<String, String[]> iIds = new Hashtable<String, String[]>();
	private Hashtable<String, TreeSet<String>> iClassProperties = new Hashtable<String, TreeSet<String>>();
	
	private SAXReader iSAXReader = null;
	private String iSource = null;
	private String iConfig = "hibernate.cfg.xml";
	
	public CreateBaseModelFromXml() throws DocumentException {
		iSAXReader = new SAXReader();
		iSAXReader.setEntityResolver(iEntityResolver);
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
			return iSAXReader.read(getClass().getClassLoader().getResourceAsStream(resource));
		} else {
			return iSAXReader.read(new File(iSource + File.separator + resource));
		}
	}
	
	@SuppressWarnings("unchecked")
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
			info("Pre-processing " + resource + " ...");
			Document resDoc = read(resource);
			Element resRoot = resDoc.getRootElement();
			String pkg = resRoot.attributeValue("package");
			for (Iterator<Element> j = resRoot.elementIterator("class");j.hasNext(); ) {
				Element classEl = j.next();
				preprocess(classEl, null, pkg);
			}
		}
		for (Iterator<Element> i = sessionFactoryElement.elementIterator("mapping"); i.hasNext(); ) {
			Element m = i.next();
			String resource = m.attributeValue("resource");
			if (resource == null) continue;
			info("Processing " + resource + " ...");
			Document resDoc = read(resource);
			Element resRoot = resDoc.getRootElement();
			String pkg = resRoot.attributeValue("package");
			for (Iterator<Element> j = resRoot.elementIterator("class");j.hasNext(); ) {
				Element classEl = j.next();
				importClass(classEl, pkg, workDir, null, null, null, null);
			}
		}
		info("All done.");
	}
	
	@SuppressWarnings("unchecked")
	private void preprocess(Element classEl, String ext, String pkg) throws IOException {
		String className = fixType(classEl.attributeValue("name"), pkg);
		if (className.indexOf('.') >= 0) className = className.substring(className.lastIndexOf('.')+1);
		if (ext!=null) iParent.put(className, ext);
		Element idEl = classEl.element("id");
		if (idEl!=null) {
			String type = fixType(idEl.attributeValue("type"), pkg);
			String name = fixName(idEl.attributeValue("name"));
			iIds.put(className, new String[] {type, name});
		}
		for (Iterator<Element> i=classEl.elementIterator("union-subclass");i.hasNext();) {
			preprocess(i.next(), className, pkg);
		}
		for (Iterator<Element> i=classEl.elementIterator("subclass");i.hasNext();) {
			preprocess(i.next(), className, pkg);
		}
	}	

	/*
	@SuppressWarnings("unchecked")
	private String param(Element el, String name) {
		if (el==null) return null;
		for (Iterator<Element> i = el.elementIterator("param"); i.hasNext();) {
			Element p = i.next();
			if (name.equals(p.attributeValue("name"))) return p.getText();
		}
		return null;
	}
	*/
	
	private String fixType(String type, String pkg) {
		if (type == null) return null;
		if (type.startsWith("java.lang.")) return type.substring("java.lang.".length());
		if ("byte[]".equals(type)) return type;
		if (type.indexOf('.')<0) type = type.substring(0,1).toUpperCase() + type.substring(1);
		if ("Boolean".equals(type)) return type;
		if ("Long".equals(type)) return type;
		if ("Integer".equals(type)) return type;
		if ("String".equals(type)) return type;
		if ("Float".equals(type)) return type;
		if ("Double".equals(type)) return type;
		if (type.equals("java.sql.Date")) return "java.util.Date";
		if (type.equalsIgnoreCase("java.sql.TimeStamp")) return "java.util.Date";
		if (type.endsWith(".XmlBlobType")) return "org.dom4j.Document";
		if (type.endsWith(".XmlClobType")) return "org.dom4j.Document";
		if (type.startsWith("java.")) return type;
		if (type.indexOf('.') < 0) type = pkg+"."+type;
		return type;
	}
	
	private String fixName(String name) {
		if (name == null) return null;
		return name.substring(0,1).toUpperCase() + name.substring(1);
	}
	
	/*
	private boolean hasLength(String type) {
		if ("Boolean".equals(type)) return false;
		if ("Long".equals(type)) return false;
		if ("Integer".equals(type)) return false;
		if ("String".equals(type)) return true;
		if ("Float".equals(type)) return false;
		if ("Double".equals(type)) return false;
		if ("Date".equals(type)) return false;
		if ("XmlBlobType".equals(type)) return false;
		if ("XmlClobType".equals(type)) return false;
		warn("Unknown type "+type);
		return false;
	}
	*/
	
	private File fileFromPackage(File outputFolder, String pkg) {
		File ret = new File(outputFolder, pkg.replace('.', File.separatorChar));
		ret.mkdirs();
		return ret;
	}
	
	private void license(PrintWriter pw) {
		pw.println("/*");
		pw.println(" * UniTime 3.2 (University Timetabling Application)");
		pw.println(" * Copyright (C) 2010, UniTime LLC, and individual contributors");
		pw.println(" * as indicated by the @authors tag.");
		pw.println(" *");
		pw.println(" * This program is free software; you can redistribute it and/or modify");
		pw.println(" * it under the terms of the GNU General Public License as published by");
		pw.println(" * the Free Software Foundation; either version 3 of the License, or");
		pw.println(" * (at your option) any later version.");
		pw.println(" * ");
		pw.println(" * This program is distributed in the hope that it will be useful,");
		pw.println(" * but WITHOUT ANY WARRANTY; without even the implied warranty of");
		pw.println(" * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		pw.println(" * GNU General Public License for more details.");
		pw.println(" * ");
		pw.println(" * You should have received a copy of the GNU General Public License along");
		pw.println(" * with this program.  If not, see <http://www.gnu.org/licenses/>.");
		pw.println(" * ");
		pw.println("*/");
	}

	@SuppressWarnings("unchecked")
	private void importClass(Element classEl, String pkg, File outputFolder, String ext, String idClass, String idName, String idType) throws IOException {
		String className = fixType(classEl.attributeValue("name"), pkg);
		@SuppressWarnings("unused")
		String table = classEl.attributeValue("table");
		StringWriter attributes = new StringWriter();
		PrintWriter pwa = new PrintWriter(attributes);
		StringWriter props = new StringWriter();
		PrintWriter pwp = new PrintWriter(props);
		StringWriter body = new StringWriter();
		PrintWriter pwb = new PrintWriter(body);
		TreeSet<String> imports = new TreeSet<String>();
		if (className.indexOf('.') >= 0) {
			//imports.add(className);
			className = className.substring(className.lastIndexOf('.')+1);
		}
		info("  "+className+" ...");
		
		Vector<String[]> manyToOnes = new Vector<String[]>();
		TreeSet<String> properties = new TreeSet<String>();
		Vector<String[]> compositeId = new Vector<String[]>();
		
		/*
		Element discriminator = classEl.element("discriminator");
		String discriminatorColumn = null;
		if (discriminator!=null) {
			discriminatorColumn = discriminator.attributeValue("column").toLowerCase();
		}
		*/
		
		boolean hasProperty = false;
		for (Iterator<Element> i = classEl.elementIterator("id"); i.hasNext();) {
			Element el = i.next();
			String type = fixType(el.attributeValue("type"), pkg);
			if (type.indexOf('.')>=0) {
				imports.add(type);
				type = type.substring(type.lastIndexOf('.')+1);
			}
			String name = fixName(el.attributeValue("name"));
			String column = el.attributeValue("column").toLowerCase();
			String attribute = name.substring(0,1).toLowerCase()+name.substring(1);
			if ("default".equals(attribute)) attribute = "defaultValue";
			idName = name; idType = type;
			pwa.println("	private "+type+" i"+name+";");
			pwp.println("	public static String PROP_"+column.toUpperCase()+" = \""+name.substring(0, 1).toLowerCase()+name.substring(1)+"\";");
			properties.add(name);
			pwb.println();
			pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
			pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
			hasProperty = true;
		}
		for (Iterator<Element> i = classEl.elementIterator("composite-id"); i.hasNext();) {
			Element cidEl = i.next();
			for (Iterator<Element> j = cidEl.elementIterator("key-many-to-one"); j.hasNext();) {
				Element el = j.next();
				String type = fixType(el.attributeValue("class"), pkg);
				if (type.indexOf('.')>=0) {
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
				String name = fixName(el.attributeValue("name"));
				// String column = el.attributeValue("column").toLowerCase();
				String attribute = name.substring(0,1).toLowerCase()+name.substring(1);
				if ("default".equals(attribute)) attribute = "defaultValue";
				pwa.println("	private "+type+" i"+name+";");
				properties.add(name);
				compositeId.add(new String[] {type, name});
				pwb.println();
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
				hasProperty = true;
			}
			for (Iterator<Element> j = cidEl.elementIterator("key-property"); j.hasNext();) {
				Element el = j.next();
				String type = fixType(el.attributeValue("type"), pkg);
				if (type.indexOf('.')>=0) {
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
				String name = fixName(el.attributeValue("name"));
				// boolean notNul = "true".equals(el.attributeValue("not-null"));
				// int length = Integer.parseInt(el.attributeValue("length","0"));
				// String column = el.attributeValue("column");
				String attribute = name.substring(0,1).toLowerCase()+name.substring(1);
				if ("default".equals(attribute)) attribute = "defaultValue";
				compositeId.add(new String[] {type, name});
				pwa.println("	private "+type+" i"+name+";");
				properties.add(name);
				pwb.println();
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
				hasProperty = true;
			}
		}
		for (Iterator<Element> i = classEl.elementIterator("property"); i.hasNext();) {
			Element el = i.next();
			String type = fixType(el.attributeValue("type"), pkg);
			if (type.indexOf('.')>=0) {
				imports.add(type);
				type = type.substring(type.lastIndexOf('.')+1);
			}
			String name = fixName(el.attributeValue("name"));
			// boolean notNul = "true".equals(el.attributeValue("not-null"));
			// int length = Integer.parseInt(el.attributeValue("length","0"));
			String column = el.attributeValue("column");
			String formula = el.attributeValue("formula");
			String attribute = name.substring(0,1).toLowerCase()+name.substring(1);
			if ("default".equals(attribute)) attribute = "defaultValue";
			if (column!=null) {
				pwa.println("	private "+type+" i"+name+";");
				properties.add(name);
				pwb.println();
				pwp.println("	public static String PROP_"+column.toUpperCase()+" = \""+name.substring(0, 1).toLowerCase()+name.substring(1)+"\";");
				if (type.equals("Boolean"))
					pwb.println("	public "+type+" is"+name+"() { return i"+name+"; }");
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
			} else if (formula!=null) {
				pwa.println("	private "+type+" i"+name+";");
				pwb.println();
				if (type.equals("Boolean"))
					pwb.println("	public "+type+" is"+name+"() { return i"+name+"; }");
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
			} else {
				System.err.println("Unknown "+el.getName()+": "+el.asXML());
			}
			hasProperty = true;
		}
		if (hasProperty) pwa.println();
		for (Iterator<Element> i = classEl.elementIterator("many-to-one"); i.hasNext();) {
			Element el = i.next();
			String type = fixType(el.attributeValue("class"), pkg);
			if (type.indexOf('.')>=0) {
				imports.add(type);
				type = type.substring(type.lastIndexOf('.')+1);
			}
			// boolean lazy = "true".equals(el.attributeValue("lazy","false"));
			// boolean eager = "false".equals(el.attributeValue("lazy","true"));
			String name = fixName(el.attributeValue("name"));
			// boolean notNul = "true".equals(el.attributeValue("not-null"));
			String column = el.attributeValue("column");
			String formula = el.attributeValue("formula");
			if (column!=null) {
				pwa.println("	private "+type+" i"+name+";");
				properties.add(name);
				pwb.println();
				manyToOnes.add(new String[] {type, name});
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+name.substring(0,1).toLowerCase()+name.substring(1)+") { i"+name+" = "+name.substring(0,1).toLowerCase()+name.substring(1)+"; }");
			} else if (formula!=null) {
				pwa.println("	private "+type+" i"+name+";");
				pwb.println();
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+name.substring(0,1).toLowerCase()+name.substring(1)+") { i"+name+" = "+name.substring(0,1).toLowerCase()+name.substring(1)+"; }");
			} else {
				System.err.println("Unknown "+el.getName()+": "+el.asXML());
			}
		}
		for (Iterator<Element> i = classEl.elementIterator("set"); i.hasNext();) {
			Element el = i.next();
			String type = null;
			String name = fixName(el.attributeValue("name"));
			// boolean lazy = "true".equals(el.attributeValue("lazy","false"));
			// boolean eager = "false".equals(el.attributeValue("lazy","true"));
			// String cascade = el.attributeValue("cascade");
			pwb.println();
			if (el.element("many-to-many")!=null) {
				// String column = el.element("key").attributeValue("column").toLowerCase();
				// String icolumn = el.element("many-to-many").attributeValue("column").toLowerCase();
				// String m2mtable = el.attributeValue("table").toLowerCase();
				type = fixType(el.element("many-to-many").attributeValue("class"), pkg);
				if (type.indexOf('.')>=0) {
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
			} else if (el.element("one-to-many")!=null) {
				// String column = el.element("key").attributeValue("column").toLowerCase();
				type = fixType(el.element("one-to-many").attributeValue("class"), pkg);
				if (type.indexOf('.')>=0) {
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
			} else if (el.element("element")!=null) {
				type = fixType(el.element("element").attributeValue("type"), pkg);
				if (type.indexOf('.')>=0) {
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
			} else {
				System.err.println("Unknown type of set");
			}
			if (type.indexOf('.')>=0) imports.add(type);
			imports.add("java.util.Set");
			imports.add("java.util.HashSet");
			pwa.println("	private Set<"+type+"> i"+name+";");
			pwb.println("	public Set<"+type+"> get"+name+"() { return i"+name+"; }");
			pwb.println("	public void set"+name+"(Set<"+type+"> "+name.substring(0,1).toLowerCase()+name.substring(1)+") { i"+name+" = "+name.substring(0,1).toLowerCase()+name.substring(1)+"; }");
			pwb.println("	public void addTo"+name.substring(0,1).toLowerCase()+name.substring(1)+"("+type+" "+type.substring(0, 1).toLowerCase()+type.substring(1)+") {");
			pwb.println("		if (i"+name+" == null) i"+name+" = new HashSet<"+type+">();");
			pwb.println("		i"+name+".add("+type.substring(0, 1).toLowerCase()+type.substring(1)+");");
			pwb.println("	}");
		}
		pwa.flush(); pwa.close();
		pwb.flush(); pwb.close();
		pwp.flush(); pwp.close();
		
		imports.add("java.io.Serializable");
		boolean abs = "true".equals(classEl.attributeValue("abstract","false"));
		ext = fixType(ext, pkg);
		if (ext != null && ext.indexOf('.')>=0) {
			imports.add(ext);
			ext = ext.substring(ext.lastIndexOf('.')+1);
		}
		if (idName != null || !compositeId.isEmpty())
			imports.add(fixType(classEl.attributeValue("name"), pkg));
		
		// Base class
		PrintWriter pw = new PrintWriter(new FileWriter(new File(fileFromPackage(outputFolder, pkg + ".base"), "Base" + className + ".java")));
		license(pw);
		pw.println("package "+pkg+".base;");
		pw.println();
		String last = null;
		for (String imp: imports) {
			String top = imp.substring(0, imp.indexOf('.'));
			if (last!=null && !last.equals(top)) pw.println();
			pw.println("import "+imp+";");
			last = top;
		}
		pw.println();
		pw.println("public abstract class Base"+className+(ext==null?"":" extends "+ext)+" implements Serializable {");
		pw.println("	private static final long serialVersionUID = 1L;");
		pw.println();
		pw.print(attributes.getBuffer());
		pw.println();
		pw.print(props.getBuffer());
		pw.println();
		pw.println("	public Base"+className+"() {");
		pw.println("		initialize();");
		pw.println("	}");
		if (idName != null) {
			String x = idName.substring(0,1).toLowerCase()+idName.substring(1);
			pw.println();
			pw.println("	public Base"+className+"("+idType+" "+x+") {");
			pw.println("		set"+idName+"(" + x + ");");
			pw.println("		initialize();");
			pw.println("	}");
		}
		pw.println();
		pw.println("	protected void initialize() {}");
		pw.print(body.getBuffer());
		iClassProperties.put(className, properties);
		if (ext!=null && iClassProperties.containsKey(ext)) {
			properties.addAll(iClassProperties.get(ext));
		}
		if (idName!=null) {
			if (idClass==null) idClass = className;
			pw.println();
			pw.println("	public boolean equals(Object o) {");
			pw.println("		if (o == null || !(o instanceof "+className+")) return false;");
			pw.println("		if (get"+idName+"() == null || (("+className+")o).get"+idName+"() == null) return false;");
			pw.println("		return get"+idName+"().equals((("+className+")o).get"+idName+"());");
			pw.println("	}");
			pw.println();
			pw.println("	public int hashCode() {");
			pw.println("		if (get"+idName+"() == null) return super.hashCode();");
			pw.println("		return get"+idName+"().hashCode();");
			pw.println("	}");
			pw.println();
			pw.println("	public String toString() {");
			if (properties.contains("Name"))
				pw.println("		return \""+className+"[\"+get"+idName+"()+\" \"+getName()+\"]\";");
			else if (properties.contains("Label"))
				pw.println("		return \""+className+"[\"+get"+idName+"()+\" \"+getLabel()+\"]\";");
			else
				pw.println("		return \""+className+"[\"+get"+idName+"()+\"]\";");
			pw.println("	}");
		} else if (!compositeId.isEmpty()) {
			String x = className.substring(0,1).toLowerCase()+className.substring(1);
			pw.println();
			pw.println("	public boolean equals(Object o) {");
			pw.println("		if (o == null || !(o instanceof "+className+")) return false;");
			pw.println("		"+className+" "+x+" = ("+className+")o;");
			for (String[] typeName: compositeId) {
				String name = typeName[1];
				pw.println("		if (get"+name+"() == null || "+x+".get"+name+"() == null || !get"+name+"().equals("+x+".get"+name+"())) return false;");
			}
			pw.println("		return true;");
			pw.println("	}");
			pw.println();
			pw.println("	public int hashCode() {");
			String xor = "", isNull = "";
			for (String[] typeName: compositeId) {
				String name = typeName[1];
				if (!xor.isEmpty()) { xor += " ^ "; isNull += " || "; }
				xor += "get"+name+"().hashCode()";
				isNull += "get"+name+"() == null";
			}
			pw.println("		if ("+isNull+") return super.hashCode();");
			pw.println("		return "+xor+";");
			pw.println("	}");
			pw.println();
			pw.println("	public String toString() {");
			String names = "";
			for (String[] typeName: compositeId) {
				String name = typeName[1];
				if (!names.isEmpty()) names += " + \", \" + ";
				names += "get"+name+"()";
			}
			pw.println("		return \""+className+"[\" + "+names+" + \"]\";");
			pw.println("	}");
		}
		pw.println();
		pw.println("	public String toDebugString() {");
		pw.println("		return \""+className+"[\" +");
		for (String p: properties)
			pw.println("			\"\\n	"+p+": \" + get"+p+"() +");
		pw.println("			\"]\";");
		pw.println("	}");
		pw.println("}");
		pw.flush(); pw.close();
		for (Iterator<Element> i=classEl.elementIterator("union-subclass");i.hasNext();) {
			importClass(i.next(), pkg, outputFolder, className, idClass, idName, idType);
		}
		for (Iterator<Element> i=classEl.elementIterator("subclass");i.hasNext();) {
			importClass(i.next(), pkg, outputFolder, className, idClass, idName, idType);
		}
		
		// Main class
		File mainFile = new File(fileFromPackage(outputFolder, pkg), className + ".java");
		if (!mainFile.exists()) {
			pw = new PrintWriter(new FileWriter(mainFile));
			license(pw);
			pw.println("package "+pkg+";");
			pw.println();
			pw.println("import "+pkg+".base.Base"+className+";");
			pw.println();
			pw.println("public"+(abs?" abstract":"")+" class "+className+" extends Base"+className+" {");
			pw.println();
			pw.println("	public " + className + "() {");
			pw.println("		super();");
			pw.println("	}");
			pw.println();
			pw.println("}");
			pw.flush(); pw.close();
		}
		
		// BASE DAO class
		pw = new PrintWriter(new FileWriter(new File(fileFromPackage(outputFolder, pkg + ".base"), "Base" + className + "DAO.java")));
		license(pw);
		pw.println("package "+pkg+".base;");
		pw.println();
		if (idType == null)
			pw.println("import java.io.Serializable;");
		if (!manyToOnes.isEmpty())
			pw.println("import java.util.List;");
		if (idType == null || !manyToOnes.isEmpty())
			pw.println();
		// pw.println("import org.hibernate.Hibernate;");
		// pw.println("import org.hibernate.criterion.Order;");
		// pw.println();
		pw.println("import "+pkg+"."+className+";");
		pw.println("import "+pkg+".dao._RootDAO;");
		pw.println("import "+pkg+".dao."+className+"DAO;");
		pw.println();
		pw.println("public abstract class Base"+className+"DAO"+" extends _RootDAO<"+className+","+(idType==null?"Serializable":idType)+"> {");
		pw.println();
		pw.println("	private static "+className+"DAO sInstance;");
		pw.println();
		pw.println("	public static "+className+"DAO getInstance() {");
		pw.println("		if (sInstance == null) sInstance = new "+className+"DAO();");
		pw.println("		return sInstance;");
		pw.println("	}");
		pw.println();
		pw.println("	public Class<"+className+"> getReferenceClass() {");
		pw.println("		return "+className+".class;");
		pw.println("	}");
		/*
		pw.println();
		pw.println("	public Order getDefaultOrder () {");
		pw.println("		return null;");
		pw.println("	}");
		String y = className.substring(0,1).toLowerCase()+className.substring(1);
		if (idName!=null) {
			String x = idName.substring(0,1).toLowerCase()+idName.substring(1);
			pw.println();
			pw.println("	public "+className+" get("+idType+" "+x+") {");
			pw.println("		return ("+className+") get(getReferenceClass(), "+x+");");
			pw.println("	}");
			pw.println();
			pw.println("	public "+className+" get("+idType+" "+x+", org.hibernate.Session hibSession) {");
			pw.println("		return ("+className+") get(getReferenceClass(), "+x+", hibSession);");
			pw.println("	}");
			pw.println();
			pw.println("	public "+className+" load("+idType+" "+x+") {");
			pw.println("		return ("+className+") load(getReferenceClass(), "+x+");");
			pw.println("	}");
			pw.println();
			pw.println("	public "+className+" load("+idType+" "+x+", org.hibernate.Session hibSession) {");
			pw.println("		return ("+className+") load(getReferenceClass(), "+x+", hibSession);");
			pw.println("	}");
			pw.println();
			pw.println("	public "+className+" loadInitialize("+idType+" "+x+", org.hibernate.Session hibSession) {");
			pw.println("		"+className+" "+y+" = load("+x+", hibSession);");
			pw.println("		if (!Hibernate.isInitialized("+y+")) Hibernate.initialize("+y+");");
			pw.println("		return "+y+";");
			pw.println("	}");
		} else {
			if (idClass==null) idClass = className;
			String x = "key";
			pw.println();
			pw.println("	public "+className+" get("+idClass+" "+x+") {");
			pw.println("		return ("+className+") get(getReferenceClass(), "+x+");");
			pw.println("	}");
			pw.println();
			pw.println("	public "+className+" get("+idClass+" "+x+", org.hibernate.Session hibSession) {");
			pw.println("		return ("+className+") get(getReferenceClass(), "+x+", hibSession);");
			pw.println("	}");
			pw.println();
			pw.println("	public "+className+" load("+idClass+" "+x+") {");
			pw.println("		return ("+className+") load(getReferenceClass(), "+x+");");
			pw.println("	}");
			pw.println();
			pw.println("	public "+className+" load("+idClass+" "+x+", org.hibernate.Session hibSession) {");
			pw.println("		return ("+className+") load(getReferenceClass(), "+x+", hibSession);");
			pw.println("	}");
			pw.println();
			pw.println("	public "+className+" loadInitialize("+idClass+" "+x+", org.hibernate.Session hibSession) {");
			pw.println("		"+className+" "+y+" = load("+x+", hibSession);");
			pw.println("		if (!Hibernate.isInitialized("+y+")) Hibernate.initialize("+y+");");
			pw.println("		return "+y+";");
			pw.println("	}");
		}
		pw.println();
		pw.println("	public void save("+className+" "+y+") {");
		pw.println("		save((Object) "+y+");");
		pw.println("	}");
		pw.println();
		pw.println("	public void save("+className+" "+y+", org.hibernate.Session hibSession) {");
		pw.println("		save((Object) "+y+", hibSession);");
		pw.println("	}");
		pw.println();
		pw.println("	public void saveOrUpdate("+className+" "+y+") {");
		pw.println("		saveOrUpdate((Object) "+y+");");
		pw.println("	}");
		pw.println();
		pw.println("	public void saveOrUpdate("+className+" "+y+", org.hibernate.Session hibSession) {");
		pw.println("		saveOrUpdate((Object) "+y+", hibSession);");
		pw.println("	}");
		pw.println();
		pw.println();
		pw.println("	public void update("+className+" "+y+") {");
		pw.println("		update((Object) "+y+");");
		pw.println("	}");
		pw.println();
		pw.println("	public void update("+className+" "+y+", org.hibernate.Session hibSession) {");
		pw.println("		update((Object) "+y+", hibSession);");
		pw.println("	}");
		pw.println();
		if (idName!=null) {
			if (idClass==null) idClass = className;
			String x = idName.substring(0,1).toLowerCase()+idName.substring(1);
			if (idType.equals("String")) {
				pw.println("	public void delete(Object "+x+") {");
				pw.println("		if ("+x+" instanceof String)");
				pw.println("			delete((Object) load((String)"+x+"));");
				pw.println("		else");
				pw.println("		super.delete("+x+");");
				pw.println("	}");
				pw.println();
				pw.println("	public void delete(Object "+x+", org.hibernate.Session hibSession) {");
				pw.println("		if ("+x+" instanceof String)");
				pw.println("			delete((Object) load((String)"+x+", hibSession), hibSession);");
				pw.println("		else");
				pw.println("			super.delete("+x+", hibSession);");
				pw.println("	}");
			} else {
				pw.println("	public void delete("+idType+" "+x+") {");
				pw.println("		delete(load("+x+"));");
				pw.println("	}");
				pw.println();
				pw.println("	public void delete("+idType+" "+x+", org.hibernate.Session hibSession) {");
				pw.println("		delete(load("+x+", hibSession), hibSession);");
				pw.println("	}");
			}
		}
		pw.println();
		pw.println("	public void delete("+className+" "+y+") {");
		pw.println("		delete((Object) "+y+");");
		pw.println("	}");
		pw.println();
		pw.println("	public void delete("+className+" "+y+", org.hibernate.Session hibSession) {");
		pw.println("		delete((Object) "+y+", hibSession);");
		pw.println("	}");
		pw.println();
		pw.println("	public void refresh("+className+" "+y+", org.hibernate.Session hibSession) {");
		pw.println("		refresh((Object) "+y+", hibSession);");
		pw.println("	}");
		if (!abs) {
			pw.println();
			pw.println("	@SuppressWarnings(\"unchecked\")");
			pw.println("	public List<"+className+"> findAll(org.hibernate.Session hibSession) {");
			pw.println("		return hibSession.createQuery(\"from "+className+"\").list();");
			pw.println("	}");
		}
		*/
		/*
		if (idType != null && idName != null) {
			String x = idName.substring(0,1).toLowerCase()+idName.substring(1);
			pw.println();
			pw.println("	public void delete("+idType+" "+x+") {");
			pw.println("		delete(load("+x+"));");
			pw.println("	}");
			pw.println();
			pw.println("	public void delete("+idType+" "+x+", org.hibernate.Session hibSession) {");
			pw.println("		delete(load("+x+", hibSession), hibSession);");
			pw.println("	}");
		}
		*/
		for (String[] attr: manyToOnes) {
			String type = attr[0];
			String name = attr[1];
			String x = name.substring(0,1).toLowerCase()+name.substring(1);
			String[] id = iIds.get(type);
			String iType = "Long";
			String iName = "UniqueId";
			if (id!=null) {
				iType = id[0];
				iName = id[1];
			}
			/*
			pw.println();
			pw.println("	public List<"+className+"> findBy"+name+"(org.hibernate.Session hibSession, "+type+" "+x+") {");
			pw.println("		return hibSession.createQuery(\"from "+className+" x where x."+x+"."+iName.substring(0,1).toLowerCase()+iName.substring(1)+" = :"+x+"Id\").set"+iType+"(\""+x+"Id\", "+x+".get"+iName+"()).list();");
			pw.println("	}");
			*/
			pw.println();
			pw.println("	@SuppressWarnings(\"unchecked\")");
			pw.println("	public List<"+className+"> findBy"+name+"(org.hibernate.Session hibSession, "+iType+" "+x+"Id) {");
			pw.println("		return hibSession.createQuery(\"from "+className+" x where x."+x+"."+iName.substring(0,1).toLowerCase()+iName.substring(1)+" = :"+x+"Id\").set"+iType+"(\""+x+"Id\", "+x+"Id).list();");
			pw.println("	}");
		}

		pw.println("}");
		pw.flush(); pw.close();
		
		// DAO class
		File daoFile = new File(fileFromPackage(outputFolder, pkg+".dao"), className + "DAO.java");
		if (!daoFile.exists()) {
			pw = new PrintWriter(new FileWriter(daoFile));
			license(pw);
			pw.println("package "+pkg+".dao;");
			pw.println();
			pw.println("import "+pkg+".base.Base"+className+"DAO;");
			pw.println();
			pw.println("public"+(abs?" abstract":"")+" class "+className+"DAO extends Base"+className+"DAO {");
			pw.println();
			pw.println("	public " + className + "DAO() {}");
			pw.println();
			pw.println("}");
			pw.flush(); pw.close();
		}
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

	public static void main(String[] args) {
		try {
			new CreateBaseModelFromXml().convert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
