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
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.EventServiceProviderDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=serviceProviders]")
public class ServiceProviders implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageServiceProvider(), MESSAGES.pageServiceProviders());
	}

	@Override
	@PreAuthorize("checkPermission('EventServiceProviders')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		Long sessionId = context.getUser().getCurrentAcademicSessionId();
		List<ListItem> appliesTo = new ArrayList<ListItem>();
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 160, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldMessage(), FieldType.textarea, 50, 3, 1000),
				new Field(MESSAGES.fieldEmailAddress(), FieldType.text, 300, 200),
				new Field(MESSAGES.fieldAppliesTo(), FieldType.list, 300, appliesTo, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldAllRooms(), FieldType.toggle, 40, Flag.DEFAULT_CHECKED),
				new Field(MESSAGES.fieldVisible(), FieldType.toggle, 40, Flag.DEFAULT_CHECKED)
				);
		data.setSortBy(1);
		
		boolean editGlobal = context.hasPermission(Right.EventServiceProviderEditGlobal);
		if (editGlobal) appliesTo.add(new ListItem("_global", MESSAGES.levelGlobal()));
		data.setSessionId(sessionId);
		data.setSessionName(SessionDAO.getInstance().get(sessionId).getLabel());
		boolean editSession = context.hasPermission(sessionId, "Session", Right.EventServiceProviderEditSession);
		if (editSession)
			appliesTo.add(new ListItem("_session", data.getSessionName()));
		data.setEditable(editGlobal || editSession);
		
		Set<Department> departments = Department.getUserDepartments(context.getUser());
		for (Department department: departments) {
			if (!department.isAllowEvents()) continue;
			boolean editDept = context.hasPermission(department, Right.EventServiceProviderEditDepartment);
			if (editDept) {
				appliesTo.add(new ListItem(department.getDeptCode(), department.getDeptCode() + " - " + department.getName()));
				data.setEditable(true);
			}
		}
		
		for (EventServiceProvider provider: (List<EventServiceProvider>)EventServiceProviderDAO.getInstance().getSession().createQuery(
				"from EventServiceProvider where session is null or session = :sessionId")
				.setLong("sessionId", sessionId).setCacheable(true).list()) {
			if (provider.getSession() == null) { // global
				Record r = data.addRecord(provider.getUniqueId());
				r.setField(0, provider.getReference(), editGlobal);
				r.setField(1, provider.getLabel(), editGlobal);
				r.setField(2, provider.getNote(), editGlobal);
				r.setField(3, provider.getEmail(), editGlobal);
				r.setField(4, editGlobal ? "_global" : MESSAGES.levelGlobal(), false);
				r.setField(5, provider.isAllRooms() ? "true" : "false", editGlobal);
				r.setField(6, provider.isVisible() ? "true" : "false", editGlobal);
				r.setDeletable(editGlobal && !provider.isUsed());
			} else if (provider.getDepartment() == null) { // session
				Record r = data.addRecord(provider.getUniqueId());
				r.setField(0, provider.getReference(), editSession);
				r.setField(1, provider.getLabel(), editSession);
				r.setField(2, provider.getNote(), editSession);
				r.setField(3, provider.getEmail(), editSession);
				r.setField(4, editSession ? "_session" : data.getSessionName(), false);
				r.setField(5, provider.isAllRooms() ? "true" : "false", editSession);
				r.setField(6, provider.isVisible() ? "true" : "false", editSession);
				r.setDeletable(editSession && !provider.isUsed());
			} else if (departments.contains(provider.getDepartment())) { // departmental
				boolean editDept = context.hasPermission(provider.getDepartment(), Right.EventServiceProviderEditDepartment);
				Record r = data.addRecord(provider.getUniqueId());
				r.setField(0, provider.getReference(), editDept);
				r.setField(1, provider.getLabel(), editDept);
				r.setField(2, provider.getNote(), editDept);
				r.setField(3, provider.getEmail(), editDept);
				r.setField(4, editDept ? provider.getDepartment().getDeptCode() : provider.getDepartment().getDeptCode() + " - " + provider.getDepartment().getName(), false);
				r.setField(5, provider.isAllRooms() ? "true" : "false", editDept);
				r.setField(6, provider.isVisible() ? "true" : "false", editDept);
				r.setDeletable(editDept && !provider.isUsed());
			}
		}
		return data;
	}
	
	@Override
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (EventServiceProvider provider: (List<EventServiceProvider>)EventServiceProviderDAO.getInstance().getSession().createQuery(
				"from EventServiceProvider where session is null or session = :sessionId")
				.setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).setCacheable(true).list()) {
			if (provider.getSession() == null) { // global
				if (!context.hasPermission(Right.EventServiceProviderEditGlobal)) continue;
			} else if (provider.getDepartment() == null) { // session
				if (!context.hasPermission(context.getUser().getCurrentAcademicSessionId(), "Session", Right.EventServiceProviderEditSession)) continue;
			} else {
				if (!context.hasPermission(provider.getDepartment(), Right.EventServiceProviderEditDepartment)) continue;
			}
			Record r = data.getRecord(provider.getUniqueId());
			if (r == null)
				delete(provider, context, hibSession);
			else
				update(provider, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}
	
	@Override
	public void save(Record record, SessionContext context, Session hibSession) {
		EventServiceProvider provider = new EventServiceProvider();
		provider.setReference(record.getField(0));
		provider.setLabel(record.getField(1));
		provider.setNote(record.getField(2));
		provider.setEmail(record.getField(3));
		provider.setAllRooms(!"false".equalsIgnoreCase(record.getField(5)));
		provider.setVisible(!"false".equalsIgnoreCase(record.getField(6)));
		if ("_global".equals(record.getField(4))) {
			context.checkPermission(Right.EventServiceProviderEditGlobal);
		} else if ("_session".equals(record.getField(4))) {
			provider.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()));
			context.checkPermission(provider.getSession(), Right.EventServiceProviderEditSession);
		} else {
			provider.setDepartment(Department.findByDeptCode(record.getField(4), context.getUser().getCurrentAcademicSessionId()));
			provider.setSession(provider.getDepartment().getSession());
			context.checkPermission(provider.getDepartment(), Right.EventServiceProviderEditDepartment);
		}
		record.setUniqueId((Long)hibSession.save(provider));
		ChangeLog.addChange(hibSession,
				context,
				provider,
				provider.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(EventServiceProvider provider, Record record, SessionContext context, Session hibSession) {
		if (provider == null) return;
		if (ToolBox.equals(provider.getReference(), record.getField(0)) &&
			ToolBox.equals(provider.getLabel(), record.getField(1)) &&
			ToolBox.equals(provider.getNote(), record.getField(2)) &&
			ToolBox.equals(provider.getEmail(), record.getField(3)) &&
			ToolBox.equals(provider.isAllRooms(), "true".equalsIgnoreCase(record.getField(5))) &&
			ToolBox.equals(provider.isAllRooms(), "true".equalsIgnoreCase(record.getField(6)))) return;
		provider.setReference(record.getField(0));
		provider.setLabel(record.getField(1));
		provider.setNote(record.getField(2));
		provider.setEmail(record.getField(3));
		provider.setAllRooms("true".equalsIgnoreCase(record.getField(5)));
		provider.setVisible("true".equalsIgnoreCase(record.getField(6)));
		hibSession.saveOrUpdate(provider);
		ChangeLog.addChange(hibSession,
				context,
				provider,
				provider.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	public void update(Record record, SessionContext context, Session hibSession) {
		update(EventServiceProviderDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(EventServiceProvider provider, SessionContext context, Session hibSession) {
		if (provider == null) return;
		ChangeLog.addChange(hibSession,
				context,
				provider,
				provider.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(provider);
	}
	
	@Override
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(EventServiceProviderDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}
