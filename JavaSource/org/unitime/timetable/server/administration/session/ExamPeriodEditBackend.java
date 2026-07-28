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
package org.unitime.timetable.server.administration.session;

import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.admin.ExamPeriodsPage.ExamPeriodEditRequest;
import org.unitime.timetable.gwt.client.admin.ExamPeriodsPage.ExamPeriodEditResponse;
import org.unitime.timetable.gwt.client.admin.ExamPeriodsPage.ExamPeriodInterface;
import org.unitime.timetable.gwt.client.admin.ExamPeriodsPage.ExamPeriodSetupInterface;
import org.unitime.timetable.gwt.client.admin.ExamPeriodsPage.PeriodSetupItemInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.instructor.PatternDatesBackend;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

@GwtRpcImplements(ExamPeriodEditRequest.class)
public class ExamPeriodEditBackend implements GwtRpcImplementation<ExamPeriodEditRequest, ExamPeriodEditResponse> {
	protected final static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	protected final static CourseMessages COURSE = Localization.create(CourseMessages.class);
	protected final static GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public ExamPeriodEditResponse execute(ExamPeriodEditRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationPeriods);
		switch (request.getOperation()) {
		case LOAD_SETUP:
			ExamPeriodEditResponse setupResponse = new ExamPeriodEditResponse();
			ExamType examType = ExamTypeDAO.getInstance().get(request.getExamTypeId());
			if (examType == null)
				throw new GwtRpcException(MSG.messageNoExamType());
			context.setAttribute(SessionAttribute.ExamType, request.getExamTypeId());
			setupResponse.setSetup(getSetup(context, examType));
			setupLookups(context, setupResponse, null);
			return setupResponse;
		case SAVE_SETUP:
			context.checkPermission(request.getSetup().getSessionId(), Right.ExaminationPeriods);
			saveSetup(context, request.getSetup());
			return null;
		case ADD:
			ExamPeriodEditResponse addResponse = new ExamPeriodEditResponse();
			addResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			addResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			addResponse.setPeriod(getPeriod(context, null));
			setupLookups(context, addResponse, null);
			return addResponse;
		case EDIT:
			ExamPeriodEditResponse response = new ExamPeriodEditResponse();
			response.setSessionId(context.getUser().getCurrentAcademicSessionId());
			response.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			
			ExamPeriod period = ExamPeriodDAO.getInstance().get(request.getPeriodId());
			if (period == null)
				throw new GwtRpcException(COURSE.errorDoesNotExists(MSG.colPeriod()));
			context.setAttribute(SessionAttribute.ExamType, period.getExamType().getUniqueId());

			response.setCanDelete(!period.isUsed());
			response.setPeriod(getPeriod(context, period));
			
			setupLookups(context, response, period);
			return response;
		case DELETE:
			deletePeriod(context, request.getPeriodId());
			return null;
		case SAVE:
			ExamPeriod other = ExamPeriod.findByDateStart(context.getUser().getCurrentAcademicSessionId(),
					getDateOffset(context, request.getPeriod().getDate()),
					request.getPeriod().getStartSlot(), request.getPeriod().getExamTypeId());
			if (other != null && !other.getUniqueId().equals(request.getPeriodId()))
				throw new GwtRpcException(MSG.errorDuplicateExaminationPeriod());
			context.setAttribute(SessionAttribute.ExamType, request.getPeriod().getExamTypeId());
			
			ExamPeriodEditResponse saveResponse = new ExamPeriodEditResponse();
			saveResponse.setSessionId(context.getUser().getCurrentAcademicSessionId());
			saveResponse.setSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			saveResponse.setPeriod(new ExamPeriodInterface());
			saveResponse.getPeriod().setPeriodId(savePeriod(context, request.getPeriod()));
			
			return saveResponse;
		}
		return null;
	}
	
	protected ExamPeriodInterface getPeriod(SessionContext context, ExamPeriod period) {
		ExamPeriodInterface p = new ExamPeriodInterface();
		if (period == null) {
			p.setPreferenceId(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral).getUniqueId());
			Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
			p.setDate(session.getExamBeginDate());
			p.setLength(120);
			Long type = (Long)context.getAttribute(SessionAttribute.ExamType);
			p.setExamTypeId(type);
			TreeSet<ExamPeriod> periods = ExamPeriod.findAll(context.getUser().getCurrentAcademicSessionId(), type);
			if (!periods.isEmpty()) {
				p.setExamTypeId(periods.last().getExamType().getUniqueId());
				p.setStartOffset(getDefaultStartOffset(periods.last().getExamType()));
				p.setStopOffset(getDefaultStopOffset(periods.last().getExamType()));
			    Map<Integer, Integer> times = new HashMap<Integer, Integer>();
			    Set<Date> dates = new HashSet<Date>();
			    for (ExamPeriod ep: periods) {
			        if (ep.getExamType().getUniqueId().equals(p.getExamTypeId())) {
			        	times.put(ep.getStartSlot(), ep.getLength());
			        	dates.add(ep.getStartDate());
			        }
			    }
			    p.setLength(null);
			    for (Iterator<Integer> i = new TreeSet<Integer>(times.keySet()).iterator(); i.hasNext(); ) {
			    	Integer start = i.next();
			        if (p.getLength() == null) {
			        	if (dates.size() > 1) {
			        		p.setStartSlot(start);
				        	Calendar cal = Calendar.getInstance(); cal.setTime(periods.last().getStartDate()); cal.add(Calendar.DAY_OF_YEAR, 1);
				        	p.setDate(cal.getTime());
			        	} else {
			        		p.setDate(periods.last().getStartDate());
			        	}
			        	p.setLength(Constants.SLOT_LENGTH_MIN*times.get(start));
			        }
			        if (start.equals(periods.last().getStartSlot()) && i.hasNext()) {
			        	int slot = (Integer)i.next();
			            p.setStartSlot(slot);
			            p.setLength(Constants.SLOT_LENGTH_MIN*times.get(slot));
			            p.setDate(periods.last().getStartDate());
			            break;
			        }
			    }
			}
		} else {
			p.setPeriodId(period.getUniqueId());
			p.setCanEdit(!period.isUsed());
			p.setDate(period.getStartDate());
			p.setStartSlot(period.getStartSlot());
			p.setExamTypeId(period.getExamType().getUniqueId());
			p.setLength(Constants.SLOT_LENGTH_MIN * period.getLength());
			if (period.getEventStartOffset() != null)
				p.setStartOffset(Constants.SLOT_LENGTH_MIN * period.getEventStartOffset());
			if (period.getEventStopOffset() != null)
				p.setStopOffset(Constants.SLOT_LENGTH_MIN * period.getEventStopOffset());
			p.setPreferenceId(period.getPrefLevel() == null ? PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral).getUniqueId() : period.getPrefLevel().getUniqueId());
		}
		return p;
	}
	
	protected void setupLookups(SessionContext context, ExamPeriodEditResponse response, ExamPeriod period) {
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false))
			response.addPreference(pref.getUniqueId(), pref.getPrefName());
		Set<ExamType> types = null;
        if (!context.hasPermission(Right.StatusIndependent) && context.getUser().getCurrentAuthority().hasRight(Right.ExaminationSolver)) {
            types = new TreeSet<ExamType>();
            for (ExamType t: ExamType.findAll()) {
            	ExamStatus status = ExamStatus.findStatus(context.getUser().getCurrentAcademicSessionId(), t.getUniqueId());
            	if (status != null && !status.getManagers().isEmpty()) {
            		for (TimetableManager m: status.getManagers()) {
            			if (context.getUser().getCurrentAuthority().hasQualifier(m)) {
            				types.add(t);
            				break;
            			}
            		}
            	} else {
            		types.add(t);
            	}
            }
        }
        for (ExamType type: (types != null ? types : ExamType.findAll()))
			response.addExamType(type.getUniqueId(), type.getLabel());
		if (period != null && response.getExamType(period.getExamType().getUniqueId()) == null)
			response.addExamType(period.getExamType().getUniqueId(), period.getExamType().getLabel());
	}
	
	protected Integer getDefaultStartOffset(ExamType examType) {
		int offset = Constants.getDefaultExamStartOffset(examType);
		return offset <= 0 ? null : Integer.valueOf(offset);
	}

	protected Integer getDefaultStopOffset(ExamType examType) {
		int offset = Constants.getDefaultExamStopOffset(examType);
		return offset <= 0 ? null : Integer.valueOf(offset);
	}
	
	protected ExamPeriodSetupInterface getSetup(SessionContext context, ExamType examType) {
		Set<Integer> days = new TreeSet<Integer>(); 
		TreeSet<Integer> times = new TreeSet<Integer>(); 
		Hashtable<Integer, Integer> lengths = new Hashtable<Integer, Integer>(); 
		Hashtable<Integer, Integer> eventStartOffsets = new Hashtable<Integer, Integer>(); 
		Hashtable<Integer, Integer> eventStopOffsets = new Hashtable<Integer, Integer>(); 
		TreeSet<ExamPeriod> periods = ExamPeriod.findAll(context.getUser().getCurrentAcademicSessionId(), examType);
		for (ExamPeriod period: periods) {
			if (period.isUsed()) return null;
			
			days.add(period.getDateOffset());
			times.add(period.getStartSlot());
			Integer length = lengths.get(period.getStartSlot());
			if (length==null)
				lengths.put(period.getStartSlot(),period.getLength());
			else if (!length.equals(period.getLength())) {
				return null;
			}
			Integer eventStartOffset = eventStartOffsets.get(period.getStartSlot());
			if (eventStartOffset == null){
				eventStartOffsets.put(period.getStartSlot(), period.getEventStartOffset());
			} else if (!eventStartOffset.equals(period.getEventStartOffset())){
				return(null);
			}
			Integer eventStopOffset = eventStopOffsets.get(period.getStartSlot());
			if (eventStopOffset == null){
				eventStopOffsets.put(period.getStartSlot(), period.getEventStopOffset());
			} else if (!eventStopOffset.equals(period.getEventStopOffset())){
				return(null);
			}
		}
		if (periods.size()!=days.size()*times.size() || times.size()>5) return null;
		ExamPeriodSetupInterface setup = new ExamPeriodSetupInterface();
		setup.setExamTypeId(examType.getUniqueId());
		setup.setExamTypeName(examType.getLabel());
		setup.setSessionId(context.getUser().getCurrentAcademicSessionId());
		if (times.isEmpty()) {
			for (int i = 0; i < 5; i++) {
				PeriodSetupItemInterface item = new PeriodSetupItemInterface();
				if (i == 0) { item.setStartSlot(222); item.setLength(60); }
				if (i == 1) { item.setStartSlot(240); item.setLength(120); }
				item.setStartOffset(getDefaultStartOffset(examType));
				item.setStopOffset(getDefaultStopOffset(examType));
				setup.addItem(item);
			}
		} else {
			for (Integer slot: times) {
				PeriodSetupItemInterface item = new PeriodSetupItemInterface();
				item.setStartSlot(slot);
				item.setLength(Constants.SLOT_LENGTH_MIN * lengths.get(slot));
				item.setStartOffset(Constants.SLOT_LENGTH_MIN * eventStartOffsets.get(slot));
				item.setStopOffset(Constants.SLOT_LENGTH_MIN * eventStopOffsets.get(slot));
				setup.addItem(item);
			}
			while (setup.getItems().size() < 5) {
				PeriodSetupItemInterface item = new PeriodSetupItemInterface();
				item.setStartOffset(getDefaultStartOffset(examType));
				item.setStopOffset(getDefaultStopOffset(examType));
				setup.addItem(item);
			}
		}
		Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		setup.setBaseOffset(DateUtils.daysBetween(DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear()), session.getExamBeginDate()));
		if (!days.isEmpty()) {
			int startMonth = session.getPatternStartMonth();
			int endMonth = session.getPatternEndMonth();
			int size = session.getDayOfYear(0,endMonth+1) - session.getDayOfYear(1,startMonth);
			BitSet bitSet = new BitSet(size);
			for (Integer day: days) {
				bitSet.set(setup.getBaseOffset() + day);
			}
			String pattern = "";
			for (int i = 0; i < bitSet.length(); i++)
				pattern += (bitSet.get(i) ? "1" : "0");
			setup.setPattern(pattern);
		}
		for (SessionMonth m: PatternDatesBackend.listMonths(session))
			setup.addMonth(m);
		return setup;
	}
	
	protected void saveSetup(SessionContext context, ExamPeriodSetupInterface setup) {
		Set<Integer> days = new HashSet<Integer>();
		if (setup.hasPattern())
			for (int i = 0; i < setup.getPattern().length(); i++) {
				if (setup.getPattern().charAt(i) == '1')
					days.add(i - setup.getBaseOffset());
			}
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
        	ExamType type = ExamTypeDAO.getInstance().get(setup.getExamTypeId(), hibSession);
        	Session session = SessionDAO.getInstance().get(setup.getSessionId());
    		TreeSet<ExamPeriod> periods = ExamPeriod.findAll(setup.getSessionId(), type);
    		TreeSet<Integer> slots = new TreeSet<Integer>();
    		TreeSet<Integer> oldDays = new TreeSet<Integer>();
    		for (Iterator<ExamPeriod> i=periods.iterator();i.hasNext();) {
    			ExamPeriod period = i.next();
    			slots.add(period.getStartSlot());
    			if (!days.contains(period.getDateOffset())) {
    			    for (Exam exam: hibSession.createQuery(
    			    		"select x from Exam x where x.assignedPeriod.uniqueId=:periodId", Exam.class)
    			    		.setParameter("periodId", period.getUniqueId())
    			    		.list()) {
    			    	exam.unassign(context.getUser().getExternalUserId(), hibSession);
    			    }
    				hibSession.remove(period);
    				i.remove();
    			} else {
    				oldDays.add(period.getDateOffset());
    			}
    		}
    		Hashtable<Integer,Integer> length = new Hashtable<Integer,Integer>();
    		Hashtable<Integer,Integer> translate = new Hashtable<Integer,Integer>();
    		Hashtable<Integer,Integer> eventStartOffsets = new Hashtable<Integer,Integer>();
    		Hashtable<Integer,Integer> eventStopOffsets = new Hashtable<Integer,Integer>();
    		
    		TreeSet<Integer> newStarts = new TreeSet<Integer>();
    		Iterator<Integer> it = slots.iterator();
    		if (setup.hasItems())
    			for (PeriodSetupItemInterface item: setup.getItems()) {
    				if (item.getStartSlot() == null) continue;
    				length.put(item.getStartSlot(), item.getLength());
    				eventStartOffsets.put(item.getStartSlot(), item.getStartOffset() == null ? 0 : item.getStartOffset().intValue());
    				eventStopOffsets.put(item.getStartSlot(), item.getStopOffset() == null ? 0 : item.getStopOffset().intValue());
    				if (it.hasNext()) translate.put(it.next(), item.getStartSlot()); else newStarts.add(item.getStartSlot());
    			}
    		for (Iterator<ExamPeriod> i=periods.iterator();i.hasNext();) {
    			ExamPeriod period = i.next();
    			Integer start = translate.get(period.getStartSlot());
    			if (start == null) {
    			    for (Exam exam: hibSession.createQuery(
    			            "select x from Exam x where x.assignedPeriod.uniqueId=:periodId", Exam.class)
    			            .setParameter("periodId", period.getUniqueId())
    			            .list()) {
    			        exam.unassign(context.getUser().getExternalUserId(), hibSession);
    			    }
    			    hibSession.remove(period);
    			    i.remove();
    			} else {
    			    period.setStartSlot(start);
    			    period.setLength(length.get(start) / Constants.SLOT_LENGTH_MIN);
    			    period.setEventStartOffset(eventStartOffsets.get(start) == null?Integer.valueOf(null):Integer.valueOf(eventStartOffsets.get(start)/Constants.SLOT_LENGTH_MIN));
    			    period.setEventStopOffset(eventStopOffsets.get(start) == null?Integer.valueOf(null):Integer.valueOf(eventStopOffsets.get(start)/Constants.SLOT_LENGTH_MIN));
    			    hibSession.merge(period);
    			}
    		}
    		for (Integer day: days) {
    			for (int start : new TreeSet<Integer>(length.keySet())) {
    			    if (oldDays.contains(day) && !newStarts.contains(start)) continue;
    			    ExamPeriod ep = new ExamPeriod();
    			    ep.setSession(session);
    			    ep.setDateOffset(day);
    			    ep.setStartSlot(start);
    			    ep.setLength(length.get(start) / Constants.SLOT_LENGTH_MIN);
    			    ep.setEventStartOffset(eventStartOffsets.get(start) == null?Integer.valueOf(null):Integer.valueOf(eventStartOffsets.get(start)/Constants.SLOT_LENGTH_MIN));
    			    ep.setEventStopOffset(eventStopOffsets.get(start) == null?Integer.valueOf(null):Integer.valueOf(eventStopOffsets.get(start)/Constants.SLOT_LENGTH_MIN));
    			    ep.setExamType(type);
    			    ep.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
    			    hibSession.persist(ep);
    			}
    		}
        	
           	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected void deletePeriod(SessionContext context, Long periodId) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	ExamPeriod ep = (ExamPeriodDAO.getInstance()).get(periodId, hibSession);
    		for (Exam exam: hibSession.createQuery(
    		        "select x from Exam x where x.assignedPeriod.uniqueId=:periodId", Exam.class)
    		        .setParameter("periodId", ep.getUniqueId())
    		        .list()) {
                exam.unassign(context.getUser().getExternalUserId(), hibSession);
    		}
    		hibSession.remove(ep);
    		
    		ChangeLog.addChange(
                    hibSession, 
                    context, 
                    ep, 
                    ChangeLog.Source.EXAM_PERIOD_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);

           	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected int getDateOffset(SessionContext context, Date date) {
		long diff = date.getTime() - SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()).getExamBeginDate().getTime();
		return (int)Math.round(diff/(1000.0 * 60 * 60 * 24));
	}
	
	protected Long savePeriod(SessionContext context, ExamPeriodInterface period) {
        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        Long sessionId = context.getUser().getCurrentAcademicSessionId();
        Long ret = null;
        
        try {
            tx = hibSession.beginTransaction();
            
            ExamPeriod ep = null;
            boolean used = false;
            if (period.getPeriodId() == null) {
            	ep = new ExamPeriod();
            	ep.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
            } else {
            	ep = (ExamPeriodDAO.getInstance()).get(period.getPeriodId(), hibSession);
            	used = ep.isUsed();
            }
            if (!used) {
            	ep.setStartDate(period.getDate());
            	ep.setStartSlot(period.getStartSlot());
            	ep.setLength(period.getLength() == null ? 0 : period.getLength() / Constants.SLOT_LENGTH_MIN);
            	ep.setEventStartOffset(period.getStartOffset() == null ? 0 : period.getStartOffset() / Constants.SLOT_LENGTH_MIN);
            	ep.setEventStopOffset(period.getStopOffset() == null ? 0 : period.getStopOffset() / Constants.SLOT_LENGTH_MIN);
                ep.setExamType(ExamTypeDAO.getInstance().get(period.getExamTypeId()));
            }
            ep.setPrefLevel(period.getPreferenceId() == null ? PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral) : PreferenceLevelDAO.getInstance().get(period.getPreferenceId(), hibSession));

            if (period.getPeriodId() == null) 
        		hibSession.persist(ep);
            else
            	hibSession.merge(ep);
        	ret = ep.getUniqueId();
        	
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    ep, 
                    ChangeLog.Source.EXAM_PERIOD_EDIT, 
                    (period.getPeriodId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE), 
                    null, 
                    null);
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        return ret;
	}

}
