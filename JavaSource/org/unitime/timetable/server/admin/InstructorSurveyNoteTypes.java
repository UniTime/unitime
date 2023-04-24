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
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.InstructorCourseRequirementType;
import org.unitime.timetable.model.dao.InstructorCourseRequirementTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=instrSurveyNoteTypes]")
public class InstructorSurveyNoteTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageInstructorSurveyNoteType(), MESSAGES.pageInstructorSurveyNoteTypes());
	}
	
	protected List<InstructorCourseRequirementType> getInstructorCourseRequirementTypes() {
		return InstructorCourseRequirementTypeDAO.getInstance().getSession().createQuery(
				"from InstructorCourseRequirementType order by sortOrder", InstructorCourseRequirementType.class).list();
	}

	@Override
	@PreAuthorize("checkPermission('InstructorSurveyNoteTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldName(), FieldType.textarea, 10, 2, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldDescription(), FieldType.textarea, 30, 2, 60, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldColumnWidth(), FieldType.number, 40, 10, Flag.NOT_EMPTY)
				);
		data.setSaveOrder(false);
		data.setCanMoveUpAndDown(true);
		data.setAllowSort(false);
		for (InstructorCourseRequirementType type: getInstructorCourseRequirementTypes()) {
			Record r = data.addRecord(type.getUniqueId());
			r.setField(0, type.getReference());
			r.setField(1, type.getLabel());
			r.setField(2, type.getLength().toString());
			r.setOrder(type.getSortOrder());
		}
		data.setEditable(context.hasPermission(Right.InstructorSurveyNoteTypeEdit));
		return data;
	}
	
	protected int nextOrd(Set<Integer> ords) {
		for (int i = 0; i < ords.size() + 1; i++) {
			if (!ords.contains(i)) {
				ords.add(i);
				return i;
			}
		}
		return ords.size();
	}
	
	protected int nextOrd() {
		List<InstructorCourseRequirementType> types = InstructorCourseRequirementTypeDAO.getInstance().findAll();
		int idx = 0;
		t: while (true) {
			for (InstructorCourseRequirementType t: types) {
				if (idx == t.getSortOrder()) { idx++; continue t; }
			}
			return idx;
		}
	}
	
	@Override
	@PreAuthorize("checkPermission('InstructorSurveyNoteTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		Set<Integer> ords = new HashSet<Integer>();
		for (Record r: data.getRecords()) {
			if (r.isEmpty(data)) continue;
			r.setOrder(nextOrd(ords));
		}
		for (InstructorCourseRequirementType type: InstructorCourseRequirementTypeDAO.getInstance().findAll()) {
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
	@PreAuthorize("checkPermission('InstructorSurveyNoteTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		InstructorCourseRequirementType type = new InstructorCourseRequirementType();
		if (record.getOrder() == null) record.setOrder(nextOrd());
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		type.setLength(Integer.parseInt(record.getField(2)));
		type.setSortOrder(record.getOrder());
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
	
	protected void update(InstructorCourseRequirementType type, Record record, SessionContext context, Session hibSession) {
		if (type == null) return;
		if (ToolBox.equals(type.getReference(), record.getField(0)) &&
			ToolBox.equals(type.getLabel(), record.getField(1)) &&
			ToolBox.equals(type.getLength().toString(), record.getField(2)) &&
			ToolBox.equals(type.getSortOrder(), record.getOrder())) return;
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		type.setLength(Integer.parseInt(record.getField(2)));
		type.setSortOrder(record.getOrder());
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
	@PreAuthorize("checkPermission('InstructorSurveyNoteTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(InstructorCourseRequirementTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(InstructorCourseRequirementType type, SessionContext context, Session hibSession) {
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
	@PreAuthorize("checkPermission('InstructorSurveyNoteTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(InstructorCourseRequirementTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
