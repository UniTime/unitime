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
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=preferences]")
public class PreferenceLevels implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pagePreferenceLevel(), MESSAGES.pagePreferenceLevels());
	}

	@Override
	@PreAuthorize("checkPermission('PreferenceLevels')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 40, 2, Flag.UNIQUE, Flag.READ_ONLY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 80, 10));
		data.setSortBy(0, 1, 2);
		for (PreferenceLevel pref: PreferenceLevelDAO.getInstance().findAll()) {
			Record r = data.addRecord(Long.valueOf(pref.getPrefId()), false);
			r.setField(0, pref.getPrefProlog(), false);
			r.setField(1, pref.getPrefName());
			r.setField(2, pref.getAbbreviation());
		}
		data.setEditable(context.hasPermission(Right.PreferenceLevelEdit));
		data.setAddable(false);
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('PreferenceLevelEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (PreferenceLevel pref: PreferenceLevelDAO.getInstance().findAll()) {
			Record r = data.getRecord(Long.valueOf(pref.getUniqueId()));
			if (r == null)
				delete(pref, context, hibSession);
			else
				update(pref, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('PreferenceLevelEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		throw new IllegalArgumentException("Insert operation is not supported for preference level.");
	}
	
	protected void update(PreferenceLevel pref, Record record, SessionContext context, Session hibSession) {
		if (pref == null) return;
		if (ToolBox.equals(pref.getPrefProlog(), record.getField(0)) &&
				ToolBox.equals(pref.getPrefName(), record.getField(1)) &&
				ToolBox.equals(pref.getAbbreviation(), record.getField(2))) return;
		pref.setPrefName(record.getField(1));
		pref.setPrefAbbv(record.getField(2));
		hibSession.saveOrUpdate(pref);
		ChangeLog.addChange(hibSession,
				context,
				pref,
				pref.getPrefProlog() + " " + pref.getPrefName(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('PreferenceLevelEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(PreferenceLevelDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(PreferenceLevel pref, SessionContext context, Session hibSession) {
		if (pref == null) return;
		throw new IllegalArgumentException("Delete operation is not supported for preference level.");
	}

	@Override
	@PreAuthorize("checkPermission('PreferenceLevelEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(PreferenceLevelDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}	
}
