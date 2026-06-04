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
package org.unitime.timetable.server.exams;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.events.RoomFilterBackend.LocationMatcher;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridCell;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridModel;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridPeriod;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridTable;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.HasRoom;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.PreferenceLevel.PrefColor;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.exams.ExamGridContext.Background;
import org.unitime.timetable.server.exams.ExamGridContext.Resource;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.RoomAvailability;

@GwtRpcImplements(ExamGridRequest.class)
public class ExamGridBackend implements GwtRpcImplementation<ExamGridRequest, ExamGridTable>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);
	protected static GwtConstants GWT_CONST = Localization.create(GwtConstants.class);
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public ExamGridTable execute(ExamGridRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationTimetable);
		
		ExamGridContext cx = new ExamGridContext(context, request.getFilter());

		for (FilterParameterInterface p: request.getFilter().getParameters()) {
			if ("examType".equals(p.getName())) {
				context.setAttribute(SessionAttribute.ExamType, Long.valueOf(p.getValue() != null ? p.getValue() : p.getDefaultValue()));
			} else if (p.getValue() != null) {
				context.getUser().setProperty("ExamGrid." + p.getName(), p.getValue());
			}
		}

		ExamSolverProxy solver = examinationSolverService.getSolver();
		
		return getTable(cx, solver);
	}
	
	public ExamGridTable getTable(final ExamGridContext cx, ExamSolverProxy solver) {
		ExamGridTable table = new ExamGridTable();
		
		for (ExamPeriod period: cx.getPeriods()) {
			table.addPeriod(toExamGridPeriod(cx, period));
		}
		
		CacheAssignedExams cache = null;
	    if (solver==null || !solver.getExamTypeId().equals(cx.getExamTypeId()))
	    	cache = new CacheAssignedExams(cx.getSessionId(), cx.getExamTypeId());

	    try {
	    	switch (cx.getResource()) {
	    	case Room:
		        for (Location location: Location.findAllExamLocations(cx.getSessionId(), cx.getExamTypeId())) {
		        	if (cx.getRoomFilter() != null && !cx.getRoomFilter().match(new LocationMatcher(location, cx.getRoomFeatureTypes()))) continue;
		            if (cx.match(location.getLabel())) {
		                if (solver!=null && solver.getExamTypeId().equals(cx.getExamTypeId()))
		                	table.addModel(toExamGridModel(cx, location, solver.getAssignedExamsOfRoom(location.getUniqueId())));
		                else if (cache != null)
		                	table.addModel(toExamGridModel(cx, location,
		                			cache.getAssignedExamsOfLocation(location.getUniqueId())));
		                else
		                	table.addModel(toExamGridModel(cx, location,
	                                Exam.findAssignedExamsOfLocation(cx.getSessionId(), cx.getExamTypeId())));
		            }
		        }
		        break;
	    	case Instructor:
		        Hashtable<String, ExamGridModel> models = new Hashtable<String,ExamGridModel> ();
	            for (DepartmentalInstructor instructor: DepartmentalInstructor.findAllExamInstructors(cx.getSessionId(), cx.getExamTypeId())) {
	                if (cx.match(instructor.getName(cx.getNameFormat()))) {
	                    Collection<ExamAssignmentInfo> assignments = null;
	                    if (solver!=null  && solver.getExamTypeId().equals(cx.getExamTypeId()))
	                        assignments = solver.getAssignedExamsOfInstructor(instructor.getUniqueId());
	                    else if (cache != null)
	                    	assignments = cache.getAssignedExamsOfInstructor(instructor.getUniqueId());
	                    else
	                        assignments = Exam.findAssignedExamsOfInstructor(instructor.getUniqueId(), cx.getExamTypeId());
	                    if (instructor.getExternalUniqueId()==null) {
	                    	table.addModel(toExamGridModel(cx, instructor, cx.getNameFormat(), assignments));
	                    } else {
	                    	ExamGridModel m = models.get(instructor.getExternalUniqueId());
	                    	if (m == null) {
	                    		m = toExamGridModel(cx, instructor, cx.getNameFormat(), assignments);
	                    		models.put(instructor.getExternalUniqueId(), m);
	                    		table.addModel(m);
	                    	} else {
	                    		for (ExamAssignmentInfo assignment: assignments)
	                    			m.addCell(toExamGridCell(cx, instructor.getUniqueId(), assignment));
	                    	}
	                    }
	                }
	            }
	            break;
	    	case Subject:
		        for (SubjectArea subject: SubjectArea.getSubjectAreaList(cx.getSessionId())) {
		            if (cx.match(subject.getSubjectAreaAbbreviation())) {
	                    if (solver!=null && solver.getExamTypeId().equals(cx.getExamTypeId()))
	                    	table.addModel(toExamGridModel(cx, subject, solver.getAssignedExams(subject.getUniqueId())));
	                    else if (cache != null)
	                    	table.addModel(toExamGridModel(cx, subject, cache.getAssignedExamsOfSubjectArea(subject.getUniqueId())));
	                    else
	                    	table.addModel(toExamGridModel(cx, subject, Exam.findAssignedExams(cx.getSessionId(), subject.getUniqueId(), cx.getExamTypeId()))); 
		            }
		        }
		    }
	    } catch (GwtRpcException e) {
	    	throw e;
	    } catch (Exception e) {
	    	new GwtRpcException(e.getMessage(), e);
	    }
	    
	    if (table.hasModels()) {
	    	Collections.sort(table.getModels(), new Comparator<ExamGridModel>() {
				@Override
				public int compare(ExamGridModel m1, ExamGridModel m2) {
					switch (cx.getOrderBy()) {
					case NameAsc :
						return m1.getName().compareTo(m2.getName());
					case NameDesc :
						return -m1.getName().compareTo(m2.getName());
					case SizeAsc:
						if (m1.getSize() == m2.getSize())
							return m1.getName().compareTo(m2.getName());
						else
							return Integer.compare(m1.getSize(), m2.getSize());
					case SizeDesc :
						if (m1.getSize() == m2.getSize())
							return m1.getName().compareTo(m2.getName());
						else
							return -Double.compare(m1.getSize(), m2.getSize());
					}
					return m1.getId().compareTo(m2.getId());
				}
			});
	    }
	    
	    fillInLegend(cx, table);
		
		return table;
	}
	
	protected ExamGridPeriod toExamGridPeriod(ExamGridContext cx, ExamPeriod period) {
		ExamGridPeriod ret = new ExamGridPeriod();
		ret.setId(period.getUniqueId());
		ret.setDate(period.getDateOffset());
		ret.setStart(period.getStartSlot());
		ret.setLength(period.getLength());
		Calendar c = Calendar.getInstance(Localization.getJavaLocale());
        c.setTime(period.getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, period.getDateOffset());
        ret.setDateLabel(Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD).format(c.getTime()));
        ret.setTimeLabel(Constants.slot2str(period.getStartSlot()) + " - " + Constants.slot2str(period.getEndSlot()));
        switch (c.get(Calendar.DAY_OF_WEEK)) {
        case Calendar.MONDAY:
        	ret.setDayOfWeek(Constants.DAY_MON);
        	break;
        case Calendar.TUESDAY:
        	ret.setDayOfWeek(Constants.DAY_TUE);
        	break;
        case Calendar.WEDNESDAY:
        	ret.setDayOfWeek(Constants.DAY_WED);
        	break;
        case Calendar.THURSDAY:
        	ret.setDayOfWeek(Constants.DAY_THU);
        	break;
        case Calendar.FRIDAY:
        	ret.setDayOfWeek(Constants.DAY_FRI);
        	break;
        case Calendar.SATURDAY:
        	ret.setDayOfWeek(Constants.DAY_SAT);
        	break;
        case Calendar.SUNDAY:
        	ret.setDayOfWeek(Constants.DAY_SUN);
        	break;
        }
        int week = c.get(Calendar.WEEK_OF_YEAR);
        ret.setWeek(week);
        Date first = c.getTime();
        while (c.get(Calendar.WEEK_OF_YEAR) == week) {
        	first = c.getTime();
        	c.add(Calendar.DAY_OF_YEAR, -1);
        }
        c.add(Calendar.DAY_OF_YEAR, +7);
        ret.setWeekLabel(MESSAGES.week(week) + "\n" +
        		Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT).format(first) + " - " +
        		Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT).format(c.getTime()));
        if (period.getPrefLevel() != null && !PreferenceLevel.sNeutral.equals(period.getPrefLevel().getPrefProlog())) {
        	if (cx.isBgPreferences() && cx.getBackground() == Background.PeriodPref)
        		ret.setBgColor(toBgColor(period.getPrefLevel()));
        	else if (period.getPrefLevel() != null && PreferenceLevel.sProhibited.equals(period.getPrefLevel().getPrefProlog()))
        		ret.setBgColor("#c8c8c8");
        }
		return ret;
	}
	
	protected String toBgColor(PreferenceLevel pref) {
		return toBgColor(pref == null ? null : pref.getPrefProlog());
	}
	
	protected String toBgColor(String prolog) {
		PrefColor color = (prolog == null || prolog.isEmpty() ? null : PrefColor.fromProlog(prolog));
		if (color == null) color = PrefColor.Neutral;
		return PrefColor.toHex(color.getLightBgColor());
	}
	
	protected ExamGridModel toExamGridModel(ExamGridContext cx, Location location, Collection<ExamAssignmentInfo> assignments) {
		ExamGridModel model = new ExamGridModel();
		model.setId(location.getUniqueId());
		model.setName(location.getLabel());
		model.setSize(location.getCapacity());
		
		for (ExamAssignmentInfo assignment: assignments)
			model.addCell(toExamGridCell(cx, location.getUniqueId(), assignment));
		
		for (ExamLocationPref pref: location.getExamPreferences()) {
			if (pref.getPrefLevel() == null || PreferenceLevel.sNeutral.equals(pref.getPrefLevel().getPrefProlog()))
				continue;
			if (pref.getExamPeriod().getExamType().getUniqueId().equals(cx.getExamTypeId())) {
				if (cx.isBgPreferences() && cx.getBackground() == Background.PeriodPref) {
					model.setPeriodBgColor(pref.getExamPeriod().getUniqueId(), toBgColor(pref.getPrefLevel().getPrefProlog()));
				} else if (PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) {
					model.setPeriodBgColor(pref.getExamPeriod().getUniqueId(), "#c8c8c8");
				}
			}
		}
		
		if (!location.isIgnoreRoomCheck() && RoomAvailability.getInstance()!=null) {
			Collection<TimeBlock> unavailabilities = RoomAvailability.getInstance().getRoomAvailability(
                    location.getUniqueId(), 
                    cx.getBounds()[0], cx.getBounds()[1],
                    cx.getExamTypeRef());
			if (unavailabilities != null) {
				for (TimeBlock unavailability: unavailabilities) {
					for (ExamPeriod period: cx.getPeriods()) {
						if (period.overlap(unavailability)) {
							model.addCell(toExamGridCell(cx, period, unavailability));
						}
					}
				}
			}
        }
		return model;
	}
	
	protected ExamGridModel toExamGridModel(ExamGridContext cx, DepartmentalInstructor instructor, String nameFormat, Collection<ExamAssignmentInfo> assignments) {
		ExamGridModel model = new ExamGridModel();
		model.setId(instructor.getUniqueId());
		model.setName(instructor.getName(nameFormat));
		
		for (ExamAssignmentInfo assignment: assignments)
			model.addCell(toExamGridCell(cx, instructor.getUniqueId(), assignment));
		
		if (RoomAvailability.getInstance()!=null) {
			Collection<TimeBlock> unavailabilities = RoomAvailability.getInstance().getInstructorAvailability(
					instructor.getUniqueId(), 
                    cx.getBounds()[0], cx.getBounds()[1],
                    cx.getExamTypeRef());
			if (unavailabilities != null) {
				for (TimeBlock unavailability: unavailabilities) {
					for (ExamPeriod period: cx.getPeriods()) {
						if (period.overlap(unavailability)) {
							model.addCell(toExamGridCell(cx, period, unavailability));
						}
					}
				}
			}
        }
		return model;
	}
	
	protected ExamGridModel toExamGridModel(ExamGridContext cx, SubjectArea subject, Collection<ExamAssignmentInfo> assignments) {
		ExamGridModel model = new ExamGridModel();
		model.setId(subject.getUniqueId());
		model.setName(subject.getSubjectAreaAbbreviation());
		
		for (ExamAssignmentInfo assignment: assignments)
			model.addCell(toExamGridCell(cx, subject.getUniqueId(), assignment));

		return model;
	}
	
	protected ExamGridCell toExamGridCell(ExamGridContext cx, Long resourceId, ExamAssignmentInfo assignment) {
		ExamGridCell cell = new ExamGridCell();
		cell.setId(assignment.getExamId());
		if (cx.isShowSections())
			cell.add(assignment.getSectionName("\n")).setClassName("name").setInline(false);
		else
			cell.add(assignment.getExamName()).setClassName("name").setInline(false);
		cell.setPeriodId(assignment.getPeriodId());
		if (cx.getResource() != Resource.Room)
			cell.add(assignment.getRoomsName(", ")).setClassName("room").setInline(false);
		if (cx.isShowStudentConflicts()) {
			int dc = assignment.getNrDirectConflicts();
			int m2d = assignment.getNrMoreThanTwoConflicts();
			int btb = assignment.getNrBackToBackConflicts();
			CellInterface conf = cell.add(null).setClassName("conflicts").setInline(false).setNoWrap(true);
			conf.add(String.valueOf(dc))
				.setColor(cx.isUsePrefStyles() || dc <= 0 ? null : PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
				.setClassName(cx.isUsePrefStyles() && dc > 0 ? "pref-" + PreferenceLevel.prolog2char(PreferenceLevel.sProhibited) : null);
			conf.add(", ");
			conf.add(String.valueOf(m2d))
				.setColor(cx.isUsePrefStyles() ||  m2d <= 0 ? null : PreferenceLevel.prolog2color(PreferenceLevel.sStronglyDiscouraged))
				.setClassName(cx.isUsePrefStyles() && m2d > 0 ? "pref-" + PreferenceLevel.prolog2char(PreferenceLevel.sStronglyDiscouraged) : null);
			conf.add(", ");
			conf.add(String.valueOf(btb))
				.setColor(cx.isUsePrefStyles() || btb <= 0 ? null : PreferenceLevel.prolog2color(PreferenceLevel.sDiscouraged))
				.setClassName(cx.isUsePrefStyles() && btb > 0 ? "pref-" + PreferenceLevel.prolog2char(PreferenceLevel.sDiscouraged) : null);
		}
		switch (cx.getBackground()) {
		case PeriodPref:
			cell.setBgColor(toBgColor(assignment.getPeriodPref()));
			break;
		case RoomPref:
            if (cx.getResource() == Resource.Room)
            	cell.setBgColor(toBgColor(assignment.getRoomPref(resourceId)));
            else
            	cell.setBgColor(toBgColor(assignment.getRoomPref()));
			break;
		case DistPref:
			cell.setBgColor(toBgColor(assignment.getDistributionPref()));
			break;
        case StudentConfs:
            if (assignment.getNrDirectConflicts()>0)
            	cell.setBgColor(toBgColor(PreferenceLevel.sProhibited));
            else if (assignment.getNrMoreThanTwoConflicts()>0)
            	cell.setBgColor(toBgColor(PreferenceLevel.sStronglyDiscouraged));
            else if (assignment.getNrBackToBackConflicts()>0)
            	cell.setBgColor(toBgColor(PreferenceLevel.sDiscouraged));
            else
            	cell.setBgColor(toBgColor(PreferenceLevel.sNeutral));
            break; 
        case DirectStudentConfs:
        	cell.setBgColor(conflicts2color(assignment.getNrDirectConflicts(), 15));
        	break;
        case MoreThanTwoADayStudentConfs:
        	cell.setBgColor(conflicts2color(assignment.getNrMoreThanTwoConflicts(), 15));
        	break;
        case BackToBackStudentConfs:
        	cell.setBgColor(conflicts2color(assignment.getNrBackToBackConflicts(), 15));
        	break;
        case InstructorConfs:
            if (assignment.getNrInstructorDirectConflicts()>0)
            	cell.setBgColor(toBgColor(PreferenceLevel.sProhibited));
            else if (assignment.getNrInstructorMoreThanTwoConflicts()>0)
            	cell.setBgColor(toBgColor(PreferenceLevel.sStronglyDiscouraged));
            else if (assignment.getNrInstructorBackToBackConflicts()>0)
            	cell.setBgColor(toBgColor(PreferenceLevel.sDiscouraged));
            else
            	cell.setBgColor(toBgColor(PreferenceLevel.sNeutral));
            break;
        case DirectInstructorConfs:
        	cell.setBgColor(conflicts2color(assignment.getNrInstructorDirectConflicts(), 5));
        	break;
        case MoreThanTwoADayInstructorConfs:
        	cell.setBgColor(conflicts2color(assignment.getNrInstructorMoreThanTwoConflicts(), 5));
        	break;
        case BackToBackInstructorConfs:
        	cell.setBgColor(conflicts2color(assignment.getNrInstructorBackToBackConflicts(), 5));
        	break;
        default:
        	cell.setBgColor(toBgColor(PreferenceLevel.sNeutral));
        	break;
		}
		cell.setUrl("examInfo.action?examId="+assignment.getExamId());
		cell.setDialog(MESSAGES.dialogExamAssign());
		return cell;
	}
		
	
	protected ExamGridCell toExamGridCell(ExamGridContext cx, ExamPeriod period, TimeBlock block) {
		ExamGridCell cell = new ExamGridCell();
		cell.setId(-block.getEventId());
		cell.add(block.getEventName()).setClassName("name");
		cell.setPeriodId(period.getUniqueId());
		if (block instanceof HasRoom && cx.getResource() != Resource.Room)
			cell.add(((HasRoom)block).getLabel()).setClassName("room");
		cell.setBgColor("#c8c8c8");
		return cell;
	}
	
	protected void fillInLegend(ExamGridContext cx, ExamGridTable table) {
		switch (cx.getBackground()) {
		case PeriodPref:
			table.addAssignedLegend(toBgColor(PreferenceLevel.sRequired), MESSAGES.legendRequiredPeriod());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sStronglyPreferred), MESSAGES.legendStronglyPreferredPeriod());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sPreferred), MESSAGES.legendPreferredPeriod());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sNeutral), MESSAGES.legendNoPeriodPreference());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sDiscouraged), MESSAGES.legendDiscouragedPeriod());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendStronglyDiscouragedPeriod());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sProhibited), MESSAGES.legendProhibitedPeriod());
	        break;
		case RoomPref:
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sRequired), MESSAGES.legendRequiredRoom());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sStronglyPreferred), MESSAGES.legendStronglyPreferredRoom());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sPreferred), MESSAGES.legendPreferredRoom());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sNeutral), MESSAGES.legendNoRoomPreference());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sDiscouraged), MESSAGES.legendDiscouragedRoom());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendStronglyDiscouragedRoom());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sProhibited), MESSAGES.legendProhibitedRoom());
	        break;
		case InstructorConfs:
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sNeutral), MESSAGES.legendNoInstructorConflict());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sDiscouraged), MESSAGES.legendOneOrMoreInstructorBackToBackConflicts());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendOneOrMoreInstructorThreeOrMoreExamsADayConflicts());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sProhibited), MESSAGES.legendOneOrMoreInstructorDirectConflicts());
	        break;
		case StudentConfs:
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sNeutral), MESSAGES.legendNoStudentConflict());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sDiscouraged), MESSAGES.legendOneOrMoreStudentBackToBackConflicts());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendOneOrMoreStudentThreeOrMoreExamsADayStudentConflicts());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sProhibited), MESSAGES.legendOneOrMoreStudentDirectConflicts());
	        break;
		case DirectInstructorConfs:
			for (int nrConflicts = 0; nrConflicts <= 5; nrConflicts++)
				table.addAssignedLegend(conflicts2color(nrConflicts, 5), MESSAGES.legendInstructorConflicts(nrConflicts + (nrConflicts == 6 ? " " + MESSAGES.legendOrMore() : "")));
			break;
		case MoreThanTwoADayInstructorConfs:
			for (int nrConflicts = 0; nrConflicts <= 5; nrConflicts++)
				table.addAssignedLegend(conflicts2color(nrConflicts, 5), MESSAGES.legendInstructorMoreThanTwoExamsADayConflicts(nrConflicts + (nrConflicts == 15 ? " " + MESSAGES.legendOrMore() : "")));
			break;
		case BackToBackInstructorConfs:
			for (int nrConflicts = 0; nrConflicts <= 5; nrConflicts++)
				table.addAssignedLegend(conflicts2color(nrConflicts, 5), MESSAGES.legendInstructorBackToBackConflicts(nrConflicts + (nrConflicts == 15 ? " " + MESSAGES.legendOrMore() : "")));
			break;
		case DirectStudentConfs:
			for (int nrConflicts = 0; nrConflicts <= 15; nrConflicts++)
				table.addAssignedLegend(conflicts2color(nrConflicts, 15), MESSAGES.legendStudentDirectConflicts(nrConflicts + (nrConflicts == 6 ? " " + MESSAGES.legendOrMore() : "")));
			break;
		case MoreThanTwoADayStudentConfs:
			for (int nrConflicts = 0; nrConflicts <= 15; nrConflicts++)
				table.addAssignedLegend(conflicts2color(nrConflicts, 15), MESSAGES.legendStudentMoreThanTwoExamsADayConflicts(nrConflicts + (nrConflicts == 15 ? " " + MESSAGES.legendOrMore() : "")));
			break;
		case BackToBackStudentConfs:
			for (int nrConflicts = 0; nrConflicts <= 15; nrConflicts++)
				table.addAssignedLegend(conflicts2color(nrConflicts, 15), MESSAGES.legendStudentBackToBackConflicts(nrConflicts + (nrConflicts == 15 ? " " + MESSAGES.legendOrMore() : "")));
			break;
		case DistPref:
			table.addAssignedLegend(toBgColor(PreferenceLevel.sNeutral), MESSAGES.legendNoViloatedDistributionConstraint());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sDiscouraged), MESSAGES.legendDiscouragedOrPreferredDistributionConstraintViolated());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendStronglyDiscouragedOrPreferredDistributionConstraintViolated());
	        table.addAssignedLegend(toBgColor(PreferenceLevel.sProhibited), MESSAGES.legendRequiredOrProhibitedDistributionConstraintViolated());
	        break;
		case None:
			break;
		}
    	table.addNotAssignedLegend("#c8c8c8", MESSAGES.legendPeriodNotAvailable());
		switch (cx.getBackground()) {
		case PeriodPref:
	        table.addNotAssignedLegend(toBgColor(PreferenceLevel.sStronglyPreferred), MESSAGES.legendStronglyPreferredPeriod());
	        table.addNotAssignedLegend(toBgColor(PreferenceLevel.sPreferred), MESSAGES.legendNoPeriodPreference());
	        table.addNotAssignedLegend(toBgColor(PreferenceLevel.sNeutral), MESSAGES.legendNoPeriodPreference());
	        table.addNotAssignedLegend(toBgColor(PreferenceLevel.sDiscouraged), MESSAGES.legendDiscouragedPeriod());
	        table.addNotAssignedLegend(toBgColor(PreferenceLevel.sStronglyDiscouraged), MESSAGES.legendStronglyPreferredPeriod());
	        table.addNotAssignedLegend(toBgColor(PreferenceLevel.sProhibited), MESSAGES.legendProhibitedPeriod());
	        break;
	    default:
		}
	}

	
    public static String conflicts2color(int p, int max) {
    	int[] points = new int[] {   0,   max / 3,  2 * max / 3,  max };
    	int[] r = new int[] {      240, 240, 240, 240 };
    	int[] g = new int[] {      240, 180, 120, 60 };
    	int[] b = new int[] {      240,  60,  60, 60 };
    	for (int i = 1; i < points.length; i++) {
    		if (p <= points[i])
    			return PrefColor.toHex(
    					(gradient(points[i-1], r[i-1], points[i], r[i], p) << 16) +
    					(gradient(points[i-1], g[i-1], points[i], g[i], p) << 8) +
    					gradient(points[i-1], b[i-1], points[i], b[i], p));
    	}
    	return PrefColor.toHex((r[points.length - 1] << 16) + (g[points.length - 1] << 8) + b[points.length - 1]);
    }
    
	private static int gradient(int min, int v1, int max, int v2, int value) {
    	return (value <= min ? v1 : value >= max ? v2 : v1 + (v2 - v1) * (value - min) / (max - min));
    }
}
