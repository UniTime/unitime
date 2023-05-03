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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private Set<String> iAbstracts = new HashSet<String>();
	private Hashtable<String, String[]> iIds = new Hashtable<String, String[]>();
	private Hashtable<String, String> iRelations = new Hashtable<String, String>();
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
		info("Reading *.hbm.xml ...");
		for (String resource: workDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".hbm.xml");
			}
		})) {
			info("Pre-processing " + resource + " ...");
			Document resDoc = read(resource);
			Element resRoot = resDoc.getRootElement();
			String pkg = resRoot.attributeValue("package");
			for (Iterator<Element> j = resRoot.elementIterator("class");j.hasNext(); ) {
				Element classEl = j.next();
				preprocess(classEl, null, pkg);
			}
		}
		for (String resource: workDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".hbm.xml");
			}
		})) {
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
		if ("true".equals(classEl.attributeValue("abstract","false"))) {
			iAbstracts.add(className);
		}
		Element idEl = classEl.element("id");
		if (idEl!=null) {
			String type = fixType(idEl.attributeValue("type"), pkg);
			String name = fixName(idEl.attributeValue("name"));
			iIds.put(className, new String[] {type, name});
		}
		for (Iterator<Element> i=classEl.elementIterator("set");i.hasNext();) {
			Element setEl = i.next();
			if (setEl.element("many-to-many") != null) {
				String name = setEl.attributeValue("name");
				String column = setEl.element("key").attributeValue("column");
				String clazz = fixType(setEl.element("many-to-many").attributeValue("class"), pkg);
				clazz = clazz.substring(clazz.lastIndexOf('.')+1);
				info("  set: " + clazz + "." + column + ":" + name);
				iRelations.put(clazz + "." + column, name);
			}
		}
		for (Iterator<Element> i=classEl.elementIterator("many-to-one");i.hasNext();) {
			Element setEl = i.next();
			String name = setEl.attributeValue("name");
			String column = setEl.attributeValue("column");
			info("  many-to-one: " + className + "." + column + ":" + name);
			iRelations.put(className + "." + column, name);
		}
		for (Iterator<Element> i=classEl.elementIterator("composite-id");i.hasNext();) {
			Element cidEl = i.next();
			for (Iterator<Element> j=cidEl.elementIterator("key-many-to-one");j.hasNext();) {
				Element setEl = j.next();
				String name = setEl.attributeValue("name");
				String column = setEl.attributeValue("column");
				info("  many-to-one: " + className + "." + column + ":" + name);
				iRelations.put(className + "." + column, name);
			}
		}
		for (Iterator<Element> i=classEl.elementIterator("union-subclass");i.hasNext();) {
			preprocess(i.next(), className, pkg);
		}
		for (Iterator<Element> i=classEl.elementIterator("subclass");i.hasNext();) {
			preprocess(i.next(), className, pkg);
		}
	}	
	
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
		pw.println(" * Licensed to The Apereo Foundation under one or more contributor license");
		pw.println(" * agreements. See the NOTICE file distributed with this work for");
		pw.println(" * additional information regarding copyright ownership.");
		pw.println(" *");
		pw.println(" * The Apereo Foundation licenses this file to you under the Apache License,");
		pw.println(" * Version 2.0 (the \"License\"); you may not use this file except in");
		pw.println(" * compliance with the License. You may obtain a copy of the License at:");
		pw.println(" *");
		pw.println(" * http://www.apache.org/licenses/LICENSE-2.0");
		pw.println(" *");
		pw.println(" * Unless required by applicable law or agreed to in writing, software");
		pw.println(" * distributed under the License is distributed on an \"AS IS\" BASIS,");
		pw.println(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
		pw.println(" *");
		pw.println(" * See the License for the specific language governing permissions and");
		pw.println(" * limitations under the License.");
		pw.println(" * ");
		pw.println("*/");
	}
	
	private String annotation(String name, String... args) {
		String ret = "	@" + name;
		int i = 0;
		for (String arg: args) {
			if (arg == null || arg.isEmpty()) continue;
			ret += (i == 0 ? "(" : ", ");
			ret += arg;
			i++;
		}
		if (i > 0) ret += ")";
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void importClass(Element classEl, String pkg, File outputFolder, String ext, String idClass, String idName, String idType) throws IOException {
		String className = fixType(classEl.attributeValue("name"), pkg);
		String table = classEl.attributeValue("table");
		StringWriter attributes = new StringWriter();
		PrintWriter pwa = new PrintWriter(attributes);
		StringWriter body = new StringWriter();
		PrintWriter pwb = new PrintWriter(body);
		StringWriter header = new StringWriter();
		PrintWriter pwh = new PrintWriter(header);
		TreeSet<String> imports = new TreeSet<String>();
		StringWriter mainHeader = new StringWriter();
		PrintWriter pwmh = new PrintWriter(mainHeader);
		TreeSet<String> mainImports = new TreeSet<String>();

		if (className.indexOf('.') >= 0) {
			//imports.add(className);
			className = className.substring(className.lastIndexOf('.')+1);
		}
		info("  "+className+" ...");
		
		Vector<String[]> manyToOnes = new Vector<String[]>();
		TreeSet<String> properties = new TreeSet<String>();
		Vector<String[]> compositeId = new Vector<String[]>();
		
		imports.add("jakarta.persistence.MappedSuperclass");
		pwh.println("@MappedSuperclass");
		mainImports.add("jakarta.persistence.Entity");
		pwmh.println("@Entity");
		if (ext == null) {
			mainImports.add("org.hibernate.annotations.Cache");
			mainImports.add("org.hibernate.annotations.CacheConcurrencyStrategy");
			pwmh.println("@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)"); // , includeLazy = false
		}
		if (table != null) {
			mainImports.add("jakarta.persistence.Table");
			pwmh.println("@Table(name = \"" + table + "\")");	
		}
		
		Element discriminator = classEl.element("discriminator");
		if (discriminator != null) {
			String column = discriminator.attributeValue("column").toLowerCase();
			String type = fixType(discriminator.attributeValue("type"), pkg);
			if (type.indexOf('.')>=0) {
				imports.add(type);
				type = type.substring(type.lastIndexOf('.')+1);
			}
			mainImports.add("jakarta.persistence.Inheritance");
			mainImports.add("jakarta.persistence.InheritanceType");
			mainImports.add("jakarta.persistence.DiscriminatorColumn");
			mainImports.add("jakarta.persistence.DiscriminatorType");
			pwmh.println("@Inheritance(strategy = InheritanceType.SINGLE_TABLE)");
			pwmh.println("@DiscriminatorColumn(name=\"" + column + "\", discriminatorType = DiscriminatorType." + type.toUpperCase() + ")");
		} else {
			String discriminatorValue = classEl.attributeValue("discriminator-value");
			if (discriminatorValue != null) {
				mainImports.add("jakarta.persistence.DiscriminatorValue");
				pwmh.println("@DiscriminatorValue(\"" + discriminatorValue + "\")");
			}
		}
			
		boolean constructor = true;
		boolean hashCode = true;
		boolean equals = true;
		boolean serializable = true;
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
			String sequence = null;
			try {
				sequence = el.element("generator").element("param").getTextTrim();
			} catch (NullPointerException e) {
			}
			idName = name; idType = type;
			pwa.println("	private "+type+" i"+name+";");
			properties.add(name);
			pwb.println();
			imports.add("jakarta.persistence.Id");
			pwb.println("	@Id");
			String tableid = table + "_id";
			if ("Location".equals(className)) tableid = "room_id";
			if ("LocationPicture".equals(className)) tableid = "room_pict_id";
			if ("Preference".equals(className)) tableid = "pref_id";
			if ("PreferenceGroup".equals(className)) tableid = "pref_group_id";
			if ("RefTableEntry".equals(className)) tableid = "ref_table_id";
			if (sequence != null) {
				imports.add("org.hibernate.annotations.GenericGenerator");
				imports.add("org.hibernate.annotations.Parameter");
				imports.add("org.unitime.commons.hibernate.id.UniqueIdGenerator");
				pwb.println("	@GenericGenerator(name = \"" + tableid + "\", type = UniqueIdGenerator.class, parameters = {");
				pwb.println("		@Parameter(name = \"sequence\", value = \"" + sequence + "\")");
				pwb.println("	})");
				imports.add("jakarta.persistence.GeneratedValue");
				pwb.println("	@GeneratedValue(generator = \"" + tableid + "\")");
			}
			imports.add("jakarta.persistence.Column");
			pwb.println("	@Column(name=\"" + column + "\")");
			pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
			pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
			hasProperty = true;
		}
		for (Iterator<Element> i = classEl.elementIterator("composite-id"); i.hasNext();) {
			StringWriter id1 = new StringWriter();
			PrintWriter pwi1 = new PrintWriter(id1);
			StringWriter id2 = new StringWriter();
			PrintWriter pwi2 = new PrintWriter(id2);
			TreeSet<String> idImports = new TreeSet<String>();
			List<String[]> cargs = new ArrayList<String[]>();
			idImports.add("java.io.Serializable");
			
			imports.add("jakarta.persistence.IdClass");
			// ext = pkg + ".base." + className + "Id";
			pwh.println("@IdClass(" + className + "Id.class)");
			
			Element cidEl = i.next();
			for (Iterator<Element> j = cidEl.elementIterator("key-many-to-one"); j.hasNext();) {
				Element el = j.next();
				String type = fixType(el.attributeValue("class"), pkg);
				if (type.indexOf('.')>=0) {
					idImports.add(type);
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
				String name = fixName(el.attributeValue("name"));
				String column = el.attributeValue("column").toLowerCase();
				String attribute = name.substring(0,1).toLowerCase()+name.substring(1);
				
				boolean lazy = "true".equals(el.attributeValue("lazy")) || "proxy".equals(el.attributeValue("lazy"));
				boolean eager = "false".equals(el.attributeValue("lazy"));
				boolean join = "join".equals(el.attributeValue("fetch"));
				String cascade = el.attributeValue("cascade");
				boolean notNul = "true".equals(el.attributeValue("not-null", "true"));

				if (lazy || join || eager) idImports.add("jakarta.persistence.FetchType");
				if ("all".equals(cascade)) idImports.add("jakarta.persistence.CascadeType");
				else if ("save-update".equals(cascade)) idImports.add("jakarta.persistence.CascadeType");
				else if ("all-delete-orphan".equals(cascade)) idImports.add("jakarta.persistence.CascadeType");
				else if ("delete-orphan".equals(cascade)) {}
				else if (cascade != null && !"none".equals(cascade)) warn("Not-supported cascade type: " + cascade);
				
				if ("default".equals(attribute)) attribute = "defaultValue";
				cargs.add(new String[] { type, "i" + name, attribute });
				pwi1.println("	private "+type+" i"+name+";");
				pwa.println("	private "+type+" i"+name+";");
				properties.add(name);
				compositeId.add(new String[] {type, name});
				pwb.println();
				imports.add("jakarta.persistence.Id");
				pwb.println("	@Id");
				imports.add("jakarta.persistence.ManyToOne");
				pwb.println(annotation("ManyToOne",
						"optional = " +  (notNul ? "false" : "true"),
						(lazy ? "fetch = FetchType.LAZY" : join || eager ? "fetch = FetchType.EAGER" : null),
						("all".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
						("all-delete-orphan".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
						("save-update".equals(cascade) ? "cascade = {CascadeType.PERSIST, CascadeType.MERGE}" : null)
						));
				imports.add("jakarta.persistence.JoinColumn");
				pwb.println("	@JoinColumn(name = \"" + column + "\")");
				if (lazy || join || eager || el.element("cache") != null) {
					imports.add("org.hibernate.annotations.Cache");
					imports.add("org.hibernate.annotations.CacheConcurrencyStrategy");
					pwb.println("	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)"); // , includeLazy = false	
				}
				if ("all-delete-orphan".equals(cascade) || "delete-orphan".equals(cascade)) {
					imports.add("org.hibernate.annotations.Cascade");
					pwb.println("	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)");
				}
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
				pwi2.println();
				pwi2.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwi2.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
				hasProperty = true;
			}
			for (Iterator<Element> j = cidEl.elementIterator("key-property"); j.hasNext();) {
				Element el = j.next();
				String type = fixType(el.attributeValue("type"), pkg);
				if (type.indexOf('.')>=0) {
					idImports.add(type);
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
				String name = fixName(el.attributeValue("name"));
				String length = el.attributeValue("length");
				String column = el.attributeValue("column");
				String attribute = name.substring(0,1).toLowerCase()+name.substring(1);
				if ("default".equals(attribute)) attribute = "defaultValue";
				compositeId.add(new String[] {type, name});
				cargs.add(new String[] { type, "i" + name, attribute });
				pwi1.println("	private "+type+" i"+name+";");
				pwa.println("	private "+type+" i"+name+";");
				properties.add(name);
				pwb.println();
				imports.add("jakarta.persistence.Id");
				pwb.println("	@Id");
				imports.add("jakarta.persistence.Column");
				pwb.println("	@Column(name=\"" + column + "\"" + (length != null && !length.isEmpty() ? ", length = " + length : "") + ")");
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
				pwi2.println();
				pwi2.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwi2.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
				hasProperty = true;
			}
			
			pwi1.flush(); pwi1.close();
			pwi2.flush(); pwi2.close();
			idType = className + "Id";
			PrintWriter pwc = new PrintWriter(new FileWriter(new File(fileFromPackage(outputFolder, pkg + ".base"), className + "Id.java")));
			license(pwc);
			pwc.println("package "+pkg+".base;");
			pwc.println();
			String last = null;
			// idImports.add("jakarta.persistence.MappedSuperclass");
			for (String imp: idImports) {
				String top = imp.substring(0, imp.indexOf('.'));
				if (last!=null && !last.equals(top)) pwc.println();
				pwc.println("import "+imp+";");
				last = top;
			}
			// pwc.println("import "+pkg+"." + className + ";");
			pwc.println();
			pwc.println("/**");
			pwc.println(" * Do not change this class. It has been automatically generated using ant create-model.");
			pwc.println(" * @see org.unitime.commons.ant.CreateBaseModelFromXml");
			pwc.println(" */");
			// pwc.println("@MappedSuperclass");
			pwc.println("public class "+className+"Id implements Serializable {");
			pwc.println("	private static final long serialVersionUID = 1L;");
			pwc.println();
			pwc.print(id1.getBuffer());
			pwc.println();
			pwc.println("	public " + className + "Id() {}");
			pwc.println();
			pwc.print("	public " + className + "Id(");
			for (Iterator<String[]> j = cargs.iterator(); j.hasNext(); ) {
				String[] carg = j.next();
				pwc.print(carg[0] + " " + carg[2]);
				if (j.hasNext()) pwc.print(", ");
			}
			pwc.println(") {");
			for (Iterator<String[]> j = cargs.iterator(); j.hasNext(); ) {
				String[] carg = j.next();
				pwc.println("		" + carg[1] + " = " + carg[2] + ";");  
			}
			pwc.println("	}");
			pwc.print(id2.getBuffer());
			pwc.println();
			pwc.println();
			String x = className.substring(0,1).toLowerCase()+className.substring(1);
			pwc.println("	@Override");
			pwc.println("	public boolean equals(Object o) {");
			pwc.println("		if (o == null || !(o instanceof "+className+"Id)) return false;");
			pwc.println("		"+className+"Id "+x+" = ("+className+"Id)o;");
			for (String[] typeName: compositeId) {
				String name = typeName[1];
				pwc.println("		if (get"+name+"() == null || "+x+".get"+name+"() == null || !get"+name+"().equals("+x+".get"+name+"())) return false;");
			}
			pwc.println("		return true;");
			pwc.println("	}");
			pwc.println();
			pwc.println("	@Override");
			pwc.println("	public int hashCode() {");
			String xor = "", isNull = "";
			for (String[] typeName: compositeId) {
				String name = typeName[1];
				if (!xor.isEmpty()) { xor += " ^ "; isNull += " || "; }
				xor += "get"+name+"().hashCode()";
				isNull += "get"+name+"() == null";
			}
			pwc.println("		if ("+isNull+") return super.hashCode();");
			pwc.println("		return "+xor+";");
			pwc.println("	}");
			pwc.println();			
			pwc.println("}");
			pwc.flush(); pwc.close();
		}
		for (Iterator<Element> i = classEl.elementIterator("property"); i.hasNext();) {
			Element el = i.next();
			String type = fixType(el.attributeValue("type"), pkg);
			if (type.indexOf('.')>=0) {
				imports.add(type);
				type = type.substring(type.lastIndexOf('.')+1);
			}
			String name = fixName(el.attributeValue("name"));
			boolean notNul = "true".equals(el.attributeValue("not-null"));
			String length = el.attributeValue("length");
			String column = el.attributeValue("column");
			String formula = el.attributeValue("formula");
			String attribute = name.substring(0,1).toLowerCase()+name.substring(1);
			if ("default".equals(attribute)) attribute = "defaultValue";
			if (column!=null) {
				pwa.println("	private "+type+" i"+name+";");
				properties.add(name);
				pwb.println();
				imports.add("jakarta.persistence.Column");
				pwb.println("	@Column(name = \"" + column + "\", nullable = " + (notNul ? "false" : "true") + (length != null && !length.isEmpty() ? ", length = " + length : "") + ")");
				if (type.equals("Boolean")) {
					pwb.println("	public "+type+" is"+name+"() { return i"+name+"; }");
					imports.add("jakarta.persistence.Transient");
					pwb.println("	@Transient");
				}
				pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				pwb.println("	public void set"+name+"("+type+" "+attribute+") { i"+name+" = "+attribute+"; }");
			} else if (formula!=null) {
				pwa.println("	private "+type+" i"+name+";");
				pwb.println();
				imports.add("org.hibernate.annotations.Formula");
				pwb.println("	@Formula(\"" + formula + "\")");
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
			boolean lazy = "true".equals(el.attributeValue("lazy")) || "proxy".equals(el.attributeValue("lazy"));
			boolean eager = "false".equals(el.attributeValue("lazy"));
			boolean join = "join".equals(el.attributeValue("fetch"));
			String cascade = el.attributeValue("cascade");
			String name = fixName(el.attributeValue("name"));
			boolean notNul = "true".equals(el.attributeValue("not-null"));
			String column = el.attributeValue("column");
			String formula = el.attributeValue("formula");
			if (lazy || join || eager) imports.add("jakarta.persistence.FetchType");
			if ("all".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
			else if ("save-update".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
			else if ("all-delete-orphan".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
			else if ("delete-orphan".equals(cascade)) {}
			else if (cascade != null && !"none".equals(cascade)) warn("Not-supported cascade type: " + cascade);
			if (column!=null) {
				pwa.println("	private "+type+" i"+name+";");
				properties.add(name);
				pwb.println();
				manyToOnes.add(new String[] {type, name});
				imports.add("jakarta.persistence.ManyToOne");
				
				pwb.println(annotation("ManyToOne",
						"optional = " +  (notNul ? "false" : "true"),
						(lazy ? "fetch = FetchType.LAZY" : join || eager ? "fetch = FetchType.EAGER" : null),
						("all".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
						("all-delete-orphan".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
						("save-update".equals(cascade) ? "cascade = {CascadeType.PERSIST, CascadeType.MERGE}" : null)
						));
				imports.add("jakarta.persistence.JoinColumn");
				pwb.println("	@JoinColumn(name = \"" + column + "\", nullable = " + (notNul ? "false" : "true") + ")");
				if (lazy || eager || join || el.element("cache") != null) {
					imports.add("org.hibernate.annotations.Cache");
					imports.add("org.hibernate.annotations.CacheConcurrencyStrategy");
					pwb.println("	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)"); // , includeLazy = false	
				}
				if ("all-delete-orphan".equals(cascade) || "delete-orphan".equals(cascade)) {
					imports.add("org.hibernate.annotations.Cascade");
					pwb.println("	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)");
				}
				if (lazy && iAbstracts.contains(type)) {
					imports.add("org.hibernate.Hibernate");
					imports.add("org.hibernate.proxy.HibernateProxy");
					pwb.println("	public "+type+" get"+name+"() {");
					pwb.println("		if (i" + name + " != null && i" + name + " instanceof HibernateProxy)");
					pwb.println("			i" + name + " = (" + type + ") Hibernate.unproxy(i" + name + ");");
					pwb.println("		return i" + name + ";");
					pwb.println("	}");
				} else {
					pwb.println("	public "+type+" get"+name+"() { return i"+name+"; }");
				}
				pwb.println("	public void set"+name+"("+type+" "+name.substring(0,1).toLowerCase()+name.substring(1)+") { i"+name+" = "+name.substring(0,1).toLowerCase()+name.substring(1)+"; }");
			} else if (formula!=null) {
				pwa.println("	private "+type+" i"+name+";");
				pwb.println();
				imports.add("jakarta.persistence.ManyToOne");
				pwb.println(annotation("ManyToOne",
						(lazy ? "fetch = FetchType.LAZY" : join ? "fetch = FetchType.EAGER" : null),
						("all".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
						("all-delete-orphan".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
						("save-update".equals(cascade) ? "cascade = {CascadeType.PERSIST, CascadeType.MERGE}" : null)
						));
				imports.add("org.hibernate.annotations.JoinFormula");
				pwb.println("	@JoinFormula(\"" + formula + "\")");
				if (lazy || eager || join || el.element("cache") != null) {
					imports.add("org.hibernate.annotations.Cache");
					imports.add("org.hibernate.annotations.CacheConcurrencyStrategy");
					pwb.println("	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)"); // , includeLazy = false	
				}
				if ("all-delete-orphan".equals(cascade) || "delete-orphan".equals(cascade)) {
					imports.add("org.hibernate.annotations.Cascade");
					pwb.println("	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)");
				}
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
			boolean lazy = "true".equals(el.attributeValue("lazy")) || "proxy".equals(el.attributeValue("lazy"));
			boolean eager = "false".equals(el.attributeValue("lazy"));
			boolean inverse = "true".equals(el.attributeValue("inverse","false"));
			boolean notNul = "true".equals(el.attributeValue("not-null"));
			String cascade = el.attributeValue("cascade");
			pwb.println();
			if (el.element("many-to-many")!=null) {
				String column = null;
				try {
					column = el.element("key").attributeValue("column").toLowerCase();
				} catch (NullPointerException e) {}
				String icolumn = el.element("many-to-many").attributeValue("column").toLowerCase();
				String m2mtable = el.attributeValue("table").toLowerCase();
				type = fixType(el.element("many-to-many").attributeValue("class"), pkg);
				if (type.indexOf('.')>=0) {
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
				imports.add("jakarta.persistence.ManyToMany");
				if (lazy || eager) imports.add("jakarta.persistence.FetchType");
				if ("all".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
				else if ("save-update".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
				else if ("all-delete-orphan".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
				else if ("delete-orphan".equals(cascade)) {}
				else if (cascade != null && !"none".equals(cascade)) warn("Not-supported cascade type: " + cascade);
				if (inverse) {
					pwb.println(annotation("ManyToMany",
							(lazy ? "fetch = FetchType.LAZY" : eager ? "fetch = FetchType.EAGER" : null),
							"mappedBy = \"" + iRelations.get(className + "." + icolumn) + "\"",
							("all".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
							("all-delete-orphan".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
							("save-update".equals(cascade) ? "cascade = {CascadeType.PERSIST, CascadeType.MERGE}" : null)
							));
				} else {
					pwb.println(annotation("ManyToMany",
							(lazy ? "fetch = FetchType.LAZY" : eager ? "fetch = FetchType.EAGER" : null),
							("all".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
							("all-delete-orphan".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
							("save-update".equals(cascade) ? "cascade = {CascadeType.PERSIST, CascadeType.MERGE}" : null)
							));
					imports.add("jakarta.persistence.JoinTable");
					imports.add("jakarta.persistence.JoinColumn");
					pwb.println("	@JoinTable(name = \"" + m2mtable + "\",");
					if (column == null) {
						pwb.println("		joinColumns = {");
						for (Iterator<Element> j = el.element("key").elementIterator("column"); j.hasNext();) {
							Element colEl = j.next();
							String col = colEl.attributeValue("name").toLowerCase();
							pwb.println("			@JoinColumn(name = \"" + col + "\")" + (j.hasNext() ? "," : ""));
						}
						pwb.println("		},");
					} else {
						pwb.println("		joinColumns = { @JoinColumn(name = \"" + column + "\") },");
					}
					pwb.println("		inverseJoinColumns = { @JoinColumn(name = \"" + icolumn + "\") })");
				}
				if (lazy || eager || el.element("cache") != null) {
					imports.add("org.hibernate.annotations.Cache");
					imports.add("org.hibernate.annotations.CacheConcurrencyStrategy");
					pwb.println("	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)"); // , includeLazy = false
				}
				if ("all-delete-orphan".equals(cascade) || "delete-orphan".equals(cascade)) {
					imports.add("org.hibernate.annotations.Cascade");
					pwb.println("	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)");
				}
			} else if (el.element("one-to-many")!=null) {
				String column = el.element("key").attributeValue("column").toLowerCase();
				type = fixType(el.element("one-to-many").attributeValue("class"), pkg);
				if (type.indexOf('.')>=0) {
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
				imports.add("jakarta.persistence.OneToMany");
				if (lazy || eager) imports.add("jakarta.persistence.FetchType");
				if ("all".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
				else if ("save-update".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
				else if ("all-delete-orphan".equals(cascade)) imports.add("jakarta.persistence.CascadeType");
				else if ("delete-orphan".equals(cascade)) {}
				else if (cascade != null && !"none".equals(cascade)) warn("Not-supported cascade type: " + cascade);
				if (inverse) {
					pwb.println(annotation("OneToMany",
							(lazy ? "fetch = FetchType.LAZY" : eager ? "fetch = FetchType.EAGER" : null),
							"mappedBy = \"" + iRelations.get(type + "." + column) + "\"",
							("all".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
							("all-delete-orphan".equals(cascade) ? "cascade = {CascadeType.ALL}, orphanRemoval = true" : null),
							("save-update".equals(cascade) ? "cascade = {CascadeType.PERSIST, CascadeType.MERGE}" : null),
							("delete-orphan".equals(cascade) ? "orphanRemoval = true" : null)
							));
				} else {
					pwb.println(annotation("OneToMany",
							(lazy ? "fetch = FetchType.LAZY" : eager ? "fetch = FetchType.EAGER" : null),
							("all".equals(cascade) ? "cascade = {CascadeType.ALL}" : null),
							("all-delete-orphan".equals(cascade) ? "cascade = {CascadeType.ALL}, orphanRemoval = true" : null),
							("save-update".equals(cascade) ? "cascade = {CascadeType.PERSIST, CascadeType.MERGE}" : null),
							("delete-orphan".equals(cascade) ? "orphanRemoval = true" : null)
							));
					imports.add("jakarta.persistence.JoinColumn");
					if (column == null)  {
						imports.add("jakarta.persistence.JoinColumns");
						pwb.println("	@JoinColumns({");
						for (Iterator<Element> j = el.element("key").elementIterator("column"); j.hasNext();) {
							Element colEl = j.next();
							String col = colEl.attributeValue("name").toLowerCase();
							pwb.println("		@JoinColumn(name = \"" + col + "\")" + (j.hasNext() ? "," : ""));
						}
						pwb.println("	})");
					} else {
						pwb.println("	@JoinColumn(name = \"" + column + "\", nullable = " + (notNul ? "false" : "true") + ")");
					}
				}
				if (lazy || eager || el.element("cache") != null) {
					imports.add("org.hibernate.annotations.Cache");
					imports.add("org.hibernate.annotations.CacheConcurrencyStrategy");
					pwb.println("	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)"); // , includeLazy = false
				}
			} else if (el.element("element")!=null) {
				String m2mtable = el.attributeValue("table").toLowerCase();
				String keyColumn = el.element("key").attributeValue("column").toLowerCase();
				String elColumn = el.element("element").attributeValue("column").toLowerCase();
				type = fixType(el.element("element").attributeValue("type"), pkg);
				if (type.indexOf('.')>=0) {
					imports.add(type);
					type = type.substring(type.lastIndexOf('.')+1);
				}
				imports.add("jakarta.persistence.ElementCollection");
				pwb.println("	@ElementCollection");
				imports.add("jakarta.persistence.CollectionTable");
				imports.add("jakarta.persistence.JoinColumn");
				pwb.println("	@CollectionTable(name = \"" + m2mtable + "\",");
				pwb.println("		joinColumns = @JoinColumn(name = \"" + keyColumn + "\")");
				pwb.println("	)");
				imports.add("jakarta.persistence.Column");
				pwb.println("	@Column(name = \"" + elColumn + "\")");
			} else {
				System.err.println("Unknown type of set");
			}
			if (type.indexOf('.')>=0) imports.add(type);
			imports.add("java.util.Set");
			imports.add("java.util.HashSet");
			pwa.println("	private Set<"+type+"> i"+name+";");
			pwb.println("	public Set<"+type+"> get"+name+"() { return i"+name+"; }");
			pwb.println("	public void set"+name+"(Set<"+type+"> "+name.substring(0,1).toLowerCase()+name.substring(1)+") { i"+name+" = "+name.substring(0,1).toLowerCase()+name.substring(1)+"; }");
			pwb.println("	public void addTo"+name+"("+type+" "+type.substring(0, 1).toLowerCase()+type.substring(1)+") {");
			pwb.println("		if (i"+name+" == null) i"+name+" = new HashSet<"+type+">();");
			pwb.println("		i"+name+".add("+type.substring(0, 1).toLowerCase()+type.substring(1)+");");
			pwb.println("	}");
			pwb.println("	@Deprecated");
			pwb.println("	public void addTo"+name.substring(0,1).toLowerCase()+name.substring(1)+"("+type+" "+type.substring(0, 1).toLowerCase()+type.substring(1)+") {");
			pwb.println("		addTo"+name+"("+type.substring(0, 1).toLowerCase()+type.substring(1)+");");
			pwb.println("	}");
			
		}
		
		if (serializable)
			imports.add("java.io.Serializable");
		boolean abs = "true".equals(classEl.attributeValue("abstract","false"));
		ext = fixType(ext, pkg);
		if (ext != null && ext.indexOf('.')>=0) {
			if (!ext.startsWith(pkg + ".base.")) imports.add(ext);
			ext = ext.substring(ext.lastIndexOf('.')+1);
		}
		if (idName != null || !compositeId.isEmpty())
			imports.add(fixType(classEl.attributeValue("name"), pkg));
		if (abs && discriminator == null && "class".equals(classEl.getName())) {
			mainImports.add("jakarta.persistence.Inheritance");
			mainImports.add("jakarta.persistence.InheritanceType");
			pwmh.println("@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)");
		}
		
		pwa.flush(); pwa.close();
		pwb.flush(); pwb.close();
		pwh.flush(); pwh.close();
		pwmh.flush(); pwmh.close();

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
		pw.println("/**");
		pw.println(" * Do not change this class. It has been automatically generated using ant create-model.");
		pw.println(" * @see org.unitime.commons.ant.CreateBaseModelFromXml");
		pw.println(" */");
		pw.print(header.getBuffer());
		pw.println("public abstract class Base"+className+(ext==null?"":" extends "+ext)+(serializable ? " implements Serializable":"") + " {");
		pw.println("	private static final long serialVersionUID = 1L;");
		pw.println();
		pw.print(attributes.getBuffer());
		if (constructor) {
			pw.println();
			pw.println("	public Base"+className+"() {");
			pw.println("	}");
		}
		if (idName != null) {
			String x = idName.substring(0,1).toLowerCase()+idName.substring(1);
			pw.println();
			pw.println("	public Base"+className+"("+idType+" "+x+") {");
			pw.println("		set"+idName+"(" + x + ");");
			pw.println("	}");
		}
		pw.println();
		pw.print(body.getBuffer());
		iClassProperties.put(className, properties);
		if (ext!=null && iClassProperties.containsKey(ext)) {
			properties.addAll(iClassProperties.get(ext));
		}
		if (idName!=null) {
			if (idClass==null) idClass = className;
			if (equals) {
				pw.println();
				pw.println("	@Override");
				pw.println("	public boolean equals(Object o) {");
				pw.println("		if (o == null || !(o instanceof "+className+")) return false;");
				pw.println("		if (get"+idName+"() == null || (("+className+")o).get"+idName+"() == null) return false;");
				pw.println("		return get"+idName+"().equals((("+className+")o).get"+idName+"());");
				pw.println("	}");
			}
			if (hashCode) {
				pw.println();
				pw.println("	@Override");
				pw.println("	public int hashCode() {");
				pw.println("		if (get"+idName+"() == null) return super.hashCode();");
				pw.println("		return get"+idName+"().hashCode();");
				pw.println("	}");
			}
			pw.println();
			pw.println("	@Override");
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
			pw.println("	@Override");
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
			pw.println("	@Override");
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
			last = null;
			for (String imp: mainImports) {
				String top = imp.substring(0, imp.indexOf('.'));
				if (last!=null && !last.equals(top)) pw.println();
				pw.println("import "+imp+";");
				last = top;
			}
			pw.println("import "+pkg+".base.Base"+className+";");
			pw.println();
			pw.print(mainHeader.getBuffer());
			pw.println("public"+(abs?" abstract":"")+" class "+className+" extends Base"+className+" {");
			pw.println();
			pw.println("}");
			pw.flush(); pw.close();
		} else {
			List<String> file = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(mainFile));
			String readLine = null;
			Pattern importPattern = Pattern.compile("import ([^;]*);");
			
			Set<String> existingImports = new HashSet<String>();
			boolean needTransient = false;
			while ((readLine = br.readLine()) != null) {
				Matcher importMatcher = importPattern.matcher(readLine);
				if (importMatcher.matches()) {
					String imp = importMatcher.group(1);
					existingImports.add(imp);
				}
				if (readLine.matches("[\t ]*(public|private|protected)[\t ]*[a-zA-Z<>\\., _\\[\\]\\?]+[\t ]+(get|is)[A-Za-z0-9_]+\\(\\).*")) {
					mainImports.add("jakarta.persistence.Transient");
					needTransient = true;
				}
				file.add(readLine);
			}
			br.close();
			
			pw = new PrintWriter(new FileWriter(mainFile));
			boolean printedImports = false;
			boolean classLine = false;
			String prev = null;
			for (String line: file) {
				if (!printedImports && importPattern.matcher(line).matches()) {
					boolean printedAtLeastOne = false;
					for (String imp: mainImports) {
						if (!existingImports.contains(imp)) {
							pw.println("import "+imp+";");
							printedAtLeastOne = true;
						}
					}
					if (printedAtLeastOne) pw.println();
					printedImports = true;
				}
				Matcher importMatcher = importPattern.matcher(line);
				if (importMatcher.matches()) {
					String imp = importMatcher.group(1);
					if ("jakarta.persistence.Transient".equals(imp) && !needTransient) continue;
				}
				if (!classLine && line.startsWith("@Entity")) continue;
				if (!classLine && line.startsWith("@Cache")) continue;
				if (!classLine && line.startsWith("@Table")) continue;
				if (!classLine && line.startsWith("@Inheritance")) continue;
				if (!classLine && line.startsWith("@Discriminator")) continue;
				if (line.equals("	@Transient")) continue;
				if (!classLine && (line.startsWith("public class ") || line.startsWith("public abstract class "))) {
					pw.print(mainHeader.toString());
					classLine = true;
				}
				if (line.matches("[\t ]*(public|private|protected)[\t ]*[a-zA-Z<>\\., _\\[\\]\\?]+[\t ]+(get|is)[A-Za-z0-9_]+\\(\\).*") && !"	@Transient".equals(prev)) {
					pw.println("	@Transient");
				}
				pw.println(line);
				prev = line;
			}
			pw.flush(); pw.close();
		}
		
		// BASE DAO class
		File f = new File(fileFromPackage(outputFolder, pkg + ".base"), "Base" + className + "DAO.java");
		if (f.exists()) f.delete();
		
		// DAO class
		File daoFile = new File(fileFromPackage(outputFolder, pkg+".dao"), className + "DAO.java");
		//if (!daoFile.exists()) {
		pw = new PrintWriter(new FileWriter(daoFile));
		license(pw);
		pw.println("package "+pkg+".dao;");
		pw.println();
		pw.println("/**");
		pw.println(" * Do not change this class. It has been automatically generated using ant create-model.");
		pw.println(" * @see org.unitime.commons.ant.CreateBaseModelFromXml");
		pw.println(" */");
		if (idType == null)
			pw.println("import java.io.Serializable;");
		else if (idType.endsWith("Id"))
			pw.println("import " + pkg + ".base." + idType + ";");
		if (!manyToOnes.isEmpty())
			pw.println("import java.util.List;");
		pw.println("import "+pkg+"."+className+";");
		pw.println();
		pw.println("public class "+className+"DAO extends _RootDAO<"+className+","+(idType==null?"Serializable":idType)+"> {");
		pw.println("	private static "+className+"DAO sInstance;");
		pw.println();
		pw.println("	public " + className + "DAO() {}");
		pw.println();
		pw.println("	public static "+className+"DAO getInstance() {");
		pw.println("		if (sInstance == null) sInstance = new "+className+"DAO();");
		pw.println("		return sInstance;");
		pw.println("	}");
		pw.println();
		pw.println("	public Class<"+className+"> getReferenceClass() {");
		pw.println("		return "+className+".class;");
		pw.println("	}");
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
			pw.println();
			pw.println("	@SuppressWarnings(\"unchecked\")");
			pw.println("	public List<"+className+"> findBy"+name+"(org.hibernate.Session hibSession, "+iType+" "+x+"Id) {");
			pw.println("		return hibSession.createQuery(\"from "+className+" x where x."+x+"."+iName.substring(0,1).toLowerCase()+iName.substring(1)+" = :"+x+"Id\", " + className + ".class).setParameter(\""+x+"Id\", "+x+"Id).list();");
			pw.println("	}");
		}
		pw.println("}");
		pw.flush(); pw.close();
		//}
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
