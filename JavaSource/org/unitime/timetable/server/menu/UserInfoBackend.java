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
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface.UserInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.UserInfoRpcRequest;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.AnonymousUserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(UserInfoRpcRequest.class)
public class UserInfoBackend implements GwtRpcImplementation<UserInfoRpcRequest, UserInfoInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

    @Autowired 
	private SessionContext sessionContext;

	@Override
	public UserInfoInterface execute(UserInfoRpcRequest request, SessionContext context) {
		UserInfoInterface ret = new UserInfoInterface();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			
			UserContext user = sessionContext.getUser();
			if (user == null || user instanceof AnonymousUserContext) return null;
			
			ret.addPair(MESSAGES.fieldName(), user.getName());
			ret.setName(user.getName());
			
			String dept = "";
			if (user.getCurrentAuthority() != null)
				for (Qualifiable q: user.getCurrentAuthority().getQualifiers("Department")) {
 					if (!dept.isEmpty()) dept += ",";
 					dept += "<span title='"+q.getQualifierLabel()+"'>"+q.getQualifierReference()+"</span>";
				}
			ret.addPair(MESSAGES.fieldDepartment(), dept);
	 		
	 		String role = (user.getCurrentAuthority() == null ? null : user.getCurrentAuthority().getLabel());
	 		if (role == null) role = MESSAGES.noRole();
	 		ret.addPair(MESSAGES.fieldRole(), role);
	 		ret.setRole(role);
	 		
	 		ret.setChameleon(sessionContext.hasPermission(Right.Chameleon) || (user instanceof UserContext.Chameleon));

		} finally {
			hibSession.close();
		}
		return ret;
	}

}
