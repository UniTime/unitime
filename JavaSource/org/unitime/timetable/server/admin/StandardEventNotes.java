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
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.StandardEventNoteDepartment;
import org.unitime.timetable.model.StandardEventNoteGlobal;
import org.unitime.timetable.model.StandardEventNoteSession;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StandardEventNoteDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=stdEvtNote]")
public class StandardEventNotes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageStandardEventNote(), MESSAGES.pageStandardEventNotes());
	}

	@Override
	@PreAuthorize("checkPermission('StandardEventNotes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		Long sessionId = context.getUser().getCurrentAcademicSessionId();
		List<ListItem> appliesTo = new ArrayList<ListItem>();
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 150, 20, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldNote(), FieldType.textarea, 50, 3, 1000, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldAppliesTo(), FieldType.list, 300, appliesTo, Flag.NOT_EMPTY)
				);
		data.setSortBy(2, 0, 1);

		boolean editGlobal = context.hasPermission(Right.StandardEventNotesGlobalEdit);
		if (editGlobal) appliesTo.add(new ListItem("_global", MESSAGES.levelGlobal()));
		for (StandardEventNote note: (List<StandardEventNote>)hibSession.createQuery("from StandardEventNoteGlobal order by reference").setCacheable(true).list()) {
			Record r = data.addRecord(note.getUniqueId());
			r.setField(0, note.getReference(), editGlobal);
			r.setField(1, note.getNote(), editGlobal);
			r.setField(2, editGlobal ? "_global" : MESSAGES.levelGlobal(), false);
			r.setDeletable(editGlobal);
		}
		
		data.setSessionId(sessionId);
		data.setSessionName(SessionDAO.getInstance().get(sessionId).getLabel());
		boolean editSession = context.hasPermission(sessionId, "Session", Right.StandardEventNotesSessionEdit);
		if (editSession)
			appliesTo.add(new ListItem("_session", data.getSessionName()));
		
		for (StandardEventNote note: (List<StandardEventNote>)hibSession.createQuery(
				"from StandardEventNoteSession where session.uniqueId = :sessionId order by reference").setLong("sessionId", sessionId).setCacheable(true).list()) {
			Record r = data.addRecord(note.getUniqueId());
			r.setField(0, note.getReference(), editSession);
			r.setField(1, note.getNote(), editSession);
			r.setField(2, (editSession ? "_session" : data.getSessionName()), false);
			r.setDeletable(editSession);
		}
		
		data.setEditable(editGlobal || editSession);
	
		for (Department department: Department.getUserDepartments(context.getUser())) {
			if (!department.isAllowEvents()) continue;
			boolean editDept = context.hasPermission(department, Right.StandardEventNotesDepartmentEdit);
			if (editDept) {
				appliesTo.add(new ListItem(department.getDeptCode(), department.getDeptCode() + " - " + department.getName()));
				data.setEditable(true);
			}
			
			for (StandardEventNote note: (List<StandardEventNote>)hibSession.createQuery(
					"from StandardEventNoteDepartment where department.uniqueId = :deptId order by reference").setLong("deptId", department.getUniqueId()).setCacheable(true).list()) {
				Record r = data.addRecord(note.getUniqueId());
				r.setField(0, note.getReference(), editDept);
				r.setField(1, note.getNote(), editDept);
				r.setField(2, editDept ? department.getDeptCode() : department.getDeptCode() + " - " + department.getName(), false);
				r.setDeletable(editDept);
			}
		}
		
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('StandardEventNotes')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		Long sessionId = context.getUser().getCurrentAcademicSessionId();
		
		if (context.hasPermission(Right.StandardEventNotesGlobalEdit))
			for (StandardEventNote note: (List<StandardEventNote>)hibSession.createQuery("from StandardEventNoteGlobal").setCacheable(true).list()) {
				Record r = data.getRecord(note.getUniqueId());
				if (r == null)
					delete(note, context, hibSession);
				else
					update(note, r, context, hibSession);
			}
		
		if (context.hasPermission(sessionId, "Session", Right.StandardEventNotesSessionEdit))
			for (StandardEventNote note: (List<StandardEventNote>)hibSession.createQuery("from StandardEventNoteSession where session.uniqueId = :sessionId").setLong("sessionId", sessionId).setCacheable(true).list()) {
				Record r = data.getRecord(note.getUniqueId());
				if (r == null)
					delete(note, context, hibSession);
				else
					update(note, r, context, hibSession);
			}
		
		for (Department department: Department.getUserDepartments(context.getUser())) {
			if (!department.isAllowEvents()) continue;
			if (!context.hasPermission(department, Right.StandardEventNotesDepartmentEdit)) continue;
			
			for (StandardEventNote note: (List<StandardEventNote>)hibSession.createQuery("from StandardEventNoteDepartment where department.uniqueId = :deptId").setLong("deptId", department.getUniqueId()).setCacheable(true).list()) {
				Record r = data.getRecord(note.getUniqueId());
				if (r == null)
					delete(note, context, hibSession);
				else
					update(note, r, context, hibSession);
			}
		}
		
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('StandardEventNotes')")
	public void save(Record record, SessionContext context, Session hibSession) {
		StandardEventNote note = null;
		if ("_global".equals(record.getField(2))) {
			note = new StandardEventNoteGlobal();
			context.checkPermission(Right.StandardEventNotesGlobalEdit);
		} else if ("_session".equals(record.getField(2))) {
			note = new StandardEventNoteSession();
			((StandardEventNoteSession)note).setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()));
			context.checkPermission(((StandardEventNoteSession)note).getSession(), Right.StandardEventNotesSessionEdit);
		} else {
			note = new StandardEventNoteDepartment();
			((StandardEventNoteDepartment)note).setDepartment(Department.findByDeptCode(record.getField(2), context.getUser().getCurrentAcademicSessionId()));
			context.checkPermission(((StandardEventNoteDepartment)note).getDepartment(), Right.StandardEventNotesDepartmentEdit);
		}
		note.setReference(record.getField(0));
		note.setNote(record.getField(1));
		record.setUniqueId((Long)hibSession.save(note));
		ChangeLog.addChange(hibSession,
				context,
				note,
				note.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(StandardEventNote note, Record record, SessionContext context, Session hibSession) {
		if (note == null) return;
		if (ToolBox.equals(note.getReference(), record.getField(0)) && ToolBox.equals(note.getNote(), record.getField(1))) return;
		note.setReference(record.getField(0));
		note.setNote(record.getField(1));
		hibSession.saveOrUpdate(note);
		ChangeLog.addChange(hibSession,
				context,
				note,
				note.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				note instanceof StandardEventNoteDepartment ? ((StandardEventNoteDepartment)note).getDepartment() : null);
	}

	@Override
	@PreAuthorize("checkPermission('StandardEventNotes')")
	public void update(Record record, SessionContext context, Session hibSession) {
		StandardEventNote note = StandardEventNoteDAO.getInstance().get(record.getUniqueId());
		if (note == null) return;
		if (note instanceof StandardEventNoteGlobal) {
			context.checkPermission(Right.StandardEventNotesGlobalEdit);
			update(note, record, context, hibSession);
		} else if (note instanceof StandardEventNoteSession) {
			context.checkPermission(((StandardEventNoteSession)note).getSession(), Right.StandardEventNotesSessionEdit);
			update(note, record, context, hibSession);
		} else if (note instanceof StandardEventNoteDepartment) {
			context.checkPermission(((StandardEventNoteDepartment)note).getDepartment(), Right.StandardEventNotesDepartmentEdit);
			update(note, record, context, hibSession);
		}
	}
	
	protected void delete(StandardEventNote note, SessionContext context, Session hibSession) {
		if (note == null) return;
		ChangeLog.addChange(hibSession,
				context,
				note,
				note.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				note instanceof StandardEventNoteDepartment ? ((StandardEventNoteDepartment)note).getDepartment() : null);
		hibSession.delete(note);
	}

	@Override
	@PreAuthorize("checkPermission('StandardEventNotes')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		StandardEventNote note = StandardEventNoteDAO.getInstance().get(record.getUniqueId());
		if (note == null) return;
		if (note instanceof StandardEventNoteGlobal) {
			context.checkPermission(Right.StandardEventNotesGlobalEdit);
			delete(note, context, hibSession);
		} else if (note instanceof StandardEventNoteSession) {
			context.checkPermission(((StandardEventNoteSession)note).getSession(), Right.StandardEventNotesSessionEdit);
			delete(note, context, hibSession);
		} else if (note instanceof StandardEventNoteDepartment) {
			context.checkPermission(((StandardEventNoteDepartment)note).getDepartment(), Right.StandardEventNotesDepartmentEdit);
			delete(note, context, hibSession);
		}
	}
}
