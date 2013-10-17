/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=courseType]")
public class CourseTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageCourseType(), MESSAGES.pageCourseTypes());
	}

	@Override
	@PreAuthorize("checkPermission('CourseTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE)
				);
		data.setSortBy(1);
		for (CourseType ctype: CourseTypeDAO.getInstance().findAll()) {
			Record r = data.addRecord(ctype.getUniqueId());
			r.setField(0, ctype.getReference());
			r.setField(1, ctype.getLabel());
			int used =
					((Number)hibSession.createQuery(
							"select count(c) from CourseOffering c where c.courseType.uniqueId = :uniqueId")
							.setLong("uniqueId", ctype.getUniqueId()).uniqueResult()).intValue();
			r.setDeletable(used == 0);
		}
		data.setEditable(context.hasPermission(Right.CourseTypeEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('CourseTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (CourseType type: CourseTypeDAO.getInstance().findAll(hibSession)) {
			Record r = data.getRecord(type.getUniqueId());
			if (r == null)
				delete(type, context, hibSession);
			else
				update(type, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}

	@Override
	@PreAuthorize("checkPermission('CourseTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		CourseType type = new CourseType();
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		record.setUniqueId((Long)hibSession.save(type));
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(CourseType type, Record record, SessionContext context, Session hibSession) {
		if (type == null) return;
		if (ToolBox.equals(type.getReference(), record.getField(0)) &&
				ToolBox.equals(type.getLabel(), record.getField(1))) return;
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		hibSession.saveOrUpdate(type);
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('CourseTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(CourseTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(CourseType type, SessionContext context, Session hibSession) {
		if (type == null) return;
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(type);
	}
	
	@Override
	@PreAuthorize("checkPermission('CourseTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(CourseTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}
