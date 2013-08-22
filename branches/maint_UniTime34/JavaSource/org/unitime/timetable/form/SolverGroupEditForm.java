/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.form;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * @author Tomas Muller
 */
public class SolverGroupEditForm extends ActionForm {
	private static final long serialVersionUID = 150007237399797836L;
	private String iOp;
    private Long iUniqueId;
    private String iName;
    private String iAbbv;
    private boolean iDepartmentsEditable;
    private List iDepartmentIds;
    private List iDepartmentNames;
    private List iAssignedDepartments;
    private List iManagerIds;
    private List iManagerNames;
    private List iAssignedManagers;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		
		try {
			Long sessionId = HttpSessionContext.getSessionContext(request.getSession().getServletContext()).getUser().getCurrentAcademicSessionId();

			if(iName==null || iName.trim().length()==0)
				errors.add("name", new ActionMessage("errors.required", ""));
			else {
				try {
					SolverGroup g = SolverGroup.findBySessionIdName(sessionId, iName);
					if (g!=null && !g.getUniqueId().equals(iUniqueId))
						errors.add("name", new ActionMessage("errors.exists", iName));
				} catch (Exception e) {
					errors.add("name", new ActionMessage("errors.generic", e.getMessage()));
				}
	        }

			
			if(iAbbv==null || iAbbv.trim().length()==0)
				errors.add("abbv", new ActionMessage("errors.required", ""));
			else {
				try {
					SolverGroup g = SolverGroup.findBySessionIdAbbv(sessionId, iAbbv);
					if (g!=null && !g.getUniqueId().equals(iUniqueId))
						errors.add("abbv", new ActionMessage("errors.exists", iAbbv));
				} catch (Exception e) {
					errors.add("abbv", new ActionMessage("errors.generic", e.getMessage()));
				}
	        }
			
		} catch (Exception e) {
			Debug.error(e);
			errors.add("name", new ActionMessage("errors.generic", e.getMessage()));
		}
		
		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; iUniqueId = null;
		iName = null; iAbbv = null;
		iAssignedDepartments = DynamicList.getInstance(new Vector(), iDynamicListFactory);
		iAssignedManagers = DynamicList.getInstance(new Vector(), iDynamicListFactory);
		iManagerIds = DynamicList.getInstance(new Vector(), iDynamicListFactory);
		iManagerNames = DynamicList.getInstance(new Vector(), iDynamicListFactory);
		iDepartmentIds = DynamicList.getInstance(new Vector(), iDynamicListFactory);
		iDepartmentNames = DynamicList.getInstance(new Vector(), iDynamicListFactory);
		iDepartmentsEditable = false;
	}
	
	public void load(SolverGroup group, Session session) throws Exception {
        Collection departments = Department.findAllBeingUsed(session.getUniqueId());
        Collection managers = new TreeSet((new TimetableManagerDAO()).findAll());
		iDepartmentIds.clear();
		iDepartmentNames.clear();
		iManagerIds.clear();
		iManagerNames.clear();
		iAssignedDepartments.clear();
		iAssignedManagers.clear();
		iDepartmentsEditable = true;
		if (group==null) {
			iUniqueId = new Long(-1);
			iName = null; iAbbv = null;
			iOp = "Save";
			for (Iterator i=departments.iterator();i.hasNext();) {
				Department d = (Department)i.next();
				if (d.getSolverGroup()==null) {
					iAssignedDepartments.add(Boolean.FALSE);
					iDepartmentIds.add(d.getUniqueId().toString());
					iDepartmentNames.add(d.getDeptCode()+" - "+d.getName());
				} 
			}
			for (Iterator i=managers.iterator();i.hasNext();) {
				TimetableManager m = (TimetableManager)i.next();
				iAssignedManagers.add(Boolean.FALSE);
				iManagerIds.add(m.getUniqueId().toString());
	        	String deptStr = "";
	        	for (Iterator j=(new TreeSet(m.departmentsForSession(session.getUniqueId()))).iterator();j.hasNext();) {
	        		Department d = (Department)j.next();
	        		deptStr += "<span title='"+d.getDeptCode()+" - "+d.getName()+"'>"+d.getDeptCode()+"</span>";
	        		if (j.hasNext()) deptStr += ", ";
	        	}
				iManagerNames.add(m.getName()+(deptStr.length()==0?"":" ("+deptStr+")"));
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
					iDepartmentIds.add(d.getUniqueId().toString());
					iDepartmentNames.add(d.getDeptCode()+" - "+d.getName());
				} else if (d.getSolverGroup()==null) {
					iAssignedDepartments.add(Boolean.FALSE);
					iDepartmentIds.add(d.getUniqueId().toString());
					iDepartmentNames.add(d.getDeptCode()+" - "+d.getName());
				} else i.remove();
			}
			for (Iterator i=managers.iterator();i.hasNext();) {
				TimetableManager m = (TimetableManager)i.next();
	        	String deptStr = "";
	        	for (Iterator j=(new TreeSet(m.departmentsForSession(session.getUniqueId()))).iterator();j.hasNext();) {
	        		Department d = (Department)j.next();
	        		deptStr += "<span title='"+d.getDeptCode()+" - "+d.getName()+"'>"+d.getDeptCode()+"</span>";
	        		if (j.hasNext()) deptStr += ", ";
	        	}
				iManagerIds.add(m.getUniqueId().toString());
				iManagerNames.add(m.getName()+(deptStr.length()==0?"":" ("+deptStr+")"));
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
		if (iUniqueId.longValue()>=0)
			group = (new SolverGroupDAO()).get(iUniqueId);
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
		Session session = (new SessionDAO()).get(sessionId, hibSession);
		group.setSession(session);
		Set newDepartments = new HashSet();
		for (int i=0;i<iAssignedDepartments.size();i++) {
			String deptId = (String)iDepartmentIds.get(i);
			Department dept = (new DepartmentDAO()).get(Long.valueOf(deptId), hibSession);
			if (dept==null) continue;
			String add = (String)iAssignedDepartments.get(i);
			if ("on".equals(add) || "true".equals(add) || "1".equals(add)) newDepartments.add(dept);
		}
		group.setDepartments(newDepartments);
		Set newManagers = new HashSet();
		for (int i=0;i<iAssignedManagers.size();i++) {
			String mgrId = (String)iManagerIds.get(i);
			TimetableManager mgr = (new TimetableManagerDAO()).get(Long.valueOf(mgrId), hibSession);
			if (mgr==null) continue;
			String add = (String)iAssignedManagers.get(i);
			if ("on".equals(add) || "true".equals(add) || "1".equals(add)) newManagers.add(mgr);
		}
		group.setTimetableManagers(newManagers);
		group.setSolutions(new HashSet<Solution>());
		hibSession.save(group);
		for (Iterator i=newDepartments.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			d.setSolverGroup(group);
			hibSession.saveOrUpdate(d);
		}
		for (Iterator i=newManagers.iterator();i.hasNext();) {
			TimetableManager mgr = (TimetableManager)i.next();
			mgr.getSolverGroups().add(group);
			hibSession.saveOrUpdate(mgr);
		}
		iUniqueId = group.getUniqueId();
		return group;
	}
	
	public void update(SolverGroup group, org.hibernate.Session hibSession) throws Exception {
		group.setName(iName);
		group.setAbbv(iAbbv);
		if (iDepartmentsEditable) {
			HashSet oldDepartments = new HashSet(group.getDepartments());
			for (int i=0;i<iAssignedDepartments.size();i++) {
				String deptId = (String)iDepartmentIds.get(i);
				Department dept = (new DepartmentDAO()).get(Long.valueOf(deptId), hibSession);
				if (dept==null) continue;
				String add = (String)iAssignedDepartments.get(i);
				if ("on".equals(add) || "true".equals(add) || "1".equals(add)) {
					if (oldDepartments.remove(dept)) {
						//not changed -> do nothing
					} else {
						group.getDepartments().add(dept);
						dept.setSolverGroup(group);
						hibSession.saveOrUpdate(dept);
					}
				}
			}
			for (Iterator i=oldDepartments.iterator();i.hasNext();) {
				Department dept = (Department)i.next();
				group.getDepartments().remove(dept);
				dept.setSolverGroup(null);
				hibSession.saveOrUpdate(dept);
			}
		}
		HashSet oldManagers = new HashSet(group.getTimetableManagers());
		for (int i=0;i<iAssignedManagers.size();i++) {
			String mgrId = (String)iManagerIds.get(i);
			TimetableManager mgr = (new TimetableManagerDAO()).get(Long.valueOf(mgrId), hibSession);
			if (mgr==null) continue;
			String add = (String)iAssignedManagers.get(i);
			if ("on".equals(add) || "true".equals(add) || "1".equals(add)) {
				if (oldManagers.remove(mgr)) {
					//not changed -> do nothing
				} else {
					group.getTimetableManagers().add(mgr);
				}
			}
		}
		for (Iterator i=oldManagers.iterator();i.hasNext();) {
			TimetableManager mgr = (TimetableManager)i.next();
			group.getTimetableManagers().remove(mgr);
			mgr.getSolverGroups().remove(group);
			hibSession.saveOrUpdate(mgr);
		}
		hibSession.saveOrUpdate(group);
		hibSession.flush();
		hibSession.refresh(group);
		for (Iterator i=group.getDepartments().iterator();i.hasNext();) 
			hibSession.refresh(i.next());
		for (Iterator i=group.getTimetableManagers().iterator();i.hasNext();) 
			hibSession.refresh(i.next());
	}
	
	public void delete(org.hibernate.Session hibSession, SessionContext context) throws Exception {
		if (iUniqueId.longValue()<0) return;
		if (!iDepartmentsEditable) return;
		SolverGroup group = (new SolverGroupDAO()).get(iUniqueId);
		if (group==null) return;
		for (Iterator i=group.getDepartments().iterator();i.hasNext();) {
			Department dept = (Department)i.next();
			dept.setSolverGroup(null);
			hibSession.saveOrUpdate(dept);
		}
		for (Iterator i=group.getTimetableManagers().iterator();i.hasNext();) {
			TimetableManager mgr = (TimetableManager)i.next();
			mgr.getSolverGroups().remove(group);
			hibSession.saveOrUpdate(mgr);
		}
        ChangeLog.addChange(
                hibSession, 
                context, 
                group, 
                ChangeLog.Source.SOLVER_GROUP_EDIT, 
                ChangeLog.Operation.DELETE, 
                null, 
                null);
		hibSession.delete(group);
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
	public List getDepartmentIds() { return iDepartmentIds; }
	public void setDepartmentIds(List departmentIds) { iDepartmentIds = departmentIds; }
	public List getDepartmentNames() { return iDepartmentNames; }
	public void setDepartmentNames(List departmentNames) { iDepartmentNames = departmentNames; }
	public List getAssignedDepartments() { return iAssignedDepartments; }
	public void setAssignedDepartments(List assignedDepartments) { iAssignedDepartments = assignedDepartments; }
	public List getManagerIds() { return iManagerIds; }
	public void setManagerIds(List managerIds) { iManagerIds = managerIds; }
	public List getManagerNames() { return iManagerNames; }
	public void setManagerNames(List managerNames) { iManagerNames = managerNames; }
	public List getAssignedManagers() { return iAssignedManagers; }
	public void setAssignedManagers(List assignedManagers) { iAssignedManagers = assignedManagers; }
	
    protected DynamicListObjectFactory iDynamicListFactory = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };
}
