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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.base.BaseDatePattern;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "date_pattern")
public class DatePattern extends BaseDatePattern implements Comparable<DatePattern> {
	private static final long serialVersionUID = 1L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static GwtConstants CONS = Localization.create(GwtConstants.class);
	
	public static enum DatePatternType {
		Standard,
		Alternate,
		NonStandard,
		Extended,
		PatternSet,
		;
		
	@Transient
		public String getLabel() {
			switch (this) {
			case Standard: return MSG.datePatternTypeStandard();
			case Alternate: return MSG.datePatternTypeAlternateWeeks();
			case NonStandard: return MSG.datePatternTypeNonStandard();
			case Extended: return MSG.datePatternTypeExtended();
			case PatternSet: return MSG.datePatternTypeAltPatternSet();
			default: return name();
			}
		}
	}

	@Deprecated
    public static final int sTypeStandard = DatePatternType.Standard.ordinal();
	@Deprecated
    public static final int sTypeAlternate = DatePatternType.Alternate.ordinal();
	@Deprecated
    public static final int sTypeNonStandard = DatePatternType.NonStandard.ordinal();
	@Deprecated
    public static final int sTypeExtended = DatePatternType.Extended.ordinal();
	@Deprecated
    public static final int sTypePatternSet = DatePatternType.PatternSet.ordinal();
    
    public static String DATE_PATTERN_LIST_ATTR = "datePatternList";
    public static String DATE_PATTERN_PARENT_LIST_ATTR = "datePatternParentsList";
    public static String DATE_PATTERN_CHILDREN_LIST_ATTR = "datePatternChildrenList";

/*[CONSTRUCTOR MARKER BEGIN]*/
	public DatePattern () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DatePattern (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	@Transient
	public int getPatternOffset() {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSession().getSessionBeginDateTime());
		int beginDate = cal.get(Calendar.DAY_OF_YEAR);
		return beginDate-(getOffset()==null?0:getOffset().intValue())-1;
	}

	private transient BitSet iCachedPatternBitSet = null;
	
	public void setPattern(String pattern) {
		super.setPattern(pattern); iCachedPatternBitSet = null;
	}
	
	public void setOffset(Integer offset) {
		super.setOffset(offset); iCachedPatternBitSet = null;
	}
	
	public void setPatternOffset(Date firstDate) {
	    Calendar cal = Calendar.getInstance(Locale.US);
	    cal.setTime(getSession().getSessionBeginDateTime());
	    int offset = 0;
	    while (cal.getTime().compareTo(firstDate)<0) {
	        offset--; cal.add(Calendar.DAY_OF_YEAR, 1);
	    }
	    while (cal.getTime().compareTo(firstDate)>0) {
	        offset++; cal.add(Calendar.DAY_OF_YEAR, -1);
	    }
	    setOffset(offset);
	}

	@Transient
	public BitSet getPatternBitSet() {
		if (iCachedPatternBitSet!=null) return iCachedPatternBitSet;
		if (getPattern()==null || getOffset()==null) return null;
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int size = getSession().getDayOfYear(0,endMonth+1)-getSession().getDayOfYear(1,startMonth);
		iCachedPatternBitSet = new BitSet(size);
		int offset = getPatternOffset() - getSession().getDayOfYear(1,startMonth);
		for (int i=0;i<getPattern().length();i++) {
			if (getPattern().charAt(i)=='1' && i+offset >= 0)
				iCachedPatternBitSet.set(i+offset);
		}
		return iCachedPatternBitSet;
	}
	
	public void setPatternBitSet(BitSet pattern) {
		String p = null; int offset = 0;
		for (int i = 0; i < pattern.length(); i++) {
			if (pattern.get(i)) {
				if (p == null) p = "";
				p += "1";
			} else {
				if (p == null) offset ++;
				else p += "0";
			}
		}
		setOffset(DateUtils.getDayOfYear(getSession().getSessionBeginDateTime()) - getSession().getDayOfYear(1, getSession().getPatternStartMonth()) - offset - 1);
		setPattern(p);
	}
	
