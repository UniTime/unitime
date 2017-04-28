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
package org.unitime.timetable.gwt.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.services.LimitAndProjectionSnapshotService;
import org.unitime.timetable.gwt.shared.LimitAndProjectionSnapshotException;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.util.PopulateProjectedDemandSnapshotData;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("snapshot.gwt")
public class LimitAndProjectionSnapshotServlet implements LimitAndProjectionSnapshotService {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
//	private static Logger sLog = Logger.getLogger(LimitAndProjectionSnapshotServlet.class);

	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }
//	protected static DateTimeFormat sLoadDateFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
		
			
	private Long getAcademicSessionId() throws PageAccessException {
		UserContext user = getSessionContext().getUser();
		if (user == null) throw new PageAccessException(
				getSessionContext().isHttpSessionNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getCurrentAuthority() == null)
			throw new PageAccessException("Insufficient user privileges.");
		Long sessionId = user.getCurrentAcademicSessionId();
		if (sessionId == null) throw new PageAccessException("No academic session is selecgted.");
		return sessionId;
	}

	@Override
	@PreAuthorize("checkPermission('LimitAndProjectionSnapshotSave')")
	public Boolean canTakeSnapshot() throws LimitAndProjectionSnapshotException, PageAccessException {
		return true;
	}
	
	
	@Override
	public Date getCurrentSnapshotDate() throws LimitAndProjectionSnapshotException, PageAccessException {
		Session session = null;
		if (getAcademicSessionId() != null) {
			session = SessionDAO.getInstance().get(getAcademicSessionId());			
		} else if (sessionContext.getUser() != null && sessionContext.getUser().getCurrentAcademicSessionId() != null) {
			session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
		}
		
		return(session.getCurrentSnapshotDate());
	}

	@Override
	public Date takeSnapshot() throws LimitAndProjectionSnapshotException, PageAccessException {
		Session session = null;
		Date snapshotDate = null;
		if (getAcademicSessionId() != null) {
			session = SessionDAO.getInstance().get(getAcademicSessionId());			
		} 
		
		if (session != null) {
			PopulateProjectedDemandSnapshotData ppdsd = new PopulateProjectedDemandSnapshotData();
			snapshotDate = ppdsd.populateProjectedDemandDataFor(session);
		}
		return snapshotDate;
	}

}
