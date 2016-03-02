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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.solver.jgroups.CourseSolverContainer;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;


/**
 * @author Tomas Muller
 */
public class TimetableInfoUtil implements TimetableInfoFileProxy {
	private static Log sLog = LogFactory.getLog(TimetableInfoUtil.class);
	private static TimetableInfoUtil sInstance = new TimetableInfoUtil();
	private TimetableInfoUtil() {}
	
	public static TimetableInfoUtil getLocalInstance() { return sInstance; }
	
	public static TimetableInfoFileProxy getInstance() {
		// Create the cluster instance
		SolverServer server = SolverServerImplementation.getInstance();
		if (server != null && server.getCourseSolverContainer() != null)
			return ((CourseSolverContainer)server.getCourseSolverContainer()).getFileProxy();
		
		// Fall back to local instance
		return getLocalInstance();
	}
	
	public boolean saveToFile(String name, TimetableInfo info) {
		FileOutputStream out = null;
		try {
			File file = new File(ApplicationProperties.getBlobFolder(),name);
			file.getParentFile().mkdirs();
			out = new FileOutputStream(file); 
			XMLWriter writer = new XMLWriter(new GZIPOutputStream(out),OutputFormat.createCompactFormat());
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement(info.getClass().getName());
			info.save(root);
			writer.write(document);
			writer.flush(); writer.close();
			out.flush();out.close();out=null;
			sLog.info("Saved info " + name + " as " + file + " (" + file.length() + " bytes)");
			return true;
		} catch (Exception e) {
			sLog.warn("Failed to save info " + name + ": " + e.getMessage(), e);
			return false;
		} finally {
    		try {
    			if (out!=null) out.close();
    		} catch (IOException e) {}
		}
	}
	
	public TimetableInfo loadFromFile(String name) {
		try {
			File file = new File(ApplicationProperties.getBlobFolder(),name);
			if (!file.exists()) return null;
			sLog.info("Loading info " + name + " from " + file + " (" + file.length() + " bytes)");
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
		} catch (Exception e) {
			sLog.warn("Failed to load info " + name + ": " + e.getMessage(), e);
			return null;
		}
	}
	
	public boolean deleteFile(String name) {
		try {
			File file = new File(ApplicationProperties.getBlobFolder(),name);
			if (file.exists()) {
				sLog.info("Deleting info " + name + " as " + file + " (" + file.length() + " bytes)");
				file.delete();
				return true;
			}
			return false;
		} catch (Exception e) {
			sLog.warn("Failed to delete info " + name + ": " + e.getMessage(), e);
			return false;
		}
	}
}
