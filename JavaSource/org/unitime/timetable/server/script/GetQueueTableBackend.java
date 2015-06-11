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

import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface.GetQueueTableRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
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
		q.setCanDelete((context.hasPermissionAnyAuthority(item.getSessionId(), "Session", Right.Chameleon) || context.getUser().getExternalUserId().equals(item.getOwnerId())));
		
		if (item instanceof ScriptExecution)
			q.setExecutionRequest(((ScriptExecution)item).getRequest());
		
		return q;
	}

}
