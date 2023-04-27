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
import org.unitime.timetable.model.Degree;
import org.unitime.timetable.model.dao.DegreeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=degree]")
public class Degrees implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageDegree(), MESSAGES.pageDegrees());
	}

	@Override
	@PreAuthorize("checkPermission('Degrees')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 240, 40, Flag.READ_ONLY),
				new Field(MESSAGES.fieldCode(), FieldType.text, 120, 20, Flag.NOT_EMPTY, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 360, 60, Flag.NOT_EMPTY));
		data.setSortBy(1,2);
		for (Degree degree: Degree.findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(degree.getUniqueId());
			r.setField(0, degree.getExternalUniqueId());
			r.setField(1, degree.getReference());
			r.setField(2, degree.getLabel());
			r.setDeletable(degree.getExternalUniqueId() == null);
		}
		data.setEditable(context.hasPermission(Right.DegreeEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('DegreeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (Degree degree: Degree.findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(degree.getUniqueId());
			if (r == null)
				delete(degree, context, hibSession);
			else
				update(degree, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('DegreeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		Degree degree = new Degree();
		degree.setExternalUniqueId(record.getField(0));
		degree.setReference(record.getField(1));
		degree.setLabel(record.getField(2));
		degree.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		hibSession.persist(degree);
		record.setUniqueId(degree.getUniqueId());
		ChangeLog.addChange(hibSession,
				context,
				degree,
				degree.getReference() + " " + degree.getLabel(),
				Source.SIMPLE_EDIT,
				Operation.CREATE,
				null,
				null);
	}

	protected void update(Degree degree, Record record, SessionContext context, Session hibSession) {
		if (degree == null) return;
		if (!ToolBox.equals(degree.getExternalUniqueId(), record.getField(0)) ||
				!ToolBox.equals(degree.getReference(), record.getField(1)) ||
				!ToolBox.equals(degree.getLabel(), record.getField(2))) {
			degree.setExternalUniqueId(record.getField(0));
			degree.setReference(record.getField(1));
			degree.setLabel(record.getField(2));
			hibSession.merge(degree);
			ChangeLog.addChange(hibSession,
					context,
					degree,
					degree.getReference() + " " + degree.getLabel(),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
		}
	}
	
	@Override
	@PreAuthorize("checkPermission('DegreeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(DegreeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(Degree degree, SessionContext context, Session hibSession) {
		if (degree == null) return;
		ChangeLog.addChange(hibSession,
				context,
				degree,
				degree.getReference() + " " + degree.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(degree);		
	}

	@Override
	@PreAuthorize("checkPermission('DegreeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(DegreeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
