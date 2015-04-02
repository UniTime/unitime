/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.form;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.webutil.timegrid.ExamGridTable;

/**
 * @author Tomas Muller
 */
public class ExamGridForm extends ActionForm {
	private static final long serialVersionUID = 1429431006186003906L;
	private Long iSessionId;
    private Map<String, TreeSet> iPeriods = new HashMap<String, TreeSet>();
    private int iSessionBeginWeek;
    private Date iSessionBeginDate;
    private Date iExamBeginDate;
    private boolean iShowSections = false;
    
    private String iOp = null;
    private Long iExamType;
    private Map<String, Integer> iDate = new HashMap<String, Integer>();
    private Map<String, Integer> iStartTime = new HashMap<String, Integer>();
    private Map<String, Integer> iEndTime = new HashMap<String, Integer>();
    private int iResource = ExamGridTable.sResourceRoom;
    private int iBackground = ExamGridTable.sBgNone;
    private String iFilter = null;
    private int iDispMode = ExamGridTable.sDispModePerWeekVertical;
    private int iOrder = ExamGridTable.sOrderByNameAsc;
    private boolean iBgPreferences = false;
    
    public int getDate(String examType) { return iDate.get(examType); }
    public void setDate(String examType, int date) { iDate.put(examType, date); }
    public boolean isAllDates(String examType) { return iDate.get(examType) == Integer.MIN_VALUE; }
    public int getStartTime(String examType) { return iStartTime.get(examType); }
    public void setStartTime(String examType, int startTime) { iStartTime.put(examType, startTime); }
    public int getEndTime(String examType) { return iEndTime.get(examType); }
    public void setEndTime(String examType, int endTime) { iEndTime.put(examType, endTime); }
    public int getResource() { return iResource; }
    public void setResource(int resource) { iResource = resource; }
    public int getBackground() { return iBackground; }
    public void setBackground(int background) { iBackground = background; }
    public String getFilter() { return iFilter; }
    public void setFilter(String filter) { iFilter = filter; }
    public int getDispMode() { return iDispMode; }
    public void setDispMode(int dispMode) { iDispMode = dispMode; }
    public int getOrder() { return iOrder; }
    public void setOrder(int order) { iOrder = order; }
    public boolean getBgPreferences() { return iBgPreferences; }
    public void setBgPreferences(boolean bgPreferences) { iBgPreferences = bgPreferences; }
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
    	iDate.clear();
        iStartTime.clear();
        iEndTime.clear();
        iResource = ExamGridTable.sResourceRoom;
        iBackground = ExamGridTable.sBgNone;
        iFilter = null;
        iDispMode = ExamGridTable.sDispModePerWeekVertical;
        iOrder = ExamGridTable.sOrderByNameAsc;
        iBgPreferences = false;
        iOp = null;
        iShowSections = false;
		iExamType = null;
		try {
			ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
			if (solver!=null)
				iExamType = solver.getProperties().getPropertyLong("Exam.Type", null);
		} catch (Exception e) {}
    }
    
    public Long getSessionId() { return iSessionId; }
    public Date getExamBeginDate() { return iExamBeginDate; }
    public TreeSet getPeriods(String examType) { return iPeriods.get(examType); }
    
    public void load(SessionContext context) throws Exception {
        Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
        iSessionId = session.getUniqueId();
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(session.getSessionBeginDateTime());
        iSessionBeginWeek = cal.get(Calendar.WEEK_OF_YEAR);
        iSessionBeginDate = session.getSessionBeginDateTime();
        iExamBeginDate = session.getExamBeginDate();
        iPeriods.clear();
        for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamTimetable)) {
        	iPeriods.put(type.getUniqueId().toString(), ExamPeriod.findAll(session.getUniqueId(), type.getUniqueId()));
        	setDate(type.getUniqueId().toString(), Integer.parseInt(context.getUser().getProperty("ExamGrid.date."+type.getUniqueId(), String.valueOf(Integer.MIN_VALUE))));
        	setStartTime(type.getUniqueId().toString(), Integer.parseInt(context.getUser().getProperty("ExamGrid.start."+type.getUniqueId(), String.valueOf(getFirstStart(type.getUniqueId().toString())))));
        	setEndTime(type.getUniqueId().toString(), Integer.parseInt(context.getUser().getProperty("ExamGrid.end."+type.getUniqueId(), String.valueOf(getLastEnd(type.getUniqueId().toString())))));
        }
        setResource(Integer.parseInt(context.getUser().getProperty("ExamGrid.resource", String.valueOf(ExamGridTable.sResourceRoom))));
        setBackground(Integer.parseInt(context.getUser().getProperty("ExamGrid.background", String.valueOf(ExamGridTable.sBgNone))));
        setFilter(context.getUser().getProperty("ExamGrid.filter"));
        setDispMode(Integer.parseInt(context.getUser().getProperty("ExamGrid.dispMode", String.valueOf(ExamGridTable.sDispModePerWeekVertical))));
        setOrder(Integer.parseInt(context.getUser().getProperty("ExamGrid.order", String.valueOf(ExamGridTable.sOrderByNameAsc))));
        setBgPreferences("1".equals(context.getUser().getProperty("ExamGrid.bgPref", "0")));
        setExamType(context.getAttribute("Exam.Type") == null ? iExamType : (Long)context.getAttribute("Exam.Type"));
        setShowSections("1".equals(context.getUser().getProperty("ExamReport.showSections", "1")));
    }
    
    public void save(SessionContext context) throws Exception {
    	for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamTimetable)) {
    		context.getUser().setProperty("ExamGrid.date."+type.getUniqueId(), String.valueOf(getDate(type.getUniqueId().toString())));
    		context.getUser().setProperty("ExamGrid.start."+type.getUniqueId(), String.valueOf(getStartTime(type.getUniqueId().toString())));
    		context.getUser().setProperty("ExamGrid.end."+type.getUniqueId(), String.valueOf(getEndTime(type.getUniqueId().toString())));
    	}
        context.getUser().setProperty("ExamGrid.resource", String.valueOf(getResource()));
        context.getUser().setProperty("ExamGrid.background", String.valueOf(getBackground()));
        context.getUser().setProperty("ExamGrid.filter", String.valueOf(getFilter()));
        context.getUser().setProperty("ExamGrid.dispMode", String.valueOf(getDispMode()));
        context.getUser().setProperty("ExamGrid.order", String.valueOf(getOrder()));
        context.getUser().setProperty("ExamGrid.bgPref", getBgPreferences() ? "1" : "0");
        context.setAttribute("Exam.Type", getExamType());
        context.getUser().setProperty("ExamReport.showSections", getShowSections() ? "1" : "0");
    }

    public Vector<ComboBoxLookup> getDates(String examType) {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        ret.addElement(new ComboBoxLookup("All Dates", String.valueOf(Integer.MIN_VALUE)));
        HashSet added = new HashSet();
        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
        for (Iterator i=iPeriods.get(examType).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            Calendar cal = Calendar.getInstance(Locale.US);
            cal.setTime(period.getStartDate());
            int week = 1;
            while (cal.getTime().after(iSessionBeginDate) && cal.get(Calendar.WEEK_OF_YEAR) != iSessionBeginWeek) {
            	cal.add(Calendar.DAY_OF_YEAR, -7); week ++;
            }
            while (cal.getTime().before(iSessionBeginDate) && cal.get(Calendar.WEEK_OF_YEAR) != iSessionBeginWeek) {
            	cal.add(Calendar.DAY_OF_WEEK, 7); week --;
            }
            cal.setTime(period.getStartDate());
            if (added.add(1000+week)) {
                while (cal.get(Calendar.DAY_OF_WEEK)!=Calendar.MONDAY) cal.add(Calendar.DAY_OF_YEAR, -1);
                String first = df.format(cal.getTime());
                while (cal.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY) cal.add(Calendar.DAY_OF_YEAR, 1);
                String end = df.format(cal.getTime());
                ret.addElement(new ComboBoxLookup(
                        "Week "+week+" ("+first+" - "+end+")",
                        String.valueOf(1000+week)));
            }
        }
        for (Iterator i=iPeriods.get(examType).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (added.add(period.getDateOffset())) {
                ret.addElement(new ComboBoxLookup(ExamGridTable.sDF.format(period.getStartDate()),period.getDateOffset().toString()));
            }
        }
        return ret;
    }
    
    public int getFirstDate(String examType) {
        int startDate = Integer.MAX_VALUE;
        for (Iterator i=iPeriods.get(examType).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            startDate = Math.min(startDate, period.getDateOffset());
        }
        return startDate;
    }
    
    public int getLastDate(String examType) {
        int endDate = Integer.MIN_VALUE;
        for (Iterator i=iPeriods.get(examType).iterator();i.hasNext();) {
        	ExamPeriod period = (ExamPeriod)i.next();
            endDate = Math.max(endDate, period.getDateOffset());
        }
        return endDate;
    }

    
    public int getFirstStart(String examType) {
        int startSlot = -1;
        for (Iterator i=iPeriods.get(examType).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (startSlot<0) startSlot = period.getStartSlot();
            else startSlot = Math.min(startSlot, period.getStartSlot());
        }
        return startSlot;
    }
    
    public int getLastEnd(String examType) {
        int endSlot = -1;
        for (Iterator i=iPeriods.get(examType).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (endSlot<0) endSlot = period.getEndSlot();
            else endSlot = Math.max(endSlot, period.getEndSlot());
        }
        return endSlot;
    }

    public Vector<ComboBoxLookup> getStartTimes(String examType) {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        HashSet added = new HashSet();
        for (Iterator i=iPeriods.get(examType).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (added.add(period.getStartSlot())) {
                ret.addElement(new ComboBoxLookup(period.getStartTimeLabel(), period.getStartSlot().toString()));
            }
        }
        return ret;
    }
    
    public Vector<ComboBoxLookup> getEndTimes(String examType) {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        HashSet added = new HashSet();
        for (Iterator i=iPeriods.get(examType).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (added.add(period.getEndSlot())) {
                ret.addElement(new ComboBoxLookup(period.getEndTimeLabel(), String.valueOf(period.getEndSlot())));
            }
        }
        return ret;
    }
    
    public Vector<ComboBoxLookup> getResources() {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        for (int i=0;i<ExamGridTable.sResources.length;i++)
            ret.addElement(new ComboBoxLookup(ExamGridTable.sResources[i], String.valueOf(i)));
        return ret;
    }

    public Vector<ComboBoxLookup> getBackgrounds() {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        for (int i=0;i<ExamGridTable.sBackgrounds.length;i++)
            ret.addElement(new ComboBoxLookup(ExamGridTable.sBackgrounds[i], String.valueOf(i)));
        return ret;
    }

    public Vector<ComboBoxLookup> getDispModes() {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        for (int i=0;i<ExamGridTable.sDispModes.length;i++)
            ret.addElement(new ComboBoxLookup(ExamGridTable.sDispModes[i], String.valueOf(i)));
        return ret;
    }

    public Vector<ComboBoxLookup> getOrders() {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        for (int i=0;i<ExamGridTable.sOrders.length;i++)
            ret.addElement(new ComboBoxLookup(ExamGridTable.sOrders[i], String.valueOf(i)));
        return ret;
    }

    public Long getExamType() { return iExamType; }
    public void setExamType(Long type) { iExamType = type; }
    
    public int getSessionBeginWeek() { return iSessionBeginWeek; }
    public Date getSessionBeginDate() { return iSessionBeginDate; }
    public boolean getShowSections() { return iShowSections; }
    public void setShowSections(boolean showSections) { iShowSections = showSections; }
}
