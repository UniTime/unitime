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

import java.util.Date;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.dao.PointInTimeDataDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("gwtAdminTable[type=pitds]")
public class PointInTimeDataSnapshots implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pagePointInTimeDataSnapshot(), MESSAGES.pagePointInTimeDataSnapshots());
	}
	@Override
	@PreAuthorize("checkPermission('PointInTimeData')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldName(), FieldType.text, 200, 100),
				new Field(MESSAGES.fieldNote(), FieldType.textarea, 100, 1000),
				new Field(MESSAGES.fieldSnapshotTimestamp(), FieldType.date, 80, Flag.READ_ONLY),
				new Field(MESSAGES.fieldSavedSuccessfully(), FieldType.toggle, 40, Flag.READ_ONLY));
		data.setSortBy(1,2);
		data.setAddable(false);
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
		for (PointInTimeData pitd: PointInTimeData.findAllForSession(context.getUser().getCurrentAcademicSessionId(), hibSession)) {
			Record r = data.addRecord(pitd.getUniqueId(), true);
			r.setField(0, pitd.getName());
			r.setField(1, pitd.getNote());
			r.setField(2, dateFormat.format(pitd.getTimestamp()));
			r.setField(3, pitd.isSavedSuccessfully().booleanValue()?"true":"false");
			r.setDeletable(true);
		}
		data.setEditable(context.hasPermission(Right.PointInTimeDataEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('PointInTimeDataEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (PointInTimeData pointInTimeData: PointInTimeDataDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(pointInTimeData.getUniqueId());
			if (r == null)
				delete(pointInTimeData, context, hibSession);
			else 
				update(pointInTimeData, r, context, hibSession);
		}
	}
	
	protected void update(PointInTimeData pointInTimeData, Record record, SessionContext context, Session hibSession) {
		if (pointInTimeData == null) return;
		boolean changed = 
				!ToolBox.equals(pointInTimeData.getName(), record.getField(0)) ||
				!ToolBox.equals(pointInTimeData.getNote(), record.getField(1));
			pointInTimeData.setName(record.getField(0));
			pointInTimeData.setNote(record.getField(1));
			hibSession.saveOrUpdate(pointInTimeData);
			if (changed)
				ChangeLog.addChange(hibSession,
						context,
						pointInTimeData,
						pointInTimeData.getName() + " " + pointInTimeData.getNote(),
						Source.SIMPLE_EDIT, 
						Operation.UPDATE,
						null,
						null);
	}

	@Override
	@PreAuthorize("checkPermission('PointInTimeDataEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(PointInTimeDataDAO.getInstance().get(record.getUniqueId()), record, context, hibSession);
	}

	protected void delete(PointInTimeData pointInTimeData, SessionContext context, Session hibSession) {
		if (pointInTimeData == null) return;
		ChangeLog.addChange(hibSession,
				context,
				pointInTimeData,
				pointInTimeData.getName() + " " + pointInTimeData.getNote(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(pointInTimeData);
	}
	
	@Override
	@PreAuthorize("checkPermission('PointInTimeDataEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(PointInTimeDataDAO.getInstance().get(record.getUniqueId()), context, hibSession);		
	}
	
	@Override
	public void save(Record record, SessionContext context, Session hibSession) {
		throw new GwtRpcException(MESSAGES.errorOperationNotSupported());
	}

}
