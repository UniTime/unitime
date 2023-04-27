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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.web.util.HtmlUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;


/** 
 * @author Tomas Muller
 */
public class SolverGroupEditForm implements UniTimeForm {
	private static final long serialVersionUID = 150007237399797836L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String iOp;
    private Long iUniqueId;
    private String iName;
    private String iAbbv;
    private boolean iDepartmentsEditable;
    private List<Long> iDepartmentIds;
    private List<String> iDepartmentNames;
    private List<Boolean> iAssignedDepartments;
    private List<Long> iManagerIds;
    private List<String> iManagerNames;
    private List<Boolean> iAssignedManagers;
    
    public SolverGroupEditForm() {
        reset();
    }

	public void validate(UniTimeAction action) {
		Long sessionId = action.getSessionContext().getUser().getCurrentAcademicSessionId();

		if (iName==null || iName.trim().length()==0)
			action.addFieldError("form.name", MSG.errorRequiredField(MSG.fieldName()));
		else {
			try {
				SolverGroup g = SolverGroup.findBySessionIdName(sessionId, iName);
				if (g!=null && !g.getUniqueId().equals(iUniqueId))
					action.addFieldError("form.name", MSG.errorAlreadyExists(iName));
			} catch (Exception e) {
				action.addFieldError("form.name", e.getMessage());
			}
        }

		
		if(iAbbv==null || iAbbv.trim().length()==0)
			action.addFieldError("form.abbv", MSG.errorRequiredField(MSG.fieldAbbreviation()));
		else {
			try {
				SolverGroup g = SolverGroup.findBySessionIdAbbv(sessionId, iAbbv);
				if (g!=null && !g.getUniqueId().equals(iUniqueId))
					action.addFieldError("form.abbv", MSG.errorAlreadyExists(iAbbv));
			} catch (Exception e) {
				action.addFieldError("form.abbv", e.getMessage());
			}
        }
	}
	
	public void reset() {
		iOp = null; iUniqueId = null;
		iName = null; iAbbv = null;
		iAssignedDepartments = new ArrayList<Boolean>();
		iAssignedManagers = new ArrayList<Boolean>();
		iManagerIds = new ArrayList<Long>();
		iManagerNames = new ArrayList<String>();
		iDepartmentIds = new ArrayList<Long>();
		iDepartmentNames = new ArrayList<String>();
		iDepartmentsEditable = false;
	}
	
	public void load(SolverGroup group, Session session, final String nameFormat) throws Exception {
        Set<Department> departments = Department.findAllBeingUsed(session.getUniqueId());
        List<TimetableManager> managers = new ArrayList<TimetableManager>(TimetableManagerDAO.getInstance().findAll());
        Collections.sort(managers, new Comparator<TimetableManager>() {
			@Override
			public int compare(TimetableManager m1, TimetableManager m2) {
				int cmp = m1.getName(nameFormat).compareToIgnoreCase(m2.getName(nameFormat));
				if (cmp != 0) return cmp;
				return m1.compareTo(m2);
			}
		});
		iDepartmentIds.clear();
		iDepartmentNames.clear();
		iManagerIds.clear();
		iManagerNames.clear();
		iAssignedDepartments.clear();
		iAssignedManagers.clear();
		iDepartmentsEditable = true;
		if (group==null) {
			iUniqueId = Long.valueOf(-1);
			iName = null; iAbbv = null;
			iOp = "Save";
			for (Department d: departments) {
				if (d.getSolverGroup()==null) {
					iAssignedDepartments.add(Boolean.FALSE);
					iDepartmentIds.add(d.getUniqueId());
					iDepartmentNames.add(d.getLabel());
				} 
			}
			for (Iterator i=managers.iterator();i.hasNext();) {
				TimetableManager m = (TimetableManager)i.next();
				boolean hasSession = false;
				for (Department d: m.getDepartments()) {
					if (d.getSession().equals(session)) { hasSession = true; break; }
				}
				if (!hasSession) continue;
				iAssignedManagers.add(Boolean.FALSE);
				iManagerIds.add(m.getUniqueId());
	        	String deptStr = "";
	        	for (Department d: new TreeSet<Department>(m.departmentsForSession(session.getUniqueId()))) {
	        		deptStr += (deptStr.isEmpty() ? "" : ", ") + "<span title='"+HtmlUtils.htmlEscape(d.getLabel())+"'>"+d.getDeptCode()+"</span>";
	        	}
				iManagerNames.add(m.getName(nameFormat) + (deptStr.isEmpty() ? "" : " ("+deptStr+")"));
			}
		} else {
			iUniqueId = group.getUniqueId();
			iName = group.getName();
			iAbbv = group.getAbbv();
			iOp = "Update";
			for (Iterator i=departments.iterator();i.hasNext();) {
				Department d = (Department)i.next();
				if (group.equals(d.getSolverGroup())) {
					iAssignedDepartments.add(Boolean.TRUE);
					iDepartmentIds.add(d.getUniqueId());
					iDepartmentNames.add(d.getLabel());
				} else if (d.getSolverGroup()==null) {
					iAssignedDepartments.add(Boolean.FALSE);
					iDepartmentIds.add(d.getUniqueId());
					iDepartmentNames.add(d.getLabel());
				} else i.remove();
			}
			for (Iterator i=managers.iterator();i.hasNext();) {
				TimetableManager m = (TimetableManager)i.next();
				String deptStr = "";
	        	for (Department d: new TreeSet<Department>(m.departmentsForSession(session.getUniqueId()))) {
	        		deptStr += (deptStr.isEmpty() ? "" : ", ") + "<span title='"+HtmlUtils.htmlEscape(d.getLabel())+"'>"+d.getDeptCode()+"</span>";
	        	}
	        	boolean hasSession = group.getTimetableManagers().contains(m);
	        	if (!hasSession)
					for (Department d: m.getDepartments()) {
						if (d.getSession().equals(session)) { hasSession = true; break; }
					}
				if (!hasSession) continue;
				iManagerIds.add(m.getUniqueId());
				iManagerNames.add(m.getName(nameFormat) + (deptStr.isEmpty() ? "" : " ("+deptStr+")"));
				if (group.getTimetableManagers().contains(m)) {
					iAssignedManagers.add(Boolean.TRUE);
				} else {
					iAssignedManagers.add(Boolean.FALSE);
				}
			}
			if (!group.getSolutions().isEmpty())
				iDepartmentsEditable = false;
		}
	}
	
