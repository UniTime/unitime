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
package org.unitime.timetable.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.form.SponsoringOrgEditForm;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Zuzana Mullerova, Tomas Muller
 */
@Service("/sponsoringOrgEdit")
public class SponsoringOrgEditAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		SponsoringOrgEditForm myForm = (SponsoringOrgEditForm) form;
		String op = myForm.getOp();
		
		sessionContext.checkPermission(Right.SponsoringOrganizations);
		
		if ("Back".equals(op)) {
			return mapping.findForward("list");
		}

		if ("Save".equals(op)) {
			
			sessionContext.checkPermission(Right.SponsoringOrganizationAdd);
			
			ActionMessages errors = myForm.validate(mapping, request);
			if (!errors.isEmpty()) saveErrors(request, errors);
			else {
				Transaction tx = null;
				try {
					Session hibSession = new _RootDAO().getSession();
					tx = hibSession.beginTransaction();
	
					// create sponsoring org
					SponsoringOrganization spor = new SponsoringOrganization();
					if (myForm.getOrgName()!= null) spor.setName(myForm.getOrgName());
					if (myForm.getOrgEmail()!=null) spor.setEmail(myForm.getOrgEmail());
					hibSession.saveOrUpdate(spor); // save sponsoring org
	
	/*				ChangeLog.addChange(
		                    hibSession,
		                    request,
		                    iEvent,
		                    ChangeLog.Source.EVENT_EDIT,
		                    ChangeLog.Operation.UPDATE,
		                    null,null);
	*/				
					tx.commit();
				} catch (Exception e) {
					if (tx!=null) tx.rollback();
					throw e;
				}
				return mapping.findForward("list");
			}
			}
		
		if ("Update".equals(op)) {
			
			sessionContext.checkPermission(myForm.getId(), "SponsoringOrganization", Right.SponsoringOrganizationEdit);
			
			ActionMessages errors = myForm.validate(mapping, request);
			if (!errors.isEmpty()) saveErrors(request, errors);
			else {
				Transaction tx = null;
				try {
					Session hibSession = new _RootDAO().getSession();
					tx = hibSession.beginTransaction();
	
					// create sponsoring org
					SponsoringOrganization spor = myForm.getOrg();
					spor.setName(myForm.getOrgName()==null?"":myForm.getOrgName());
					spor.setEmail(myForm.getOrgEmail()==null?"":myForm.getOrgEmail());
					hibSession.saveOrUpdate(spor); // save sponsoring org
	
	/*				ChangeLog.addChange(
		                    hibSession,
		                    request,
		                    iEvent,
		                    ChangeLog.Source.EVENT_EDIT,
		                    ChangeLog.Operation.UPDATE,
		                    null,null);
	*/				
					tx.commit();
				} catch (Exception e) {
					if (tx!=null) tx.rollback();
					e.printStackTrace();
					throw e;
				}
				
				return mapping.findForward("list");
			}
		}

		if ("Delete".equals(op)) {
			
			sessionContext.checkPermission(myForm.getId(), "SponsoringOrganization", Right.SponsoringOrganizationDelete);
			
			Transaction tx = null;
            try {
    			Session hibSession = new _RootDAO().getSession();
            	tx = hibSession.beginTransaction();
            	
                SponsoringOrganization spor = myForm.getOrg();
               	hibSession.delete(spor);
                tx.commit();
                return mapping.findForward("list");
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                throw e;
            }			
		}
		
		if ("add".equals(myForm.getScreen())) return mapping.findForward("add");
		else return mapping.findForward("show");
	}
}
