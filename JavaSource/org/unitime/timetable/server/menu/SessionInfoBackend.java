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
package org.unitime.timetable.server.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface.SessionInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.SessionInfoRpcRequest;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SessionInfoRpcRequest.class)
public class SessionInfoBackend implements GwtRpcImplementation<SessionInfoRpcRequest, SessionInfoInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

    @Autowired 
	private SessionContext sessionContext;

	@Override
	public SessionInfoInterface execute(SessionInfoRpcRequest request, SessionContext context) {
		SessionInfoInterface ret = new SessionInfoInterface();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			UserContext user = sessionContext.getUser();
			if (user == null) return null;
			
			if (user.getCurrentAcademicSessionId() == null) {
				if (sessionContext.hasPermissionAnyAuthority(Right.HasRole)) {
					ret.addPair(MESSAGES.fieldSession(), MESSAGES.notSelected());
					ret.addPair(MESSAGES.fieldDatabase(), HibernateUtil.getDatabaseName());
					ret.setSession(MESSAGES.notSelected());
			 		return ret;
				}
				return null;
			}
			
			Session session = SessionDAO.getInstance().get(user.getCurrentAcademicSessionId(), hibSession);
	 		
			ret.addPair(MESSAGES.fieldSession(), session.getLabel());
 			ret.addPair(MESSAGES.fieldStatus(), session.getStatusType().getLabel());
 			ret.setSession(session.getLabel());
	 		
 			ret.addPair(MESSAGES.fieldDatabase(), HibernateUtil.getDatabaseName());
	 		
		} finally {
			hibSession.close();
		}		
		return ret;
	}

}