	public boolean isOffered(int day, int month) {
		if (getPattern()==null || getOffset()==null) return false;
		int idx = getSession().getDayOfYear(day, month)-getPatternOffset();
		if (idx<0 || idx>=getPattern().length()) return false;
		return (getPattern().charAt(idx)=='1');
	}
	
	public boolean isUsed(int day, int month, Set<Integer> usage) {
		if (usage==null || getPattern()==null || getOffset()==null) return false;
		return usage.contains(getSession().getDayOfYear(day, month));
	}
	
	@Transient
	public String getPatternText() {
		StringBuffer sb = new StringBuffer();
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int year = getSession().getSessionStartYear();
		for (int m=startMonth;m<=endMonth;m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			for (int d=1;d<=daysOfMonth;d++) {
				sb.append(isOffered(d,m)?"1":"0");
			}
		}
		return sb.toString();
	}

	@Transient
	public String getPatternArray() {
		StringBuffer sb = new StringBuffer("[");
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int year = getSession().getSessionStartYear();
		for (int m=startMonth;m<=endMonth;m++) {
			if (m!=startMonth) sb.append(",");
			sb.append("[");
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			for (int d=1;d<=daysOfMonth;d++) {
				if (d>1) sb.append(",");
				sb.append(isOffered(d,m)?"'1'":"'0'");
			}
			sb.append("]");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Transient
	public String getPatternString() {
		
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		HashMap<Date, Date> dates = getPatternDateStringHashMaps();
		TreeSet<Date> ts = new TreeSet<Date>();
		ts.addAll(dates.keySet());
		for(Date startDate: ts){
			Date endDate = (Date) dates.get(startDate);
			Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_SHORT);
			String startDateStr = df.format(startDate);
			String endDateStr = df.format(endDate);
			if (first){
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(startDateStr);
			if (!startDateStr.equals(endDateStr)){
				sb.append("-" + endDateStr);
			}
		}
		return sb.toString();
	}
	
	@Transient
	public HashMap<Date, Date> getPatternDateStringHashMaps() {
		Calendar startDate = Calendar.getInstance(Locale.US);
		startDate.setTime(getStartDate());
		Calendar endDate = Calendar.getInstance(Locale.US);
		endDate.setTime(getEndDate());

		int startMonth = startDate.get(Calendar.MONTH);
		int endMonth = endDate.get(Calendar.MONTH);
		int startYear = startDate.get(Calendar.YEAR);
		int endYear = endDate.get(Calendar.YEAR);
		if (endYear > startYear){
			endMonth += (12 * (endYear - startYear));
		}
		
		HashMap<Date, Date> mapStartToEndDate = new HashMap<Date, Date>();
		Date first = null, previous = null;
		char[] ptrn = getPattern().toCharArray();
		int charPosition = 0;
		int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK);
		Calendar cal = Calendar.getInstance(Locale.US);

		for (int m=startMonth;m<=endMonth;m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, startYear);
			int d;
			if (m == startMonth){
				d = startDate.get(Calendar.DAY_OF_MONTH);
			} else {
				d = 1;
			}
			for (;d<=daysOfMonth && charPosition < ptrn.length ;d++) {
				if (ptrn[charPosition] == '1' || (first != null && dayOfWeek == Calendar.SUNDAY && charPosition + 1 < ptrn.length && ptrn[1 + charPosition] == '1')) {
					if (first==null){
						//first = ((m<0?12+m:m%12)+1)+"/"+d+"/"+((m>=12)?startYear+1:startYear);
						cal.setTime(getStartDate());
						cal.add(Calendar.DAY_OF_YEAR, charPosition);
						first = cal.getTime();
					}
				} else {
					if (first!=null) {
						mapStartToEndDate.put(first, previous);
						first=null;
					}
				}
				//previous = ((m<0?12+m:m%12)+1)+"/"+d+"/"+((m>=12)?startYear+1:startYear);
				cal.setTime(getStartDate());
				cal.add(Calendar.DAY_OF_YEAR, charPosition);
				previous = cal.getTime();
				
				charPosition++;
				dayOfWeek++;
				if (dayOfWeek > Calendar.SATURDAY){
					dayOfWeek = Calendar.SUNDAY;
				}
			}
		}
		if (first!=null) {
			mapStartToEndDate.put(first, previous);
			first=null;
		}
		return(mapStartToEndDate);
	}

