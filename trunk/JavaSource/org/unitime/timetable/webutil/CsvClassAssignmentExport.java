/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
				new CSVField("DIV-SEC"),
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
                new CSVField("CLASS"),
                new CSVField("DIV-SEC"),
                new CSVField("DATE_PATTERN"),
                new CSVField("DAY"),
                new CSVField("TIME"),
                new CSVField("ROOM"),
                new CSVField("INSTRUCTOR"),
                new CSVField("INSTRUCTOR_RANK"),
                new CSVField("CROSS_LIST"),
                new CSVField("TITLE"),
                new CSVField("NOTES"),
            });
        
        for (Iterator i=classes.iterator();i.hasNext();) {
            Class_ clazz = (Class_)i.next();
            Vector leads = clazz.getLeadInstructors();
            StringBuffer leadsSb = new StringBuffer();
            StringBuffer rankSb = new StringBuffer();
            for (Enumeration e=leads.elements();e.hasMoreElements();) {
                DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
                leadsSb.append(instructor.getName(instructorFormat));
                rankSb.append(instructor.getPositionType().getLabel());
                if (e.hasMoreElements()) leadsSb.append("\n");
                if (e.hasMoreElements()) rankSb.append("\n");
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
                file.addLine(new CSVField[] {
                        new CSVField(clazz.getClassLabel()),
                        new CSVField(divSec),
                        new CSVField(clazz.effectiveDatePattern().getName()),
                        new CSVField(placement.getTimeLocation().getDayHeader()),
                        new CSVField(placement.getTimeLocation().getStartTimeHeader()+" - "+placement.getTimeLocation().getEndTimeHeader()),
                        new CSVField(placement.getRoomName("\n")),
                        new CSVField(leadsSb),
                        new CSVField(rankSb),
                        new CSVField(crossListSb),
                        new CSVField(titleSb),
                        new CSVField(noteSb)
                });
            } else {
                int arrHrs = Math.round(clazz.getSchedulingSubpart().getMinutesPerWk().intValue()/50f);
                file.addLine(new CSVField[] {
                        new CSVField(clazz.getClassLabel()),
                        new CSVField(divSec),
                        new CSVField(clazz.effectiveDatePattern().getName()),
                        new CSVField(""),
                        new CSVField("Arr "+(arrHrs<=0?"":arrHrs+" ")+"Hrs"),
                        new CSVField(""),
                        new CSVField(leadsSb),
                        new CSVField(rankSb),
                        new CSVField(crossListSb),
                        new CSVField(titleSb),
                        new CSVField(noteSb)
                });
            }
        }
        return file;
    }
}
