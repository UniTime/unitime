package org.unitime.timetable.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventAddInfoForm;

public class EventAddInfoAction extends Action {

	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	
		EventAddInfoForm myForm = (EventAddInfoForm) form;
		
//Verification of user being logged in
		if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }		

//Operations		
		String iOp = myForm.getOp();
		
		if (iOp!=null) {
			
			if ("Back".equals(iOp)) {
				myForm.save(request.getSession());
				request.setAttribute("back", "eventAddInfo");
				return mapping.findForward("back");
			}

			if ("Submit".equals(iOp)) {
	        	ActionMessages errors = myForm.validate(mapping, request);
	        	if (!errors.isEmpty()) {
	        		saveErrors(request, errors);
	        	} else {
	        		myForm.submit(request.getSession());
	        		myForm.cleanSessionAttributes(request.getSession());
	        		response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getEventId()));
	        		return null;
	        	}
			}
			
			if ("Update".equals(iOp)) {
	        	ActionMessages errors = myForm.validate(mapping, request);
	        	if (!errors.isEmpty()) {
	        		saveErrors(request, errors);
	        	} else {
	        		myForm.update(request.getSession());
	        		myForm.cleanSessionAttributes(request.getSession());
	        		response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getEventId()));
	        		return null;
	        	}
			}
			
			if ("Cancel Event".equals(iOp)) {
        		myForm.cleanSessionAttributes(request.getSession());				
				return mapping.findForward("eventList");
			}
		
			if ("Cancel".equals(iOp)) {
				myForm.cleanSessionAttributes(request.getSession());
				response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getEventId()));
				return null;
			}
		}
		
		return mapping.findForward("show");
	}
	
}
