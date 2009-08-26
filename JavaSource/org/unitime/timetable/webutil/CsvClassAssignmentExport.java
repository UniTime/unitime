/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.webutil;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.ifs.util.CSVFile;
import net.sf.cpsolver.ifs.util.CSVFile.CSVField;

/**
 * @author Tomas Muller
 */
public class CsvClassAssignmentExport {

	public static CSVFile exportCsv(User user, Collection classes, ClassAssignmentProxy proxy) {
		CSVFile file = new CSVFile();
		String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
		file.setSeparator(",");
		file.setQuotationMark("\"");
		file.setHeader(new CSVField[] {
				new CSVField("COURSE"),
				new CSVField("ITYPE"),
				new CSVField("SECTION"),
				new CSVField("SUFFIX"),
				new CSVField("EXTERNAL_ID"),
				new CSVField("DATE_PATTERN"),
				new CSVField("DAY"),
				new CSVField("START_TIME"),
				new CSVField("END_TIME"),
				new CSVField("ROOM"),
				new CSVField("INSTRUCTOR"),
				new CSVField("SCHEDULE_NOTE")
			});
		
		for (Iterator i=classes.iterator();i.hasNext();) {
			Class_ clazz = (Class_)i.next();
			Vector leads = clazz.getLeadInstructors();
			StringBuffer leadsSb = new StringBuffer();
			for (Enumeration e=leads.elements();e.hasMoreElements();) {
				DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
				leadsSb.append(instructor.getName(instructorFormat));
				if (e.hasMoreElements()) leadsSb.append("\n");
			}
            String divSec = clazz.getDivSecNumber();
            if (divSec==null)
                divSec = (new DecimalFormat("000")).format(clazz.getSectionNumber().intValue())+"-001";
			Assignment assignment = null;
			try {
				assignment = proxy.getAssignment(clazz);
			} catch (Exception e) {
				Debug.error(e);
			}
			if (assignment!=null) {
				Placement placement = assignment.getPlacement();
				file.addLine(new CSVField[] {
						new CSVField(clazz.getCourseName()),
						new CSVField(clazz.getItypeDesc()),
						new CSVField(clazz.getSectionNumber()),
						new CSVField(clazz.getSchedulingSubpart().getSchedulingSubpartSuffix()),
						new CSVField(divSec),
						new CSVField(clazz.effectiveDatePattern().getName()),
						new CSVField(placement.getTimeLocation().getDayHeader()),
						new CSVField(placement.getTimeLocation().getStartTimeHeader()),
						new CSVField(placement.getTimeLocation().getEndTimeHeader()),
						new CSVField(placement.getRoomName(",")),
						new CSVField(leadsSb),
						new CSVField(clazz.getSchedulePrintNote()==null?"":clazz.getSchedulePrintNote())
				});
			} else {
                int arrHrs = Math.round(clazz.getSchedulingSubpart().getMinutesPerWk().intValue()/50f);
				file.addLine(new CSVField[] {
						new CSVField(clazz.getCourseName()),
						new CSVField(clazz.getItypeDesc()),
						new CSVField(clazz.getSectionNumber()),
						new CSVField(clazz.getSchedulingSubpart().getSchedulingSubpartSuffix()),
						new CSVField(divSec),
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

    public static CSVFile exportCsv2(User user, Collection classes, ClassAssignmentProxy proxy) {
        CSVFile file = new CSVFile();
        String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
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
            Class_ clazz = (Class_)i.next();
            Vector leads = clazz.getLeadInstructors();
            StringBuffer lastNameSb = new StringBuffer();
            StringBuffer firstNameSb = new StringBuffer();
            StringBuffer midNameSb = new StringBuffer();
            StringBuffer iniSb = new StringBuffer();
            StringBuffer rankSb = new StringBuffer();
            for (Enumeration e=leads.elements();e.hasMoreElements();) {
                DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
                lastNameSb.append(instructor.getLastName()==null?"":instructor.getLastName().trim().substring(0,1)+instructor.getLastName().trim().substring(1).toLowerCase());
                firstNameSb.append(instructor.getFirstName()==null?"":instructor.getFirstName().trim().substring(0,1)+instructor.getFirstName().trim().substring(1).toLowerCase());
                midNameSb.append(instructor.getMiddleName()==null?"":instructor.getMiddleName().trim());
                iniSb.append(instructor.getFirstName()==null || instructor.getFirstName().length()==0?"":instructor.getFirstName().substring(0,1));
                iniSb.append(instructor.getMiddleName()==null || instructor.getMiddleName().length()==0?"":" "+instructor.getMiddleName().substring(0,1));
                rankSb.append(instructor.getPositionType().getLabel());
                if (e.hasMoreElements()) {
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
                    if (clazz.getClassLimit(proxy,co)>0) {
                        if (crossListSb.length()>0) crossListSb.append("\n");
                        crossListSb.append(co.getCourseName());
                    }
                }
            }
            String divSec = clazz.getDivSecNumber();
            if (divSec==null)
                divSec = (new DecimalFormat("000")).format(clazz.getSectionNumber().intValue())+"-001";
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
                    for (Enumeration e=placement.getRoomLocations().elements();e.hasMoreElements();) {
                        RoomLocation r = (RoomLocation)e.nextElement();
                        String room = (placement.getRoomLocation()==null?"":placement.getRoomLocation().getName());
                        bldgSb.append(room.substring(0,room.lastIndexOf(' ')));
                        roomSb.append(room.substring(room.lastIndexOf(' ')+1));
                        if (e.hasMoreElements()) {
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
                        new CSVField(divSec.substring(1,3)+divSec.substring(5,7)),
                        new CSVField(titleSb),
                        new CSVField(clazz.effectiveDatePattern().getName()),
                        new CSVField(placement.getTimeLocation().getDayHeader()),
                        new CSVField(placement.getTimeLocation().getStartTimeHeader()+" - "+placement.getTimeLocation().getEndTimeHeader()),
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
                        new CSVField(divSec.substring(1,3)+divSec.substring(5,7)),
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
