package org.unitime.timetable.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.util.DateUtils;

public class EveningPeriodPreferenceModel {
    private TreeSet<Integer> iDates = new TreeSet<Integer>();
    private Vector<Integer> iStarts = new Vector<Integer>();
    private Hashtable<Integer,Integer> iPreferences = new Hashtable<Integer,Integer>();
    private TreeSet iPeriods = null;
    private Date iFirstDate = null, iLastDate = null;
    private boolean iLocation = false;
    
    private ExamPeriod iPeriod = null;
    private Date iExamBeginDate = null;
    private Session iSession = null;
    
    public static SimpleDateFormat sDF = new SimpleDateFormat("EEE MM/dd");
    
    public EveningPeriodPreferenceModel(Session session) {
        this(session, null);
    }

    public EveningPeriodPreferenceModel(Session session, ExamAssignment assignment) {
        iPeriod = (assignment==null?null:assignment.getPeriod());
        iSession = session;
        iExamBeginDate = session.getExamBeginDate();
        iPeriods = ExamPeriod.findAll(session.getUniqueId(), Exam.sExamTypeEvening);
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (!iStarts.contains(period.getStartSlot()))
            	iStarts.add(period.getStartSlot());
            iDates.add(period.getDateOffset());
            iPreferences.put(period.getDateOffset(),0);
            if (iFirstDate==null) {
            	iFirstDate = period.getStartDate(); iLastDate = period.getStartDate();
            } else {
            	if (period.getStartDate().compareTo(iFirstDate)<0)
            		iFirstDate = period.getStartDate();
            	if (period.getStartDate().compareTo(iLastDate)>0)
            		iLastDate = period.getStartDate();
            }
        }
        Collections.sort(iStarts);
    }
    
    public boolean canDo() {
        if (iStarts.size()>2) return false;
        if (iStarts.size()*iDates.size()!=iPeriods.size()) return false;
        return true;
    }
    
    public void load(PreferenceGroup pg) {
        for (Iterator i=pg.getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
        	ExamPeriodPref pref = (ExamPeriodPref)i.next();
        	Integer currentPref = iPreferences.get(pref.getExamPeriod().getDateOffset());
        	int bit = 1<<iStarts.indexOf(pref.getExamPeriod().getStartSlot());
        	iPreferences.put(pref.getExamPeriod().getDateOffset(), (currentPref==null?0:currentPref.intValue()) | bit);
        }
    }
    
    public void save(Set preferences, PreferenceGroup pg) {
    	for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            Integer pref = iPreferences.get(period.getDateOffset());
            if (pref==null) continue;
            int bit = 1<<iStarts.indexOf(period.getStartSlot());
            if ((pref.intValue() & bit)!=0) {
            	ExamPeriodPref p = new ExamPeriodPref();
                p.setOwner(pg);
                p.setExamPeriod(period);
                p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                preferences.add(p);
            }
        }
    }
    
    public void load(Location location) {
    	iLocation = true;
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (PreferenceLevel.sProhibited.equals(location.getExamPreference(period).getPrefProlog())) {
            	Integer currentPref = iPreferences.get(period.getDateOffset());
            	int bit = 1<<iStarts.indexOf(period.getStartSlot());
            	iPreferences.put(period.getDateOffset(), (currentPref==null?0:currentPref.intValue()) | bit);
            }
        }
    }

    public void save(Location location) {
    	for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            Integer pref = iPreferences.get(period.getDateOffset());
            if (pref==null) continue;
            int bit = 1<<iStarts.indexOf(period.getStartSlot());
            if ((pref.intValue() & bit)!=0) {
                location.addExamPreference(period, PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited));
            }
        }
    }
    
	public int getExamOffset() {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(iExamBeginDate);
		return cal.get(Calendar.DAY_OF_YEAR);
	}
    
    public Integer getDateOffset(int day, int month) {
    	return (1+DateUtils.getDayOfYear(day, month, getYear())-getExamOffset());
    }

    public Date getDate(Integer dateOffset) {
    	Calendar cal = Calendar.getInstance(Locale.US);
    	cal.setTime(iExamBeginDate);
    	cal.add(Calendar.DAY_OF_YEAR, dateOffset);
    	return cal.getTime();
    }
    
    public int getStartMonth() {
    	Calendar begin = Calendar.getInstance(Locale.US);
    	begin.setTime(iExamBeginDate);
    	Calendar first = Calendar.getInstance(Locale.US);
    	first.setTime(iFirstDate);
    	int month = first.get(Calendar.MONTH);
    	if (first.get(Calendar.YEAR)!=begin.get(Calendar.YEAR)) month-=12;
    	return month;
    }
    
    public int getEndMonth() {
    	Calendar begin = Calendar.getInstance(Locale.US);
    	begin.setTime(iExamBeginDate);
    	Calendar last = Calendar.getInstance(Locale.US);
    	last.setTime(iLastDate);
    	int month = last.get(Calendar.MONTH);
    	if (last.get(Calendar.YEAR)!=begin.get(Calendar.YEAR)) month+=12;
    	return month;
    }
    
    public int getYear() {
    	Calendar begin = Calendar.getInstance(Locale.US);
    	begin.setTime(iExamBeginDate);
    	return begin.get(Calendar.YEAR);
    }
    
	public String getBorder(int day, int month) {
		if (iPeriod!=null && iPeriod.getDateOffset().equals(getDateOffset(day, month)))
			return "'purple 2px solid'";
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(iSession.getSessionBeginDateTime());
		if (day==cal.get(Calendar.DAY_OF_MONTH) && month==cal.get(Calendar.MONTH))
			return "'blue 2px solid'";
		cal.setTime(iSession.getSessionEndDateTime());
		if (day==cal.get(Calendar.DAY_OF_MONTH) && month==cal.get(Calendar.MONTH))
			return "'blue 2px solid'";
		cal.setTime(iSession.getExamBeginDate());
		if (day==cal.get(Calendar.DAY_OF_MONTH) && month==cal.get(Calendar.MONTH))
			return "'green 2px solid'";
		int holiday = iSession.getHoliday(day, month);
		if (holiday!=Session.sHolidayTypeNone)
			return "'"+Session.sHolidayTypeColors[holiday]+" 2px solid'";
		return "null";
	}
	
	public String getColor(Integer pref) {
		if (pref==null) return PreferenceLevel.prolog2bgColor(PreferenceLevel.sNeutral);
		if (iLocation) {
			switch (pref.intValue()) {
			case  1 : return PreferenceLevel.prolog2bgColor(PreferenceLevel.sDiscouraged); 
			case  2 : return PreferenceLevel.prolog2bgColor(PreferenceLevel.sStronglyDiscouraged); 
			case  3 : return PreferenceLevel.prolog2bgColor(PreferenceLevel.sProhibited);
			default : return PreferenceLevel.prolog2bgColor(PreferenceLevel.sNeutral); 
			}
		}
			
		switch (pref.intValue()) {
		case  1 : return PreferenceLevel.prolog2bgColor(PreferenceLevel.sPreferred); 
		case  2 : return PreferenceLevel.prolog2bgColor(PreferenceLevel.sStronglyPreferred); 
		case  3 : return PreferenceLevel.prolog2bgColor(PreferenceLevel.sRequired);
		default : return PreferenceLevel.prolog2bgColor(PreferenceLevel.sNeutral); 
		}
	}

    public String print(boolean editable) throws Exception {
		StringBuffer border = new StringBuffer("[");
		StringBuffer pattern = new StringBuffer("[");
		for (int m=getStartMonth();m<=getEndMonth();m++) {
			if (m!=getStartMonth()) { border.append(","); pattern.append(","); }
			border.append("["); pattern.append("[");
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, getYear());;
			for (int d=1;d<=daysOfMonth;d++) {
				if (d>1) { border.append(","); pattern.append(","); }
				Integer date = getDateOffset(d, m);
				Integer pref = iPreferences.get(date);
				boolean hasPeriod = iDates.contains(date);
				border.append(getBorder(d,m));
				pattern.append(hasPeriod?"'"+(pref==null?0:pref.intValue())+"'":"'@'");
			}
			border.append("]");
			pattern.append("]");
		}
		border.append("]");
		pattern.append("]");
		StringBuffer sb = new StringBuffer(); 
        sb.append("<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>");
		sb.append("<script language='JavaScript'>");
		sb.append(
			"calGenerate("+getYear()+","+
				getStartMonth()+","+
				getEndMonth()+","+
				pattern+","+
				"['3','2','1','0','@'],"+
				(iLocation?"['Not Available','Available Erly Period Only','Available Late Priod Only','Available','No Period'],":"['Both Periods','Late Period','Early Priod','Not Available','No Period'],")+
				"['"+getColor(3)+"','"+getColor(2)+"','"+getColor(1)+"','"+getColor(0)+"','rgb(150,150,150)'],"+
				"'3',"+
				border+","+editable+","+true+");");
		sb.append("</script>");
		return sb.toString();
	}
    
	public void load(HttpServletRequest request) {
		iPreferences.clear();
		for (int m=getStartMonth();m<=getEndMonth();m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, getYear());;
			for (int d=1;d<=daysOfMonth;d++) {
				String pref = request.getParameter("cal_val_"+((12+m)%12)+"_"+d);
				if (pref==null || "0".equals(pref) || "@".equals(pref)) continue;
				iPreferences.put(getDateOffset(d, m), Integer.valueOf(pref));
			}
		}
	}
    
    public String toString() {
    	SimpleDateFormat df = new SimpleDateFormat("MM/dd");
        StringBuffer sb = new StringBuffer();
        for (Integer date: iDates) {
        	Integer pref = iPreferences.get(date);
        	if (pref==null || pref.intValue()==0) continue;
        	if (sb.length()>0) sb.append(", ");
        	if (pref.intValue()==3)
        		sb.append(df.format(getDate(date)));
        	if (pref.intValue()==1)
        		sb.append("Early "+df.format(getDate(date)));
        	if (pref.intValue()==2)
        		sb.append("Late "+df.format(getDate(date)));
        }
        return sb.toString();
    }
}
