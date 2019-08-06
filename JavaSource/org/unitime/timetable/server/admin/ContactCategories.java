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


import java.util.List;

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
import org.unitime.timetable.model.ContactCategory;
import org.unitime.timetable.model.dao.ContactCategoryDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=contactCategory]")
public class ContactCategories implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageContactCategory(), MESSAGES.pageContactCategories());
	}

	@Override
	@PreAuthorize("checkPermission('ContactCategories')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE),
				new Field(MESSAGES.fieldMessage(), FieldType.textarea, 100, 10, 2048),
				new Field(MESSAGES.fieldHasRole(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldEmailAddress(), FieldType.textarea, 50, 3, 1000)
				);
		data.setSortBy(1);
		for (ContactCategory cc: (List<ContactCategory>)ContactCategoryDAO.getInstance().getSession().createQuery(
				"from ContactCategory order by reference").list()) {
			Record r = data.addRecord(cc.getUniqueId());
			r.setField(0, cc.getReference());
			r.setField(1, cc.getLabel());
			r.setField(2, cc.getMessage());
			r.setField(3, cc.isHasRole() ? "true" : "false");
			r.setField(4, cc.getEmail());
		}
		data.setEditable(context.hasPermission(Right.ContactCategoryEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('ContactCategoryEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (ContactCategory cc: ContactCategoryDAO.getInstance().findAll(hibSession)) {
			Record r = data.getRecord(cc.getUniqueId());
			if (r == null)
				delete(cc, context, hibSession);
			else
				update(cc, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}

	@Override
	@PreAuthorize("checkPermission('ContactCategoryEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		ContactCategory cc = new ContactCategory();
		cc.setReference(record.getField(0));
		cc.setLabel(record.getField(1));
		cc.setMessage(record.getField(2));
		cc.setHasRole("true".equalsIgnoreCase(record.getField(3)));
		cc.setEmail(record.getField(4));
		record.setUniqueId((Long)hibSession.save(cc));
		ChangeLog.addChange(hibSession,
				context,
				cc,
				cc.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(ContactCategory cc, Record record, SessionContext context, Session hibSession) {
		if (cc == null) return;
		if (ToolBox.equals(cc.getReference(), record.getField(0)) &&
				ToolBox.equals(cc.getLabel(), record.getField(1)) &&
				ToolBox.equals(cc.getMessage(), record.getField(2)) &&
				ToolBox.equals(cc.getHasRole(), "true".equalsIgnoreCase(record.getField(3))) &&
				ToolBox.equals(cc.getEmail(), record.getField(4))) return;
		cc.setReference(record.getField(0));
		cc.setLabel(record.getField(1));
		cc.setMessage(record.getField(2));
		cc.setHasRole("true".equalsIgnoreCase(record.getField(3)));
		cc.setEmail(record.getField(4));
		hibSession.saveOrUpdate(cc);
		ChangeLog.addChange(hibSession,
				context,
				cc,
				cc.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('ContactCategoryEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(ContactCategoryDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(ContactCategory cc, SessionContext context, Session hibSession) {
		if (cc == null) return;
		ChangeLog.addChange(hibSession,
				context,
				cc,
				cc.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(cc);
	}
	
	@Override
	@PreAuthorize("checkPermission('ContactCategoryEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(ContactCategoryDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
