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
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.ClassDurationTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=durationType]")
public class DurationTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageDurationType(), MESSAGES.pageDurationTypes());
	}

	@Override
	@PreAuthorize("checkPermission('DurationTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 160, 20, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldImplementation(), FieldType.text, 600, 255, Flag.NOT_EMPTY, Flag.HIDDEN),
				new Field(MESSAGES.fieldParameters(), FieldType.text, 300, 200),
				new Field(MESSAGES.fieldVisible(), FieldType.toggle, 40)
				);
		data.setSortBy(2);
		data.setAddable(false);
		for (ClassDurationType type: ClassDurationTypeDAO.getInstance().findAll()) {
			Record r = data.addRecord(type.getUniqueId(), false);
			r.setField(0, type.getReference(), false);
			r.setField(1, type.getAbbreviation());
			r.setField(2, type.getLabel());
			r.setField(3, type.getImplementation(), false);
			r.setField(4, type.getParameter());
			r.setField(5, type.isVisible() ? "true" : "false");
		}
		data.setEditable(context.hasPermission(Right.DurationTypeEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('DurationTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (ClassDurationType type: ClassDurationTypeDAO.getInstance().findAll()) {
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
	@PreAuthorize("checkPermission('DurationTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		ClassDurationType type = new ClassDurationType();
		type.setReference(record.getField(0));
		type.setAbbreviation(record.getField(1));
		type.setLabel(record.getField(2));
		type.setImplementation(record.getField(3));
		type.setParameter(record.getField(4));
		type.setVisible("true".equalsIgnoreCase(record.getField(5)));
		record.setUniqueId((Long)hibSession.save(type));
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference() + " " + type.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(ClassDurationType type, Record record, SessionContext context, Session hibSession) {
		if (type == null) return;
		if (ToolBox.equals(type.getReference(), record.getField(0)) &&
				ToolBox.equals(type.getAbbreviation(), record.getField(1)) &&
				ToolBox.equals(type.getLabel(), record.getField(2)) &&
				ToolBox.equals(type.getImplementation(), record.getField(3)) &&
				ToolBox.equals(type.getParameter(), record.getField(4)) &&
				ToolBox.equals(type.isVisible(), "true".equalsIgnoreCase(record.getField(5)))) return;
		type.setReference(record.getField(0));
		type.setAbbreviation(record.getField(1));
		type.setLabel(record.getField(2));
		type.setImplementation(record.getField(3));
		type.setParameter(record.getField(4));
		type.setVisible("true".equalsIgnoreCase(record.getField(5)));
		hibSession.saveOrUpdate(type);
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference() + " " + type.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('DurationTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(ClassDurationTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(ClassDurationType type, SessionContext context, Session hibSession) {
		if (type == null) return;
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference() + " " + type.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(type);
	}

	@Override
	@PreAuthorize("checkPermission('DurationTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(ClassDurationTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
