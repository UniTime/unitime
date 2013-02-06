/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.server.admin;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@Service("gwtAdminTable[type=sectioning]")
public class StudentSchedulingStatusTypes implements AdminTable {
	@Override
	public PageName name() {
		return new PageName("Student Scheduling Status Type");
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field("Abbreviation", FieldType.text, 160, 20, Flag.UNIQUE),
				new Field("Name", FieldType.text, 300, 60, Flag.UNIQUE),
				new Field("Access", FieldType.toggle, 40),
				new Field("Advisor", FieldType.toggle, 40),
				new Field("Email", FieldType.toggle, 40),
				new Field("Message", FieldType.text, 400, 200)
				);
		data.setSortBy(0, 1);
		for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll()) {
			Record r = data.addRecord(status.getUniqueId());
			r.setField(0, status.getReference());
			r.setField(1, status.getLabel());
			r.setField(2, status.hasOption(StudentSectioningStatus.Option.enabled) ? "true" : "false");
			r.setField(3, status.hasOption(StudentSectioningStatus.Option.advisor) ? "true" : "false");
			r.setField(4, status.hasOption(StudentSectioningStatus.Option.email) ? "true" : "false");
			r.setField(5, status.getMessage());
		}
		data.setEditable(context.hasPermission(Right.StudentSchedulingStatusTypeEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll()) {
			Record r = data.getRecord(status.getUniqueId());
			if (r == null)
				delete(status, context, hibSession);
			else
				update(status, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		StudentSectioningStatus status = new StudentSectioningStatus();
		int value = 0;
		if ("true".equals(record.getField(2))) value += StudentSectioningStatus.Option.enabled.toggle();
		if ("true".equals(record.getField(3))) value += StudentSectioningStatus.Option.advisor.toggle();
		if ("true".equals(record.getField(4))) value += StudentSectioningStatus.Option.email.toggle();
		status.setReference(record.getField(0));
		status.setLabel(record.getField(1));
		status.setStatus(value);
		status.setMessage(record.getField(5));
		record.setUniqueId((Long)hibSession.save(status));
		ChangeLog.addChange(hibSession,
				context,
				status,
				status.getReference() + " " + status.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(StudentSectioningStatus status, Record record, SessionContext context, Session hibSession) {
		if (status == null) return;
		int value = 0;
		if ("true".equals(record.getField(2))) value += StudentSectioningStatus.Option.enabled.toggle();
		if ("true".equals(record.getField(3))) value += StudentSectioningStatus.Option.advisor.toggle();
		if ("true".equals(record.getField(4))) value += StudentSectioningStatus.Option.email.toggle();
		boolean changed = 
			!ToolBox.equals(status.getReference(), record.getField(0)) ||
			!ToolBox.equals(status.getLabel(), record.getField(1)) ||
			!ToolBox.equals(status.getStatus(), value) ||
			!ToolBox.equals(status.getMessage(), record.getField(5));
		status.setReference(record.getField(0));
		status.setLabel(record.getField(1));
		status.setStatus(value);
		status.setMessage(record.getField(5));
		hibSession.saveOrUpdate(status);
		if (changed)
			ChangeLog.addChange(hibSession,
					context,
					status,
					status.getReference() + " " + status.getLabel(),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
	}

	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(StudentSectioningStatusDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(StudentSectioningStatus status, SessionContext context, Session hibSession) {
		if (status == null) return;
		ChangeLog.addChange(hibSession,
				context,
				status,
				status.getReference() + " " + status.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(status);
	}
	
	@Override
	@PreAuthorize("checkPermission('StudentSchedulingStatusTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(StudentSectioningStatusDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
