/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;

public class CourseLoader {
	private static Logger sLog = Logger.getLogger(CourseLoader.class);
	private AcademicSessionInfo iAcademicSession;
	private Hashtable<Long, Course> iCourseTable;
	private Hashtable<Long, Section> iClassTable;
	private Hashtable<Long, Student> iStudentTable;
	
	private Hashtable<Long, CourseInfo> iCourseForId = null;
	private Hashtable<String, TreeSet<CourseInfo>> iCourseForName = null;
	private TreeSet<CourseInfo> iCourses = null;
	private StudentSectioningModel iModel = null;
	
	private Hashtable<Long, String> iCourseNames = new Hashtable<Long, String>();

	CourseLoader(StudentSectioningModel model, AcademicSessionInfo academicSession, Hashtable<Long, Course> courseTable, Hashtable<Long, Section> classTable, Hashtable<Long, Student> studentTable,
			Hashtable<Long, CourseInfo> courseForId, Hashtable<String, TreeSet<CourseInfo>> courseForName, TreeSet<CourseInfo> courses) {
		iAcademicSession = academicSession;
		iCourseTable = courseTable;
		iClassTable = classTable;
		iStudentTable = studentTable;
		iCourseForId = courseForId;
		iCourseForName = courseForName;
		iCourses = courses;
		iModel = model;
	}
	
