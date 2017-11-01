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
package org.unitime.timetable.api.connectors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.api.BinaryFileApiHelper;
import org.unitime.timetable.api.BinaryFileApiHelper.BinaryFile;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.gwt.shared.ScriptInterface.ExecuteScriptRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ScriptDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.script.GetQueueTableBackend;
import org.unitime.timetable.server.script.ScriptExecution;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.QueueMessage;

/**
 * @author Tomas Muller
 */
@Service("/api/script")
public class ScriptConnector extends ApiConnector {
	
	@Autowired SolverServerService solverServerService;
	
	@Override
	public void doGet(ApiHelper helper) throws IOException {
		helper.getSessionContext().checkPermission(Right.Scripts);
		if (helper.getParameter("output") != null) {
			DataSource ds = solverServerService.getQueueProcessor().getFile(helper.getParameter("output"));
			if (ds == null)
				helper.sendError(HttpServletResponse.SC_NO_CONTENT, "No output found for task " + helper.getParameter("output"));
			else
				helper.setResponse(new BinaryFileApiHelper.BinaryFile(IOUtils.toByteArray(ds.getInputStream()), ds.getContentType(), ds.getName()));
		} else if (helper.getParameter("log") != null) {
			solverServerService.getQueueProcessor().get(helper.getParameter("log"));
			QueueItem item = solverServerService.getQueueProcessor().get(helper.getParameter("log"));
			if (item == null)
				helper.sendError(HttpServletResponse.SC_NO_CONTENT, "No task found for " + helper.getParameter("output"));
			else {
				String log = "";
				for (QueueMessage m: item.getLog()) {
					log += m.toString() + "\n";
				}
				helper.setResponse(new BinaryFileApiHelper.BinaryFile(log.getBytes(), "plain/text", item.getId() + ".log"));
			}
		} else if (helper.getParameter("id") != null) {
			solverServerService.getQueueProcessor().get(helper.getParameter("id"));
			QueueItem item = solverServerService.getQueueProcessor().get(helper.getParameter("id"));
			if (item == null)
				helper.sendError(HttpServletResponse.SC_NO_CONTENT, "No task found for " + helper.getParameter("output"));
			helper.setResponse(GetQueueTableBackend.convert(item, helper.getSessionContext()));
		} else if (helper.getParameter("finished") != null) {
			solverServerService.getQueueProcessor().get(helper.getParameter("finished"));
			QueueItem item = solverServerService.getQueueProcessor().get(helper.getParameter("finished"));
			if (item == null)
				helper.sendError(HttpServletResponse.SC_NO_CONTENT, "No task found for " + helper.getParameter("output"));
			else
				helper.setResponse(new Boolean(item.finished() != null));
		} else if (helper.getParameter("delete") != null) {
			Boolean ret = solverServerService.getQueueProcessor().remove(helper.getParameter("delete"));
			helper.setResponse(ret);
		} else if (helper.getParameter("script") != null) {
			doPost(helper);
		} else {
			List<QueueItem> items = solverServerService.getQueueProcessor().getItems(null, null, "Script");
			List<QueueItemInterface> converted = new ArrayList<QueueItemInterface>();
			if (items != null)
				for (QueueItem item: items)
					converted.add(GetQueueTableBackend.convert(item, helper.getSessionContext()));
			helper.setResponse(converted);
		}
	}
	
	@Override
	public void doPost(ApiHelper helper) throws IOException {
		helper.getSessionContext().checkPermissionAnyAuthority(Right.Scripts);
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId, helper.getHibSession());
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");
		
		String scriptName = helper.getParameter("script");
		if (scriptName == null)
			throw new IllegalArgumentException("SCRIPT parameter not provided.");
		
		Script script = (Script)ScriptDAO.getInstance().getSession().createQuery(
				"from Script where name = :name").setString("name", scriptName).uniqueResult();
		if (script == null)
			throw new IllegalArgumentException("Script " + scriptName + " does not exist.");
		
