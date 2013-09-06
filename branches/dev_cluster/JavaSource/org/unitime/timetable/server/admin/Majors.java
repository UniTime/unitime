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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@Service("gwtAdminTable[type=major]")
public class Majors implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageMajor(), MESSAGES.pageMajors());
	}

	@Override
	@PreAuthorize("checkPermission('Majors')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> areas = new ArrayList<ListItem>();
		for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			areas.add(new ListItem(area.getUniqueId().toString(), area.getAcademicAreaAbbreviation() + " - " + area.getTitle()));
		}
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.READ_ONLY),
				new Field(MESSAGES.fieldCode(), FieldType.text, 80, 10, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 50, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldAcademicArea(), FieldType.list, 300, areas));
		data.setSortBy(3,1,2);
		for (PosMajor major: PosMajorDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(major.getUniqueId());
			r.setField(0, major.getExternalUniqueId());
			r.setField(1, major.getCode());
			r.setField(2, major.getName());
			r.setDeletable(major.getExternalUniqueId() == null);
			for (AcademicArea area: major.getAcademicAreas())
				r.setField(3, area.getUniqueId().toString());
		}
		data.setEditable(context.hasPermission(Right.MajorEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('MajorEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (PosMajor major: PosMajorDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(major.getUniqueId());
			if (r == null)
				delete(major, context, hibSession);
			else
				update(major, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('MajorEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		PosMajor major = new PosMajor();
		major.setExternalUniqueId(record.getField(0));
		major.setCode(record.getField(1));
		major.setName(record.getField(2));
		major.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		major.setAcademicAreas(new HashSet<AcademicArea>());
		for (String areaId: record.getValues(3)) {
			AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
			major.getAcademicAreas().add(area);
			area.getPosMajors().add(major);
		}
		record.setUniqueId((Long)hibSession.save(major));
		ChangeLog.addChange(hibSession,
				context,
				major,
				major.getCode() + " " + major.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}

	protected void update(PosMajor major, Record record, SessionContext context, Session hibSession) {
		if (major == null) return;
		boolean changed =
				!ToolBox.equals(major.getExternalUniqueId(), record.getField(0)) ||
				!ToolBox.equals(major.getCode(), record.getField(1)) ||
				!ToolBox.equals(major.getName(), record.getField(2));
			major.setExternalUniqueId(record.getField(0));
			major.setCode(record.getField(1));
			major.setName(record.getField(2));
			Set<AcademicArea> delete = new HashSet<AcademicArea>(major.getAcademicAreas());
			for (String areaId: record.getValues(3)) {
				AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
				if (!delete.remove(area)) {
					major.getAcademicAreas().add(area);
					area.getPosMajors().add(major);
					changed = true;
				}
			}
			for (AcademicArea area: delete) {
				major.getAcademicAreas().remove(area);
				area.getPosMajors().remove(major);
				changed = true;
			}
			hibSession.saveOrUpdate(major);
			if (changed)
				ChangeLog.addChange(hibSession,
						context,
						major,
						major.getCode() + " " + major.getName(),
						Source.SIMPLE_EDIT, 
						Operation.UPDATE,
						null,
						null);
	}
	
	@Override
	@PreAuthorize("checkPermission('MajorEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(PosMajorDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(PosMajor major, SessionContext context, Session hibSession) {
		if (major == null) return;
		ChangeLog.addChange(hibSession,
				context,
				major,
				major.getCode() + " " + major.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(major);		
	}

	@Override
	@PreAuthorize("checkPermission('MajorEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(PosMajorDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
	
	

}
