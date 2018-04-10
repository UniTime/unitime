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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseLong;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLStoreRpcRequest;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLStoreRpcRequest.class)
public class HQLStoreBackend implements GwtRpcImplementation<HQLStoreRpcRequest, GwtRpcResponseLong>{
	@Autowired 
	private SessionContext sessionContext;

	@Override
	@PreAuthorize("(#query.id != null and checkPermission(#query.id, 'SavedHQL', 'HQLReportEdit')) or (#query.id == null and checkPermission('HQLReportAdd'))")
	public GwtRpcResponseLong execute(HQLStoreRpcRequest query, SessionContext context) {
		if (SavedHQL.Flag.ADMIN_ONLY.isSet(query.getFlags()))
			sessionContext.checkPermission(Right.HQLReportsAdminOnly);
		org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
		SavedHQL hql = null;
		if (query.getId() != null) {
			hql = SavedHQLDAO.getInstance().get(query.getId(), hibSession);
		}
		if (hql == null) {
			hql = new SavedHQL();
			hql.setParameters(new HashSet<SavedHQLParameter>());
		}
		hql.setName(query.getName());
		hql.setDescription(query.getDescription());
		hql.setType(query.getFlags());
		hql.setQuery(query.getQuery());
		
		if (query.hasParameters()) {
			Map<String, SavedHQLParameter> params = new HashMap<String, SavedHQLParameter>();
			for (SavedHQLParameter parameter: hql.getParameters())
				params.put(parameter.getName(), parameter);
			for (SavedHQLInterface.Parameter p: query.getParameters()) {
				SavedHQLParameter parameter = params.remove(p.getName());
				if (parameter == null) {
					parameter = new SavedHQLParameter();
					parameter.setName(p.getName());
					parameter.setSavedHQL(hql);
					hql.getParameters().add(parameter);
				}
				parameter.setType(p.getType());
				parameter.setLabel(p.getLabel());
				parameter.setDefaultValue(p.getDefaultValue());
			}
			for (SavedHQLParameter parameter: params.values()) {
				hibSession.delete(parameter);
				hql.getParameters().remove(parameter);
			}
		} else {
			for (Iterator<SavedHQLParameter> i = hql.getParameters().iterator(); i.hasNext(); ) {
				hibSession.delete(i.next());
				i.remove();
			}
		}
		
		hibSession.saveOrUpdate(hql);
		hibSession.flush();
		hibSession.refresh(hql);
		return new GwtRpcResponseLong(hql.getUniqueId());
	}

}
