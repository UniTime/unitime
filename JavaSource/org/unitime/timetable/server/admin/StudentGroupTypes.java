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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.StudentGroupType;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.StudentGroupTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=stdgrtypes]")
public class StudentGroupTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageStudentGroupType(), MESSAGES.pageStudentGroupTypes());
	}

	@Override
	@PreAuthorize("checkPermission('StudentGroupTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> allow = new ArrayList<ListItem>();
		allow.add(new ListItem(String.valueOf(StudentGroupType.AllowDisabledSection.NotAllowed.ordinal()), MESSAGES.itemAllowDisabledSectionsNotAllowed()));
		allow.add(new ListItem(String.valueOf(StudentGroupType.AllowDisabledSection.WithGroupReservation.ordinal()), MESSAGES.itemAllowDisabledSectionsAllowedReservation()));
		allow.add(new ListItem(String.valueOf(StudentGroupType.AllowDisabledSection.AlwaysAllowed.ordinal()), MESSAGES.itemAllowDisabledSectionsAlwaysAllowed()));
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldCode(), FieldType.text, 150, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 400, 60, Flag.UNIQUE),
				new Field(MESSAGES.fieldKeepTogether(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldAllowDisabledSections(), FieldType.list, 100, allow, Flag.NOT_EMPTY)
				);
		data.setSortBy(1);
		Set<Long> used = new HashSet<Long>(
				StudentGroupTypeDAO.getInstance().getSession().createQuery(
						"select distinct g.type.uniqueId from StudentGroup g").setCacheable(true).list());
		for (StudentGroupType type: StudentGroupTypeDAO.getInstance().findAll()) {
			Record r = data.addRecord(type.getUniqueId());
			r.setField(0, type.getReference());
			r.setField(1, type.getLabel());
			r.setField(2, type.isKeepTogether() ? "true" : "false");
			r.setField(3, type.getAllowDisabled().toString());
			r.setDeletable(!used.contains(type.getUniqueId()));
		}
		data.setEditable(context.hasPermission(Right.StudentGroupTypeEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('StudentGroupTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (StudentGroupType type: StudentGroupTypeDAO.getInstance().findAll()) {
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
	@PreAuthorize("checkPermission('StudentGroupTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		StudentGroupType type = new StudentGroupType();
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		type.setKeepTogether("true".equals(record.getField(2)));
		type.setAllowDisabled(record.getField(3) == null ? 0 : Short.valueOf(record.getField(3)));
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
	
	protected void update(StudentGroupType type, Record record, SessionContext context, Session hibSession) {
		if (type == null) return;
		boolean changed = 
				!ToolBox.equals(type.getReference(), record.getField(0)) ||
				!ToolBox.equals(type.getLabel(), record.getField(1)) ||
				!ToolBox.equals(type.isKeepTogether(), "true".equals(record.getField(2))) || 
				!ToolBox.equals(type.getAllowDisabled().toString(), record.getField(3));
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		type.setKeepTogether("true".equals(record.getField(2)));
		type.setAllowDisabled(record.getField(3) == null ? 0 : Short.valueOf(record.getField(3)));
		hibSession.saveOrUpdate(type);
		if (changed)
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
	@PreAuthorize("checkPermission('StudentGroupTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(StudentGroupTypeDAO.getInstance().get(record.getUniqueId()), record, context, hibSession);
	}

	protected void delete(StudentGroupType type, SessionContext context, Session hibSession) {
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
	@PreAuthorize("checkPermission('StudentGroupTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(StudentGroupTypeDAO.getInstance().get(record.getUniqueId()), context, hibSession);	
	}

}
