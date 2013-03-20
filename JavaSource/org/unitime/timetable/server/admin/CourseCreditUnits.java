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
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.CourseCreditUnitTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@Service("gwtAdminTable[type=creditUnit]")
public class CourseCreditUnits implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageCourseCreditUnit(), MESSAGES.pageCourseCreditUnits());
	}

	@Override
	@PreAuthorize("checkPermission('CourseCreditUnits')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE),
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 80, 10));
		data.setSortBy(0, 1, 2);
		for (CourseCreditUnitType credit: CourseCreditUnitTypeDAO.getInstance().findAll()) {
			int used =
				((Number)hibSession.createQuery(
						"select count(c) from CourseCreditUnitConfig c where c.creditUnitType.uniqueId = :uniqueId")
						.setLong("uniqueId", credit.getUniqueId()).uniqueResult()).intValue();
			Record r = data.addRecord(credit.getUniqueId(), used == 0);
			r.setField(0, credit.getReference());
			r.setField(1, credit.getLabel());
			r.setField(2, credit.getAbbreviation());
		}
		data.setEditable(context.hasPermission(Right.CourseCreditUnitEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('CourseCreditUnitEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (CourseCreditUnitType credit: CourseCreditUnitTypeDAO.getInstance().findAll()) {
			Record r = data.getRecord(credit.getUniqueId());
			if (r == null)
				delete(credit, context, hibSession);
			else
				update(credit, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('CourseCreditUnitEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		CourseCreditUnitType credit = new CourseCreditUnitType();
		credit.setReference(record.getField(0));
		credit.setLabel(record.getField(1));
		credit.setAbbreviation(record.getField(2));
		record.setUniqueId((Long)hibSession.save(credit));
		ChangeLog.addChange(hibSession,
				context,
				credit,
				credit.getReference() + " " + credit.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(CourseCreditUnitType credit, Record record, SessionContext context, Session hibSession) {
		if (credit == null) return;
		if (ToolBox.equals(credit.getReference(), record.getField(0)) &&
				ToolBox.equals(credit.getLabel(), record.getField(1)) &&
				ToolBox.equals(credit.getAbbreviation(), record.getField(2))) return;
		credit.setReference(record.getField(0));
		credit.setLabel(record.getField(1));
		credit.setAbbreviation(record.getField(2));
		hibSession.saveOrUpdate(credit);
		ChangeLog.addChange(hibSession,
				context,
				credit,
				credit.getReference() + " " + credit.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('CourseCreditUnitEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(CourseCreditUnitTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(CourseCreditUnitType credit, SessionContext context, Session hibSession) {
		if (credit == null) return;
		ChangeLog.addChange(hibSession,
				context,
				credit,
				credit.getReference() + " " + credit.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(credit);
	}

	@Override
	@PreAuthorize("checkPermission('CourseCreditUnitEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(CourseCreditUnitTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}	
}
