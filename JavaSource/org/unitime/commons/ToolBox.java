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
package org.unitime.commons;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/** 
 * This class provides several static functions from different areas.
 * @author Tomas Muller
 */
public class ToolBox {

	/** Replaces all occurances of a substring in a string.
	 * @param source source string
	 * @param substring substring
	 * @param newsubstring replacement for substring
	 * @return string with replacements
	 */
	public static String replace(
		String source,
		String substring,
		String newsubstring) {

		StringBuffer sb = new StringBuffer(source);
		int idx = -1;
		int len1 = substring.length();
		int len2 = newsubstring.length();

		while ((idx = (sb.toString().indexOf(substring, idx))) >= 0) {
			sb.replace(idx, idx + len1, newsubstring);
			idx += len2;
		}

		return sb.toString();
	}

	/** Replaces all occurances of a substring in a string -- ignores cases of strings.
	 * @param source source string
	 * @param substring substring
	 * @param newsubstring replacement for substring
	 * @return string with replacements
	 */
	public static String replaceIgnoreCase(
		String source,
		String substring,
		String newsubstring) {

		StringBuffer sb = new StringBuffer(source);
		int idx = -1;
		int len1 = substring.length();
		int len2 = newsubstring.length();

		while ((idx =
			(sb
				.toString()
				.toLowerCase()
				.indexOf(substring.toLowerCase(), idx)))
			>= 0) {
			sb.replace(idx, idx + len1, newsubstring);
			idx += len2;
		}

		return sb.toString();
	}

	/** Expand a string into the required length by adding a given character at the begining or at the end of the string.
	 * @param source source string
	 * @param length desired length
	 * @param ch character to be added
	 * @param beg if true character ch is repeatedly added at the beginning of the string. If false at the end of the string.
	 * @return desired string
	 */
	public static String expand(
		String source,
		int length,
		char ch,
		boolean beg) {

		StringBuffer sb =
			new StringBuffer(
				source == null
					? ""
					: source.length() > length
					? (beg
						? source.substring(source.length() - length)
						: source.substring(0, length))
					: source);

		while (sb.length() < length) {
			if (beg) {
				sb.insert(0, ch);
			} else {
				sb.append(ch);
			}
		}

		return sb.toString();
	}

	/** Return caller (name of class which call the method from which this method was called) - for debug.
	 * @return caller class
	 */
	public static String getCaller() {
		return getCaller(4);
	}

	/** Return caller class name.
	 * @param depth depth (1 .. ToolBox, 2 .. class which called ToolBox.getCaller, 3 .. class which called the caller class, ... )
	 * @return caller class
	 */
	public static String getCaller(int depth) {

		try {
			throw new Exception();
		} catch (Exception e) {
			try {
				StackTraceElement trace = e.getStackTrace()[depth-1];
				return trace.getClassName()+"."+trace.getMethodName();
			} catch (Exception ex) {
			}
		}

		return "unknown";
	}

	/** Sort enumeration
	 * @param e an enumeration
	 * @return sorted enumeration
	 */
	public static Enumeration sortEnumeration(java.util.Enumeration e) {

		return sortEnumeration(e, null);
	}

	/** Sort enumeration
	 * @param e an enumeration
	 * @param c comparator of two objects in enumeration e
	 * @return sorted enumeration
	 */
	public static Enumeration sortEnumeration(
		java.util.Enumeration e,
		java.util.Comparator c) {

		Vector v = new Vector();

		for (; e.hasMoreElements();) {
			v.addElement(e.nextElement());
		}
		Collections.sort(v, c);

		return v.elements();
	}

	/**
	 * Return true if two Strings are different
	 * @param a
	 * @param b
	 * @return boolean
	 */
	public static boolean diff(String a, String b) {

		if (a == null ? b == null : a.equals(b))
			return false;
		Debug.log("  diff: '" + a + "' with '" + b + "'");
		return true;
	}

	/**
	 * Return true if two Vectors are different
	 * @param a
	 * @param b
	 * @return boolean
	 */
	public static boolean diff(Vector a, Vector b) {

		if (a == null) {
			return (b == null ? false : true);
		}
		if (b == null) {
			return true;
		}
		if (a.size() != b.size()) {
			Debug.log("  diff: size");
			return true;
		}
		for (int i = 0; i < a.size(); i++) {
			Object oa = a.elementAt(i);
			Object ob = b.elementAt(i);

			if (!oa.equals(ob)) {
				Debug.log("  diff: " + oa + " with " + ob);
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if two Hashtables are different
	 * @param a
	 * @param b
	 * @return boolean
	 */
	public static boolean diff(Hashtable a, Hashtable b) {

		if (a == null) {
			return (b == null ? false : true);
		}
		if (b == null) {
			return true;
		}
		if (a.size() != b.size()) {
			Debug.log("  diff: size");
			return true;
		}
		for (Enumeration e = a.keys(); e.hasMoreElements();) {
			Object keya = e.nextElement();
			Object keyb = null;

			if (!b.containsKey(keya)) { // hashCode differs but try to find it
				for (Enumeration eb = b.keys();
					keyb == null && eb.hasMoreElements();
					) {
					Object key = eb.nextElement();

					if (key.equals(keya)) {
						keyb = key;
					}
				}
			} else {
				keyb = keya;
			}
			if (keyb == null) {
				Debug.log("  diff: key " + keya);
				return true;
			}
			Object oa = a.get(keya);
			Object ob = b.get(keyb);

			if (!oa.equals(ob)) {
				Debug.log("  diff: " + oa + " with " + ob);
				return true;
			}
		}
		return false;
	}

	/**
	 * This function constructs the absolute path to the target folder
	 * by traversing up from the App URL till the target folder
	 * @param AppURL URL of the app
	 * @param targetFolder The folder for which path is to be obtained
	 * @return Absolute file path 
	 */
	public synchronized static String getBasePath(URL appURL, String targetFolder) {

		//Get file and parent		
		java.io.File file = new java.io.File(appURL.getFile());		
		java.io.File parent = file.getParentFile();		
		
		// Iterate up the folder structure till WEB-INF is encountered
		while ( parent!=null && ! parent.getName().equals( targetFolder ) )  			
			parent = parent.getParentFile();
			
		return ( parent!=null ? parent.getAbsolutePath() : null );		
	}	

}