	public TreeSet<Integer> getUsage(Collection<Class_> classes) {
		TreeSet<Integer> days = new TreeSet<Integer>();
		
		int dowOffset = Constants.getDayOfWeek(DateUtils.getDate(1, 0, getSession().getSessionStartYear()));
		int offset = getPatternOffset();
		for (Class_ clazz : classes) {
			
			for (@SuppressWarnings("unchecked")
			Iterator<TimePref> k=clazz.effectivePreferences(TimePref.class).iterator();k.hasNext();) {
				TimePref tp = (TimePref)k.next();
				if (tp.getTimePattern().isExactTime()) {
					//System.out.println("    -- exact time "+tp.getTimePatternModel().getExactDays());
					int dayCode = tp.getTimePatternModel().getExactDays();
					
					for (int x=0;x<getPattern().length();x++) {
						if (getPattern().charAt(x)!='1') continue;
						int dayOfWeek = (x+offset+dowOffset) % 7;
						if ((dayCode&Constants.DAY_CODES[dayOfWeek < 0 ? dayOfWeek + 7 : dayOfWeek])!=0)
							days.add(x+offset);
					}
				} else {
					//System.out.println("    -- time pattern "+tp.getTimePattern().getName());
					TimePatternModel m = tp.getTimePatternModel();
					boolean req = false;
					for (int d=0;d<m.getNrDays();d++) {
						boolean used = false;
						for (int t=0;t<m.getNrTimes();t++) {
							if (PreferenceLevel.sRequired.equals(m.getPreference(d,t))) {
								used=true; break;
							}
						}
						if (!used) continue;
						req = true;
						int dayCode = m.getDayCode(d);
						//System.out.println("      -- required "+dayCode);
    					for (int x=0;x<getPattern().length();x++) {
    						if (getPattern().charAt(x)!='1') continue;
    						int dayOfWeek = (x+offset+dowOffset) % 7;
    						if ((dayCode&Constants.DAY_CODES[dayOfWeek < 0 ? dayOfWeek + 7 : dayOfWeek])!=0)
    							days.add(x+offset);
    					}
					}
					if (!req) {
    					for (int d=0;d<m.getNrDays();d++) {
    						boolean used = false;
    						for (int t=0;t<m.getNrTimes();t++) {
    							if (!PreferenceLevel.sProhibited.equals(m.getPreference(d,t))) {
    								used=true; break;
    							}
    						}
    						if (!used) continue;
    						req = true;
    						int dayCode = m.getDayCode(d);
    						//System.out.println("      -- not prohibited "+dayCode);
        					for (int x=0;x<getPattern().length();x++) {
        						if (getPattern().charAt(x)!='1') continue;
        						int dayOfWeek = (x+offset+dowOffset) % 7;
        						if ((dayCode&Constants.DAY_CODES[dayOfWeek < 0 ? dayOfWeek + 7 : dayOfWeek])!=0)
        							days.add(x+offset);
        					}
    					}
					}
				}
			}
		}

		return days;
	}
	
	@SuppressWarnings("unchecked")
	public TreeSet<Integer> getUsage(Long uniqueId) {
		if (uniqueId==null) return null;
		@SuppressWarnings("unchecked")
		HashSet<Class_> classes = new HashSet<Class_>(
				DatePatternDAO.getInstance().getSession().
				createQuery("select distinct c from Class_ as c inner join c.datePattern as dp where dp.uniqueId=:uniqueId", Class_.class).
				setParameter("uniqueId", uniqueId.intValue()).setCacheable(true).list());
		for (SchedulingSubpart s :
			DatePatternDAO.getInstance().getSession().
			createQuery("select distinct s from SchedulingSubpart as s inner join s.datePattern as dp where dp.uniqueId=:uniqueId", SchedulingSubpart.class).
			setParameter("uniqueId", uniqueId.intValue()).setCacheable(true).list()) {
			for (Class_ c : s.getClasses()) {
				if (c.getDatePattern()==null)
					classes.add(c);
			}
		}
		return getUsage(classes);
	}
	
