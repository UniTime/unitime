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

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ExamPeriodEditForm implements UniTimeForm {
	private static final long serialVersionUID = -8152697759737310033L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	protected static GwtConstants CONS = Localization.create(GwtConstants.class);
	
	private Long iUniqueId;
    private String iOp;
    private String iDate;
    private Integer iStart;
    private Integer iLength;
    private Integer iStartOffset;
    private Integer iStopOffset;
    private Integer iStart2;
    private Integer iLength2;
    private Integer iStartOffset2;
    private Integer iStopOffset2;
    private Integer iStart3;
    private Integer iLength3;
    private Integer iStartOffset3;
    private Integer iStopOffset3;
    private Integer iStart4;
    private Integer iLength4;
    private Integer iStartOffset4;
    private Integer iStopOffset4;
    private Integer iStart5;
    private Integer iLength5;
    private Integer iStartOffset5;
    private Integer iStopOffset5;
    private Long iType;
    private Long iPrefLevel;
    private boolean iAutoSetup;
    private Session iSession;
    private Boolean iEditable;
    
    public ExamPeriodEditForm() {
    	reset();
    }
    
    @Override
	public void validate(UniTimeAction action) {
	    if (!iAutoSetup) {
		    Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		    Date date = null;
		    try {
		    	date = df.parse(iDate);
		    } catch (Exception e) {}
		    if (date == null)
		    	action.addFieldError("form.date", MSG.errorExamDateIsNotValid());
	    }

        if (iType==null || iType < 0)
        	action.addFieldError("form.examType", MSG.errorExamTypeIsRequired());

        if (iStart==null || iStart<=0) {
            if (!iAutoSetup) action.addFieldError("form.start", MSG.errorStartTimeIsRequired());
        } else {
            int hour = iStart/100;
            int min = iStart%100;
            if (hour>=24)
                action.addFieldError("form.start", MSG.errorInvalidStartTimeHour(hour));
            if (min>=60)
                action.addFieldError("form.start", MSG.errorInvalidStartTimeMin(min));
            if ((min%Constants.SLOT_LENGTH_MIN)!=0)
                action.addFieldError("form.start", MSG.errorInvalidStartTimeMin5(min));

            if (iLength==null || iLength<=0)
                action.addFieldError("form.length", MSG.errorLengthIsRequired());
            else if ((iLength%Constants.SLOT_LENGTH_MIN)!=0)
                action.addFieldError("form.length", MSG.errorInvalidLength5(iLength));
            if (iStartOffset != null) {
            	if(iStartOffset.intValue() < 0){
                  	action.addFieldError("form.start offset", MSG.errorInvalidStartOffsetNegative(iStartOffset));    
            	}
            	if ((iStartOffset.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
                    action.addFieldError("form.start offset", MSG.errorInvalidStartOffset5(iStartOffset));            		
            	}
            }
            if (iStopOffset != null) {
            	if(iStopOffset.intValue() < 0){
                  	action.addFieldError("form.stop offset", MSG.errorInvalidStopOffsetNegative(iStopOffset));    
            	}
            	if ((iStopOffset.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
                    action.addFieldError("form.stop offset", MSG.errorInvalidStopOffset5(iStopOffset));            		
            	}
            }
       }
	    
	    if (iAutoSetup) {
	        if (iStart2!=null && iStart2>0) {
	            int hour = iStart2/100;
	            int min = iStart2%100;
	            if (hour>=24)
	                action.addFieldError("form.start2", MSG.errorInvalidStartTimeHour(hour));
	            if (min>=60)
	                action.addFieldError("form.start2", MSG.errorInvalidStartTimeMin(min));
	            if ((min%Constants.SLOT_LENGTH_MIN)!=0)
	                action.addFieldError("form.start2", MSG.errorInvalidStartTimeMin5(min));
	            if (iLength2==null || iLength2<=0)
	                action.addFieldError("form.length2", MSG.errorLengthIsRequired());
	            else if ((iLength2%Constants.SLOT_LENGTH_MIN)!=0)
	                action.addFieldError("form.length2", MSG.errorInvalidLength5(iLength2));
	            if (iStartOffset2 != null) {
	            	if(iStartOffset2.intValue() < 0){
	                  	action.addFieldError("form.startOffset2", MSG.errorInvalidStartOffsetNegative(iStartOffset2));    
	            	}
	            	if ((iStartOffset2.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
	                    action.addFieldError("form.startOffset2", MSG.errorInvalidStartOffset5(iStartOffset2));            		
	            	}
	            }
	            if (iStopOffset2 != null) {
	            	if(iStopOffset2.intValue() < 0){
	                  	action.addFieldError("form.stopOffset2", MSG.errorInvalidStopOffsetNegative(iStopOffset2));    
	            	}
	            	if ((iStopOffset2.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
	                    action.addFieldError("form.stopOffset2", MSG.errorInvalidStopOffset5(iStopOffset2));            		
	            	}
	            }
	        }
	        
            if (iStart3!=null && iStart3>0) {
                int hour = iStart3/100;
                int min = iStart3%100;
                if (hour>=24)
                    action.addFieldError("form.start3", MSG.errorInvalidStartTimeHour(hour));
                if (min>=60)
                    action.addFieldError("form.start3", MSG.errorInvalidStartTimeMin(min));
                if ((min%Constants.SLOT_LENGTH_MIN)!=0)
                    action.addFieldError("form.start3", MSG.errorInvalidStartTimeMin5(min));
                if (iLength3==null || iLength3<=0)
                    action.addFieldError("form.length3", MSG.errorLengthIsRequired());
                else if ((iLength3%Constants.SLOT_LENGTH_MIN)!=0)
                    action.addFieldError("form.length3", MSG.errorInvalidLength5(iLength3));
                if (iStartOffset3 != null) {
                	if(iStartOffset3.intValue() < 0){
                      	action.addFieldError("form.startOffset3", MSG.errorInvalidStartOffsetNegative(iStartOffset3));    
                	}
                	if ((iStartOffset3.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
                        action.addFieldError("form.startOffset3", MSG.errorInvalidStartOffset5(iStartOffset3));            		
                	}
                }
                if (iStopOffset3 != null) {
                	if(iStopOffset3.intValue() < 0){
                      	action.addFieldError("form.stopOffset3", MSG.errorInvalidStopOffsetNegative(iStopOffset3));    
                	}
                	if ((iStopOffset3.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
                        action.addFieldError("form.stopOffset3", MSG.errorInvalidStopOffset5(iStopOffset3));
                	}
                }
           }

            if (iStart4!=null && iStart4>0) {
                int hour = iStart4/100;
                int min = iStart4%100;
                if (hour>=24)
                    action.addFieldError("form.start4", MSG.errorInvalidStartTimeHour(hour));
                if (min>=60)
                    action.addFieldError("form.start4", MSG.errorInvalidStartTimeMin(min));
                if ((min%Constants.SLOT_LENGTH_MIN)!=0)
                    action.addFieldError("form.start4", MSG.errorInvalidStartTimeMin5(min));
                if (iLength4==null || iLength4<=0)
                    action.addFieldError("form.length4", MSG.errorLengthIsRequired());
                else if ((iLength4%Constants.SLOT_LENGTH_MIN)!=0)
                    action.addFieldError("form.length4", MSG.errorInvalidLength5(iLength4));
                if (iStartOffset4 != null) {
                	if(iStartOffset4.intValue() < 0){
                      	action.addFieldError("form.startOffset4", MSG.errorInvalidStartOffsetNegative(iStartOffset4));
                	}
                	if ((iStartOffset4.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
                        action.addFieldError("form.startOffset4", MSG.errorInvalidStartOffset5(iStartOffset4));
                	}
                }
                if (iStopOffset4 != null) {
                	if(iStopOffset4.intValue() < 0){
                      	action.addFieldError("form.stopOffset4", MSG.errorInvalidStopOffsetNegative(iStopOffset4));
                	}
                	if ((iStopOffset4.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
                        action.addFieldError("form.stopOffset4", MSG.errorInvalidStopOffset5(iStopOffset4));
                	}
                }
            }

            if (iStart5!=null && iStart5>0) {
                int hour = iStart5/100;
                int min = iStart5%100;
                if (hour>=24)
                    action.addFieldError("form.start5", MSG.errorInvalidStartTimeHour(hour));
                if (min>=60)
                    action.addFieldError("form.start5", MSG.errorInvalidStartTimeMin(min));
                if ((min%Constants.SLOT_LENGTH_MIN)!=0)
                    action.addFieldError("form.start5", MSG.errorInvalidStartTimeMin5(min));
                if (iLength5==null || iLength5<=0)
                    action.addFieldError("form.length5", MSG.errorLengthIsRequired());
                else if ((iLength5%Constants.SLOT_LENGTH_MIN)!=0)
                    action.addFieldError("form.length5", MSG.errorInvalidLength5(iLength5));
                if (iStartOffset5 != null) {
                	if(iStartOffset5.intValue() < 0){
                      	action.addFieldError("form.startOffset5", MSG.errorInvalidStartOffsetNegative(iStartOffset5));
                	}
                	if ((iStartOffset5.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
                        action.addFieldError("form.startOffset5", MSG.errorInvalidStartOffset5(iStartOffset5));
                	}
                }
                if (iStopOffset5 != null) {
                	if(iStopOffset5.intValue() < 0){
                      	action.addFieldError("form.stopOffset5", MSG.errorInvalidStopOffsetNegative(iStopOffset5));
                	}
                	if ((iStopOffset5.intValue()%Constants.SLOT_LENGTH_MIN)!=0){
                        action.addFieldError("form.stopOffset5", MSG.errorInvalidStopOffset5(iStopOffset5));
                	}
                }
            }
	    }
	    
	    try {
	        if (!action.hasFieldErrors()) {
	            Session session = SessionDAO.getInstance().get(action.getSessionContext().getUser().getCurrentAcademicSessionId());
	            Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
	            Date startDate = df.parse(iDate);
	            long diff = startDate.getTime()-session.getExamBeginDate().getTime();
	            int dateOffset = (int)Math.round(diff/(1000.0 * 60 * 60 * 24)); 
	            int hour = iStart / 100;
	            int min = iStart % 100;
	            int slot = (hour*60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	            ExamPeriod period = ExamPeriod.findByDateStart(session.getUniqueId(), dateOffset, slot, iType);
	            if (period!=null && !period.getUniqueId().equals(getUniqueId())) {
	                action.addFieldError("form.date", MSG.errorDuplicateExaminationPeriod());
	            }
	        }
	    } catch (Exception e) {}
	}
	
    @Override
	public void reset() {
		iOp = null; iUniqueId = Long.valueOf(-1); iDate = null; 
		iStart = null; iLength = null; iStartOffset = null; iStopOffset = null;
		iStart2 = null; iLength2 = null; iStartOffset2 = null; iStopOffset2 = null;
		iStart3 = null; iLength3 = null; iStartOffset3 = null; iStopOffset3 = null;
		iStart4 = null; iLength4 = null; iStartOffset4 = null; iStopOffset4 = null;
		iStart5 = null; iLength5 = null; iStartOffset5 = null; iStopOffset5 = null;
		iPrefLevel = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral).getUniqueId();
		iType = null;
		iAutoSetup = false;
		iEditable = false;
	}
	
	public void load(ExamPeriod ep, SessionContext context) throws Exception {
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		if (ep==null) {
			reset();
			Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
			iDate = df.format(session.getExamBeginDate());
			iLength = 120;
			iType = (Long)context.getAttribute(SessionAttribute.ExamType);
			TreeSet periods = ExamPeriod.findAll(context.getUser().getCurrentAcademicSessionId(), iType);
			if (!periods.isEmpty()) {
				iType = ((ExamPeriod)periods.last()).getExamType().getUniqueId();
			    Map<Integer, Integer> times = new HashMap<Integer, Integer>();
			    Set<Date> dates = new HashSet<Date>();
			    for (Iterator i=periods.iterator();i.hasNext();) {
			        ExamPeriod p = (ExamPeriod)i.next();
			        if (p.getExamType().getUniqueId().equals(iType)) {
			        	times.put(p.getStartSlot(), p.getLength());
			        	dates.add(p.getStartDate());
			        }
			    }
			    iLength = null;
			    for (Iterator i=new TreeSet<Integer>(times.keySet()).iterator();i.hasNext();) {
			        Integer start = (Integer)i.next();
			        if (iLength == null) {
			        	if (dates.size() > 1) {
				        	int time = Constants.SLOT_LENGTH_MIN*start+Constants.FIRST_SLOT_TIME_MIN;
			        		iStart = 100*(time/60)+(time%60);
				        	Calendar cal = Calendar.getInstance(); cal.setTime(((ExamPeriod)periods.last()).getStartDate()); cal.add(Calendar.DAY_OF_YEAR, 1);
						    iDate = df.format(cal.getTime());
			        	} else {
			        		iDate = df.format(((ExamPeriod)periods.last()).getStartDate());
			        	}
			        	iLength = Constants.SLOT_LENGTH_MIN*times.get(start);
			        }
			        if (start.equals(((ExamPeriod)periods.last()).getStartSlot()) && i.hasNext()) {
			        	int slot = (Integer)i.next();
			            int time = Constants.SLOT_LENGTH_MIN*slot+Constants.FIRST_SLOT_TIME_MIN;
			            iStart = 100*(time/60)+(time%60);
			            iLength = Constants.SLOT_LENGTH_MIN*times.get(slot);
					    iDate = df.format(((ExamPeriod)periods.last()).getStartDate());
			            break;
			        }
			    }
				iStartOffset = getDefaultStartOffset(iType);
				iStopOffset = getDefaultStopOffset(iType);
			}

			iPrefLevel = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral).getUniqueId();
			iOp = "Save";
			iEditable = true;
		} else {
		    iUniqueId = ep.getUniqueId();
			iDate = df.format(ep.getStartDate());
			iStart = ep.getStartHour()*100 + ep.getStartMinute();
			iLength = ep.getLength() * Constants.SLOT_LENGTH_MIN;
			iStartOffset = ep.getEventStartOffset() * Constants.SLOT_LENGTH_MIN;
			iStopOffset = ep.getEventStopOffset() * Constants.SLOT_LENGTH_MIN;
			iPrefLevel = ep.getPrefLevel().getUniqueId();
			iType = ep.getExamType().getUniqueId();
			context.setAttribute(SessionAttribute.ExamType, iType);
			iOp = "Update";
			iEditable = !ep.isUsed();
		}
	}
	
	public void update(ExamPeriod ep, SessionContext context, org.hibernate.Session hibSession) throws Exception {
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		ep.setStartDate(df.parse(iDate));
	    int hour = iStart / 100;
	    int min = iStart % 100;
	    int slot = (hour*60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	    ep.setStartSlot(slot);
	    ep.setLength(iLength / Constants.SLOT_LENGTH_MIN);
	    ep.setPrefLevel(new PreferenceLevelDAO().get(iPrefLevel));
	    ep.setExamType(ExamTypeDAO.getInstance().get(iType));
	    ep.setEventStartOffset(iStartOffset == null?Integer.valueOf(0):Integer.valueOf(iStartOffset.intValue()/Constants.SLOT_LENGTH_MIN));
	    ep.setEventStopOffset(iStopOffset == null?Integer.valueOf(0):Integer.valueOf(iStopOffset.intValue()/Constants.SLOT_LENGTH_MIN));
		hibSession.saveOrUpdate(ep);
	}
	
	public ExamPeriod create(SessionContext context, org.hibernate.Session hibSession) throws Exception {
	    ExamPeriod ep = new ExamPeriod();
        Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
        ep.setSession(session);
        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
        ep.setStartDate(df.parse(iDate));
        int hour = iStart / 100;
        int min = iStart % 100;
        int slot = (hour*60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
        ep.setStartSlot(slot);
        ep.setLength(iLength / Constants.SLOT_LENGTH_MIN);
        ep.setPrefLevel(new PreferenceLevelDAO().get(iPrefLevel));
        ep.setExamType(ExamTypeDAO.getInstance().get(iType));
        ep.setEventStartOffset(iStartOffset == null?Integer.valueOf(0):Integer.valueOf(iStartOffset.intValue() / Constants.SLOT_LENGTH_MIN));
        ep.setEventStopOffset(iStopOffset == null?Integer.valueOf(0):Integer.valueOf(iStopOffset.intValue() / Constants.SLOT_LENGTH_MIN));
        hibSession.saveOrUpdate(ep);
		setUniqueId(ep.getUniqueId());
		return ep;
	}
	
	public ExamPeriod saveOrUpdate(HttpServletRequest request, SessionContext context, org.hibernate.Session hibSession) throws Exception {
		if (getAutoSetup()) {
			setDays(request);
			TreeSet periods = ExamPeriod.findAll(context.getUser().getCurrentAcademicSessionId(), iType);
			TreeSet<Integer> slots = new TreeSet();
			TreeSet oldDays = new TreeSet();
			for (Iterator i=periods.iterator();i.hasNext();) {
				ExamPeriod period = (ExamPeriod)i.next();
				slots.add(period.getStartSlot());
				if (!iDays.contains(period.getDateOffset())) {
				    for (Iterator j=hibSession.createQuery(
                    "select x from Exam x where x.assignedPeriod.uniqueId=:periodId")
                    .setLong("periodId", period.getUniqueId())
                    .iterate();j.hasNext();) {
				        Exam exam = (Exam)j.next();
				        exam.unassign(context.getUser().getExternalUserId(), hibSession);
				    }
					hibSession.delete(period);
					i.remove();
				} else {
					oldDays.add(period.getDateOffset());
				}
			}
			Hashtable<Integer,Integer> length = new Hashtable();
			Hashtable<Integer,Integer> translate = new Hashtable();
			Hashtable<Integer,Integer> eventStartOffsets = new Hashtable();
			Hashtable<Integer,Integer> eventStopOffsets = new Hashtable();
			
			TreeSet<Integer> newStarts = new TreeSet();
			Iterator<Integer> it = slots.iterator();
            if (iStart!=null && iStart>0) {
                int slot = ((iStart/100)*60 + (iStart%100) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN; 
                length.put(slot, iLength);
                eventStartOffsets.put(slot, iStartOffset);
                eventStopOffsets.put(slot, iStopOffset);
                if (it.hasNext()) translate.put(it.next(), slot); else newStarts.add(slot);
            } else if (it.hasNext()) it.next();
            if (iStart2!=null && iStart2>0) {
                int slot = ((iStart2/100)*60 + (iStart2%100) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
                length.put(slot, iLength2);
                eventStartOffsets.put(slot, iStartOffset2);
                eventStopOffsets.put(slot, iStopOffset2);
                if (it.hasNext()) translate.put(it.next(), slot); else newStarts.add(slot);
            } else if (it.hasNext()) it.next();
            if (iStart3!=null && iStart3>0) {
                int slot = ((iStart3/100)*60 + (iStart3%100) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
                length.put(slot, iLength3);
                eventStartOffsets.put(slot, iStartOffset3);
                eventStopOffsets.put(slot, iStopOffset3);
               if (it.hasNext()) translate.put(it.next(), slot); else newStarts.add(slot);
            } else if (it.hasNext()) it.next();
            if (iStart4!=null && iStart4>0) {
                int slot = ((iStart4/100)*60 + (iStart4%100) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
                length.put(slot, iLength4);
                eventStartOffsets.put(slot, iStartOffset4);
                eventStopOffsets.put(slot, iStopOffset4);
                if (it.hasNext()) translate.put(it.next(), slot); else newStarts.add(slot);
            } else if (it.hasNext()) it.next();
            if (iStart5!=null && iStart5>0) {
                int slot = ((iStart5/100)*60 + (iStart5%100) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
                length.put(slot, iLength5);
                eventStartOffsets.put(slot, iStartOffset5);
                eventStopOffsets.put(slot, iStopOffset5);
                if (it.hasNext()) translate.put(it.next(), slot); else newStarts.add(slot);
            } else if (it.hasNext()) it.next();
			for (Iterator i=periods.iterator();i.hasNext();) {
				ExamPeriod period = (ExamPeriod)i.next();
				Integer start = translate.get(period.getStartSlot());
				if (start==null) {
				    for (Iterator j=hibSession.createQuery(
				            "select x from Exam x where x.assignedPeriod.uniqueId=:periodId")
				            .setLong("periodId", period.getUniqueId())
				            .iterate();j.hasNext();) {
				        Exam exam = (Exam)j.next();
				        exam.unassign(context.getUser().getExternalUserId(), hibSession);
				    }
				    hibSession.delete(period);
				    i.remove();
				} else {
				    period.setStartSlot(start);
				    period.setLength(length.get(start) / Constants.SLOT_LENGTH_MIN);
				    period.setEventStartOffset(eventStartOffsets.get(start) == null?Integer.valueOf(null):Integer.valueOf(eventStartOffsets.get(start)/Constants.SLOT_LENGTH_MIN));
				    period.setEventStopOffset(eventStopOffsets.get(start) == null?Integer.valueOf(null):Integer.valueOf(eventStopOffsets.get(start)/Constants.SLOT_LENGTH_MIN));
				    hibSession.update(period);
				}
			}
			for (Iterator i=iDays.iterator();i.hasNext();) {
				Integer day = (Integer)i.next();
				for (int start : new TreeSet<Integer>(length.keySet())) {
				    if (oldDays.contains(day) && !newStarts.contains(start)) continue;
				    ExamPeriod ep = new ExamPeriod();
				    ep.setSession(iSession);
				    ep.setDateOffset(day);
				    ep.setStartSlot(start);
				    ep.setLength(length.get(start) / Constants.SLOT_LENGTH_MIN);
				    ep.setEventStartOffset(eventStartOffsets.get(start) == null?Integer.valueOf(null):Integer.valueOf(eventStartOffsets.get(start)/Constants.SLOT_LENGTH_MIN));
				    ep.setEventStopOffset(eventStopOffsets.get(start) == null?Integer.valueOf(null):Integer.valueOf(eventStopOffsets.get(start)/Constants.SLOT_LENGTH_MIN));
				    ep.setExamType(ExamTypeDAO.getInstance().get(iType));
				    ep.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
				    hibSession.save(ep);
				}
			}
			return null;
		} else {
			ExamPeriod ep = null;
			if (getUniqueId().longValue()>=0)
				ep = (new ExamPeriodDAO()).get(getUniqueId());
			if (ep==null)
				ep = create(context, hibSession);
			else 
				update(ep, context, hibSession);
			return ep;
		}
	}
	
	public void delete(SessionContext context, org.hibernate.Session hibSession) throws Exception {
		if (getUniqueId().longValue()<0) return;
		ExamPeriod ep = (new ExamPeriodDAO()).get(getUniqueId(), hibSession);
		for (Iterator j=hibSession.createQuery(
		        "select x from Exam x where x.assignedPeriod.uniqueId=:periodId")
		        .setLong("periodId", ep.getUniqueId())
		        .iterate();j.hasNext();) {
		    Exam exam = (Exam)j.next();
            exam.unassign(context.getUser().getExternalUserId(), hibSession);
		}
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
    public Integer getStart2() { return iStart2; }
    public void setStart2(Integer start2) { iStart2 = start2; }
    public Integer getLength2() { return iLength2; }
    public void setLength2(Integer length2) { iLength2 = length2; }
    public Integer getStart3() { return iStart3; }
    public void setStart3(Integer start3) { iStart3 = start3; }
    public Integer getLength3() { return iLength3; }
    public void setLength3(Integer length3) { iLength3 = length3; }
    public Integer getStart4() { return iStart4; }
    public void setStart4(Integer start4) { iStart4 = start4; }
    public Integer getLength4() { return iLength4; }
    public void setLength4(Integer length4) { iLength4 = length4; }
    public Integer getStart5() { return iStart5; }
    public void setStart5(Integer start5) { iStart5 = start5; }
    public Integer getLength5() { return iLength5; }
    public void setLength5(Integer length5) { iLength5 = length5; }
    public Long getPrefLevel() { return iPrefLevel; }
    public void setPrefLevel(Long prefLevel) { iPrefLevel = prefLevel; }
	public Integer getStartOffset() {
		return iStartOffset;
	}

	public void setStartOffset(Integer startOffset) {
		iStartOffset = startOffset;
	}

	public Integer getStopOffset() {
		return iStopOffset;
	}

	public void setStopOffset(Integer stopOffset) {
		iStopOffset = stopOffset;
	}

	public Integer getStartOffset2() {
		return iStartOffset2;
	}

	public void setStartOffset2(Integer startOffset2) {
		iStartOffset2 = startOffset2;
	}

	public Integer getStopOffset2() {
		return iStopOffset2;
	}

	public void setStopOffset2(Integer stopOffset2) {
		iStopOffset2 = stopOffset2;
	}

	public Integer getStartOffset3() {
		return iStartOffset3;
	}

	public void setStartOffset3(Integer startOffset3) {
		iStartOffset3 = startOffset3;
	}

	public Integer getStopOffset3() {
		return iStopOffset3;
	}

	public void setStopOffset3(Integer stopOffset3) {
		iStopOffset3 = stopOffset3;
	}

	public Integer getStartOffset4() {
		return iStartOffset4;
	}

	public void setStartOffset4(Integer startOffset4) {
		iStartOffset4 = startOffset4;
	}

	public Integer getStopOffset4() {
		return iStopOffset4;
	}

	public void setStopOffset4(Integer stopOffset4) {
		iStopOffset4 = stopOffset4;
	}

	public Integer getStartOffset5() {
		return iStartOffset5;
	}

	public void setStartOffset5(Integer startOffset5) {
		iStartOffset5 = startOffset5;
	}

	public Integer getStopOffset5() {
		return iStopOffset5;
	}

	public void setStopOffset5(Integer stopOffset5) {
		iStopOffset5 = stopOffset5;
	}
    
    public Vector getPrefLevels() {
        Vector ret = new Vector();
        for (PreferenceLevel level: PreferenceLevel.getPreferenceLevelList()) {
            if (PreferenceLevel.sRequired.equals(level.getPrefProlog())) continue;
            ret.addElement(level);
        }
        return ret;
    }
    public Long getExamType() { return iType; }
    public void setExamType(Long type) { iType = type; }
    public boolean getAutoSetup() { return iAutoSetup; }
    public void setAutoSetup(boolean autoSetup) { iAutoSetup = autoSetup; }
    public boolean getEditable() { return iEditable; }
    public void setEditable(boolean editable) { iEditable = editable; }
    
	public String getBorder(int day, int month) {
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
	
	TreeSet<Integer> iDays = null;
	public boolean getCanAutoSetup(String examType) {
		return getCanAutoSetup(Long.valueOf(examType));
	}
	public boolean getCanAutoSetup(Long examType) {
		iDays = new TreeSet<Integer>(); 
		TreeSet<Integer> times = new TreeSet<Integer>(); 
		Hashtable<Integer, Integer> lengths = new Hashtable<Integer, Integer>(); 
		Hashtable<Integer, Integer> eventStartOffsets = new Hashtable<Integer, Integer>(); 
		Hashtable<Integer, Integer> eventStopOffsets = new Hashtable<Integer, Integer>(); 
		TreeSet periods = ExamPeriod.findAll(iSession.getUniqueId(),examType);
		iType = examType;
		for (Iterator i=periods.iterator();i.hasNext();) {
			ExamPeriod period = (ExamPeriod)i.next();
			if (period.isUsed()) return false;
			
			iDays.add(period.getDateOffset());
			times.add(period.getStartSlot());
			Integer length = lengths.get(period.getStartSlot());
			if (length==null)
				lengths.put(period.getStartSlot(),period.getLength());
			else if (!length.equals(period.getLength())) {
				return false;
			}
			Integer eventStartOffset = eventStartOffsets.get(period.getStartSlot());
			if (eventStartOffset == null){
				eventStartOffsets.put(period.getStartSlot(), period.getEventStartOffset());
			} else if (!eventStartOffset.equals(period.getEventStartOffset())){
				return(false);
			}
			Integer eventStopOffset = eventStopOffsets.get(period.getStartSlot());
			if (eventStopOffset == null){
				eventStopOffsets.put(period.getStartSlot(), period.getEventStopOffset());
			} else if (!eventStopOffset.equals(period.getEventStopOffset())){
				return(false);
			}
		}
		if (periods.size()!=iDays.size()*times.size() || times.size()>5) return false;
		if (times.isEmpty()) {
		    iStart = 1830; iLength = 60; iStartOffset = getDefaultStartOffset(examType); iStopOffset = getDefaultStopOffset(examType);
		    iStart2 = 2000; iLength2 = 120; iStartOffset2 = getDefaultStartOffset(examType); iStopOffset2 = getDefaultStopOffset(examType);
		    iStart3 = null; iLength3 = null; iStartOffset3 = getDefaultStartOffset(examType); iStopOffset3 = getDefaultStopOffset(examType);
		    iStart4 = null; iLength4 = null; iStartOffset4 = getDefaultStartOffset(examType); iStopOffset4 = getDefaultStopOffset(examType);
		    iStart5 = null; iLength5 = null; iStartOffset5 = getDefaultStartOffset(examType); iStopOffset5 = getDefaultStopOffset(examType);
		}
		Iterator<Integer> it = times.iterator();
        if (it.hasNext()) {
            int slot = it.next();
            int min = Constants.SLOT_LENGTH_MIN*times.first()+Constants.FIRST_SLOT_TIME_MIN;
            iStart = 100 * (min / 60) + (min % 60);
            iLength = Constants.SLOT_LENGTH_MIN * lengths.get(slot);
            iStartOffset = Constants.SLOT_LENGTH_MIN * eventStartOffsets.get(slot);
            iStopOffset = Constants.SLOT_LENGTH_MIN * eventStopOffsets.get(slot);
       } else {
            iStart = null; iLength = null; 
        }
        if (it.hasNext()) {
            int slot = it.next();
            int min = Constants.SLOT_LENGTH_MIN*slot+Constants.FIRST_SLOT_TIME_MIN;
            iStart2 = 100 * (min / 60) + (min % 60);
            iLength2 = Constants.SLOT_LENGTH_MIN * lengths.get(slot);
            iStartOffset2 = Constants.SLOT_LENGTH_MIN * eventStartOffsets.get(slot);
            iStopOffset2 = Constants.SLOT_LENGTH_MIN * eventStopOffsets.get(slot);
        } else {
            iStart2 = null; iLength2 = null;
        }
        if (it.hasNext()) {
            int slot = it.next();
            int min = Constants.SLOT_LENGTH_MIN*slot+Constants.FIRST_SLOT_TIME_MIN;
            iStart3 = 100 * (min / 60) + (min % 60);
            iLength3 = Constants.SLOT_LENGTH_MIN * lengths.get(slot);
            iStartOffset3 = Constants.SLOT_LENGTH_MIN * eventStartOffsets.get(slot);
            iStopOffset3 = Constants.SLOT_LENGTH_MIN * eventStopOffsets.get(slot);
        } else {
            iStart3 = null; iLength3 = null; 
        }
        if (it.hasNext()) {
            int slot = it.next();
            int min = Constants.SLOT_LENGTH_MIN*slot+Constants.FIRST_SLOT_TIME_MIN;
            iStart4 = 100 * (min / 60) + (min % 60);
            iLength4 = Constants.SLOT_LENGTH_MIN * lengths.get(slot);
            iStartOffset4 = Constants.SLOT_LENGTH_MIN * eventStartOffsets.get(slot);
            iStopOffset4 = Constants.SLOT_LENGTH_MIN * eventStopOffsets.get(slot);
        } else {
            iStart4 = null; iLength4 = null;
        }
        if (it.hasNext()) {
            int slot = it.next();
            int min = Constants.SLOT_LENGTH_MIN*slot+Constants.FIRST_SLOT_TIME_MIN;
            iStart5 = 100 * (min / 60) + (min % 60);
            iLength5 = Constants.SLOT_LENGTH_MIN * lengths.get(slot);
            iStartOffset5 = Constants.SLOT_LENGTH_MIN * eventStartOffsets.get(slot);
            iStopOffset5 = Constants.SLOT_LENGTH_MIN * eventStopOffsets.get(slot);
        } else {
            iStart5 = null; iLength5 = null;
        }
		return true;
	}
	
	public int getExamOffset() {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(iSession.getExamBeginDate());
		return(DateUtils.getDayOfYear(cal.get(Calendar.DAY_OF_MONTH), DateUtils.getStartMonth(iSession.getExamBeginDate(), iSession.getSessionStartYear(), 0), iSession.getSessionStartYear()) + 1);
	}
	
	public boolean hasExam(int day, int month) {
		return iDays.contains(1+iSession.getDayOfYear(day, month)-getExamOffset());
	}

	
	public String getPatternHtml() {
		try {
		int startMonth = iSession.getStartMonth();
		int endMonth = iSession.getEndMonth();
		StringBuffer border = new StringBuffer("[");
		StringBuffer pattern = new StringBuffer("[");
		for (int m=startMonth;m<=endMonth;m++) {
			if (m!=startMonth) { border.append(","); pattern.append(","); }
			border.append("["); pattern.append("[");
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, iSession.getSessionStartYear());
			for (int d=1;d<=daysOfMonth;d++) {
				if (d>1) { border.append(","); pattern.append(","); }
				border.append(getBorder(d,m));
				pattern.append(hasExam(d,m)?"'1'":"'0'");
			}
			border.append("]");
			pattern.append("]");
		}
		border.append("]");
		pattern.append("]");
		StringBuffer sb = new StringBuffer(); 
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
			"calGenerate("+iSession.getSessionStartYear()+",\n\t"+
				iSession.getStartMonth()+",\n\t"+
				iSession.getEndMonth()+",\n\t"+
				pattern+",\n\t"+
				"['1','0'],\n\t"+
				"['" + MSG.legentExaminationsOffered() +"','" + MSG.legentExaminationsNotOffered() + "'],\n\t"+
				"['rgb(240,240,50)','rgb(240,240,240)'],\n\t"+
				"'1',\n\t"+
				border+","+true+","+true+");\n");
		sb.append("</script>\n");
		return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setDays(HttpServletRequest request) {
		int startMonth = iSession.getStartMonth();
		int endMonth = iSession.getEndMonth();
		iDays = new TreeSet<Integer>();
		for (int m=startMonth;m<=endMonth;m++) {
		    int yr = DateUtils.calculateActualYear(m, iSession.getSessionStartYear());
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, iSession.getSessionStartYear());
			for (int d=1;d<=daysOfMonth;d++) {
				String exam = request.getParameter("cal_val_"+yr+"_"+((12+m)%12)+"_"+d);
				if ("1".equals(exam)) {
					iDays.add(1+iSession.getDayOfYear(d, m)-getExamOffset());
				}
			}
		}
	}

	public Integer getDefaultStartOffset(Long type) {
		return Constants.getDefaultExamStartOffset(ExamTypeDAO.getInstance().get(type));
	}

	public Integer getDefaultStopOffset(Long type) {
		return Constants.getDefaultExamStopOffset(ExamTypeDAO.getInstance().get(type));
	}
	
	public void setSession(Session session) { iSession = session; }
}
