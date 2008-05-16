package org.unitime.timetable.action;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventAddForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.DateUtils;

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
		
		System.out.println(">>> "+op+" <<<");
		
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
			
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}			
		
        // CALENDAR TO PICK EVENTS //

        // get months for the calendar to pick dates from //
		Session s = Session.getSessionById(myForm.getSessionId());
        Calendar first = Calendar.getInstance();
        first.setTime(s.getSessionBeginDateTime());
        Calendar last = Calendar.getInstance();
        last.setTime(s.getSessionEndDateTime());
        int year = first.get(Calendar.YEAR);
        int startMonth = first.get(Calendar.MONTH);
        int endMonth = last.get(Calendar.MONTH);
        if (last.get(Calendar.YEAR)!=year) endMonth+=12;
        
        
/*        // set which dates have meetings //
        for (Meeting meeting: meetings) {
            Calendar c = Calendar.getInstance();
            c.setTime(meeting.getMeetingDate());
            int m = c.get(Calendar.MONTH);
            int d = c.get(Calendar.DAY_OF_MONTH);
            if (c.get(Calendar.YEAR)!=year) m+=12;
            myForm.addMeetingDate(m+":"+d);
        }
*/
        // set up calendar //
        String pattern = "[", border = "[";
        //myForm.clearMeetingDates();
                        for (int m=startMonth;m<=endMonth;m++) {

                                   if (m!=startMonth) {pattern+=","; border+=","; }
                                    pattern+="["; border+="[";
                                    int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
                                    for (int d=1;d<=daysOfMonth;d++) {
                                                if (d>1) {pattern+=","; border+=","; }
                                                //pattern+=(myForm.hasMeetingDate(year, m, d)?"'1'":"'0'");
                                                pattern+=(myForm.getMeetingDates().contains(m+":"+d)?"'1'":"'0'");
//                                                border += (Math.random()<0.1?"'rgb(100,20,180) 2px solid'":Math.random()<0.1?"'red 2px solid'":"null");
                                                if ("1".equals(request.getParameter("cal_val_"+((12+m)%12)+"_"+d))) {
                                                            //ma meeting
                                                            Calendar c = Calendar.getInstance(); c.setTimeInMillis(0);
                                                            c.set(Calendar.YEAR, year+(m>=12?1:m<0?-1:0));
                                                            c.set(Calendar.MONTH, ((12+m)%12));
                                                            c.set(Calendar.DATE, d);
                                                            //myForm.addMeetingDate(c.getTime());
                                                }
                                                border += s.getBorder(d, m);
                                    }
                                    pattern+="]"; border+="]";
                        }
                        pattern+="]"; border+="]";
                        
                        // show the calendar //
                        String dates = "<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>";
                        dates += "<script language='JavaScript'>";
        dates += "calGenerate("+year+","+startMonth+","+endMonth+","+
            pattern+","+"['1','0'],"+
            "['Selected','Not Selected'],"+
            "['rgb(240,240,50)','rgb(240,240,240)'],"+
            "'1',"+border+",true,true);";
        dates += "</script>";
        request.setAttribute("dates", dates);
                        
        
        // END OF CALENDAR TO PICK EVENTS //

	
	
        return mapping.findForward("show");
	}
}

