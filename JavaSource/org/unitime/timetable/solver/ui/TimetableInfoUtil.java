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
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.spring.SpringApplicationContextHolder;


/**
 * @author Tomas Muller
 */
public class TimetableInfoUtil implements TimetableInfoFileProxy {
	private static Log sLog = LogFactory.getLog(TimetableInfoUtil.class);
	private static TimetableInfoUtil sInstance = new TimetableInfoUtil();
	private TimetableInfoUtil() {}
	
	public static TimetableInfoUtil getLocalInstance() { return sInstance; }
	
	public static TimetableInfoFileProxy getInstance() {
		SolverServer server = null;
		try {
			if (SpringApplicationContextHolder.isInitialized()) {
				// Spring -> user solver server service
				server = ((SolverServerService)SpringApplicationContextHolder.getBean("solverServerService")).getLocalServer();
			} else {
				// Standalone -> use get instance
				server = SolverServerImplementation.getInstance();
			}
		} catch (NoClassDefFoundError e) {
			// Standalone and unaware of Spring -> use get instance
			server = SolverServerImplementation.getInstance();
		}
		
		// Create the cluster instance
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
				System.out.println("Deleting info " + name + " as " + file + " (" + file.length() + " bytes)");
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
