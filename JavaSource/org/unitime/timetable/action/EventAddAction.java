package org.unitime.timetable.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventAddForm;

/**
 * @author Zuzana Mullerova
 */
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

//Collect initial info - form & model
		EventAddForm myForm = (EventAddForm) form;

/*        EventModel model = (EventModel)request.getSession().getAttribute("Event.model");
        if (model==null) {
            model = new EventModel();
            request.getSession().setAttribute("Event.model", model);
        }
*/		

//		myForm.load(request.getSession());
        
//Verification of user being logged in
		if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }		

//Operations
		String op = myForm.getOp();
		if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
			op = request.getParameter("op2");
		
		// if a different session is selected, display calendar for this new session
		if (op!=null && !"SessionChanged".equals(op)) {
			myForm.loadDates(request);
		}
		
        if ("Show Scheduled Events".equals(op)) {
        	ActionMessages errors = myForm.validate(mapping, request);
        	if (!errors.isEmpty()) {
        		saveErrors(request, errors);
        	} else {
        		System.out.println("Event dates:"+myForm.getMeetingDates());
        		myForm.save(request.getSession());
        	}
        }

        if ("Show Availability".equals(op)) {
        	ActionMessages errors = myForm.validate(mapping, request);
        	if (!errors.isEmpty()) {
        		saveErrors(request, errors);
        	} else {
        		myForm.save(request.getSession());
        		response.sendRedirect(response.encodeURL("eventRoomAvailability.do"));
//        		return mapping.findForward("showEventRoomAvailability");
        	}
        }        
		
//test:        System.out.println(">>> "+op+" <<<");


//set the model        
//        myForm.setModel(model);
//        model.apply(request, myForm);
        
        
//Display the page        
        return mapping.findForward("show");
	}
}