	public String getBorderArray(Long uniqueId) {
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int year = getSession().getSessionStartYear();
		Set<Integer> usage = (uniqueId!=null?getUsage(uniqueId):null);
		StringBuffer sb = new StringBuffer("[");
		for (int m=startMonth;m<=endMonth;m++) {
			if (m!=startMonth) sb.append(",");
			sb.append("[");
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			for (int d=1;d<=daysOfMonth;d++) {
				if (d>1) sb.append(",");
				String border = getSession().getBorder(d,m);
				if (isUsed(d,m,usage)) 
					border = "'green 2px solid'";
				sb.append(border);
			}
			sb.append("]");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Transient
	public String getPatternHtml() {
		return getPatternHtml(true, null);
	}
	
	public String getPatternHtml(boolean editable) {
		return getPatternHtml(editable, null);
	}
    
    public String getPatternHtml(boolean editable, Long uniqueId) {
        return getPatternHtml(editable, uniqueId, true);
    }
    
	public String getPatternHtml(boolean editable, Long uniqueId, boolean includeScript) {
		StringBuffer sb = new StringBuffer(); 
        if (includeScript)
            sb.append("<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>\n");
		sb.append("<script language='JavaScript'>\n");
		sb.append("var CAL_WEEKDAYS = [");
		for (int i = 0; i < 7; i++) {
			if (i > 0) sb.append(", ");
			sb.append("\"" + CONS.days()[(i + 6) % 7] + "\"");
		}
		sb.append("];\n");
		sb.append("var CAL_MONTHS = [");
		for (int i = 0; i < 12; i++) {
			if (i > 0) sb.append(", ");
			sb.append("\"" + Month.of(1 + i).getDisplayName(TextStyle.FULL_STANDALONE, Localization.getJavaLocale()) + "\"");
		}
		sb.append("];\n");
		sb.append(
			"calGenerate2("+getSession().getSessionStartYear()+",\n\t"+
				(getSession().getPatternStartMonth()) +",\n\t"+
				(getSession().getPatternEndMonth())+",\n\t"+
				getPatternArray()+",\n\t"+
				"['1','0'],\n\t" +
				"['" + MSG.legendClassesOffered() + "','" + MSG.legendClassesNotOffered() + "'],\n\t" +
				"['rgb(240,240,50)','rgb(240,240,240)'],\n\t" +
				"'1',\n\t" +
				getBorderArray(uniqueId)+","+getSession().getColorArray()+","+editable+","+editable+");\n");
		sb.append("</script>\n");
		return sb.toString();
	}
	
	public void setPatternAndOffset(HttpServletRequest request) {
		int startMonth = getSession().getPatternStartMonth();
		int endMonth = getSession().getPatternEndMonth();
		int firstOne = 0, lastOne = 0;
		int year = getSession().getSessionStartYear();
		StringBuffer sb = null;
		int idx = getSession().getDayOfYear(1,startMonth);
		for (int m=startMonth;m<=endMonth;m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			int yr = DateUtils.calculateActualYear(m, year);
			for (int d=1;d<=daysOfMonth;d++) {
				String offered = request.getParameter("cal_val_"+yr+"_"+((12+m)%12)+"_"+d);
				if (offered!=null) {
					if (sb!=null || !offered.equals("0")) {
						if (sb==null) {
							firstOne = idx; sb = new StringBuffer();
						}
						sb.append(offered);
					}
					if (!offered.equals("0")) lastOne=idx;
				}
				idx++;
			}
		}
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSession().getSessionBeginDateTime());
		if (sb!=null) {
			setPattern(sb.substring(0,lastOne-firstOne+1));
			setOffset(cal.get(Calendar.DAY_OF_YEAR)-firstOne-1);
		} else {
			setPattern("0"); setOffset(0);
		}
	}

	public static DatePattern findByName(Session session, String name) {
		return findByName(session.getUniqueId(), name);
	}

    public static DatePattern findByName(Long sessionId, String name) {
    	@SuppressWarnings("unchecked")
		List<DatePattern> list = (DatePatternDAO.getInstance()).getSession().
    		createQuery("select distinct p from DatePattern as p where p.session.uniqueId=:sessionId and p.name=:name", DatePattern.class).
    		setParameter("sessionId", sessionId).
    		setParameter("name", name).setCacheable(true).list();
    	if (list==null || list.isEmpty()) return null;
    	return (DatePattern)list.get(0);
	}
    
    public static List<DatePattern> findAll(UserContext user, Department department, DatePattern includeGiven) throws Exception {
    	boolean includeExtended = user.getCurrentAuthority().hasRight(Right.ExtendedDatePatterns);
    	return findAll(user.getCurrentAcademicSessionId(), includeExtended, department, includeGiven);
    }
    
    public static List<DatePattern> findAll(Session session, boolean includeExtended, Department department, DatePattern includeGiven) {
    	return findAll(session.getUniqueId(), includeExtended, department, includeGiven);
    }
    
    public static List<DatePattern> findAll(Long sessionId, boolean includeExtended, Department department, DatePattern includeGiven) {
    	@SuppressWarnings("unchecked")
		List<DatePattern> list = DatePatternDAO.getInstance().getSession().createQuery(
    			"select distinct p from DatePattern as p where p.session.uniqueId=:sessionId" + (!includeExtended ? " and p.type!="+DatePatternType.Extended.ordinal() : ""), DatePattern.class)
    			.setParameter("sessionId", sessionId)
    			.setCacheable(true).list();
    	
    	if (!includeExtended) {
    		for (Iterator<DatePattern> i = list.iterator(); i.hasNext(); ) {
    			DatePattern p = i.next();
    			if (p.getType() == DatePatternType.PatternSet.ordinal() && !p.getDepartments().isEmpty() && (department == null || !p.getDepartments().contains(department)))
    				i.remove();
    		}
    	}

    	if (!includeExtended && department != null)
    		for (DatePattern dp: department.getDatePatterns()) {
    			if (dp.getType() == DatePatternType.Extended.ordinal())
    				list.add(dp);
    		}
    	
    	if (includeGiven != null && !list.contains(includeGiven))
    		list.add(includeGiven);
    	
    	Collections.sort(list);
    	
    	return list;
	}

    @SuppressWarnings("unchecked")
	public static Set<DatePattern> findAllUsed(Long sessionId) {
		TreeSet<DatePattern> ret = new TreeSet<DatePattern>(
    			DatePatternDAO.getInstance().getSession().
        		createQuery("select distinct dp from Class_ as c inner join c.datePattern as dp where dp.session.uniqueId=:sessionId", DatePattern.class).
        		setParameter("sessionId", sessionId.longValue()).
        		setCacheable(true).list());
    	ret.addAll(DatePatternDAO.getInstance().getSession().
        		createQuery("select distinct dp from SchedulingSubpart as s inner join s.datePattern as dp where dp.session.uniqueId=:sessionId", DatePattern.class).
        		setParameter("sessionId", sessionId.longValue()).
        		setCacheable(true).list());
    	ret.addAll(DatePatternDAO.getInstance().getSession().
        		createQuery("select distinct dp from Assignment a inner join a.datePattern dp where dp.session.uniqueId=:sessionId", DatePattern.class).
        		setParameter("sessionId", sessionId.longValue()).
        		setCacheable(true).list());
    	Session session = SessionDAO.getInstance().get(sessionId);
    	if (session.getDefaultDatePattern()!=null) ret.add(session.getDefaultDatePattern());
    	return ret;
    }
    
    public List<DatePattern> findChildren() {
    	return findChildren(null);
    }
    
    @SuppressWarnings("unchecked")
	public List<DatePattern> findChildren(org.hibernate.Session hibSession) {
    	if (getType() != null && getType() != DatePatternType.PatternSet.ordinal()) return new ArrayList<DatePattern>();
    	List<DatePattern> ret = new ArrayList<DatePattern>(getChildren());
    	Collections.sort(ret);
    	return ret;
    }
    
    @SuppressWarnings("unchecked")
	public static List<DatePattern> findAllParents(Long sessionId) {    	
    	return (List<DatePattern>)DatePatternDAO.getInstance().getSession().
        		createQuery("from DatePattern where type = :parentType and session.uniqueId=:sessionId order by name", DatePattern.class).
        		setParameter("parentType", DatePatternType.PatternSet.ordinal()).setParameter("sessionId", sessionId).setCacheable(true).list();
    }
    
	public static List<DatePattern> findAllChildren(Long sessionId) {    	
    	return (List<DatePattern>)DatePatternDAO.getInstance().getSession().
        		createQuery("from DatePattern where type != :parentType and session.uniqueId=:sessionId order by name", DatePattern.class).
        		setParameter("parentType", DatePatternType.PatternSet.ordinal()).setParameter("sessionId", sessionId).setCacheable(true).list();
    }

	@Transient
    public boolean isUsed() {
    	return findAllUsed(getSession().getUniqueId()).contains(this);
    }
    
	@Transient
    public boolean isDefault() {
    	return this.equals(getSession().getDefaultDatePattern());
    }

    public int size() {
		if (getPattern()==null) return 0;
		int size = 0;
		for (int i=0;i<getPattern().length();i++)
			if ('1'==getPattern().charAt(i)) size++;
		return size;
	}
	
	public int compareTo(DatePattern o) {
    	if (o==null || !(o instanceof DatePattern)) return -1;
    	DatePattern dp = (DatePattern)o;
    	int cmp = getType().compareTo(dp.getType());
    	if (cmp!=0) return cmp;
    	if (dp.getType().intValue() == DatePatternType.PatternSet.ordinal()) {
    		// compare just by name
    	} else if (dp.getType().intValue()==DatePatternType.Standard.ordinal()) {
    		cmp = Float.compare(dp.getEffectiveNumberOfWeeks(), getEffectiveNumberOfWeeks());
    		if (cmp != 0) return cmp;
    		cmp = dp.getOffset().compareTo(getOffset());
        	if (cmp != 0) return cmp;
    	} else {
    		cmp = dp.getOffset().compareTo(getOffset());
        	if (cmp != 0) return cmp;
        	cmp = Float.compare(getEffectiveNumberOfWeeks(), dp.getEffectiveNumberOfWeeks());
    		if (cmp != 0) return cmp;
    	}
    	cmp = getName().compareTo(dp.getName());
    	if (cmp != 0) return cmp;
    	return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(dp.getUniqueId() == null ? -1 : dp.getUniqueId());
    }
	
	public Object clone() {
		DatePattern dp = new DatePattern();
		dp.setSession(getSession());
		dp.setOffset(getOffset());
		dp.setPattern(getPattern());
		dp.setName(getName());
		dp.setType(getType());
		dp.setVisible(isVisible());
		dp.setNumberOfWeeks(dp.getNumberOfWeeks());
		return dp;
	}
	
	@Transient
	public Date getStartDate() {
		if (getPattern()==null || getOffset()==null) return null;
		int idx = getPattern().indexOf('1');
		if (idx<0) return getSession().getSessionBeginDateTime();
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSession().getSessionBeginDateTime());
		cal.add(Calendar.DAY_OF_YEAR, idx - getOffset().intValue());
		return cal.getTime();
	}