	@SuppressWarnings("unchecked")
	public void updateAll(org.hibernate.Session hibSession) {
		sLog.info("Updating course infos and the student sectining model for session "+iAcademicSession);
		synchronized (iCourseTable) {
			long t0 = System.currentTimeMillis();
			iClassTable.clear();
			iStudentTable.clear();
			iCourseTable.clear();
			iCourseForId.clear();
			iCourseForName.clear();
			iCourses.clear();
			iCourseNames.clear();
			
			List<InstructionalOffering> offerings = hibSession.createQuery(
					"select distinct io from InstructionalOffering io " +
					"left join fetch io.courseOfferings co " +
					"left join fetch co.courseReservations r "+
					"left join fetch io.instrOfferingConfigs cf " +
					"left join fetch cf.schedulingSubparts ss " +
					"left join fetch ss.classes c " +
					"left join fetch c.assignments a " +
					"left join fetch a.rooms r " +
					"left join fetch c.classInstructors i " +
					"where io.session.uniqueId = :sessionId and io.notOffered = false")
					.setLong("sessionId", iAcademicSession.getUniqueId()).list();
			for (InstructionalOffering offering: offerings) {
				if (offering.getInstrOfferingConfigs().isEmpty()) continue;
				for (Iterator<CourseOffering> j = offering.getCourseOfferings().iterator(); j.hasNext();) {
					CourseOffering course = j.next();
					CourseInfo info = new CourseInfo(course);
					String courseName = (info.getSubjectArea() + " " + info.getCourseNbr()).toLowerCase();
					TreeSet<CourseInfo> infos = iCourseForName.get(courseName);
					if (infos == null) {
						infos = new TreeSet<CourseInfo>();
						iCourseForName.put(courseName, infos);
					}
					iCourses.add(info);
					infos.add(info);
					iCourseForId.put(course.getUniqueId(), info);
					if (infos.size() > 1) {
						for (CourseInfo i: infos) i.setHasUniqueName(false);
					}
				}
			}
			for (InstructionalOffering offering: offerings)
				loadOffering(offering);
			List<org.unitime.timetable.model.Student> students = hibSession.createQuery(
                    "select distinct s from Student s " +
                    "left join fetch s.courseDemands as cd " +
                    "left join fetch cd.courseRequests as cr " +
                    "left join fetch s.classEnrollments as e " +
                    "where s.session.uniqueId=:sessionId").
                    setLong("sessionId",iAcademicSession.getUniqueId()).list();
            for (org.unitime.timetable.model.Student student: students) {
            	Student s = loadStudent(student);
            	iModel.addStudent(s);
            	assignStudent(s, student, true);
            }
            
        	List<SectioningInfo> infos = hibSession.createQuery(
			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
			.setLong("sessionId", iAcademicSession.getUniqueId())
			.list();
        	for (SectioningInfo info : infos) {
        		Section section = iClassTable.get(info.getClazz().getUniqueId());
        		if (section != null) {
        			section.setSpaceExpected(info.getNbrExpectedStudents());
        			section.setSpaceHeld(info.getNbrHoldingStudents());
        			if (section.getLimit() >= 0 && (section.getLimit() - section.getEnrollments().size()) <= section.getSpaceExpected())
        				sLog.info("Section " + section.getSubpart().getConfig().getOffering().getName() + " " + section.getSubpart().getName() + " " +
        						section.getName() + " has high demand (limit: " + section.getLimit() + ", enrollment: " + section.getEnrollments().size() +
        						", expected: " + section.getSpaceExpected() + ")");
        		}
        	}
            
			long t1 = System.currentTimeMillis();
			sLog.info("  Update of session " + iAcademicSession + " done " + new DecimalFormat("0.0").format((t1 - t0) / 1000.0) + " seconds.");
			sLog.info(ToolBox.dict2string(iModel.getInfo(), 2));
		}
	}
	
    private Offering loadOffering(InstructionalOffering io) {
    	if (io.getInstrOfferingConfigs().isEmpty()) {
    		return null;
    	}
    	String courseName = io.getCourseName();
        Offering offering = new Offering(io.getUniqueId().longValue(), courseName);
        for (Iterator<CourseOffering> i = io.getCourseOfferings().iterator(); i.hasNext(); ) {
        	CourseOffering co = i.next();
            int projected = (co.getProjectedDemand()==null?0:co.getProjectedDemand().intValue());
            boolean unlimited = false;
            int limit = 0;
            for (Iterator<InstrOfferingConfig> j = io.getInstrOfferingConfigs().iterator(); j.hasNext(); ) {
            	InstrOfferingConfig ioc = j.next();
                if (ioc.isUnlimitedEnrollment()) unlimited = true;
                limit += ioc.getLimit();
            }
            for (Iterator<CourseOfferingReservation> k = co.getCourseReservations().iterator(); k.hasNext(); ) {
            	CourseOfferingReservation reservation = k.next();
                if (reservation.getCourseOffering().equals(co) && reservation.getReserved()!=null)
                    limit = reservation.getReserved();
            }
            if (limit >= 9999) unlimited = true;
            if (unlimited) limit=-1;
            Course course = new Course(co.getUniqueId(), co.getSubjectArea().getSubjectAreaAbbreviation(), co.getCourseNbr(), offering, limit, projected);
            iCourseTable.put(co.getUniqueId(), course);
        }
        Hashtable<Long,Section> class2section = new Hashtable<Long,Section>();
        Hashtable<Long,Subpart> ss2subpart = new Hashtable<Long, Subpart>();
        DecimalFormat df = new DecimalFormat("000");
        for (Iterator<InstrOfferingConfig> i = io.getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
        	InstrOfferingConfig ioc = i.next();
            Config config = new Config(ioc.getUniqueId(), courseName + " [" + ioc.getName() + "]", offering);
            TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator());
            subparts.addAll(ioc.getSchedulingSubparts());
            for (SchedulingSubpart ss: subparts) {
                String sufix = ss.getSchedulingSubpartSuffix();
                Subpart parentSubpart = (ss.getParentSubpart() == null ? null : (Subpart)ss2subpart.get(ss.getParentSubpart().getUniqueId()));
                if (ss.getParentSubpart() != null && parentSubpart == null) {
                    sLog.error("Subpart " + ss.getSchedulingSubpartLabel() + " has parent " + 
                    		ss.getSchedulingSubpartLabel() +", but the appropriate parent subpart is not loaded. [" + iAcademicSession + "]");
                }
                Subpart subpart = new Subpart(ss.getUniqueId().longValue(), df.format(ss.getItype().getItype()) + sufix,
                		ss.getItype().getAbbv().trim(), config, parentSubpart);
                ss2subpart.put(ss.getUniqueId(), subpart);
                for (Iterator<Class_> j = ss.getClasses().iterator(); j.hasNext(); ) {
                	Class_ c = j.next();
                    Section parentSection = (c.getParentClass() == null ? null : (Section)class2section.get(c.getParentClass().getUniqueId()));
                    if (c.getParentClass()!=null && parentSection==null) {
                        sLog.error("Class " + c.getClassLabel() + " has parent " + c.getClassLabel() + ", but the appropriate parent section is not loaded. [" + iAcademicSession + "]");
                    }
                    Assignment a = c.getCommittedAssignment();
                    Placement p = (a == null ? null : a.getPlacement());
                    if (p != null && p.getTimeLocation() != null) {
                    	p.getTimeLocation().setDatePattern(
                    			p.getTimeLocation().getDatePatternId(),
                    			datePatternName(p.getTimeLocation()),
                    			p.getTimeLocation().getWeekCode());
                    }
                    int minLimit = c.getExpectedCapacity();
                	int maxLimit = c.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && p != null) {
                		int roomLimit = Math.round((c.getRoomRatio() == null ? 1.0f : c.getRoomRatio()) * p.getRoomSize());
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (ioc.isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                    String instructorIds = "";
                    String instructorNames = "";
                    for (Iterator<ClassInstructor> k = c.getClassInstructors().iterator(); k.hasNext(); ) {
                    	ClassInstructor ci = k.next();
                    	if (!ci.isLead()) continue;
                    	if (!instructorIds.isEmpty()) {
                    		instructorIds += ":"; instructorNames += ":";
                    	}
                    	instructorIds += ci.getInstructor().getUniqueId().toString();
                    	instructorNames += ci.getInstructor().getName(DepartmentalInstructor.sNameFormatShort) + "|"  + (ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
                    }
                    Section section = new Section(c.getUniqueId().longValue(), limit, (c.getExternalUniqueId() == null ? c.getClassSuffix() : c.getExternalUniqueId()), subpart, p, instructorIds, instructorNames, parentSection);
                    class2section.put(c.getUniqueId(), section);
                    iClassTable.put(c.getUniqueId(), section);
                }
            }
        }
        return offering;
    }
    
    public Student loadStudent(org.unitime.timetable.model.Student s) {
        Student student = new Student(s.getUniqueId());
        iStudentTable.put(s.getUniqueId(), student);
        
        if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.enabled", "true"))) {
    		TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
    			public int compare(CourseDemand d1, CourseDemand d2) {
    				if (d1.isAlternative() && !d2.isAlternative()) return 1;
    				if (!d1.isAlternative() && d2.isAlternative()) return -1;
    				int cmp = d1.getPriority().compareTo(d2.getPriority());
    				if (cmp != 0) return cmp;
    				return d1.getUniqueId().compareTo(d2.getUniqueId());
    			}
    		});
    		demands.addAll(s.getCourseDemands());
            for (CourseDemand cd: demands) {
                if (cd.getFreeTime()!=null) {
                	TimeLocation ft = new TimeLocation(
                            cd.getFreeTime().getDayCode(),
                            cd.getFreeTime().getStartSlot(),
                            cd.getFreeTime().getLength(),
                            0, 0, -1l, "", iAcademicSession.getFreeTimePattern(), 0);
                    new FreeTimeRequest(
                            cd.getUniqueId(),
                            cd.getPriority(),
                            cd.isAlternative(),
                            student, ft);
                } else if (!cd.getCourseRequests().isEmpty()) {
                    Vector<Course> courses = new Vector<Course>();
                    HashSet<Section> assignedSections = new HashSet<Section>();
                    Config assignedConfig = null;
                    TreeSet<org.unitime.timetable.model.CourseRequest> crs = new TreeSet<org.unitime.timetable.model.CourseRequest>(new Comparator<org.unitime.timetable.model.CourseRequest>() {
                    	public int compare(org.unitime.timetable.model.CourseRequest r1, org.unitime.timetable.model.CourseRequest r2) {
                    		return r1.getOrder().compareTo(r2.getOrder());
                    	}
    				});
                    crs.addAll(cd.getCourseRequests());
                    for (org.unitime.timetable.model.CourseRequest cr: crs) {
                        Course course = iCourseTable.get(cr.getCourseOffering().getUniqueId());
                        if (course==null) {
                            sLog.warn("Student " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") requests course " + cr.getCourseOffering().getCourseName() + " that is not loaded. [" + iAcademicSession + "]");
                            continue;
                        }
                        if (assignedConfig==null) {
                            HashSet<Long> subparts = new HashSet<Long>();
                            for (Iterator<StudentClassEnrollment> i = (cr.getClassEnrollments() == null ? s.getClassEnrollments() : cr.getClassEnrollments()).iterator(); i.hasNext(); ) {
                            	StudentClassEnrollment enrl = i.next();
                            	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                                if (section!=null) {
                                    assignedSections.add(section);
                                    if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                                    	sLog.error("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): classes from different configurations. [" + iAcademicSession + "]");
                                    }
                                    assignedConfig = section.getSubpart().getConfig();
                                    if (!subparts.add(section.getSubpart().getId())) {
                                    	sLog.error("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart. [" + iAcademicSession + "]");
                                    }
                                } else {
                                	sLog.error("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel() + " not known. [" + iAcademicSession + "]");
                                }
                            }
                        }
                        courses.addElement(course);
                    }
                    if (courses.isEmpty()) continue;
                    CourseRequest request = new CourseRequest(
                            cd.getUniqueId(),
                            cd.getPriority(),
                            cd.isAlternative(),
                            student,
                            courses,
                            cd.isWaitlist());
                    if (assignedConfig!=null && assignedSections.size() == assignedConfig.getSubparts().size()) {
                        Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections);
                        request.setInitialAssignment(enrollment);
                    }
                    if (assignedConfig!=null && assignedSections.size() != assignedConfig.getSubparts().size()) {
                    	sLog.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") wrong number of classes (" +
                    			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + "). [" + iAcademicSession + "]");
                    }
                }
            }
        }
        
        if (student.getRequests().isEmpty() && !s.getClassEnrollments().isEmpty()) {
        	TreeSet<Course> courses = new TreeSet<Course>(new Comparator<Course>() {
        		public int compare(Course c1, Course c2) {
        			return (c1.getSubjectArea() + " " + c1.getCourseNumber()).compareTo(c2.getSubjectArea() + " " + c2.getCourseNumber());
        		}
        	});
        	for (Iterator<StudentClassEnrollment> i = s.getClassEnrollments().iterator(); i.hasNext(); ) {
        		StudentClassEnrollment enrl = i.next();
        		Course course = iCourseTable.get(enrl.getCourseOffering().getUniqueId());
                if (course==null) {
                    sLog.warn("Student " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") requests course " + enrl.getCourseOffering().getCourseName()+" that is not loaded. [" + iAcademicSession + "]");
                    continue;
                }
                courses.add(course);
        	}
        	int priority = 0;
        	courses: for (Course course: courses) {
        		Vector<Course> cx = new Vector<Course>(); cx.add(course);
                CourseRequest request = new CourseRequest(
                        course.getId(),
                        priority++,
                        false,
                        student,
                        cx,
                        false);
                HashSet<Section> assignedSections = new HashSet<Section>();
                Config assignedConfig = null;
                HashSet<Long> subparts = new HashSet<Long>();
                for (Iterator<StudentClassEnrollment> i = s.getClassEnrollments().iterator(); i.hasNext(); ) {
                	StudentClassEnrollment enrl = i.next();
                	if (course.getId() != enrl.getCourseOffering().getUniqueId()) continue;
                	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                    if (section!=null) {
                        assignedSections.add(section);
                        if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                        	sLog.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): classes from different configurations. [" + iAcademicSession + "]");
                        	continue courses;
                        }
                        assignedConfig = section.getSubpart().getConfig();
                        if (!subparts.add(section.getSubpart().getId())) {
                        	sLog.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart. [" + iAcademicSession + "]");
                        	continue courses;
                        }
                    } else {
                    	sLog.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel() + " not known. [" + iAcademicSession + "]");
                    	Section x = iClassTable.get(enrl.getClazz().getUniqueId());
                    	if (x != null) {
                    		sLog.info("  but a class with the same id is loaded, but under offering " + x.getSubpart().getConfig().getOffering().getName() + " (id is " + x.getSubpart().getConfig().getOffering().getId() + 
                    				", expected " +course.getOffering().getId() + ") [" + iAcademicSession + "]");
                    	}
                    	continue courses;
                    }
                }
                if (assignedConfig!=null && assignedSections.size() == assignedConfig.getSubparts().size()) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections);
                    request.setInitialAssignment(enrollment);
                }
                if (assignedConfig!=null && assignedSections.size() != assignedConfig.getSubparts().size()) {
                	sLog.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): wrong number of classes (" +
                			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + "). [" + iAcademicSession + "]");
                }
        	}
        }
        
        return student;
    }
    
    @SuppressWarnings("unchecked")
	 public void assignStudent(Student student, org.unitime.timetable.model.Student s, boolean tweakLimits) {
		for (Request r: student.getRequests()) {
			if (r instanceof CourseRequest && r.getInitialAssignment() != null) {
                if (r.getModel().conflictValues(r.getInitialAssignment()).isEmpty()) {
                	r.assign(0, r.getInitialAssignment());
                } else {
                	CourseRequest cr = (CourseRequest)r;
                	Enrollment enrl = (Enrollment)r.getInitialAssignment();
                	sLog.error("There is a problem assigning " + cr.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") [" + iAcademicSession + "]");
                	boolean hasLimit = false, hasOverlap = false;
                	sections: for (Iterator<Section> i = enrl.getSections().iterator(); i.hasNext();) {
                		Section section = i.next();
                		if (section.getTime() != null) {
                    		for (Request q: student.getRequests()) {
                    			if (q.getAssignment() == null || !(q instanceof CourseRequest)) continue;
                    			Enrollment enrlx = (Enrollment)q.getAssignment();
                    			for (Iterator<Section> j = enrlx.getSections().iterator(); j.hasNext();) {
                    				Section sectionx = j.next();
                    				if (sectionx.getTime() == null) continue;
                    				if (sectionx.getTime().hasIntersection(section.getTime())) {
                    					sLog.info("  " + section.getSubpart().getName() + " " + section.getName() + " " + section.getTime().getLongName() +
                    							" overlaps with " + sectionx.getSubpart().getConfig().getOffering().getName() + " " + sectionx.getSubpart().getName() + " " +
                    							sectionx.getName() + " " + sectionx.getTime().getLongName());
                    					hasOverlap = true;
                    					continue sections;
                    				}
                    			}
                    		}
                		}
                		if (section.getLimit() >= section.getEnrollments().size()) {
        					sLog.info("  " + section.getSubpart().getName() + " " + section.getName() + (section.getTime() == null ? "" : " " + section.getTime().getLongName()) +
        							" has no space available (limit is "+ section.getLimit() + ")");
        					if (tweakLimits) {
        						section.setLimit(section.getEnrollments().size() + 1);
        						sLog.info("    limit increased to "+section.getLimit());
        					}
                			hasLimit = true;
                			continue sections;
                		}
    					sLog.info("  " + section.getSubpart().getName() + " " + section.getName() + (section.getTime() == null ? "" : " " + section.getTime().getLongName()));
                	}
                	if (!hasLimit && !hasOverlap) {
                		for (Iterator<Enrollment> i = r.getModel().conflictValues(r.getInitialAssignment()).iterator(); i.hasNext();) {
                			Enrollment enrlx = i.next();
                			for (Iterator<Section> j = enrlx.getSections().iterator(); j.hasNext();) {
                				Section sectionx = j.next();
                				sLog.info("    conflicts with " + sectionx.getSubpart().getConfig().getOffering().getName() + " " + sectionx.getSubpart().getName() + " " +
            							sectionx.getName() + (sectionx.getTime() == null ? "" : " " + sectionx.getTime().getLongName()));
                			}
            				if (enrlx.getRequest().getStudent().getId() != student.getId())
            					sLog.info("    of a different student");
                		}
                	}
                	if (hasLimit && tweakLimits && r.getModel().conflictValues(r.getInitialAssignment()).isEmpty()) {
                    	r.assign(0, r.getInitialAssignment());
                	}
                }
			}
		}
		for (Request r: student.getRequests()) {
			if (r instanceof FreeTimeRequest) {
				FreeTimeRequest ft = (FreeTimeRequest)r;
                Enrollment enrollment = ft.createEnrollment();
                if (r.getModel().conflictValues(enrollment).isEmpty()) {
                    ft.setInitialAssignment(enrollment);
                    ft.assign(0, enrollment);
                }
			}
		}
    }

    private String datePatternName(TimeLocation time) {
    	if (time.getWeekCode().isEmpty()) return time.getDatePatternName();
    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(iAcademicSession.getDatePatternFirstDate());
    	int idx = time.getWeekCode().nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date first = null;
    	while (idx < time.getWeekCode().size() && first == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) first = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (first == null) return time.getDatePatternName();
    	cal.setTime(iAcademicSession.getDatePatternFirstDate());
    	idx = time.getWeekCode().length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) last = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;
    	}
    	if (last == null) return time.getDatePatternName();
        SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
    	return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    }
}
