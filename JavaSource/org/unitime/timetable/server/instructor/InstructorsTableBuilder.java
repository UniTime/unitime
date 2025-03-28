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
package org.unitime.timetable.server.instructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.ImageGenerator;
import org.unitime.timetable.gwt.client.tables.TableInterface.ImageInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.TableBuilder;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.RequiredTimeTable;

public class InstructorsTableBuilder extends TableBuilder{
	private String iInstructorSortOrder;
	private Format<Date> iDF;

	public InstructorsTableBuilder(SessionContext context, String backType, String backId) {
		super(context, backType, backId);
		iInstructorSortOrder = UserProperty.SortNames.get(context.getUser());
		iDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	}
	
	public boolean matches(DepartmentalInstructor instructor, FilterInterface filter) {
		String positions = filter.getParameterValue("positions");
		if (positions != null && !positions.isEmpty()) {
			boolean positionsMatch = false;
			for (String posId: positions.split(",")) {
				if (posId.equals("-1") && instructor.getPositionType() == null) {
					positionsMatch = true; break;
				} else if (instructor.getPositionType() != null && instructor.getPositionType().getUniqueId().toString().equals(posId)) {
					positionsMatch = true; break;
				}
			}
			if (!positionsMatch) return false;
		}
		
		return true;
	}
	
	public TableInterface generateTableForDepartment(Department department, FilterInterface filter, SessionContext context) {
		List<DepartmentalInstructor> instructors = DepartmentalInstructor.findInstructorsForDepartment(department.getUniqueId());
		
		TableInterface table = new TableInterface();
		table.setName(MSG.sectionDepartmentalInstructors(department.getName()));
		table.setId("Instructors");
		LineInterface header = table.addHeader();
		
		boolean hasCoursePrefs = false;
		boolean hasTeachPref = false;
		boolean hasMaxLoad = false;
		boolean hasUnavailableDates = false;
		Map<String, InstructorSurvey> instructorSurveys = InstructorSurvey.getInstructorSurveysForDepartment(department.getUniqueId());
		boolean hasInstructorSurvey = (instructorSurveys != null && !instructorSurveys.isEmpty());
		TreeSet<InstructorAttributeType> attributeTypes = new TreeSet<InstructorAttributeType>(new Comparator<InstructorAttributeType>() {
			@Override
			public int compare(InstructorAttributeType o1, InstructorAttributeType o2) {
				return o1.getReference().compareTo(o2.getReference());
			}
		});
		for (DepartmentalInstructor di: instructors) {
			if (!matches(di, filter)) continue;
			if (!di.getPreferences(InstructorCoursePref.class).isEmpty()) hasCoursePrefs = true;
			if (di.getMaxLoad() != null && di.getMaxLoad() > 0f) hasMaxLoad = true;
			if (di.hasUnavailabilities()) hasUnavailableDates = true;
			if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) hasTeachPref = true;
			for (InstructorAttribute at: di.getAttributes())
				if (at.getType() != null)
					attributeTypes.add(at.getType());
		}
		
