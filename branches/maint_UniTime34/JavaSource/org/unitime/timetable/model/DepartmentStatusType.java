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
package org.unitime.timetable.model;

import java.util.Iterator;
import java.util.TreeSet;

import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseDepartmentStatusType;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;


public class DepartmentStatusType extends BaseDepartmentStatusType implements Comparable{
	private static final long serialVersionUID = 1L;
	
	public static enum Status {
		ManagerView,
		ManagerEdit,
		ManagerLimitedEdit,
		OwnerView,
		OwnerEdit,
		OwnerLimitedEdit,
		Audit,
		Timetable,
		Commit,
		ExamView,
		ExamEdit,
		ExamTimetable,
		ReportExamsFinal,
		ReportExamsMidterm,
		ReportClasses,
		StudentsAssistant,
		StudentsPreRegister,
		StudentsOnline,
		TestSession,
		AllowNoRole,
		AllowRollForward,
		;
		
		public int toInt() { return 1 << ordinal(); }
		public boolean has(int rights) { return (rights & toInt()) == toInt(); }
	}
	
	public static enum Apply {
		Session,
		Department;
		
		public int toInt() { return 1 << ordinal(); }
	}
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public DepartmentStatusType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DepartmentStatusType (Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	/**
	 * 
	 */
	public static DepartmentStatusType findById(Integer uid) throws Exception {
		if (uid==null) return null;
		return (DepartmentStatusType)
			(new DepartmentStatusTypeDAO()).
			getSession().
			createCriteria(DepartmentStatusType.class).
			add(Restrictions.eq("uniqueId", uid)).
			uniqueResult();
	}
	
	public static TreeSet findAll() {
		return new TreeSet((new DepartmentStatusTypeDAO().findAll()));
	}
	
	public static TreeSet<DepartmentStatusType> findAll(int apply) {
		TreeSet<DepartmentStatusType> ret = findAll();
		for (Iterator<DepartmentStatusType> i=ret.iterator();i.hasNext();) {
			DepartmentStatusType t = i.next();
			if (!t.apply(apply)) i.remove();
		}
		return ret;
	}
	
	public static TreeSet<DepartmentStatusType> findAllForSession(boolean includeTestSessions) {
		TreeSet<DepartmentStatusType> ret = findAll(Apply.Session.toInt());
		if (!includeTestSessions)
			for (Iterator<DepartmentStatusType> i = ret.iterator(); i.hasNext(); )
				if (i.next().isTestSession()) i.remove();
		return ret;
	}

	public static TreeSet<DepartmentStatusType> findAllForDepartment() {
		return findAll(Apply.Department.toInt());
	}

	public static DepartmentStatusType findByRef(String ref) {
		if (ref==null) return null;
		return (DepartmentStatusType)
			(new DepartmentStatusTypeDAO()).
			getSession().
			createCriteria(DepartmentStatusType.class).
			add(Restrictions.eq("reference", ref)).
			uniqueResult();
	}

	public int compareTo(Object o) {
        if (o==null || !(o instanceof DepartmentStatusType)) return -1;
        DepartmentStatusType t = (DepartmentStatusType) o;
        int cmp = getOrd().compareTo(t.getOrd());
        if (cmp != 0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(t.getUniqueId() == null ? -1 : t.getUniqueId());
	}
	
	public boolean can(int operation) {
		return (getStatus().intValue() & operation) == operation;
	}
	
	public boolean can(Status status) {
		return can(status.toInt());
	}

	public boolean canManagerEdit() {
		return can(Status.ManagerEdit);
	}
	
	public boolean canManagerLimitedEdit() {
		return can(Status.ManagerLimitedEdit);
	}

	public boolean canManagerView() {
		return can(Status.ManagerView);
	}

	public boolean canOwnerEdit() {
		return can(Status.OwnerEdit);
	}

	public boolean canOwnerLimitedEdit() {
		return can(Status.OwnerLimitedEdit);
	}

	public boolean canOwnerView() {
		return can(Status.OwnerView);
	}

	public boolean canAudit() {
		return can(Status.Audit);
	}

	public boolean canTimetable() {
		return can(Status.Timetable);
	}

	public boolean canCommit() {
		return can(Status.Commit);
	}
	
	public boolean canExamView() {
	    return can(Status.ExamView);
	}
	
    public boolean canExamEdit() {
        return can(Status.ExamEdit);
    }

    public boolean canExamTimetable() {
        return can(Status.ExamTimetable);
    }
    
    public boolean canNoRoleReportExamFinal() {
        return can(Status.ReportExamsFinal);
    }

    public boolean canNoRoleReportExamMidterm() {
        return can(Status.ReportExamsMidterm);
    }

    public boolean canNoRoleReportClass() {
        return can(Status.ReportClasses);
    }
    
    public boolean canSectionAssistStudents() {
        return can(Status.StudentsAssistant);
    }
    
    public boolean canPreRegisterStudents() {
    	return can(Status.StudentsPreRegister);
    }

    public boolean canOnlineSectionStudents() {
    	return can(Status.StudentsOnline);
    }
    
    public boolean isTestSession() {
    	return can(Status.TestSession);
    }

    public boolean canNoRoleReportExam() {
        return canNoRoleReportExamFinal() || canNoRoleReportExamMidterm();
    }

    public boolean canNoRoleReport() {
        return canNoRoleReportClass() || canNoRoleReportExam();
    }
    
    public boolean apply(int apply) {
		return (getApply().intValue() & apply) == apply;
	}
	
	public boolean applySession() {
		return apply(Apply.Session.toInt());
	}

	public boolean applyDepartment() {
		return apply(Apply.Department.toInt());
	}
	
	public boolean isAllowNoRole() {
		return can(Status.AllowNoRole);
	}
	
	public boolean isAllowRollForward() {
		return can(Status.AllowRollForward);
	}
	
	/** Status is active when someone can edit, timetable or commit*/
	public boolean isActive() {
	    return canTimetable() || canCommit() || canManagerEdit() || canOwnerEdit() || canManagerLimitedEdit() || canOwnerLimitedEdit() || canExamEdit() || canExamTimetable() || canNoRoleReport();
	}
	
	public boolean canLockOfferings() {
		return !isTestSession() && (canOnlineSectionStudents() || canSectionAssistStudents());
	}
}
