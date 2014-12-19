/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import org.apache.log4j.Logger;
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
    protected static Logger sLog = Logger.getLogger(ExamVerificationReport.class);
    private boolean iSkipHoles = true;
    // Skip subparts of the same itype as parent subpart
    private boolean iSkipSuffixSubparts = ApplicationProperty.ExaminationPdfReportsSkipSuffixSubpart.isTrue();
    private boolean iHasAssignment = false;
    
    public ExamVerificationReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "EXAMINATION VERIFICATION REPORT", session, examType, subjectAreas, exams);
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
        String message = "** NO EXAM **";
        if (hasCourseExam && !hasSectionExam) message = ""; // Has other exam
        if (!hasSectionExam && !clazz.getSchedulingSubpart().getItype().isOrganized()) message = "Not organized instructional type";
        else {
            ClassEvent event = class2event.get(clazz.getUniqueId());
            if (event==null || event.getMeetings().isEmpty()) {
                message = "Class not organized";
            } else if (!isFullTerm(event)) {
                TreeSet meetings = new TreeSet(event.getMeetings());
                Meeting first = (Meeting)meetings.first();
                Meeting last = (Meeting)meetings.last();
                SimpleDateFormat df = new SimpleDateFormat("MM/dd");
                message = "Class not full-term ("+df.format(first.getMeetingDate())+(first.getMeetingDate().equals(last.getMeetingDate())?"":" - "+df.format(last.getMeetingDate()))+")";
            }
        }
        return message;
    }

    private void print(Vector<Class_> same, boolean hasCourseExam, boolean hasSectionExam, int minLimit, int maxLimit, int minEnrl, int maxEnrl, Hashtable<Long,ClassEvent> class2event) throws DocumentException {
        String cmw = getMeetWith(same.firstElement(),same);
        TreeSet<ExamAssignmentInfo> exams = getExams(same.firstElement());
        iPeriodPrinted = false;
        if (exams.isEmpty()) {
            String message = "** NO EXAM **";
            if (hasCourseExam && !hasSectionExam) message = ""; // Has other exam
            if (!hasSectionExam && !same.firstElement().getSchedulingSubpart().getItype().isOrganized()) message = "Not organized instructional type";
            else {
                ClassEvent classEvent = class2event.get(same.firstElement().getUniqueId());
                if (classEvent==null || classEvent.getMeetings().isEmpty()) {
                    message = "Class not organized";
                } else if (!isFullTerm(classEvent)) {
                    TreeSet meetings = new TreeSet(classEvent.getMeetings());
                    Meeting first = (Meeting)meetings.first();
                    Meeting last = (Meeting)meetings.last();
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd");
                    message = "Class not full-term ("+df.format(first.getMeetingDate())+(first.getMeetingDate().equals(last.getMeetingDate())?"":" - "+df.format(last.getMeetingDate()))+")";
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
            boolean mwSameLine = hasMw && !titleSameLine && (" m/w "+cmw).length()<=((iDispLimits?28:46)-formatSection(same).length()-(same.size()>1?" ("+same.size()+")":"").length());
            boolean mwSeparateLine = hasMw && !mwSameLine;
            if ((titleSeparateLine || mwSeparateLine) && getLineNumber()+1+(titleSeparateLine?0:1)+(mwSeparateLine?1:0)>iNrLines) newPage();
            println(
                    lpad(iITypePrinted?"":same.firstElement().getSchedulingSubpart().getItypeDesc().trim(),13)+" "+
                    rpad(formatSection(same)+(same.size()>1?" ("+same.size()+")":"")+
                            (titleSameLine?" "+title:"")+(mwSameLine?" m/w "+cmw:""),(iDispLimits?28:46))+
                    (iDispLimits?lpad(maxLimit<=0?"":minLimit!=maxLimit?minLimit+"-"+maxLimit:""+minLimit,9)+lpad(maxEnrl<=0?"":minEnrl!=maxEnrl?minEnrl+"-"+maxEnrl:""+minEnrl,9)+" ":" ")+
                    "         "+message);
            if (titleSeparateLine)
                println(lpad("",13)+"  "+(title.length()>118?title.substring(0,115)+"...":title));
            if (mwSeparateLine)
                println(lpad("",13)+"  Meets with "+(cmw.length()>107?cmw.substring(0,104)+"...":cmw));
            iITypePrinted = !iNewPage;
        } else for (ExamAssignmentInfo exam : exams) {
            Vector<String> rooms = new Vector();
            Vector<String> times = new Vector();
            if (exam.getPeriod()==null) {
                times.add(rpad(iHasAssignment?" Exam not assigned":" Section exam",26));
                rooms.add(rpad("", 23));
                //if (exam.getMaxRooms()==0) rooms.add(" "+rpad(iNoRoom, 22));
                for (Iterator i=new TreeSet(exam.getExam().getPreferences()).iterator();i.hasNext();) {
                    Preference pref = (Preference)i.next();
                    if (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) || PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) {
                        String pf = (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"!");
                        if (pref instanceof ExamPeriodPref) {
                            ExamPeriodPref xp = (ExamPeriodPref)pref;
                            times.add(pf+rpad(formatPeriod(xp.getExamPeriod(), exam.getLength(), exam.getPrintOffset()), 25));
                        } else if (exam.getMaxRooms()>0) {
                            if (pref instanceof RoomPref) {
                                RoomPref rp = (RoomPref)pref;
                                rooms.add(pf+formatRoom(rp.getRoom().getLabel())+" "+
                                        lpad(""+rp.getRoom().getCapacity(),4)+" "+
                                        lpad(""+rp.getRoom().getExamCapacity(),5));
                            } else if (pref instanceof BuildingPref) {
                                BuildingPref bp = (BuildingPref)pref;
                                rooms.add(pf+rpad(bp.getBuilding().getAbbreviation(), 22));
                            } else if (pref instanceof RoomFeaturePref) {
                                RoomFeaturePref fp = (RoomFeaturePref)pref;
                                rooms.add(pf+rpad(fp.getRoomFeature().getLabel(), 22));
                            } else if (pref instanceof RoomGroupPref) {
                                RoomGroupPref gp = (RoomGroupPref)pref;
                                rooms.add(pf+rpad(gp.getRoomGroup().getName(), 22));
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
                    rooms.add(" "+rpad(iNoRoom, 22));
                } else for (ExamRoomInfo room : exam.getRooms()) {
                    rooms.add(" "+formatRoom(room.getName())+" "+
                            lpad(""+room.getCapacity(),4)+" "+
                            lpad(""+room.getExamCapacity(),5));
                }
                times.add(" "+rpad(formatPeriod(exam),25));
            }
            Vector<String> meetsWith = new Vector();
            int cnt = 0;
            int maxCnt = Math.max(4,Math.max(rooms.size(), times.size())-1);
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (section.getOwnerType()==ExamOwner.sOwnerTypeClass && same.contains(section.getOwner().getOwnerObject())) continue;
                if (section.getOwnerType()==ExamOwner.sOwnerTypeConfig && section.getOwnerId().equals(same.firstElement().getSchedulingSubpart().getInstrOfferingConfig().getUniqueId())) continue;
                if (cnt>=maxCnt) {
                    meetsWith.add(" "+rpad("...",14)); break;
                }
                if (iItype)
                    meetsWith.add(" "+rpad(section.getName(),14));
                else
                    meetsWith.add(" "+
                            rpad(section.getSubject(),4)+" "+
                            rpad(section.getCourseNbr(),5)+" "+
                            rpad(section.getSection(),3));
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
            boolean mwSameLine = hasMw && !titleSameLine && (" m/w "+cmw).length()<=((iDispLimits?28:46)-formatSection(same).length()-(same.size()>1?" ("+same.size()+")":"").length());
            boolean mwSecondLine = hasMw && !mwSameLine && !titleSecondLine && nrLines>1 && (" Meets with "+cmw).length()<=(iDispLimits?28:46);
            boolean mwThirdLine = hasMw && !mwSameLine && titleSecondLine && nrLines>2 && (" Meets with "+cmw).length()<=(iDispLimits?28:46);
            boolean mwSeparateLine = hasMw && !mwSameLine && !mwSecondLine && !mwThirdLine;
            if (getLineNumber()+nrLines+(mwSeparateLine?1:0)+(titleSeparateLine?1:0)>iNrLines) newPage();
            
            for (int idx = 0; idx < nrLines; idx++) {
                String room = (idx<rooms.size()?rooms.elementAt(idx):rpad("",23));
                String mw = (idx<meetsWith.size()?meetsWith.elementAt(idx):"");
                String time = (idx<times.size()?times.elementAt(idx):rpad("",26));
                println(lpad(idx>0 || iITypePrinted?"":same.firstElement().getSchedulingSubpart().getItypeDesc().trim(),13)+" "+
                        rpad(iPeriodPrinted?"":idx>0 ? (idx==1 && mwSecondLine?" Meets with "+cmw:"")+
                                     (idx==1 && titleSecondLine?" "+title:"")+
                                     (idx==2 && mwThirdLine?" Meets with "+cmw:"")
                                   : formatSection(same)+(same.size()>1?" ("+same.size()+")":"")+
                                     (titleSameLine?" "+title:"")+
                                     (mwSameLine?" m/w "+cmw:"")
                                   ,(iDispLimits?28:46))+
                        (iDispLimits?lpad(iPeriodPrinted || idx>0 || maxLimit<=0?"":minLimit!=maxLimit?minLimit+"-"+maxLimit:""+minLimit,9)+
                        lpad(iPeriodPrinted || idx>0 || maxEnrl<=0?"":minEnrl!=maxEnrl?minEnrl+"-"+maxEnrl:""+minEnrl,9)+" ":" ")+
                        lpad(idx>0?"":exam.getSeatingType()==Exam.sSeatingTypeExam?"yes":"no",4)+" "+
                        lpad(idx>0?"":String.valueOf(exam.getLength()),3)+time+room+mw
                        );
                if (idx==0 && titleSeparateLine)
                    println(lpad("",13)+"  "+(title.length()>118?title.substring(0,115)+"...":title));
                if (idx==0 && mwSeparateLine)
                    println(lpad("",13)+"  Meets with "+(cmw.length()>107?cmw.substring(0,104)+"...":cmw));
            }
            iITypePrinted = iPeriodPrinted = !iNewPage;
        }
    }
    
    public void printReport() throws DocumentException {
        sLog.info("  Loading courses ...");
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
        sLog.info("  Loading class events...");
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
            sLog.info("  Loading course limits ...");
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
            sLog.info("  Loading class limits ...");
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
        sLog.info("  Printing report ...");
        SubjectArea subject = null;
        setHeader(new String[] {
                "Course        Title                       "+(iDispLimits?"                  ":"                  ")+" Alt  Len                                                  ",
                "   InsType      Sections                  "+(iDispLimits?" Limit    Enrollmt":"                  ")+" Seat gth Date & Time               Room         Cap ExCap Exam with",
                "------------- ----------------------------"+(iDispLimits?" -------- --------":"------------------")+" ---- --- ------------------------- ----------- ---- ----- --------------"});
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
                newPage(); setFooter(subject.getSubjectAreaAbbreviation());
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
                        rpad(courseName,14)+
                        rpad(course.getTitle()==null?"":course.getTitle(),(iDispLimits?28:46))+
                        (iDispLimits?lpad(courseLimit<=0?unlimited?"  inf":"":String.valueOf(courseLimit),9)+lpad(enrl==null || enrl<=0?"":String.valueOf(enrl),9)+" ":" ")+
                        "         "+(hasCourseExam?"** NO EXAM**":""));
                } else for (ExamAssignmentInfo exam : exams) {
                    Vector<String> rooms = new Vector();
                    Vector<String> times = new Vector();
                    if (exam.getPeriod()==null) {
                        times.add(rpad(iHasAssignment?" Exam not assigned":" Course Exam",26));
                        rooms.add(rpad("", 23));
                        //if (exam.getMaxRooms()==0) rooms.add(" "+rpad(iNoRoom, 22));
                        for (Iterator i=new TreeSet(exam.getExam().getPreferences()).iterator();i.hasNext();) {
                            Preference pref = (Preference)i.next();
                            if (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) || PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) {
                                String pf = (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"!");
                                if (pref instanceof ExamPeriodPref) {
                                    ExamPeriodPref xp = (ExamPeriodPref)pref;
                                    times.add(pf+rpad(formatPeriod(xp.getExamPeriod(),exam.getLength(), exam.getPrintOffset()), 25));
                                } else if (exam.getMaxRooms()>0) {
                                    if (pref instanceof RoomPref) {
                                        RoomPref rp = (RoomPref)pref;
                                        rooms.add(pf+formatRoom(rp.getRoom().getLabel())+" "+
                                                lpad(""+rp.getRoom().getCapacity(),4)+" "+
                                                lpad(""+rp.getRoom().getExamCapacity(),5));
                                    } else if (pref instanceof BuildingPref) {
                                        BuildingPref bp = (BuildingPref)pref;
                                        rooms.add(pf+rpad(bp.getBuilding().getAbbreviation(), 22));
                                    } else if (pref instanceof RoomFeaturePref) {
                                        RoomFeaturePref fp = (RoomFeaturePref)pref;
                                        rooms.add(pf+rpad(fp.getRoomFeature().getLabel(), 22));
                                    } else if (pref instanceof RoomGroupPref) {
                                        RoomGroupPref gp = (RoomGroupPref)pref;
                                        rooms.add(pf+rpad(gp.getRoomGroup().getName(), 22));
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
                            rooms.add(" "+rpad(iNoRoom, 22));
                        } else for (ExamRoomInfo room : exam.getRooms()) {
                            rooms.add(" "+formatRoom(room.getName())+" "+
                                    lpad(""+room.getCapacity(),4)+" "+
                                    lpad(""+room.getExamCapacity(),5));
                        }
                        times.add(" "+rpad(formatPeriod(exam),25));
                    }
                    Vector<String> meetsWith = new Vector();
                    int cnt = 0;
                    int maxCnt = Math.max(4,Math.max(rooms.size(), times.size())-1);
                    for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                        if (section.getOwnerType()==ExamOwner.sOwnerTypeCourse && course.getUniqueId().equals(section.getOwnerId())) continue;
                        if (section.getOwnerType()==ExamOwner.sOwnerTypeOffering && course.getInstructionalOffering().getUniqueId().equals(section.getOwnerId())) continue;
                        if (cnt>=maxCnt) {
                            meetsWith.add(" "+rpad("...",14)); break;
                        }
                        if (iItype)
                            meetsWith.add(" "+rpad(section.getName(),14));
                        else
                            meetsWith.add(" "+
                                    rpad(section.getSubject(),4)+" "+
                                    rpad(section.getCourseNbr(),5)+" "+
                                    rpad(section.getSection(),3));
                        cnt++;
                    }

                    int nrLines = Math.max(Math.max(rooms.size(), meetsWith.size()),times.size());
                    for (int idx = 0; idx < nrLines; idx++) {
                        String room = (idx<rooms.size()?rooms.elementAt(idx):rpad("",23));
                        String mw = (idx<meetsWith.size()?meetsWith.elementAt(idx):"");
                        String time = (idx<times.size()?times.elementAt(idx):rpad("",26));
                        println(rpad(idx>0 || iCoursePrinted?"":courseName,14)+
                                rpad(idx>0 || iCoursePrinted?"":course.getTitle()==null?"":course.getTitle(),(iDispLimits?28:46))+
                                (iDispLimits?lpad(idx>0 || iCoursePrinted?"":courseLimit<=0?unlimited?"  inf":"":String.valueOf(courseLimit),9)+
                                        lpad(idx>0 || iCoursePrinted || enrl==null || enrl<=0?"":String.valueOf(enrl),9)+" ":" ")+
                                lpad(idx>0?"":exam.getSeatingType()==Exam.sSeatingTypeExam?"yes":"no",4)+" "+
                                lpad(idx>0?"":String.valueOf(exam.getLength()),3)+
                                time+room+mw
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
            if (!iNewPage) println("");
        }
        lastPage();
    }
}
