package org.unitime.timetable.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.timetable.form.SponsoringOrgEditForm;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.dao._RootDAO;

public class SponsoringOrgEditAction extends Action {

	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		SponsoringOrgEditForm myForm = (SponsoringOrgEditForm) form;
		HttpSession session = request.getSession();
		String op = myForm.getOp();
		
		if ("Back".equals(op)) {
			return mapping.findForward("list");
		}

		if ("Save".equals(op)) {
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
		
		if ("Update".equals(op)) {
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

		if ("Delete".equals(op)) {
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
		
		return mapping.findForward("show");
	}
}