		header.addCell(MSG.columnExternalId()).setSortable(true);
		header.addCell(MSG.columnInstructorName()).setSortable(true);
		header.addCell(MSG.columnInstructorPosition()).setSortable(true);
		header.addCell(MSG.columnInstructorNote()).setSortable(true);
		header.addCell(MSG.columnPreferences() + "\n" + MSG.columnTimePref());
		header.addCell("\n" + MSG.columnRoomPref());
		header.addCell("\n" + MSG.columnDistributionPref());
		if (hasCoursePrefs)
			header.addCell("\n" + MSG.columnCoursePref());
		if (hasTeachPref)
			header.addCell(MSG.columnTeachingPreference()).setSortable(true);
		if (hasUnavailableDates)
			header.addCell(MSG.columnUnavailableDates());
		if (hasMaxLoad)
			header.addCell(MSG.columnMaxTeachingLoad()).setSortable(true);
		for (InstructorAttributeType at: attributeTypes)
			header.addCell(at.getReference()).setSortable(true);
		header.addCell(MSG.columnInstructorClassAssignments());
		header.addCell(MSG.columnInstructorExamAssignments());
		header.addCell(MSG.columnInstructorIgnoreTooFar()).setSortable(true);
		if (hasInstructorSurvey)
			header.addCell(MSG.columnInstrSurvey()).setSortable(true);
		
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n").replace("<BR>", "\n"));
    		cell.addStyle("white-space: pre");
    	}
		
		List<Long> ids = new ArrayList<Long>();
		for (DepartmentalInstructor di: instructors) {
			if (!matches(di, filter)) continue;
			LineInterface line = table.addLine();
			line.setId(di.getUniqueId());
			line.setClassName("instructor-line");
			ids.add(di.getUniqueId());
			if (context.hasPermission(di, Right.InstructorDetail))
				line.setURL("instructor?id=" + di.getUniqueId());
			if (di.getExternalUniqueId() != null && !di.getExternalUniqueId().trim().isEmpty()) {
				line.addCell(di.getExternalUniqueId()).addAnchor("A" + di.getUniqueId());
			} else {
				line.addCell().setComparable("").addAnchor("A" + di.getUniqueId())
					.setAria(MSG.altNotAvailableExternalId())
					.addImage()
					.setSource("images/error.png")
					.setAlt(MSG.altNotAvailableExternalId())
					.setTitle(MSG.titleInstructorExternalIdNotSupplied());
			}
			if (di.getUniqueId().toString().equals(getBackId()))
				line.getCells().get(0).addAnchor("back");
			line.addCell(di.getName(getInstructorNameFormat()))
				.setComparable(CommonValues.SortAsDisplayed.eq(iInstructorSortOrder) ? di.getName(getInstructorNameFormat()).toLowerCase() : di.nameLastNameFirst().toLowerCase());
			
			if (di.getPositionType() != null)
				line.addCell(di.getPositionType().getLabel()).setComparable(di.getPositionType().getSortOrder());
			else
				line.addCell(MSG.instructorPositionNotSpecified()).addStyle("font-style: italic;").setComparable(Integer.MAX_VALUE);
			line.addCell(di.getNote()).addStyle("white-space: pre-wrap; max-width: 300px; ");
			
			CellInterface timePref = line.addCell().addStyle("white-space: pre-wrap;");
			for (TimePref tp: di.getTimePreferences()) {
				RequiredTimeTable rtt = tp.getRequiredTimeTable();
				if (getGridAsText()) {
					timePref.add(rtt.getModel().toString().replace(", ", "\n"))
						.setMouseOver("$wnd.showGwtInstructorAvailabilityHint($wnd.lastMouseOverElement, '" + di.getUniqueId() + "');")
						.setMouseOut("$wnd.hideGwtInstructorAvailabilityHint();");
				} else {
					timePref.add(null).setImage(
	        				new ImageInterface().setSource("pattern?v=" + (getTimeVertival() ? 1 : 0) + "&s=" + rtt.getModel().getDefaultSelection() + "&p=" + rtt.getModel().getPreferences()
	            					).setAlt(rtt.getModel().toString())
	        						.setGenerator(new ImageGenerator() {
	        							public Object generate() {
	        								return rtt.createBufferedImage(getTimeVertival());
	        							}
	        						}
	                				))
						.setMouseOver("$wnd.showGwtInstructorAvailabilityHint($wnd.lastMouseOverElement, '" + di.getUniqueId() + "');")
						.setMouseOut("$wnd.hideGwtInstructorAvailabilityHint();")
	        			.addStyle("display: inline-block;")
	        			.setAria(rtt.getModel().toString());
				}
			}
			
			CellInterface roomPref = line.addCell();
			for (Preference p: di.effectivePreferences(RoomPref.class))
				roomPref.addItem(preferenceCell(p));
			for (Preference p: di.effectivePreferences(BuildingPref.class))
				roomPref.addItem(preferenceCell(p));
			for (Preference p: di.effectivePreferences(RoomFeaturePref.class))
				roomPref.addItem(preferenceCell(p));
			for (Preference p: di.effectivePreferences(RoomGroupPref.class))
				roomPref.addItem(preferenceCell(p));
			
			CellInterface distPref = line.addCell();
			for (Preference p: di.effectivePreferences(DistributionPref.class))
				distPref.addItem(preferenceCell(p));
			
			if (hasCoursePrefs) {
				CellInterface coursePref = new CellInterface();
				for (Preference p: di.effectivePreferences(InstructorCoursePref.class))
					coursePref.addItem(preferenceCell(p));
				line.addCell(coursePref);
			}
			
			if (hasTeachPref) {
				PreferenceLevel pref = di.getTeachingPreference();
				if (pref == null) pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited);
				line.addCell(pref.getPrefName())
					.setColor(PreferenceLevel.prolog2color(pref.getPrefProlog()))
					.setComparable(pref.getPrefId());
			}
			
			if (hasUnavailableDates)
				line.addCell(di.hasUnavailabilities() ? di.getUnavailableDaysText("\n") : "").addStyle("white-space: pre-wrap;");
			
			if (hasMaxLoad) {
				if (di.getMaxLoad() == null)
					line.addCell("").setComparable(0f);
				else
					line.addCell(Formats.getNumberFormat("0.##").format(di.getMaxLoad())).setComparable(di.getMaxLoad());
			}
			
			for (InstructorAttributeType at: attributeTypes) {
				CellInterface cell = line.addCell();
				for (InstructorAttribute a: di.getAttributes(at))
					cell.add(a.getCode()).setTitle(a.getName()).setInline(false);
			}
			
			TreeSet<ClassInstructor> classes = new TreeSet<ClassInstructor>(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_LABEL)));
			classes.addAll(di.getClasses());
			CellInterface cls = line.addCell();
			for (ClassInstructor ci: classes) {
				Class_ c = ci.getClassInstructing();
				String className = c.getClassLabel();
	    		String title = className;
	    		if (c.isCancelled())
	    			title = MSG.classNoteCancelled(c.getClassLabel());
	    		title += " ("+ci.getPercentShare()+"%"+(ci.isLead().booleanValue()?", " + MSG.titleCheckConflicts() :"")+")";
	    		if (!c.isDisplayInstructor())
	    			title += " - " + MSG.titleDoNotDisplayInstructor();
	    		CellInterface cl = cls.add(className);
	    		cl.setTitle(title);
	    		if (c.isCancelled()) {
	    			cl.addStyle("text-decoration: line-through;");
	    			cl.setColor("gray");
	    		}
	    		if (ci.isLead())
	    			cl.addStyle("font-weight:bold;");
	    		if (!c.isDisplayInstructor())
	    			cl.addStyle("font-style:italic;");
	    		cl.setNoWrap(true).setInline(false);
			}
			
			TreeSet<Exam> exams = new TreeSet<Exam>(di.getExams());
			CellInterface exs = line.addCell();
			for (Exam exam: exams) {
				if (!context.hasPermission(exam, Right.ExaminationView)) continue;
				CellInterface ex = exs.add(exam.getLabel());
				ex.setTitle(exam.getLabel() + " " + exam.getExamType().getLabel());
				if (exam.getExamType().getType() == ExamType.sExamTypeFinal)
					ex.addStyle("font-weight:bold;");
				ex.setNoWrap(true).setInline(false);
			}
			
			if (di.isIgnoreToFar() != null && di.isIgnoreToFar().booleanValue()) {
				line.addCell().setComparable(true).addImage().setSource("images/accept.png")
					.setAlt(MSG.titleIgnoreTooFarDistances()).setTitle(MSG.titleIgnoreTooFarDistances());
			} else {
				line.addCell().setComparable(false);
			}
			
			if (hasInstructorSurvey) {
				if (di.getExternalUniqueId() == null || di.getExternalUniqueId().isEmpty()) {
					line.addCell().setComparable(Long.MAX_VALUE);
				} else {
					InstructorSurvey is = instructorSurveys.get(di.getExternalUniqueId());
					if (is != null && is.getCourseRequirements().isEmpty() && is.getPreferences().isEmpty() && (is.getNote() == null || is.getNote().isEmpty()))
						is = null;
					if (is != null) {
						if (is.getSubmitted() != null) {
							line.addCell(iDF.format(is.getSubmitted())).setComparable(is.getSubmitted().getTime());
						} else {
							line.addCell(MSG.instrSurveyNotSubmitted()).setComparable(0l).addStyle("font-style: italic;").setColor("#dcb414");
						}
					} else {
						line.addCell(MSG.instrSurveyNotFilled()).setComparable(-1l).addStyle("font-style: italic;").setColor("#c81e14");
					}
				}
			}
		}
		
		table.setNavigationLevel(Navigation.sInstructionalOfferingLevel);
		Navigation.set(context, Navigation.sInstructionalOfferingLevel, ids);
		
		return table;
	}

}
