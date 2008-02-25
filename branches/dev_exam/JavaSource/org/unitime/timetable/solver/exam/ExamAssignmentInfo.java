package org.unitime.timetable.solver.exam;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.PreferenceLevel;

import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamStudent;

public class ExamAssignmentInfo extends ExamAssignment implements Serializable  {
    private TreeSet iDirects = new TreeSet();
    private TreeSet iBackToBacks = new TreeSet();
    private TreeSet iMoreThanTwoADays = new TreeSet();

    public ExamAssignmentInfo(ExamPlacement placement) {
        super(placement);
        Exam exam = (Exam)placement.variable();
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
                        BackToBackConflict btb = (BackToBackConflict)backToBacks.get(other);
                        if (btb==null) {
                            btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                    (btbDist<0?false:placement.getDistance((ExamPlacement)other.getAssignment())>btbDist));
                            directs.put(other, btb);
                        } else btb.incNrStudents();
                    }
                }
            }
            if (placement.getPeriod().next()!=null) {
                if (model.isDayBreakBackToBack() || placement.getPeriod().next().getDay()==placement.getPeriod().getDay()) {
                    Set exams = student.getExams(placement.getPeriod().next());
                    for (Iterator i=exams.iterator();i.hasNext();) {
                        Exam other = (Exam)i.next();
                        BackToBackConflict btb = (BackToBackConflict)backToBacks.get(other);
                        if (btb==null) {
                            btb = new BackToBackConflict(new ExamAssignment((ExamPlacement)other.getAssignment()),
                                    (btbDist<0?false:placement.getDistance((ExamPlacement)other.getAssignment())>btbDist));
                            directs.put(other, btb);
                        } else btb.incNrStudents();
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
            } else m2d.incNrStudents();
        }
        iMoreThanTwoADays.addAll(m2ds.values());
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
        for (Iterator i=getBackToBackConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        for (Iterator i=getMoreThanTwoADaysConflicts().iterator();i.hasNext();)
            ret += i.next().toString();
        ret += "</table>";
        return ret;
    }
    
    public static class DirectConflict implements Serializable, Comparable {
        protected ExamAssignment iOtherExam;
        protected int iNrStudents = 1;
        
        protected DirectConflict(ExamAssignment otherExam) {
            iOtherExam = otherExam;
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
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
        
        protected BackToBackConflict(ExamAssignment otherExam, boolean isDistance) {
            iOtherExam = otherExam;
            iIsDistance = isDistance;
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
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("2")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("2")+";'>";
            if (isDistance()) ret+="Distance ";
            ret += "Back-To-Back";
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
        
        protected MoreThanTwoADayConflict(TreeSet otherExams) {
            iOtherExams = otherExams;
        }
        protected void incNrStudents() {
            iNrStudents++;
        }
        public int getNrStudents() {
            return iNrStudents;
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
            ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';\" onmouseout=\"this.style.backgroundColor='transparent';\">";
            ret += "<td colspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color("1")+";'>";
            ret += String.valueOf(getNrStudents());
            ret += "</td>";
            ret += "<td colspan='"+getOtherExams().size()+"' style='font-weight:bold;color:"+PreferenceLevel.prolog2color("1")+";'>";
            ret += "&gt;2 A Day";
            ret += "</td>";
            for (Iterator i=getOtherExams().iterator();i.hasNext();) {
                ExamAssignment a = (ExamAssignment)i.next();
                ret += "<td>"+a.getExamName()+"</td>";
                ret += "<td>"+a.getPeriodAbbreviationWithPref()+"</td>";
                ret += "<td>"+a.getRoomsNameWithPref(", ")+"</td>";
                ret += "</tr>";
                if (i.hasNext()) ret += "<tr>";
            }
            return ret;
        }
    }
}
