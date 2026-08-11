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

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface.GetQueueLogRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemLogInterface;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.queue.QueueItem;

@GwtRpcImplements(GetQueueLogRpcRequest.class)
public class GetQueueLogRpcBackend implements GwtRpcImplementation<GetQueueLogRpcRequest, QueueItemLogInterface>{
	
	@Autowired SolverServerService solverServerService;

	@Override
	public QueueItemLogInterface execute(GetQueueLogRpcRequest request, SessionContext context) {
		switch(request.getType()) {
		case ExamPdfReport:
			context.checkPermission(Right.ExaminationPdfReports);
			break;
		case DataExchange:
			context.checkPermission(Right.DataExchange);
			break;
		case EnrollmentPdfReport:
			context.checkPermission(Right.EnrollmentAuditPDFReports);
			break;
		case RollForward:
			context.checkPermission(Right.SessionRollForward);
			break;
		case Script:
		default:
			context.checkPermission(Right.Scripts);
		}
		
		QueueItem item = solverServerService.getQueueProcessor().get(request.getQueueId());
		if (item != null) {
			QueueItemLogInterface ret = new QueueItemLogInterface();
			ret.setId(item.getId());
			ret.setName(item.name());
			ret.setLog(item.log());
			return ret;
		}
		
		return null;
	}

}
