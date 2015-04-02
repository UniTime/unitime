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
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;
import org.unitime.timetable.model.dao.ExamStatusDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=examStatus]")
public class ExaminationStatuses implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageExaminationStatus(), MESSAGES.pageExaminationStatuses());
	}

	@Override
	@PreAuthorize("checkPermission('ExaminationStatuses')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		Long sessionId = context.getUser().getCurrentAcademicSessionId();
		List<ListItem> managers = new ArrayList<ListItem>();
		NameFormat nf = NameFormat.fromReference(context.getUser().getProperty(UserProperty.NameFormat));
		for (TimetableManager m: (List<TimetableManager>)ExamStatusDAO.getInstance().getSession().createQuery(
				"select distinct m from TimetableManager m inner join m.departments d inner join m.managerRoles mr " +
				"where d.session.uniqueId = :sessionId and mr.role.enabled = true "+
				"and :prmExMgr in elements(mr.role.rights) and :prmAdmin not in elements(mr.role.rights) " +
				"order by m.lastName, m.firstName")
				.setLong("sessionId", sessionId)
				.setString("prmExMgr", Right.ExaminationSolver.name())
				.setString("prmAdmin", Right.StatusIndependent.name())
				.setCacheable(true).list()) {
			managers.add(new ListItem(m.getUniqueId().toString(), nf.format(m)));
		}
		List<ListItem> states = new ArrayList<ListItem>();
		states.add(new ListItem("-1", MESSAGES.examStatusDefault()));
		for (DepartmentStatusType t: DepartmentStatusType.findAll(DepartmentStatusType.Apply.ExamStatus.toInt())) {
			states.add(new ListItem(t.getUniqueId().toString(), t.getLabel()));
		}
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldType(), FieldType.text, 160, 60, Flag.READ_ONLY),
				new Field(MESSAGES.fieldStatus(), FieldType.list, 250, states),
				new Field(MESSAGES.fieldManager(), FieldType.multi, 300, managers)
				);
		data.setSortBy(0);
		data.setAddable(false);
		for (ExamType xtype: ExamTypeDAO.getInstance().findAll()) {
			Record r = data.addRecord(xtype.getUniqueId(), false);
			ExamStatus s = ExamStatus.findStatus(sessionId, xtype.getUniqueId());
			r.setField(0, xtype.getLabel());
			r.setField(1, s == null || s.getStatus() == null ? "-1" : s.getStatus().getUniqueId().toString());
			if (s != null)
				for (TimetableManager m: s.getManagers())
					r.addToField(2, m.getUniqueId().toString());
		}
		data.setEditable(context.hasPermission(Right.ExaminationStatusEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('ExaminationStatusEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (ExamType type: ExamTypeDAO.getInstance().findAll(hibSession)) {
			Record r = data.getRecord(type.getUniqueId());
			if (r != null)
				update(type, r, context, hibSession);
		}
	}

	@Override
	@PreAuthorize("checkPermission('ExaminationStatusEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {		
	}
	
	protected void update(ExamType type, Record record, SessionContext context, Session hibSession) {
		if (type == null) return;
		ExamStatus status = ExamStatus.findStatus(context.getUser().getCurrentAcademicSessionId(), type.getUniqueId());
		boolean changed = false;
		if (status == null) {
			status = new ExamStatus();
			status.setType(type);
			status.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()));
			status.setManagers(new HashSet<TimetableManager>());
			changed = true;
		}
		if (!ToolBox.equals(status.getStatus() == null ? "-1" : status.getStatus().getUniqueId().toString(), record.getField(1))) {
			status.setStatus("-1".equals(record.getField(1)) ? null : DepartmentStatusTypeDAO.getInstance().get(Long.valueOf(record.getField(1))));
			changed = true;
		}
		Set<TimetableManager> managers = new HashSet<TimetableManager>(status.getManagers());
		for (String id: record.getValues(2)) {
			TimetableManager m = TimetableManagerDAO.getInstance().get(Long.valueOf(id));
			if (!managers.remove(m)) {
				status.getManagers().add(m);
				changed = true;
			}
		}
		if (!managers.isEmpty()) {
			status.getManagers().removeAll(managers);
			changed = true;
		}
		if (!changed) return;
		hibSession.saveOrUpdate(status);
		ChangeLog.addChange(hibSession,
				context,
				status.getType(),
				status.getType().getLabel() + (status.getStatus() == null ? "" : ": " + status.getStatus().getLabel()),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('ExaminationStatusEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(ExamTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	@Override
	@PreAuthorize("checkPermission('ExaminationStatusEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
	}
}
