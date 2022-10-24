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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.unitime.localization.messages.ConstantsMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.localization.messages.PageNames;
import org.unitime.localization.messages.PointInTimeDataReports;
import org.unitime.localization.messages.SecurityMessages;
import org.unitime.timetable.gwt.resources.CPSolverMessages;
import org.unitime.timetable.gwt.resources.Constants;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.Messages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;

/**
 * @author Tomas Muller
 */
public class POHelper extends ArrayList<POHelper.Block> {
	private static final long serialVersionUID = -5644853313303107356L;
	private String iLocale;
	private Map<String, String> iPageNames;
	
	static public enum Bundle {
		Constants(ConstantsMessages.class, "Constants"),
		Course(CourseMessages.class, "Course"),
		Exams(ExaminationMessages.class, "Exams"),
		Pages(PageNames.class, "Pages"),
		PIT(PointInTimeDataReports.class, "PIT"),
		Security(SecurityMessages.class, "Security"),
		GWTConst(GwtConstants.class, "GWT-Const"),
		GWT(GwtMessages.class, "GWT"),
		Aria(GwtAriaMessages.class, "Aria"),
		StudentConst(StudentSectioningConstants.class, "Student-Const"),
		Students(StudentSectioningMessages.class, "Students"),
		Solver(CPSolverMessages.class, "Solver"),
		;
		private Class iClass;
		private String iPrefix;
		Bundle(Class clazz, String prefix) {
			iClass = clazz;
			iPrefix = prefix;
		}
		public Class getClazz() { return iClass; }
		public String getPrefix() { return iPrefix; }
	}
	
	public POHelper(String locale, Map<String, String> pageNames) {
		super();
		iLocale = locale;
		iPageNames = pageNames;
	}

