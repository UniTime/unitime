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

import java.util.TreeSet;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.ExactTimeMinsDAO;
import org.unitime.timetable.security.SessionContext;

@Service("gwtAdminTable[type=exactTimes]")
public class ExactTimes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final CourseMessages COURSE = Localization.create(CourseMessages.class);

	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageExactTime(), MESSAGES.pageExactTimes());
	}

	@Override
	@PreAuthorize("checkPermission('ExactTimes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(COURSE.columnExactTimeMinuesRange().replace("<br>", "\n"), FieldType.text, 80, 10, Flag.READ_ONLY),
				new Field(COURSE.columnExactTimeSlotsPerMeeting().replace("<br>", "\n"), FieldType.number, 40, 3, Flag.NOT_EMPTY),
				new Field(COURSE.columnExactTimeBreakTime(), FieldType.number, 40, 3, Flag.NOT_EMPTY));
		data.setAddable(false);
		data.setEditable(true);
		int ord = 0;
		for (ExactTimeMins exact: new TreeSet<ExactTimeMins>(ExactTimeMinsDAO.getInstance().findAll())) {
			Record r = data.addRecord(exact.getUniqueId(), false);
			if (exact.getMinsPerMtgMin() == exact.getMinsPerMtgMax())
				r.setField(0, String.valueOf(exact.getMinsPerMtgMin()), false);
			else
				r.setField(0, exact.getMinsPerMtgMin() + " .. " + exact.getMinsPerMtgMax(), false);
			r.setField(1, exact.getNrSlots() == null ? "" : exact.getNrSlots().toString());
			r.setField(2, exact.getBreakTime() == null ? "0" : exact.getBreakTime().toString());
			r.setOrder(ord++);
		}
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('ExactTimes')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (ExactTimeMins exact: ExactTimeMinsDAO.getInstance().findAll()) {
			Record r = data.getRecord(Long.valueOf(exact.getUniqueId()));
			if (r == null)
				delete(exact, context, hibSession);
			else
				update(exact, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('ExactTimes')")
	public void save(Record record, SessionContext context, Session hibSession) {
		throw new IllegalArgumentException("Insert operation is not supported for exact times.");
	}
	
	protected void update(ExactTimeMins exact, Record record, SessionContext context, Session hibSession) {
		if (exact == null) return;
		if (ToolBox.equals(exact.getNrSlots() == null ? "" : exact.getNrSlots().toString(), record.getField(1)) &&
				ToolBox.equals(exact.getBreakTime() == null ? "0" : exact.getBreakTime().toString(), record.getField(2))) return;
		exact.setNrSlots(Integer.valueOf(record.getField(1)));
		exact.setBreakTime(Integer.valueOf(record.getField(2)));
		hibSession.merge(exact);
		ChangeLog.addChange(hibSession,
				context,
				exact,
				"Exact Time " + exact.getMinsPerMtgMin() + (exact.getMinsPerMtgMin() == exact.getMinsPerMtgMax() ? "" : " .. " + exact.getMinsPerMtgMax()),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('ExactTimes')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(ExactTimeMinsDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(ExactTimeMins pref, SessionContext context, Session hibSession) {
		if (pref == null) return;
		throw new IllegalArgumentException("Delete operation is not supported for exact times.");
	}

	@Override
	@PreAuthorize("checkPermission('ExactTimes')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(ExactTimeMinsDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}	

}
