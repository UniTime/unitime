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
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=classification]")
public class AcademicClassifications implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageAcademicClassification(), MESSAGES.pageAcademicClassifications());
	}

	@Override
	@PreAuthorize("checkPermission('AcademicClassifications')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.READ_ONLY),
				new Field(MESSAGES.fieldCode(), FieldType.text, 80, 10, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 500, 50, Flag.UNIQUE));
		data.setSortBy(1,2);
		for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(clasf.getUniqueId());
			r.setField(0, clasf.getExternalUniqueId());
			r.setField(1, clasf.getCode());
			r.setField(2, clasf.getName());
			r.setDeletable(clasf.getExternalUniqueId() == null);
		}
		data.setEditable(context.hasPermission(Right.AcademicClassificationEdit));
		return data;
	}
	
	@Override
	@PreAuthorize("checkPermission('AcademicClassificationEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(clasf.getUniqueId());
			if (r == null)
				delete(clasf, context, hibSession);
			else
				update(clasf, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicClassificationEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		AcademicClassification clasf = new AcademicClassification();
		clasf.setExternalUniqueId(record.getField(0));
		clasf.setCode(record.getField(1));
		clasf.setName(record.getField(2));
		clasf.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		record.setUniqueId((Long)hibSession.save(clasf));
		ChangeLog.addChange(hibSession,
				context,
				clasf,
				clasf.getCode() + " " + clasf.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}

	protected void update(AcademicClassification clasf, Record record, SessionContext context, Session hibSession) {
		if (clasf == null) return;
		if (ToolBox.equals(clasf.getExternalUniqueId(), record.getField(0)) &&
				ToolBox.equals(clasf.getCode(), record.getField(1)) &&
				ToolBox.equals(clasf.getName(), record.getField(2))) return;
			clasf.setExternalUniqueId(record.getField(0));
			clasf.setCode(record.getField(1));
			clasf.setName(record.getField(2));
			hibSession.saveOrUpdate(clasf);
			ChangeLog.addChange(hibSession,
					context,
					clasf,
					clasf.getCode() + " " + clasf.getName(),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);		
	}


	@Override
	@PreAuthorize("checkPermission('AcademicClassificationEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(AcademicClassificationDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(AcademicClassification clasf, SessionContext context, Session hibSession) {
		if (clasf == null) return;
		ChangeLog.addChange(hibSession,
				context,
				clasf,
				clasf.getCode() + " " + clasf.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(clasf);
	}
	
	@Override
	@PreAuthorize("checkPermission('AcademicClassificationEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(AcademicClassificationDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}

}
