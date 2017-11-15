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
package org.unitime.timetable.server.script;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.Transaction;
import org.unitime.commons.Email;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.gwt.shared.ScriptInterface.ExecuteScriptRpcRequest;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.ScriptDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.queue.QueueItem;

/**
 * @author Tomas Muller
 */
public class ScriptExecution extends QueueItem {
	private static final long serialVersionUID = 1L;
	private ExecuteScriptRpcRequest iRequest;
	private FileItem iFile = null;
	
	public ScriptExecution(ExecuteScriptRpcRequest request, SessionContext context) {
		super(context.getUser());
		iRequest = request;
		
		Script script = ScriptDAO.getInstance().get(request.getScriptId());
		if (script.getPermission() != null)
			context.checkPermission(Right.valueOf(script.getPermission().replace(" ", "")));
		
		for (ScriptParameter parameter: script.getParameters())
			if ("file".equals(parameter.getType()))
				iFile = (FileItem)context.getAttribute(UploadServlet.SESSION_LAST_FILE);
	}
	
	public ExecuteScriptRpcRequest getRequest() { return iRequest; }

	@Override
	public String type() { return "Script"; }

	@Override
	public String name() { return iRequest.getScriptName(); }
	
	public void debug(String message) { super.debug(message); }
	public void info(String message) { super.info(message); }
	public void warn(String message) { super.warn(message); }
	public void error(String message) { super.error(message); }
	public void error(String message, Throwable t) { super.error(message, t); setError(t); }
	
	public File createOutput(String prefix, String ext) {
		return super.createOutput(prefix, ext);
	}
	
	protected Department lookupDepartment(org.hibernate.Session hibSession, String value) {
		try {
			Department d = DepartmentDAO.getInstance().get(Long.valueOf(value), hibSession);
			if (d != null) return d;
		} catch (NumberFormatException e) {}
		return Department.findByDeptCode(value, getSessionId(), hibSession);
	}
	
	protected SubjectArea lookupSubjectArea(org.hibernate.Session hibSession, String value) {
		try {
			SubjectArea s = SubjectAreaDAO.getInstance().get(Long.valueOf(value), hibSession);
			if (s != null) return s;
		} catch (NumberFormatException e) {}
		return SubjectArea.findByAbbv(hibSession, getSessionId(), value);
	}
	
	protected Building lookupBuilding(org.hibernate.Session hibSession, String value) {
		try {
			Building b = BuildingDAO.getInstance().get(Long.valueOf(value), hibSession);
			if (b != null) return b;
		} catch (NumberFormatException e) {}
		return Building.findByBldgAbbv(hibSession, getSessionId(), value);
	}

	protected Location lookupLocation(org.hibernate.Session hibSession, String value) {
		try {
			Location l = LocationDAO.getInstance().get(Long.valueOf(value), hibSession);
			if (l != null) return l;
		} catch (NumberFormatException e) {}
		return Location.findByName(hibSession, getSessionId(), value);
	}
	
	protected Room lookupRoom(org.hibernate.Session hibSession, String value) {
		try {
			Room r = RoomDAO.getInstance().get(Long.valueOf(value), hibSession);
			if (r != null) return r;
		} catch (NumberFormatException e) {}
		Location l = Location.findByName(hibSession, getSessionId(), value);
		return (l != null && l instanceof Room ? (Room)l : null);
	}

