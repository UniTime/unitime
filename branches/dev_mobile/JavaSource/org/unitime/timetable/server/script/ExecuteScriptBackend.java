/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.script;

import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface.ExecuteScriptRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.queue.QueueProcessor;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ExecuteScriptRpcRequest.class)
public class ExecuteScriptBackend implements GwtRpcImplementation<ExecuteScriptRpcRequest, QueueItemInterface> {

	@Override
	@PreAuthorize("checkPermission('Scripts')")
	public QueueItemInterface execute(ExecuteScriptRpcRequest request, SessionContext context) {
		try {

		ScriptExecution execution = new ScriptExecution(request, context);
		
		QueueProcessor.getInstance().add(execution);
		
		return GetQueueTableBackend.convert(execution, context);
		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

} 