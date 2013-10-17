/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
