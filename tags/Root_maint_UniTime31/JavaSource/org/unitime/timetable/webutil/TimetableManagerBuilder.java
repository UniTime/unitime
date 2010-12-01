/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.webutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.Order;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.RolesComparator;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/**
 * Build list of Managers for the currently selected academic session
 * 
 * @author Heston Fernandes
 */
public class TimetableManagerBuilder {
    
    public PdfWebTable getManagersTable(HttpServletRequest request, boolean images, boolean html) {

        int cols = 7;
		org.hibernate.Session hibSession = null;
        
        Session session = null;
        try {
            session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
        } catch (Exception e) {}
        
        User user = Web.getUser(request.getSession());
        boolean dispLastChanges = (!"no".equals(Settings.getSettingValue(user, Constants.SETTINGS_DISP_LAST_CHANGES)));
        if (dispLastChanges) cols++;


		// Create new table
        PdfWebTable webTable = new PdfWebTable( cols,
			    (html?"":"Manager List - "+Web.getUser(request.getSession()).getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME)),
			    "timetableManagerList.do?order=%%",
                (dispLastChanges?
                        new String[] {"Roles", "External ID", "Name", "Email Address", "Department", "Subject Area", "Solver Group", "Last Change"}:
                        new String[] {"Roles", "External ID", "Name", "Email Address", "Department", "Subject Area", "Solver Group"}),
			    new String[] {"left", "left", "left", "left", "left", "left", "left", "left"},
			    new boolean[] {true, true, true, true, true, true, true, false} );
        webTable.enableHR("#EFEFEF");
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
		    
		    String onClick = "onClick=\"document.location='timetableManagerEdit.do?op=Edit&id="
				+ manager.getUniqueId() + "';\"";

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
                String roleRef = mgrRole.getRole().getReference(); 
		        String title = roleRef;
		        boolean receivesEmail = (mgrRole.isReceiveEmails() == null?false:mgrRole.isReceiveEmails().booleanValue());
                if (images && html) {
                    String border = "0";
                    if(mgrRoles.size()>1 && mgrRole.isPrimary().booleanValue()) {
                        border="1";
                        title += " - Primary Role";
                    }
                    if (!receivesEmail){
                    	title += ", * No Email for this Role";
                    	border="1";
                    }
                    roleStr += "<IMG height='25' width='25' border='" + border + "'" +
                        "src='" + request.getContextPath() + "/images/" + Roles.getRoleIcon(roleRef) + "' " +
		        		"title='" + title + "' " +
		        		"alt='" + title + "' " +
		        		"align='middle'>";
                } else {
                    if (roleStr.length()>0) roleStr+=","+(html?"<br>":"\n");
                    if (mgrRoles.size()>1 && mgrRole.isPrimary().booleanValue()) {
                        roleStr += (html?"<span title='"+roleRef+" - Primary Role" + (receivesEmail?"":", * No Email for this Role")+"' style='font-weight:bold;'>"+roleRef + (receivesEmail?"":"*") +"</span>":"@@BOLD "+roleRef + (receivesEmail?"":"*")+"@@END_BOLD ");
                    } else {
                        roleStr += (html?(!receivesEmail?"<span title='"+roleRef + (receivesEmail?"":", * No Email for this Role")+"' style='font-weight:normal;'>"+roleRef + (receivesEmail?"":"*") +"</span>": roleRef):roleRef + (receivesEmail?"":"*"));
                    }
                }
		        roleOrd += title;
		    }
		    
		    /*
		    if (manager.isExternalManager()) {
                if (images && html)
                    roleStr += "<IMG height='25' width='25' src='" + request.getContextPath() + "/images/ext-mgr-icon.gif' alt='External Manager' title='External Manager' border='0' align='middle'>";
                else
                    roleStr += ","+(html?"<br>":"\n")+"External Manager";
		    }
		    */

		    Long currentAcadSession = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		    
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
                        subjectList += (html?"<span title='"+sa.getLongTitle()+"'>"+sa.getSubjectAreaAbbreviation().trim()+"</span>":sa.getSubjectAreaAbbreviation().trim());
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
                if (session!=null) changes = ChangeLog.findLastNChanges(session.getUniqueId(), manager.getUniqueId(), null, null, 1);
                ChangeLog lastChange = (changes==null || changes.isEmpty()?null:(ChangeLog)changes.get(0));
                if (html)
                    lastChangeStr = (lastChange==null?"&nbsp;":"<span title='"+lastChange.getLabel(request)+"'>"+lastChange.getSourceTitle(request)+" ("+lastChange.getOperationTitle(request)+") on "+ChangeLog.sDFdate.format(lastChange.getTimeStamp())+"</span>");
                else
                    lastChangeStr = (lastChange==null?"":lastChange.getSourceTitle(request)+" ("+lastChange.getOperationTitle(request)+") on "+ChangeLog.sDFdate.format(lastChange.getTimeStamp()));
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