	@Transient
	public Date getEndDate() {
		if (getPattern()==null || getOffset()==null) return null;
		int idx = getPattern().lastIndexOf('1');
		if (idx<0) return getSession().getSessionEndDateTime();
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(getSession().getSessionBeginDateTime());
		cal.add(Calendar.DAY_OF_YEAR, idx - getOffset().intValue());
		return cal.getTime();
	}
    
    public String toString() {
        return getName();
    }
    
    public DatePattern findCloseMatchDatePatternInSession(Session session){
    	List<DatePattern> allDatePatterns = DatePattern.findAll(session, true, null, null);
 		TreeSet<Integer> days = new TreeSet<Integer>();

 		
		if (days.isEmpty()) {
			int offset = getPatternOffset();
			for (int x=0;x<getPattern().length();x++) {
                if (getPattern().charAt(x)!='1') continue;
					days.add(x+offset);
			}
		}

		DatePattern likeDp = null;
		int likeDiff = 0;
		
		for (DatePattern xdp : allDatePatterns) {
    		if (xdp.getName().startsWith("generated")) continue;
    		if (getOffset().equals(xdp.getOffset()) && getPattern().equals(xdp.getPattern())) {
    			likeDp = xdp; likeDiff = 0;
    			break;
    		}
    		TreeSet<Integer> xdays = new TreeSet<Integer>();
			if (xdays.isEmpty()) {
				int offset = xdp.getPatternOffset();
				for (int x=0;x<xdp.getPattern().length();x++) {
					if (xdp.getPattern().charAt(x)!='1') continue;
					xdays.add(x+offset);
				}
			}

			int diff = diff(days, xdays);
    		if (likeDp==null || likeDiff>diff || (likeDiff==diff && xdp.isDefault())) {
    			likeDp = xdp; likeDiff = diff;
    		}
		}
		
		if (likeDp!=null) {
            if (likeDiff<=5) {
                return(likeDp);
            }
		}
		return(null);
     }
    
