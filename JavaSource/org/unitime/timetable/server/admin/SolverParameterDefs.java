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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.server.admin.AdminTable.HasFilter;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=solverParamDef]")
public class SolverParameterDefs implements AdminTable, HasFilter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageSolverParam(), MESSAGES.pageSolverParams());
	}

	@Override
	@PreAuthorize("checkPermission('SolverParameters')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		return load(null, context, hibSession);
	}
	
	protected List<SolverParameterDef> getSolverParameterDefs(String[] filter, SessionContext context) {
		if (filter == null || filter[0] == null || filter[0].isEmpty() || filter[0].equals("null")) {
			context.getUser().setProperty("Admin.SolverParameterDef.FilterType", null);
			return SolverParameterGroupDAO.getInstance().getSession().createQuery(
					"from SolverParameterDef order by group.order, order, name", SolverParameterDef.class
					).list();
		} else {
			context.getUser().setProperty("Admin.SolverParameterDef.FilterType", filter[0]);
			return SolverParameterGroupDAO.getInstance().getSession().createQuery(
					"from SolverParameterDef where group.uniqueId = :group order by group.order, order, name", SolverParameterDef.class
					).setParameter("group", Long.valueOf(filter[0])).list();
		}
		
	}
	
	protected List<ListItem> getGroupTypes() {
		List<ListItem> types = new ArrayList<ListItem>();
		for (SolverParameterGroup group: SolverParameterGroupDAO.getInstance().getSession().createQuery(
				"from SolverParameterGroup order by order, name", SolverParameterGroup.class).list()) {
			String name = group.getDescription();
			switch (group.getSolverType()) {
			case COURSE:
				name = MESSAGES.solverSolverParameterGroupCourses(group.getDescription()); break;
			case EXAM:
				name = MESSAGES.solverSolverParameterGroupExams(group.getDescription()); break;
			case INSTRUCTOR:
				name = MESSAGES.solverSolverParameterGroupInstructors(group.getDescription()); break;
			case STUDENT:
				name = MESSAGES.solverSolverParameterGroupStudents(group.getDescription()); break;
			}
			types.add(new ListItem(group.getUniqueId().toString(), name));
		}
		return types;
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameters')")
	public SimpleEditInterface load(String[] filter, SessionContext context, Session hibSession) {
		Field name = null;
		Field groups = null;
		if (filter != null && filter[0] != null && !filter[0].isEmpty() && !filter[0].equals("null")) {
			name = new Field(MESSAGES.fieldReference(), FieldType.text, 300, 100, Flag.UNIQUE, Flag.NOT_EMPTY);
			groups = new Field(MESSAGES.fieldSolverParameterGroup(), FieldType.list, 200, getGroupTypes(), Flag.NOT_EMPTY, Flag.NO_LIST).withDefault(filter[0]);
		} else {
			name = new Field(MESSAGES.fieldReference(), FieldType.text, 300, 100, Flag.NOT_EMPTY);
			groups = new Field(MESSAGES.fieldSolverParameterGroup(), FieldType.list, 200, getGroupTypes(), Flag.NOT_EMPTY);
		}
		SimpleEditInterface data = new SimpleEditInterface(
				name,
				groups,
				new Field(MESSAGES.fieldDescription(), FieldType.text, 700, 1000, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldType(), FieldType.text, 700, 1000, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldDefault(), FieldType.text, 700, 2048),
				new Field(MESSAGES.fieldVisible(), FieldType.toggle, 40, Flag.DEFAULT_CHECKED)
				);
		data.setSaveOrder(false);
		data.setCanMoveUpAndDown(true);
		data.setAllowSort(false);
		
		for (SolverParameterDef def: getSolverParameterDefs(filter, context)) {
			Record r = data.addRecord(def.getUniqueId());
			r.setField(0, def.getName());
			r.setField(1, def.getGroup() == null ? "" : def.getGroup().getUniqueId().toString());
			r.setField(2, def.getDescription());
			r.setField(3, def.getType());
			r.setField(4, def.getDefault());
			r.setField(5, def.getVisible() ? "true" : "false");
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
	
	protected int nextOrd(Long groupId) {
		List<SolverParameterDef> defs = SolverParameterGroupDAO.getInstance().getSession().createQuery(
				"from SolverParameterDef where group.uniqueId = :group order by group.order, order, name", SolverParameterDef.class
				).setParameter("group", groupId).list();
		int idx = 0;
		t: while (true) {
			for (SolverParameterDef t: defs) {
				if (idx == t.getOrder()) { idx++; continue t; }
			}
			return idx;
		}
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameters')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		save(null, data, context, hibSession);
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameters')")
	public void save(String[] filter, SimpleEditInterface data, SessionContext context, Session hibSession) {
		Map<Long, Set<Integer>> g2ords = new HashMap<Long, Set<Integer>>();
		if (filter != null && filter[0] != null && !filter[0].isEmpty() && !filter[0].equals("null")) {
			for (SolverParameterDef def: SolverParameterGroupDAO.getInstance().getSession().createQuery(
					"from SolverParameterDef where group.uniqueId != :group order by group.order, order, name", SolverParameterDef.class
					).setParameter("group", Long.valueOf(filter[0])).list()) {
				Set<Integer> ords = g2ords.get(def.getGroup().getUniqueId());
				if (ords == null) {
					ords = new HashSet<Integer>();
					g2ords.put(def.getGroup().getUniqueId(), ords);
				}
				ords.add(def.getOrder());
			}
		}
		for (Record r: data.getRecords()) {
			if (r.isEmpty(data)) continue;
			Long gid = Long.valueOf(r.getField(1));
			Set<Integer> ords = g2ords.get(gid);
			if (ords == null) {
				ords = new HashSet<Integer>();
				g2ords.put(gid, ords);
			}
			r.setOrder(nextOrd(ords));
		}
		for (SolverParameterDef def: getSolverParameterDefs(filter, context)) {
			Record r = data.getRecord(def.getUniqueId());
			if (r == null)
				delete(def, context, hibSession);
			else
				update(def, r, r.getOrder(), context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, r.getOrder(), context, hibSession);		
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameters')")
	public void save(Record record, SessionContext context, Session hibSession) {
		save(record, null, context, hibSession);
	}
	
	protected void save(Record record, Integer order, SessionContext context, Session hibSession) {
		Long gid = Long.valueOf(record.getField(1));
		if (order == null) order = nextOrd(gid);
		SolverParameterDef def = new SolverParameterDef();
		def.setName(record.getField(0));
		def.setGroup(SolverParameterGroupDAO.getInstance().get(gid));
		def.setDescription(record.getField(2));
		def.setType(record.getField(3));
		def.setDefault(record.getField(4));
		def.setVisible(!"false".equalsIgnoreCase(record.getField(5)));
		def.setOrder(order);
		hibSession.persist(def);
		record.setUniqueId(def.getUniqueId());
		ChangeLog.addChange(hibSession,
				context,
				def,
				def.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(SolverParameterDef def, Record record, Integer order, SessionContext context, Session hibSession) {
		if (def == null) return;
		Long gid = Long.valueOf(record.getField(1));
		if (order == null) {
			if (gid.equals(def.getGroup().getUniqueId()))
				order = def.getOrder();
			else
				order = nextOrd(gid);
		}
		boolean changed = !ToolBox.equals(def.getName(), record.getField(0)) ||
				!ToolBox.equals(def.getGroup().getUniqueId().toString(), record.getField(1)) ||
				!ToolBox.equals(def.getDescription(), record.getField(2)) ||
				!ToolBox.equals(def.getType(), record.getField(3)) ||
				!ToolBox.equals(def.getDefault(), record.getField(4)) ||
				!ToolBox.equals(def.isVisible() ? "true" : "false", record.getField(5));
		if (!changed && ToolBox.equals(def.getOrder(), order)) return;
		def.setName(record.getField(0));
		def.setGroup(SolverParameterGroupDAO.getInstance().get(gid));
		def.setDescription(record.getField(2));
		def.setType(record.getField(3));
		def.setDefault(record.getField(4));
		def.setVisible(!"false".equalsIgnoreCase(record.getField(5)));
		def.setOrder(order);
		hibSession.merge(def);
		if (changed)
			ChangeLog.addChange(hibSession,
					context,
					def,
					def.getName(),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
	}

	@Override
	@PreAuthorize("checkPermission('SolverParameters')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(SolverParameterDefDAO.getInstance().get(record.getUniqueId(), hibSession), record, null, context, hibSession);
	}

	protected void delete(SolverParameterDef def, SessionContext context, Session hibSession) {
		if (def == null) return;
		ChangeLog.addChange(hibSession,
				context,
				def,
				def.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(def);
	}
	
	@Override
	@PreAuthorize("checkPermission('SolverParameters')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(SolverParameterDefDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('SolverParameters')")
	public Filter getFilter(SessionContext context, Session hibSession) {
		List<ListItem> types = getGroupTypes();
		types.add(0, new ListItem("", MESSAGES.itemAll()));
		SimpleEditInterface.Filter filter = new SimpleEditInterface.Filter(new Field(MESSAGES.fieldSolverParameterGroup(), FieldType.list, 100, types));
		String lastId = context.getUser().getProperty("Admin.SolverParameterDef.FilterType");
		if (lastId != null)
			filter.getDefaultValue().setField(0, lastId);
		return filter;
	}
}