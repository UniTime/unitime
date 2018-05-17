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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.TaskParameter;
import org.unitime.timetable.model.dao.PeriodicTaskDAO;
import org.unitime.timetable.model.dao.TaskExecutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class TaskOutputFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected SessionContext getSessionContext() {
		return HttpSessionContext.getSessionContext(getServletContext());
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String e = request.getParameter("e");
		if (e != null) {
			TaskExecution exec = TaskExecutionDAO.getInstance().get(Long.valueOf(e));
			if (exec == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task execution not found.");
				return;
			}
			if (exec.getOutputFile() == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task execution has no output file.");
				return;
			}
			getSessionContext().checkPermission(exec.getTask().getSession(), Right.TaskDetail);
			if (exec.getTask().getScript().getPermission() != null)
				try {
					Right right = Right.valueOf(exec.getTask().getScript().getPermission().replace(" ", ""));
					if (!getSessionContext().hasPermission(right)) {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing permission " + exec.getTask().getScript().getPermission());
						return;
					}
				} catch (IllegalArgumentException ex) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad permission " + exec.getTask().getScript().getPermission());
					return;
				}
			response.setContentType(exec.getOutputContentType());
			response.setHeader( "Content-Disposition", "attachment; filename=\"" + exec.getOutputName() + "\"" );
			OutputStream out = response.getOutputStream();
			InputStream in = new ByteArrayInputStream(exec.getOutputFile());
			try {
				IOUtils.copy(in, out);
				out.flush();
			} finally {
				in.close();	
				out.close();
			}
			return;
		}
		String t = request.getParameter("t");
		if (t != null) {
			PeriodicTask task = PeriodicTaskDAO.getInstance().get(Long.valueOf(t));
			if (task == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task not found.");
				return;
			}
			if (task.getInputFile() == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task has no input file.");
				return;
			}
			getSessionContext().checkPermission(task.getSession(), Right.TaskDetail);
			if (task.getScript().getPermission() != null)
				try {
					Right right = Right.valueOf(task.getScript().getPermission().replace(" ", ""));
					if (!getSessionContext().hasPermission(right)) {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing permission " + task.getScript().getPermission());
						return;
					}
				} catch (IllegalArgumentException ex) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad permission " + task.getScript().getPermission());
					return;
				}
			ScriptParameter fp = null;
			for (ScriptParameter p: task.getScript().getParameters())
				if ("file".equalsIgnoreCase(p.getType())) { fp = p; break; }
			String name = "unknown.file";
			for (TaskParameter p: task.getParameters())
				if (fp != null && fp.getName().equals(p.getName())) name = p.getValue();
			response.setContentType("application/octet-stream");
			response.setHeader( "Content-Disposition", "attachment; filename=\"" + name + "\"" );
			OutputStream out = response.getOutputStream();
			InputStream in = new ByteArrayInputStream(task.getInputFile());
			try {
				IOUtils.copy(in, out);
				out.flush();
			} finally {
				in.close();	
				out.close();
			}
			return;
		}
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task or execution id not provided.");
	}

}