		ExecuteScriptRpcRequest request = new ExecuteScriptRpcRequest();
		request.setScriptId(script.getUniqueId());
		request.setScriptName(script.getName());
		request.setEmail(helper.getParameter("email"));
		for (ScriptParameter parameter: script.getParameters()) {
			if ("file".equals(parameter.getType())) {
				final BinaryFile file = helper.getRequest(BinaryFile.class);
				if (file == null)
					throw new IllegalArgumentException("Input file not provided.");
				helper.getSessionContext().setAttribute(UploadServlet.SESSION_LAST_FILE, new BinaryFileItem(file));
			} else {
				String[] values = helper.getParameterValues(parameter.getName());
				if (values == null || values.length == 0) continue;
				String value = "";
				for (String v: values)
					value += (value.isEmpty() ? "" : ",") + convertParameter(v, parameter.getType(), helper);
				if (value != null)
					request.setParameter(parameter.getName(), value);
			}
		}
		
		if (!helper.getOptinalParameterBoolean("queue", true)) {
			ScriptExecution item = new ScriptExecution(request, helper.getSessionContext());
			item.executeItem();
			if (item.hasOutput()) {
				FileInputStream is = new FileInputStream(item.output());
				helper.setResponse(new BinaryFile(IOUtils.toByteArray(is), FileTypeMap.getDefaultFileTypeMap().getContentType(item.output()), item.getOutputName()));
				is.close();
			} else {
				String log = "";
				for (QueueMessage m: item.getLog()) {
					log += m.toString() + "\n";
				}
				helper.setResponse(new BinaryFileApiHelper.BinaryFile(log.getBytes(), "plain/text", item.getId() + ".log"));
			}
		} else {
			QueueItem item = solverServerService.getQueueProcessor().add(new ScriptExecution(request, helper.getSessionContext()));
			helper.setResponse(GetQueueTableBackend.convert(item, helper.getSessionContext()));
		}
	}
	
	protected String convertParameter(String value, String type, ApiHelper helper) {
		for (SavedHQL.Option option: SavedHQL.Option.values()) {
			if (type.equalsIgnoreCase(option.name())) {
				Long id = option.lookupValue(helper.getSessionContext().getUser(), value);
				if (id == null) {
					try {
						id = Long.valueOf(value);
					} catch (NumberFormatException e) {}
				}
				if (id != null)
					return id.toString();
				throw new IllegalArgumentException(option.text() + " " + value + " not found.");
			}
		}
		return value;
	}
	
	@Override
	protected ApiHelper createHelper(HttpServletRequest request, HttpServletResponse response) {
		return new BinaryFileApiHelper(request, response, sessionContext, getCacheMode());
	}
	
	@Override
	protected String getName() {
		return "script";
	}
	
	public static class BinaryFileItem implements FileItem {
		private static final long serialVersionUID = 1L;
		BinaryFile iFile;
		public BinaryFileItem(BinaryFile file) { iFile = file; }
		@Override
		public void setHeaders(FileItemHeaders headers) {}
		@Override
		public FileItemHeaders getHeaders() { return null; }
		@Override
		public void write(File file) throws Exception {}
		@Override
		public void setFormField(boolean state) {}
		@Override
		public void setFieldName(String name) {}
		@Override
		public boolean isInMemory() { return true; }
		@Override
		public boolean isFormField() { return false; }
		@Override
		public String getString(String encoding) throws UnsupportedEncodingException { return new String(iFile.getBytes(), encoding); }
		@Override
		public String getString() { return new String(iFile.getBytes()); }
		@Override
		public long getSize() { return iFile.getBytes().length; }
		@Override
		public OutputStream getOutputStream() throws IOException { return null; }
		@Override
		public String getName() { return iFile.getFileName(); }
		@Override
		public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(iFile.getBytes()); }
		@Override
		public String getFieldName() { return null; }
		@Override
		public String getContentType() { return iFile.getContentType(); }
		@Override
		public byte[] get() { return iFile.getBytes(); }
		@Override
		public void delete() {}
	}
}
