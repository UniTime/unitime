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

import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface.GetQueueTableRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.QueueProcessor;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetQueueTableRpcRequest.class)
public class GetQueueTableBackend implements GwtRpcImplementation<GetQueueTableRpcRequest, GwtRpcResponseList<QueueItemInterface>>{

	@Override
	public GwtRpcResponseList<QueueItemInterface> execute(GetQueueTableRpcRequest request, SessionContext context) {
		if (request.getDeleteId() != null)
			QueueProcessor.getInstance().remove(request.getDeleteId());
		

		List<QueueItem> queue = QueueProcessor.getInstance().getItems(null, null, "Script");
		GwtRpcResponseList<QueueItemInterface> table = new GwtRpcResponseList<QueueItemInterface>();
		
		Date now = new Date();
		long timeToShow = 1000 * 60 * 60;
		for (QueueItem item: queue) {
			if (item.finished() != null && now.getTime() - item.finished().getTime() > timeToShow) continue;

			table.add(convert(item, context));
		}
		
		return table;
	}
	
	public static QueueItemInterface convert(QueueItem item, SessionContext context) {
		QueueItemInterface q = new QueueItemInterface();
		
		q.setId(item.getId());
		q.setName(item.name());
		q.setStatus(item.status());
		q.setProgress(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%");
		q.setOwner(item.getOwnerName());
		q.setSession(item.getSession() == null ? "None" : item.getSession().getLabel());
		q.setCreated(item.created());
		q.setStarted(item.started());
		q.setFinished(item.finished());
		if (item.finished() != null && item.hasOutput())
			q.setOutput(item.output().getName());
		q.setLog(item.log());
		q.setCanDelete(context.getUser().getExternalUserId().equals(item.getOwnerId()) && (item.started() == null || item.finished() != null));
		
		if (item instanceof ScriptExecution)
			q.setExecutionRequest(((ScriptExecution)item).getRequest());
		
		return q;
	}

}