	public void readPOFile(Bundle bundle, Reader reader) throws Exception {
		BufferedReader in = new BufferedReader(reader);
		List<String> lines = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			if (line.isEmpty()) {
				if (!lines.isEmpty()) {
					Block b = new Block(lines);
					if (bundle != null) b.setPrefix(bundle.getPrefix());
					if (!b.isEmpty()) add(b);
					lines.clear();
				}
			} else {
				lines.add(line);
			}
		}
		if (!lines.isEmpty()) {
			Block b = new Block(lines);
			if (bundle != null) b.setPrefix(bundle.getPrefix());
			if (!b.isEmpty()) add(b);
		}
		in.close();
	}
	
	public void readProperties(Bundle bundle, File sources, String locale) throws Exception {
		Properties properties = new Properties();
		File f = new File(sources, bundle.getClazz().getName().replace('.', '/') + "_" + locale + ".properties");
		if (f.exists())
			properties.load(new FileReader(f));
		
		Properties enUK = new Properties();
		f = new File(sources, bundle.getClazz().getName().replace('.', '/') + "_en_UK.properties");
		if (f.exists())
			enUK.load(new FileReader(f));
		
		if (PageNames.class.equals(bundle.getClazz())) {
			for (String prop: new TreeSet<String>(iPageNames.keySet())) {
				String name = iPageNames.get(prop);
				String text = properties.getProperty(prop);
				if ("en".equals(locale) && text == null)
					text = name;
				add(new Block(bundle.getPrefix() + ":" + prop, name, text));
			}
		}
		
		for (Method method: bundle.getClazz().getMethods()) {
			String text = properties.getProperty(method.getName());
			String altValue = enUK.getProperty(method.getName());
			String value = null;
			Messages.DefaultMessage dm = method.getAnnotation(Messages.DefaultMessage.class);
			if (dm != null)
				value = dm.value();
			Constants.DefaultBooleanValue db = method.getAnnotation(Constants.DefaultBooleanValue.class);
			if (db != null)
				value = (db.value() ? "true" : "false");
			Constants.DefaultDoubleValue dd = method.getAnnotation(Constants.DefaultDoubleValue.class);
			if (dd != null)
				value = String.valueOf(dd.value());
			Constants.DefaultFloatValue df = method.getAnnotation(Constants.DefaultFloatValue.class);
			if (df != null)
				value = String.valueOf(df.value());
			Constants.DefaultIntValue di = method.getAnnotation(Constants.DefaultIntValue.class);
			if (di != null)
				value = String.valueOf(di.value());					
			Constants.DefaultStringValue ds = method.getAnnotation(Constants.DefaultStringValue.class);
			if (ds != null)
				value = ds.value();
			Constants.DefaultStringArrayValue dsa = method.getAnnotation(Constants.DefaultStringArrayValue.class);
			if (dsa != null)
				value = array2string(dsa.value());
			Constants.DefaultStringMapValue dsm = method.getAnnotation(Constants.DefaultStringMapValue.class);
			if (dsm != null)
				value = array2string(dsm.value());
			
			if ("translateMessage".equals(method.getName())) continue;

			boolean doNotTranslate = (method.getAnnotation(Messages.DoNotTranslate.class) != null) || (method.getAnnotation(Constants.DoNotTranslate.class) != null);
			if (doNotTranslate) {
				if (text != null && !text.equals(value))
					System.err.println("       [" + locale + "] @DoNotTranslate " +  bundle.getPrefix() + ":" + method.getName());
				continue;
			}
			
			if ("en".equals(locale) && text == null)
				text = value;
			else if ((bundle == Bundle.GWTConst || bundle == Bundle.StudentConst) && text == null && altValue != null && !locale.equals("ar"))
				text = altValue;

			Block b = new Block(bundle.getPrefix() + ":" + method.getName(), value, text);
			// if (doNotTranslate) b.dnt = true;

			add(b);
		}
	}
	
	public void readPropertiesFile(Bundle bundle, File file) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(file));
		List<String> lines = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			if (line.isEmpty()) {
				if (!lines.isEmpty()) {
					Block b = new Block(lines);
					if (bundle != null) b.setPrefix(bundle.getPrefix());
					if (!b.isEmpty()) add(b);
					lines.clear();
				}
			} else {
				lines.add(line);
			}
		}
		if (!lines.isEmpty()) {
			Block b = new Block(lines);
			if (bundle != null) b.setPrefix(bundle.getPrefix());
			if (!b.isEmpty()) add(b);
		}
		
		in.close();
	}
	
	public boolean has(Bundle bundle, String method) {
		return contains(new Block(bundle.getPrefix() + ":" + method, null, null));
	}
	
	public String translate(Bundle bundle, String method) {
		String msgctxt = bundle.getPrefix() + ":" + method;
		for (Block b: this) {
			if (msgctxt.equals(b.msgctxt))
				return b.msgstr;
		}
		return null;
	}
	
	public List<Block> getBlocks(Bundle bundle) {
		ArrayList<Block> ret = new ArrayList<Block>();
		for (Block b: this) {
			if (bundle.getPrefix().equals(b.getPrefix()))
				ret.add(b);
		}
		Collections.sort(ret);
		return ret;
	}
	
	public void writePropertiesFile(File sources, Bundle bundle) throws Exception {
		Class clazz = bundle.getClazz();
		File folder = new File(sources, clazz.getName().substring(0, clazz.getName().lastIndexOf('.')).replace('.', File.separatorChar));

		File output = new File(folder, clazz.getSimpleName() + "_" + iLocale + ".properties");

		Properties old = new Properties();
		if (output.exists()) old.load(new FileReader(output));
		
		List<Block> blocks = new ArrayList<POHelper.Block>();
		for (Method method: clazz.getMethods()) {
			String text = old.getProperty(method.getName());
			String value = null;
			Messages.DefaultMessage dm = method.getAnnotation(Messages.DefaultMessage.class);
			if (dm != null)
				value = dm.value();
			Constants.DefaultBooleanValue db = method.getAnnotation(Constants.DefaultBooleanValue.class);
			if (db != null)
				value = (db.value() ? "true" : "false");
			Constants.DefaultDoubleValue dd = method.getAnnotation(Constants.DefaultDoubleValue.class);
			if (dd != null)
				value = String.valueOf(dd.value());
			Constants.DefaultFloatValue df = method.getAnnotation(Constants.DefaultFloatValue.class);
			if (df != null)
				value = String.valueOf(df.value());
			Constants.DefaultIntValue di = method.getAnnotation(Constants.DefaultIntValue.class);
			if (di != null)
				value = String.valueOf(di.value());					
			Constants.DefaultStringValue ds = method.getAnnotation(Constants.DefaultStringValue.class);
			if (ds != null)
				value = ds.value();
			Constants.DefaultStringArrayValue dsa = method.getAnnotation(Constants.DefaultStringArrayValue.class);
			if (dsa != null)
				value = array2string(dsa.value());
			Constants.DefaultStringMapValue dsm = method.getAnnotation(Constants.DefaultStringMapValue.class);
			if (dsm != null)
				value = array2string(dsm.value());
			
			if ("translateMessage".equals(method.getName())) continue;
			
			String translated = translate(bundle, method.getName());
			boolean doNotTranslate = (method.getAnnotation(Messages.DoNotTranslate.class) != null) || (method.getAnnotation(Constants.DoNotTranslate.class) != null);
			if (doNotTranslate && translated == null && text != null)
				translated = text;
			
			if (text == null && translated == null) continue;
			blocks.add(new Block(bundle.getPrefix() + ":" + method.getName(), value, translated));
		}
		
		if (PageNames.class.equals(clazz)) {
			for (String prop: new TreeSet<String>(iPageNames.keySet())) {
				String name = iPageNames.get(prop);
				String text = old.getProperty(prop);
				String translated = translate(bundle, prop);
				if (text != null || translated != null)
					blocks.add(new Block(bundle.getPrefix() + ":" + prop, name, translated));
			}
		}
		Collections.sort(blocks);
		
		PrintStream out = new PrintStream(output);
		
		out.println("# Licensed to The Apereo Foundation under one or more contributor license");
		out.println("# agreements. See the NOTICE file distributed with this work for");
		out.println("# additional information regarding copyright ownership.");
		out.println("#");
		out.println("# The Apereo Foundation licenses this file to you under the Apache License,");
		out.println("# Version 2.0 (the \"License\"); you may not use this file except in");
		out.println("# compliance with the License. You may obtain a copy of the License at:");
		out.println("#");
		out.println("# http://www.apache.org/licenses/LICENSE-2.0");
		out.println("#");
		out.println("# Unless required by applicable law or agreed to in writing, software");
		out.println("# distributed under the License is distributed on an \"AS IS\" BASIS,");
		out.println("# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
		out.println("#");
		out.println("# See the License for the specific language governing permissions and");
		out.println("# limitations under the License.");
		out.println("#");			
		
		boolean empty = true;
		for (Block b: blocks) {
			if (b.getTranslation() == null && !old.containsKey(b.getMethod()))
				continue;
			out.println();
			if (b.getDefaultText() != null)
				out.println("# Default: " + unicodeEscape(b.getDefaultText(), false, false).trim());
			if (b.getTranslation() == null) {
				if (b.getDefaultText() != null)
					out.println("# FIXME: Translate \"" + unicodeEscape(b.getDefaultText(), false, true) + "\"");
				else
					out.println("# FIXME: Translate " + b.getMethod());
				out.println("# " + b.getMethod() + "=");
			} else {
				out.println(b.getMethod() + "=" + unicodeEscape(b.getTranslation(), true, true));
				empty = false;
			}	
		}
		if (empty) output.delete();
		
		out.flush();
		out.close();
	}
	
	
	
	public void writePOFile(File file) throws IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
		
		out.println("msgid \"\"");
		out.println("msgstr \"\"");
		out.println("\"MIME-Version: 1.0\\n\"");
		out.println("\"Content-Transfer-Encoding: 8bit\\n\"");
		out.println("\"Content-Type: text/plain; charset=UTF-8\\n\"");
		out.println("\"PO-Revision-Date: " + new Date() + "\\n\"");
		out.println("\"Language: " + iLocale + "\\n\"");
		out.println("\"X-Generator: UniTime " + org.unitime.timetable.util.Constants.VERSION + "\\n\"");
		
		for (Block block: this) {
			block.print(out);
		}
		
		out.flush(); out.close();		
	}
	
	public static class Block implements Comparable<Block> {
		String msgctxt, msgid, msgstr;
		boolean dnt = false;
		
		public Block(String msgctxt, String msgid, String msgstr) {
			this.msgctxt = msgctxt;
			this.msgid = msgid;
			this.msgstr = msgstr;
		}
		
		public Block(List<String> lines) {
			int tag = -1;
			for (String line: lines) {
				if (line.startsWith("#")) continue;
				if (line.startsWith("msgctxt ")) {
					line = line.substring("msgctxt ".length());
					tag = 0;
				} else if (line.startsWith("msgid ")) {
					line = line.substring("msgid ".length());
					tag = 1;
				} else if (line.startsWith("msgstr ")) {
					line = line.substring("msgstr ".length());
					tag = 2;
				}
				if (line.startsWith("\"") && line.endsWith("\"") && tag >= 0) {
					line = line.substring(1, line.length() - 1);
					//line = line.replaceAll("\\\\+", "\\\\");
					line = line.replace("\\\\", "\\");
					//line = line.replace("\\\\", "\\");
					line = line.replace("\\ ", " ");
					line = line.replace("\\\\\"", "\\\"");
					switch (tag) {
					case 0:
						if (msgctxt == null)
							msgctxt = line;
						else
							msgctxt += line;
						break;
					case 1:
						if (msgid == null)
							msgid = line;
						else
							msgid += line;
						break;
					case 2:
						if (msgstr == null)
							msgstr = line;
						else
							msgstr += line;
						break;
					}
				}
			}
			if (!isEmpty() && !"".equals(msgid) && "".equals(msgstr))
				msgstr = null;
		}
		
		public boolean isEmpty() {
			return msgctxt == null || msgctxt.indexOf(':') < 0;
		}
		
		public boolean hasMessage() {
			if (isEmpty()) return false;
			if (msgstr != null && !msgstr.isEmpty()) return true;
			if (msgid == null || msgid.isEmpty()) return true; // both are empty -> ok
			return false;
		}
		
		public String getPrefix() {
			return msgctxt.substring(0, msgctxt.indexOf(':'));
		}
		
		public String getMethod() {
			return msgctxt.substring(msgctxt.indexOf(':') + 1);
		}
		
		public void setPrefix(String prefix) {
			if (msgctxt == null || msgctxt.isEmpty()) return;
			if (msgctxt.indexOf(':') < 0)
				msgctxt = prefix + ":" + msgctxt;
			else
				msgctxt = prefix + ":" + getMethod();
		}
		
		public String getDefaultText() {
			return msgid;
		}
		
		public String getTranslation() {
			return msgstr;
		}

		@Override
		public int compareTo(Block b) {
			return getMethod().compareTo(b.getMethod());
		}
		
		@Override
		public String toString() {
			return getMethod() + "=" + getTranslation();
		}
		
		@Override
		public int hashCode() {
			return msgctxt.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Block)) return false;
			return msgctxt.equals(((Block)o).msgctxt);
		}
		
		public void print(PrintWriter out) {
			out.println();
			if (dnt)
				out.println("# \"Do Not Translate\"");
			out.println("msgctxt \"" + msgctxt + "\"");
			POHelper.print(out, "msgid", msgid);
			POHelper.print(out, "msgstr", msgstr);
		}
	}
	
	private static String array2string(String[] value) {
		String ret = "";
		for (String s: value) {
			if (!ret.isEmpty()) ret += ",";
			ret += s.replace(",", "\\,");
		}
		return ret;
	}
	
	private static final char[] hexChar = {
			'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
	};
	
	private static String unicodeEscape(String s, boolean includeColon, boolean spaces) {
    	StringBuilder sb = new StringBuilder();
    	boolean allspace = true;
    	for (int i = 0; i < s.length(); i++) {
    		char c = s.charAt(i);
    		if (spaces && c == ' ' && allspace) {
    			sb.append("\\");
    		} else {
    			allspace = false;
    		}
    		if (spaces && c == ' ' && s.substring(i).trim().isEmpty()) {
    			sb.append("\\");
    		}
    	    if ((c >> 7) > 0) {
        		sb.append("\\u");
        		sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
        		sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
        		sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
        		sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
    	    } else if (c == ':' && includeColon) {
    	    	sb.append("\\:");
    	    } else if (c == '\n') {
    	    	sb.append("\\n");
    	    } else if (c == '\\' && i+1 < s.length() && s.charAt(1 + i) == ',' && includeColon) {
    	    	sb.append("\\\\");
    	    } else {
    	    	sb.append(c);
    	    }
    	}
    	return sb.toString();
    }
	
	private static void print(PrintWriter out, String tag, String text) {
		if (text == null) return;
		if (text.indexOf('\n') >= 0) {
			String[] t = text.split("\n");
			for (int i = 0; i < t.length; i++) {
				out.println((i == 0 ? tag + " " : "") + "\"" + t[i].replace("\"", "\\\"") + (i + 1 < t.length ? "\\n" : "") + "\"");
			}
		} else {
			out.println(tag + " \"" + text.replace("\"", "\\\"") + "\"");
		}
	}
}