	private int diff(Set<Integer> x, Set<Integer> y) {
		int diff = 0;
		for (Integer o : x) {
			if (!y.contains(o)) diff++;
		}
		for (Integer o : y) {
			if (!x.contains(o)) diff++;
		}
		return diff;
	}
	
	public static Date[] getBounds(Long sessionId) {
        Date startDate = null, endDate = null;
        for (DatePattern dp : DatePattern.findAllUsed(sessionId)) {
            if (startDate == null || startDate.compareTo(dp.getStartDate())>0)
                startDate = dp.getStartDate();
            if (endDate == null || endDate.compareTo(dp.getEndDate())<0)
                endDate = dp.getEndDate();
        }
        if (startDate == null) {
        	Session session = SessionDAO.getInstance().get(sessionId);
        	startDate = DateUtils.getDate(1, session.getStartMonth(), session.getSessionStartYear());
        	endDate = DateUtils.getDate(0, session.getEndMonth() + 1, session.getSessionStartYear());
        }
        Calendar startDateCal = Calendar.getInstance(Locale.US);
        startDateCal.setTime(startDate);
        startDateCal.set(Calendar.HOUR_OF_DAY, 0);
        startDateCal.set(Calendar.MINUTE, 0);
        startDateCal.set(Calendar.SECOND, 0);
        Calendar endDateCal = Calendar.getInstance(Locale.US);
        endDateCal.setTime(endDate);
        endDateCal.set(Calendar.HOUR_OF_DAY, 23);
        endDateCal.set(Calendar.MINUTE, 59);
        endDateCal.set(Calendar.SECOND, 59);
        return new Date[] { startDateCal.getTime(), endDateCal.getTime()};
	}
	
