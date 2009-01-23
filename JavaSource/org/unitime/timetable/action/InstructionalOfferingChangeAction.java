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
package org.unitime.timetable.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;


/**
 * @author Stephanie Schluttenhofer
 */

public class InstructionalOfferingChangeAction extends LookupDispatchAction {

	protected Map getKeyMethodMap() {
	      Map map = new HashMap();
	      map.put("button.saveNotOfferedChanges", "saveNotOfferedChanges");
	      return map;
	  }
	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */

	public ActionForward saveNotOfferedChanges(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		    HttpSession webSession = request.getSession();
	        if(!Web.isLoggedIn( webSession )) {
	            throw new Exception ("Access Denied.");
	        }
	        
		    User user = Web.getUser(webSession);	    
		    InstructionalOfferingListForm instructionalOfferingListForm = (InstructionalOfferingListForm) form;
		    instructionalOfferingListForm.setSubjectAreas(Session.getCurrentAcadSession(user).getSubjectAreas());
			instructionalOfferingListForm.setInstructionalOfferings(this.getInstructionalOfferings(instructionalOfferingListForm));
			if (instructionalOfferingListForm.getInstructionalOfferings().isEmpty()) {
			    return mapping.findForward("showInstructionalOfferingSearch");
			} else {
			    Iterator it = instructionalOfferingListForm.getInstructionalOfferings().iterator();
			    InstructionalOffering io = null;
			    InstructionalOfferingDAO dao = new InstructionalOfferingDAO(); 
			    while (it.hasNext()){
			        io = (InstructionalOffering) it.next();
			        dao.save(io);
			    }
			    return mapping.findForward("showInstructionalOfferingList");
			}
		}

    private Set getInstructionalOfferings(InstructionalOfferingListForm form) {
		org.hibernate.Session hibSession = (new InstructionalOfferingDAO()).getSession();
		StringBuffer query = new StringBuffer();
		query.append("select io from InstructionalOffering as io , CourseOffering co2 ");
		query.append(" where co2.subjectArea.uniqueId = :subjectAreaId ");
		query.append(" and io.uniqueId = co2.instructionalOffering.uniqueId ");
        if (form.getCourseNbr() != null && form.getCourseNbr().length() > 0){
            query.append(" and co2.courseNbr = '");
            query.append(form.getCourseNbr());
            query.append("'  ");
        }
        if (form.getShowNotOffered() != null && !form.getShowNotOffered().booleanValue()){
            query.append(" and io.notOffered != true ");
        }
		Query q = hibSession.createQuery(query.toString());
		q.setInteger("subjectAreaId", Integer.parseInt(form.getSubjectAreaId()));
        TreeSet ts = new TreeSet(new InstructionalOfferingComparator(Long.valueOf(form.getSubjectAreaId())));
		long sTime = new java.util.Date().getTime();
		ts.addAll(q.list());
		long eTime = new java.util.Date().getTime();
        Debug.debug("fetch time = " + (eTime - sTime));
      return (ts);

    }
}
