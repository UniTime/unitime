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
package org.unitime.timetable.webutil.csv;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.cpsolver.ifs.util.CSVFile.CSVLine;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.RequiredTimeTable;
import org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder;

/**
 * @author Tomas Muller
 */
public class CsvInstructionalOfferingTableBuilder extends WebInstructionalOfferingTableBuilder {
	protected CSVFile iFile = null;
	
	protected static String indent = "    ";
	protected static String TEXT_SEPARATOR = " ";
	protected static String LINE_SEPARATOR = "\n";

    public CsvInstructionalOfferingTableBuilder() {
        super();
    }
    
    protected String escape(String text) {
    	if (text == null) return null;
    	return text.replaceAll("\"", "\"\"");
    }
    
	public CSVField createCell(String text) {
		return new CSVField(escape(text));
	}
	
	public CSVField createCell() {
		return createCell(null);
	}
	
	public void addText(CSVField cell, String text, boolean newLine) {
		cell.set(cell.toString().isEmpty() ? escape(text) : cell.toString() + (newLine ? LINE_SEPARATOR : TEXT_SEPARATOR) + escape(text));
	}
	
	public void addText(CSVField cell, String text) {
		addText(cell, text, false);
	}
	
	public void setText(CSVField cell, String text) {
		cell.set(escape(text));
	}
	
	public int getNrColumns() {
		int ret = 0;
    	if (isShowLabel()) ret+=1;
    	if (isShowDivSec()) ret+=1;
    	if (isShowDemand()) ret+=1;
    	if (isShowProjectedDemand()) ret+=1;
    	if (isShowLimit()) ret+=1;
    	if (isShowRoomRatio()) ret+=1;
    	if (isShowManager()) ret+=1;
    	if (isShowDatePattern()) ret+=1;
    	if (isShowMinPerWk()) ret+=1;
    	if (isShowTimePattern()) ret+=1;
    	if (isShowPreferences()) ret+=PREFERENCE_COLUMN_ORDER.length+(getDisplayDistributionPrefs()?0:-1);
    	if (isShowInstructor()) ret+=1;
    	if (getDisplayTimetable() && isShowTimetable()) ret+=TIMETABLE_COLUMN_ORDER.length;
    	if (isShowTitle()) ret+=1;
    	if (isShowCredit()) ret+=1;
    	if (isShowSubpartCredit()) ret+=1;
    	if (isShowConsent()) ret+=1;
    	if (isShowSchedulePrintNote()) ret+=1;
    	if (isShowNote()) ret+=1;
    	if (isShowExam()) {
    	    if (isShowExamName()) ret+=1;
    	    if (isShowExamTimetable()) ret+=2;
    	}
    	return ret;
	}
	
    
    protected void csvBuildTableHeader(Long sessionId) {
    	//first line
    	List<CSVField> line = new ArrayList<CSVField>();
    	if (isShowLabel()) {
    		line.add(createCell(MSG.columnName()));
    	}
    	if (isShowDivSec()) {
    		line.add(createCell(MSG.columnExternalId()));
    	}   	
    	if (isShowDemand()) {
    		if (StudentClassEnrollment.sessionHasEnrollments(sessionId)) {
    			line.add(createCell(MSG.columnDemand()));
    		} else {
    			line.add(createCell(MSG.columnLastDemand()));
    		}
    	}
    	if (isShowProjectedDemand()) {
    		line.add(createCell(MSG.columnProjectedDemand()));
    	}
    	if (isShowLimit()) {
    		line.add(createCell(MSG.columnLimit()));
    	}
    	if (isShowRoomRatio()) {
    		line.add(createCell(MSG.columnRoomRatio()));
    	}
    	if (isShowManager()) {
    		line.add(createCell(MSG.columnManager()));
    	}
    	if (isShowDatePattern()) {
    		line.add(createCell(MSG.columnDatePattern()));
    	}
    	if (isShowMinPerWk()) {
    		CSVField c = createCell();
    		ClassDurationType dtype = ClassDurationType.findDefaultType(sessionId, null);
    		setText(c, dtype == null ? MSG.columnMinPerWk() : dtype.getLabel());
    		line.add(c);
    	}
    	if (isShowTimePattern()) {
    		line.add(createCell(MSG.columnTimePattern()));
    	}
    	if (isShowPreferences()) {
	    	for (int j = 0; j < PREFERENCE_COLUMN_ORDER.length + (getDisplayDistributionPrefs() ? 0 : -1); j++) {
	    		line.add(createCell(PREFERENCE_COLUMN_ORDER[j] + LINE_SEPARATOR + MSG.columnPreferences()));
	    	}
    	}
    	if (isShowInstructor()) {
    		line.add(createCell(MSG.columnInstructor()));
    	}
    	if (getDisplayTimetable() && isShowTimetable()) {
	    	for(int j = 0; j < TIMETABLE_COLUMN_ORDER.length; j++) {
	    		line.add(createCell(TIMETABLE_COLUMN_ORDER[j]));
	    	}
    	}
    	if (isShowTitle()) {
    		line.add(createCell(MSG.columnTitle()));
       }
    	if (isShowCredit()) {
    		line.add(createCell(MSG.columnOfferingCredit()));
    	}
    	if (isShowSubpartCredit()) {
    		line.add(createCell(MSG.columnSubpartCredit()));
    	}
    	if (isShowConsent()) {
    		line.add(createCell(MSG.columnConsent()));
    	}
    	if (isShowSchedulePrintNote()) {
    		line.add(createCell(MSG.columnSchedulePrintNote()));
    	}
    	if (isShowNote()) {
    		line.add(createCell(MSG.columnNote()));
    	}
    	if (isShowExam()) {
            if (isShowExamName()) {
            	line.add(createCell(MSG.columnExam() + LINE_SEPARATOR + MSG.columnExamName()));
            }
            if (isShowExamTimetable()) {
            	line.add(createCell(MSG.columnExamPeriod()));
            	line.add(createCell(MSG.columnExamRoom()));
            }
    	}
    	if (iFile.getHeader() == null && iFile.getLines() == null)
    		iFile.setHeader(line);
    	else
    		iFile.addLine(line);
   }