	@Override
	protected void execute() throws Exception {
		org.hibernate.Session hibSession = ScriptDAO.getInstance().getSession();
		
		Transaction tx = hibSession.beginTransaction();
		try {
			setStatus(MSG.scriptStatusStartingUp(), 3);

			Script script = ScriptDAO.getInstance().get(iRequest.getScriptId(), hibSession);
			
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName(script.getEngine());
			engine.put("hibSession", hibSession);
			engine.put("session", SessionDAO.getInstance().get(getSessionId()));
			engine.put("log", this);
			
			incProgress();
			
			engine.getContext().setWriter(new Writer() {
				@Override
				public void write(char[] cbuf, int off, int len) throws IOException {
					String line = String.valueOf(cbuf, off, len);
					if (line.endsWith("\n")) line = line.substring(0, line.length() - 1);
					if (!line.isEmpty())
						info(line);
				}
				@Override
				public void flush() throws IOException {}
				@Override
				public void close() throws IOException {}
			});
			engine.getContext().setErrorWriter(new Writer() {
				@Override
				public void write(char[] cbuf, int off, int len) throws IOException {
					String line = String.valueOf(cbuf, off, len);
					if (line.endsWith("\n")) line = line.substring(0, line.length() - 1);
					if (!line.isEmpty())
						warn(line);
				}
				@Override
				public void flush() throws IOException {}
				@Override
				public void close() throws IOException {}
			});
			
			incProgress();
			
			debug("Engine: " + engine.getFactory().getEngineName() + " (ver. " + engine.getFactory().getEngineVersion() + ")");
			debug("Language: " + engine.getFactory().getLanguageName() + " (ver. " + engine.getFactory().getLanguageVersion() + ")");
			
			for (ScriptParameter parameter: script.getParameters()) {
				String value = iRequest.getParameters().get(parameter.getName());
				
				if ("file".equals(parameter.getType()) && iFile != null) {
					debug(parameter.getName() + ": " + iFile.getName() + " (" + iFile.getSize() + " bytes)");
					engine.put(parameter.getName(), iFile);
					continue;
				}
				
				if (value == null) value = parameter.getDefaultValue();
				if (value == null) {
					engine.put(parameter.getName(), null);
					continue;
				}
				debug(parameter.getName() + ": " + value);
				
				if (parameter.getType().equalsIgnoreCase("boolean")) {
					engine.put(parameter.getName(), "true".equalsIgnoreCase(value));
				} else if (parameter.getType().equalsIgnoreCase("long")) {
					engine.put(parameter.getName(), value == null || value.isEmpty() ? null : Long.valueOf(value));
				} else if (parameter.getType().equalsIgnoreCase("int") || parameter.getType().equalsIgnoreCase("integer")) {
					engine.put(parameter.getName(), value == null || value.isEmpty() ? null : Integer.valueOf(value));
				} else if (parameter.getType().equalsIgnoreCase("double")) {
					engine.put(parameter.getName(), value == null || value.isEmpty() ? null : Double.valueOf(value));
				} else if (parameter.getType().equalsIgnoreCase("float")) {
					engine.put(parameter.getName(), value == null || value.isEmpty() ? null : Float.valueOf(value));
				} else if (parameter.getType().equalsIgnoreCase("short")) {
					engine.put(parameter.getName(), value == null || value.isEmpty() ? null : Short.valueOf(value));
				} else if (parameter.getType().equalsIgnoreCase("byte")) {
					engine.put(parameter.getName(), value == null || value.isEmpty() ? null : Byte.valueOf(value));
				} else if (parameter.getType().equalsIgnoreCase("date")) {
					Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
					engine.put(parameter.getName(), dateFormat.parse(value));
				} else if (parameter.getType().equalsIgnoreCase("department")) {
					engine.put(parameter.getName(), lookupDepartment(hibSession, value));
				} else if (parameter.getType().equalsIgnoreCase("departments")) {
					List<Department> departments = new ArrayList<Department>();
					for (String id: value.split(","))
						if (!id.isEmpty()) {
							Department d = lookupDepartment(hibSession, id);
							if (d != null) departments.add(d);
						}
					engine.put(parameter.getName(), departments);
				} else if (parameter.getType().equalsIgnoreCase("subject")) {
					engine.put(parameter.getName(), lookupSubjectArea(hibSession, value));
				} else if (parameter.getType().equalsIgnoreCase("subjects")) {
					List<SubjectArea> subjects = new ArrayList<SubjectArea>();
					for (String id: value.split(","))
						if (!id.isEmpty()) {
							SubjectArea s = lookupSubjectArea(hibSession, id);
							if (s != null) subjects.add(s);
						}
					engine.put(parameter.getName(), subjects);
				} else if (parameter.getType().equalsIgnoreCase("building")) {
					engine.put(parameter.getName(), lookupBuilding(hibSession, value));
				} else if (parameter.getType().equalsIgnoreCase("buildings")) {
					List<Building> buildings = new ArrayList<Building>();
					for (String id: value.split(","))
						if (!id.isEmpty()) {
							Building b = lookupBuilding(hibSession, id);
							if (b != null) buildings.add(b);
						}
					engine.put(parameter.getName(), buildings);
				} else if (parameter.getType().equalsIgnoreCase("room")) {
					engine.put(parameter.getName(), lookupRoom(hibSession, value));
				} else if (parameter.getType().equalsIgnoreCase("rooms")) {
					List<Room> rooms = new ArrayList<Room>();
					for (String id: value.split(","))
						if (!id.isEmpty()) {
							Room r = lookupRoom(hibSession, id);
							if (r != null) rooms.add(r);
						}
					engine.put(parameter.getName(), rooms);
				} else if (parameter.getType().equalsIgnoreCase("location")) {
					engine.put(parameter.getName(), lookupLocation(hibSession, value));
				} else if (parameter.getType().equalsIgnoreCase("locations")) {
					List<Location> locations = new ArrayList<Location>();
					for (String id: value.split(","))
						if (!id.isEmpty()) {
							Location l = lookupLocation(hibSession, id);
							if (l != null) locations.add(l);
						}
					engine.put(parameter.getName(), locations);
				} else {
					engine.put(parameter.getName(), value);
				}
			}
			
			incProgress();
			
			if (engine instanceof Compilable) {
				setStatus(MSG.scriptStatusCompiling(), 1);
				CompiledScript compiled = ((Compilable)engine).compile(script.getScript());
				incProgress();
				setStatus(MSG.scriptStatusRunning(), 100);
				compiled.eval();
			} else {
				setStatus(MSG.scriptStatusRunning(), 100);
				engine.eval(script.getScript());
			}
			
			hibSession.flush();
			tx.commit();
			
			setStatus(MSG.scriptStatusAllDone(), 1);
			incProgress();
		} catch (Exception e) {
			tx.rollback();
			error(MSG.failedExecution(e.getMessage()), e);
		} finally {
			hibSession.close();
		}
	}
	
	@Override
	public void executeItem() {
		super.executeItem();
		sendEmail();
	}
	
	protected boolean sendEmail() {
		if (!getRequest().hasEmail()) return false;
		try {
			Email email = Email.createEmail();
			String suffix = ApplicationProperty.EmailDefaultAddressSuffix.value();
			for (String address: getRequest().getEmail().split("[\n,]")) {
				if (!address.trim().isEmpty()) {
					if (suffix != null && address.indexOf('@') < 0)
						email.addRecipientCC(address.trim() + suffix, null);
					else
						email.addRecipientCC(address.trim(), null);
				}
			}
			email.setHTML(log());
			if (hasOutput())
				email.addAttachment(output(), getOutputName());
			if (hasOwnerEmail())
				email.setReplyTo(getOwnerEmail(), getOwnerName());
			email.setSubject(name() + (hasError() ? " -- " + error().getMessage() : ""));
			email.send();
		} catch (Exception e) {
			Throwable t = error();
			error(MSG.failedEmail(e.getMessage()), e);
			if (t != null) setError(t);
		}
		return true;
	}
}