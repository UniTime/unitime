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

import java.awt.Image;
import java.util.Comparator;
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
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;


/**
 * @author Heston Fernandes, Tomas Muller, Zuzana Mullerova
 */
public class InstructorListBuilder {
    
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
    public String htmlTableForInstructor(SessionContext context, String deptId, int order, String backId) throws Exception {
        
		boolean timeVertical = RequiredTimeTable.getTimeGridVertical(context.getUser());
		boolean gridAsText = RequiredTimeTable.getTimeGridAsText(context.getUser());
		String timeGridSize = RequiredTimeTable.getTimeGridSize(context.getUser());

		// Loop through Instructor class
		List list = null;
		if (deptId.equals(Constants.ALL_OPTION_VALUE))
		    list = DepartmentalInstructor.findInstructorsForSession(context.getUser().getCurrentAcademicSessionId());
		else
		    list = DepartmentalInstructor.findInstructorsForDepartment(Long.valueOf(deptId));

		if (list==null || list.size() == 0) {		    
			return null;
		}  else {
			boolean hasCoursePrefs = false;
			boolean hasTeachPref = false;
			boolean hasMaxLoad = false;
			TreeSet<InstructorAttributeType> attributeTypes = new TreeSet<InstructorAttributeType>(new Comparator<InstructorAttributeType>() {
				@Override
				public int compare(InstructorAttributeType o1, InstructorAttributeType o2) {
					return o1.getReference().compareTo(o2.getReference());
				}
			});
			for (Iterator i = list.iterator(); i.hasNext();) {
				DepartmentalInstructor di = (DepartmentalInstructor)i.next();
				if (!di.getPreferences(InstructorCoursePref.class).isEmpty()) hasCoursePrefs = true;
				if (di.getMaxLoad() != null && di.getMaxLoad() > 0f) hasMaxLoad = true;
				if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) hasTeachPref = true;
				for (InstructorAttribute at: di.getAttributes())
					if (at.getType() != null)
						attributeTypes.add(at.getType());
			}
			String[] fixedHeaders1 = new String[] {
					MSG.columnExternalId(),
					MSG.columnInstructorName(),
					MSG.columnInstructorPosition(),
					MSG.columnInstructorNote(),
					MSG.columnPreferences()+"<BR>"+MSG.columnTimePref(),
					"<BR>"+MSG.columnRoomPref(),
					"<BR>"+MSG.columnDistributionPref()};
			String[] fixedHeaders2 = new String[] {
					MSG.columnInstructorClassAssignments(),
					MSG.columnInstructorExamAssignments(),
					MSG.columnInstructorIgnoreTooFar()};
			
			String[] headers = new String[fixedHeaders1.length + (hasCoursePrefs ? 1 : 0) + (hasTeachPref ? 1 : 0) + (hasMaxLoad ? 1 : 0) + attributeTypes.size() + fixedHeaders2.length];
			String[] aligns = new String[headers.length];
			boolean[] asc = new boolean[headers.length];
			int idx = 0;
			for (String h: fixedHeaders1) {
				headers[idx] = h;
				aligns[idx] = "left";
				asc[idx] = true;
				idx++;
			}
			if (hasCoursePrefs) {
				headers[idx] = "<BR>" + MSG.columnCoursePref();
				aligns[idx] = "left";
				asc[idx] = true;
				idx++;
			}
			if (hasTeachPref) {
				headers[idx] = MSG.columnTeachingPreference();
				aligns[idx] = "left";
				asc[idx] = true;
				idx++;
			}
			if (hasMaxLoad) {
				headers[idx] = MSG.columnMaxTeachingLoad();
				aligns[idx] = "left";
				asc[idx] = true;
				idx++;
			}
			for (InstructorAttributeType at: attributeTypes) {
				headers[idx] = at.getReference();
				aligns[idx] = "left";
				asc[idx] = true;
				idx++;
			}
			for (String h: fixedHeaders2) {
				headers[idx] = h;
				aligns[idx] = "left";
				asc[idx] = true;
				idx++;
			}
			
			// Create new table
			WebTable webTable = new WebTable(headers.length, "", "instructorList.do?order=%%&deptId=" + deptId, headers, aligns, asc);
			webTable.setRowStyle("white-space:nowrap;");
			webTable.enableHR("#9CB0CE");

			
			String instructorNameFormat = UserProperty.NameFormat.get(context.getUser());
			String instructorSortOrder = UserProperty.SortNames.get(context.getUser());
			
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				DepartmentalInstructor di = (DepartmentalInstructor) iter.next();
				
				String[] line = new String[headers.length];
				Comparable[] cmp = new Comparable[headers.length]; 
				idx = 0;

				// puid
				if (di.getExternalUniqueId()!=null && di.getExternalUniqueId().trim().length()>0) {
				    line[idx] = di.getExternalUniqueId();
				    cmp[idx] = di.getExternalUniqueId();
				} else {
					line[idx] = "<center><img src='images/error.png' border='0' alt='" + MSG.altNotAvailableExternalId() + "' title='"+MSG.titleInstructorExternalIdNotSupplied()+"'></center>";
					cmp[idx] = "";
				}
				idx++;
				
				// instructor name
				line[idx] = Constants.toInitialCase(di.getName(instructorNameFormat), "-".toCharArray());
				if (CommonValues.SortAsDisplayed.eq(instructorSortOrder))
					cmp[idx] = line[idx].toLowerCase();
				else
					cmp[idx] = di.nameLastNameFirst().toLowerCase();
				idx ++;
							
				// position
				if (di.getPositionType() != null) {
				    line[idx] = di.getPositionType().getLabel();
				    cmp[idx] = di.getPositionType().getSortOrder();
				} else {
					line[idx] = MSG.instructorPositionNotSpecified();
					cmp[idx] = Integer.MAX_VALUE;
				}
				idx ++;
				
				// note
				if (di.getNote() != null) {
					line[idx] = di.getNote();
					cmp[idx] = di.getNote();
				} else {
					line[idx] = "";
					cmp[idx] = "";
				}
				idx++;
				
				// time preference
				StringBuffer timePref = new StringBuffer();
				if (di.getTimePreferences() != null) {
					try {
					for (Iterator i = di.getTimePreferences().iterator(); i.hasNext();) {
						TimePref tp = (TimePref) i.next();
						RequiredTimeTable rtt = tp.getRequiredTimeTable();
						if (gridAsText) {
							timePref.append("<span onmouseover=\"showGwtInstructorAvailabilityHint(this, '" + di.getUniqueId() + "');\" onmouseout=\"hideGwtInstructorAvailabilityHint();\">" + 
										rtt.getModel().toString().replaceAll(", ","<br>") + "</span>");
						} else {
							rtt.getModel().setDefaultSelection(timeGridSize);
							timePref.append("<img border='0' onmouseover=\"showGwtInstructorAvailabilityHint(this, '" + di.getUniqueId() + "');\" onmouseout=\"hideGwtInstructorAvailabilityHint();\" " +
									"src='pattern?v=" + (timeVertical ? 1 : 0) + "&s=" + rtt.getModel().getDefaultSelection() + "&p=" + rtt.getModel().getPreferences() + "' title='"+rtt.getModel().toString()+"' >&nbsp;");
						}
					}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				line[idx] = timePref.toString();
				idx ++;
				
				// room preferences
				line[idx] = "";
	    		String x = di.getEffectivePrefHtmlForPrefType(RoomPref.class);
	    		if (x != null && !x.trim().isEmpty()) {
	    			line[idx] += x;
	    		}
				
	    		x = di.getEffectivePrefHtmlForPrefType(BuildingPref.class);
	    		if (x != null && !x.trim().isEmpty()) {
	    			if (!line[idx].isEmpty()) line[idx] += "<br>";
	    			line[idx] += x;
	    		}

	    		x = di.getEffectivePrefHtmlForPrefType(RoomFeaturePref.class);
	    		if (x != null && !x.trim().isEmpty()) {
	    			if (!line[idx].isEmpty()) line[idx] += "<br>";
	    			line[idx] += x;
	    		}

	    		x = di.getEffectivePrefHtmlForPrefType(RoomGroupPref.class);
	    		if (x != null && !x.trim().isEmpty()) {
	    			if (!line[idx].isEmpty()) line[idx] += "<br>";
	    			line[idx] += x;
	    		}
	    		idx ++;

	    		// distribution preferences
				line[idx] = di.getEffectivePrefHtmlForPrefType(DistributionPref.class);
				idx ++;
				
				// course preferences
				if (hasCoursePrefs) {
					line[idx] = di.getEffectivePrefHtmlForPrefType(InstructorCoursePref.class);
					idx ++;
				}
				
				// teaching preferences
				if (hasTeachPref) {
					PreferenceLevel pref = di.getTeachingPreference();
					if (pref == null) pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited);
					line[idx] = "<span style='font-weight:bold; color:" + PreferenceLevel.prolog2color(pref.getPrefProlog()) + ";' title='" + pref.getPrefName() + "'>" + pref.getPrefName() + "</span>";
					cmp[idx] = pref.getPrefId();
					idx ++;
				}
				
				// max load
				if (hasMaxLoad) {
					if (di.getMaxLoad() == null) {
						line[idx] = "";
						cmp[idx] = 0f;
					} else {
						line[idx] = Formats.getNumberFormat("0.##").format(di.getMaxLoad());
						cmp[idx] = di.getMaxLoad();
					}
					idx ++;
				}
				
				for (InstructorAttributeType at: attributeTypes) {
					line[idx] = "";
					for (InstructorAttribute a: di.getAttributes(at)) {
						if (!line[idx].isEmpty()) { line[idx] += "<br>"; }
						line[idx] += "<span title='" + a.getName() + "'>" + a.getCode() + "</span>";
					}
					cmp[idx] = line[idx];
					idx ++;
				}
				
				TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_LABEL)));
				classes.addAll(di.getClasses());
				line[idx] = "";
				for (Iterator i=classes.iterator();i.hasNext();) {
					ClassInstructor ci = (ClassInstructor)i.next();
					Class_ c = ci.getClassInstructing(); 
					String className = c.getClassLabel();
		    		String title = className;
		    		if (c.isCancelled())
		    			title = MSG.classNoteCancelled(c.getClassLabel());
		    		title += " ("+ci.getPercentShare()+"%"+(ci.isLead().booleanValue()?", " + MSG.titleCheckConflicts() :"")+")";
		    		if (!c.isDisplayInstructor().booleanValue()){
		    			title += " - " + MSG.titleDoNotDisplayInstructor();
		    		}
		    		if (c.isCancelled()) {
		    			line[idx] +=  "<span style='color: gray; text-decoration: line-through;" + (ci.isLead() ? "font-weight:bold;" : "") + (c.isDisplayInstructor() ? "" : "font-style:italic;") + "' title='"+title+"'>";
		    		} else if (ci.isLead().booleanValue()){
		    			line[idx] +=  "<span style='font-weight:bold;"+(c.isDisplayInstructor().booleanValue()?"":"font-style:italic;")+"' title='"+title+"'>";
		    		} else {
		    			line[idx] += "<span title='"+title+"'>";
		    		}
		    		line[idx] += className;
		    		line[idx] += "</span>";
					if (i.hasNext()) line[idx] += "<br>";
				}
				idx ++;
				
				TreeSet exams = new TreeSet(di.getExams());
				line[idx] = "";
				for (Iterator i=exams.iterator();i.hasNext();) {
				    Exam exam = (Exam)i.next();
				    if (!context.hasPermission(exam, Right.ExaminationView)) continue;
                    String examName = exam.getLabel();
                    if (exam.getExamType().getType()==ExamType.sExamTypeMidterm) {
                    	line[idx] += "<span title='"+examName+" "+MSG.titleMidtermExamination()+"'>"+examName+"</span>";
                    } else {
                    	line[idx] += "<span style='font-weight:bold;' title='"+examName+" "+MSG.titleFinalExamination()+"'>"+examName+"</span>";
                    }
                    if (i.hasNext()) line[idx] += "<br>";
				}
				idx ++;
				
				if (di.isIgnoreToFar()==null?false:di.isIgnoreToFar().booleanValue()) {
					line[idx] = "<img border='0' title='" + MSG.titleIgnoreTooFarDistances() + "' alt='true' align='absmiddle' src='images/accept.png'>";
					cmp[idx] = true;
				} else {
					line[idx] = "";
					cmp[idx] = false;
				}
				idx ++;
				
				boolean back = di.getUniqueId().toString().equals(backId);
				if (back) line[0] = "<A name=\"back\"></A>" + line[0];
				
				// Add to web table
				webTable.addLine("onClick=\"document.location='instructorDetail.do?instructorId=" + di.getUniqueId() + "&deptId=" + deptId + "';\"", line, cmp);
			}
			
			String tblData = webTable.printTable(order);
			return tblData;
		}        
    }
    
    public PdfWebTable pdfTableForInstructor(SessionContext context, String deptId, boolean canHaveImages) throws Exception {
		boolean timeVertical = RequiredTimeTable.getTimeGridVertical(context.getUser());
		boolean gridAsText = RequiredTimeTable.getTimeGridAsText(context.getUser());
		String timeGridSize = RequiredTimeTable.getTimeGridSize(context.getUser());

		// Loop through Instructor class
		List list = null;
		if (deptId.equals(Constants.ALL_OPTION_VALUE))
		    list = DepartmentalInstructor.findInstructorsForSession(context.getUser().getCurrentAcademicSessionId());
		else
		    list = DepartmentalInstructor.findInstructorsForDepartment(Long.valueOf(deptId));

		if (list==null || list.size() == 0)		    
			return null;
		
		boolean hasCoursePrefs = false;
		boolean hasTeachPref = false;
		boolean hasMaxLoad = false;
		TreeSet<InstructorAttributeType> attributeTypes = new TreeSet<InstructorAttributeType>(new Comparator<InstructorAttributeType>() {
			@Override
			public int compare(InstructorAttributeType o1, InstructorAttributeType o2) {
				return o1.getReference().compareTo(o2.getReference());
			}
		});
		for (Iterator i = list.iterator(); i.hasNext();) {
			DepartmentalInstructor di = (DepartmentalInstructor)i.next();
			if (!di.getPreferences(InstructorCoursePref.class).isEmpty()) hasCoursePrefs = true;
			if (di.getMaxLoad() != null && di.getMaxLoad() > 0f) hasMaxLoad = true;
			if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) hasTeachPref = true;
			for (InstructorAttribute at: di.getAttributes())
				if (at.getType() != null)
					attributeTypes.add(at.getType());
		}
		String[] fixedHeaders1 = new String[] {
				MSG.columnExternalId(),
				MSG.columnInstructorName(),
				MSG.columnInstructorPosition(),
				MSG.columnInstructorNote(),
				MSG.columnPreferences()+"\n"+MSG.columnTimePref(),
				"\n"+MSG.columnRoomPref(),
				"\n"+MSG.columnDistributionPref()};
		String[] fixedHeaders2 = new String[] {
				MSG.columnInstructorClassAssignmentsPDF(),
				MSG.columnInstructorExamAssignmentsPDF(),
				MSG.columnInstructorIgnoreTooFarPDF()};
		
		String[] headers = new String[fixedHeaders1.length + (hasCoursePrefs ? 1 : 0) + (hasTeachPref ? 1 : 0) + (hasMaxLoad ? 1 : 0) + attributeTypes.size() + fixedHeaders2.length];
		String[] aligns = new String[headers.length];
		boolean[] asc = new boolean[headers.length];
		int idx = 0;
		for (String h: fixedHeaders1) {
			headers[idx] = h;
			aligns[idx] = "left";
			asc[idx] = true;
			idx++;
		}
		if (hasCoursePrefs) {
			headers[idx] = "\n" + MSG.columnCoursePref();
			aligns[idx] = "left";
			asc[idx] = true;
			idx++;
		}
		if (hasTeachPref) {
			headers[idx] = MSG.columnTeachingPreferencePDF();
			aligns[idx] = "left";
			asc[idx] = true;
			idx++;
		}
		if (hasMaxLoad) {
			headers[idx] = MSG.columnMaxTeachingLoadPDF();
			aligns[idx] = "left";
			asc[idx] = true;
			idx++;
		}
		for (InstructorAttributeType at: attributeTypes) {
			headers[idx] = at.getReference();
			aligns[idx] = "left";
			asc[idx] = true;
			idx++;
		}
		for (String h: fixedHeaders2) {
			headers[idx] = h;
			aligns[idx] = "left";
			asc[idx] = true;
			idx++;
		}
		
		// Create new table
		PdfWebTable webTable = new PdfWebTable(headers.length, MSG.sectionTitleInstructorList(), null, headers, aligns, asc);

		String instructorNameFormat =  UserProperty.NameFormat.get(context.getUser());
		String instructorSortOrder = UserProperty.SortNames.get(context.getUser());
		
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			DepartmentalInstructor di = (DepartmentalInstructor) iter.next(); 

			String[] line = new String[headers.length];
			Comparable[] cmp = new Comparable[headers.length]; 
			idx = 0;

			// puid
			if (di.getExternalUniqueId()!=null && di.getExternalUniqueId().trim().length()>0) {
			    line[idx] = di.getExternalUniqueId();
			    cmp[idx] = di.getExternalUniqueId();
			} else {
				line[idx] = "@@ITALIC "+MSG.instructorExternalIdNotSpecified();
				cmp[idx] = "";
			}
			idx++;
			
			// instructor name
			line[idx] = Constants.toInitialCase(di.getName(instructorNameFormat), "-".toCharArray());
			if (CommonValues.SortAsDisplayed.eq(instructorSortOrder))
				cmp[idx] = line[idx].toLowerCase();
			else
				cmp[idx] = di.nameLastNameFirst().toLowerCase();
			idx ++;
						
			// position
			if (di.getPositionType() != null) {
			    line[idx] = di.getPositionType().getLabel();
			    cmp[idx] = di.getPositionType().getSortOrder();
			} else {
				line[idx] = "@@ITALIC " + MSG.instructorPositionNotSpecified();
				cmp[idx] = Integer.MAX_VALUE;
			}
			idx ++;
			
			// note
			if (di.getNote() != null) {
				line[idx] = di.getNote();
				cmp[idx] = di.getNote();
			} else {
				line[idx] = "";
				cmp[idx] = "";
			}
			idx++;
			
			// time preference
			StringBuffer timePref = new StringBuffer();
			if (di.getTimePreferences() != null) {
				for (Iterator i = di.getTimePreferences().iterator(); i.hasNext();) {
					TimePref tp = (TimePref) i.next();
					RequiredTimeTable rtt = tp.getRequiredTimeTable();
					if (gridAsText || !canHaveImages) {
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
			line[idx] = timePref.toString();
			idx ++;
			
			// room preferences
			line[idx] = "";
			for (Iterator i=di.effectivePreferences(RoomPref.class).iterator();i.hasNext();) {
				RoomPref rp = (RoomPref)i.next();
				if (!line[idx].isEmpty()) line[idx] += "\n";
				line[idx] += "@@COLOR " + PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()) + " " + rp.getPrefLevel().getAbbreviation()+" "+rp.getRoom().getLabel();
			}
			for (Iterator i=di.effectivePreferences(BuildingPref.class).iterator();i.hasNext();) {
				BuildingPref bp = (BuildingPref)i.next();
				if (!line[idx].isEmpty()) line[idx] += "\n";
				line[idx] += "@@COLOR " + PreferenceLevel.prolog2color(bp.getPrefLevel().getPrefProlog()) + " " + bp.getPrefLevel().getAbbreviation()+" "+bp.getBuilding().getAbbreviation();
			}
			for (Iterator i=di.effectivePreferences(RoomFeaturePref.class).iterator();i.hasNext();) {
				RoomFeaturePref rfp = (RoomFeaturePref)i.next();
				if (!line[idx].isEmpty()) line[idx] += "\n";
				line[idx] += "@@COLOR " + PreferenceLevel.prolog2color(rfp.getPrefLevel().getPrefProlog()) + " " + rfp.getPrefLevel().getAbbreviation()+" "+rfp.getRoomFeature().getLabel();
			}
			for (Iterator i=di.effectivePreferences(RoomGroupPref.class).iterator();i.hasNext();) {
				RoomGroupPref rgp = (RoomGroupPref)i.next();
				if (!line[idx].isEmpty()) line[idx] += "\n";
				line[idx] += "@@COLOR " + PreferenceLevel.prolog2color(rgp.getPrefLevel().getPrefProlog()) + " " + rgp.getPrefLevel().getAbbreviation()+" "+rgp.getRoomGroup().getName();
			}
    		idx ++;

    		// distribution preferences
    		line[idx] = "";
    		for (Iterator i=di.effectivePreferences(DistributionPref.class).iterator();i.hasNext();) {
				DistributionPref dp = (DistributionPref)i.next();
				if (!line[idx].isEmpty()) line[idx] += "\n";
				line[idx] += "@@COLOR " + PreferenceLevel.prolog2color(dp.getPrefLevel().getPrefProlog()) + " " + dp.getPrefLevel().getAbbreviation()+" "+dp.getDistributionType().getAbbreviation();
			}
			idx ++;
			
			// course preferences
			if (hasCoursePrefs) {
				line[idx] = "";
	    		for (Iterator i=di.effectivePreferences(InstructorCoursePref.class).iterator();i.hasNext();) {
	    			InstructorCoursePref dp = (InstructorCoursePref)i.next();
					if (!line[idx].isEmpty()) line[idx] += "\n";
					line[idx] += "@@COLOR " + PreferenceLevel.prolog2color(dp.getPrefLevel().getPrefProlog()) + " " + dp.getPrefLevel().getAbbreviation()+" "+dp.getCourse().getCourseName();
				}
				idx ++;
			}
			
			// teaching preferences
			if (hasTeachPref) {
				PreferenceLevel pref = di.getTeachingPreference();
				if (pref == null) pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited);
				line[idx] = "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefProlog()) + " " + pref.getPrefName();
				cmp[idx] = pref.getPrefId();
				idx ++;
			}
			
			// max load
			if (hasMaxLoad) {
				if (di.getMaxLoad() == null) {
					line[idx] = "";
					cmp[idx] = 0f;
				} else {
					line[idx] = Formats.getNumberFormat("0.##").format(di.getMaxLoad());
					cmp[idx] = di.getMaxLoad();
				}
				idx ++;
			}
			
			for (InstructorAttributeType at: attributeTypes) {
				line[idx] = "";
				for (InstructorAttribute a: di.getAttributes(at)) {
					if (!line[idx].isEmpty()) { line[idx] += "\n"; }
					line[idx] += a.getCode();
				}
				cmp[idx] = line[idx];
				idx ++;
			}
			
			TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_LABEL)));
			classes.addAll(di.getClasses());
			line[idx] = "";
			for (Iterator i=classes.iterator();i.hasNext();) {
				ClassInstructor ci = (ClassInstructor)i.next();
				Class_ c = ci.getClassInstructing(); 
				String className = c.getClassLabel();
	    		if (ci.isLead().booleanValue())
	    			line[idx] +=  "@@BOLD ";
	    		if (!c.isDisplayInstructor().booleanValue())
	    			line[idx] += "@@ITALIC ";
	    		line[idx] += className;
	    		if (!c.isDisplayInstructor().booleanValue())
	    			line[idx] += "@@END_ITALIC ";
	    		if (ci.isLead().booleanValue())
	    			line[idx] +=  "@@END_BOLD ";
				if (i.hasNext()) line[idx] += "\n";
			}
			idx ++;
			
			TreeSet exams = new TreeSet(di.getExams());
			line[idx] = "";
			for (Iterator i=exams.iterator();i.hasNext();) {
			    Exam exam = (Exam)i.next();
			    if (!context.hasPermission(exam, Right.ExaminationView)) continue;
                String examName = exam.getLabel();
                if (exam.getExamType().getType()==ExamType.sExamTypeMidterm) {
                	line[idx] += examName;
                } else {
                	line[idx] += "@@BOLD " + examName + "@@END_BOLD ";
                }
                if (i.hasNext()) line[idx] += "\n";
			}
			idx ++;
			
			if (di.isIgnoreToFar()==null?false:di.isIgnoreToFar().booleanValue()) {
				line[idx] = "@@ITALIC " + MSG.yes();
				cmp[idx] = true;
			} else {
				line[idx] = "@@ITALIC " + MSG.no();
				cmp[idx] = false;
			}
			idx ++;
			
			// Add to web table
			webTable.addLine(null, line, cmp);
		}
		
		return webTable;
    }
}
