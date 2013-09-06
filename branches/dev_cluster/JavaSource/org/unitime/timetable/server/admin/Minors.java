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
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.PosMinorDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@Service("gwtAdminTable[type=minor]")
public class Minors implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageMinor(), MESSAGES.pageMinors());
	}

	@Override
	@PreAuthorize("checkPermission('Minors')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> areas = new ArrayList<ListItem>();
		for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			areas.add(new ListItem(area.getUniqueId().toString(), area.getAcademicAreaAbbreviation() + " - " + area.getTitle()));
		}
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.READ_ONLY),
				new Field(MESSAGES.fieldCode(), FieldType.text, 80, 10, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 50, Flag.UNIQUE),
				new Field(MESSAGES.fieldAcademicArea(), FieldType.list, 300, areas));
		data.setSortBy(3,1,2);
		for (PosMinor minor: PosMinorDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(minor.getUniqueId());
			r.setField(0, minor.getExternalUniqueId());
			r.setField(1, minor.getCode());
			r.setField(2, minor.getName());
			for (AcademicArea area: minor.getAcademicAreas())
				r.setField(3, area.getUniqueId().toString());
			r.setDeletable(minor.getExternalUniqueId() == null);
		}
		data.setEditable(context.hasPermission(Right.MinorEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('MinorEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (PosMinor minor: PosMinorDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(minor.getUniqueId());
			if (r == null) {
				ChangeLog.addChange(hibSession,
						context,
						minor,
						minor.getCode() + " " + minor.getName(),
						Source.SIMPLE_EDIT, 
						Operation.DELETE,
						null,
						null);
				hibSession.delete(minor);
			} else {
				boolean changed =
					!ToolBox.equals(minor.getExternalUniqueId(), r.getField(0)) ||
					!ToolBox.equals(minor.getCode(), r.getField(1)) ||
					!ToolBox.equals(minor.getName(), r.getField(2));
				minor.setExternalUniqueId(r.getField(0));
				minor.setCode(r.getField(1));
				minor.setName(r.getField(2));
				Set<AcademicArea> delete = new HashSet<AcademicArea>(minor.getAcademicAreas());
				for (String areaId: r.getValues(3)) {
					AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
					if (!delete.remove(area)) {
						minor.getAcademicAreas().add(area);
						area.getPosMinors().add(minor);
						changed = true;
					}
				}
				for (AcademicArea area: delete) {
					minor.getAcademicAreas().remove(area);
					area.getPosMinors().remove(minor);
					changed = true;
				}
				hibSession.saveOrUpdate(minor);
				if (changed)
					ChangeLog.addChange(hibSession,
							context,
							minor,
							minor.getCode() + " " + minor.getName(),
							Source.SIMPLE_EDIT, 
							Operation.UPDATE,
							null,
							null);
			}
		}
		for (Record r: data.getNewRecords()) {
			PosMinor minor = new PosMinor();
			minor.setExternalUniqueId(r.getField(0));
			minor.setCode(r.getField(1));
			minor.setName(r.getField(2));
			minor.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
			minor.setAcademicAreas(new HashSet<AcademicArea>());
			for (String areaId: r.getValues(3)) {
				AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
				minor.getAcademicAreas().add(area);
				area.getPosMinors().add(minor);
			}
			r.setUniqueId((Long)hibSession.save(minor));
			ChangeLog.addChange(hibSession,
					context,
					minor,
					minor.getCode() + " " + minor.getName(),
					Source.SIMPLE_EDIT, 
					Operation.CREATE,
					null,
					null);
		}		
	}

	@Override
	@PreAuthorize("checkPermission('MinorEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		PosMinor minor = new PosMinor();
		minor.setExternalUniqueId(record.getField(0));
		minor.setCode(record.getField(1));
		minor.setName(record.getField(2));
		minor.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		minor.setAcademicAreas(new HashSet<AcademicArea>());
		for (String areaId: record.getValues(3)) {
			AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
			minor.getAcademicAreas().add(area);
			area.getPosMinors().add(minor);
		}
		record.setUniqueId((Long)hibSession.save(minor));
		ChangeLog.addChange(hibSession,
				context,
				minor,
				minor.getCode() + " " + minor.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(PosMinor minor, Record record, SessionContext context, Session hibSession) {
		if (minor == null) return;
		boolean changed =
				!ToolBox.equals(minor.getExternalUniqueId(), record.getField(0)) ||
				!ToolBox.equals(minor.getCode(), record.getField(1)) ||
				!ToolBox.equals(minor.getName(), record.getField(2));
			minor.setExternalUniqueId(record.getField(0));
			minor.setCode(record.getField(1));
			minor.setName(record.getField(2));
			Set<AcademicArea> delete = new HashSet<AcademicArea>(minor.getAcademicAreas());
			for (String areaId: record.getValues(3)) {
				AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
				if (!delete.remove(area)) {
					minor.getAcademicAreas().add(area);
					area.getPosMinors().add(minor);
					changed = true;
				}
			}
			for (AcademicArea area: delete) {
				minor.getAcademicAreas().remove(area);
				area.getPosMinors().remove(minor);
				changed = true;
			}
			hibSession.saveOrUpdate(minor);
			if (changed)
				ChangeLog.addChange(hibSession,
						context,
						minor,
						minor.getCode() + " " + minor.getName(),
						Source.SIMPLE_EDIT, 
						Operation.UPDATE,
						null,
						null);
	}

	@Override
	@PreAuthorize("checkPermission('MinorEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(PosMinorDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(PosMinor minor, SessionContext context, Session hibSession) {
		if (minor == null) return;
		ChangeLog.addChange(hibSession,
				context,
				minor,
				minor.getCode() + " " + minor.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(minor);		
	}

	@Override
	@PreAuthorize("checkPermission('MinorEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(PosMinorDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}

}