	@Transient
	public float getComputedNumberOfWeeks() {
		if (getType() != null && getType() == DatePatternType.PatternSet.ordinal()) {
			for (DatePattern dp: findChildren())
				return dp.getEffectiveNumberOfWeeks();
		}
		int daysInWeek[] = new int[7];
		for (int i = 0; i < 7; i++) daysInWeek[i] = 0;
		for (int i = 0; i < getPattern().length(); i++) {
			if (getPattern().charAt(i) == '1')
				daysInWeek[i % 7]++;
		}
		Arrays.sort(daysInWeek);
		return daysInWeek[2];
	}
	
	@Transient
	public float getEffectiveNumberOfWeeks() {
		return (getNumberOfWeeks() == null ? getComputedNumberOfWeeks() : getNumberOfWeeks());
	}
	
	private boolean respectsSessionHolidays(boolean alsoReturnFalseIfBreaksExistOnNonHolidays) {
		int indexOfFirstDayOfSessionInSessionString = Integer.parseInt((new SimpleDateFormat("dd")).format(this.getSession().getSessionBeginDateTime())) - 1;
		int datePatternIndex = 0;
	
		if ((indexOfFirstDayOfSessionInSessionString - this.getOffset()) < 0) {
			int lengthToCheck;
			if (-1 * (indexOfFirstDayOfSessionInSessionString - this.getOffset()) > this.getPattern().length() - 1) {
				lengthToCheck = this.getPattern().length();
			} else {
				lengthToCheck = -1 * (indexOfFirstDayOfSessionInSessionString - this.getOffset());
			}
			if (alsoReturnFalseIfBreaksExistOnNonHolidays && this.getPattern().substring(0, lengthToCheck).contains("0")) {
				return false;
			}
			datePatternIndex = lengthToCheck;
		}

		if (datePatternIndex >= this.getPattern().length()) {
			return true;
		}
		
		String sessionHolidays = getSession().getHolidays();
		int sessionPatternIndex = 0;
		if (datePatternIndex > 0) {
			sessionPatternIndex = 0;
		} else {
			sessionPatternIndex = indexOfFirstDayOfSessionInSessionString - this.getOffset();
		}
		while (sessionPatternIndex < sessionHolidays.length() && datePatternIndex < this.getPattern().length()) {
			if (Integer.parseInt(sessionHolidays.substring(sessionPatternIndex, sessionPatternIndex + 1)) == Session.sHolidayTypeBreak
					|| Integer.parseInt(sessionHolidays.substring(sessionPatternIndex, sessionPatternIndex + 1)) == Session.sHolidayTypeHoliday) {
				if (Integer.parseInt(this.getPattern().substring(datePatternIndex, datePatternIndex + 1)) != 0) {
					return false;
				} 
			} else if (alsoReturnFalseIfBreaksExistOnNonHolidays && Integer.parseInt(this.getPattern().substring(datePatternIndex, datePatternIndex + 1)) == 0) {
				return false;
			}
			datePatternIndex++;
			sessionPatternIndex++;
		} 
		if (alsoReturnFalseIfBreaksExistOnNonHolidays && datePatternIndex < (this.getPattern().length() - 1)) {
			if (this.getPattern().substring(datePatternIndex, this.getPattern().length()).contains("0")) {
				return false;
			} 
		}
		return true;
	}
	
