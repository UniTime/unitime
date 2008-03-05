package org.unitime.timetable.form;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.webutil.timegrid.ExamGridTable;

public class ExamGridForm extends ActionForm {
    private Long iSessionId;
    private TreeSet[] iPeriods;
    private int iSessionBeginWeek;
    private Date iSessionBeginDate;
    private Date iExamBeginDate;
    
    private String iOp = null;
    private int iExamType = Exam.sExamTypeFinal;
    private int[] iDate;
    private int[] iStartTime;
    private int[] iEndTime;
    private int iResource = ExamGridTable.sResourceRoom;
    private int iBackground = ExamGridTable.sBgNone;
    private String iFilter = null;
    private int iDispMode = ExamGridTable.sDispModePerWeekVertical;
    private int iOrder = ExamGridTable.sOrderByNameAsc;
    private boolean iBgPreferences = false;
    private boolean iHasEveningExams = false;
    
    public int getDate(int examType) { return iDate[examType]; }
    public void setDate(int examType, int date) { iDate[examType] = date; }
    public boolean isAllDates(int examType) { return iDate[examType] == Integer.MIN_VALUE; }
    public int getStartTime(int examType) { return iStartTime[examType]; }
    public void setStartTime(int examType, int startTime) { iStartTime[examType] = startTime; }
    public int getEndTime(int examType) { return iEndTime[examType]; }
    public void setEndTime(int examType, int endTime) { iEndTime[examType] = endTime; }
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
        iDate = new int[Exam.sExamTypes.length];
        iStartTime = new int[Exam.sExamTypes.length];
        iEndTime = new int[Exam.sExamTypes.length];
        for (int i=0;i<Exam.sExamTypes.length;i++)
        	iDate[i] = iStartTime[i] = iEndTime[i] = -1;
        iResource = ExamGridTable.sResourceRoom;
        iBackground = ExamGridTable.sBgNone;
        iFilter = null;
        iDispMode = ExamGridTable.sDispModePerWeekVertical;
        iOrder = ExamGridTable.sOrderByNameAsc;
        iBgPreferences = false;
        iOp = null;
		iExamType = Exam.sExamTypeFinal;
		try {
			ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
			if (solver!=null)
				iExamType = solver.getProperties().getPropertyInt("Exam.Type", iExamType);
		} catch (Exception e) {}
        try {
            iHasEveningExams = Exam.hasEveningExams(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId());
        } catch (Exception e) {}
    }
    
    public Long getSessionId() { return iSessionId; }
    public Date getExamBeginDate() { return iExamBeginDate; }
    public TreeSet getPeriods(int examType) { return iPeriods[examType]; }
    
    public void load(HttpSession httpSession) throws Exception {
        Session session = Session.getCurrentAcadSession(Web.getUser(httpSession));
        iSessionId = session.getUniqueId();
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(session.getSessionBeginDateTime());
        iSessionBeginWeek = cal.get(Calendar.WEEK_OF_YEAR);
        iSessionBeginDate = session.getSessionBeginDateTime();
        iExamBeginDate = session.getExamBeginDate();
        iPeriods = new TreeSet[Exam.sExamTypes.length];
        for (int i=0;i<Exam.sExamTypes.length;i++) {
        	iPeriods[i] = ExamPeriod.findAll(session.getUniqueId(), i);
        	setDate(i, UserData.getPropertyInt(httpSession,"ExamGrid.date."+i,Integer.MIN_VALUE));
        	setStartTime(i, UserData.getPropertyInt(httpSession,"ExamGrid.start."+i,getFirstStart(i)));
        	setEndTime(i, UserData.getPropertyInt(httpSession,"ExamGrid.end."+i,getLastEnd(i)));
        }
        setResource(UserData.getPropertyInt(httpSession,"ExamGrid.resource",ExamGridTable.sResourceRoom));
        setBackground(UserData.getPropertyInt(httpSession,"ExamGrid.background",ExamGridTable.sBgNone));
        setFilter(UserData.getProperty(httpSession,"ExamGrid.filter"));
        setDispMode(UserData.getPropertyInt(httpSession,"ExamGrid.dispMode",ExamGridTable.sDispModePerWeekVertical));
        setOrder(UserData.getPropertyInt(httpSession,"ExamGrid.order",ExamGridTable.sOrderByNameAsc));
        setBgPreferences(UserData.getPropertyBoolean(httpSession,"ExamGrid.bgPref",false));
        setExamType(httpSession.getAttribute("Exam.Type")==null?iExamType:(Integer)httpSession.getAttribute("Exam.Type"));
    }
    
    public void save(HttpSession httpSession) throws Exception {
    	for (int i=0;i<Exam.sExamTypes.length;i++) {
    		UserData.setPropertyInt(httpSession, "ExamGrid.date."+i, getDate(i));
    		UserData.setPropertyInt(httpSession, "ExamGrid.start."+i, getStartTime(i));
    		UserData.setPropertyInt(httpSession, "ExamGrid.end."+i, getEndTime(i));
    	}
        UserData.setPropertyInt(httpSession, "ExamGrid.resource", getResource());
        UserData.setPropertyInt(httpSession, "ExamGrid.background", getBackground());
        UserData.setProperty(httpSession, "ExamGrid.filter", getFilter());
        UserData.setPropertyInt(httpSession, "ExamGrid.dispMode", getDispMode());
        UserData.setPropertyInt(httpSession, "ExamGrid.order", getOrder());
        UserData.setPropertyBoolean(httpSession, "ExamGrid.bgPref", getBgPreferences());
        httpSession.setAttribute("Exam.Type", getExamType());
    }

    public Vector<ComboBoxLookup> getDates(int examType) {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        ret.addElement(new ComboBoxLookup("All Dates", String.valueOf(Integer.MIN_VALUE)));
        HashSet added = new HashSet();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd");
        for (Iterator i=iPeriods[examType].iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            Calendar cal = Calendar.getInstance(Locale.US);
            cal.setTime(period.getStartDate());
            int week = 1+cal.get(Calendar.WEEK_OF_YEAR)-iSessionBeginWeek;
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
        for (Iterator i=iPeriods[examType].iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (added.add(period.getDateOffset())) {
                ret.addElement(new ComboBoxLookup(ExamGridTable.sDF.format(period.getStartDate()),period.getDateOffset().toString()));
            }
        }
        return ret;
    }
    
    public int getFirstDate(int examType) {
        int startDate = Integer.MAX_VALUE;
        for (Iterator i=iPeriods[examType].iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            startDate = Math.min(startDate, period.getDateOffset());
        }
        return startDate;
    }
    
    public int getLastDate(int examType) {
        int endDate = Integer.MIN_VALUE;
        for (Iterator i=iPeriods[examType].iterator();i.hasNext();) {
        	ExamPeriod period = (ExamPeriod)i.next();
            endDate = Math.max(endDate, period.getDateOffset());
        }
        return endDate;
    }

    
    public int getFirstStart(int examType) {
        int startSlot = -1;
        for (Iterator i=iPeriods[examType].iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (startSlot<0) startSlot = period.getStartSlot();
            else startSlot = Math.min(startSlot, period.getStartSlot());
        }
        return startSlot;
    }
    
    public int getLastEnd(int examType) {
        int endSlot = -1;
        for (Iterator i=iPeriods[examType].iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (endSlot<0) endSlot = period.getEndSlot();
            else endSlot = Math.max(endSlot, period.getEndSlot());
        }
        return endSlot;
    }

    public Vector<ComboBoxLookup> getStartTimes(int examType) {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        HashSet added = new HashSet();
        for (Iterator i=iPeriods[examType].iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (added.add(period.getStartSlot())) {
                ret.addElement(new ComboBoxLookup(ExamGridTable.sTF.format(period.getStartTime()), period.getStartSlot().toString()));
            }
        }
        return ret;
    }
    
    public Vector<ComboBoxLookup> getEndTimes(int examType) {
        Vector<ComboBoxLookup> ret = new Vector<ComboBoxLookup>();
        HashSet added = new HashSet();
        for (Iterator i=iPeriods[examType].iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (added.add(period.getEndSlot())) {
                ret.addElement(new ComboBoxLookup(ExamGridTable.sTF.format(period.getEndTime()), String.valueOf(period.getEndSlot())));
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

    public int getExamType() { return iExamType; }
    public void setExamType(int type) { iExamType = type; }
    public Collection getExamTypes() {
    	Vector ret = new Vector(Exam.sExamTypes.length);
        for (int i=0;i<Exam.sExamTypes.length;i++) {
            if (i==Exam.sExamTypeEvening && !iHasEveningExams) continue;
            ret.add(new ComboBoxLookup(Exam.sExamTypes[i], String.valueOf(i)));
        }
    	return ret;
    }
    
    public int getSessionBeginWeek() { return iSessionBeginWeek; }
    public Date getSessionBeginDate() { return iSessionBeginDate; }
}
