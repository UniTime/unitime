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
package org.unitime.timetable.solver.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.ApplicationProperties;


/**
 * @author Tomas Muller
 */
public class TimetableInfoUtil implements TimetableInfoFileProxy {
	private static TimetableInfoUtil sInstance = new TimetableInfoUtil();
	private TimetableInfoUtil() {}
	
	public static TimetableInfoUtil getInstance() { return sInstance; }
	
	public void saveToFile(String name, TimetableInfo info) throws Exception {
		File file = new File(ApplicationProperties.getBlobFolder(),name);
		file.getParentFile().mkdirs();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file); 
			XMLWriter writer = new XMLWriter(new GZIPOutputStream(out),OutputFormat.createCompactFormat());
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement(info.getClass().getName());
			info.save(root);
			writer.write(document);
			writer.flush(); writer.close();
			out.flush();out.close();out=null;
		} finally {
    		try {
    			if (out!=null) out.close();
    		} catch (IOException e) {}
		}
	}
	
	public TimetableInfo loadFromFile(String name) throws Exception {
		File file = new File(ApplicationProperties.getBlobFolder(),name);
		if (!file.exists()) return null;
		Document document = null;
		GZIPInputStream gzipInput = null;
		try {
			gzipInput = new GZIPInputStream(new FileInputStream(file));
			document = (new SAXReader()).read(gzipInput);
		} finally {
			if (gzipInput!=null) gzipInput.close();
		}
		Element root = document.getRootElement();
		String infoClassName = root.getName();
		Class infoClass = Class.forName(infoClassName);
		TimetableInfo info = (TimetableInfo)infoClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
		info.load(root);
		return info;
	}
	
	public void deleteFile(String name) throws Exception {
		File file = new File(ApplicationProperties.getBlobFolder(),name);
		if (file.exists())
			file.delete();
	}
}
