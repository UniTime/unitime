package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
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
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class ExamVerificationReport extends PdfLegacyExamReport {
    private CourseOffering iCourseOffering = null;

    public ExamVerificationReport(File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(file, "EXAMINATION REQUESTS", session, examType, subjectArea, exams);
    }
    
    public TreeSet<ExamAssignmentInfo> getExams(CourseOffering course) {
        TreeSet<ExamAssignmentInfo> exams = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            for (ExamSectionInfo section : exam.getSections()) {
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
            for (ExamSectionInfo section : exam.getSections()) {
                if (section.getOwnerType()==ExamOwner.sOwnerTypeClass && section.getOwnerId().equals(clazz.getUniqueId()))
                    exams.add(exam);
                if (section.getOwnerType()==ExamOwner.sOwnerTypeConfig && section.getOwnerId().equals(clazz.getSchedulingSubpart().getInstrOfferingConfig().getUniqueId()))
                    exams.add(exam);
            }
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
    
    public String getMeetWith(Class_ clazz, boolean exclude) {
        TreeSet<Class_> classes = new TreeSet(new Comparator<Class_>() {
            public int compare(Class_ c1, Class_ c2) {
                if (c1.getSchedulingSubpart().equals(c2.getSchedulingSubpart())) {
                    String sx1 = c1.getClassSuffix();
                    String sx2 = c2.getClassSuffix();
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
                if (exclude && xObj.equals(dObj)) continue;
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
            if (prev.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().equals(c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea())) {
                //same subject area
                if (prev.getSchedulingSubpart().getControllingCourseOffering().equals(c.getSchedulingSubpart().getControllingCourseOffering())) {
                    //same course number
                    if (prev.getSchedulingSubpart().equals(c.getSchedulingSubpart()))
                        ret+=genName(ApplicationProperties.getProperty("tmtbl.exam.name.sameSubpart.Class"),c);
                    else
                        ret+=genName(ApplicationProperties.getProperty("tmtbl.exam.name.sameCourse.Class"),c);
                } else {
                    //different course number
                    ret+=genName(ApplicationProperties.getProperty("tmtbl.exam.name.sameSubject.Class"),c);
                }
            } else {
                if (ret.length()>0) ret+=genName(ApplicationProperties.getProperty("tmtbl.exam.name.diffSubject.separator"),prev);
                ret+=genName(ApplicationProperties.getProperty("tmtbl.exam.name.Class"),c);
            }
            prev = c;
        }
        return ret;
    }
    
    private String formatSection(Class_ clazz) { 
        return (clazz.getClassSuffix()==null || clazz.getClassSuffix().length()==0?clazz.getSectionNumberString():clazz.getClassSuffix());
    }
    
    private String formatSection(Vector<Class_>  classes) {
        if (classes.isEmpty()) return "";
        if (classes.size()==1) return formatSection(classes.firstElement());
        return formatSection(classes.firstElement())+" - "+formatSection(classes.lastElement());
    }
    
    private boolean sameExams(TreeSet<ExamAssignmentInfo> x1, TreeSet<ExamAssignmentInfo> x2) {
        if (x1.equals(x2)) return true;
        if (x1.size()!=x2.size()) return false;
        return false;
    }

    private void print(Vector<Class_> same, boolean hasCourseExam, boolean hasSectionExam, int minLimit, int maxLimit, int minEnrl, int maxEnrl) throws DocumentException {
        String cmw = getMeetWith(same.firstElement(),true);
        TreeSet<ExamAssignmentInfo> exams = getExams(same.firstElement());
        if (exams.isEmpty()) {
            String message = "** NO SECTION EXAM **";
            if (!hasSectionExam && !same.firstElement().getSchedulingSubpart().getItype().isOrganized()) message = "Not organized instructional type";
            if (hasCourseExam && !hasSectionExam) message = "Has course exam";
            if (cmw.length()>0 && getLineNumber()+2>sNrLines) newPage();
            println(
                    lpad(iITypePrinted?"":same.firstElement().getSchedulingSubpart().getItypeDesc(),11)+" "+
                    rpad(formatSection(same)+(same.size()>1?" ("+same.size()+" classes)":""),28)+" "+
                    lpad(maxLimit<=0?"":minLimit!=maxLimit?minLimit+"-"+maxLimit:""+minLimit,9)+" "+
                    lpad(maxEnrl<=0?"":minEnrl!=maxEnrl?minEnrl+"-"+maxEnrl:""+minEnrl,9)+" "+
                    "         "+message);
            if (cmw.length()>0)
                println(lpad("",11)+"  Meets with "+cmw);
            iITypePrinted = !iNewPage;
        } else for (ExamAssignmentInfo exam : exams) {
            Vector<String> rooms = new Vector();
            Vector<String> times = new Vector();
            if (exam.getPeriod()==null) {
                times.add(rpad(" Exam not assigned",26));
                rooms.add(rpad("", 23));
                if (exam.getMaxRooms()==0) rooms.add(" "+rpad(iNoRoom, 22));
                for (Iterator i=exam.getExam().getPreferences().iterator();i.hasNext();) {
                    Preference pref = (Preference)i.next();
                    if (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) || PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) {
                        String pf = (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"*");
                        if (pref instanceof ExamPeriodPref) {
                            ExamPeriodPref xp = (ExamPeriodPref)pref;
                            times.add(pf+rpad(formatPeriod(xp.getExamPeriod()), 25));
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
                    for (Iterator j=new TreeSet(pref.getDistributionObjects()).iterator();j.hasNext();) {
                        DistributionObject xObj = (DistributionObject)j.next();
                        if (xObj.equals(dObj)) continue;
                        Exam x = (Exam)xObj.getPrefGroup();
                        for (Iterator k=new TreeSet(x.getOwners()).iterator();k.hasNext();) {
                            ExamOwner own = (ExamOwner)k.next();
                            times.add(
                                    (line>0 || PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"*")+
                                    rpad((line==0?pref.getDistributionType().getAbbreviation():rpad("",pref.getDistributionType().getAbbreviation().length()))+" "+own.getLabel(),25));
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
                times.add(" "+rpad(formatPeriod(exam.getPeriod()),25));
            }
            Vector<String> meetsWith = new Vector();
            int cnt = 0;
            int maxCnt = Math.max(4,Math.max(rooms.size(), times.size())-1);
            for (ExamSectionInfo section : exam.getSections()) {
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
            if (cmw.length()>0 && nrLines==1) nrLines = 2;
            if (getLineNumber()+nrLines>sNrLines) newPage();
            for (int idx = 0; idx < nrLines; idx++) {
                String room = (idx<rooms.size()?rooms.elementAt(idx):rpad("",23));
                String mw = (idx<meetsWith.size()?meetsWith.elementAt(idx):"");
                String time = (idx<times.size()?times.elementAt(idx):rpad("",26));
                println(lpad(idx>0 || iITypePrinted?"":same.firstElement().getSchedulingSubpart().getItypeDesc(),11)+" "+
                        rpad(idx>0 ? (idx==1 && cmw.length()>0?" Meets with "+cmw:""):formatSection(same)+(cmw.length()>0?" m/w "+cmw:""),28)+" "+
                        lpad(idx>0 || maxLimit<=0?"":minLimit!=maxLimit?minLimit+"-"+maxLimit:""+minLimit,9)+" "+
                        lpad(idx>0 || maxEnrl<=0?"":minEnrl!=maxEnrl?minEnrl+"-"+maxEnrl:""+minEnrl,9)+" "+
                        lpad(idx>0?"":exam.getSeatingType()==Exam.sSeatingTypeExam?"yes":"no",4)+" "+
                        lpad(idx>0?"":String.valueOf(exam.getLength()),3)+time+room+mw
                        );
            }
            iITypePrinted = !iNewPage;
        }
    }
    
    public void printReport() throws DocumentException {
        System.out.println("Loading courses ...");
        TreeSet<CourseOffering> allCourses = new TreeSet(new Comparator<CourseOffering>() {
            public int compare(CourseOffering co1, CourseOffering co2) {
                int cmp = co1.getCourseNbr().compareTo(co2.getCourseNbr());
                if (cmp!=0) return cmp;
                return co1.getUniqueId().compareTo(co2.getUniqueId());
            }
        });
        if (getSubjectArea()!=null)
            allCourses.addAll(new SessionDAO().getSession().
                createQuery("select co from CourseOffering co where  co.subjectArea.uniqueId=:subjectAreaId").
                setLong("subjectAreaId", getSubjectArea().getUniqueId()).list());
        else
            allCourses.addAll(new SessionDAO().getSession().
                    createQuery("select co from CourseOffering co where  co.subjectArea.session.uniqueId=:sessionId").
                    setLong("sessionId", getSession().getUniqueId()).list());
        if (allCourses.isEmpty()) return;
        System.out.println("Printing report ...");
        SubjectArea subject = null;
        setHeader(new String[] {
                "Course      Title                                            Alt  Len                                                  ",
                "   InsType    Sections                   Limit     Enrollmnt Seat ght Date & Time               Room         Cap ExCap Meets with",
                "----------- ---------------------------- --------- --------- ---- --- ------------------------- ----------- ---- ----- --------------"});
        printHeader();
        for (CourseOffering co : allCourses) {
            InstructionalOffering io = co.getInstructionalOffering();
            if (!co.isIsControl() && co.getInstructionalOffering().getControllingCourseOffering().getSubjectArea().equals(co.getSubjectArea())) continue;
            if (subject==null) {
                subject = co.getSubjectArea();
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
                for (Iterator i=offering.getCourseReservations().iterator();i.hasNext();) {
                    CourseOfferingReservation r = (CourseOfferingReservation)i.next();
                    if (r.getCourseOffering().equals(co))
                        courseLimit = r.getReserved().intValue();
                }
                if (courseLimit<0) {
                    if (offering.getCourseOfferings().size()==1 && offering.getLimit()!=null)
                        courseLimit = offering.getLimit().intValue();
                }
                for (Iterator i=offering.getInstrOfferingConfigs().iterator();i.hasNext();) {
                    InstrOfferingConfig config = (InstrOfferingConfig)i.next();
                    if (config.isUnlimitedEnrollment().booleanValue()) unlimited=true;
                }
                int enrl = 
                    ((Number)new _RootDAO().getSession().createQuery(
                            "select count(*) from StudentClassEnrollment s where s.courseOffering.uniqueId=:courseId")
                            .setLong("courseId", course.getUniqueId()).uniqueResult()).intValue();
                TreeSet<ExamAssignmentInfo> exams = getExams(course);
                String courseName = (course.isIsControl()?"":" ")+course.getCourseName();
                iCoursePrinted = false;
                if (exams.isEmpty()) {
                    println(
                        rpad(courseName,11)+" "+
                        rpad(course.getTitle()==null?"":course.getTitle(),28)+" "+
                        lpad(courseLimit<=0?unlimited?"  inf":"":String.valueOf(courseLimit),9)+" "+
                        lpad(enrl<=0?"":String.valueOf(enrl),9)+" "+
                        "         "+(hasCourseExam?"** NO COURSE EXAM**":""));
                } else for (ExamAssignmentInfo exam : exams) {
                    Vector<String> rooms = new Vector();
                    Vector<String> times = new Vector();
                    if (exam.getPeriod()==null) {
                        times.add(rpad(" Exam not assigned",26));
                        rooms.add(rpad("", 23));
                        if (exam.getMaxRooms()==0) rooms.add(" "+rpad(iNoRoom, 22));
                        for (Iterator i=exam.getExam().getPreferences().iterator();i.hasNext();) {
                            Preference pref = (Preference)i.next();
                            if (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog()) || PreferenceLevel.sProhibited.equals(pref.getPrefLevel().getPrefProlog())) {
                                String pf = (PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"*");
                                if (pref instanceof ExamPeriodPref) {
                                    ExamPeriodPref xp = (ExamPeriodPref)pref;
                                    times.add(pf+rpad(formatPeriod(xp.getExamPeriod()), 25));
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
                            for (Iterator j=new TreeSet(pref.getDistributionObjects()).iterator();j.hasNext();) {
                                DistributionObject xObj = (DistributionObject)j.next();
                                if (xObj.equals(dObj)) continue;
                                Exam x = (Exam)xObj.getPrefGroup();
                                for (Iterator k=new TreeSet(x.getOwners()).iterator();k.hasNext();) {
                                    ExamOwner own = (ExamOwner)k.next();
                                    times.add(
                                            (line>0 || PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())?" ":"*")+
                                            rpad((line==0?pref.getDistributionType().getAbbreviation():rpad("",pref.getDistributionType().getAbbreviation().length()))+" "+own.getLabel(),25));
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
                        times.add(" "+rpad(formatPeriod(exam.getPeriod()),25));
                    }
                    Vector<String> meetsWith = new Vector();
                    int cnt = 0;
                    int maxCnt = Math.max(4,Math.max(rooms.size(), times.size())-1);
                    for (ExamSectionInfo section : exam.getSections()) {
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
                        println(rpad(idx>0 || iCoursePrinted?"":courseName,11)+" "+
                                rpad(idx>0 || iCoursePrinted?"":course.getTitle()==null?"":course.getTitle(),28)+" "+
                                lpad(idx>0 || iCoursePrinted?"":courseLimit<=0?unlimited?"  inf":"":String.valueOf(courseLimit),9)+" "+
                                lpad(idx>0 || iCoursePrinted || enrl<=0?"":String.valueOf(enrl),9)+" "+
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
                if (cfg==null) {
                    cfg = subpart.getInstrOfferingConfig();
                } else if (!cfg.equals(subpart.getInstrOfferingConfig())) {
                    cfg = subpart.getInstrOfferingConfig();
                    hasSubpartExam = false;
                }
                iITypePrinted = false;
                TreeSet<Class_> classes = new TreeSet(new Comparator<Class_>() {
                   public int compare(Class_ c1, Class_ c2) {
                       String sx1 = c1.getClassSuffix();
                       String sx2 = c2.getClassSuffix();
                       if (sx1!=null && sx2!=null) return sx1.compareTo(sx2);
                       return c1.getSectionNumber().compareTo(c2.getSectionNumber());
                   }
                });
                classes.addAll(subpart.getClasses());
                String mw = null;
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
                if (allSectionsHaveExam && classes.size()>1) hasSubpartExam = true;
                for (Class_ clazz : classes) {
                    int enrl = 
                        ((Number)new _RootDAO().getSession().createQuery(
                                "select count(*) from StudentClassEnrollment s where s.clazz.uniqueId=:classId")
                                .setLong("classId", clazz.getUniqueId()).uniqueResult()).intValue();
                    if (!same.isEmpty() && same.lastElement().getSectionNumber()+1==clazz.getSectionNumber() && exams.equals(getExams(clazz)) && mw.equals(getMeetWith(clazz, false))) {
                        minEnrl = Math.min(minEnrl, enrl);
                        maxEnrl = Math.max(maxEnrl, enrl);
                        minLimit = Math.min(minLimit, clazz.getClassLimit());
                        maxLimit = Math.max(maxLimit, clazz.getClassLimit());
                        same.add(clazz);
                        continue;
                    }
                    if (!same.isEmpty()) {
                        print(same, hasCourseExam || hasSubpartExam, hasSectionExam, minLimit, maxLimit, minEnrl, maxEnrl);
                        same.clear();
                    }
                    exams = getExams(clazz);
                    mw = getMeetWith(clazz, false);
                    minEnrl = maxEnrl = enrl;
                    minLimit = maxLimit = clazz.getClassLimit();
                    same.add(clazz);
                }
                if (!same.isEmpty()) print(same, hasCourseExam || hasSubpartExam, hasSectionExam, minLimit, maxLimit, minEnrl, maxEnrl);

            }
            if (!iNewPage) println("");
        }
    }

}
