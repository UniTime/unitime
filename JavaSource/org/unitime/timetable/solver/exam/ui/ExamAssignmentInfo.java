package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.PreferenceLevel;

import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamInstructor;
import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamStudent;

public class ExamAssignmentInfo extends ExamAssignment implements Serializable  {
    private TreeSet iDirects = new TreeSet();
    private TreeSet iBackToBacks = new TreeSet();
    private TreeSet iMoreThanTwoADays = new TreeSet();
    private TreeSet iInstructorDirects = new TreeSet();
    private TreeSet iInstructorBackToBacks = new TreeSet();
    private TreeSet iInstructorMoreThanTwoADays = new TreeSet();
    
    public ExamAssignmentInfo(ExamPlacement placement) {
        this((Exam)placement.variable(),placement);
    }

    public ExamAssignmentInfo(Exam exam, ExamPlacement placement) {
        super(exam, placement);
        if (placement!=null) {
            ExamModel model = (ExamModel)exam.getModel();
            Hashtable directs = new Hashtable();
            for (Enumeration e=exam.getStudents().elements();e.hasMoreElements();) {
                ExamStudent student = (ExamStudent)e.nextElement();
                for (Iterator i=student.getExams(placement.getPeriod()).iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    DirectConflict dc = (DirectConflict)directs.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(new ExamAssignment((ExamPlacement)other.getAssignment()));
                        directs.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(student.getId());
                }
            }
            iDirects.addAll(directs.values());
            int btbDist = model.getBackToBackDistance();
            Hashtable backToBacks = new Hashtable();
            for (Enumeration e=exam.getStudents().elements();e.hasMoreElements();) {
                ExamStudent student = (ExamStudent)e.nextElement();
                if (placement.getPeriod().prev()!=null) {
                    if (model.isDayBreakBackToBack() || placement.getPeriod().prev().getDay()==placement.getPeriod().getDay()) {
                        Set exams = student.getExams(placement.getPeriod().prev());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            double distance = placement.getDistance((ExamPlacement)other.getAssignment());
                            BackToBackConflict btb = (BackToBackConflict)backToBacks.get(other);
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                        (btbDist<0?false:distance>btbDist), distance/5.0);
                                backToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(student.getId());
                        }
                    }
                }
                if (placement.getPeriod().next()!=null) {
                    if (model.isDayBreakBackToBack() || placement.getPeriod().next().getDay()==placement.getPeriod().getDay()) {
                        Set exams = student.getExams(placement.getPeriod().next());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            BackToBackConflict btb = (BackToBackConflict)backToBacks.get(other);
                            double distance = placement.getDistance((ExamPlacement)other.getAssignment());
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                        (btbDist<0?false:distance>btbDist), distance);
                                backToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(student.getId());
                        }
                    }
                }
            }
            iBackToBacks.addAll(backToBacks.values());
            Hashtable m2ds = new Hashtable();
            for (Enumeration e=exam.getStudents().elements();e.hasMoreElements();) {
                ExamStudent student = (ExamStudent)e.nextElement();
                Set exams = student.getExamsADay(placement.getPeriod());
                int nrExams = exams.size() + (exams.contains(exam)?0:1);
                if (nrExams<=2) continue;
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator i=exams.iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    examIds.add(other.getId());
                    otherExams.add(new ExamAssignment((ExamPlacement)other.getAssignment()));
                }
                MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)m2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    m2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                m2d.getStudents().add(student.getId());
            }
            iMoreThanTwoADays.addAll(m2ds.values());

            Hashtable idirects = new Hashtable();
            for (Enumeration e=exam.getInstructors().elements();e.hasMoreElements();) {
                ExamInstructor instructor = (ExamInstructor)e.nextElement();
                for (Iterator i=instructor.getExams(placement.getPeriod()).iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    DirectConflict dc = (DirectConflict)idirects.get(other);
                    if (dc==null) {
                        dc = new DirectConflict(new ExamAssignment((ExamPlacement)other.getAssignment()));
                        idirects.put(other, dc);
                    } else dc.incNrStudents();
                    dc.getStudents().add(instructor.getId());
                }
            }
            iInstructorDirects.addAll(idirects.values());
            Hashtable ibackToBacks = new Hashtable();
            for (Enumeration e=exam.getInstructors().elements();e.hasMoreElements();) {
                ExamInstructor instructor = (ExamInstructor)e.nextElement();
                if (placement.getPeriod().prev()!=null) {
                    if (model.isDayBreakBackToBack() || placement.getPeriod().prev().getDay()==placement.getPeriod().getDay()) {
                        Set exams = instructor.getExams(placement.getPeriod().prev());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            double distance = placement.getDistance((ExamPlacement)other.getAssignment());
                            BackToBackConflict btb = (BackToBackConflict)ibackToBacks.get(other);
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                        (btbDist<0?false:distance>btbDist), distance/5.0);
                                ibackToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(instructor.getId());
                        }
                    }
                }
                if (placement.getPeriod().next()!=null) {
                    if (model.isDayBreakBackToBack() || placement.getPeriod().next().getDay()==placement.getPeriod().getDay()) {
                        Set exams = instructor.getExams(placement.getPeriod().next());
                        for (Iterator i=exams.iterator();i.hasNext();) {
                            Exam other = (Exam)i.next();
                            BackToBackConflict btb = (BackToBackConflict)ibackToBacks.get(other);
                            double distance = placement.getDistance((ExamPlacement)other.getAssignment());
                            if (btb==null) {
                                btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                        (btbDist<0?false:distance>btbDist), distance);
                                ibackToBacks.put(other, btb);
                            } else btb.incNrStudents();
                            btb.getStudents().add(instructor.getId());
                        }
                    }
                }
            }
            iInstructorBackToBacks.addAll(ibackToBacks.values());
            Hashtable im2ds = new Hashtable();
            for (Enumeration e=exam.getInstructors().elements();e.hasMoreElements();) {
                ExamInstructor instructor = (ExamInstructor)e.nextElement();
                Set exams = instructor.getExamsADay(placement.getPeriod());
                int nrExams = exams.size() + (exams.contains(exam)?0:1);
                if (nrExams<=2) continue;
                TreeSet examIds = new TreeSet();
                TreeSet otherExams = new TreeSet();
                for (Iterator i=exams.iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    if (other.equals(exam)) continue;
                    examIds.add(other.getId());
                    otherExams.add(new ExamAssignment((ExamPlacement)other.getAssignment()));
                }
                MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)im2ds.get(examIds.toString());
                if (m2d==null) {
                    m2d = new MoreThanTwoADayConflict(otherExams);
                    im2ds.put(examIds.toString(), m2d);
                } else m2d.incNrStudents();
                m2d.getStudents().add(instructor.getId());
            }
            iInstructorMoreThanTwoADays.addAll(im2ds.values());
        }
    }
    
    public ExamAssignmentInfo(org.unitime.timetable.model.Exam exam) {
        super(exam);
        if (exam.getConflicts()!=null && !exam.getConflicts().isEmpty()) {
            for (Iterator i=exam.getConflicts().iterator();i.hasNext();) {
                ExamConflict conf = (ExamConflict)i.next();
                if (conf.isDirectConflict()) {
                    ExamAssignment other = null;
                    for (Iterator j=conf.getExams().iterator();j.hasNext();) {
                        org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)j.next();
                        if (x.equals(exam)) continue;
                        if (x.getAssignedPeriod()!=null) other = new ExamAssignment(x);
                    }
                    if (other==null) continue;
                    if (conf.getNrStudents()>0)
                        iDirects.add(new DirectConflict(other, conf.getNrStudents()));
                    if (conf.getNrInstructors()>0)
                        iInstructorDirects.add(new DirectConflict(other, conf.getNrInstructors()));
                } else if (conf.isBackToBackConflict()) {
                    ExamAssignment other = null;
                    for (Iterator j=conf.getExams().iterator();j.hasNext();) {
                        org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)j.next();
                        if (x.equals(exam)) continue;
                        if (x.getAssignedPeriod()!=null) other = new ExamAssignment(x);
                    }
                    if (other==null) continue;
                    if (conf.getNrStudents()>0)
                        iBackToBacks.add(new BackToBackConflict(other, conf.getNrStudents(), conf.isDistanceBackToBackConflict(), conf.getDistance()));
                    if (conf.getNrInstructors()>0)
                        iInstructorBackToBacks.add(new BackToBackConflict(other, conf.getNrInstructors(), conf.isDistanceBackToBackConflict(), conf.getDistance()));
                } else if (conf.isMoreThanTwoADayConflict()) {
                    TreeSet other = new TreeSet();
                    for (Iterator j=conf.getExams().iterator();j.hasNext();) {
                        org.unitime.timetable.model.Exam x = (org.unitime.timetable.model.Exam)j.next();
                        if (x.equals(exam)) continue;
                        if (x.getAssignedPeriod()!=null) other.add(new ExamAssignment(x));
                    }
                    if (other.size()<2) continue;
                    if (conf.getNrStudents()>0)
                        iMoreThanTwoADays.add(new MoreThanTwoADayConflict(other, conf.getNrStudents()));
                    if (conf.getNrInstructors()>0)
                        iInstructorMoreThanTwoADays.add(new MoreThanTwoADayConflict(other, conf.getNrInstructors()));
                }
            }
        }
    }
    
    public TreeSet getDirectConflicts() {
        return iDirects;
    }

    public TreeSet getBackToBackConflicts() {
        return iBackToBacks;
    }
    
    public TreeSet getMoreThanTwoADaysConflicts() {
        return iMoreThanTwoADays;
    }
    
    public int countDirectConflicts() {
        int ret = 0;
        for (Iterator i=iDirects.iterator();i.hasNext();) {
            DirectConflict dc = (DirectConflict)i.next();
            ret += dc.getNrStudents();
        }
        return ret;
    }

    public int countBackToBackConflicts() {
        int ret = 0;
        for (Iterator i=iBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            ret += btb.getNrStudents();
        }
        return ret;
    }
    
    public int countDistanceBackToBackConflicts() {
        int ret = 0;
        for (Iterator i=iBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            if (btb.isDistance())
                ret += btb.getNrStudents();
        }
        return ret;
    }

    public int countMoreThanTwoConflicts() {
        int ret = 0;
        for (Iterator i=iMoreThanTwoADays.iterator();i.hasNext();) {
            MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)i.next();
            ret += m2d.getNrStudents();
        }
        return ret;
    }

    public boolean hasConflicts() {
        return !getDirectConflicts().isEmpty() || !getBackToBackConflicts().isEmpty() || !getMoreThanTwoADaysConflicts().isEmpty();
    }
    
    public String getConflictTable(boolean header) {
        String ret = "<table border='0' width='95%' cellspacing='0' cellpadding='3'>";
        if (header) {
            ret += "<tr>";
            ret += "<td><i>Students</i></td>";
            ret += "<td><i>Conflict</i></td>";
            ret += "<td><i>Exam</i></td>";
            ret += "<td><i>Period</i></td>";
            ret += "<td><i>Room</i></td>";
            ret += "</tr>";
        }
        for (Iterator i=getDirectConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getMoreThanTwoADaysConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getBackToBackConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        ret += "</table>";
        return ret;
    }
    
    public TreeSet getInstructorDirectConflicts() {
        return iInstructorDirects;
    }

    public TreeSet getInstructorBackToBackConflicts() {
        return iInstructorBackToBacks;
    }
    
    public TreeSet getInstructorMoreThanTwoADaysConflicts() {
        return iInstructorMoreThanTwoADays;
    }
    
    public int countInstructorDirectConflicts() {
        int ret = 0;
        for (Iterator i=iInstructorDirects.iterator();i.hasNext();) {
            DirectConflict dc = (DirectConflict)i.next();
            ret += dc.getNrStudents();
        }
        return ret;
    }

    public int countInstructorBackToBackConflicts() {
        int ret = 0;
        for (Iterator i=iInstructorBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            ret += btb.getNrStudents();
        }
        return ret;
    }
    
    public int countInstructorDistanceBackToBackConflicts() {
        int ret = 0;
        for (Iterator i=iInstructorBackToBacks.iterator();i.hasNext();) {
            BackToBackConflict btb = (BackToBackConflict)i.next();
            if (btb.isDistance())
                ret += btb.getNrStudents();
        }
        return ret;
    }

    public int countInstructorMoreThanTwoConflicts() {
        int ret = 0;
        for (Iterator i=iInstructorMoreThanTwoADays.iterator();i.hasNext();) {
            MoreThanTwoADayConflict m2d = (MoreThanTwoADayConflict)i.next();
            ret += m2d.getNrStudents();
        }
        return ret;
    }

    public boolean hasInstructorConflicts() {
        return !getInstructorDirectConflicts().isEmpty() || !getInstructorBackToBackConflicts().isEmpty() || !getInstructorMoreThanTwoADaysConflicts().isEmpty();
    }
    
    public String getInstructorConflictTable(boolean header) {
        String ret = "<table border='0' width='95%' cellspacing='0' cellpadding='3'>";
        if (header) {
            ret += "<tr>";
            ret += "<td><i>Instructors</i></td>";
            ret += "<td><i>Conflict</i></td>";
            ret += "<td><i>Exam</i></td>";
            ret += "<td><i>Period</i></td>";
            ret += "<td><i>Room</i></td>";
            ret += "</tr>";
        }
        for (Iterator i=getInstructorDirectConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getInstructorMoreThanTwoADaysConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getInstructorBackToBackConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        ret += "</table>";
        return ret;
    }
    
    
    public static class DirectConflict implements Serializable, Comparable {
        protected ExamAssignment iOtherExam;
        protected int iNrStudents = 1;
        protected transient Vector iStudents = new Vector();
        
        protected DirectConflict(ExamAssignment otherExam) {
            iOtherExam = otherExam;
        }
        protected DirectConflict(ExamAssignment otherExam, int nrStudents) {
            iOtherExam = otherExam;
            iNrStudents = nrStudents;
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
        }
        public Vector getStudents() {
            return iStudents;
        }
        public ExamAssignment getOtherExam() {
            return iOtherExam;
        }
        public int compareTo(Object o) {
            DirectConflict c = (DirectConflict)o;
            int cmp = -Double.compare(getNrStudents(), c.getNrStudents());
            if (cmp!=0) return cmp;
            return getOtherExam().compareTo(c.getOtherExam());
        }
        public String toString() {
            String ret = "";
            ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';\" onmouseout=\"this.style.backgroundColor='transparent';\">";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("P")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("P")+";'>";
            ret += "Direct";
            ret += "</td>";
            ret += "<td>"+getOtherExam().getExamName()+"</td>";
            ret += "<td>"+getOtherExam().getPeriodAbbreviationWithPref()+"</td>";
            ret += "<td>"+getOtherExam().getRoomsNameWithPref(", ")+"</td>";
            ret += "</tr>";
            return ret;
        }
    }
    
    public static class BackToBackConflict implements Serializable, Comparable {
        protected ExamAssignment iOtherExam;
        protected int iNrStudents = 1;
        protected boolean iIsDistance = false; 
        protected transient Vector iStudents = new Vector();
        protected double iDistance = 0;
        
        protected BackToBackConflict(ExamAssignment otherExam, boolean isDistance, double distance) {
            iOtherExam = otherExam;
            iIsDistance = isDistance;
            iDistance = distance;
        }
        protected BackToBackConflict(ExamAssignment otherExam, int nrStudents, boolean isDistance, double distance) {
            iOtherExam = otherExam;
            iNrStudents = nrStudents;
            iIsDistance = isDistance;
            iDistance = distance;
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
        }
        public boolean isDistance() {
            return iIsDistance;
        }
        public ExamAssignment getOtherExam() {
            return iOtherExam;
        }
        public Vector getStudents() {
            return iStudents;
        }
        public double getDistance() {
            return iDistance;
        }
        public int compareTo(Object o) {
            BackToBackConflict c = (BackToBackConflict)o;
            int cmp = -Double.compare(getNrStudents(), c.getNrStudents());
            if (cmp!=0) return cmp;
            if (isDistance()!=c.isDistance()) return (isDistance()?-1:1);
            return getOtherExam().compareTo(c.getOtherExam());
        }
        public String toString() {
            String ret = "";
            ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';\" onmouseout=\"this.style.backgroundColor='transparent';\">";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("1")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("1")+";'>";
            ret += "Back-To-Back";
            if (isDistance()) ret+="<br>("+Math.round(10.0*getDistance())+" m)";
            ret += "</td>";
            ret += "<td>"+getOtherExam().getExamName()+"</td>";
            ret += "<td>"+getOtherExam().getPeriodAbbreviationWithPref()+"</td>";
            ret += "<td>"+getOtherExam().getRoomsNameWithPref(", ")+"</td>";
            ret += "</tr>";
            return ret;
        }
    }

    public static class MoreThanTwoADayConflict implements Serializable, Comparable {
        protected TreeSet iOtherExams;
        protected int iNrStudents = 1;
        protected transient Vector iStudents = new Vector();
        
        protected MoreThanTwoADayConflict(TreeSet otherExams) {
            iOtherExams = otherExams;
        }
        protected MoreThanTwoADayConflict(TreeSet otherExams, int nrStudents) {
            iOtherExams = otherExams;
            iNrStudents = nrStudents;
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
        }
        public Vector getStudents() {
            return iStudents;
        }
        public TreeSet getOtherExams() {
            return iOtherExams;
        }
        public int compareTo(Object o) {
            MoreThanTwoADayConflict c = (MoreThanTwoADayConflict)o;
            int cmp = -Double.compare(getNrStudents(), c.getNrStudents());
            if (cmp!=0) return cmp;
            cmp = -Double.compare(getOtherExams().size(), c.getOtherExams().size());
            if (cmp!=0) return cmp;
            Iterator i1 = getOtherExams().iterator(), i2 = c.getOtherExams().iterator();
            while (i1.hasNext()) {
                ExamAssignment a1 = (ExamAssignment)i1.next();
                ExamAssignment a2 = (ExamAssignment)i2.next();
                if (!a1.equals(a2)) return a1.compareTo(a2);
            }
            return 0;
        }
        public String toString() {
            String ret = "";
            String mouseOver = "";
            String mouseOut = "";
            String id = "";
            for (Iterator i=getOtherExams().iterator();i.hasNext();) {
                ExamAssignment a = (ExamAssignment)i.next();
                id+=a.getExamId(); 
                if (i.hasNext()) id+=":";
            }
            int idx = 0;
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamAssignment a = (ExamAssignment)i.next();
                mouseOver += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='rgb(223,231,242)';";
                mouseOut += "document.getElementById('"+id+":"+idx+"').style.backgroundColor='transparent';";
            }
            idx = 0;
            ret += "<tr id='"+id+":"+idx+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
            ret += "<td valign='top' rowspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color("2")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td valign='top' rowspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color("2")+";'>";
            ret += "&gt;2 A Day";
            ret += "</td>";
            for (Iterator i=getOtherExams().iterator();i.hasNext();idx++) {
                ExamAssignment a = (ExamAssignment)i.next();
                ret += "<td>"+a.getExamName()+"</td>";
                ret += "<td>"+a.getPeriodAbbreviationWithPref()+"</td>";
                ret += "<td>"+a.getRoomsNameWithPref(", ")+"</td>";
                ret += "</tr>";
                if (i.hasNext()) 
                    ret += "<tr id='"+id+":"+(1+idx)+"' onmouseover=\""+mouseOver+"\" onmouseout=\""+mouseOut+"\">";
            }
            return ret;
        }
    }
}
