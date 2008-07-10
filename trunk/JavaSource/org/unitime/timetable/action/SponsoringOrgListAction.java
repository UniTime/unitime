package org.unitime.timetable.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.form.SponsoringOrgListForm;

public class SponsoringOrgListAction extends Action {

	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		SponsoringOrgListForm myForm = (SponsoringOrgListForm) form;
		String op = myForm.getOp();
		HttpSession session = request.getSession();
		
		if("Add Organization".equals(op)) {
			request.setAttribute("op", "add");
			return mapping.findForward("add");
		}
		
		return mapping.findForward("show");
	}
	
}
