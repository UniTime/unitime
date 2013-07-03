/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.webutil;

import java.awt.Image;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;


/**
 * @author Heston Fernandes
 */
public class InstructorListBuilder {
    
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
    public String htmlTableForInstructor(SessionContext context, String deptId, int order, String backId) throws Exception {
        
		int cols = 11;
		boolean timeVertical = RequiredTimeTable.getTimeGridVertical(context.getUser());
		boolean gridAsText = RequiredTimeTable.getTimeGridAsText(context.getUser());
		String timeGridSize = RequiredTimeTable.getTimeGridSize(context.getUser());

		// Create new table
		WebTable webTable = new WebTable(cols, "",
				"instructorList.do?order=%%&deptId=" + deptId,
				new String[] { 	MSG.columnExternalId(),
								MSG.columnInstructorName(),
								MSG.columnInstructorPosition(),
								MSG.columnInstructorNote(),
								MSG.columnPreferences()+"<BR>"+MSG.columnTimePref(),
								"<BR>"+MSG.columnRoomPref(),
								"<BR>"+MSG.columnDistributionPref(),
								MSG.columnInstructorClassAssignments(),
								MSG.columnInstructorExamAssignments(),
								MSG.columnInstructorIgnoreTooFar()}, 
				new String[] { "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
				new boolean[] { true, true, true, true, true, true, true, true, true, true});
		webTable.setRowStyle("white-space:nowrap;");
		webTable.enableHR("#9CB0CE");

		// Loop through Instructor class
		List list = null;
		if (deptId.equals(Constants.ALL_OPTION_VALUE))
		    list = DepartmentalInstructor.findInstructorsForSession(context.getUser().getCurrentAcademicSessionId());
		else
		    list = DepartmentalInstructor.findInstructorsForDepartment(Long.valueOf(deptId));

		if (list==null || list.size() == 0) {		    
			return null;
		}  else {
			String instructorNameFormat = UserProperty.NameFormat.get(context.getUser());
			String instructorSortOrder = UserProperty.SortNames.get(context.getUser());
			
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				DepartmentalInstructor di = (DepartmentalInstructor) iter.next(); 

				//puid
				String puid = "";
				if (di.getExternalUniqueId()!=null && di.getExternalUniqueId().trim().length()>0)
				    puid = di.getExternalUniqueId();
				else
				    puid = "<center><IMG src='images/Error16.jpg' border='0' alt='" + MSG.altNotAvailableExternalId()+
				    		"' title='"+MSG.titleInstructorExternalIdNotSupplied()+"'></center>";
				
				//get instructor name 
				String name = Constants.toInitialCase(di.getName(instructorNameFormat), "-".toCharArray());
				String nameOrd = di.nameLastNameFirst().toLowerCase();
				if (CommonValues.SortAsDisplayed.eq(instructorSortOrder))
				    nameOrd = name.toLowerCase();
							
				// position
				String posType = MSG.instructorPositionNotSpecified();
				if (di.getPositionType()!=null)
				    posType = di.getPositionType().getLabel();
				
				/*
				//get departments
				StringBuffer deptFte = new StringBuffer();
				deptFte.append(percentFormatter.format(di.getFte()==null?0.0:di.getFte().doubleValue()));
				*/
				
				/*
				 * The following piece of code increases response time by a large factor (minutes instead of seconds)
				 * For now just display FTE for current department instead of displaying FTE for other departments
				 */
				/*
				List all = DepartmentalInstructor.getAllForInstructor(di);
				TreeSet sortedAll = new TreeSet(all);
				for (Iterator iterInstDept = sortedAll.iterator(); iterInstDept.hasNext();) {
					DepartmentalInstructor anotherDi = (DepartmentalInstructor) iterInstDept.next();
					Department d = anotherDi.getDepartment();
					if (d != null) {
						if (deptFte.length() > 0 ) {
							deptFte.append("<br>");
						}
					    deptFte.append(d.getAbbreviation());
						if (anotherDi.getFte() != null && anotherDi.getFte().intValue() != 1) {
							deptFte.append(" (" + percentFormatter.format(anotherDi.getFte().doubleValue()) + ")");
						}
					}
				}
				*/
				
				// note
				String note = "";
				if (di.getNote()!=null)
				    note = di.getNote();

				//get room preferences
				String rmPref = "";
				
	    		String x = di.getEffectivePrefHtmlForPrefType(RoomPref.class);
	    		if (x!=null && x.trim().length()>0) {
	    			rmPref += x;
	    		}
				
	    		x = di.getEffectivePrefHtmlForPrefType(BuildingPref.class);
	    		if (x!=null && x.trim().length()>0) {
	    			if (rmPref.length()>0) rmPref += "<br>";
	    			rmPref += x;
	    		}

	    		x = di.getEffectivePrefHtmlForPrefType(RoomFeaturePref.class);
	    		if (x!=null && x.trim().length()>0) {
	    			if (rmPref.length()>0) rmPref += "<br>";
	    			rmPref += x;
	    		}

	    		x = di.getEffectivePrefHtmlForPrefType(RoomGroupPref.class);
	    		if (x!=null && x.trim().length()>0) {
	    			if (rmPref.length()>0) rmPref += "<br>";
	    			rmPref += x;
	    		}

				//get time preference
				StringBuffer timePref = new StringBuffer();
				if (di.getTimePreferences() != null) {
					try {
					for (Iterator i = di.getTimePreferences().iterator(); i.hasNext();) {
						TimePref tp = (TimePref) i.next();
						RequiredTimeTable rtt = tp.getRequiredTimeTable();
						if (gridAsText) {
							timePref.append(rtt.getModel().toString().replaceAll(", ","<br>"));
						} else {
							rtt.getModel().setDefaultSelection(timeGridSize);
							timePref.append("<img border='0' src='pattern?v=" + (timeVertical ? 1 : 0) + "&s=" + rtt.getModel().getDefaultSelection() + "&p=" + rtt.getModel().getPreferences() + "' title='"+rtt.getModel().toString()+"' >&nbsp;");
						}
					}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				String distPref = di.getEffectivePrefHtmlForPrefType(DistributionPref.class);
				
				TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_LABEL)));
				classes.addAll(di.getClasses());
				String classesStr = "";
				for (Iterator i=classes.iterator();i.hasNext();) {
					ClassInstructor ci = (ClassInstructor)i.next();
					Class_ c = ci.getClassInstructing(); 
					String className = c.getClassLabel();
		    		String title = className;
		    		title += " ("+ci.getPercentShare()+"%"+(ci.isLead().booleanValue()?", " + MSG.titleCheckConflicts() :"")+")";
		    		if (!c.isDisplayInstructor().booleanValue()){
		    			title += " - " + MSG.titleDoNotDisplayInstructor();
		    		}
		    		if (ci.isLead().booleanValue()){
		    			classesStr +=  "<span style='font-weight:bold;"+(c.isDisplayInstructor().booleanValue()?"":"font-style:italic;")+"' title='"+title+"'>";
		    		} else {
		    			classesStr += "<span title='"+title+"'>";
		    		}
		    		classesStr += className;
		    		classesStr += "</span>";
					if (i.hasNext()) classesStr += "<br>";
				}
				
				TreeSet exams = new TreeSet(di.getExams());
				String examsStr = "";
				for (Iterator i=exams.iterator();i.hasNext();) {
				    Exam exam = (Exam)i.next();
                    String examName = exam.getLabel();
                    if (exam.getExamType().getType()==ExamType.sExamTypeMidterm) {
                        examsStr += "<span title='"+examName+" "+MSG.titleMidtermExamination()+"'>"+examName+"</span>";
                    } else {
                        examsStr += "<span style='font-weight:bold;' title='"+examName+" "+MSG.titleFinalExamination()+"'>"+examName+"</span>";
                    }
                    if (i.hasNext()) examsStr += "<br>";
				}
				
				boolean back = di.getUniqueId().toString().equals(backId);
                boolean itf = (di.isIgnoreToFar()==null?false:di.isIgnoreToFar().booleanValue());

				// Add to web table
				webTable.addLine(
						"onClick=\"document.location='instructorDetail.do?instructorId="
								+ di.getUniqueId() + "&deptId=" + deptId
								+ "';\"", 
						new String[] { 
							(back?"<A name=\"back\"></A>":"")+
							puid, 	        
							name, 
							putSpace(posType),
							putSpace(note),
							putSpace(timePref.toString()), 
					        putSpace(rmPref),
					        putSpace(distPref),
					        putSpace(classesStr),
					        putSpace(examsStr),
                            (itf?"<IMG border='0' title='"+MSG.titleIgnoreTooFarDistances()+"' alt='true' align='absmiddle' src='images/tick.gif'>":"&nbsp;")}, 
						new Comparable[] { puid, nameOrd, posType, null, null, null, null, null, null, new Integer(itf?0:1) });

			}
			
			String tblData = webTable.printTable(order);
			return tblData;
		}        
    }
    
    public PdfWebTable pdfTableForInstructor(SessionContext context, String deptId) throws Exception {
		int cols = 10;
		boolean timeVertical = RequiredTimeTable.getTimeGridVertical(context.getUser());
		boolean gridAsText = RequiredTimeTable.getTimeGridAsText(context.getUser());
		String timeGridSize = RequiredTimeTable.getTimeGridSize(context.getUser());

		// Create new table
		PdfWebTable webTable = new PdfWebTable(cols, 
				MSG.sectionTitleInstructorList(),
				null,
				new String[] { 	MSG.columnExternalId(),
						MSG.columnInstructorName(),
						MSG.columnInstructorPosition(),
						MSG.columnInstructorNote(),
						MSG.columnPreferences()+"\n"+MSG.columnTimePref(),
						"\n"+MSG.columnRoomPref(),
						"\n"+MSG.columnDistributionPref(),
						MSG.columnInstructorClassAssignmentsPDF(),
						MSG.columnInstructorExamAssignmentsPDF(),
						MSG.columnInstructorIgnoreTooFarPDF()},
				new String[] { "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
				new boolean[] { true, true, true, true, true, true, true, true, true, true});

		// Loop through Instructor class
		List list = null;
		if (deptId.equals(Constants.ALL_OPTION_VALUE))
		    list = DepartmentalInstructor.findInstructorsForSession(context.getUser().getCurrentAcademicSessionId());
		else
		    list = DepartmentalInstructor.findInstructorsForDepartment(Long.valueOf(deptId));

		if (list==null || list.size() == 0)		    
			return null;

		String instructorNameFormat =  UserProperty.NameFormat.get(context.getUser());
		String instructorSortOrder = UserProperty.SortNames.get(context.getUser());
		
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			DepartmentalInstructor di = (DepartmentalInstructor) iter.next(); 

			//puid
			String puid = "";
			if (di.getExternalUniqueId()!=null && di.getExternalUniqueId().trim().length()>0)
			    puid = di.getExternalUniqueId();
			else
			    puid = "@@ITALIC "+MSG.instructorExternalIdNotSpecified();
			
			//get instructor name 
			String name = Constants.toInitialCase(di.getName(instructorNameFormat), "-".toCharArray());
			String nameOrd = di.nameLastNameFirst().toLowerCase();
			if (CommonValues.SortAsDisplayed.eq(instructorSortOrder))
			    nameOrd = name.toLowerCase();
						
			// position
			String posType = "@@ITALIC "+MSG.instructorPositionNotSpecified();
			if (di.getPositionType()!=null)
			    posType = di.getPositionType().getLabel();
			
			/*
			//get departments
			StringBuffer deptFte = new StringBuffer();
			deptFte.append(percentFormatter.format(di.getFte()==null?0.0:di.getFte().doubleValue()));
			*/				
			
			// note
			String note = "";
			if (di.getNote()!=null)
			    note = di.getNote();

			//get room preferences
			String rmPref = "";
			
			for (Iterator i=di.effectivePreferences(RoomPref.class).iterator();i.hasNext();) {
				RoomPref rp = (RoomPref)i.next();
				if (rmPref.length()>0) rmPref += "\n";
				rmPref += "@@COLOR " + PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()) + " " + PreferenceLevel.prolog2abbv(rp.getPrefLevel().getPrefProlog())+" "+rp.getRoom().getLabel();
			}
			
			for (Iterator i=di.effectivePreferences(BuildingPref.class).iterator();i.hasNext();) {
				BuildingPref bp = (BuildingPref)i.next();
				if (rmPref.length()>0) rmPref += "\n";
				rmPref += "@@COLOR " + PreferenceLevel.prolog2color(bp.getPrefLevel().getPrefProlog()) + " " + PreferenceLevel.prolog2abbv(bp.getPrefLevel().getPrefProlog())+" "+bp.getBuilding().getAbbreviation();
			}
			
			for (Iterator i=di.effectivePreferences(RoomFeaturePref.class).iterator();i.hasNext();) {
				RoomFeaturePref rfp = (RoomFeaturePref)i.next();
				if (rmPref.length()>0) rmPref += "\n";
				rmPref += "@@COLOR " + PreferenceLevel.prolog2color(rfp.getPrefLevel().getPrefProlog()) + " " + PreferenceLevel.prolog2abbv(rfp.getPrefLevel().getPrefProlog())+" "+rfp.getRoomFeature().getLabel();
			}

			for (Iterator i=di.effectivePreferences(RoomGroupPref.class).iterator();i.hasNext();) {
				RoomGroupPref rgp = (RoomGroupPref)i.next();
				if (rmPref.length()>0) rmPref += "\n";
				rmPref += "@@COLOR " + PreferenceLevel.prolog2color(rgp.getPrefLevel().getPrefProlog()) + " " + PreferenceLevel.prolog2abbv(rgp.getPrefLevel().getPrefProlog())+" "+rgp.getRoomGroup().getName();
			}

			//get time preference
			StringBuffer timePref = new StringBuffer();
			if (di.getTimePreferences() != null) {
				for (Iterator i = di.getTimePreferences().iterator(); i
						.hasNext();) {
					TimePref tp = (TimePref) i.next();
					RequiredTimeTable rtt = tp.getRequiredTimeTable();
					if (gridAsText) {
						timePref.append(rtt.getModel().toString().replaceAll(", ","\n"));
					} else {
						rtt.getModel().setDefaultSelection(timeGridSize);
						Image image = rtt.createBufferedImage(timeVertical);
						if (image != null) {
							webTable.addImage(tp.getUniqueId().toString(), image);
							timePref.append("@@IMAGE "+tp.getUniqueId().toString()+" ");
						} else
							timePref.append(rtt.getModel().toString().replaceAll(", ","\n"));
						if (i.hasNext()) timePref.append("\n");
					}
				}
			}
			
			String distPref = "";
			for (Iterator i=di.effectivePreferences(DistributionPref.class).iterator();i.hasNext();) {
				DistributionPref dp = (DistributionPref)i.next();
				if (distPref.length()>0) distPref += "\n";
				distPref += "@@COLOR " + PreferenceLevel.prolog2color(dp.getPrefLevel().getPrefProlog()) + " " + PreferenceLevel.prolog2abbv(dp.getPrefLevel().getPrefProlog())+" "+dp.getDistributionType().getAbbreviation();
			}
			
			TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_LABEL)));
			classes.addAll(di.getClasses());
			String classesStr = "";
			for (Iterator i=classes.iterator();i.hasNext();) {
				ClassInstructor ci = (ClassInstructor)i.next();
				Class_ c = ci.getClassInstructing(); 
				String className = c.getClassLabel();
	    		if (ci.isLead().booleanValue())
	    			classesStr +=  "@@BOLD ";
	    		if (!c.isDisplayInstructor().booleanValue())
	    			classesStr += "@@ITALIC ";
	    		classesStr += className;
	    		if (!c.isDisplayInstructor().booleanValue())
	    			classesStr += "@@END_ITALIC ";
	    		if (ci.isLead().booleanValue())
	    			classesStr +=  "@@END_BOLD ";
				if (i.hasNext()) classesStr += "\n";
			}
			
            TreeSet exams = new TreeSet(di.getExams());
            String examsStr = "";
            for (Iterator i=exams.iterator();i.hasNext();) {
                Exam exam = (Exam)i.next();
                String examName = exam.getLabel();
                if (exam.getExamType().getType()==ExamType.sExamTypeMidterm) {
                    examsStr += examName;
                } else {
                    examsStr += "@@BOLD "+examName+"@@END_BOLD ";
                }
                if (i.hasNext()) examsStr += "\n";
            }


			// Add to web table
			webTable.addLine(
					null, 
					new String[] { 
						puid, 	        
						name, 
						posType,
						note,
						timePref.toString(), 
				        rmPref,
				        distPref,
				        classesStr,
				        examsStr,
				        di.isIgnoreToFar() != null && di.isIgnoreToFar() ? "@@ITALIC " + MSG.yes() : "@@ITALIC " + MSG.no()}, 
					new Comparable[] { puid, nameOrd, posType, null, null, null, null, null, null, di.isIgnoreToFar() });

		}
		
		return webTable;
    }
    
    /**
     * Puts a space &nbsp; if string is of 0 length
     * @param string
     * @return
     */
    private String putSpace(String str) {
        if (str==null || str.trim().length()==0)
            return "&nbsp;";
        
        return str;
    }
    
}
