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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.UserDataInterface;
import org.unitime.timetable.gwt.shared.UserDataInterface.GetUserDataRpcRequest;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetUserDataRpcRequest.class)
public class GetUserDataBackend implements GwtRpcImplementation<GetUserDataRpcRequest, UserDataInterface> {
    @Autowired 
	private SessionContext sessionContext;

	@Override
	public UserDataInterface execute(GetUserDataRpcRequest request, SessionContext context) {
		UserDataInterface ret = new UserDataInterface();
		UserContext user = sessionContext.getUser();
		if (user == null) return null;
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			for (UserData u: (List<UserData>)hibSession.createQuery(
					"from UserData u where u.externalUniqueId = :externalUniqueId and u.name in :names")
					.setString("externalUniqueId", user.getExternalUserId())
					.setParameterList("names", request)
					.setCacheable(true).list()) {
				ret.put(u.getName(), u.getValue());
			}
		} finally {
			hibSession.close();
		}
		return ret;
	}

}
