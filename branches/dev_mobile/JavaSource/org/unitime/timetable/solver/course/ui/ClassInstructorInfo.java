/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2009 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;

import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.dao.ClassInstructorDAO;

/**
 * @author Tomas Muller
 */
public class ClassInstructorInfo implements Serializable, Comparable<ClassInstructorInfo> {
	private static final long serialVersionUID = 8576391767085203451L;
	protected Long iId;
    protected String iExternalUniqueId = null;
    protected String iName = null;
    protected boolean iIsLead = false;
    protected int iShare = 0;
    protected transient ClassInstructor iInstructor;
    public ClassInstructorInfo(ClassInstructor instructor) {
        iId = instructor.getUniqueId();
        iName = instructor.getInstructor().getNameLastFirst();
        iIsLead = instructor.isLead();
        iShare = instructor.getPercentShare();
        iExternalUniqueId = instructor.getInstructor().getExternalUniqueId();
        iInstructor = instructor;
    }
    public Long getId() { return iId; }
    public String getName() { return iName; }
    public boolean isLead() { return iIsLead; }
    public int getShare() { return iShare; }
    public String getExternalUniqueId() {
        if (iExternalUniqueId==null && iInstructor==null)
            iExternalUniqueId = getInstructor().getInstructor().getExternalUniqueId();
        return iExternalUniqueId; 
    }
    public ClassInstructor getInstructor() {
        if (iInstructor==null)
            iInstructor = ClassInstructorDAO.getInstance().get(getId());
        return iInstructor;
    }
    public ClassInstructor getInstructor(org.hibernate.Session hibSession) {
        return ClassInstructorDAO.getInstance().get(getId(), hibSession);
    }
    public int compareTo(ClassInstructorInfo i) {
        int cmp = getName().compareTo(i.getName());
        if (cmp!=0) return cmp;
        return getId().compareTo(i.getId());
    }
    public int hashCode() {
        if (getExternalUniqueId()!=null) return getExternalUniqueId().hashCode();
        return getId().hashCode();
    }
    public boolean equals(Object o) {
        if (o==null || !(o instanceof ClassInstructorInfo)) return false;
        ClassInstructorInfo i = (ClassInstructorInfo)o;
        if (getExternalUniqueId()!=null && getExternalUniqueId().equals(i.getExternalUniqueId())) return true;
        return getId().equals(i.getId());
    }

}
