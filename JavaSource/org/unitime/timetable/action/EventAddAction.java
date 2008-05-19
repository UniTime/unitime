package org.unitime.timetable.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventAddForm;

public class EventAddAction extends Action {

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */	
	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		EventAddForm myForm = (EventAddForm) form;
		
		String op = myForm.getOp();
		if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
			op = request.getParameter("op2");
		
		if (op!=null && !"SessionChanged".equals(op)) {
			myForm.loadDates(request);
		}
		
        if ("Show Scheduled Events".equals(op)) {
        	ActionMessages errors = myForm.validate(mapping, request);
        	if (!errors.isEmpty()) {
        		saveErrors(request, errors);
        	} else {
        		System.out.println("Event dates:"+myForm.getMeetingDates());
        	}
        }

        if ("Show Availability".equals(op)) {
        	ActionMessages errors = myForm.validate(mapping, request);
        	if (!errors.isEmpty()) {
        		saveErrors(request, errors);
        	} /*else {
        		System.out.println("Event dates:"+myForm.getMeetingDates());
        	}*/
        }        
		
//        System.out.println(">>> "+op+" <<<");
		
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
			
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}			
		
        return mapping.findForward("show");
	}
}

