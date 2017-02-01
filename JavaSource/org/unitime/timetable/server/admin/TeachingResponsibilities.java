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
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TeachingResponsibility.Option;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=teachingResponsibility]")
public class TeachingResponsibilities implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageTeachingResponsibility(), MESSAGES.pageTeachingResponsibilities());
	}

	@Override
	@PreAuthorize("checkPermission('TeachingResponsibilities')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 200, 40),
				new Field(MESSAGES.fieldInstructor(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldCoordinator(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldAuxiliaryNoReport(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldHideInEvents(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldNoExport(), FieldType.toggle, 40)
				);
		data.setSortBy(1);
		for (TeachingResponsibility responsibility: TeachingResponsibilityDAO.getInstance().findAll()) {
			Record r = data.addRecord(responsibility.getUniqueId());
			r.setField(0, responsibility.getReference());
			r.setField(1, responsibility.getLabel());
			r.setField(2, responsibility.getAbbreviation());
			r.setField(3, responsibility.isInstructor() ? "true" : "false");
			r.setField(4, responsibility.isCoordinator() ? "true" : "false");
			r.setField(5, responsibility.hasOption(Option.auxiliary) ? "true" : "false");
			r.setField(6, responsibility.hasOption(Option.noevents) ? "true" : "false");
			r.setField(7, responsibility.hasOption(Option.noexport) ? "true" : "false");
			r.setDeletable(!responsibility.isUsed());
		}
		data.setEditable(context.hasPermission(Right.TeachingResponsibilityEdit));
		return data;
	}
	
	@Override
	@PreAuthorize("checkPermission('TeachingResponsibilityEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (TeachingResponsibility responsibility: TeachingResponsibilityDAO.getInstance().findAll(hibSession)) {
			Record r = data.getRecord(responsibility.getUniqueId());
			if (r == null)
				delete(responsibility, context, hibSession);
			else
				update(responsibility, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}
	
	protected static Integer options(Record record) {
		int ret = 0;
		if ("true".equals(record.getField(5))) ret += Option.auxiliary.toggle();
		if ("true".equals(record.getField(6))) ret += Option.noevents.toggle();
		if ("true".equals(record.getField(7))) ret += Option.noexport.toggle();
		return ret;
	}

	@Override
	@PreAuthorize("checkPermission('TeachingResponsibilityEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		TeachingResponsibility responsibility = new TeachingResponsibility();
		responsibility.setReference(record.getField(0));
		responsibility.setLabel(record.getField(1));
		responsibility.setAbbreviation(record.getField(2));
		responsibility.setInstructor("true".equals(record.getField(3)));
		responsibility.setCoordinator("true".equals(record.getField(4)));
		responsibility.setOptions(options(record));
		record.setUniqueId((Long)hibSession.save(responsibility));
		ChangeLog.addChange(hibSession,
				context,
				responsibility,
				responsibility.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(TeachingResponsibility responsibility, Record record, SessionContext context, Session hibSession) {
		if (responsibility == null) return;
		if (ToolBox.equals(responsibility.getReference(), record.getField(0)) &&
			ToolBox.equals(responsibility.getLabel(), record.getField(1)) &&
			ToolBox.equals(responsibility.getAbbreviation(), record.getField(2)) &&
			ToolBox.equals(responsibility.isInstructor(), "true".equals(record.getField(3))) &&
			ToolBox.equals(responsibility.isCoordinator(), "true".equals(record.getField(4))) &&
			ToolBox.equals(responsibility.getOptions(), options(record))) return;
		responsibility.setReference(record.getField(0));
		responsibility.setLabel(record.getField(1));
		responsibility.setAbbreviation(record.getField(2));
		responsibility.setInstructor("true".equals(record.getField(3)));
		responsibility.setCoordinator("true".equals(record.getField(4)));
		responsibility.setOptions(options(record));
		hibSession.saveOrUpdate(responsibility);
		ChangeLog.addChange(hibSession,
				context,
				responsibility,
				responsibility.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('TeachingResponsibilityEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(TeachingResponsibilityDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(TeachingResponsibility responsibility, SessionContext context, Session hibSession) {
		if (responsibility == null) return;
		ChangeLog.addChange(hibSession,
				context,
				responsibility,
				responsibility.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(responsibility);
	}
	
	@Override
	@PreAuthorize("checkPermission('TeachingResponsibilityEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(TeachingResponsibilityDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}
