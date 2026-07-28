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

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.admin.ExamPeriodsPage.ExamPeriodsRequest;
import org.unitime.timetable.gwt.client.admin.ExamPeriodsPage.ExamPeriodsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

@GwtRpcImplements(ExamPeriodsRequest.class)
public class ExamPeriodsBackend implements GwtRpcImplementation<ExamPeriodsRequest, ExamPeriodsResponse>{
	protected final static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	protected final static CourseMessages COURSE = Localization.create(CourseMessages.class);

	@Override
	public ExamPeriodsResponse execute(ExamPeriodsRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationPeriods);
		
		ExamPeriodsResponse response = new ExamPeriodsResponse();
		TableInterface table = new TableInterface();
		table.setId("ExamPeriods");
		table.setDefaultSortCookie(MSG.colType());
		table.setName(MSG.sectExaminationPeriods());
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.colType());
		header.addCell(COURSE.columnDatePatternUsed());
        header.addCell(MSG.colDate());
        header.addCell(MSG.colStartTime());
        header.addCell(MSG.colEndTime());
        header.addCell(MSG.colExamLength()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.colEventStartOffset()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.colEventStopOffset()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.colPreference());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        
        Set<ExamPeriod> periods = ExamPeriod.findAll(context.getUser().getCurrentAcademicSessionId(), (Long)null);
		if (periods.isEmpty()) table.setErrorMessage(MSG.infoNoExaminationPeriodsDefined());
        Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_MEETING);
        Formats.Format<Date> stf = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
        
        Set<ExamType> types = null;
        if (!context.hasPermission(Right.StatusIndependent) && context.getUser().getCurrentAuthority().hasRight(Right.ExaminationSolver)) {
            types = new HashSet<ExamType>();
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
        for (ExamType type: (types != null ? types : ExamType.findAll())) {
        	if (canAutoSetup(context, type))
        		response.addCanSetupPeriodType(type.getUniqueId(), type.getLabel());
        }
        
		for (ExamPeriod period: periods) {
			if (types != null && !types.contains(period.getExamType())) continue;
			
			Date startDate = period.getStartDate();
			Date startTime = period.getStartTime();
			Date endTime = period.getEndTime();
			
			LineInterface line = table.addLine();
			line.setId(period.getUniqueId());
			line.setURL("#" + period.getUniqueId());
			line.setAnchor("A" + period.getUniqueId());
			
			line.addCell(period.getExamType().getLabel()).setComparable(period.getExamType().getLabel(), startTime);
			if (period.isUsed()) {
				line.addCell().setComparable(true, period.getExamType().getLabel(), startTime)
					.addImage().setSource("images/accept.png").setAlt(COURSE.altYes()).setTitle(COURSE.altYes());
			} else {
				line.addCell().setComparable(false, period.getExamType().getLabel(), startTime)
					.setTitle(request.isExport() ? COURSE.no() : null);
			}

			line.addCell(sdf.format(startDate)).setComparable(startTime, period.getExamType().getLabel());
			line.addCell(stf.format(startTime)).setComparable(period.getStartSlot(), period.getDateOffset(), period.getExamType().getLabel());
			line.addCell(stf.format(endTime)).setComparable(period.getEndSlot(), period.getDateOffset(), period.getExamType().getLabel());
			line.addCell(String.valueOf(Constants.SLOT_LENGTH_MIN*period.getLength()))
				.setComparable(period.getLength(), startTime, period.getExamType().getLabel())
				.setTextAlignment(Alignment.RIGHT);
			line.addCell(period.getEventStartOffset() == null || period.getEventStartOffset() == 0 ? "" : String.valueOf(Constants.SLOT_LENGTH_MIN*period.getEventStartOffset()))
				.setComparable(period.getEventStartOffset(), startTime, period.getExamType().getLabel())
				.setTextAlignment(Alignment.RIGHT);
			line.addCell(period.getEventStopOffset() == null || period.getEventStopOffset() == 0 ? "" : String.valueOf(Constants.SLOT_LENGTH_MIN*period.getEventStopOffset()))
				.setComparable(period.getEventStopOffset(), startTime, period.getExamType().getLabel())
				.setTextAlignment(Alignment.RIGHT);
			if (period.getPrefLevel() == null || PreferenceLevel.sNeutral.equals(period.getPrefLevel().getPrefProlog()))
				line.addCell();
			else
				line.addCell(period.getPrefLevel().getPrefName())
					.setColor(PreferenceLevel.prolog2color(period.getPrefLevel().getPrefProlog()))
					.setComparable(PreferenceLevel.prolog2int(period.getPrefLevel().getPrefProlog()), startTime, period.getExamType().getLabel());
		}
        
		response.setTable(table);
		response.setCanAdd(true);
		return response;
	}
	
	public static boolean canAutoSetup(SessionContext context, ExamType examType) {
		Set<Integer> days = new TreeSet<Integer>(); 
		TreeSet<Integer> times = new TreeSet<Integer>(); 
		Hashtable<Integer, Integer> lengths = new Hashtable<Integer, Integer>(); 
		Hashtable<Integer, Integer> eventStartOffsets = new Hashtable<Integer, Integer>(); 
		Hashtable<Integer, Integer> eventStopOffsets = new Hashtable<Integer, Integer>(); 
		TreeSet<ExamPeriod> periods = ExamPeriod.findAll(context.getUser().getCurrentAcademicSessionId(), examType);
		for (ExamPeriod period: periods) {
			if (period.isUsed()) return false;
			days.add(period.getDateOffset());
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
		if (periods.size()!=days.size()*times.size() || times.size() > 5) return false;
		return true;
	}

}