	public SolverGroup saveOrUpdate(org.hibernate.Session hibSession, SessionContext context) throws Exception {
		SolverGroup group = null;
		if (iUniqueId >= 0)
			group = SolverGroupDAO.getInstance().get(iUniqueId);
		if (group==null) {
			group = create(hibSession, context.getUser().getCurrentAcademicSessionId());
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    group, 
                    ChangeLog.Source.SOLVER_GROUP_EDIT, 
                    ChangeLog.Operation.CREATE, 
                    null, 
                    null);
        } else { 
			update(group, hibSession);
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    group, 
                    ChangeLog.Source.SOLVER_GROUP_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
        }
		return group;
	}
	
	public SolverGroup create(org.hibernate.Session hibSession, Long sessionId) throws Exception {
		SolverGroup group = new SolverGroup();
		group.setName(iName);
		group.setAbbv(iAbbv);
		Session session = SessionDAO.getInstance().get(sessionId, hibSession);
		group.setSession(session);
		Set<Department> newDepartments = new HashSet<Department>();
		for (int i = 0; i < iAssignedDepartments.size(); i++) {
			if (!iAssignedDepartments.get(i)) continue;
			Long deptId = iDepartmentIds.get(i);
			Department dept = DepartmentDAO.getInstance().get(deptId, hibSession);
			if (dept != null) newDepartments.add(dept);
		}
		group.setDepartments(newDepartments);
		Set<TimetableManager> newManagers = new HashSet<TimetableManager>();
		for (int i = 0; i < iAssignedManagers.size(); i++) {
			if (!iAssignedManagers.get(i)) continue;
			Long mgrId = iManagerIds.get(i);
			TimetableManager mgr = TimetableManagerDAO.getInstance().get(mgrId, hibSession);
			if (mgr != null) newManagers.add(mgr);
		}
		group.setTimetableManagers(newManagers);
		group.setSolutions(new HashSet<Solution>());
		hibSession.persist(group);
		for (Department d: newDepartments) {
			d.setSolverGroup(group);
			hibSession.merge(d);
		}
		for (TimetableManager mgr: newManagers) {
			mgr.getSolverGroups().add(group);
			hibSession.merge(mgr);
		}
		iUniqueId = group.getUniqueId();
		return group;
	}
	
	public void update(SolverGroup group, org.hibernate.Session hibSession) throws Exception {
		group.setName(iName);
		group.setAbbv(iAbbv);
		if (iDepartmentsEditable) {
			HashSet<Department> oldDepartments = new HashSet<Department>(group.getDepartments());
			for (int i = 0; i < iAssignedDepartments.size(); i++) {
				if (!iAssignedDepartments.get(i)) continue;
				Long deptId = iDepartmentIds.get(i);
				Department dept = DepartmentDAO.getInstance().get(deptId, hibSession);
				if (dept==null) continue;
				if (oldDepartments.remove(dept)) {
					//not changed -> do nothing
				} else {
					group.getDepartments().add(dept);
					dept.setSolverGroup(group);
					hibSession.merge(dept);
				}
			}
			for (Department dept: oldDepartments) {
				group.getDepartments().remove(dept);
				dept.setSolverGroup(null);
				hibSession.merge(dept);
			}
		}
		HashSet<TimetableManager> oldManagers = new HashSet<TimetableManager>(group.getTimetableManagers());
		for (int i = 0; i < iAssignedManagers.size(); i++) {
			if (!iAssignedManagers.get(i)) continue;
			Long mgrId = iManagerIds.get(i);
			TimetableManager mgr = TimetableManagerDAO.getInstance().get(mgrId, hibSession);
			if (mgr==null) continue;
			if (oldManagers.remove(mgr)) {
				//not changed -> do nothing
			} else {
				group.getTimetableManagers().add(mgr);
			}
		}
		for (TimetableManager mgr: oldManagers) {
			group.getTimetableManagers().remove(mgr);
			mgr.getSolverGroups().remove(group);
			hibSession.merge(mgr);
		}
		hibSession.merge(group);
		hibSession.flush();
		hibSession.refresh(group);
		for (Iterator i=group.getDepartments().iterator();i.hasNext();) 
			hibSession.refresh(i.next());
		for (Iterator i=group.getTimetableManagers().iterator();i.hasNext();) 
			hibSession.refresh(i.next());
	}
	
	public void delete(org.hibernate.Session hibSession, SessionContext context) throws Exception {
		if (iUniqueId < 0) return;
		if (!iDepartmentsEditable) return;
		SolverGroup group = (SolverGroupDAO.getInstance()).get(iUniqueId);
		if (group == null) return;
		for (Department dept: group.getDepartments()) {
			dept.setSolverGroup(null);
			hibSession.merge(dept);
		}
		for (TimetableManager mgr: group.getTimetableManagers()) {
			mgr.getSolverGroups().remove(group);
			hibSession.merge(mgr);
		}
        ChangeLog.addChange(
                hibSession, 
                context, 
                group, 
                ChangeLog.Source.SOLVER_GROUP_EDIT, 
                ChangeLog.Operation.DELETE, 
                null, 
                null);
		hibSession.remove(group);
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	public boolean getDepartmentsEditable() { return iDepartmentsEditable; }
	public void setDepartmentsEditable(boolean departmentsEditable) { iDepartmentsEditable = departmentsEditable; }

	public List<Long> getDepartmentIds() { return iDepartmentIds; }
	public void setDepartmentIds(List<Long> departmentIds) { iDepartmentIds = departmentIds; }
	public Long getDepartmentIds(int idx) { return iDepartmentIds.get(idx); }
	public void setDepartmentIds(int idx, Long departmentId) { iDepartmentIds.set(idx, departmentId); }

	public List<String> getDepartmentNames() { return iDepartmentNames; }
	public void setDepartmentNames(List<String> departmentNames) { iDepartmentNames = departmentNames; }
	public String getDepartmentNames(int idx) { return iDepartmentNames.get(idx); }
	public void setDepartmentNames(int idx, String departmentName) { iDepartmentNames.set(idx, departmentName); }
	
	public List<Boolean> getAssignedDepartments() { return iAssignedDepartments; }
	public void setAssignedDepartments(List<Boolean> assignedDepartments) { iAssignedDepartments = assignedDepartments; }
	public Boolean getAssignedDepartments(int idx) { return iAssignedDepartments.get(idx); }
	public void setAssignedDepartments(int idx, Boolean assignedDepartment) { iAssignedDepartments.set(idx, assignedDepartment); }
	
	public List<Long> getManagerIds() { return iManagerIds; }
	public void setManagerIds(List<Long> managerIds) { iManagerIds = managerIds; }
	public Long getManagerIds(int idx) { return iManagerIds.get(idx); }
	public void setManagerIds(int idx, Long managerId) { iManagerIds.set(idx, managerId); }
	
	public List<String> getManagerNames() { return iManagerNames; }
	public void setManagerNames(List<String> managerNames) { iManagerNames = managerNames; }
	public String getManagerNames(int idx) { return iManagerNames.get(idx); }
	public void setManagerNames(int idx, String managerName) { iManagerNames.set(idx, managerName); }
	
	public List<Boolean> getAssignedManagers() { return iAssignedManagers; }
	public void setAssignedManagers(List<Boolean> assignedManagers) { iAssignedManagers = assignedManagers; }
	public Boolean getAssignedManagers(int idx) { return iAssignedManagers.get(idx); }
	public void setAssignedManagers(int idx, Boolean assignedManagers) { iAssignedManagers.set(idx, assignedManagers); }
}