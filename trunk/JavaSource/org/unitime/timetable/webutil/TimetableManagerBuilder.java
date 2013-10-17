/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.hibernate.criterion.Order;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.RolesComparator;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/**
 * Build list of Managers for the currently selected academic session
 * 
 * @author Heston Fernandes, Tomas Muller, Stephanie Schluttenhofer
 */
public class TimetableManagerBuilder {
    
    public PdfWebTable getManagersTable(SessionContext context, boolean html) {

        int cols = 7;
		org.hibernate.Session hibSession = null;
        
        boolean dispLastChanges = CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(context.getUser()));
        if (dispLastChanges) cols++;

	    Long currentAcadSession = context.getUser().getCurrentAcademicSessionId();
	    
		// Create new table
        PdfWebTable webTable = new PdfWebTable( cols,
			    (html?"":"Manager List - " + (currentAcadSession == null ? "" : " - " + context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel())),
			    "timetableManagerList.do?order=%%",
                (dispLastChanges?
                        new String[] {"Roles", "External ID", "Name", "Email Address", "Department", "Subject Area", "Solver Group", "Last Change"}:
                        new String[] {"Roles", "External ID", "Name", "Email Address", "Department", "Subject Area", "Solver Group"}),
			    new String[] {"left", "left", "left", "left", "left", "left", "left", "left"},
			    new boolean[] {true, true, true, true, true, true, true, false} );
        webTable.enableHR("#9CB0CE");
        webTable.setRowStyle("white-space: nowrap");
        	
	    TimetableManagerDAO empDao = new TimetableManagerDAO();
		hibSession = empDao.getSession();

		List empList = hibSession
						.createCriteria(TimetableManager.class)
						.addOrder(Order.asc("managerRoles"))
						.addOrder(Order.asc("lastName"))
						.addOrder(Order.asc("firstName"))
						.list();
		Iterator iterEmp = empList.iterator();

