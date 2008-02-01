/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.form;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;


/** 
 * @author Tomas Muller
 */
public class ExamPeriodEditForm extends ActionForm {
    private Long iUniqueId;
    private String iOp;
    private String iDate;
    private Integer iStart;
    private Integer iLength;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
	    ActionErrors errors = new ActionErrors();
	    
	    if (!CalendarUtils.isValidDate(iDate, "MM/dd/yyyy"))
	        errors.add("date", new ActionMessage("errors.invalidDate", "Examination Date"));
	    
        if (iStart==null || iStart<=0)
            errors.add("start", new ActionMessage("errors.required", ""));
        else {
            int hour = iStart/100;
            int min = iStart%100;
            if (hour>=24)
                errors.add("start", new ActionMessage("errors.generic","Invalid start time -- hour ("+hour+") must be between 0 and 23."));
            if (min>=60)
                errors.add("start", new ActionMessage("errors.generic","Invalid start time -- minute ("+min+") must be between 0 and 59."));
            if ((min%Constants.SLOT_LENGTH_MIN)!=0)
                errors.add("start", new ActionMessage("errors.generic","Invalid start time -- minute ("+min+") must be divisible by "+Constants.SLOT_LENGTH_MIN+"."));
        }

	    if (iLength==null || iLength<=0)
	        errors.add("length", new ActionMessage("errors.required", ""));
	    else if ((iLength%Constants.SLOT_LENGTH_MIN)!=0)
            errors.add("length", new ActionMessage("errors.generic","Invalid length -- period length must be divisible by "+Constants.SLOT_LENGTH_MIN+"."));
	    
	    try {
	        if (errors.isEmpty()) {
	            User user = Web.getUser(request.getSession());
	            Session session = Session.getCurrentAcadSession(user);
	            Date startDate = new SimpleDateFormat("MM/dd/yyyy").parse(iDate);
	            long diff = startDate.getTime()-session.getExamBeginDate().getTime();
	            int dateOffset = (int)Math.round(diff/(1000.0 * 60 * 60 * 24)); 
	            int hour = iStart / 100;
	            int min = iStart % 100;
	            int slot = (hour*60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	            ExamPeriod period = ExamPeriod.findByDateStart(session.getUniqueId(), dateOffset, slot);
	            if (period!=null && !period.getUniqueId().equals(getUniqueId())) {
	                errors.add("date", new ActionMessage("errors.exists", "An examination period with given date and start time"));
	            }
	        }
	    } catch (Exception e) {}
	    
	    return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; iUniqueId = new Long(-1); iDate = null; iStart = null; iLength = null;
	}
	
	public void load(ExamPeriod ep, HttpServletRequest request) throws Exception {
		if (ep==null) {
			reset(null, null);
	        User user = Web.getUser(request.getSession());
	        Session session = Session.getCurrentAcadSession(user);
			iDate = new SimpleDateFormat("MM/dd/yyyy").format(session.getExamBeginDate());
			iLength = 120;
			iOp = "Save";
		} else {
		    iUniqueId = ep.getUniqueId();
			iDate = new SimpleDateFormat("MM/dd/yyyy").format(ep.getStartDate());
			iStart = ep.getStartHour()*100 + ep.getStartMinute();
			iLength = ep.getLength() * Constants.SLOT_LENGTH_MIN;
			iOp = "Update";
		}
	}
	
	public void update(ExamPeriod ep, HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
	    ep.setStartDate(new SimpleDateFormat("MM/dd/yyyy").parse(iDate));
	    int hour = iStart / 100;
	    int min = iStart % 100;
	    int slot = (hour*60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	    ep.setStartSlot(slot);
	    ep.setLength(iLength / Constants.SLOT_LENGTH_MIN);
		hibSession.saveOrUpdate(ep);
	}
	
	public ExamPeriod create(HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
	    ExamPeriod ep = new ExamPeriod();
        User user = Web.getUser(request.getSession());
        Session session = Session.getCurrentAcadSession(user);
        ep.setSession(session);
        ep.setStartDate(new SimpleDateFormat("MM/dd/yyyy").parse(iDate));
        int hour = iStart / 100;
        int min = iStart % 100;
        int slot = (hour*60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
        ep.setStartSlot(slot);
        ep.setLength(iLength / Constants.SLOT_LENGTH_MIN);
        hibSession.saveOrUpdate(ep);
		setUniqueId(ep.getUniqueId());
		return ep;
	}
	
	public ExamPeriod saveOrUpdate(HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
	    ExamPeriod ep = null;
		if (getUniqueId().longValue()>=0)
			ep = (new ExamPeriodDAO()).get(getUniqueId());
		if (ep==null)
			ep = create(request, hibSession);
		else 
			update(ep, request, hibSession);
		return ep;
	}
	
	public void delete(org.hibernate.Session hibSession) throws Exception {
		if (getUniqueId().longValue()<0) return;
		ExamPeriod ep = (new ExamPeriodDAO()).get(getUniqueId(), hibSession);
		hibSession.delete(ep);
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
	public String getDate() { return iDate; }
	public void setDate(String date) { iDate = date; }
	public Integer getStart() { return iStart; }
	public void setStart(Integer start) { iStart = start; }
    public Integer getLength() { return iLength; }
    public void setLength(Integer length) { iLength = length; }
}