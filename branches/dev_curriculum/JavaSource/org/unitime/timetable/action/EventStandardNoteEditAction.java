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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.timetable.form.EventStandardNoteEditForm;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Zuzana Mullerova
 */
public class EventStandardNoteEditAction extends Action {

	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		EventStandardNoteEditForm myForm = (EventStandardNoteEditForm) form;
		HttpSession session = request.getSession();
		String op = myForm.getOp();
		
		if ("Back".equals(op)) {
			return mapping.findForward("list");
		}

		if ("Save".equals(op)) {
			ActionMessages errors = myForm.validate(mapping, request);
			if (!errors.isEmpty()) saveErrors(request, errors);
			else {
				Transaction tx = null;
				try {
					Session hibSession = new _RootDAO().getSession();
					tx = hibSession.beginTransaction();
	
					// create sponsoring org
					StandardEventNote sen = new StandardEventNote();
					if (myForm.getNote()!= null) sen.setNote(myForm.getNote());
					if (myForm.getReference()!=null) sen.setReference(myForm.getReference());
					hibSession.saveOrUpdate(sen); // save sponsoring org
	
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
			ActionMessages errors = myForm.validate(mapping, request);
			if (!errors.isEmpty()) saveErrors(request, errors);
			else {
				Transaction tx = null;
				try {
					Session hibSession = new _RootDAO().getSession();
					tx = hibSession.beginTransaction();
	
					// create sponsoring org
					StandardEventNote sen = myForm.getStandardNote();
					sen.setNote(myForm.getNote()==null?"":myForm.getNote());
					sen.setReference(myForm.getReference()==null?"":myForm.getReference());
					hibSession.saveOrUpdate(sen); // save sponsoring org
	
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
			Transaction tx = null;
            try {
    			Session hibSession = new _RootDAO().getSession();
            	tx = hibSession.beginTransaction();
            	
                StandardEventNote sen = myForm.getStandardNote();
               	hibSession.delete(sen);
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
