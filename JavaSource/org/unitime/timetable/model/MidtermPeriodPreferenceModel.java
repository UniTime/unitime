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
package org.unitime.timetable.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class MidtermPeriodPreferenceModel {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
    private TreeSet<Integer> iDates = new TreeSet<Integer>();
    private TreeSet<Integer> iStarts = new TreeSet<Integer>();
    private Hashtable<Integer,Hashtable<Integer,String>> iPreferences = new Hashtable<Integer,Hashtable<Integer,String>>();
    private Hashtable<Integer,Integer> iLength = new Hashtable<Integer,Integer>();
    private TreeSet iPeriods = null;
    private Date iFirstDate = null, iLastDate = null;
    private boolean iLocation = false;
    
    private ExamPeriod iPeriod = null;
    private Date iExamBeginDate = null;
    private Session iSession = null;
    private ExamType iExamType = null;
    private String iName = "mp";
    
    public MidtermPeriodPreferenceModel(Session session, ExamType type) {
        this(session, type, (ExamPeriod)null);
    }
    
    public MidtermPeriodPreferenceModel(Session session, ExamType type, ExamAssignment assignment) {
    	this(session, type, assignment == null ? null : assignment.getPeriod());
    }

    public MidtermPeriodPreferenceModel(Session session, ExamType type, ExamPeriod assignment) {
        iPeriod = assignment;
        iSession = session;
        iExamType = type;
        iExamBeginDate = session.getExamBeginDate();
        iPeriods = ExamPeriod.findAll(session.getUniqueId(), type);
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
           	iStarts.add(period.getStartSlot());
           	iLength.put(period.getStartSlot(), period.getLength());
            iDates.add(period.getDateOffset());
            Hashtable<Integer,String> pref = iPreferences.get(period.getDateOffset());
            if (pref==null) {
                pref = new Hashtable(); iPreferences.put(period.getDateOffset(), pref);
            }
            pref.put(period.getStartSlot(), PreferenceLevel.sProhibited);
            if (iFirstDate==null) {
            	iFirstDate = period.getStartDate(); iLastDate = period.getStartDate();
            } else {
            	if (period.getStartDate().compareTo(iFirstDate)<0)
            		iFirstDate = period.getStartDate();
            	if (period.getStartDate().compareTo(iLastDate)>0)
            		iLastDate = period.getStartDate();
            }
        }
    }
    
    public void setName(String name) { iName = name; }
    
    public void load(PreferenceGroup pg) {
        for (Iterator i=pg.getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
        	ExamPeriodPref pref = (ExamPeriodPref)i.next();
        	if (!iExamType.equals(pref.getExamPeriod().getExamType())) continue;
        	iPreferences.get(pref.getExamPeriod().getDateOffset()).put(pref.getExamPeriod().getStartSlot(), pref.getPrefLevel().getPrefProlog());
        }
        invertIfNeeded();
    }
    
    private void invertIfNeeded() {
        boolean hasReq = false;
        dates: for (Integer date: iDates) {
            Hashtable<Integer,String> pref = iPreferences.get(date);
            for (Integer time: iStarts) {
                if (PreferenceLevel.sRequired.equals(pref.get(time)))
                    {hasReq = true; break dates; }
            }
        }
        if (hasReq) {
            for (Integer date: iDates) {
                Hashtable<Integer,String> pref = iPreferences.get(date);
                for (Integer time: iStarts) {
                    if (pref.get(time)==null) continue;
                    pref.put(time, PreferenceLevel.sRequired.equals(pref.get(time))?PreferenceLevel.sNeutral:PreferenceLevel.sProhibited);
                }
            }
        }
    }
    
    public void save(Set preferences, PreferenceGroup pg) {
    	for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            String pref = iPreferences.get(period.getDateOffset()).get(period.getStartSlot());
            if (pref!=null && !PreferenceLevel.sProhibited.equals(pref)) {
            	ExamPeriodPref p = new ExamPeriodPref();
                p.setOwner(pg);
                p.setExamPeriod(period);
                p.setPrefLevel(PreferenceLevel.getPreferenceLevel(pref));
                preferences.add(p);
            }
        }
    }
    
    public void load(Location location) {
    	iLocation = true;
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            iPreferences.get(period.getDateOffset()).put(period.getStartSlot(),location.getExamPreference(period).getPrefProlog());
        }
        invertIfNeeded();
    }

    public void save(Location location) {
        location.clearExamPreferences(iExamType);
    	for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            String pref = iPreferences.get(period.getDateOffset()).get(period.getStartSlot());
            if (pref!=null && !PreferenceLevel.sNeutral.equals(pref)) {
                location.addExamPreference(period, PreferenceLevel.getPreferenceLevel(pref));
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
    	if (first.get(Calendar.YEAR)!=begin.get(Calendar.YEAR)) month-=(12*(begin.get(Calendar.YEAR)-first.get(Calendar.YEAR)));
    	return month;
    }
    
     public int getEndMonth() {
    	Calendar begin = Calendar.getInstance(Locale.US);
    	begin.setTime(iExamBeginDate);
    	Calendar last = Calendar.getInstance(Locale.US);
    	last.setTime(iLastDate);
    	int month = last.get(Calendar.MONTH);
    	if (last.get(Calendar.YEAR)!=begin.get(Calendar.YEAR)) month+=(12*(last.get(Calendar.YEAR)-begin.get(Calendar.YEAR)));
    	return month;
    }
    
    public int getYear() {
    	Calendar begin = Calendar.getInstance(Locale.US);
    	begin.setTime(iExamBeginDate);
    	return begin.get(Calendar.YEAR);
    }
    
	public String getBorder(int day, int month, int start) {
		if (iPeriod!=null && iPeriod.getDateOffset().equals(getDateOffset(day, month)) && iPeriod.getStartSlot().equals(start))
			return "'purple 2px solid'";
		int m = month;
		int sessStartYr = iSession.getSessionStartYear(); 
		if (sessStartYr < getYear()){
			m += (12  * (getYear() - sessStartYr));
		} else if (sessStartYr > getYear()){
			m -= (12 * (sessStartYr - getYear()));
		}
		return iSession.getBorder(day, m);
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
	
	private String getBorderArray(Integer start) {
        StringBuffer border = new StringBuffer("[");
        for (int m=getStartMonth();m<=getEndMonth();m++) {
            if (m!=getStartMonth()) border.append(","); 
            border.append("["); 
            int daysOfMonth = DateUtils.getNrDaysOfMonth(m, getYear());;
            for (int d=1;d<=daysOfMonth;d++) {
                if (d>1) border.append(","); 
                border.append(getBorder(d,m,start));
            }
            border.append("]");
        }
        border.append("]");
        return border.toString();
	}
	
	private String getPattern(Integer start) {
        StringBuffer pattern = new StringBuffer("[");
        for (int m=getStartMonth();m<=getEndMonth();m++) {
            if (m!=getStartMonth()) pattern.append(",");
            pattern.append("[");
            int daysOfMonth = DateUtils.getNrDaysOfMonth(m, getYear());;
            for (int d=1;d<=daysOfMonth;d++) {
                if (d>1) pattern.append(",");
                Integer date = getDateOffset(d, m);
                if (iDates.contains(date)) {
                    String pref = iPreferences.get(date).get(start);
                    if (pref==null)
                        pattern.append("'@'");
                    else
                        pattern.append("'"+pref+"'");
                } else 
                    pattern.append("'@'");
            }
            pattern.append("]");
        }
        pattern.append("]");
        return pattern.toString();
	}
	
	private boolean isAllProhibited(Integer start) {
	    for (Integer date : iDates) {
	        String pref = iPreferences.get(date).get(start);
	        if (pref!=null && !PreferenceLevel.sProhibited.equals(pref)) return false;
	    }
	    return true;
	}
	
    public String print(boolean editable, int length) throws Exception {
        TreeSet<Integer> starts = new TreeSet<Integer>();
        for (Integer start: iStarts) {
            if ((length <= Constants.SLOT_LENGTH_MIN * iLength.get(start)) && (editable || iLocation || !isAllProhibited(start))) 
                starts.add(start);
        }
        if (starts.isEmpty()) {
            boolean tooLong = true;
            for (Integer start: iStarts) {
                if (length <= Constants.SLOT_LENGTH_MIN * iLength.get(start)) { tooLong=false; break; }
            }
            if (tooLong)
                return "<font color='red'>Examination is too long, no period is availabile.</font>";
            else
                return "<font color='red'>No period is availabile.</font>";
        }

        StringBuffer legendCode = new StringBuffer("[");
        StringBuffer legendText = new StringBuffer("[");
        StringBuffer legendColor = new StringBuffer("[");
        for (PreferenceLevel p: PreferenceLevel.getPreferenceLevelList()) {
        	if (p.getPrefProlog().equalsIgnoreCase(PreferenceLevel.sRequired)) continue;
            legendCode.append("'"+p.getPrefProlog()+"',");
            legendText.append("'"+p.getPrefName()+"',");
            legendColor.append("'"+PreferenceLevel.prolog2bgColor(p.getPrefProlog())+"',");
        }
        legendCode.append("'@']");
        legendText.append("'No Period']");
        legendColor.append("'rgb(150,150,150)']");

        StringBuffer sb = new StringBuffer(); 
        sb.append("<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>");
        sb.append("<script language='JavaScript'>");
        int legendIdx = (starts.size()==1?0:starts.size()==2?1:starts.size()/2);
        int idx = 0;
        for (Integer start: starts) {
            String startTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*start+Constants.FIRST_SLOT_TIME_MIN);
            String endTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*(start+iLength.get(start))+Constants.FIRST_SLOT_TIME_MIN);
            sb.append(
                    "calGenerate("+getYear()+","+
                        getStartMonth()+","+
                        getEndMonth()+","+
                        getPattern(start)+","+
                        legendCode+","+legendText+","+legendColor+",'0',"+
                        getBorderArray(start)+","+editable+","+(editable && legendIdx==idx)+","+
                        "'"+iName+start+"','("+startTime+" - "+endTime+")',6,"+(start==starts.first())+","+(start==starts.last())+");");
            idx++;
        }
        sb.append("</script>");
		return sb.toString();
	}
    
	public void load(HttpServletRequest request) {
		int year = getYear();
		for (int m=getStartMonth();m<=getEndMonth();m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);;
			int yr = DateUtils.calculateActualYear(m, year);
			for (int d=1;d<=daysOfMonth;d++) {
			    if (!iDates.contains(getDateOffset(d,m))) continue; 
			    for (int start:iStarts) {
			        String pref = request.getParameter(iName+start+"_val_"+yr+"_"+((12+m)%12)+"_"+d);
			        if (pref!=null && !"@".equals(pref))
			            iPreferences.get(getDateOffset(d,m)).put(start, pref);
			    }
			}
		}
	}
	
	public String toString() {
	    return toString(false);
	}
	
    private String getLabel(int fDate, int lDate, Hashtable<Integer,String> prefs, boolean html, boolean color) {
    	Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);
        String dates = df.format(getDate(fDate))+(fDate==lDate?"":" - "+df.format(getDate(lDate)));
        String lastPref = null; int fStart = -1, lStart = -1;
        String ret = "";
        for (int start: iStarts) {
            String pref = prefs.get(start);
            if (pref==null) continue;
            if (lastPref==null) {
                lastPref = pref; fStart = start;
            } else if (!pref.equals(lastPref)) {
                if (iLocation && PreferenceLevel.sNeutral.equals(lastPref)) {
                    //
                } else if (!iLocation && PreferenceLevel.sProhibited.equals(lastPref)) {
                    //
                } else {
                    String startTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*fStart+Constants.FIRST_SLOT_TIME_MIN);
                    String endTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*(lStart+iLength.get(lStart))+Constants.FIRST_SLOT_TIME_MIN);
                    if (ret.length()>0) ret+=", ";
                    if (html) {
                        ret+="<span style='color:"+PreferenceLevel.prolog2color(lastPref)+"; white-space:nowrap;' "+
                            "title='"+PreferenceLevel.prolog2string(lastPref)+" "+
                            dates+" "+startTime+" - "+endTime+
                            "'>"+dates+" "+(iStarts.size()==2?fStart==iStarts.first()?"Early":"Late":startTime)+(fStart==lStart?"":" - "+endTime)+"</span>";
                    } else {
                    	if (color)
                    		ret += "@@COLOR " + PreferenceLevel.prolog2color(lastPref) + " ";
                        ret+=PreferenceLevel.prolog2abbv(lastPref)+" "+
                            dates+" "+(iStarts.size()==2?fStart==iStarts.first()?"Early":"Late":startTime)+(fStart==lStart?"":" - "+endTime);
                    }
                }
                lastPref = pref; fStart = start;
            }
            lStart = start;
        }
        if (lastPref!=null) {
            if (iLocation && PreferenceLevel.sNeutral.equals(lastPref)) {
                //
            } else if (!iLocation && PreferenceLevel.sProhibited.equals(lastPref)) {
                //
            } else {
                String startTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*fStart+Constants.FIRST_SLOT_TIME_MIN);
                String endTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*(lStart+iLength.get(lStart))+Constants.FIRST_SLOT_TIME_MIN);
                if (fStart==iStarts.first()) {
                    if (html) {
                        ret+="<span style='color:"+PreferenceLevel.prolog2color(lastPref)+"; white-space:nowrap;' "+
                            "title='"+PreferenceLevel.prolog2string(lastPref)+" "+
                            dates+" "+startTime+" - "+endTime+
                            "'>"+dates+"</span>";
                    } else {
                    	if (color)
                    		ret += "@@COLOR " + PreferenceLevel.prolog2color(lastPref) + " ";
                        ret+=PreferenceLevel.prolog2abbv(lastPref)+" "+dates;
                    }
                } else {
                    if (ret.length()>0) ret+=", ";
                    if (html) {
                        ret+="<span style='color:"+PreferenceLevel.prolog2color(lastPref)+"; white-space:nowrap;' "+
                            "title='"+PreferenceLevel.prolog2string(lastPref)+" "+
                            dates+" "+startTime+" - "+endTime+
                            "'>"+dates+" "+(iStarts.size()==2?fStart==iStarts.first()?"Early":"Late":startTime)+(fStart==lStart?"":" - "+endTime)+"</span>";
                    } else {
                    	if (color)
                    		ret += "@@COLOR " + PreferenceLevel.prolog2color(lastPref) + " ";
                        ret+=PreferenceLevel.prolog2abbv(lastPref)+" "+
                            dates+" "+(iStarts.size()==2?fStart==iStarts.first()?"Early":"Late":startTime)+(fStart==lStart?"":" - "+endTime);
                    }
                }
            }
        }
        return ret;
    }
    
    public String toString(boolean html) {
    	return toString(html, false);
    }
    
    public String toString(boolean html, boolean color) {
        if (iStarts.isEmpty()) return "";
        String ret = "";
        Hashtable<Integer,String> fPref = null; 
        int fDate = -1, lDate = -1;
        for (Integer date: iDates) {
        	Hashtable<Integer,String> pref = iPreferences.get(date);
        	if (fPref==null) {
        	    fPref = pref; fDate = date;
        	} else if (!fPref.equals(pref)) {
        	    String label = getLabel(fDate, lDate, fPref, html, color);
        	    if (label.length()>0) {
        	        if (ret.length()>0) ret+=", ";
        	        ret+=label;
        	    }
        	    fPref = pref; fDate = date;
        	}
        	lDate = date;
        }
        if (fPref!=null) {
            String label = getLabel(fDate, lDate, fPref, html, color);
            if (label.length()>0) {
                if (ret.length()>0) ret+=", ";
                ret+=label;
            }
        }
        return ret;
    }
}