    private CSVField csvSubjectAndCourseInfo(InstructionalOffering io, CourseOffering co) {
    	CSVField cell = createCell(co != null ? co.getSubjectAreaAbbv() + " " + co.getCourseNbr() : "");
    	InstructionalMethod im = (co != null && co.getInstructionalOffering().getInstrOfferingConfigs().size() == 1 ? co.getInstructionalOffering().getInstrOfferingConfigs().iterator().next().getInstructionalMethod() : null);
        if (!co.isIsControl().booleanValue()) {
        	String managedAs = MSG.crossListManagedAs(io.getControllingCourseOffering().getCourseName());
        	if (im != null) {
        		if (co.getCourseType() != null) {
        			addText(cell, " (" + managedAs + ", " + co.getCourseType().getReference() + ", " + im.getReference() + ")", false);
        		} else {
        			addText(cell, " (" + managedAs + ", " + im.getReference() + ")", false);
        		}
        	} else if (co.getCourseType() != null) {
    			addText(cell, " (" + managedAs + ", " + co.getCourseType().getReference() + ")", false);
        	} else {
        		addText(cell, " (" + managedAs + ")", false);
        	}
        } else {
        	if (im != null) {
        		if (co.getCourseType() != null) {
        			addText(cell, " (" + co.getCourseType().getReference() + ", " + im.getReference() + ")", false);
        		} else {
        			addText(cell, " (" + im.getReference() + ")", false);
        		}
        	} else if (co.getCourseType() != null) {
    			addText(cell, " (" + co.getCourseType().getReference() + ")", false);
        	}
        }
        for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext(); ) {
        	CourseOffering tempCo = (org.unitime.timetable.model.CourseOffering) it.next();
            addText(cell, indent +tempCo.getSubjectAreaAbbv()+" "+tempCo.getCourseNbr() + " " + (tempCo.getCourseType() != null ? " (" + tempCo.getCourseType().getReference() + ")" : ""), true);
        }
        return cell;
    }  
    
    protected CSVField csvBuildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel){
        String label = prefGroup.toString();
        if (prefGroup instanceof Class_) {
        	Class_ aClass = (Class_) prefGroup;
        	label = aClass.getClassLabel(co);
        	if (aClass.isCancelled()) label += " (" + MSG.statusCancelled() + ")";
		}
        if (prevLabel != null && label.equals(prevLabel)){
        	label = "";
        }
        return createCell(indentSpaces + label);
    }
    
    protected CSVField csvBuildDatePatternCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	Assignment a = null;
		if (getDisplayTimetable() && isShowTimetable() && classAssignment!=null && prefGroup instanceof Class_) {
			try {
				a = classAssignment.getAssignment((Class_)prefGroup);
			} catch (Exception e) {
				Debug.error(e);
			}
    	}
    	DatePattern dp = (a != null ? a.getDatePattern() : prefGroup.effectiveDatePattern());
    	CSVField cell = createCell();
    	if (dp != null) {
			setText(cell,dp.getName());
    		if (dp.getType() == DatePattern.sTypePatternSet && isEditable) {
    			boolean hasReq = false;
    			for (Iterator i=prefGroup.effectivePreferences(DatePatternPref.class).iterator(); i.hasNext();) {
    				Preference pref = (Preference)i.next();
    				if (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())) {
    					hasReq = true; break;
    				}
    			}
    			for (Iterator i=prefGroup.effectivePreferences(DatePatternPref.class).iterator(); i.hasNext();) {
    				Preference pref = (Preference)i.next();
    				if (!hasReq || PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()))
    					addText(cell, pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(), true);
    			}
    		}
    	}
        return cell;
    }

    private CSVField csvBuildTimePatternCell(PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();
    	for (Iterator i=prefGroup.effectiveTimePatterns().iterator(); i.hasNext();) {
    		TimePattern tp = (TimePattern)i.next();
    		addText(cell, tp.getName(), true);  
    	}
        if (prefGroup instanceof Class_ && prefGroup.effectiveTimePatterns().isEmpty()) {
        	Class_ clazz = (Class_)prefGroup;
        	DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
        	Integer ah = dm.getArrangedHours(clazz.getSchedulingSubpart().getMinutesPerWk(), clazz.effectiveDatePattern());
            if (ah == null) {
                addText(cell, "Arr Hrs", true);
            } else {
                addText(cell, "Arr "+ah+" Hrs", true);
            }
        }
        return cell;
    }
    
    private CSVField csvBuildTimePrefCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
		Assignment a = null;
		if (getDisplayTimetable() && isShowTimetable() && classAssignment!=null && prefGroup instanceof Class_) {
			try {
				a = classAssignment.getAssignment((Class_)prefGroup);
			} catch (Exception e) {
				Debug.error(e);
			}
    	}
		CSVField cell = createCell();
		for (Iterator i=prefGroup.effectivePreferences(TimePref.class).iterator(); i.hasNext();) {
			TimePref tp = (TimePref)i.next();
			RequiredTimeTable rtt = tp.getRequiredTimeTable(a == null ? null : a.getTimeLocation());
			addText(cell, rtt.getModel().toString().replaceAll(", ", LINE_SEPARATOR), true);
		}
    	return cell;
    }
    
    private CSVField csvBuildPreferenceCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, Class prefType, boolean isEditable){
    	if (!isEditable) return createCell();
    	if (TimePref.class.equals(prefType)) {
    		return csvBuildTimePrefCell(classAssignment, prefGroup, isEditable);
    	} else if (DistributionPref.class.equals(prefType)) {
        	CSVField cell = createCell();
        	for (Iterator i=prefGroup.effectivePreferences(prefType).iterator();i.hasNext();) {
        		DistributionPref pref = (DistributionPref)i.next();
        		addText(cell, pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(true, true, " (", ", ",")").replaceAll("&lt;","<").replaceAll("&gt;",">"), true);
        	}
    		return cell ;
    	} else {
        	CSVField cell = createCell();
        	for (Iterator i=prefGroup.effectivePreferences(prefType).iterator();i.hasNext();) {
        		Preference pref = (Preference)i.next();
        		addText(cell, pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(), true);
        	}
    		return cell ;
    	}
    	
    }
    
    private CSVField csvBuildPreferenceCell(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, Class[] prefTypes, boolean isEditable){
    	if (!isEditable) return createCell();
    	CSVField cell = createCell();
    	boolean noRoomPrefs = false;
    	if (prefGroup instanceof Class_ && ((Class_)prefGroup).getNbrRooms().intValue()==0) {
    		 noRoomPrefs = true;
    	}
        if (prefGroup instanceof SchedulingSubpart && ((SchedulingSubpart)prefGroup).getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue())
            noRoomPrefs = true;
    	for (int i=0;i<prefTypes.length;i++) {
    		Class prefType = prefTypes[i];
    		if (noRoomPrefs) {
    			if (//prefType.equals(RoomPref.class) || 
    				prefType.equals(RoomGroupPref.class) || 
    				prefType.equals(RoomFeaturePref.class) || 
    				prefType.equals(BuildingPref.class))
    				continue;
    		}
    		for (Iterator j=prefGroup.effectivePreferences(prefType).iterator();j.hasNext();) {
    			Preference pref = (Preference)j.next();
    			addText(cell, pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(), true);
    		}
    	}
    	if (noRoomPrefs && cell.toString().isEmpty())
    		addText(cell, "N/A", true);
		return cell;
    }

    private CSVField csvBuildPrefGroupDemand(PreferenceGroup prefGroup, boolean isEditable){
    	if (prefGroup instanceof Class_) {
			Class_ c = (Class_) prefGroup;
			if (StudentClassEnrollment.sessionHasEnrollments(c.getSessionId())){
				CSVField tc = createCell();
				if (c.getEnrollment() != null){
					addText(tc, c.getEnrollment().toString());
				} else {
					addText(tc, "0");
				}
				return (tc);
			}
		}
    	return createCell();
    }
    
    private CSVField csvBuildPrefGroupProjectedDemand(PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();
    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_)prefGroup;
    		SectioningInfo i = c.getSectioningInfo();
    		if (i != null && i.getNbrExpectedStudents() != null) {
    			addText(cell, String.valueOf(Math.round(Math.max(0.0, c.getEnrollment() + i.getNbrExpectedStudents()))));
    		}
    	}
    	return cell;
    }
    
    private CSVField csvBuildLimit(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();
    	if (prefGroup instanceof SchedulingSubpart){
	    	SchedulingSubpart ss = (SchedulingSubpart) prefGroup;
	    	boolean unlimited = ss.getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue();
	    	if (!unlimited) {
		    	int limit = (ss.getLimit()==null?0:ss.getLimit().intValue());
		    	int maxExpCap = ss.getMaxExpectedCapacity(); 
		    	if (limit==maxExpCap)
		    		addText(cell, String.valueOf(limit), true);
		    	else
		    		addText(cell, limit+"-"+maxExpCap, true);
	    	} 
    	} else if (prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
	    	boolean unlimited = aClass.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue();
	    	if (!unlimited) {
	    		String limitString = null;
                Assignment a = null;
                try {
                    if (classAssignment!=null) a = classAssignment.getAssignment(aClass);
                } catch (Exception e) {}
                if (a==null) {
                    if (aClass.getExpectedCapacity() != null){
                        limitString = aClass.getExpectedCapacity().toString();
                        if (aClass.getMaxExpectedCapacity() != null && !aClass.getMaxExpectedCapacity().equals(aClass.getExpectedCapacity())){
                            limitString = limitString + "-" + aClass.getMaxExpectedCapacity().toString();
                        }
                    } else {
                        limitString = "0";
                        if (aClass.getMaxExpectedCapacity() != null && aClass.getMaxExpectedCapacity().intValue() != 0){
                            limitString = limitString + "-" + aClass.getMaxExpectedCapacity().toString();
                        }
                    }
                } else {
                    limitString = ""+aClass.getClassLimit(classAssignment);
                }
	    		addText(cell, limitString, true);
	    	}
    	} 
    	
        return cell;
    }
    
    private CSVField csvBuildDivisionSection(CourseOffering co, PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		// String divSec = aClass.getDivSecNumber();
    		String divSec = aClass.getClassSuffix(co);
    		if (divSec!=null)
    			addText(cell, divSec, true);
    	}
        return cell;
    }

    protected CSVField csvBuildInstructor(PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();
    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
        	TreeSet sortedInstructors = new TreeSet(new InstructorComparator());
        	sortedInstructors.addAll(aClass.getClassInstructors());
    		for (Iterator i=sortedInstructors.iterator(); i.hasNext();) {
    			ClassInstructor ci = (ClassInstructor)i.next();
        		String label = ci.getInstructor().getName(getInstructorNameFormat());
        		addText(cell, label, true);
    		}
    	}
    	
        return cell;
    }

    private CSVField csvBuildCredit(PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();

    	if (prefGroup instanceof SchedulingSubpart) {
    		SchedulingSubpart ss = (SchedulingSubpart) prefGroup;
    		if (ss.getCredit() != null) {
    			addText(cell, ss.getCredit().creditAbbv(), true);
    		}   		
    	}
    	
        return cell;
    }

    private CSVField csvBuildSchedulePrintNote(PreferenceGroup prefGroup, boolean isEditable, UserContext user) {
    	CSVField cell = createCell();

    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.getSchedulePrintNote()!=null) {
    			addText(cell, c.getSchedulePrintNote(), true);
    		}
    	}
    	
        return cell;
    }

    private CSVField csvBuildSchedulePrintNote(InstructionalOffering io, boolean isEditable, UserContext user){
    	CSVField cell = createCell();

    	StringBuffer note = new StringBuffer("");
		Set s = io.getCourseOfferings();
		for (Iterator i=s.iterator(); i.hasNext(); ) {
			CourseOffering coI = (CourseOffering) i.next();
			if (coI.getScheduleBookNote()!=null && coI.getScheduleBookNote().trim().length()>0) {
				if (note.length()>0)
					note.append("\n");
				note.append(coI.getScheduleBookNote());
			}
		}
		
		addText(cell, note.toString(), true);
        return(cell);
    }    
    
    private CSVField csvBuildNote(PreferenceGroup prefGroup, boolean isEditable, UserContext user){
    	CSVField cell = createCell();

    	if (prefGroup instanceof Class_) {
    		Class_ c = (Class_) prefGroup;
    		if (c.getNotes()!=null) {
    			if (c.getNotes().length() <= 30  || user == null || CommonValues.NoteAsFullText.eq(user.getProperty(UserProperty.ManagerNoteDisplay))){
    				addText(cell, c.getNotes(), true);
    			} else {
    				addText(cell, c.getNotes().substring(0, 30) + "...", true);
    			}
    		}
    	}
    	
        return cell;
    }

    private CSVField csvBuildManager(PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();

    	Department managingDept = null;
    	if (prefGroup instanceof Class_) {
    		managingDept = ((Class_)prefGroup).getManagingDept();
    	} else if (prefGroup instanceof SchedulingSubpart) {
    		managingDept = ((SchedulingSubpart)prefGroup).getManagingDept();
    	}
    	if (managingDept!=null) {
    		addText(cell, managingDept.getShortLabel(), true);
    	}

        return cell;
    }

    private CSVField csvBuildMinPerWeek(PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();

    	if (prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		String suffix = "";
    		ClassDurationType dtype = aClass.getSchedulingSubpart().getInstrOfferingConfig().getEffectiveDurationType();
    		if (dtype != null && !dtype.equals(aClass.getSchedulingSubpart().getSession().getDefaultClassDurationType())) {
    			suffix = " " + dtype.getAbbreviation();
    		}
    		addText(cell, aClass.getSchedulingSubpart().getMinutesPerWk() + suffix, true);
    	} else if (prefGroup instanceof SchedulingSubpart) {
    		SchedulingSubpart aSchedulingSubpart = (SchedulingSubpart) prefGroup;
    		String suffix = "";
    		ClassDurationType dtype = aSchedulingSubpart.getInstrOfferingConfig().getEffectiveDurationType();
    		if (dtype != null && !dtype.equals(aSchedulingSubpart.getSession().getDefaultClassDurationType())) {
    			suffix = " " + dtype.getAbbreviation();
    		}
    		addText(cell, aSchedulingSubpart.getMinutesPerWk() + suffix, true);
    	} 

        return cell;
    }

    private CSVField csvBuildRoomLimit(PreferenceGroup prefGroup, boolean isEditable, boolean classLimitDisplayed){
    	CSVField cell = createCell();

    	if (prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		if (aClass.getNbrRooms()!=null && aClass.getNbrRooms().intValue()!=1) {
    			if (aClass.getNbrRooms().intValue()==0)
    				addText(cell, "N/A", true);
    			else {
    				String text = aClass.getNbrRooms().toString();
    				text += " at ";
    				if (aClass.getRoomRatio() != null)
    					text += sRoomRatioFormat.format(aClass.getRoomRatio().floatValue());
    				else
    					text += "0";
    				addText(cell, text, true);
    			}
    		} else {
    			if (aClass.getRoomRatio() != null){
    				if (classLimitDisplayed && aClass.getRoomRatio().equals(new Float(1.0))){
    					addText(cell, "", true);
    				} else {
    					addText(cell, sRoomRatioFormat.format(aClass.getRoomRatio().floatValue()), true);
    				}
    			} else {
    				if (aClass.getExpectedCapacity() == null){
    					addText(cell, "", true);
    				} else {
    					addText(cell, "0", true);
    				}
    			}
    		}
    	}
        return cell;
    }
    
    private CSVField csvBuildAssignedTime(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable){
    	CSVField cell = createCell();

    	if (classAssignment!=null && prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		Assignment a = null;
    		try {
    			a = classAssignment.getAssignment(aClass);
    		} catch (Exception e) {
    			Debug.error(e);
    		}
    		if (a!=null) {
    			StringBuffer sb = new StringBuffer();
   				Enumeration<Integer> e = a.getTimeLocation().getDays();
   				while (e.hasMoreElements()){
   					sb.append(Constants.DAY_NAMES_SHORT[e.nextElement()]);
   				}
   				sb.append(" ");
   				sb.append(a.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm()));
   				sb.append("-");
   				sb.append(a.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm()));
   				addText(cell, sb.toString(), true);
    		} 
    	}
    	
        return cell;
    }
   
    private CSVField csvBuildAssignedRoom(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable) {
    	CSVField cell = createCell();

    	if (classAssignment!=null && prefGroup instanceof Class_) {
    		Class_ aClass = (Class_) prefGroup;
    		Assignment a = null;
    		try {
    			a= classAssignment.getAssignment(aClass);
    		} catch (Exception e) {
    			Debug.error(e);
    		}
    		if (a!=null) {
    			StringBuffer sb = new StringBuffer();
	    		Iterator it2 = a.getRooms().iterator();
	    		while (it2.hasNext()){
	    			Location room = (Location)it2.next();
	    			sb.append(room.getLabel());
	    			if (it2.hasNext()){
	        			sb.append("\n");
	        		} 
	    		}
	    		addText(cell, sb.toString(), true);
    		}
    	}
    	
        return cell;
    }

    private CSVField csvBuildAssignedRoomCapacity(ClassAssignmentProxy classAssignment, PreferenceGroup prefGroup, boolean isEditable) {
    	CSVField cell = createCell();

    	if (classAssignment!=null && prefGroup instanceof Class_){
    		Class_ aClass = (Class_) prefGroup;
    		Assignment a = null;
   			try {
   				a = classAssignment.getAssignment(aClass);
   			} catch (Exception e) {
   				Debug.error(e);
   			}
	   		if (a!=null) {
	   			StringBuffer sb = new StringBuffer();
				Iterator it2 = a.getRooms().iterator();
				while (it2.hasNext()){
					sb.append(((Location) it2.next()).getCapacity());
					if (it2.hasNext()){
    					sb.append("\n");
    				} 
				}
				addText(cell, sb.toString(), true);
    		}
    	}

    	return cell;
    }
    
    private CSVField csvBuildExamName(TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            sb.append(exam.getLabel());
            if (i.hasNext()) sb.append("\n");
        }
        
        CSVField cell = createCell();
        addText(cell, sb.toString(), true);
        return cell;
    }

    private CSVField csvBuildExamPeriod(ExamAssignmentProxy examAssignment, TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
                ExamAssignment ea = examAssignment.getAssignment(exam.getUniqueId());
                if (ea==null && !isShowExamName()) continue;
                sb.append(ea==null?"":ea.getPeriodAbbreviation());
            } else {
                if (exam.getAssignedPeriod()==null && !isShowExamName()) continue;
                sb.append(exam.getAssignedPeriod()==null?"":exam.getAssignedPeriod().getAbbreviation());
            }
            if (i.hasNext()) sb.append("\n");
        }
        
        CSVField cell = createCell();
        addText(cell, sb.toString(), true);
        return cell;
    }

    private CSVField csvBuildExamRoom(ExamAssignmentProxy examAssignment, TreeSet exams, boolean isEditable) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
                ExamAssignment ea = examAssignment.getAssignment(exam.getUniqueId());
                if (ea==null && !isShowExamName()) continue;
                sb.append(ea==null?"":ea.getRoomsName(", "));
            } else {
                if (exam.getAssignedPeriod()==null && !isShowExamName()) continue;
                for (Iterator j=new TreeSet(exam.getAssignedRooms()).iterator();j.hasNext();) {
                    Location location = (Location)j.next();
                    sb.append(location.getLabel());
                    if (j.hasNext()) sb.append(", ");
                }
            }
            if (i.hasNext()) sb.append("\n");
        }
        
        CSVField cell = createCell();
        addText(cell, sb.toString(), true);
        return cell;
    }

    
    protected void csvBuildClassOrSubpartRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable, String prevLabel, SessionContext context){
    	boolean classLimitDisplayed = false;
    	List<CSVField> line = new ArrayList<CSVField>();
    	if (isShowLabel()){
	        line.add(csvBuildPrefGroupLabel(co, prefGroup, indentSpaces, isEditable, prevLabel));
    	} 
    	if (isShowDivSec()){
    		line.add(csvBuildDivisionSection(co, prefGroup, isEditable));
    	}
    	if (isShowDemand()){
    		line.add(csvBuildPrefGroupDemand(prefGroup, isEditable));
    	} 
    	if (isShowProjectedDemand()){
    		line.add(csvBuildPrefGroupProjectedDemand(prefGroup, isEditable));
    	} 
    	if (isShowLimit()){
    		classLimitDisplayed = true;
    		line.add(csvBuildLimit(classAssignment, prefGroup, isEditable));
       	} 
    	if (isShowRoomRatio()){
    		line.add(csvBuildRoomLimit(prefGroup, isEditable, classLimitDisplayed));
       	} 
    	if (isShowManager()){
    		line.add(csvBuildManager(prefGroup, isEditable));
     	} 
    	if (isShowDatePattern()){
    		line.add(csvBuildDatePatternCell(classAssignment, prefGroup, isEditable));
     	} 
    	if (isShowMinPerWk()){
    		line.add(csvBuildMinPerWeek(prefGroup, isEditable));
       	} 
    	if (isShowTimePattern()){
    		line.add(csvBuildTimePatternCell(prefGroup, isEditable));
    	} 
    	if (isShowPreferences()){
	        for (int j = 0; j < PREFERENCE_COLUMN_ORDER.length; j++) {
	        	if (PREFERENCE_COLUMN_ORDER[j].equals(MSG.columnTimePref())) {
	        		line.add(csvBuildPreferenceCell(classAssignment,prefGroup, TimePref.class, isEditable));
	        	} else if (sAggregateRoomPrefs && PREFERENCE_COLUMN_ORDER[j].equals(MSG.columnAllRoomPref())) {
	        		line.add(csvBuildPreferenceCell(classAssignment,prefGroup, new Class[] {RoomPref.class, BuildingPref.class, RoomFeaturePref.class, RoomGroupPref.class} , isEditable));
	        	} else if (PREFERENCE_COLUMN_ORDER[j].equals(MSG.columnRoomPref())) {
	        		line.add(csvBuildPreferenceCell(classAssignment,prefGroup, RoomPref.class, isEditable));
	        	} else if (PREFERENCE_COLUMN_ORDER[j].equals(MSG.columnBuildingPref())) {
	        		line.add(csvBuildPreferenceCell(classAssignment,prefGroup, BuildingPref.class, isEditable));
	        	} else if (PREFERENCE_COLUMN_ORDER[j].equals(MSG.columnRoomFeaturePref())) {
	        		line.add(csvBuildPreferenceCell(classAssignment,prefGroup, RoomFeaturePref.class, isEditable));
	        	} else if (getDisplayDistributionPrefs() && PREFERENCE_COLUMN_ORDER[j].equals(MSG.columnDistributionPref())) {
	        		line.add(csvBuildPreferenceCell(classAssignment,prefGroup, DistributionPref.class, isEditable));
	        	} else if (PREFERENCE_COLUMN_ORDER[j].equals(MSG.columnRoomGroupPref())) {
	        		line.add(csvBuildPreferenceCell(classAssignment,prefGroup, RoomGroupPref.class, isEditable));
	        	}
	        }
    	} 
    	if (isShowInstructor()){
    		line.add(csvBuildInstructor(prefGroup, isEditable));
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
	        for (int j = 0; j < TIMETABLE_COLUMN_ORDER.length; j++) {
	        	if (TIMETABLE_COLUMN_ORDER[j].equals(MSG.columnAssignedTime())){
	        		line.add(csvBuildAssignedTime(classAssignment, prefGroup, isEditable));
	        	} else if (TIMETABLE_COLUMN_ORDER[j].equals(MSG.columnAssignedRoom())){
	        		line.add(csvBuildAssignedRoom(classAssignment, prefGroup, isEditable));
	        	} else if (TIMETABLE_COLUMN_ORDER[j].equals(MSG.columnAssignedRoomCapacity())){
	        		line.add(csvBuildAssignedRoomCapacity(classAssignment, prefGroup, isEditable));
	        	}
	        }
    	} 
    	if (isShowTitle()) {
    		line.add(createCell());
    	}
    	if (isShowCredit()){
    		line.add(createCell());     		
    	} 
    	if (isShowSubpartCredit()){
    		line.add(csvBuildCredit(prefGroup, isEditable));     		
    	} 
    	if (isShowConsent()){
	        line.add(createCell());
    	} 
    	if (isShowSchedulePrintNote()){
    		line.add(csvBuildSchedulePrintNote(prefGroup, isEditable, context.getUser()));     		
    	} 
    	if (isShowNote()){
    		line.add(csvBuildNote(prefGroup, isEditable, context.getUser()));     		
    	}
        if (isShowExam()) {
            TreeSet exams = new TreeSet();
            if (prefGroup instanceof Class_) {
                exams = getExams((Class_)prefGroup);
            }
            for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
            	if (!context.hasPermission(i.next(), Right.ExaminationView))
            		i.remove();
            }
            if (isShowExamName()) {
                line.add(csvBuildExamName(exams, isEditable));
            }
            if (isShowExamTimetable()) {
                line.add(csvBuildExamPeriod(examAssignment, exams, isEditable));
                line.add(csvBuildExamRoom(examAssignment, exams, isEditable));
            }
        }
        iFile.addLine(line);
    }
    
    private void csvBuildSchedulingSubpartRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, SchedulingSubpart ss, String indentSpaces, SessionContext context){
        boolean isEditable = context.hasPermission(ss, Right.SchedulingSubpartDetail);
        csvBuildClassOrSubpartRow(classAssignment, examAssignment, co, ss, indentSpaces, isEditable, null, context);
    }
    
    private void csvBuildSchedulingSubpartRows(Vector subpartIds, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, SchedulingSubpart ss, String indentSpaces, SessionContext context){
    	if (subpartIds!=null) subpartIds.add(ss.getUniqueId());
        csvBuildSchedulingSubpartRow(classAssignment, examAssignment, co, ss, indentSpaces, context);
        Set childSubparts = ss.getChildSubparts();
        
		if (childSubparts != null && !childSubparts.isEmpty()){
		    
		    ArrayList childSubpartList = new ArrayList(childSubparts);
		    Collections.sort(childSubpartList, new SchedulingSubpartComparator());
            Iterator it = childSubpartList.iterator();
            SchedulingSubpart child = null;
            
            while (it.hasNext()){              
                child = (SchedulingSubpart) it.next();
                csvBuildSchedulingSubpartRows(subpartIds, classAssignment, examAssignment, co, child, indentSpaces + indent, context);
            }
        }
    }
 
    protected void csvBuildClassRow(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, int ct, CourseOffering co, Class_ aClass, String indentSpaces, SessionContext context, String prevLabel){
        boolean isEditable = context.hasPermission(aClass, Right.ClassDetail);
        csvBuildClassOrSubpartRow(classAssignment, examAssignment, co, aClass, indentSpaces, isEditable && !aClass.isCancelled(), prevLabel, context);
    }
    
    private void csvBuildClassRows(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, int ct, CourseOffering co, Class_ aClass, String indentSpaces, SessionContext context, String prevLabel){
        csvBuildClassRow(classAssignment, examAssignment, ct, co, aClass, indentSpaces, context, prevLabel);
    	Set childClasses = aClass.getChildClasses();

    	if (childClasses != null && !childClasses.isEmpty()){
        
    	    ArrayList childClassesList = new ArrayList(childClasses);
            Collections.sort(childClassesList, getClassComparator());
            
            Iterator it = childClassesList.iterator();
            Class_ child = null;
            String previousLabel = aClass.htmlLabel();
            while (it.hasNext()){              
                child = (Class_) it.next();
                csvBuildClassRows(classAssignment, examAssignment, ct, co, child, indentSpaces + indent, context, previousLabel);
            }
        }
    }


	protected void csvBuildConfigRow(Vector subpartIds, ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, InstrOfferingConfig ioc, SessionContext context, boolean printConfigLine) {
	    boolean isEditable = context.hasPermission(ioc.getInstructionalOffering(), Right.InstructionalOfferingDetail);
	    List<CSVField> line = new ArrayList<CSVField>();	    
	    String configName = ioc.getName();
	    boolean unlimited = ioc.isUnlimitedEnrollment().booleanValue();
	    boolean hasConfig = false;
		if (printConfigLine) {
        	if (isShowLabel()) {
        	    if (configName==null || configName.trim().length()==0)
        	        configName = ioc.getUniqueId().toString();
        	    CSVField cell = createCell();
        	    addText(cell, indent + (ioc.getInstructionalMethod() == null ? MSG.labelConfiguration(configName) : MSG.labelConfigurationWithInstructionalMethod(configName, ioc.getInstructionalMethod().getReference())),
        	    		true);
        	    line.add(cell);
        	}
        	if (isShowDivSec()){
        	    line.add(createCell());
    		}
        	if (isShowDemand()){
        	    line.add(createCell());
    		}
        	if (isShowProjectedDemand()){
        	    line.add(createCell());
    		} 
        	if (isShowLimit()){
        	    CSVField cell = createCell();
        	    addText(cell, (unlimited?"inf":ioc.getLimit().toString()), true);
        	    line.add(cell);
        	} 
        	if (isShowRoomRatio()){
        	    line.add(createCell());
        	} 
        	if (isShowManager()){
        	    line.add(createCell());
        	} 
        	if (isShowDatePattern()){
        	    line.add(createCell());
	       	} 
        	if (isShowMinPerWk()){
        	    line.add(createCell());
        	} 
        	if (isShowTimePattern()){
        	    line.add(createCell());
        	} 
        	if (isShowPreferences()){
		        for (int j = 0; j < PREFERENCE_COLUMN_ORDER.length + (getDisplayDistributionPrefs()?0:-1); j++) {
	        	    line.add(createCell());
		        }
        	} 
        	if (isShowInstructor()){
        	    line.add(createCell());
        	} 
        	if (getDisplayTimetable() && isShowTimetable()){
        		for (int j = 0; j < TIMETABLE_COLUMN_ORDER.length; j++){
            	    line.add(createCell());
        		}
        	} 
        	if (isShowTitle()) {
        		line.add(createCell());
        	}
        	if (isShowCredit()){
        	    line.add(createCell());
        	} 
        	if (isShowSubpartCredit()){
        	    line.add(createCell());
        	} 
        	if (isShowConsent()){
    	        line.add(createCell());
        	} 
        	if (isShowSchedulePrintNote()){
        	    line.add(createCell());
        	} 
        	if (isShowNote()){
        	    line.add(createCell());
        	}

            if (isShowExam()) {
                TreeSet exams = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeConfig,ioc.getUniqueId()));
                if (isShowExamName()) {
                    line.add(csvBuildExamName(exams, isEditable));
                }
                for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
                	if (!context.hasPermission(i.next(), Right.ExaminationView))
                		i.remove();
                }
                if (isShowExamTimetable()) {
                    line.add(csvBuildExamPeriod(examAssignment, exams, isEditable));
                    line.add(csvBuildExamRoom(examAssignment, exams, isEditable));
                }
            }

	        hasConfig = true;
			iFile.addLine(line);
		}
		
        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        Iterator it = subpartList.iterator();
        SchedulingSubpart ss = null;
        while(it.hasNext()){
            ss = (SchedulingSubpart) it.next();
            if (ss.getParentSubpart() == null){
                csvBuildSchedulingSubpartRows(subpartIds, classAssignment, examAssignment, co, ss, (hasConfig?indent+indent:indent) , context);
            }
        }
        it = subpartList.iterator();
        int ct = 0;
        String prevLabel = null;
        while (it.hasNext()) {   	
			ss = (SchedulingSubpart) it.next();
			if (ss.getParentSubpart() == null) {
				if (ss.getClasses() != null) {
					Vector classes = new Vector(ss.getClasses());
					Collections.sort(classes,getClassComparator());
					Iterator cIt = classes.iterator();					
					Class_ c = null;
					while (cIt.hasNext()) {
						c = (Class_) cIt.next();
						csvBuildClassRows(classAssignment, examAssignment, ++ct, co, c, indent, context, prevLabel);
						prevLabel = c.htmlLabel();
					}
				}
			}
		}
   }

    private void csvBuildConfigRows(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, CourseOffering co, Set instrOfferingConfigs, SessionContext context, boolean printConfigLine) {
        Iterator it = instrOfferingConfigs.iterator();
        InstrOfferingConfig ioc = null;
        while (it.hasNext()){
            ioc = (InstrOfferingConfig) it.next();
            csvBuildConfigRow(null, classAssignment, examAssignment, co, ioc, context, printConfigLine && instrOfferingConfigs.size()>1);
        }
    }

    private void csvAddInstrOffrRowsToTable(ClassAssignmentProxy classAssignment, ExamAssignmentProxy examAssignment, InstructionalOffering io, Long subjectAreaId, SessionContext context){
        CourseOffering co = io.findSortCourseOfferingForSubjectArea(subjectAreaId);
        boolean isEditable = context.hasPermission(co.getInstructionalOffering(), Right.InstructionalOfferingDetail); 
        List<CSVField> line = new ArrayList<CSVField>();        
    	if (isShowLabel()){
    		line.add(csvSubjectAndCourseInfo(io, co));
    	}
    	if (isShowDivSec()) {
    		line.add(createCell());
		}
    	if (isShowDemand()){
    	    CSVField cell = createCell();
    	    if (StudentClassEnrollment.sessionHasEnrollments(io.getSessionId())){
        	    addText(cell, (io.getEnrollment() != null?io.getEnrollment().toString(): "0"), true);
    	    } else {
        	    addText(cell, (io.getDemand() != null?io.getDemand().toString(): "0"), true);    	    	
    	    }
    	    line.add(cell);
		}
    	if (isShowProjectedDemand()){
    	    CSVField cell = createCell();
    	    addText(cell, (io.getProjectedDemand() != null?io.getProjectedDemand().toString(): "0"), true);
    	    line.add(cell);
		} 
    	if (isShowLimit()){
			boolean unlimited = false;
			for (Iterator x=io.getInstrOfferingConfigs().iterator();!unlimited && x.hasNext();)
				if ((((InstrOfferingConfig)x.next())).isUnlimitedEnrollment().booleanValue())
					unlimited = true;
    	    CSVField cell = createCell();
    	    addText(cell, (unlimited?"inf":io.getLimit()==null?"0":io.getLimit().toString()), true);
    	    line.add(cell);
    	}
    	int emptyCels = 0;
    	if (isShowRoomRatio()){
    		emptyCels ++;
    	} 
    	if (isShowManager()){
    		emptyCels ++;
    	}
    	if (isShowDatePattern()){
    		emptyCels ++;
       	}
    	if (isShowMinPerWk()) {
    		emptyCels ++;
    	} 
    	if (isShowTimePattern()){
    		emptyCels ++;
    	}
    	if (isShowPreferences()){
    		emptyCels += PREFERENCE_COLUMN_ORDER.length + (getDisplayDistributionPrefs()?0:-1);
    	}
    	if (isShowInstructor()){
    		emptyCels ++;
    	}
    	if (getDisplayTimetable() && isShowTimetable()){
    		emptyCels += TIMETABLE_COLUMN_ORDER.length;
    	} 
    	if (emptyCels > 0) {
            for (int i = 0; i < emptyCels; i++)
            	line.add(createCell());	
    	}
    	if (isShowTitle()) {
       	    CSVField titleCell = createCell();
    		addText(titleCell, (co.getTitle() == null ? "" : co.getTitle()), true);
            for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
    			addText(titleCell, (x.getTitle() == null ? "" : x.getTitle()), true);
            }
    	    line.add(titleCell);
    	}
    	if (isShowCredit()){
    	    CSVField cell = createCell();
    	    addText(cell, (co.getCredit() != null ? co.getCredit().creditAbbv() : ""), true);
    	    for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
            	addText(cell, (x.getCredit() != null ? x.getCredit().creditAbbv() : ""), true);
    	    }
    	    line.add(cell);
    	}
    	if (isShowSubpartCredit()) {
    		line.add(createCell());
    	}
    	if (isShowConsent()){
    	    CSVField cell = createCell();
    	    addText(cell, co.getConsentType() != null ? co.getConsentType().getAbbv() : MSG.noConsentRequired(), true);
    	    for (Iterator it = io.courseOfferingsMinusSortCourseOfferingForSubjectArea(co.getSubjectArea().getUniqueId()).iterator(); it.hasNext();) {
            	CourseOffering x = (CourseOffering)it.next();
            	addText(cell, (x.getConsentType() != null ? x.getConsentType().getAbbv() : MSG.noConsentRequired()), true);
    	    }
    	    line.add(cell);
    	} 
    	if (isShowSchedulePrintNote()){
    		line.add(csvBuildSchedulePrintNote(io, isEditable, context.getUser()));     		
    	}
    	if (isShowNote()){
    		line.add(createCell());     		
    	}
        if (isShowExam()) {
            TreeSet exams = new TreeSet(Exam.findAll(ExamOwner.sOwnerTypeOffering,io.getUniqueId()));
            for (Iterator i=io.getCourseOfferings().iterator();i.hasNext();) {
                CourseOffering cox = (CourseOffering)i.next();
                exams.addAll(Exam.findAll(ExamOwner.sOwnerTypeCourse,cox.getUniqueId()));
            }
            if (io.getInstrOfferingConfigs().size()==1) {
                for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();) {
                    InstrOfferingConfig ioc = (InstrOfferingConfig)i.next();
                    exams.addAll(Exam.findAll(ExamOwner.sOwnerTypeConfig,ioc.getUniqueId()));
                }
            }
            for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
            	if (!context.hasPermission(i.next(), Right.ExaminationView))
            		i.remove();
            }
            if (isShowExamName()) {
                line.add(csvBuildExamName(exams, isEditable));
            }
            if (isShowExamTimetable()) {
                line.add(csvBuildExamPeriod(examAssignment, exams, isEditable));
                line.add(csvBuildExamRoom(examAssignment, exams, isEditable));
            }
        }
        iFile.addLine(line);

        if (io.getInstrOfferingConfigs() != null & !io.getInstrOfferingConfigs().isEmpty()){
        	TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
            csvBuildConfigRows(classAssignment, examAssignment, io.getControllingCourseOffering(), configs, context, true);
        }
    }
    
    protected void save(PrintWriter out) {
        if (iFile.getHeader() != null)
            out.println(iFile.getHeader().toString());
        if (iFile.getLines() != null) {
            for (CSVLine line : iFile.getLines()) {
                out.println(line.toString());
            }
        }
		out.flush();
    }

    public void csvTableForInstructionalOffering(
    		PrintWriter out,
    		ClassAssignmentProxy classAssignment, 
    		ExamAssignmentProxy examAssignment,
            Long instructionalOfferingId, 
            SessionContext context,
            Comparator classComparator) throws Exception{
    	
    	if (instructionalOfferingId != null && context != null){
	        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(instructionalOfferingId);
	        Long subjectAreaId = io.getControllingCourseOffering().getSubjectArea().getUniqueId();
	        
	        // Get Configuration
	        TreeSet ts = new TreeSet();
	        ts.add(io);
	        WebInstructionalOfferingTableBuilder iotbl = new WebInstructionalOfferingTableBuilder();
	        iotbl.setDisplayDistributionPrefs(false);
	        setVisibleColumns(COLUMNS);
	        
		    csvTableForInstructionalOfferings(out,
				        classAssignment, examAssignment,
				        ts, subjectAreaId, context, false, false, classComparator);
    	}
    }
    
    public void csvTableForInstructionalOfferings(
    		PrintWriter out,
            ClassAssignmentProxy classAssignment,
            ExamAssignmentProxy examAssignment,
            InstructionalOfferingListForm form, 
            String[] subjectAreaIds, 
            SessionContext context,
            boolean displayHeader,
            boolean allCoursesAreGiven) throws Exception{
    	
    	setVisibleColumns(form);
    	
    	for (int i = 0; i < subjectAreaIds.length; i++) {
    		Long subjectAreaId = Long.valueOf(subjectAreaIds[i]);
    		if (i > 0) { out.println(); out.println(); }
        	csvTableForInstructionalOfferings(out, classAssignment, examAssignment,
        			form.getInstructionalOfferings(subjectAreaId), 
        			subjectAreaId,
        			context,
        			displayHeader, allCoursesAreGiven,
        			new ClassCourseComparator(form.getSortBy(), classAssignment, false));
    	}
    }
    
    protected void csvTableForInstructionalOfferings(
    		PrintWriter out,
            ClassAssignmentProxy classAssignment, 
            ExamAssignmentProxy examAssignment,
            TreeSet insructionalOfferings, 
            Long subjectAreaId, 
            SessionContext context,
            boolean displayHeader, boolean allCoursesAreGiven,
            Comparator classComparator) throws Exception {
    	
    	if (insructionalOfferings == null) return;
    	
    	SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(subjectAreaId);
    	
    	if (classComparator!=null)
    		setClassComparator(classComparator);
    	
    	if (isShowTimetable()) {
    		boolean hasTimetable = false;
    		if (context.hasPermission(Right.ClassAssignments) && classAssignment != null) {
            	if (classAssignment instanceof CachedClassAssignmentProxy) {
            		Vector allClasses = new Vector();
    				for (Iterator i=insructionalOfferings.iterator();!hasTimetable && i.hasNext();) {
    					InstructionalOffering io = (InstructionalOffering)i.next();
    					for (Iterator j=io.getInstrOfferingConfigs().iterator();!hasTimetable && j.hasNext();) {
    						InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
    						for (Iterator k=ioc.getSchedulingSubparts().iterator();!hasTimetable && k.hasNext();) {
    							SchedulingSubpart ss = (SchedulingSubpart)k.next();
    							for (Iterator l=ss.getClasses().iterator();l.hasNext();) {
    								Class_ clazz = (Class_)l.next();
    								allClasses.add(clazz);
    							}
    						}
    					}
    				}
            		((CachedClassAssignmentProxy)classAssignment).setCache(allClasses);
            		hasTimetable = !classAssignment.getAssignmentTable(allClasses).isEmpty();
            	} else {
    				for (Iterator i=insructionalOfferings.iterator();!hasTimetable && i.hasNext();) {
    					InstructionalOffering io = (InstructionalOffering)i.next();
    					for (Iterator j=io.getInstrOfferingConfigs().iterator();!hasTimetable && j.hasNext();) {
    						InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
    						for (Iterator k=ioc.getSchedulingSubparts().iterator();!hasTimetable && k.hasNext();) {
    							SchedulingSubpart ss = (SchedulingSubpart)k.next();
    							for (Iterator l=ss.getClasses().iterator();l.hasNext();) {
    								Class_ clazz = (Class_)l.next();
    								if (classAssignment.getAssignment(clazz)!=null) {
    									hasTimetable = true; break;
    								}
    							}
            				}
            			}
            		}
            	}
    		}
    		setDisplayTimetable(hasTimetable);
    	}
    	
        if (isShowExam())
            setShowExamTimetable(examAssignment != null || Exam.hasTimetable(context.getUser().getCurrentAcademicSessionId()));

        ArrayList notOfferedOfferings = new ArrayList();
        ArrayList offeredOfferings = new ArrayList();
        ArrayList offeringIds = new ArrayList();
        
        Iterator it = insructionalOfferings.iterator();
        InstructionalOffering io = null;
        boolean hasOfferedCourses = false;
        boolean hasNotOfferedCourses = false;
		setUserSettings(context.getUser());
        
         while (it.hasNext()){
            io = (InstructionalOffering) it.next();
            if (io.isNotOffered() == null || io.isNotOffered().booleanValue()){
            	hasNotOfferedCourses = true;
            	notOfferedOfferings.add(io);
            } else {
            	hasOfferedCourses = true;
            	offeredOfferings.add(io);
            }
        }
        
        iFile = new CSVFile();
        if (hasOfferedCourses || allCoursesAreGiven) {
        	if (displayHeader)
        		iFile.addLine(MSG.labelOfferedCourses(subjectArea.getSubjectAreaAbbreviation()));
    		csvBuildTableHeader(context.getUser().getCurrentAcademicSessionId());
                  
            if (hasOfferedCourses){
                it = offeredOfferings.iterator();
                while (it.hasNext()){
                    io = (InstructionalOffering) it.next();
                    offeringIds.add(io.getUniqueId());
                    	csvAddInstrOffrRowsToTable(classAssignment, examAssignment, io, subjectAreaId, context);            	
                }
            } else {
                if(displayHeader)
                	iFile.addLine(MSG.errorNoCoursesOffered(subjectArea.getSubjectAreaAbbreviation())); 
            }
        }
        
        if (hasNotOfferedCourses || allCoursesAreGiven) {
        	if (displayHeader) {
   	        	iFile.addLine();
   	        	iFile.addLine(MSG.labelNotOfferedCourses(subjectArea.getSubjectAreaAbbreviation()));
            }
            csvBuildTableHeader(context.getUser().getCurrentAcademicSessionId());
            
            if (hasNotOfferedCourses){
                it = notOfferedOfferings.iterator();
                while (it.hasNext()){
                    io = (InstructionalOffering) it.next();
                    offeringIds.add(io.getUniqueId());
                    csvAddInstrOffrRowsToTable(classAssignment, examAssignment, io, subjectAreaId, context);            	
                }
            } else {
                if (displayHeader)
                	iFile.addLine(MSG.errorAllCoursesOffered(subjectArea.getSubjectAreaAbbreviation()));
            }
        }
        
        save(out);
    }

}
