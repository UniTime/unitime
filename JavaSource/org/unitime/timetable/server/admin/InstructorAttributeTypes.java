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
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.dao.InstructorAttributeTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=attributeType]")
public class InstructorAttributeTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageInstructorAttributeType(), MESSAGES.pageInstructorAttributeTypes());
	}

	@Override
	@PreAuthorize("checkPermission('InstructorAttributeTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE),
				new Field(MESSAGES.fieldConjunctive(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldRequiredAttribute(), FieldType.toggle, 40)
				);
		data.setSortBy(2, 1);
		for (InstructorAttributeType atype: InstructorAttributeTypeDAO.getInstance().findAll()) {
			Record r = data.addRecord(atype.getUniqueId());
			r.setField(0, atype.getReference());
			r.setField(1, atype.getLabel());
			r.setField(2, atype.isConjunctive() ? "true" : "false");
			r.setField(3, atype.isRequired() ? "true" : "false");
			int used =
					((Number)hibSession.createQuery(
							"select count(a) from InstructorAttribute a where a.type.uniqueId = :uniqueId")
							.setLong("uniqueId", atype.getUniqueId()).uniqueResult()).intValue();
			r.setDeletable(used == 0);
		}
		data.setEditable(context.hasPermission(Right.InstructorAttributeTypeEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('InstructorAttributeTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (InstructorAttributeType type: InstructorAttributeTypeDAO.getInstance().findAll(hibSession)) {
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
	@PreAuthorize("checkPermission('InstructorAttributeTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		InstructorAttributeType type = new InstructorAttributeType();
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		type.setConjunctive("true".equals(record.getField(2)));
		type.setRequired("true".equals(record.getField(3)));
		record.setUniqueId((Long)hibSession.save(type));
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(InstructorAttributeType type, Record record, SessionContext context, Session hibSession) {
		if (type == null) return;
		if (ToolBox.equals(type.getReference(), record.getField(0)) &&
				ToolBox.equals(type.getLabel(), record.getField(1)) &&
				ToolBox.equals(type.isConjunctive(), "true".equals(record.getField(2))) &&
				ToolBox.equals(type.isRequired(), "true".equals(record.getField(3)))) return;
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		type.setConjunctive("true".equals(record.getField(2)));
		type.setRequired("true".equals(record.getField(3)));
		hibSession.saveOrUpdate(type);
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('InstructorAttributeTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(InstructorAttributeTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(InstructorAttributeType type, SessionContext context, Session hibSession) {
		if (type == null) return;
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(type);
	}

	@Override
	@PreAuthorize("checkPermission('InstructorAttributeTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(InstructorAttributeTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
