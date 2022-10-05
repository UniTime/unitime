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
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.dao.SettingsDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=defaultSettings]")
public class DefaultSettings implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageDefaultSetting(), MESSAGES.pageDefaultSettings());
	}

	@Override
	@PreAuthorize("checkPermission('SettingsAdmin')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 217, 30, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldDescription(), FieldType.text, 500, 100, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldDefaulValue(), FieldType.text, 217, 100, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldAllowedValues(), FieldType.textarea, 30, 5, 500, Flag.NOT_EMPTY)
				);
		data.setSortBy(1);
		
		for (Settings setting: SettingsDAO.getInstance().findAll()) {
			Record r = data.addRecord(setting.getUniqueId());
			boolean canEdit = context.hasPermission(Right.SettingsAdmin);
			r.setField(0, setting.getKey(), canEdit);
			r.setField(1, setting.getDescription(), canEdit);
			r.setField(2, setting.getDefaultValue(), canEdit);
			r.setField(3, (setting.getAllowedValues() == null ? null : setting.getAllowedValues().replace(',', '\n')), canEdit);
			r.setDeletable(context.hasPermission(Right.SettingsAdmin));
		}
		data.setAddable(context.hasPermission(Right.SettingsAdmin));
		return data;
	}
	
	@Override
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (Settings setting: SettingsDAO.getInstance().findAll()) {
			Record r = data.getRecord(setting.getUniqueId());
			if (r == null)
				delete(setting, context, hibSession);
			else
				update(setting, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}
	
	@Override
	public void save(Record record, SessionContext context, Session hibSession) {
		context.checkPermission(Right.SettingsAdmin);
		Settings setting = new Settings();
		setting.setKey(record.getField(0));
		setting.setDescription(record.getField(1));
		setting.setDefaultValue(record.getField(2));
		setting.setAllowedValues(record.getField(3).replace('\n', ','));
		record.setUniqueId((Long)hibSession.save(setting));
		ChangeLog.addChange(hibSession,
				context,
				setting,
				setting.getKey(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(Settings setting, Record record, SessionContext context, Session hibSession) {
		if (setting == null) return;
		context.checkPermission(Right.SettingsAdmin);
		if (ToolBox.equals(setting.getKey(), record.getField(0)) &&
			ToolBox.equals(setting.getDescription(), record.getField(1)) &&
			ToolBox.equals(setting.getDefaultValue(), record.getField(2)) &&
			ToolBox.equals(setting.getAllowedValues(), record.getField(3).replace('\n', ','))) return;
		setting.setKey(record.getField(0));
		setting.setDescription(record.getField(1));
		setting.setDefaultValue(record.getField(2));
		setting.setAllowedValues(record.getField(3).replace('\n', ','));
		hibSession.saveOrUpdate(setting);
		ChangeLog.addChange(hibSession,
				context,
				setting,
				setting.getKey(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	public void update(Record record, SessionContext context, Session hibSession) {
		update(SettingsDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(Settings setting, SessionContext context, Session hibSession) {
		if (setting == null) return;
		context.checkPermission(Right.SettingsAdmin);
		ChangeLog.addChange(hibSession,
				context,
				setting,
				setting.getKey(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(setting);
	}
	
	@Override
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(SettingsDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}