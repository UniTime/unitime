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
package org.unitime.timetable.server.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Filter;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.server.admin.AdminTable.HasFilter;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=solverParamGroup]")
public class SolverParameterGroups implements AdminTable, HasFilter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageSolverParamGroup(), MESSAGES.pageSolverParamGroups());
	}

	@Override
	@PreAuthorize("checkPermission('SolverParameterGroups')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		return load(null, context, hibSession);
	}
	
	protected List<SolverParameterGroup> getSolverParameterGroups(String[] filter, SessionContext context) {
		if (filter == null || filter[0] == null || filter[0].isEmpty() || filter[0].equals("null")) {
			context.getUser().setProperty("Admin.SolverParamGroup.FilterType", null);
			return (List<SolverParameterGroup>)SolverParameterGroupDAO.getInstance().getSession().createQuery(
					"from SolverParameterGroup order by order, name"
					).list();
		} else {
			context.getUser().setProperty("Admin.SolverParamGroup.FilterType", filter[0]);
			return (List<SolverParameterGroup>)SolverParameterGroupDAO.getInstance().getSession().createQuery(
					"from SolverParameterGroup where type = :type order by order, name"
					).setInteger("type", Integer.valueOf(filter[0])).list();
		}
		
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameterGroups')")
	public SimpleEditInterface load(String[] filter, SessionContext context, Session hibSession) {
		List<ListItem> types = new ArrayList<ListItem>();
		types.add(new ListItem(String.valueOf(SolverType.COURSE.ordinal()), MESSAGES.solverCourse()));
		types.add(new ListItem(String.valueOf(SolverType.EXAM.ordinal()), MESSAGES.solverExamination()));
		types.add(new ListItem(String.valueOf(SolverType.STUDENT.ordinal()), MESSAGES.solverStudent()));
		types.add(new ListItem(String.valueOf(SolverType.INSTRUCTOR.ordinal()), MESSAGES.solverInstructor()));
		
		String defaultType = null;
		if (filter != null && filter[0] != null && !filter[0].isEmpty() && !filter[0].equals("null")) {
			defaultType = filter[0];
		}

		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 300, 100, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldType(), FieldType.list, 200, types, Flag.NOT_EMPTY).withDefault(defaultType),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 1000, Flag.NOT_EMPTY)
				);
		data.setSaveOrder(false);
		data.setCanMoveUpAndDown(true);
		data.setAllowSort(false);
		
		for (SolverParameterGroup group: getSolverParameterGroups(filter, context)) {
			Record r = data.addRecord( group.getUniqueId());
			r.setField(0, group.getName());
			r.setField(1, group.getType() == null ? "" : group.getType().toString(), group.getParameters().isEmpty());
			r.setField(2, group.getDescription());
			r.setDeletable(group.getParameters().isEmpty());
		}
		return data;
	}

	
	protected int nextOrd(Set<Integer> ords) {
		for (int i = 0; i < ords.size() + 1; i++) {
			if (!ords.contains(i)) {
				ords.add(i);
				return i;
			}
		}
		return ords.size();
	}
	
	protected int nextOrd() {
		List<SolverParameterGroup> groups = SolverParameterGroupDAO.getInstance().findAll();
		int idx = 0;
		t: while (true) {
			for (SolverParameterGroup t: groups) {
				if (idx == t.getOrder()) { idx++; continue t; }
			}
			return idx;
		}
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameterGroups')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		save(null, data, context, hibSession);
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameterGroups')")
	public void save(String[] filter, SimpleEditInterface data, SessionContext context, Session hibSession) {
		Set<Integer> ords = new HashSet<Integer>();
		if (filter != null && filter[0] != null && !filter[0].isEmpty() && !filter[0].equals("null")) {
			for (SolverParameterGroup group: (List<SolverParameterGroup>)SolverParameterGroupDAO.getInstance().getSession().createQuery(
					"from SolverParameterGroup where type != :type"
					).setInteger("type", Integer.valueOf(filter[0])).list()) {
				ords.add(group.getOrder());
			}
		}
		for (Record r: data.getRecords()) {
			if (r.isEmpty(data)) continue;
			r.setOrder(nextOrd(ords));
		}
		for (SolverParameterGroup group: getSolverParameterGroups(filter, context)) {
			Record r = data.getRecord(group.getUniqueId());
			if (r == null)
				delete(group, context, hibSession);
			else
				update(group, r, r.getOrder(), context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, r.getOrder(), context, hibSession);		
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameterGroups')")
	public void save(Record record, SessionContext context, Session hibSession) {
		save(record, null, context, hibSession);
	}
	
	protected void save(Record record, Integer order, SessionContext context, Session hibSession) {
		if (order == null) order = nextOrd();
		SolverParameterGroup group = new SolverParameterGroup();
		group.setName(record.getField(0));
		group.setType(Integer.valueOf(record.getField(1)));
		group.setDescription(record.getField(2));
		group.setOrder(order);
		record.setUniqueId((Long)hibSession.save(group));
		ChangeLog.addChange(hibSession,
				context,
				group,
				group.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(SolverParameterGroup group, Record record, Integer order, SessionContext context, Session hibSession) {
		if (group == null) return;
		if (order == null) order = group.getOrder();
		boolean changed = !ToolBox.equals(group.getName(), record.getField(0)) ||
				!ToolBox.equals(group.getType().toString(), record.getField(1)) ||
				!ToolBox.equals(group.getDescription(), record.getField(2));
		if (!changed && ToolBox.equals(group.getOrder(), order)) return;
		group.setName(record.getField(0));
		group.setType(Integer.valueOf(record.getField(1)));
		group.setDescription(record.getField(2));
		group.setOrder(order);
		hibSession.saveOrUpdate(group);
		if (changed)
			ChangeLog.addChange(hibSession,
					context,
					group,
					group.getName(),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
	}

	@Override
	@PreAuthorize("checkPermission('SolverParameterGroups')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(SolverParameterGroupDAO.getInstance().get(record.getUniqueId(), hibSession), record, null, context, hibSession);
	}

	protected void delete(SolverParameterGroup group, SessionContext context, Session hibSession) {
		if (group == null) return;
		ChangeLog.addChange(hibSession,
				context,
				group,
				group.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(group);
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameterGroups')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(SolverParameterGroupDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('SolverParameterGroups')")
	public Filter getFilter(SessionContext context, Session hibSession) {
		List<ListItem> types = new ArrayList<ListItem>();
		types.add(new ListItem("", MESSAGES.itemAll()));
		types.add(new ListItem(String.valueOf(SolverType.COURSE.ordinal()), MESSAGES.solverCourse()));
		types.add(new ListItem(String.valueOf(SolverType.EXAM.ordinal()), MESSAGES.solverExamination()));
		types.add(new ListItem(String.valueOf(SolverType.STUDENT.ordinal()), MESSAGES.solverStudent()));
		types.add(new ListItem(String.valueOf(SolverType.INSTRUCTOR.ordinal()), MESSAGES.solverInstructor()));
		
		SimpleEditInterface.Filter filter = new SimpleEditInterface.Filter(new Field(MESSAGES.fieldType(), FieldType.list, 100, types));
		String lastId = context.getUser().getProperty("Admin.SolverParamGroup.FilterType");
		if (lastId != null)
			filter.getDefaultValue().setField(0, lastId);
		return filter;
	}
}