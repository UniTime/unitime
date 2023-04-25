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
import org.unitime.timetable.model.Program;
import org.unitime.timetable.model.dao.ProgramDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=program]")
public class Programs implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageProgram(), MESSAGES.pagePrograms());
	}

	@Override
	@PreAuthorize("checkPermission('Programs')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 240, 40, Flag.READ_ONLY),
				new Field(MESSAGES.fieldCode(), FieldType.text, 120, 20, Flag.NOT_EMPTY, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 360, 60, Flag.NOT_EMPTY));
		data.setSortBy(1,2);
		for (Program program: Program.findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(program.getUniqueId());
			r.setField(0, program.getExternalUniqueId());
			r.setField(1, program.getReference());
			r.setField(2, program.getLabel());
			r.setDeletable(program.getExternalUniqueId() == null);
		}
		data.setEditable(context.hasPermission(Right.ProgramEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('ProgramEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (Program program: Program.findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(program.getUniqueId());
			if (r == null)
				delete(program, context, hibSession);
			else
				update(program, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('ProgramEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		Program program = new Program();
		program.setExternalUniqueId(record.getField(0));
		program.setReference(record.getField(1));
		program.setLabel(record.getField(2));
		program.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		record.setUniqueId((Long)hibSession.save(program));
		ChangeLog.addChange(hibSession,
				context,
				program,
				program.getReference() + " " + program.getLabel(),
				Source.SIMPLE_EDIT,
				Operation.CREATE,
				null,
				null);
	}

	protected void update(Program program, Record record, SessionContext context, Session hibSession) {
		if (program == null) return;
		if (!ToolBox.equals(program.getExternalUniqueId(), record.getField(0)) ||
				!ToolBox.equals(program.getReference(), record.getField(1)) ||
				!ToolBox.equals(program.getLabel(), record.getField(2))) {
			program.setExternalUniqueId(record.getField(0));
			program.setReference(record.getField(1));
			program.setLabel(record.getField(2));
			hibSession.saveOrUpdate(program);
			ChangeLog.addChange(hibSession,
					context,
					program,
					program.getReference() + " " + program.getLabel(),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
		}
	}
	
	@Override
	@PreAuthorize("checkPermission('ProgramEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(ProgramDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(Program program, SessionContext context, Session hibSession) {
		if (program == null) return;
		ChangeLog.addChange(hibSession,
				context,
				program,
				program.getReference() + " " + program.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(program);		
	}

	@Override
	@PreAuthorize("checkPermission('ProgramEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(ProgramDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
