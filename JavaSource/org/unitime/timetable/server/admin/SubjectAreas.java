package org.unitime.timetable.server.admin;

import java.util.ArrayList;
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
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

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
		
		for (Department dept: deptList) {
			depts.add(new ListItem(dept.getUniqueId().toString(), dept.getLabel()));
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
			r.setField(3, area.getDepartment().getUniqueId().toString(), context.hasPermission(area.getUniqueId(), "SubjectArea", Right.SubjectAreaChangeDepartment));
			if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
				r.setField(4, area.getFundingDept() == null?"-1":area.getFundingDept().getUniqueId().toString());
			}			
			r.setDeletable(area.getExternalUniqueId() == null && !area.hasOfferedCourses());
		}
		data.setEditable(context.hasPermission(Right.SubjectAreaEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('SubjectAreas')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (SubjectArea area: SubjectAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(area.getUniqueId());
			if (r == null)
				delete(area, context, hibSession);
			else
				update(area, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('SubjectAreas')")
	public void save(Record record, SessionContext context, Session hibSession) {
		SubjectArea area = new SubjectArea();
		area.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		area.setSubjectAreaAbbreviation(record.getField(0));
		area.setTitle(record.getField(1));
		area.setExternalUniqueId(record.getField(2));
		for(String deptId: record.getValues(3)) {
			Department dept = DepartmentDAO.getInstance().get(Long.valueOf(deptId), hibSession);
			area.setDepartment(dept);
			dept.getSubjectAreas().add(area);
		}
		for(String deptId: record.getValues(4)) {
			Department fundDept = DepartmentDAO.getInstance().get(Long.valueOf(deptId), hibSession);
			if (fundDept != null) {
				area.setFundingDept(fundDept);
				fundDept.getSubjectAreas().add(area);
			}
		}
		record.setUniqueId((Long)hibSession.save(area));
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}	

	protected void update(SubjectArea area, Record record, SessionContext context, Session hibSession) {
		if (area==null) return;
		boolean changed = false;
		Department dept = null;
		Department fundDept = null;
		changed =
			changed ||
			!ToolBox.equals(area.getSubjectAreaAbbreviation(), record.getField(0)) ||
			!ToolBox.equals(area.getTitle(), record.getField(1)) ||
			!ToolBox.equals(area.getExternalUniqueId(), record.getField(2));
		for(String deptId: record.getValues(3)) {
			dept = DepartmentDAO.getInstance().get(Long.valueOf(deptId), hibSession);
			changed = changed || !ToolBox.equals(dept, area.getDepartment());
		}
		for(String deptId: record.getValues(4)) {
			fundDept = DepartmentDAO.getInstance().get(Long.valueOf(deptId), hibSession);
			changed = changed || !ToolBox.equals(fundDept, area.getFundingDept());
		}
		if (changed) {
			area.setSubjectAreaAbbreviation(record.getField(0));
			area.setTitle(record.getField(1));
			area.setExternalUniqueId(record.getField(2));
			if(!(dept == null)) {
				area.setDepartment(dept);
				dept.getSubjectAreas().add(area);
			}
			if (fundDept == null) {
				fundDept = area.getFundingDept();
				if (fundDept != null) {
					fundDept.getSubjectAreas().remove(area);
				} 
				area.setFundingDept(null);
			} else {
				area.setFundingDept(fundDept);
				fundDept.getSubjectAreas().add(area);
			}
			hibSession.saveOrUpdate(area);		
			ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
		}
	}
	
	@Override
	@PreAuthorize("checkPermission('SubjectAreas')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(SubjectAreaDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(SubjectArea area, SessionContext context, Session hibSession) {
		if (area==null) return;
		ChangeLog.addChange(hibSession,
				context,
				area,
				area.getSubjectAreaAbbreviation() + " " + area.getTitle(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(area);
	}
	
	@Override
	@PreAuthorize("checkPermission('SubjectAreas')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(SubjectAreaDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
