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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLQueriesRpcRequest;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Query;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;
import org.unitime.timetable.model.dao.RefTableEntryDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLQueriesRpcRequest.class)
public class HQLQueriesBackend implements GwtRpcImplementation<HQLQueriesRpcRequest, GwtRpcResponseList<Query>>{
	@Autowired 
	private SessionContext sessionContext;
	
	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public GwtRpcResponseList<Query> execute(HQLQueriesRpcRequest request, SessionContext context) {
		SavedHQL.Flag ap = getAppearanceFlag(request.getAppearance());
		if (ap.getPermission() != null)
			sessionContext.checkPermission(ap.getPermission());
		GwtRpcResponseList<SavedHQLInterface.Query> ret = new GwtRpcResponseList<SavedHQLInterface.Query>(); 
		for (SavedHQL hql: SavedHQL.listAll(null, ap, sessionContext.hasPermission(Right.HQLReportsAdminOnly))) {
			SavedHQLInterface.Query query = new SavedHQLInterface.Query();
			query.setName(hql.getName());
			query.setDescription(hql.getDescription());
			query.setQuery(hql.getQuery());
			query.setFlags(hql.getType());
			query.setId(hql.getUniqueId());
			for (SavedHQLParameter p: hql.getParameters()) {
				SavedHQLInterface.Parameter parameter = new SavedHQLInterface.Parameter();
				parameter.setLabel(p.getLabel() == null ? p.getName() : p.getLabel());
				parameter.setName(p.getName());
				parameter.setDefaultValue(p.getDefaultValue());
				parameter.setType(p.getType());
				if (p.getType().startsWith("enum(") && p.getType().endsWith(")")) {
					for (String option: p.getType().substring("enum(".length(), p.getType().length() - 1).split(","))
						parameter.addOption(option, option);
				} else if (p.getType().startsWith("reference(") && p.getType().endsWith(")")) {
					try {
						String clazz = p.getType().substring("reference(".length(), p.getType().length() - 1);
						for (RefTableEntry entry: (List<RefTableEntry>)RefTableEntryDAO.getInstance().getSession().createQuery("from " + clazz).setCacheable(true).list()) {
							parameter.addOption(entry.getReference(), entry.getLabel());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					for (SavedHQL.Option option: SavedHQL.Option.values()) {
						if (p.getType().equalsIgnoreCase(option.name())) {
							parameter.setMultiSelect(option.allowMultiSelection());
							for (Map.Entry<Long, String> entry: option.values(context.getUser()).entrySet()) {
								parameter.addOption(entry.getKey().toString(), entry.getValue());
							}
							if (p.getDefaultValue() != null) {
								Long id = option.lookupValue(context.getUser(), p.getDefaultValue());
								if (id != null)
									parameter.setValue(id.toString());
							}
							break;
						}
					}
				}
				query.addParameter(parameter);
			}
			ret.add(query);
		}
		return ret;
	}

	private SavedHQL.Flag getAppearanceFlag(String appearance) {
		for (SavedHQL.Flag flag: SavedHQL.Flag.values())
			if (flag.getAppearance() != null && flag.getAppearance().equalsIgnoreCase(appearance))
				return flag;
		return SavedHQL.Flag.APPEARANCE_COURSES;
	}
}