		while(iterEmp.hasNext()) {
		    TimetableManager manager = (TimetableManager) iterEmp.next();

		    String puid = manager.getExternalUniqueId();
		    String middleName = manager.getMiddleName();
		    String email = manager.getEmailAddress()!=null ? manager.getEmailAddress() : " ";
		    String fullName = "";
		    String subjectList = "";
		    String roleStr = "";
		    String deptStr = "";
		    Set depts = manager.getDepartments();
		    Set mgrRolesSet = manager.getManagerRoles();
		    
		    String onClick = (context.hasPermission(manager, Right.TimetableManagerEdit) ? "onClick=\"document.location='timetableManagerEdit.do?op=Edit&id=" + manager.getUniqueId() + "';\"" : null);

		    // Construct Full Name
		    if(middleName==null || middleName.equals("null") || middleName.trim().length()==0)
		        fullName = manager.getLastName() + ", " + manager.getFirstName() + " ";
		    else {
		    	String mn = "";
		    	StringTokenizer strTok = new StringTokenizer(middleName, " ");
		    	while (strTok.hasMoreTokens()) {
		    		mn += strTok.nextToken().substring(0,1) + " ";
		    	}
		        fullName = manager.getLastName() + ", " + manager.getFirstName() + " " + mn;
		    }

		    // Determine role type
		    String roleOrd = "";
	        ArrayList mgrRoles = new ArrayList(mgrRolesSet);
	        Collections.sort(mgrRoles, new RolesComparator());
	        
		    for (Iterator i=mgrRoles.iterator(); i.hasNext(); ) {
		        ManagerRole mgrRole = (ManagerRole) i.next();
                String roleRef = mgrRole.getRole().getAbbv(); 
		        String title = roleRef;
		        boolean receivesEmail = (mgrRole.isReceiveEmails() == null?false:mgrRole.isReceiveEmails().booleanValue());
                if (roleStr.length()>0) roleStr+=","+(html?"<br>":"\n");
                if (mgrRoles.size()>1 && mgrRole.isPrimary().booleanValue()) {
                    roleStr += (html?"<span title='"+roleRef+" - Primary Role" + (receivesEmail?"":", * No Email for this Role")+"' style='font-weight:bold;'>"+roleRef + (receivesEmail?"":"*") +"</span>":"@@BOLD "+roleRef + (receivesEmail?"":"*")+"@@END_BOLD ");
                } else {
                    roleStr += (html?(!receivesEmail?"<span title='"+roleRef + (receivesEmail?"":", * No Email for this Role")+"' style='font-weight:normal;'>"+roleRef + (receivesEmail?"":"*") +"</span>": roleRef):roleRef + (receivesEmail?"":"*"));
                }
		        roleOrd += title;
		    }
		    
		    // Departments
		    for (Iterator di=depts.iterator(); di.hasNext(); ) {
		        Department dept = (Department) di.next();
		        
		        if (!dept.getSession().getUniqueId().equals(currentAcadSession)) continue;
		        
	            if (deptStr.trim().length()>0) deptStr += ", "+(html?"<br>":"\n");
	            deptStr += 
                    (html?
                            "<span title='"+dept.getHtmlTitle()+"'>"+(dept.isExternalManager()?"<b>":"")+
                            dept.getDeptCode()+(dept.getAbbreviation()==null?"":": "+dept.getAbbreviation().trim())+
                            (dept.isExternalManager()?"</b>":"")+"</span>"
                         :
                             (dept.isExternalManager()?"@@BOLD ":"")+
                             dept.getDeptCode()+(dept.getAbbreviation()==null?"":": "+dept.getAbbreviation().trim())+
                             (dept.isExternalManager()?"@@END_BOLD ":"")
                     );
		        
		        // Construct SubjectArea List
			    Set saList = dept.getSubjectAreas();
			    if (saList!=null && saList.size()>0) {
			        for (Iterator si = saList.iterator(); si.hasNext(); ) {
			            SubjectArea sa = (SubjectArea) si.next();
                        if (subjectList.length()>0) subjectList+=","+(html?"<br>":"\n");
                        subjectList += (html?"<span title='"+sa.getTitle()+"'>"+sa.getSubjectAreaAbbreviation().trim()+"</span>":sa.getSubjectAreaAbbreviation().trim());
			        }
			    }
		    }
		    
		    if (html && deptStr.trim().length()==0)
		        deptStr = "&nbsp;";
		    if (html && subjectList.trim().length()==0)
		        subjectList = "&nbsp;";
		    
		    String solverGroupStr = "";
		    for (Iterator i=manager.getSolverGroups().iterator();i.hasNext();) {
		    	SolverGroup sg = (SolverGroup)i.next();
		    	if (!sg.getSession().getUniqueId().equals(currentAcadSession)) continue;
		    	if (solverGroupStr.length()>0) solverGroupStr += ","+(html?"<br>":"\n");
		    	solverGroupStr += (html?"<span title='"+sg.getName()+"'>"+sg.getAbbv()+"</span>":sg.getAbbv());
		    }
		    if (html && solverGroupStr.length()==0) solverGroupStr = "&nbsp;";
            
            String lastChangeStr = null;
            Long lastChangeCmp = null;
            if (dispLastChanges) {
                List changes = null;
                if (currentAcadSession!=null) changes = ChangeLog.findLastNChanges(currentAcadSession, manager.getUniqueId(), null, null, 1);
                ChangeLog lastChange = (changes==null || changes.isEmpty()?null:(ChangeLog)changes.get(0));
                if (html)
                    lastChangeStr = (lastChange==null?"&nbsp;":"<span title='"+lastChange.getLabel()+"'>"+lastChange.getSourceTitle()+" ("+lastChange.getOperationTitle()+") on "+ChangeLog.sDFdate.format(lastChange.getTimeStamp())+"</span>");
                else
                    lastChangeStr = (lastChange==null?"":lastChange.getSourceTitle()+" ("+lastChange.getOperationTitle()+") on "+ChangeLog.sDFdate.format(lastChange.getTimeStamp()));
                lastChangeCmp = new Long(lastChange==null?0:lastChange.getTimeStamp().getTime());
            }
		    
		    // Add to web table
		    webTable.addLine(
	        	onClick,
	        	new String[] { roleStr, (html?"<A name='" + manager.getUniqueId() + "'>" + puid + "&nbsp;</A>":puid), fullName, email, deptStr, subjectList, solverGroupStr, lastChangeStr},
	        	new Comparable[] {roleOrd, puid, fullName, email, deptStr, subjectList, solverGroupStr, lastChangeCmp} );
		}
        
        return webTable;
    }
}
