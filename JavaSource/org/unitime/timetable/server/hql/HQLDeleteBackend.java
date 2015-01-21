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
package org.unitime.timetable.server.hql;

import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLDeleteRpcRequest;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLDeleteRpcRequest.class)
public class HQLDeleteBackend implements GwtRpcImplementation<HQLDeleteRpcRequest, GwtRpcResponseBoolean>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	@PreAuthorize("checkPermission(#request.id, 'SavedHQL', 'HQLReportDelete')")
	public GwtRpcResponseBoolean execute(HQLDeleteRpcRequest request, SessionContext context) {
		if (request.getId() == null) throw new GwtRpcException(MESSAGES.errorNoReportProvided());
		org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
		SavedHQL hql = SavedHQLDAO.getInstance().get(request.getId(), hibSession);
		if (hql != null) {
			hibSession.delete(hql);
			hibSession.flush();
		}
		return new GwtRpcResponseBoolean(hql != null);
	}
}
