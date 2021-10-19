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
import java.util.Collections;
import java.util.List;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Sean Justice
 */
@Service("gwtAdminTable[type=SubjectArea]")
public class SubjectAreas implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageSubjectArea(), MESSAGES.pageSubjectAreas());
	}

	@Override
	@PreAuthorize("checkPermission('SubjectAreas')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<Department> deptList = DepartmentDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId());
		List<ListItem> depts = new ArrayList<ListItem>();
		List<ListItem> fundDepts = new ArrayList<ListItem>();

		Collections.sort(deptList);
		for (Department dept: deptList) {
			if (dept.isExternalManager() == null || !dept.isExternalManager()) {
				depts.add(new ListItem(dept.getUniqueId().toString(), dept.getLabel()));
			}
		}
		fundDepts.add(new ListItem("-1", MESSAGES.noFundingDepartment()));
		for (Department dept: deptList) {
			if (dept.isExternalFundingDept() != null && dept.isExternalFundingDept()) {
				fundDepts.add(new ListItem(dept.getUniqueId().toString(), dept.getLabel()));
			}
		}
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldAbbv(), FieldType.text, 100, 40, Flag.UNIQUE),
				new Field(MESSAGES.fieldTitle(), FieldType.text, 120, 40, Flag.UNIQUE),
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.UNIQUE_IF_SET),
				new Field(MESSAGES.fieldDepartment(), FieldType.list, 300, depts, Flag.NOT_EMPTY),
				(ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()? new Field(MESSAGES.fieldFundingDepartment(), FieldType.list, 300, fundDepts) : new Field(MESSAGES.fieldFundingDepartment(), FieldType.list, 300, fundDepts, Flag.HIDDEN))
		);
		data.setSortBy(1,2);
		for (SubjectArea area: SubjectAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(area.getUniqueId());
			r.setField(0, area.getSubjectAreaAbbreviation());
			r.setField(1, area.getTitle());
			r.setField(2, area.getExternalUniqueId());
			r.setField(3, area.getDepartment().getUniqueId().toString(), context.hasPermission(area, Right.SubjectAreaChangeDepartment));
			if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
				r.setField(4, area.getFundingDept() == null?"-1":area.getFundingDept().getUniqueId().toString());
			}			
			r.setDeletable(context.hasPermission(area, Right.SubjectAreaDelete));
		}
		data.setAddable(context.hasPermission(Right.SubjectAreaAdd));
		data.setEditable(context.hasPermission(Right.SubjectAreaEdit) || context.hasPermission(Right.SubjectAreaAdd));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('SubjectAreas')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (SubjectArea area: SubjectAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(area.getUniqueId());
			if (r == null) {
				if (context.hasPermission(Right.SubjectAreaDelete)) {
					delete(area, context, hibSession);
				}
			} else {
				if (context.hasPermission(Right.SubjectAreaEdit)) {
					update(area, r, context, hibSession);
				}
			}
		}
		if (context.hasPermission(Right.SubjectAreaAdd)) {
			for (Record r: data.getNewRecords())			
				save(r, context, hibSession);
		}
	}

	@Override
	@PreAuthorize("checkPermission('SubjectAreaAdd')")
	public void save(Record record, SessionContext context, Session hibSession) {
		Department dept = DepartmentDAO.getInstance().get(Long.valueOf(record.getField(3)), hibSession);
		Department fundDept = (record.getField(4) == null || "-1".equals(record.getField(4)) ? null : DepartmentDAO.getInstance().get(Long.valueOf(record.getField(4)), hibSession));

		SubjectArea area = new SubjectArea();
		area.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		area.setSubjectAreaAbbreviation(record.getField(0));
		area.setTitle(record.getField(1));
		area.setExternalUniqueId(record.getField(2));
		area.setDepartment(dept);
		dept.getSubjectAreas().add(area);
		area.setFundingDept(fundDept);
		
		record.setUniqueId((Long)hibSession.save(area));
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SUBJECT_AREA_EDIT, 
				Operation.CREATE,
			    area,
			    area.getDepartment());

	}	

	protected void update(SubjectArea area, Record record, SessionContext context, Session hibSession) {
		if (area==null) return;
		boolean changed = false;
		boolean fundingEnabled = ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue();
		Department dept = DepartmentDAO.getInstance().get(Long.valueOf(record.getField(3)), hibSession);
		Department fundDept = DepartmentDAO.getInstance().get(Long.valueOf(record.getField(4)), hibSession);
		changed =
			changed ||
			!ToolBox.equals(area.getSubjectAreaAbbreviation(), record.getField(0)) ||
			!ToolBox.equals(area.getTitle(), record.getField(1)) ||
			!ToolBox.equals(area.getExternalUniqueId(), record.getField(2)) ||
			!ToolBox.equals(dept, area.getDepartment()) || 
			(fundingEnabled && !ToolBox.equals(fundDept, area.getFundingDept()));
		
		if (changed) {
			area.setSubjectAreaAbbreviation(record.getField(0));
			area.setTitle(record.getField(1));
			area.setExternalUniqueId(record.getField(2));
			if (!dept.equals(area.getDepartment())) {
				  area.getDepartment().getSubjectAreas().remove(area);
				  area.setDepartment(dept);
				  dept.getSubjectAreas().add(area);
				}
			if (fundingEnabled) {
				area.setFundingDept(fundDept);
			}
			hibSession.saveOrUpdate(area);
			ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SUBJECT_AREA_EDIT, 
				Operation.UPDATE,
				area,
				area.getDepartment());
		}
	}
	
	@Override
	@PreAuthorize("checkPermission(#record.uniqueId, 'SubjectAreaEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(SubjectAreaDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(SubjectArea area, SessionContext context, Session hibSession) {
		if (area==null) return;
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SUBJECT_AREA_EDIT, 
				Operation.DELETE,
				null,
			    area.getDepartment());
		hibSession.delete(area);
	}
	
	@Override
	@PreAuthorize("checkPermission(#record.uniqueId, 'SubjectAreaDelete')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(SubjectAreaDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
