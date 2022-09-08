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
package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class ExamVerificationReport extends PdfLegacyExamReport {
    protected static Log sLog = LogFactory.getLog(ExamVerificationReport.class);
    private boolean iSkipHoles = true;
    // Skip subparts of the same itype as parent subpart
    private boolean iSkipSuffixSubparts = ApplicationProperty.ExaminationPdfReportsSkipSuffixSubpart.isTrue();
    private boolean iHasAssignment = false;
    
    public ExamVerificationReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, MSG.legacyReportExaminationVerificationReport(), session, examType, subjectAreas, exams);
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getPeriod()!=null) { iHasAssignment = true; break; }
        }
    }
    
    public TreeSet<ExamAssignmentInfo> getExams(CourseOffering course) {
        TreeSet<ExamAssignmentInfo> exams = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (section.getOwnerType()==ExamOwner.sOwnerTypeCourse && section.getOwnerId().equals(course.getUniqueId()))
                    exams.add(exam);
                if (section.getOwnerType()==ExamOwner.sOwnerTypeOffering && section.getOwnerId().equals(course.getInstructionalOffering().getUniqueId()))
                    exams.add(exam);
            }
        }
        return exams;
    }
    
    public TreeSet<ExamAssignmentInfo> getExams(Class_ clazz) {
        TreeSet<ExamAssignmentInfo> exams = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (section.getOwnerType()==ExamOwner.sOwnerTypeClass && section.getOwnerId().equals(clazz.getUniqueId()))
                    exams.add(exam);
                if (section.getOwnerType()==ExamOwner.sOwnerTypeConfig && section.getOwnerId().equals(clazz.getSchedulingSubpart().getInstrOfferingConfig().getUniqueId()))
                    exams.add(exam);
            }
        }
        if (iSkipSuffixSubparts && clazz.getChildClasses() != null)
        	for (Class_ child: clazz.getChildClasses()) {
        		if (child.getSchedulingSubpart().getItype().equals(clazz.getSchedulingSubpart().getItype()))
        			exams.addAll(getExams(child));
        	}
        return exams;
    }
    
    public String genName(String pattern, Class_ clazz) {
        String name = pattern;
        int idx = -1;
        while (name.indexOf('%',idx+1)>=0) {
            idx = name.indexOf('%',idx);
            char code = name.charAt(idx+1);
            String name4code = genName(code, clazz);
            name = name.substring(0,idx)+(name4code==null?"":name4code)+name.substring(idx+2);
        }
        return name;
    }
    
    protected String genName(char code, Class_ clazz) {
        switch (code) {
        case '_' : return " ";
        case 's' : return clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation();
        case 'c' : return clazz.getSchedulingSubpart().getControllingCourseOffering().getCourseNbr();
        case 'T' : return clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
        case 'i' : return clazz.getSchedulingSubpart().getItypeDesc().trim();
        case 'n' : return clazz.getSectionNumberString();
        case 'x' : return clazz.getSchedulingSubpart().getInstrOfferingConfig().getName();
        case 'D' : return clazz.getControllingDept().getDeptCode();
        case 'd' :
            Department d = clazz.getControllingDept();
            return (d.getAbbreviation()==null || d.getAbbreviation().length()==0?d.getDeptCode():d.getAbbreviation());
        case 'a' : return clazz.getClassSuffix();
        case 'y' : return clazz.getSchedulingSubpart().getSchedulingSubpartSuffix();
        case 'e' : return clazz.getExternalUniqueId();
        case 'f' : return clazz.getSchedulingSubpart().getControllingCourseOffering().getExternalUniqueId();
        case 'o' : return clazz.getSchedulingSubpart().getControllingCourseOffering().getInstructionalOffering().getExternalUniqueId();
        case 't' : return "";
        case 'I' : return clazz.getSchedulingSubpart().getItype().getItype().toString();
        case 'p' : 
            ItypeDesc itype = clazz.getSchedulingSubpart().getItype();
            while (itype.getParent()!=null) itype = itype.getParent();
            return itype.getAbbv();
        case 'P' :
            itype = clazz.getSchedulingSubpart().getItype();
            while (itype.getParent()!=null) itype = itype.getParent();
            return itype.getItype().toString();
        case 'm':
        	InstructionalMethod im = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
        	if (im != null) return im.getReference();
        	return "";
        case 'M':
        	im = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
        	if (im != null) return im.getLabel();
        	return "";
        }
        return "";
    }
    
    public String getMeetWith(Class_ clazz, Vector<Class_> exclude) {
        TreeSet<Class_> classes = new TreeSet(new Comparator<Class_>() {
            public int compare(Class_ c1, Class_ c2) {
                if (c1.getSchedulingSubpart().equals(c2.getSchedulingSubpart())) {
                    String sx1 = (iUseClassSuffix?c1.getClassSuffix():c1.getSectionNumberString());
                    String sx2 = (iUseClassSuffix?c2.getClassSuffix():c2.getSectionNumberString());
                    if (sx1!=null && sx2!=null) return sx1.compareTo(sx2);
                    return c1.getSectionNumber().compareTo(c2.getSectionNumber());
                } 
                return new SchedulingSubpartComparator().compare(c1.getSchedulingSubpart(), c2.getSchedulingSubpart());
            }
        });
        for (Iterator i=clazz.getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject dObj = (DistributionObject)i.next();
            if (!"MEET_WITH".equals(dObj.getDistributionPref().getDistributionType().getReference())) continue;
            for (Iterator j=dObj.getDistributionPref().getDistributionObjects().iterator();j.hasNext();) {
                DistributionObject xObj = (DistributionObject)j.next();
                if (exclude!=null && exclude.contains(xObj.getPrefGroup())) continue;
                if (xObj.getPrefGroup() instanceof Class_) {
                    classes.add((Class_)xObj.getPrefGroup());
                } else {
                    classes.addAll(((SchedulingSubpart)xObj.getPrefGroup()).getClasses());
                }
            }
        }
        if (classes.isEmpty()) return "";
        Class_ prev = clazz;
        String ret = "";
        for (Class_ c : classes) {
            if (ret.length()==0)
                ret+=genName(ApplicationProperty.ExamNameClass.value(),c);
            else if (prev.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().equals(c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea())) {
                //same subject area
                if (prev.getSchedulingSubpart().getControllingCourseOffering().equals(c.getSchedulingSubpart().getControllingCourseOffering())) {
                    //same course number
                    if (prev.getSchedulingSubpart().equals(c.getSchedulingSubpart()))
                        ret+=genName(ApplicationProperty.ExamNameSameSubpartClass.value(), c);
                    else
                        ret+=genName(ApplicationProperty.ExamNameSameCourseClass.value(), c);
                } else {
                    //different course number
                    ret+=genName(ApplicationProperty.ExamNameSameSubjectClass.value(), c);
                }
            } else {
                ret+=genName(ApplicationProperty.ExamNameSeparator.value(),prev);
                ret+=genName(ApplicationProperty.ExamNameClass.value(),c);
            }
            prev = c;
        }
        return ret;
    }
    
    private String formatSection(Class_ clazz) { 
        return (!iUseClassSuffix || clazz.getClassSuffix()==null || clazz.getClassSuffix().length()==0?clazz.getSectionNumberString():clazz.getClassSuffix());
    }
    
    private String formatSection(Vector<Class_>  classes) {
        if (classes.isEmpty()) return "";
        if (classes.size()==1) return formatSection(classes.firstElement());
        return formatSection(classes.firstElement())+" - "+formatSection(classes.lastElement());
    }
        
    public String getMessage(Class_ clazz, boolean hasCourseExam, boolean hasSectionExam, Hashtable<Long,ClassEvent> class2event) {
        TreeSet<ExamAssignmentInfo> exams = getExams(clazz);
        if (!exams.isEmpty()) return "";
        String message = MSG.lrNoExam();
        if (hasCourseExam && !hasSectionExam) message = ""; // Has other exam
        if (!hasSectionExam && !clazz.getSchedulingSubpart().getItype().isOrganized()) message = MSG.lrNotOrganizedIType();
        else {
            ClassEvent event = class2event.get(clazz.getUniqueId());
            if (event==null || event.getMeetings().isEmpty()) {
                message = MSG.lrClassNotOrganized();
            } else if (!isFullTerm(event)) {
                TreeSet meetings = new TreeSet(event.getMeetings());
                Meeting first = (Meeting)meetings.first();
                Meeting last = (Meeting)meetings.last();
                SimpleDateFormat df = new SimpleDateFormat(MSG.lrDateFormat());
                message = MSG.lrClassNotFullTerm()+" ("+df.format(first.getMeetingDate())+(first.getMeetingDate().equals(last.getMeetingDate())?"":" - "+df.format(last.getMeetingDate()))+")";
            }
        }
        return message;
    }

    private void print(Vector<Class_> same, boolean hasCourseExam, boolean hasSectionExam, int minLimit, int maxLimit, int minEnrl, int maxEnrl, Hashtable<Long,ClassEvent> class2event) throws DocumentException {
        String cmw = getMeetWith(same.firstElement(),same);
        TreeSet<ExamAssignmentInfo> exams = getExams(same.firstElement());
        iPeriodPrinted = false;
        if (exams.isEmpty()) {
            String message = MSG.lrNoExam();
            if (hasCourseExam && !hasSectionExam) message = ""; // Has other exam
            if (!hasSectionExam && !same.firstElement().getSchedulingSubpart().getItype().isOrganized()) message = MSG.lrNotOrganizedIType();
            else {
                ClassEvent classEvent = class2event.get(same.firstElement().getUniqueId());
                if (classEvent==null || classEvent.getMeetings().isEmpty()) {
                    message = MSG.lrClassNotOrganized();
                } else if (!isFullTerm(classEvent)) {
                    TreeSet meetings = new TreeSet(classEvent.getMeetings());
                    Meeting first = (Meeting)meetings.first();
                    Meeting last = (Meeting)meetings.last();
                    SimpleDateFormat df = new SimpleDateFormat(MSG.lrDateFormat());
                    message = MSG.lrClassNotFullTerm()+" ("+df.format(first.getMeetingDate())+(first.getMeetingDate().equals(last.getMeetingDate())?"":" - "+df.format(last.getMeetingDate()))+")";
                }
            }
            String title = (iDispNote ? same.firstElement().getSchedulePrintNote() : null);
            /*
            if (title!=null && title.equals(same.firstElement().getSchedulingSubpart().getControllingCourseOffering().getTitle()))
                title = null;
                */
            boolean hasTitle = (title!=null && title.trim().length()>0);
            boolean titleSameLine = hasTitle && (" "+title).length()<=((iDispLimits?28:46)-formatSection(same).length()-(same.size()>1?" ("+same.size()+")":"").length());
            boolean titleSeparateLine = hasTitle && !titleSameLine;
            boolean hasMw = cmw.length()>0;
            boolean mwSameLine = hasMw && !titleSameLine && (" "+MSG.lrMW()+" "+cmw).length()<=((iDispLimits?28:46)-formatSection(same).length()-(same.size()>1?" ("+same.size()+")":"").length());
            boolean mwSeparateLine = hasMw && !mwSameLine;
            if ((titleSeparateLine || mwSeparateLine) && getLineNumber()+1+(titleSeparateLine?0:1)+(mwSeparateLine?1:0)>getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
            println(
                    lpad(iITypePrinted?"":same.firstElement().getSchedulingSubpart().getItypeDesc().trim(),13),
                    rpad(formatSection(same)+(same.size()>1?" ("+same.size()+")":"")+(titleSameLine?" "+title:"")+(mwSameLine?" "+MSG.lrMW()+" "+cmw:""),(iDispLimits?28:46)).withSeparator(iDispLimits ? "" : " "),
                    (iDispLimits?lpad(maxLimit<=0?"":minLimit!=maxLimit?minLimit+"-"+maxLimit:""+minLimit,9):NULL).withSeparator(""),
                    (iDispLimits?lpad(maxEnrl<=0?"":minEnrl!=maxEnrl?minEnrl+"-"+maxEnrl:""+minEnrl,9):NULL),
                    rpad("", 4), lpad("", 3), new Cell(message).withColSpan(iDispLimits ? 5 : 3)
                    );
            if (titleSeparateLine)
                println(lpad("",13).withSeparator("  "),
                		new Cell(title.length()>118 && getNrCharsPerLine() < 1000?title.substring(0,115)+"...":title).withColSpan(iDispLimits ? 10 : 8));
            if (mwSeparateLine)
            	println(lpad("",13).withSeparator("  "),
            			new Cell(MSG.lrMeetsWith()+" "+(cmw.length()>107 && getNrCharsPerLine() < 1000?cmw.substring(0,104)+"...":cmw)).withColSpan(iDispLimits ? 10 : 8));
            iITypePrinted = !iNewPage;
        } else for (ExamAssignmentInfo exam : exams) {
            Vector<Cell> rooms = new Vector();
            Vector<Cell> roomCaps = new Vector();
            Vector<Cell> roomExCaps = new Vector();
            Vector<Cell> times = new Vector();
            if (exam.getPeriod()==null) {
                times.add(rpad(iHasAssignment?" " + MSG.lrExamNotAssigned():" " + MSG.lrSectionExam(),26));
                rooms.add(rpad("", 12));
                roomCaps.add(rpad("", 4));
                roomExCaps.add(rpad("", 5));
                //if (exam.getMaxRooms()==0) rooms.add(" "+rpad(iNoRoom, 22));
                for (Iterator i=new TreeSet(exam.getExam().getPreferences()).iterator();i.hasNext();) {
                    Preference pref = (Preference)i.next();
                    if (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) || PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) {
                        String pf = (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"!");
                        if (pref instanceof ExamPeriodPref) {
                            ExamPeriodPref xp = (ExamPeriodPref)pref;
                            times.add(rpad(pf + formatPeriod(xp.getExamPeriod(), exam.getLength(), exam.getPrintOffset()), 26));
                        } else if (exam.getMaxRooms()>0) {
                            if (pref instanceof RoomPref) {
                                RoomPref rp = (RoomPref)pref;
                                rooms.add(new Cell(new Cell(pf).withSeparator(""), formatRoom(rp.getRoom())));
                                roomCaps.add(lpad(""+rp.getRoom().getCapacity(),4));
                                roomExCaps.add(lpad(""+rp.getRoom().getExamCapacity(),5));
                            } else if (pref instanceof BuildingPref) {
                                BuildingPref bp = (BuildingPref)pref;
                                rooms.add(new Cell(new Cell(pf).withSeparator(""), rpad(bp.getBuilding().getAbbreviation(), 22)).withColSpan(3));
                                roomCaps.add(NULL);
                                roomExCaps.add(NULL);
                            } else if (pref instanceof RoomFeaturePref) {
                                RoomFeaturePref fp = (RoomFeaturePref)pref;
                                rooms.add(new Cell(new Cell(pf).withSeparator(""), rpad(fp.getRoomFeature().getLabel(), 22)).withColSpan(3));
                                roomCaps.add(NULL);
                                roomExCaps.add(NULL);
                            } else if (pref instanceof RoomGroupPref) {
                                RoomGroupPref gp = (RoomGroupPref)pref;
                                rooms.add(new Cell(new Cell(pf).withSeparator(""), rpad(gp.getRoomGroup().getName(), 22)).withColSpan(3));
                                roomCaps.add(NULL);
                                roomExCaps.add(NULL);
                            }
                        }
                    }
                }
                for (Iterator i=exam.getExam().getDistributionObjects().iterator();i.hasNext();) {
                    DistributionObject dObj = (DistributionObject)i.next();
                    DistributionPref pref = dObj.getDistributionPref();
                    if (!PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) && !PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) continue;
                    int line = 0;
                    String name = (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"!")+pref.getDistributionType().getAbbreviation();
                    if (name.toUpperCase().startsWith("!SAME ")) name = " Diff"+name.substring(5);
                    for (Iterator j=new TreeSet(pref.getDistributionObjects()).iterator();j.hasNext();) {
                        DistributionObject xObj = (DistributionObject)j.next();
                        if (xObj.equals(dObj)) continue;
                        Exam x = (Exam)xObj.getPrefGroup();
                        for (Iterator k=new TreeSet(x.getOwners()).iterator();k.hasNext();) {
                            ExamOwner own = (ExamOwner)k.next();
                            times.add(rpad(rpad(line>0?"":name,name.length())+" "+own.getLabel(),26));
                            line++;
                        }
                    }
                }
            } else {
                if (exam.getRooms()==null || exam.getRooms().isEmpty()) {
                	rooms.add(rpad(iNoRoom, 12));
                    roomCaps.add(rpad("", 4));
                    roomExCaps.add(rpad("", 5));
                } else for (ExamRoomInfo room : exam.getRooms()) {
                	rooms.add(new Cell(new Cell(" ").withSeparator(""), formatRoom(room)));
                    roomCaps.add(lpad(""+room.getCapacity(),4));
                    roomExCaps.add(lpad(""+room.getExamCapacity(),5));;
                }
                times.add(new Cell(new Cell(" ").withSeparator(""), rpad(formatPeriod(exam),25)));
            }
            Vector<Cell> meetsWith = new Vector();
            int cnt = 0;
            int maxCnt = Math.max(4,Math.max(rooms.size(), times.size())-1);
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (section.getOwnerType()==ExamOwner.sOwnerTypeClass && same.contains(section.getOwner().getOwnerObject())) continue;
                if (section.getOwnerType()==ExamOwner.sOwnerTypeConfig && section.getOwnerId().equals(same.firstElement().getSchedulingSubpart().getInstrOfferingConfig().getUniqueId())) continue;
                if (cnt>=maxCnt) {
                    meetsWith.add(rpad(" ...",14)); break;
                }
                if (iItype)
                    meetsWith.add(rpad(section.getName(),14));
                else
                    meetsWith.add(new Cell(
                            rpad(section.getSubject(),4),
                            rpad(section.getCourseNbr(),5),
                            rpad(section.getSection(),3)));
                cnt++;
            }

            int nrLines = Math.max(Math.max(rooms.size(), meetsWith.size()),times.size());
            String title = (iDispNote ? same.firstElement().getSchedulePrintNote() : null);
            /*
            if (title!=null && title.equals(same.firstElement().getSchedulingSubpart().getControllingCourseOffering().getTitle()))
                title = null;
                */
            boolean hasTitle = !iPeriodPrinted && (title!=null && title.trim().length()>0);
            boolean titleSameLine = hasTitle && (" "+title).length()<=((iDispLimits?28:46)-formatSection(same).length()-(same.size()>1?" ("+same.size()+")":"").length());
            boolean titleSecondLine = hasTitle && !titleSameLine && nrLines>1 && (" "+title).length()<=(iDispLimits?28:46);
            boolean titleSeparateLine = hasTitle && !titleSameLine && !titleSecondLine;
            
            boolean hasMw = !iPeriodPrinted && cmw.length()>0;
            boolean mwSameLine = hasMw && !titleSameLine && (" "+MSG.lrMW()+" "+cmw).length()<=((iDispLimits?28:46)-formatSection(same).length()-(same.size()>1?" ("+same.size()+")":"").length());
            boolean mwSecondLine = hasMw && !mwSameLine && !titleSecondLine && nrLines>1 && (" " + MSG.lrMeetsWith() + " "+cmw).length()<=(iDispLimits?28:46);
            boolean mwThirdLine = hasMw && !mwSameLine && titleSecondLine && nrLines>2 && (" " + MSG.lrMeetsWith() + " "+cmw).length()<=(iDispLimits?28:46);
            boolean mwSeparateLine = hasMw && !mwSameLine && !mwSecondLine && !mwThirdLine;
            if (getLineNumber()+nrLines+(mwSeparateLine?1:0)+(titleSeparateLine?1:0)>getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
            
            for (int idx = 0; idx < nrLines; idx++) {
                Cell room = (idx<rooms.size()?rooms.elementAt(idx):rpad("",11));
                Cell roomCap = (idx<roomCaps.size()?roomCaps.elementAt(idx):rpad("",4));
                Cell roomExCap = (idx<roomExCaps.size()?roomExCaps.elementAt(idx):rpad("",5));
                Cell mw = (idx<meetsWith.size()?meetsWith.elementAt(idx):new Cell(""));
                Cell time = (idx<times.size()?times.elementAt(idx):rpad("",26));
                println(lpad(idx>0 || iITypePrinted?"":same.firstElement().getSchedulingSubpart().getItypeDesc().trim(),13),
                        rpad(iPeriodPrinted?"":idx>0 ? (idx==1 && mwSecondLine?" " + MSG.lrMeetsWith() + " "+cmw:"")+
                                     (idx==1 && titleSecondLine?" "+title:"")+
                                     (idx==2 && mwThirdLine?" " + MSG.lrMeetsWith() + " "+cmw:"")
                                   : formatSection(same)+(same.size()>1?" ("+same.size()+")":"")+
                                     (titleSameLine?" "+title:"")+
                                     (mwSameLine?" "+MSG.lrMW()+" "+cmw:"")
                                   ,(iDispLimits?28:46)).withSeparator(iDispLimits ? "" : " "),
                        (iDispLimits?lpad(iPeriodPrinted || idx>0 || maxLimit<=0?"":minLimit!=maxLimit?minLimit+"-"+maxLimit:""+minLimit,9).withSeparator(""):NULL),
                        (iDispLimits?lpad(iPeriodPrinted || idx>0 || maxEnrl<=0?"":minEnrl!=maxEnrl?minEnrl+"-"+maxEnrl:""+minEnrl,9):NULL),
                        lpad(idx>0?"":exam.getSeatingType()==Exam.sSeatingTypeExam?MSG.lrYes():MSG.lrNo(),4),
                        lpad(idx>0?"":String.valueOf(exam.getLength()),3).withSeparator(""),
                        time.withSeparator(""),
                        room, roomCap, roomExCap,
                        mw
                        );
                if (idx==0 && titleSeparateLine)
                    println(lpad("",13),
                    		new Cell(title.length()>118 && getNrCharsPerLine() < 1000?title.substring(0,115)+"...":title).withColSpan(iDispLimits ? 10 : 8));
                if (idx==0 && mwSeparateLine)
                    println(lpad("",13), new Cell("  " + MSG.lrMeetsWith() + " " +(cmw.length()>107 && getNrCharsPerLine() < 1000?cmw.substring(0,104)+"...":cmw)).withColSpan(iDispLimits ? 10 : 8));
            }
            iITypePrinted = iPeriodPrinted = !iNewPage;
        }
    }
    
    public void printReport() throws DocumentException {
        sLog.info(MSG.statusLoadingCourses());
        TreeSet<CourseOffering> allCourses = new TreeSet(new Comparator<CourseOffering>() {
            public int compare(CourseOffering co1, CourseOffering co2) {
                int cmp = co1.getSubjectAreaAbbv().compareTo(co2.getSubjectAreaAbbv());
                if (cmp!=0) return cmp;
                cmp = co1.getCourseNbr().compareTo(co2.getCourseNbr());
                if (cmp!=0) return cmp;
                return co1.getUniqueId().compareTo(co2.getUniqueId());
            }
        });
        if (hasSubjectAreas()) {
        	for (SubjectArea subject: getSubjectAreas()) {
        		allCourses.addAll(new SessionDAO().getSession().
        				createQuery("select co from CourseOffering co where  co.subjectArea.uniqueId=:subjectAreaId").
        				setLong("subjectAreaId", subject.getUniqueId()).list());
        	}
        } else {
            allCourses.addAll(new SessionDAO().getSession().
                    createQuery("select co from CourseOffering co where  co.subjectArea.session.uniqueId=:sessionId").
                    setLong("sessionId", getSession().getUniqueId()).list());
        }
        if (allCourses.isEmpty()) return;
        sLog.info(MSG.statusLoadingClassEvents());
        Hashtable<Long,ClassEvent> class2event = new Hashtable();
        if (hasSubjectAreas()) {
        	for (SubjectArea subject: getSubjectAreas()) {
                for (Iterator i=new SessionDAO().getSession().createQuery(
                        "select c.uniqueId, e from ClassEvent e inner join e.clazz c left join fetch e.meetings m "+
                        "inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where "+
                        "co.subjectArea.uniqueId=:subjectAreaId").
                        setLong("subjectAreaId", subject.getUniqueId()).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    class2event.put((Long)o[0], (ClassEvent)o[1]);
                }        		
        	}
        } else {
            for (Iterator i=new SessionDAO().getSession().createQuery(
                    "select c.uniqueId, e from ClassEvent e inner join e.clazz c left join fetch e.meetings m "+
                    "inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where "+
                    "co.subjectArea.session.uniqueId=:sessionId").
                    setLong("sessionId", getSession().getUniqueId()).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
                class2event.put((Long)o[0], (ClassEvent)o[1]);
            }
        }
        Hashtable<Long,Integer> courseLimits = new Hashtable();
        Hashtable<Long,Integer> classLimits = new Hashtable();
        if (iDispLimits) {
            sLog.info(MSG.statusLoadingCourseLimits());
            if (hasSubjectAreas()) {
            	for (SubjectArea subject: getSubjectAreas()) {
                    for (Iterator i=new SessionDAO().getSession().createQuery(
                            "select co.uniqueId, count(distinct s.student.uniqueId) from "+
                            "StudentClassEnrollment s inner join s.courseOffering co where co.subjectArea.uniqueId=:subjectAreaId "+
                            "group by co.uniqueId").setLong("subjectAreaId", subject.getUniqueId()).list().iterator();i.hasNext();) {
                        Object[] o = (Object[])i.next();
                        courseLimits.put((Long)o[0],((Number)o[1]).intValue());
                    }
            	}
            } else {
                for (Iterator i=new SessionDAO().getSession().createQuery(
                        "select co.uniqueId, count(distinct s.student.uniqueId) from "+
                        "StudentClassEnrollment s inner join s.courseOffering co where co.subjectArea.session.uniqueId=:sessionId "+
                        "group by co.uniqueId").setLong("sessionId", getSession().getUniqueId()).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    courseLimits.put((Long)o[0],((Number)o[1]).intValue());
                }
            }
            sLog.info(MSG.statusLoadingClassLimits());
            if (hasSubjectAreas()) {
            	for (SubjectArea subject: getSubjectAreas()) {
                    for (Iterator i=new SessionDAO().getSession().createQuery(
                            "select c.uniqueId, count(distinct s.student.uniqueId) from "+
                            "StudentClassEnrollment s inner join s.clazz c inner join s.courseOffering co where co.subjectArea.uniqueId=:subjectAreaId "+
                            "group by c.uniqueId").setLong("subjectAreaId", subject.getUniqueId()).list().iterator();i.hasNext();) {
                        Object[] o = (Object[])i.next();
                        classLimits.put((Long)o[0],((Number)o[1]).intValue());
                    }
            	}
            } else {
                for (Iterator i=new SessionDAO().getSession().createQuery(
                        "select c.uniqueId, count(distinct s.student.uniqueId) from "+
                        "StudentClassEnrollment s inner join s.clazz c inner join s.courseOffering co where co.subjectArea.session.uniqueId=:sessionId "+
                        "group by c.uniqueId").setLong("sessionId", getSession().getUniqueId()).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    classLimits.put((Long)o[0],((Number)o[1]).intValue());
                }
            }
        }
        sLog.info(MSG.statusPrintingReport());
        SubjectArea subject = null;
        setHeaderLine(
        		new Line(
        				rpad(MSG.lrCourse(), 13),
        				rpad(MSG.lrTitle(), iDispLimits ? 28 : 46),
        				(iDispLimits ? lpad("", 8) : NULL),
        				(iDispLimits ? lpad("", 8) : NULL),
        				lpad(MSG.lrAlt(), 4),
        				lpad(MSG.lrLen1(), 3),
        				rpad("", 25),
        				rpad("", 11),
        				lpad("", 4),
        				lpad("", 5),
        				lpad("", 14)
        		), new Line(
        				rpad("  " + MSG.lrInsType(), 13),
        				rpad("  " + MSG.lrSections(), iDispLimits ? 28 : 46),
        				(iDispLimits ? lpad(MSG.lrLimit(), 8) : NULL),
        				(iDispLimits ? lpad(MSG.lrEnrollmt(), 8) : NULL),
        				lpad(MSG.lrSeat(), 4),
        				lpad(MSG.lrLen2(), 3),
        				rpad(MSG.lrDateAmpTime(), 25),
        				rpad(MSG.lrRoom(), 11),
        				lpad(MSG.lrCap(), 4),
        				lpad(MSG.lrExCap(), 5),
        				rpad(MSG.lrExamWith(), 14)
        		), new Line(
        				lpad("", '-', 13),
        				lpad("", '-', iDispLimits ? 28 : 46),
        				(iDispLimits ? lpad("", '-', 8) : NULL),
        				(iDispLimits ? lpad("", '-', 8) : NULL),
        				lpad("", '-', 4),
        				lpad("", '-', 3),
        				lpad("", '-', 25),
        				lpad("", '-', 11),
        				lpad("", '-', 4),
        				lpad("", '-', 5),
        				lpad("", '-', 14)
        		));
        printHeader();
        for (CourseOffering co : allCourses) {
            InstructionalOffering io = co.getInstructionalOffering();
            if(io.isNotOffered().booleanValue()){
            	continue;
            }
            if (!co.isIsControl() && co.getInstructionalOffering().getControllingCourseOffering().getSubjectArea().equals(co.getSubjectArea())) continue;
            if (subject==null) {
                subject = co.getSubjectArea();
                setFooter(subject.getSubjectAreaAbbreviation());
            } else if (!subject.equals(co.getSubjectArea())) {
                subject = co.getSubjectArea();
                setCont(null); newPage(); setFooter(subject.getSubjectAreaAbbreviation());
            }
            setPageName(co.getCourseName());
            setCont(co.getCourseName());
            TreeSet<CourseOffering> courses = new TreeSet(new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_CTRL_CRS));
            courses.addAll(co.getInstructionalOffering().getCourseOfferings());
            boolean hasCourseExam = false;
            for (CourseOffering course : courses) {
                if (!getExams(course).isEmpty()) {
                    hasCourseExam = true; break;
                }
            }
            for (CourseOffering course : courses) {
                int courseLimit = -1;
                InstructionalOffering offering = course.getInstructionalOffering();
                boolean unlimited = false;
                if (co.getReservation() != null)
                	courseLimit = co.getReservation();
                if (courseLimit<0) {
                    if (offering.getCourseOfferings().size()==1 && offering.getLimit()!=null)
                        courseLimit = offering.getLimit().intValue();
                }
                for (Iterator i=offering.getInstrOfferingConfigs().iterator();i.hasNext();) {
                    InstrOfferingConfig config = (InstrOfferingConfig)i.next();
                    if (config.isUnlimitedEnrollment().booleanValue()) unlimited=true;
                }
                Integer enrl = (iDispLimits?courseLimits.get(course.getUniqueId()):null);
                TreeSet<ExamAssignmentInfo> exams = getExams(course);
                String courseName = (course.isIsControl()?"":" ")+course.getCourseName();
                iCoursePrinted = false;
                if (exams.isEmpty()) {
                    println(
                        rpad(courseName,14).withSeparator(""),
                        rpad(course.getTitle()==null?"":course.getTitle(),(iDispLimits?28:46)).withSeparator(iDispLimits ? "" : " "),
                        (iDispLimits?lpad(courseLimit<=0?unlimited?"  inf":"":String.valueOf(courseLimit),9).withSeparator(""):NULL),
                        (iDispLimits?lpad(enrl==null || enrl<=0?"":String.valueOf(enrl),9):NULL),
                        rpad("", 4), rpad("", 3),
                        new Cell(hasCourseExam?MSG.lrNoExam():"").withColSpan(5));
                } else for (ExamAssignmentInfo exam : exams) {
                	Vector<Cell> rooms = new Vector();
                    Vector<Cell> roomCaps = new Vector();
                    Vector<Cell> roomExCaps = new Vector();
                    Vector<Cell> times = new Vector();
                    if (exam.getPeriod()==null) {
                        times.add(rpad(iHasAssignment?" "+MSG.lrExamNotAssigned():" "+MSG.lrCourseExam(),26));
                        rooms.add(rpad("", 12));
                        roomCaps.add(rpad("", 4));
                        roomExCaps.add(rpad("", 5));
                        //if (exam.getMaxRooms()==0) rooms.add(" "+rpad(iNoRoom, 22));
                        for (Iterator i=new TreeSet(exam.getExam().getPreferences()).iterator();i.hasNext();) {
                            Preference pref = (Preference)i.next();
                            if (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) || PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) {
                                String pf = (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"!");
                                if (pref instanceof ExamPeriodPref) {
                                    ExamPeriodPref xp = (ExamPeriodPref)pref;
                                    times.add(new Cell(new Cell(pf).withSeparator(""),rpad(formatPeriod(xp.getExamPeriod(),exam.getLength(), exam.getPrintOffset()), 25)));
                                } else if (exam.getMaxRooms()>0) {
                                    if (pref instanceof RoomPref) {
                                        RoomPref rp = (RoomPref)pref;
                                        rooms.add(new Cell(new Cell(pf).withSeparator(""), formatRoom(rp.getRoom())));
                                        roomCaps.add(lpad(""+rp.getRoom().getCapacity(),4));
                                        roomExCaps.add(lpad(""+rp.getRoom().getExamCapacity(),5));
                                    } else if (pref instanceof BuildingPref) {
                                        BuildingPref bp = (BuildingPref)pref;
                                        rooms.add(new Cell(new Cell(pf).withSeparator(""), rpad(bp.getBuilding().getAbbreviation(), 22)).withColSpan(3));
                                        roomCaps.add(NULL);
                                        roomExCaps.add(NULL);
                                    } else if (pref instanceof RoomFeaturePref) {
                                        RoomFeaturePref fp = (RoomFeaturePref)pref;
                                        rooms.add(new Cell(new Cell(pf).withSeparator(""), rpad(fp.getRoomFeature().getLabel(), 22)).withColSpan(3));
                                        roomCaps.add(NULL);
                                        roomExCaps.add(NULL);
                                    } else if (pref instanceof RoomGroupPref) {
                                        RoomGroupPref gp = (RoomGroupPref)pref;
                                        rooms.add(new Cell(new Cell(pf).withSeparator(""), rpad(gp.getRoomGroup().getName(), 22)).withColSpan(3));
                                        roomCaps.add(NULL);
                                        roomExCaps.add(NULL);
                                    }
                                }
                            }
                        }
                        for (Iterator i=exam.getExam().getDistributionObjects().iterator();i.hasNext();) {
                            DistributionObject dObj = (DistributionObject)i.next();
                            DistributionPref pref = dObj.getDistributionPref();
                            if (!PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) && !PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) continue;
                            String name = (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"!")+pref.getDistributionType().getAbbreviation();
                            if (name.toUpperCase().startsWith("!SAME ")) name = " Diff"+name.substring(5);
                            int line = 0;
                            for (Iterator j=new TreeSet(pref.getDistributionObjects()).iterator();j.hasNext();) {
                                DistributionObject xObj = (DistributionObject)j.next();
                                if (xObj.equals(dObj)) continue;
                                Exam x = (Exam)xObj.getPrefGroup();
                                for (Iterator k=new TreeSet(x.getOwners()).iterator();k.hasNext();) {
                                    ExamOwner own = (ExamOwner)k.next();
                                    times.add(rpad(rpad(line>0?"":name,name.length())+" "+own.getLabel(),26));
                                    line++;
                                }
                            }
                        }
                    } else {
                        if (exam.getRooms()==null || exam.getRooms().isEmpty()) {
                        	rooms.add(rpad(iNoRoom, 12));
                            roomCaps.add(rpad("", 4));
                            roomExCaps.add(rpad("", 5));
                        } else for (ExamRoomInfo room : exam.getRooms()) {
                        	rooms.add(new Cell(new Cell(" ").withSeparator(""), formatRoom(room)));
                            roomCaps.add(lpad(""+room.getCapacity(),4));
                            roomExCaps.add(lpad(""+room.getExamCapacity(),5));;
                        }
                        times.add(new Cell(new Cell(" ").withSeparator(""), rpad(formatPeriod(exam),25)));
                    }
                    Vector<Cell> meetsWith = new Vector();
                    int cnt = 0;
                    int maxCnt = Math.max(4,Math.max(rooms.size(), times.size())-1);
                    for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                        if (section.getOwnerType()==ExamOwner.sOwnerTypeCourse && course.getUniqueId().equals(section.getOwnerId())) continue;
                        if (section.getOwnerType()==ExamOwner.sOwnerTypeOffering && course.getInstructionalOffering().getUniqueId().equals(section.getOwnerId())) continue;
                        if (cnt>=maxCnt) {
                            meetsWith.add(rpad("...",14)); break;
                        }
                        if (iItype)
                            meetsWith.add(rpad(section.getName(),14));
                        else
                            meetsWith.add(new Cell(
                                    rpad(section.getSubject(),4),
                                    rpad(section.getCourseNbr(),5),
                                    rpad(section.getSection(),3)));
                        cnt++;
                    }

                    int nrLines = Math.max(Math.max(rooms.size(), meetsWith.size()),times.size());
                    for (int idx = 0; idx < nrLines; idx++) {
                    	Cell room = (idx<rooms.size()?rooms.elementAt(idx):rpad("",11));
                        Cell roomCap = (idx<roomCaps.size()?roomCaps.elementAt(idx):rpad("",4));
                        Cell roomExCap = (idx<roomExCaps.size()?roomExCaps.elementAt(idx):rpad("",5));
                        Cell mw = (idx<meetsWith.size()?meetsWith.elementAt(idx):new Cell(""));
                        Cell time = (idx<times.size()?times.elementAt(idx):rpad("",26));
                        println(rpad(idx>0 || iCoursePrinted?"":courseName,14).withSeparator(""),
                                rpad(idx>0 || iCoursePrinted?"":course.getTitle()==null?"":course.getTitle(),(iDispLimits?28:46)).withSeparator(iDispLimits ? "" : " "),
                                (iDispLimits?lpad(idx>0 || iCoursePrinted?"":courseLimit<=0?unlimited?"  inf":"":String.valueOf(courseLimit),9).withSeparator(""):NULL),
                                (iDispLimits?lpad(idx>0 || iCoursePrinted || enrl==null || enrl<=0?"":String.valueOf(enrl),9):NULL),
                                lpad(idx>0?"":exam.getSeatingType()==Exam.sSeatingTypeExam?MSG.lrYes():MSG.lrNo(),4),
                                lpad(idx>0?"":String.valueOf(exam.getLength()),3).withSeparator(""),
                                time.withSeparator(""),
                                room, roomCap, roomExCap,
                                mw
                                );
                    }
                    iCoursePrinted = !iNewPage;
                }
            }
            TreeSet<SchedulingSubpart> subparts = new TreeSet(new SchedulingSubpartComparator());
            for (Iterator i=co.getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
                InstrOfferingConfig cfg = (InstrOfferingConfig)i.next(); subparts.addAll(cfg.getSchedulingSubparts());
            }
            boolean hasSubpartExam = false; InstrOfferingConfig cfg = null;
            for (SchedulingSubpart subpart : subparts) {
            	if (iSkipSuffixSubparts && subpart.getParentSubpart() != null && subpart.getItype().equals(subpart.getParentSubpart().getItype())) {
            		continue;
            	}
                if (cfg==null) {
                    cfg = subpart.getInstrOfferingConfig();
                } else if (!cfg.equals(subpart.getInstrOfferingConfig())) {
                    cfg = subpart.getInstrOfferingConfig();
                    hasSubpartExam = false;
                }
                iITypePrinted = false;
                TreeSet<Class_> classes = new TreeSet(new Comparator<Class_>() {
                   public int compare(Class_ c1, Class_ c2) {
                       if (iUseClassSuffix) {
                           String sx1 = c1.getClassSuffix();
                           String sx2 = c2.getClassSuffix();
                           if (sx1!=null && sx2!=null) return sx1.compareTo(sx2);
                       }
                       return c1.getSectionNumber().compareTo(c2.getSectionNumber());
                   }
                });
                classes.addAll(subpart.getClasses());
                String mw = null;
                String message = null;
                TreeSet<ExamAssignmentInfo> exams = null;
                int minEnrl = 0, maxEnrl = 0, minLimit = 0, maxLimit = 0;
                Vector<Class_> same = new Vector();
                boolean hasSectionExam = false, allSectionsHaveExam = true;
                for (Class_ clazz : classes) {
                    if (!getExams(clazz).isEmpty()) {
                        hasSectionExam = true;
                    } else {
                        allSectionsHaveExam = false;
                    }
                }
                if (allSectionsHaveExam) hasSubpartExam = true;
                for (Class_ clazz : classes) {
                    Integer enrl = (iDispLimits?classLimits.get(clazz.getUniqueId()):null);
                    if (!same.isEmpty() && 
                            (iSkipHoles || same.lastElement().getSectionNumber()+1==clazz.getSectionNumber()) && 
                            (!iDispNote || ToolBox.equals(clazz.getSchedulePrintNote(), same.lastElement().getSchedulePrintNote())) && 
                            exams.equals(getExams(clazz)) && 
                            mw.equals(getMeetWith(clazz, null)) &&
                            message.equals(getMessage(clazz, hasCourseExam || hasSubpartExam, hasSectionExam, class2event))) {
                        minEnrl = Math.min(minEnrl, (enrl==null?0:enrl.intValue()));
                        maxEnrl = Math.max(maxEnrl, (enrl==null?0:enrl.intValue()));
                        minLimit = Math.min(minLimit, clazz.getClassLimit());
                        maxLimit = Math.max(maxLimit, clazz.getClassLimit());
                        message = getMessage(clazz, hasCourseExam || hasSubpartExam, hasSectionExam, class2event);
                        same.add(clazz);
                        continue;
                    }
                    if (!same.isEmpty()) {
                        print(same, hasCourseExam, hasSectionExam, minLimit, maxLimit, minEnrl, maxEnrl, class2event);
                        same.clear();
                    }
                    exams = getExams(clazz);
                    mw = getMeetWith(clazz, null);
                    minEnrl = maxEnrl = (enrl==null?0:enrl.intValue());
                    minLimit = maxLimit = clazz.getClassLimit();
                    message = getMessage(clazz, hasCourseExam || hasSubpartExam, hasSectionExam, class2event);
                    same.add(clazz);
                }
                if (!same.isEmpty()) print(same, hasCourseExam || hasSubpartExam, hasSectionExam, minLimit, maxLimit, minEnrl, maxEnrl, class2event);

            }
            if (!iNewPage) println(new Line());
        }
        lastPage();
    }
}