	public boolean respectsSessionHolidays() {
		return(respectsSessionHolidays(false));
	}
	
	public boolean respectsSessionHolidaysAndHasNoNonHolidayBreaks() {
		return(respectsSessionHolidays(true));
	}
	
	@Transient
	public DatePatternType getDatePatternType() {
		if (getType() == null) return null;
		return DatePatternType.values()[getType()];
	}
	
	public void setDatePatternType(DatePatternType type) {
		if (type == null)
			setType(null);
		else
			setType(type.ordinal());
	}
	
	@Transient
	public boolean isAlternate() {
		return getDatePatternType() == DatePatternType.Alternate;
	}
	@Transient
	public boolean isExtended() {
		return getDatePatternType() == DatePatternType.Extended;
	}
	@Transient
	public boolean isPatternSet() {
		return getDatePatternType() == DatePatternType.PatternSet;
	}
	
    public int getFirstMeeting(int daysOfWeek, int dayOfWeekOffset) {
        BitSet pattern = getPatternBitSet();
    	if (daysOfWeek != 0) {
            int idx = -1;
            while ((idx = pattern.nextSetBit(1 + idx)) >= 0) {
                int dow = (idx + dayOfWeekOffset) % 7;
                if ((daysOfWeek & Constants.DAY_CODES[dow]) != 0) break;
            }
            return idx;
    	} else {
    		return pattern.nextSetBit(0);
    	}
    }
    
    public int getLastMeeting(int daysOfWeek, int dayOfWeekOffset) {
        BitSet pattern = getPatternBitSet();
    	if (daysOfWeek != 0) {
    		int idx = -1;
    		int lastMeeting = 0;
            while ((idx = pattern.nextSetBit(1 + idx)) >= 0) {
                int dow = (idx + dayOfWeekOffset) % 7;
                if ((daysOfWeek & Constants.DAY_CODES[dow]) != 0)
                	lastMeeting = idx;
            }
            return lastMeeting;
    	} else {
    		return pattern.length() - 1;
    	}
    }	
}
