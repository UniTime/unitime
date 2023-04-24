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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimePattern.TimePatternType;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.RequiredTimeTable;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class TimePatternEditForm implements UniTimeForm {
	private static final long serialVersionUID = 2957933039987971879L;
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String iOp;
    private Long iUniqueId;
    private String iType;
    private String iNrMtgs;
    private String iMinPerMtg;
    private String iSlotsPerMtg;
    private String iDayCodes;
    private String iStartTimes;
    private boolean iEditable;
    private boolean iVisible;
    private String iName;
    private List<Long> iDepartmentIds = new ArrayList<Long>();
    private Long iDepartmentId;
    private String iBreakTime;
    private Long iPreviousId, iNextId;
    
    public TimePatternEditForm() {
    	reset();
    }

    @Override
	public void validate(UniTimeAction action) {
		if (iName==null || iName.trim().isEmpty())
			action.addFieldError("form.name", MSG.errorRequiredField(MSG.columnTimePatternName()));
		else {
			try {
				TimePattern pat = TimePattern.findByName(action.getSessionContext().getUser().getCurrentAcademicSessionId(),iName);
				if (pat!=null && !pat.getUniqueId().equals(iUniqueId))
					action.addFieldError("form.name", MSG.errorAlreadyExists(iName));
			} catch (Exception e) {
				action.addFieldError("form.name", e.getMessage());
			}
        }
        
		try {
			if (Integer.parseInt(getNrMtgs())<=0 && getTypeInt()!=TimePatternType.ExactTime.ordinal())
				action.addFieldError("form.nrMtgs", MSG.errorNumberOfMeetingsPerWeekRequired());
		} catch (NumberFormatException e) {
			action.addFieldError("form.nrMtgs", MSG.errorNumberOfMeetingsPerWeekRequired());
		}
		try {
			if (Integer.parseInt(getMinPerMtg())<=0 && getTypeInt()!=TimePatternType.ExactTime.ordinal())
				action.addFieldError("form.minPerMtg", MSG.errorMinutesPerMeetingRequired());
		} catch (NumberFormatException e) {
			action.addFieldError("form.minPerMtg", MSG.errorMinutesPerMeetingRequired());
		}
		try {
			if (Integer.parseInt(getSlotsPerMtg())<=0 && getTypeInt()!=TimePatternType.ExactTime.ordinal())
				action.addFieldError("form.slotsPerMtg", MSG.errorNumberOfSlotsPerMeetingRequired());
		} catch (NumberFormatException e) {
			action.addFieldError("form.slotsPerMtg", MSG.errorNumberOfSlotsPerMeetingRequired());
		}
		if (getTypeInt()<0)
			action.addFieldError("form.type", MSG.errorRequiredField(MSG.columnTimePatternType()));
		if (getBreakTime() == null || getBreakTime().isEmpty()) {
			setBreakTime("0");
		} else {
			try {
				Integer.parseInt(getBreakTime());
			} catch (NumberFormatException e) {
				action.addFieldError("form.breakTime", MSG.errorNotNumber(MSG.columnTimePatternBreakTime()));
			}
		}

		try {
			str2dayCodes(iDayCodes,"\n, ");
		} catch (Exception e) {
			action.addFieldError("form.dayCodes", e.getMessage());
		}

		try {
			str2startSlots(iStartTimes,"\n, ");
		} catch (Exception e) {
			action.addFieldError("form.startTimes", e.getMessage());
		}
        
		if (getTypeInt()!=TimePatternType.Extended.ordinal() && getTypeInt()!=TimePatternType.ExactTime.ordinal() && !iDepartmentIds.isEmpty())
			action.addFieldError("form.type", MSG.errorOnlyExtTimePatternHasDepartments());
	}

	@Override
	public void reset() {
		iOp = "List"; iUniqueId = Long.valueOf(-1); iType = TimePatternType.Standard.name(); iNrMtgs = ""; iMinPerMtg = ""; iSlotsPerMtg = "";
		iDayCodes = ""; iStartTimes = ""; iEditable = false; iVisible = false; iName = ""; iBreakTime = "";
		iDepartmentId = null; iDepartmentIds.clear();
		iPreviousId = null; iNextId = null;
	}
	
	public void load(TimePattern tp, Long sessionId) {
		if (tp==null) {
			reset();
			iOp = MSG.actionSaveTimePattern();
			iVisible = true; iEditable = true;
		} else {
			setName(tp.getName());
			setVisible(tp.isVisible().booleanValue());
			setEditable(tp.isEditable());
			setTypeInt(tp.getType().intValue());
			setMinPerMtg(tp.getMinPerMtg().toString());
			setNrMtgs(tp.getNrMeetings().toString());
			setBreakTime(tp.getBreakTime().toString());
			setSlotsPerMtg(tp.getSlotsPerMtg().toString());
			setUniqueId(tp.getUniqueId());
			setDayCodes(dayCodes2str(tp.getDays(),(getEditable()?"\n":", ")));
			setStartTimes(startSlots2str(tp.getTimes(),(getEditable()?"\n":", ")));
			iDepartmentIds.clear();
			for (Iterator i=tp.getDepartments(sessionId).iterator();i.hasNext();) {
				Department d = (Department)i.next();
				iDepartmentIds.add(d.getUniqueId());
			}
			iOp = MSG.actionUpdateTimePattern();
		}
	}
	
	public void update(TimePattern tp, org.hibernate.Session hibSession) throws Exception {
		tp.setName(getName());
		tp.setVisible(Boolean.valueOf(getVisible()));
		tp.setType(Integer.valueOf(getTypeInt()));
		tp.setBreakTime(Integer.valueOf(getBreakTime()));
		if (getEditable()) {
			int oldDays = tp.getDays().size();
			int oldTimes = tp.getTimes().size();
			tp.setMinPerMtg(Integer.valueOf(getMinPerMtg()));
			tp.setNrMeetings(Integer.valueOf(getNrMtgs()));
			tp.setSlotsPerMtg(Integer.valueOf(getSlotsPerMtg()));
			for (Iterator i=tp.getTimes().iterator();i.hasNext();) {
				TimePatternTime t = (TimePatternTime)i.next();
				hibSession.delete(t);
			}
			for (Iterator i=tp.getDays().iterator();i.hasNext();) {
				TimePatternDays d = (TimePatternDays)i.next();
				hibSession.delete(d);
			}
			tp.setTimes(str2startSlots(getStartTimes(),"\n, "));
			tp.setDays(str2dayCodes(getDayCodes(),"\n, "));
			for (Iterator i=tp.getTimes().iterator();i.hasNext();) {
				TimePatternTime t = (TimePatternTime)i.next();
				hibSession.save(t);
			}
			for (Iterator i=tp.getDays().iterator();i.hasNext();) {
				TimePatternDays d = (TimePatternDays)i.next();
				hibSession.save(d);
			}
			if (tp.getSession() != null & tp.getSession().getStatusType().isAllowRollForward()){
				if (oldDays != tp.getDays().size() || oldTimes != tp.getTimes().size()) {
					for (TimePref tpref: hibSession.createQuery(
							"from TimePref tp where tp.timePattern.uniqueId = :tpid", TimePref.class)
							.setParameter("tpid", tp.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
						tpref.setPreference(null);
						hibSession.update(tpref);
					}
				}
			}
		}
		HashSet oldDepts = new HashSet(tp.getDepartments());
		for (Long departmentId: iDepartmentIds) {
			Department d = (DepartmentDAO.getInstance()).get(departmentId,hibSession);
			if (d==null) continue;
			if (oldDepts.remove(d)) {
				//not changed -> do nothing
			} else {
				tp.getDepartments().add(d);
				d.getTimePatterns().add(tp);
				hibSession.saveOrUpdate(d);
			}
		}
		for (Iterator i=oldDepts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			tp.getDepartments().remove(d);
			d.getTimePatterns().remove(tp);
			hibSession.saveOrUpdate(d);
		}
		hibSession.saveOrUpdate(tp);
	}
	
	public TimePattern create(SessionContext context, org.hibernate.Session hibSession) throws Exception {
		TimePattern tp = new TimePattern();
		tp.setName(getName());
		tp.setVisible(Boolean.valueOf(getVisible()));
		tp.setType(Integer.valueOf(getTypeInt()));
		tp.setMinPerMtg(Integer.valueOf(getMinPerMtg()));
		tp.setNrMeetings(Integer.valueOf(getNrMtgs()));
		tp.setBreakTime(Integer.valueOf(getBreakTime()));
		tp.setSlotsPerMtg(Integer.valueOf(getSlotsPerMtg()));
		tp.setTimes(str2startSlots(getStartTimes(),"\n, "));
		tp.setDays(str2dayCodes(getDayCodes(),"\n, "));
		tp.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		for (Iterator i=tp.getTimes().iterator();i.hasNext();) {
			TimePatternTime t = (TimePatternTime)i.next();
			hibSession.save(t);
		}
		for (Iterator i=tp.getDays().iterator();i.hasNext();) {
			TimePatternDays d = (TimePatternDays)i.next();
			hibSession.save(d);
		}
		HashSet newDepts = new HashSet();
		for (Long departmentId: iDepartmentIds) {
			Department d = (DepartmentDAO.getInstance()).get(departmentId,hibSession);
			if (d==null) continue;
			newDepts.add(d);
		}
		tp.setDepartments(newDepts);
		hibSession.save(tp);
		for (Iterator i=newDepts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			d.getTimePatterns().add(tp);
			hibSession.saveOrUpdate(d);
		}
		setUniqueId(tp.getUniqueId());
		return tp;
	}
	
	public TimePattern saveOrUpdate(SessionContext context, org.hibernate.Session hibSession) throws Exception {
		TimePattern tp = null;
		if (getUniqueId().intValue()>=0)
			tp = (TimePatternDAO.getInstance()).get(getUniqueId());
		if (tp==null) {
			tp = create(context, hibSession);
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    tp, 
                    ChangeLog.Source.TIME_PATTERN_EDIT, 
                    ChangeLog.Operation.CREATE, 
                    null, 
                    null);
        } else { 
			update(tp, hibSession);
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    tp, 
                    ChangeLog.Source.TIME_PATTERN_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
        }
		return tp;
	}
	
	public void delete(SessionContext context, org.hibernate.Session hibSession) throws Exception {
		if (getUniqueId().intValue()<0)
			return;
		if (!getEditable())
			return;
		TimePattern tp = (TimePatternDAO.getInstance()).get(getUniqueId(), hibSession);
		for (Iterator i=tp.getDepartments().iterator();i.hasNext();) {
			Department d = (Department)i.next();
			d.getTimePatterns().remove(tp);
			hibSession.saveOrUpdate(d);
		}
        ChangeLog.addChange(
                hibSession, 
                context, 
                tp, 
                ChangeLog.Source.TIME_PATTERN_EDIT, 
                ChangeLog.Operation.DELETE, 
                null, 
                null);
		hibSession.delete(tp);
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
	public String getType() { return iType; }
	public void setType(String type) { iType = type; }
	public List<ComboBoxLookup> getTypes() {
		List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
		for (TimePatternType t: TimePatternType.values())
			ret.add(new ComboBoxLookup(t.getLabel(), t.name()));
		return ret;
	}
	public int getTypeInt() {
		for (TimePatternType t: TimePatternType.values())
			if (t.name().equals(iType)) return t.ordinal();
		return -1;
	}
	public void setTypeInt(int type) { iType = (type<0?"":TimePatternType.values()[type].name()); }
	public String getNrMtgs() { return iNrMtgs; }
	public void setNrMtgs(String nrMtgs) { iNrMtgs = nrMtgs; }
	public String getBreakTime() { return iBreakTime; }
	public void setBreakTime(String breakTime) { iBreakTime = breakTime; }
	public String getMinPerMtg() { return iMinPerMtg; }
	public void setMinPerMtg(String minPerMtg) { iMinPerMtg = minPerMtg; }
	public String getSlotsPerMtg() { return iSlotsPerMtg; }
	public void setSlotsPerMtg(String slotsPerMtg) { iSlotsPerMtg = slotsPerMtg; }
	public String getDayCodes() { return iDayCodes; }
	public void setDayCodes(String dayCodes) { iDayCodes = dayCodes; }
	public String getStartTimes() { return iStartTimes; }
	public void setStartTimes(String startTimes) { iStartTimes = startTimes; }
	public boolean getEditable() { return iEditable; }
	public void setEditable(boolean editable) { iEditable = editable; }
	public boolean getVisible() { return iVisible; }
	public void setVisible(boolean visible) { iVisible = visible; }
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public List<Long> getDepartmentIds() { return iDepartmentIds; }
	public void setDepartmentIds(List<Long> departmentIds) { iDepartmentIds = departmentIds; }
	public Long getDepartmentIds(int idx) { return iDepartmentIds.get(idx); }
	public void setDepartmentIds(int idx, Long value) { iDepartmentIds.set(idx, value); }
	public Long getDepartmentId() { return iDepartmentId; }
	public void setDepartmentId(Long deptId) { iDepartmentId = deptId; }
	
	public Long getNextId() { return iNextId; }
	public void setNextId(Long nextId) { iNextId = nextId; }
	public boolean getHasNext() { return iNextId != null && iNextId >= 0; }
	public Long getPreviousId() { return iPreviousId; }
	public void setPreviousId(Long previousId) { iPreviousId = previousId; }
	public boolean getHasPrevious() { return iPreviousId != null && iPreviousId >= 0; }

	public static String dayCodes2str(Collection dayCodes, String delim) {
		StringBuffer sb = new StringBuffer();
		for (Iterator i=(new TreeSet(dayCodes)).iterator();i.hasNext();) {
			int dayCode = ((TimePatternDays)i.next()).getDayCode().intValue();
			int nrDays = 0;
			for (int j=0;j<Constants.NR_DAYS;j++)
				if ((dayCode&Constants.DAY_CODES[j])!=0) nrDays++;
			for (int j=0;j<Constants.NR_DAYS;j++) {
				if ((Constants.DAY_CODES[j]&dayCode)==0) continue;
				sb.append(nrDays==1?CONSTANTS.days()[j]:CONSTANTS.shortDays()[j]);
			}
			if (i.hasNext())
				sb.append(delim);
		}
		return sb.toString();
	}
	public static String startSlots2str(Collection startSlots, String delim) {
		StringBuffer sb = new StringBuffer();
		for (Iterator i=(new TreeSet(startSlots)).iterator();i.hasNext();) {
			int startSlot = ((TimePatternTime)i.next()).getStartSlot().intValue();
			int min = startSlot*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
			int time = 100*(min/60) + (min%60);
			sb.append(time);
			if (i.hasNext())
				sb.append(delim);
		}
		return sb.toString();
	}
	
	public static int getDayCode(String token, int day, int dayCode) {
		if (day==Constants.NR_DAYS) {
			if (token.length()==0) return dayCode;
			else return -1;
		}
		if (token.startsWith(CONSTANTS.shortDays()[day])) {
			int code = getDayCode(token.substring(CONSTANTS.shortDays()[day].length()),day+1,dayCode + Constants.DAY_CODES[day]);
			if (code>=0) return code;
		}
		if (token.startsWith(CONSTANTS.days()[day])) {
			int code = getDayCode(token.substring(CONSTANTS.days()[day].length()),day+1,dayCode + Constants.DAY_CODES[day]);
			if (code>=0) return code;
		}
		return getDayCode(token, day+1, dayCode);
	}
	
	public Set str2dayCodes(String dayCodes, String delim) throws Exception {
		Set ret = new TreeSet();
		StringTokenizer stk = new StringTokenizer(dayCodes, (delim==null?" ,\t\n\r\f":delim));
		while (stk.hasMoreTokens()) {
			String token = stk.nextToken();
			if (token.trim().length()==0) continue;
			int dayCode = getDayCode(token.trim(), 0, 0);
			if (dayCode<0)
				throw new Exception(MSG.errorInvalidDaysForToken(token));
			int nrDays = 0;
			for (int i=0;i<Constants.NR_DAYS;i++)
				if ((dayCode&Constants.DAY_CODES[i])!=0) nrDays++;
			try {
				if (nrDays!=Integer.parseInt(getNrMtgs()))
					throw new Exception(MSG.errorWrongNumberOfDaysForToken(token));
			} catch (NumberFormatException e) {}
			TimePatternDays days = new TimePatternDays();
			days.setDayCode(Integer.valueOf(dayCode));
			if (ret.contains(days))
				throw new Exception(MSG.errorDuplicateDaysToken(token));
			ret.add(days);
		}
		return ret;
	}

	public Set str2startSlots(String startTimes, String delim) throws Exception {
		Set ret = new TreeSet();
		StringTokenizer stk = new StringTokenizer(startTimes, (delim==null?" ,\t\n\r\f":delim));
		while (stk.hasMoreTokens()) {
			String token = stk.nextToken();
			if (token.trim().length()==0) continue;
			int slot = -1;
			try {
				int time = Integer.parseInt(token.trim());
				int hour = time/100;
				int min = time%100;
				if (hour>=24)
					throw new Exception(MSG.errorWrongHoursForTimeToken(token, hour));
				if (min>=60)
					throw new Exception(MSG.errorWrongMinutesForTimeToken(token, min));
				if ((min%Constants.SLOT_LENGTH_MIN)!=0)
					throw new Exception(MSG.errorMinutesNotDivisibleByFiveForTimeToken(token, min));
				try {
					int endTime = hour * 60 + min + (Constants.SLOT_LENGTH_MIN * Integer.parseInt(getSlotsPerMtg()));
					if (endTime/Constants.SLOT_LENGTH_MIN-Constants.FIRST_SLOT_TIME_MIN > Constants.SLOTS_PER_DAY)
						throw new Exception(MSG.errorTimeGoesOverMidnightForToken(token));
				} catch (NumberFormatException e) {}
				slot = (hour*60+min - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
			} catch (NumberFormatException ex) {
				throw new Exception(MSG.errorTimeNotNumberForToken(token));
			}
			if (slot<0)
				throw new Exception(MSG.errorNotValidTimeForToken(token));
			TimePatternTime time = new TimePatternTime();
			time.setStartSlot(Integer.valueOf(slot));
			if (ret.contains(time))
				throw new Exception(MSG.errorDiplicateTimeToken(token));
			ret.add(time);
		}
		return ret;
	}
	
	public String getExample() {
		try {
			TimePattern tp = new TimePattern(Long.valueOf(Long.MAX_VALUE));
			tp.setName(getName());
			tp.setVisible(Boolean.valueOf(getVisible()));
			tp.setType(Integer.valueOf(getTypeInt()));
			tp.setMinPerMtg(Integer.valueOf(getMinPerMtg()));
			tp.setNrMeetings(Integer.valueOf(getNrMtgs()));
			tp.setBreakTime(Integer.valueOf(getBreakTime()));
			tp.setSlotsPerMtg(Integer.valueOf(getSlotsPerMtg()));
			tp.setTimes(str2startSlots(getStartTimes(),"\n, "));
			tp.setDays(str2dayCodes(getDayCodes(),"\n, "));
			if (tp.getTimes().isEmpty() || tp.getDays().isEmpty()) return null;
			RequiredTimeTable rtt = tp.getRequiredTimeTable(true);
			return rtt.print(false,false,false,false);
		} catch (Exception e) {
			return null;
		}
	}
}

