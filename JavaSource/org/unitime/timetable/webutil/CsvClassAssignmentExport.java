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
package org.unitime.timetable.webutil;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.solver.ClassAssignmentProxy;


/**
 * @author Tomas Muller
 */
public class CsvClassAssignmentExport {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	public static CSVFile exportCsv(UserContext user, Collection classes, ClassAssignmentProxy proxy) {
		CSVFile file = new CSVFile();
		String instructorFormat = user.getProperty(UserProperty.NameFormat);
		file.setSeparator(",");
		file.setQuotationMark("\"");
		file.setHeader(new CSVField[] {
				new CSVField("COURSE"),
				new CSVField("ITYPE"),
				new CSVField("SECTION"),
				new CSVField("SUFFIX"),
				new CSVField("EXTERNAL_ID"),
				new CSVField("ENROLLMENT"),
				new CSVField("LIMIT"),
				new CSVField("DATE_PATTERN"),
				new CSVField("DAY"),
				new CSVField("START_TIME"),
				new CSVField("END_TIME"),
				new CSVField("ROOM"),
				new CSVField("INSTRUCTOR"),
				new CSVField("SCHEDULE_NOTE")
			});
		
		for (Iterator i=classes.iterator();i.hasNext();) {
			Object[] o = (Object[])i.next(); Class_ clazz = (Class_)o[0]; CourseOffering co = (CourseOffering)o[1];
			StringBuffer leadsSb = new StringBuffer();
			if (clazz.isDisplayInstructor()) 
			for (ClassInstructor ci: clazz.getClassInstructors()) {
				if (!leadsSb.toString().isEmpty())
					leadsSb.append("\n");
				DepartmentalInstructor instructor = ci.getInstructor();
				leadsSb.append(instructor.getName(instructorFormat));
			}
            String divSec = clazz.getClassSuffix(co);
			Assignment assignment = null;
			try {
				assignment = proxy.getAssignment(clazz);
			} catch (Exception e) {
				Debug.error(e);
			}
			if (assignment!=null) {
				Placement placement = assignment.getPlacement();
				file.addLine(new CSVField[] {
						new CSVField(co.getCourseName()),
						new CSVField(clazz.getItypeDesc()),
						new CSVField(clazz.getSectionNumber()),
						new CSVField(clazz.getSchedulingSubpart().getSchedulingSubpartSuffix()),
						new CSVField(divSec),
						new CSVField(clazz.getEnrollment()),
						new CSVField(clazz.getClassLimit(proxy)),
						new CSVField(assignment.getDatePattern().getName()),
						new CSVField(placement.getTimeLocation().getDayHeader()),
						new CSVField(placement.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm())),
						new CSVField(placement.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm())),
						new CSVField(placement.getRoomName(",")),
						new CSVField(leadsSb),
						new CSVField(clazz.getSchedulePrintNote()==null?"":clazz.getSchedulePrintNote())
				});
			} else {
                int arrHrs = Math.round(clazz.getSchedulingSubpart().getMinutesPerWk().intValue()/50f);
				file.addLine(new CSVField[] {
						new CSVField(co.getCourseName()),
						new CSVField(clazz.getItypeDesc()),
						new CSVField(clazz.getSectionNumber()),
						new CSVField(clazz.getSchedulingSubpart().getSchedulingSubpartSuffix()),
						new CSVField(divSec),
						new CSVField(clazz.getEnrollment()),
						new CSVField(clazz.getClassLimit(proxy)),
						new CSVField(clazz.effectiveDatePattern().getName()),
						new CSVField("Arr "+(arrHrs<=0?"":arrHrs+" ")+"Hrs"),
						new CSVField(""),
						new CSVField(""),
						new CSVField(""),
						new CSVField(leadsSb),
						new CSVField(clazz.getSchedulePrintNote()==null?"":clazz.getSchedulePrintNote())
				});
			}
		}
		return file;
	}

    public static CSVFile exportCsv2(UserContext user, Collection classes, ClassAssignmentProxy proxy) {
        CSVFile file = new CSVFile();
        file.setSeparator(",");
        file.setQuotationMark("\"");
        file.setHeader(new CSVField[] {
                new CSVField("ID"),
                new CSVField("COURSE"),
                new CSVField("DIVSEC"),
                new CSVField("TITLEOFFHR"),
                new CSVField("DATE_PATTERN"),
                new CSVField("DAYS"),
                new CSVField("TIME"),
                new CSVField("BUILDING"),
                new CSVField("ROOM"),
                new CSVField("LASTNAME"),
                new CSVField("FIRSTNAME"),
                new CSVField("MIDINITIAL"),
                new CSVField("INITIALS"),
                new CSVField("RANK"),
                new CSVField("NOTES"),
                new CSVField("SUBJECT"),
                new CSVField("INSTR_TYPE"),
                new CSVField("CROSS_LIST")
            });
        
        int idx = 1;
        for (Iterator i=classes.iterator();i.hasNext();) {
        	Object[] o = (Object[])i.next(); Class_ clazz = (Class_)o[0]; CourseOffering course = (CourseOffering)o[1];
        	if (!course.isIsControl()) continue;
            List<DepartmentalInstructor> leads = clazz.getLeadInstructors();
            StringBuffer lastNameSb = new StringBuffer();
            StringBuffer firstNameSb = new StringBuffer();
            StringBuffer midNameSb = new StringBuffer();
            StringBuffer iniSb = new StringBuffer();
            StringBuffer rankSb = new StringBuffer();
            for (Iterator<DepartmentalInstructor> e=leads.iterator();e.hasNext();) {
                DepartmentalInstructor instructor = (DepartmentalInstructor)e.next();
                lastNameSb.append(instructor.getLastName()==null?"":instructor.getLastName().trim().substring(0,1)+instructor.getLastName().trim().substring(1).toLowerCase());
                firstNameSb.append(instructor.getFirstName()==null?"":instructor.getFirstName().trim().substring(0,1)+instructor.getFirstName().trim().substring(1).toLowerCase());
                midNameSb.append(instructor.getMiddleName()==null?"":instructor.getMiddleName().trim());
                iniSb.append(instructor.getFirstName()==null || instructor.getFirstName().length()==0?"":instructor.getFirstName().substring(0,1));
                iniSb.append(instructor.getMiddleName()==null || instructor.getMiddleName().length()==0?"":" "+instructor.getMiddleName().substring(0,1));
                rankSb.append(instructor.getPositionType().getLabel());
                if (e.hasNext()) {
                    lastNameSb.append("\n");
                    firstNameSb.append("\n");
                    midNameSb.append("\n");
                    iniSb.append("\n");
                    rankSb.append("\n");
                }
            }
            StringBuffer noteSb = new StringBuffer();
            if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getScheduleBookNote()!=null)
                noteSb.append(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getScheduleBookNote());
            if (clazz.getSchedulePrintNote()!=null) {
                if (noteSb.length()>0) noteSb.append("\n");
                noteSb.append(clazz.getSchedulePrintNote());
            }
            if (clazz.getNotes()!=null) {
                if (noteSb.length()>0) noteSb.append("\n");
                noteSb.append(clazz.getNotes());
            }
            StringBuffer titleSb = new StringBuffer();
            if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getTitle()!=null)
                titleSb.append(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getTitle());
            StringBuffer crossListSb = new StringBuffer();
            if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().size()>1) {
                for (Iterator j=clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().iterator();j.hasNext();) {
                    CourseOffering co = (CourseOffering)j.next();
                    /*
                    if (clazz.getClassLimit(proxy,co)>0) {
                        if (crossListSb.length()>0) crossListSb.append("\n");
                        crossListSb.append(co.getCourseName());
                    }
                    */
                    crossListSb.append(co.getCourseName());
                }
            }
            String divSec = clazz.getClassSuffix(course);
            Assignment assignment = null;
            try {
                assignment = proxy.getAssignment(clazz);
            } catch (Exception e) {
                Debug.error(e);
            }
            if (assignment!=null) {
                Placement placement = assignment.getPlacement();
                StringBuffer roomSb = new StringBuffer();
                StringBuffer bldgSb = new StringBuffer();
                if (placement.isMultiRoom()) {
                    for (Iterator<RoomLocation> e=placement.getRoomLocations().iterator();e.hasNext();) {
                        RoomLocation r = e.next();
                        String room = r.getName();
                        bldgSb.append(room.substring(0,room.lastIndexOf(' ')));
                        roomSb.append(room.substring(room.lastIndexOf(' ')+1));
                        if (e.hasNext()) {
                            roomSb.append('\n');
                            bldgSb.append('\n');
                        }
                    }
                } else {
                    String room = (placement.getRoomLocation()==null?"":placement.getRoomLocation().getName());
                    bldgSb.append(room.substring(0,room.lastIndexOf(' ')));
                    roomSb.append(room.substring(room.lastIndexOf(' ')+1));
                }
                file.addLine(new CSVField[] {
                        new CSVField(idx++),
                        new CSVField(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getCourseNbr()),
                        new CSVField(divSec),
                        new CSVField(titleSb),
                        new CSVField(assignment.getDatePattern().getName()),
                        new CSVField(placement.getTimeLocation().getDayHeader()),
                        new CSVField(placement.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm())+" - "+placement.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm())),
                        new CSVField(bldgSb),
                        new CSVField(roomSb),
                        new CSVField(lastNameSb),
                        new CSVField(firstNameSb),
                        new CSVField(midNameSb),
                        new CSVField(iniSb),
                        new CSVField(rankSb),
                        new CSVField(noteSb),
                        new CSVField(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectAreaAbbv()),
                        new CSVField(clazz.getSchedulingSubpart().getItypeDesc().trim().toUpperCase()),
                        new CSVField(crossListSb)
                });
            } else {
                int arrHrs = Math.round(clazz.getSchedulingSubpart().getMinutesPerWk().intValue()/50f);
                file.addLine(new CSVField[] {
                        new CSVField(idx++),
                        new CSVField(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getCourseNbr()),
                        new CSVField(divSec),
                        new CSVField(titleSb),
                        new CSVField(clazz.effectiveDatePattern().getName()),
                        new CSVField(""),
                        new CSVField("Arr "+(arrHrs<=0?"":arrHrs+" ")+"Hrs"),
                        new CSVField(""),
                        new CSVField(""),
                        new CSVField(lastNameSb),
                        new CSVField(firstNameSb),
                        new CSVField(midNameSb),
                        new CSVField(iniSb),
                        new CSVField(rankSb),
                        new CSVField(noteSb),
                        new CSVField(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectAreaAbbv()),
                        new CSVField(clazz.getSchedulingSubpart().getItypeDesc().trim().toUpperCase()),
                        new CSVField(crossListSb)
                });
            }
        }
        return file;
    }
}
