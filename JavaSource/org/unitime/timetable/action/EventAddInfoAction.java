package org.unitime.timetable.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventAddInfoForm;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;

public class EventAddInfoAction extends Action {

	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	
		EventAddInfoForm myForm = (EventAddInfoForm) form;
		User user = Web.getUser(request.getSession());
		
//Verification of user being logged in
		if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
		
		myForm.setMainContactLookup(user.isAdmin() || Roles.EVENT_MGR_ROLE.equals(user.getRole()));
		if (!myForm.getMainContactLookup()) {
		    myForm.setMainContactExternalId(user.getId());
		    TimetableManager m = TimetableManager.getManager(user);
		    EventContact c = EventContact.findByExternalUniqueId(user.getId());
		    if (c!=null) {
                if (myForm.getMainContactFirstName()==null || myForm.getMainContactFirstName().length()==0)
                    myForm.setMainContactFirstName(c.getFirstName());
                if (myForm.getMainContactLastName()==null || myForm.getMainContactLastName().length()==0)
                    myForm.setMainContactLastName(c.getLastName());
                if (myForm.getMainContactEmail()==null || myForm.getMainContactEmail().length()==0)
                    myForm.setMainContactEmail(c.getEmailAddress());
                if (myForm.getMainContactPhone()==null || myForm.getMainContactPhone().length()==0)
                    myForm.setMainContactPhone(c.getPhone());
		    } else if (m!=null) {
		        if (myForm.getMainContactFirstName()==null || myForm.getMainContactFirstName().length()==0)
		            myForm.setMainContactFirstName(m.getFirstName());
		        if (myForm.getMainContactLastName()==null || myForm.getMainContactLastName().length()==0)
		            myForm.setMainContactLastName(m.getLastName());
		        if (myForm.getMainContactEmail()==null || myForm.getMainContactEmail().length()==0)
		            myForm.setMainContactEmail(m.getEmailAddress());
		    } else {
		        if (myForm.getMainContactLastName()==null || myForm.getMainContactLastName().length()==0)
		            myForm.setMainContactLastName(user.getName());
		    }
		}

//Operations		
		String iOp = myForm.getOp();
		if (iOp!=null) {
			
			if ("Change Selection".equals(iOp)) {
				myForm.save(request.getSession());
				request.setAttribute("back", "eventAddInfo");
				return mapping.findForward("back");
			}

			if ("Submit".equals(iOp)) {
	        	ActionMessages errors = myForm.validate(mapping, request);
	        	if (!errors.isEmpty()) {
	        		saveErrors(request, errors);
	        	} else {
	        		myForm.submit(request);
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
	        		myForm.update(request);
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
