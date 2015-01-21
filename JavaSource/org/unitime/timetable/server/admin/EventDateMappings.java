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

import java.text.ParseException;
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
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.EventDateMappingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=dateMapping]")
public class EventDateMappings implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageEventDateMapping(), MESSAGES.pageEventDateMappings());
	}

	@Override
	@PreAuthorize("checkPermission('EventDateMappings')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldClassDate(), FieldType.date, 100, Flag.UNIQUE),
				new Field(MESSAGES.fieldEventDate(), FieldType.date, 100, Flag.UNIQUE),
				new Field(MESSAGES.fieldNote(), FieldType.text, 300, 1000)
				);
		data.setSortBy(0, 1);
		data.setSessionId(context.getUser().getCurrentAcademicSessionId());
		data.setSessionName(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()).getLabel());
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
		for (EventDateMapping mapping: EventDateMapping.findAll(context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(mapping.getUniqueId());
			r.setField(0, dateFormat.format(mapping.getClassDate()));
			r.setField(1, dateFormat.format(mapping.getEventDate()));
			r.setField(2, mapping.getNote());
		}
		data.setEditable(context.hasPermission(Right.EventDateMappingEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('EventDateMappingEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (EventDateMapping mapping: EventDateMapping.findAll(context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(mapping.getUniqueId());
			if (r == null)
				delete(mapping, context, hibSession);
			else
				update(mapping, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('EventDateMappingEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		try {
			Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
			EventDateMapping mapping = new EventDateMapping();
			mapping.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()));
			mapping.setClassDate(dateFormat.parse(record.getField(0)));
			mapping.setEventDate(dateFormat.parse(record.getField(1)));
			mapping.setNote(record.getField(2));
			record.setUniqueId((Long)hibSession.save(mapping));
			ChangeLog.addChange(hibSession,
					context,
					mapping,
					dateFormat.format(mapping.getClassDate()) + " &rarr; " + dateFormat.format(mapping.getEventDate()),
					Source.SIMPLE_EDIT, 
					Operation.CREATE,
					null,
					null);
		} catch (ParseException e) {
			throw new GwtRpcException(e.getMessage(), e);
		}
	}

	protected void update(EventDateMapping mapping, Record record, SessionContext context, Session hibSession) {
		try {
			if (mapping == null) return;
			Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
			if (ToolBox.equals(dateFormat.format(mapping.getClassDate()), record.getField(0)) &&
					ToolBox.equals(dateFormat.format(mapping.getEventDate()), record.getField(1)) &&
					ToolBox.equals(mapping.getNote(), record.getField(2))) return;
			mapping.setClassDate(dateFormat.parse(record.getField(0)));
			mapping.setEventDate(dateFormat.parse(record.getField(1)));
			mapping.setNote(record.getField(2));
			hibSession.saveOrUpdate(mapping);
			ChangeLog.addChange(hibSession,
					context,
					mapping,
					dateFormat.format(mapping.getClassDate()) + " &rarr; " + dateFormat.format(mapping.getEventDate()),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
		} catch (ParseException e) {
			throw new GwtRpcException(e.getMessage(), e);
		}
	}
	
	@Override
	@PreAuthorize("checkPermission('EventDateMappingEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(EventDateMappingDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(EventDateMapping mapping, SessionContext context, Session hibSession) {
		if (mapping == null) return;
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
		ChangeLog.addChange(hibSession,
				context,
				mapping,
				dateFormat.format(mapping.getClassDate()) + " &rarr; " + dateFormat.format(mapping.getEventDate()),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(mapping);
	}
	
	@Override
	@PreAuthorize("checkPermission('EventDateMappingEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(EventDateMappingDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
