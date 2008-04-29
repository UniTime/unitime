package org.unitime.timetable.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

public class EveningPeriodPreferenceModel {
    private TreeSet<Integer> iDates = new TreeSet<Integer>();
    private Vector<Integer> iStarts = new Vector<Integer>();
    private Hashtable<Integer,String[]> iPreferences = new Hashtable<Integer,String[]>();
    private Hashtable<Integer,Integer> iLength = new Hashtable<Integer,Integer>();
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
            if (!iStarts.contains(period.getStartSlot())) {
            	iStarts.add(period.getStartSlot());
            	iLength.put(period.getStartSlot(), period.getLength());
            }
            iDates.add(period.getDateOffset());
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
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            String[] pref = iPreferences.get(period.getDateOffset());
            if (pref==null) { pref = new String[] {"@","@"}; iPreferences.put(period.getDateOffset(),pref); }
            boolean early = (iStarts.indexOf(period.getStartSlot())==0);
            pref[early?0:1] = PreferenceLevel.sNeutral;
        }
    }
    
    public boolean canDo() {
        if (iStarts.size()!=2) return false;
        if (iStarts.size()*iDates.size()!=iPeriods.size()) return false;
        return true;
    }
    
    public void load(PreferenceGroup pg) {
        for (Iterator i=pg.getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
        	ExamPeriodPref pref = (ExamPeriodPref)i.next();
        	String[] currentPref = iPreferences.get(pref.getExamPeriod().getDateOffset());
        	boolean early = (iStarts.indexOf(pref.getExamPeriod().getStartSlot())==0);
        	currentPref[early?0:1] = pref.getPrefLevel().getPrefProlog();
        }
        boolean hasReq = false, noPref = true;
        for (Integer date: iDates) {
            String[] prefs = iPreferences.get(date);
            if (PreferenceLevel.sRequired.equals(prefs[0]) || PreferenceLevel.sRequired.equals(prefs[1])) {
                hasReq = true; break;
            } 
        }
        if (hasReq) {
            for (Integer date: iDates) {
                String[] prefs = iPreferences.get(date);
                prefs[0] = (PreferenceLevel.sRequired.equals(prefs[0])?PreferenceLevel.sNeutral:PreferenceLevel.sProhibited);
                prefs[1] = (PreferenceLevel.sRequired.equals(prefs[1])?PreferenceLevel.sNeutral:PreferenceLevel.sProhibited);
            }
        }
    }
    
    public void invertRequired() {
        for (Integer date: iDates) {
            String[] prefs = iPreferences.get(date);
            prefs[0] = (PreferenceLevel.sRequired.equals(prefs[0])?PreferenceLevel.sNeutral:PreferenceLevel.sProhibited);
            prefs[1] = (PreferenceLevel.sRequired.equals(prefs[1])?PreferenceLevel.sNeutral:PreferenceLevel.sProhibited);
        }
    }
    
    public void save(Set preferences, PreferenceGroup pg) {
    	for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            boolean early = (iStarts.indexOf(period.getStartSlot())==0);
            String[] pref = iPreferences.get(period.getDateOffset());
            if (!PreferenceLevel.sNeutral.equals(pref[early?0:1])) {
            	ExamPeriodPref p = new ExamPeriodPref();
                p.setOwner(pg);
                p.setExamPeriod(period);
                p.setPrefLevel(PreferenceLevel.getPreferenceLevel(pref[early?0:1]));
                preferences.add(p);
            }
        }
    }
    
    public void load(Location location) {
    	iLocation = true;
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            String[] currentPref = iPreferences.get(period.getDateOffset());
            boolean early = (iStarts.indexOf(period.getStartSlot())==0);
            currentPref[early?0:1] = location.getExamPreference(period).getPrefProlog();
        }
        boolean hasReq = false;
        for (Integer date: iDates) {
            String[] prefs = iPreferences.get(date);
            if (PreferenceLevel.sRequired.equals(prefs[0]) || PreferenceLevel.sRequired.equals(prefs[1])) {
                hasReq = true; break;
            }
        }
        if (hasReq) {
            for (Integer date: iDates) {
                String[] prefs = iPreferences.get(date);
                prefs[0] = (PreferenceLevel.sRequired.equals(prefs[0])?PreferenceLevel.sNeutral:PreferenceLevel.sProhibited);
                prefs[1] = (PreferenceLevel.sRequired.equals(prefs[1])?PreferenceLevel.sNeutral:PreferenceLevel.sProhibited);
            }
        }
    }

    public void save(Location location) {
        location.clearExamPreferences(Exam.sExamTypeEvening);
    	for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            boolean early = (iStarts.indexOf(period.getStartSlot())==0);
            String[] pref = iPreferences.get(period.getDateOffset());
            if (!PreferenceLevel.sNeutral.equals(pref[early?0:1])) {
                location.addExamPreference(period, PreferenceLevel.getPreferenceLevel(pref[early?0:1]));
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
	    return print(editable, 0);
	}
	
    public String print(boolean editable, int length) throws Exception {
		StringBuffer border = new StringBuffer("[");
		StringBuffer patternEarly = new StringBuffer("[");
		StringBuffer patternLate = new StringBuffer("[");
		boolean earlyProh = true, lateProh = true;
		for (int m=getStartMonth();m<=getEndMonth();m++) {
			if (m!=getStartMonth()) { border.append(","); patternEarly.append(","); patternLate.append(","); }
			border.append("["); patternEarly.append("["); patternLate.append("[");
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, getYear());;
			for (int d=1;d<=daysOfMonth;d++) {
				if (d>1) { border.append(","); patternEarly.append(","); patternLate.append(","); }
				Integer date = getDateOffset(d, m);
				String[] pref = iPreferences.get(date);
				boolean hasPeriod = iDates.contains(date);
				if (hasPeriod && !"@".equals(pref[0]) && !PreferenceLevel.sProhibited.equals(pref[0])) earlyProh = false;
				if (hasPeriod && !"@".equals(pref[1]) && !PreferenceLevel.sProhibited.equals(pref[1])) lateProh = false;
				border.append(getBorder(d,m));
				patternEarly.append(hasPeriod?"'"+pref[0]+"'":"'@'");
				patternLate.append(hasPeriod?"'"+pref[1]+"'":"'@'");
			}
			border.append("]");
			patternEarly.append("]");
			patternLate.append("]");
		}
		border.append("]");
		patternEarly.append("]");
		patternLate.append("]");
		int firstStartHour = (Constants.SLOT_LENGTH_MIN*iStarts.firstElement()+Constants.FIRST_SLOT_TIME_MIN) / 60;
		int firstStartMin = (Constants.SLOT_LENGTH_MIN*iStarts.firstElement()+Constants.FIRST_SLOT_TIME_MIN) % 60;
		String firstStart = (firstStartHour>12?firstStartHour-12:firstStartHour)+":"+(firstStartMin<10?"0":"")+firstStartMin+(firstStartHour>=12?"p":"a");
		int firstEndHour = (Constants.SLOT_LENGTH_MIN*(iStarts.firstElement()+iLength.get(iStarts.firstElement()))+Constants.FIRST_SLOT_TIME_MIN) / 60;
		int firstEndMin = (Constants.SLOT_LENGTH_MIN*(iStarts.firstElement()+iLength.get(iStarts.firstElement()))+Constants.FIRST_SLOT_TIME_MIN) % 60;
		String firstEnd = (firstEndHour>12?firstEndHour-12:firstEndHour)+":"+(firstEndMin<10?"0":"")+firstEndMin+(firstEndHour>=12?"p":"a");
        int lastStartHour = (Constants.SLOT_LENGTH_MIN*iStarts.lastElement()+Constants.FIRST_SLOT_TIME_MIN) / 60;
        int lastStartMin = (Constants.SLOT_LENGTH_MIN*iStarts.lastElement()+Constants.FIRST_SLOT_TIME_MIN) % 60;
        String lastStart = (lastStartHour>12?lastStartHour-12:lastStartHour)+":"+(lastStartMin<10?"0":"")+lastStartMin+(lastStartHour>=12?"p":"a");
        int lastEndHour = (Constants.SLOT_LENGTH_MIN*(iStarts.lastElement()+iLength.get(iStarts.lastElement()))+Constants.FIRST_SLOT_TIME_MIN) / 60;
        int lastEndMin = (Constants.SLOT_LENGTH_MIN*(iStarts.lastElement()+iLength.get(iStarts.lastElement()))+Constants.FIRST_SLOT_TIME_MIN) % 60;
        String lastEnd = (lastEndHour>12?lastEndHour-12:lastEndHour)+":"+(lastEndMin<10?"0":"")+lastEndMin+(lastEndHour>=12?"p":"a");
        StringBuffer legendCode = new StringBuffer("[");
        StringBuffer legendText = new StringBuffer("[");
        StringBuffer legendColor = new StringBuffer("[");
        Vector prefs = new Vector(PreferenceLevel.getPreferenceLevelList(false));
        prefs.remove(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
        for (Enumeration e=prefs.elements();e.hasMoreElements();) {
            PreferenceLevel p = (PreferenceLevel)e.nextElement();
            legendCode.append("'"+p.getPrefProlog()+"',");
            legendText.append("'"+p.getPrefName()+"',");
            /*
            if (!iLocation && PreferenceLevel.sNeutral.equals(p.getPrefProlog())) {
                legendColor.append("'"+PreferenceLevel.prolog2bgColor(PreferenceLevel.sDiscouraged)+"',");
            } else if (!iLocation && PreferenceLevel.sDiscouraged.equals(p.getPrefProlog())) {
                legendColor.append("'"+PreferenceLevel.prolog2bgColor(PreferenceLevel.sStronglyDiscouraged)+"',");
            } else if (!iLocation && PreferenceLevel.sStronglyDiscouraged.equals(p.getPrefProlog())) {
                legendColor.append("'"+PreferenceLevel.prolog2bgColor(PreferenceLevel.sProhibited)+"',");
            } else if (!iLocation && PreferenceLevel.sProhibited.equals(p.getPrefProlog())) {
                legendColor.append("'"+PreferenceLevel.prolog2bgColor(PreferenceLevel.sNeutral)+"',");
            } else */ 
            legendColor.append("'"+PreferenceLevel.prolog2bgColor(p.getPrefProlog())+"',");
        }
        legendCode.append("'@']");
        legendText.append("'No Period']");
        legendColor.append("'rgb(150,150,150)']");
        boolean early = (length <= Constants.SLOT_LENGTH_MIN * iLength.get(iStarts.firstElement())) && (editable || iLocation || !earlyProh);
        boolean late = (length <= Constants.SLOT_LENGTH_MIN * iLength.get(iStarts.lastElement())) && (editable || iLocation || !lateProh);
        StringBuffer sb = new StringBuffer(); 
        sb.append("<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>");
		sb.append("<script language='JavaScript'>");
		if (early) sb.append(
			"calGenerate("+getYear()+","+
				getStartMonth()+","+
				getEndMonth()+","+
				patternEarly+","+
                legendCode+","+legendText+","+legendColor+",'0',"+
				border+","+editable+","+editable+","+
				"'early','("+firstStart+" - "+firstEnd+")',6,true,"+!late+");");
		if (late) sb.append(
            "calGenerate("+getYear()+","+
                getStartMonth()+","+
                getEndMonth()+","+
                patternLate+","+
                legendCode+","+legendText+","+legendColor+",'0',"+
                border+","+editable+","+!early+","+
                "'late','("+lastStart+" - "+lastEnd+")',6,"+!early+",true);");
        sb.append("</script>");
        if (!early && !late) {
            if (length > Constants.SLOT_LENGTH_MIN * iLength.get(iStarts.firstElement()) && length > Constants.SLOT_LENGTH_MIN * iLength.get(iStarts.lastElement())) 
                sb.append("<font color='red'>Examination is too long, no period is availabile.</font>");
            else
                sb.append("<font color='red'>No period is availabile.</font>");
        }
		return sb.toString();
	}
    
	public void load(HttpServletRequest request) {
		iPreferences.clear();
		for (int m=getStartMonth();m<=getEndMonth();m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, getYear());;
			for (int d=1;d<=daysOfMonth;d++) {
			    if (!iDates.contains(getDateOffset(d,m))) continue; 
			    iPreferences.put(getDateOffset(d,m), new String[] {
                    request.getParameter("early_val_"+((12+m)%12)+"_"+d),
                    request.getParameter("late_val_"+((12+m)%12)+"_"+d)});
			}
		}
	}
	
	public String toString() {
	    return toString(false);
	}
	
    public String toString(boolean html) {
    	SimpleDateFormat df = new SimpleDateFormat("MM/dd");
        StringBuffer sb = new StringBuffer();
        String[] fPref = null; 
        int fDate = -1, lDate = -1;
        for (Integer date: iDates) {
        	String[] pref = iPreferences.get(date);
        	if (fPref==null) {
        	    fPref = pref; fDate = date;
        	} else if (!fPref[0].equals(pref[0]) || !fPref[1].equals(pref[1])) {
        	    if (fPref[0].equals(fPref[1])) {
        	        if (iLocation && PreferenceLevel.sNeutral.equals(fPref[0])) {
        	            //
        	        } else if (!iLocation && PreferenceLevel.sProhibited.equals(fPref[0])) {
        	            //
        	        } else {
        	            if (sb.length()>0) sb.append(", ");
        	            if (html) {
        	                sb.append("<span style='color:"+PreferenceLevel.prolog2color(fPref[0])+";' title='");
                            sb.append(PreferenceLevel.prolog2string(fPref[0])+" ");
                            sb.append(df.format(getDate(fDate)));
                            if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
        	                sb.append("'>");
        	                sb.append(df.format(getDate(fDate)));
                            if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                            sb.append("</span>");
        	            } else {
        	                sb.append(PreferenceLevel.prolog2abbv(fPref[0])+" ");
        	                sb.append(df.format(getDate(fDate)));
        	                if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
        	            }
        	        }
        	    } else {
                    if ("@".equals(fPref[0])) {
                        //
                    } else if (iLocation && PreferenceLevel.sNeutral.equals(fPref[0])) {
                        //
                    } else if (!iLocation && PreferenceLevel.sProhibited.equals(fPref[0])) {
                        //
                    } else {
                        if (sb.length()>0) sb.append(", ");
                        if (html) {
                            sb.append("<span style='color:"+PreferenceLevel.prolog2color(fPref[0])+";' title='");
                            sb.append(PreferenceLevel.prolog2string(fPref[0])+" Early ");
                            sb.append(df.format(getDate(fDate)));
                            if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                            sb.append("'>");
                            sb.append("Early ");
                            sb.append(df.format(getDate(fDate)));
                            if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                            sb.append("</span>");
                        } else {
                            sb.append(PreferenceLevel.prolog2abbv(fPref[0])+" Early ");
                            sb.append(df.format(getDate(fDate)));
                            if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                        }
                    }
                    if ("@".equals(fPref[1])) {
                        //
                    } else if (iLocation && PreferenceLevel.sNeutral.equals(fPref[1])) {
                        //
                    } else if (!iLocation && PreferenceLevel.sProhibited.equals(fPref[1])) {
                        //
                    } else {
                        if (sb.length()>0) sb.append(", ");
                        if (html) {
                            sb.append("<span style='color:"+PreferenceLevel.prolog2color(fPref[1])+";' title='");
                            sb.append(PreferenceLevel.prolog2string(fPref[1])+" Late ");
                            sb.append(df.format(getDate(fDate)));
                            if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                            sb.append("'>");
                            sb.append("Late ");
                            sb.append(df.format(getDate(fDate)));
                            if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                            sb.append("</span>");
                        } else {
                            sb.append(PreferenceLevel.prolog2abbv(fPref[1])+" Late ");
                            sb.append(df.format(getDate(fDate)));
                            if (fDate!=lDate) 
                                sb.append(" - "+df.format(getDate(lDate)));
                        }
                    }
        	    }
        	    fPref = pref; fDate = date;
        	}
        	lDate = date;
        }
        if (fPref!=null) {
            if (fPref[0].equals(fPref[1])) {
                if (iLocation && PreferenceLevel.sNeutral.equals(fPref[0])) {
                    //
                } else if (!iLocation && PreferenceLevel.sProhibited.equals(fPref[0])) {
                    //
                } else {
                    if (sb.length()>0) sb.append(", ");
                    if (html) {
                        sb.append("<span style='color:"+PreferenceLevel.prolog2color(fPref[0])+";' title='");
                        sb.append(PreferenceLevel.prolog2string(fPref[0])+" ");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                        sb.append("'>");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                        sb.append("</span>");
                    } else {
                        sb.append(PreferenceLevel.prolog2abbv(fPref[0])+" ");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                    }
                }
            } else {
                if ("@".equals(fPref[0])) {
                    //
                } else if (iLocation && PreferenceLevel.sNeutral.equals(fPref[0])) {
                    //
                } else if (!iLocation && PreferenceLevel.sProhibited.equals(fPref[0])) {
                    //
                } else {
                    if (sb.length()>0) sb.append(", ");
                    if (html) {
                        sb.append("<span style='color:"+PreferenceLevel.prolog2color(fPref[0])+";' title='");
                        sb.append(PreferenceLevel.prolog2string(fPref[0])+" Early ");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                        sb.append("'>");
                        sb.append("Early ");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                        sb.append("</span>");
                    } else {
                        sb.append(PreferenceLevel.prolog2abbv(fPref[0])+" Early ");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                    }
                }
                if ("@".equals(fPref[1])) {
                    //
                } else if (iLocation && PreferenceLevel.sNeutral.equals(fPref[1])) {
                    //
                } else if (!iLocation && PreferenceLevel.sProhibited.equals(fPref[1])) {
                    //
                } else {
                    if (sb.length()>0) sb.append(", ");
                    if (html) {
                        sb.append("<span style='color:"+PreferenceLevel.prolog2color(fPref[1])+";' title='");
                        sb.append(PreferenceLevel.prolog2string(fPref[1])+" Late ");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                        sb.append("'>");
                        sb.append("Late ");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) sb.append(" - "+df.format(getDate(lDate)));
                        sb.append("</span>");
                    } else {
                        sb.append(PreferenceLevel.prolog2abbv(fPref[1])+" Late ");
                        sb.append(df.format(getDate(fDate)));
                        if (fDate!=lDate) 
                            sb.append(" - "+df.format(getDate(lDate)));
                    }
                }
            }
        }
        /*
        if (iLocation && fPref!=null && fDate==iDates.first() && lDate==iDates.last()) {
            if (fPref[0].equals(fPref[1])) {
                if (PreferenceLevel.sNeutral.equals(fPref[0])) {
                    return "";
                } else if (PreferenceLevel.sProhibited.equals(fPref[0])) {
                    return "Not Available";
                } else {
                    return PreferenceLevel.getPreferenceLevel(fPref[0]).getPrefName();
                }
            } else {
                String ret = "";
                if ("@".equals(fPref[0])) {
                } else if (PreferenceLevel.sNeutral.equals(fPref[0])) {
                } else if (PreferenceLevel.sProhibited.equals(fPref[0])) {
                    ret += "Not Available Early";
                } else {
                    ret += PreferenceLevel.getPreferenceLevel(fPref[0]).getPrefName()+" Early";
                }
                if ("@".equals(fPref[1])) {
                } else if (PreferenceLevel.sNeutral.equals(fPref[1])) {
                } else if (PreferenceLevel.sProhibited.equals(fPref[1])) {
                    if (ret.length()>0) ret+=", ";
                    ret += "Not Available Late";
                } else {
                    ret += PreferenceLevel.getPreferenceLevel(fPref[0]).getPrefName()+" Late";
                }
            }
        }
        */
        return sb.toString();//(iLocation?sb.toString().replaceAll(PreferenceLevel.prolog2abbv(PreferenceLevel.sProhibited),"N/A"):sb.toString());
    }
}
