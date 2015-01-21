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
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=area]")
public class AcademicAreas implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageAcademicArea(), MESSAGES.pageAcademicAreas());
	}

	@Override
	@PreAuthorize("checkPermission('AcademicAreas')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.READ_ONLY),
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 80, 10, Flag.UNIQUE),
				new Field(MESSAGES.fieldTitle(), FieldType.text, 500, 100, Flag.NOT_EMPTY)
				);
		data.setSortBy(1,2,3);
		for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(area.getUniqueId());
			r.setField(0, area.getExternalUniqueId());
			r.setField(1, area.getAcademicAreaAbbreviation());
			r.setField(2, area.getTitle());
			r.setDeletable(area.getExternalUniqueId() == null);
		}
		data.setEditable(context.hasPermission(Right.AcademicAreaEdit));
		return data;
	}
	
	@Override
	@PreAuthorize("checkPermission('AcademicAreaEdit')")
	public void save(SimpleEditInterface data, SessionContext context, org.hibernate.Session hibSession) {
		for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(area.getUniqueId());
			if (r == null)
				delete(area, context, hibSession);
			else
				update(area, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}
	
	
	@Override
	@PreAuthorize("checkPermission('AcademicAreaEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		AcademicArea area = new AcademicArea();
		area.setExternalUniqueId(record.getField(0));
		area.setAcademicAreaAbbreviation(record.getField(1));
		area.setTitle(record.getField(2));
		area.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		record.setUniqueId((Long)hibSession.save(area));
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getAcademicAreaAbbreviation() + " " + area.getTitle(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(AcademicArea area, Record record, SessionContext context, Session hibSession) {
		if (area == null) return;
		if (ToolBox.equals(area.getExternalUniqueId(), record.getField(0)) &&
				ToolBox.equals(area.getAcademicAreaAbbreviation(), record.getField(1)) &&
				ToolBox.equals(area.getTitle(), record.getField(2))) return;
		area.setExternalUniqueId(record.getField(0));
		area.setAcademicAreaAbbreviation(record.getField(1));
		area.setTitle(record.getField(2));
		hibSession.saveOrUpdate(area);
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getAcademicAreaAbbreviation() + " " + area.getTitle(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);	
	}

	@Override
	@PreAuthorize("checkPermission('AcademicAreaEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(AcademicAreaDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(AcademicArea area, SessionContext context, Session hibSession) {
		if (area == null) return;
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getAcademicAreaAbbreviation() + " " + area.getTitle(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(area);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicAreaEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(AcademicAreaDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
