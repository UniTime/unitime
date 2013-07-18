/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.updates;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.cpsolver.coursett.constraint.GroupConstraint;
import net.sf.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.studentsct.constraint.LinkedSections;
import net.sf.cpsolver.studentsct.model.AcademicAreaCode;
import net.sf.cpsolver.studentsct.model.Choice;
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
import net.sf.cpsolver.studentsct.reservation.CourseReservation;
import net.sf.cpsolver.studentsct.reservation.CurriculumReservation;
import net.sf.cpsolver.studentsct.reservation.DummyReservation;
import net.sf.cpsolver.studentsct.reservation.GroupReservation;
import net.sf.cpsolver.studentsct.reservation.IndividualReservation;
import net.sf.cpsolver.studentsct.reservation.Reservation;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.solver.studentsct.StudentSectioningDatabaseLoader;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class ReloadAllData implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	@Override
	public Boolean execute(final OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.lockAll();
		try {
			helper.beginTransaction();
			try {
				helper.info("Updating course infos and the student sectining model for session " + server.getAcademicSession());
				long t0 = System.currentTimeMillis();
				server.clearAll();

				List<InstructionalOffering> offerings = helper.getHibSession().createQuery(
						"select distinct io from InstructionalOffering io " +
						"left join fetch io.courseOfferings co " +
						"left join fetch io.instrOfferingConfigs cf " +
						"left join fetch cf.schedulingSubparts ss " +
						"left join fetch ss.classes c " +
						"left join fetch c.assignments a " +
						"left join fetch a.rooms r " +
						"left join fetch c.classInstructors i " +
						"left join fetch io.reservations x " +
						"where io.session.uniqueId = :sessionId and io.notOffered = false")
						.setLong("sessionId", server.getAcademicSession().getUniqueId()).list();
				for (InstructionalOffering io: offerings) {
					Offering offering = loadOffering(io, server, helper);
					if (offering != null)
						server.update(offering);
					for (CourseOffering co: io.getCourseOfferings())
						server.update(new CourseInfo(co));
				}
				
		    	List<DistributionPref> distPrefs = helper.getHibSession().createQuery(
		        		"select p from DistributionPref p, Department d where p.distributionType.reference in (:ref1, :ref2) and d.session.uniqueId = :sessionId" +
		        		" and p.owner = d and p.prefLevel.prefProlog = :pref")
		        		.setString("ref1", GroupConstraint.ConstraintType.LINKED_SECTIONS.reference())
		        		.setString("ref2", IgnoreStudentConflictsConstraint.REFERENCE)
		        		.setString("pref", PreferenceLevel.sRequired)
		        		.setLong("sessionId", server.getAcademicSession().getUniqueId())
		        		.list();
		        if (!distPrefs.isEmpty()) {
		        	StudentSectioningDatabaseLoader.SectionProvider p = new StudentSectioningDatabaseLoader.SectionProvider() {
						@Override
						public Section get(Long classId) {
							return server.getSection(classId);
						}
					};
		        	for (DistributionPref pref: distPrefs) {
		        		for (Collection<Section> sections: StudentSectioningDatabaseLoader.getSections(pref, p)) {
		        			if (GroupConstraint.ConstraintType.LINKED_SECTIONS.reference().equals(pref.getDistributionType().getReference())) {
		        				server.addLinkedSections(new LinkedSections(sections));
		        			} else {
		        				for (Section s1: sections)
		                			for (Section s2: sections)
		                				if (!s1.equals(s2)) s1.addIgnoreConflictWith(s2.getId());
		        			}
		        		}
		        	}
		        }
				
				if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.load", "true"))) {
					List<org.unitime.timetable.model.Student> students = helper.getHibSession().createQuery(
		                    "select distinct s from Student s " +
		                    "left join fetch s.courseDemands as cd " +
		                    "left join fetch cd.courseRequests as cr " +
		                    "left join fetch cr.classWaitLists as cwl " + 
		                    "left join fetch s.classEnrollments as e " +
		                    "left join fetch s.academicAreaClassifications as a " +
		                    "left join fetch s.posMajors as mj " +
		                    "left join fetch s.waitlists as w " +
		                    "left join fetch cr.classEnrollments as cre "+
		                    "left join fetch s.groups as g " +
		                    "where s.session.uniqueId=:sessionId").
		                    setLong("sessionId",server.getAcademicSession().getUniqueId()).list();
		            for (org.unitime.timetable.model.Student student: students) {
		            	Student s = loadStudent(student, server, helper);
		            	if (s != null)
		            		server.update(s);
		            }
				}
				
		    	List<SectioningInfo> infos = helper.getHibSession().createQuery(
		    			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
		    			.setLong("sessionId", server.getAcademicSession().getUniqueId())
		    			.list();
		    	for (SectioningInfo info : infos) {
		    		Section section = server.getSection(info.getClazz().getUniqueId());
		    		if (section != null) {
		    			section.setSpaceExpected(info.getNbrExpectedStudents());
		    			section.setSpaceHeld(info.getNbrHoldingStudents());
		    			if (section.getLimit() >= 0 && (section.getLimit() - section.getEnrollments().size()) < Math.round(section.getSpaceExpected()))
		    				helper.debug("Section " + section.getSubpart().getConfig().getOffering().getName() + " " + section.getSubpart().getName() + " " +
		    						section.getName() + " has high demand (limit: " + section.getLimit() + ", enrollment: " + section.getEnrollments().size() +
		    						", expected: " + section.getSpaceExpected() + ")");
		    		}
		    	}
		        
				long t1 = System.currentTimeMillis();
				helper.info("  Update of session " + server.getAcademicSession() + " done " + new DecimalFormat("0.0").format((t1 - t0) / 1000.0) + " seconds.");
				
				helper.commitTransaction();
				return true;
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		} finally {
			lock.release();
		}		
	}
	
    public static Offering loadOffering(InstructionalOffering io, OnlineSectioningServer server, OnlineSectioningHelper helper) {
    	if (io.getInstrOfferingConfigs().isEmpty()) {
    		return null;
    	}
    	String courseName = io.getCourseName();
        Offering offering = new Offering(io.getUniqueId().longValue(), courseName);
        for (Iterator<CourseOffering> i = io.getCourseOfferings().iterator(); i.hasNext(); ) {
        	CourseOffering co = i.next();
            int projected = (co.getProjectedDemand() != null ? co.getProjectedDemand().intValue() : co.getDemand() != null ? co.getDemand().intValue() : 0);
            boolean unlimited = false;
            int limit = 0;
            for (Iterator<InstrOfferingConfig> j = io.getInstrOfferingConfigs().iterator(); j.hasNext(); ) {
            	InstrOfferingConfig ioc = j.next();
                if (ioc.isUnlimitedEnrollment()) unlimited = true;
                limit += ioc.getLimit();
            }
            if (co.getReservation() != null)
            	limit = co.getReservation();
            if (limit >= 9999) unlimited = true;
            if (unlimited) limit=-1;
            Course course = new Course(co.getUniqueId(), co.getSubjectArea().getSubjectAreaAbbreviation(), co.getCourseNbr(), offering, limit, projected);
            course.setNote(co.getScheduleBookNote());
        }
        Hashtable<Long,Section> class2section = new Hashtable<Long,Section>();
        Hashtable<Long,Subpart> ss2subpart = new Hashtable<Long, Subpart>();
        DecimalFormat df = new DecimalFormat("000");
        for (Iterator<InstrOfferingConfig> i = io.getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
        	InstrOfferingConfig ioc = i.next();
        	int configLimit = (ioc.isUnlimitedEnrollment() ? -1 : ioc.getLimit());
        	if (configLimit >= 9999) configLimit = -1;
            Config config = new Config(ioc.getUniqueId(), configLimit, courseName + " [" + ioc.getName() + "]", offering);
            TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator());
            subparts.addAll(ioc.getSchedulingSubparts());
            boolean subpartCredit = false;
            for (SchedulingSubpart ss: subparts) {
            	if (ss.getCredit() != null) { subpartCredit = true; break; }
            }
            boolean offeringCredit = !subpartCredit;
            for (SchedulingSubpart ss: subparts) {
                String sufix = ss.getSchedulingSubpartSuffix(helper.getHibSession());
                Subpart parentSubpart = (ss.getParentSubpart() == null ? null : (Subpart)ss2subpart.get(ss.getParentSubpart().getUniqueId()));
                if (ss.getParentSubpart() != null && parentSubpart == null) {
                    helper.error("Subpart " + ss.getSchedulingSubpartLabel() + " has parent " + 
                    		ss.getSchedulingSubpartLabel() +", but the appropriate parent subpart is not loaded.");
                }
                Subpart subpart = new Subpart(ss.getUniqueId().longValue(), df.format(ss.getItype().getItype()) + sufix,
                		ss.getItype().getAbbv().trim(), config, parentSubpart);
                if (subpartCredit && ss.getCredit() != null) {
                	subpart.setCredit(ss.getCredit().creditAbbv() + "|" + ss.getCredit().creditText());
                } else if (offeringCredit && io.getCredit() != null) {
                	subpart.setCredit(io.getCredit().creditAbbv() + "|" + io.getCredit().creditText());
                	offeringCredit = false;
                }
                subpart.setAllowOverlap(ss.isStudentAllowOverlap());
                ss2subpart.put(ss.getUniqueId(), subpart);
                for (Iterator<Class_> j = ss.getClasses().iterator(); j.hasNext(); ) {
                	Class_ c = j.next();
                    Section parentSection = (c.getParentClass() == null ? null : (Section)class2section.get(c.getParentClass().getUniqueId()));
                    if (c.getParentClass()!=null && parentSection==null) {
                        helper.error("Class " + c.getClassLabel() + " has parent " + c.getClassLabel() + ", but the appropriate parent section is not loaded.");
                    }
                    Assignment a = c.getCommittedAssignment();
                    Placement p = (a == null ? null : a.getPlacement());
                    if (p != null && p.getTimeLocation() != null) {
                    	p.getTimeLocation().setDatePattern(
                    			p.getTimeLocation().getDatePatternId(),
                    			datePatternName(p.getTimeLocation(), server.getAcademicSession()),
                    			p.getTimeLocation().getWeekCode());
                    }
                    if (p != null)
                    	p.setAssignment(null);
                    int minLimit = c.getExpectedCapacity();
                	int maxLimit = c.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && p != null) {
                		int roomLimit = (int) Math.floor(p.getRoomSize() / (c.getRoomRatio() == null ? 1.0f : c.getRoomRatio()));
                		// int roomLimit = Math.round((c.getRoomRatio() == null ? 1.0f : c.getRoomRatio()) * p.getRoomSize());
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (ioc.isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                    if (!c.isEnabledForStudentScheduling()) limit = 0;
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
                    Section section = new Section(c.getUniqueId().longValue(), limit, (c.getClassSuffix() == null ? c.getSectionNumberString(helper.getHibSession()) : c.getClassSuffix()), subpart, p, instructorIds, instructorNames, parentSection);
                    for (CourseOffering co: io.getCourseOfferings())
                    	section.setName(co.getUniqueId(), c.getClassSuffix(co));
                    section.setName(-1l, c.getSectionNumberString(helper.getHibSession()));
                    section.setNote(c.getSchedulePrintNote());
                    class2section.put(c.getUniqueId(), section);
                }
            }
        }
        for (org.unitime.timetable.model.Reservation reservation: io.getReservations()) {
        	Reservation r = null;
        	if (reservation instanceof org.unitime.timetable.model.IndividualReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((org.unitime.timetable.model.IndividualReservation)reservation).getStudents())
        			studentIds.add(s.getUniqueId());
        		r = new IndividualReservation(reservation.getUniqueId(), offering, studentIds);
        	} else if (reservation instanceof StudentGroupReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((StudentGroupReservation)reservation).getGroup().getStudents())
        			studentIds.add(s.getUniqueId());
        		r = new GroupReservation(reservation.getUniqueId(), (reservation.getLimit() == null ? -1.0 : reservation.getLimit()),
        				offering, studentIds);
        	} else if (reservation instanceof org.unitime.timetable.model.CurriculumReservation) {
        		org.unitime.timetable.model.CurriculumReservation cr = (org.unitime.timetable.model.CurriculumReservation)reservation;
        		List<String> classifications = new ArrayList<String>();
        		for (AcademicClassification clasf: cr.getClassifications())
        			classifications.add(clasf.getCode());
        		List<String> majors = new ArrayList<String>();
        		for (PosMajor major: cr.getMajors())
        			majors.add(major.getCode());
        		r = new CurriculumReservation(reservation.getUniqueId(), (reservation.getLimit() == null ? -1.0 : reservation.getLimit()),
        				offering, cr.getArea().getAcademicAreaAbbreviation(), classifications, majors);
        	} else if (reservation instanceof org.unitime.timetable.model.CourseReservation) {
        		CourseOffering co = ((org.unitime.timetable.model.CourseReservation)reservation).getCourse();
        		for (Course course: offering.getCourses()) {
        			if (co.getUniqueId().equals(course.getId()))
        				r = new CourseReservation(reservation.getUniqueId(), course);
        		}
        	}
        	if (r == null) {
        		helper.warn("Failed to load reservation " + reservation.getUniqueId() + "."); continue;
        	}
        	r.setExpired(reservation.isExpired());
        	configs: for (InstrOfferingConfig ioc: reservation.getConfigurations()) {
        		for (Config config: offering.getConfigs()) {
        			if (ioc.getUniqueId().equals(config.getId())) {
        				r.addConfig(config);
        				continue configs;
        			}
        		}
        	}
        	classes: for (Class_ c: reservation.getClasses()) {
        		for (Config config: offering.getConfigs()) {
        			for (Subpart subpart: config.getSubparts()) {
        				for (Section section: subpart.getSections()) {
        					if (c.getUniqueId().equals(section.getId())) {
        						r.addSection(section);
        						continue classes;
        					}
        				}
        			}
        		}
        	}
        }
        if (io.isByReservationOnly())
        	new DummyReservation(offering);
        return offering;
    }
    
    public static Student loadStudent(org.unitime.timetable.model.Student s, OnlineSectioningServer server, OnlineSectioningHelper helper) {
        Student student = new Student(s.getUniqueId());
        student.setExternalId(s.getExternalUniqueId());
        student.setName(s.getName(ApplicationProperties.getProperty("unitime.enrollment.student.name", DepartmentalInstructor.sNameFormatLastFirstMiddle)));
        student.setStatus(s.getSectioningStatus() == null ? null : s.getSectioningStatus().getReference());
        student.setEmailTimeStamp(s.getScheduleEmailedDate() == null ? null : s.getScheduleEmailedDate().getTime());
        
        for (Iterator i=s.getAcademicAreaClassifications().iterator();i.hasNext();) {
            AcademicAreaClassification aac = (AcademicAreaClassification)i.next();
            student.getAcademicAreaClasiffications().add(
                    new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),aac.getAcademicClassification().getCode()));
            for (Iterator j=aac.getAcademicArea().getPosMajors().iterator();j.hasNext();) {
                PosMajor major = (PosMajor)j.next();
                if (s.getPosMajors().contains(major)) {
                    student.getMajors().add(
                            new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),major.getCode()));
                }
                    
            }
        }
        for (StudentGroup g: s.getGroups())
        	student.getMinors().add(new AcademicAreaCode("", g.getGroupAbbreviation()));
        
        if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.requests.save", "false"))) {
        	Date ts = new Date();
    		TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
    			public int compare(CourseDemand d1, CourseDemand d2) {
    				if (d1.isAlternative() && !d2.isAlternative()) return 1;
    				if (!d1.isAlternative() && d2.isAlternative()) return -1;
    				int cmp = d1.getPriority().compareTo(d2.getPriority());
    				if (cmp != 0) return cmp;
    				return d1.getUniqueId().compareTo(d2.getUniqueId());
    			}
    		});
    		Date enrollmentTS = null;
    		String approval = null;
    		demands.addAll(s.getCourseDemands());
            for (CourseDemand cd: demands) {
                if (cd.getFreeTime()!=null) {
                	TimeLocation ft = new TimeLocation(
                            cd.getFreeTime().getDayCode(),
                            cd.getFreeTime().getStartSlot(),
                            cd.getFreeTime().getLength(),
                            0, 0, -1l, "", server.getAcademicSession().getFreeTimePattern(), 0);
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
                    List<Choice> classSelections = new ArrayList<Choice>();
                    for (org.unitime.timetable.model.CourseRequest cr: crs) {
                        Course course = server.getCourse(cr.getCourseOffering().getUniqueId());
                        if (course==null) {
                            helper.warn("Student " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") requests course " + cr.getCourseOffering().getCourseName() + " that is not loaded.");
                            continue;
                        }
                        if (assignedConfig==null) {
                            HashSet<Long> subparts = new HashSet<Long>();
                            for (Iterator<StudentClassEnrollment> i = (cr.getClassEnrollments() == null ? s.getClassEnrollments() : cr.getClassEnrollments()).iterator(); i.hasNext(); ) {
                            	StudentClassEnrollment enrl = i.next();
                            	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                                if (section!=null) {
                                	if (enrollmentTS == null || (enrl.getTimestamp() != null && enrl.getTimestamp().after(enrollmentTS)))
                                		enrollmentTS = enrl.getTimestamp();
                                	if (approval == null && enrl.getApprovedBy() != null && enrl.getApprovedDate() != null) {
										TimetableManager mgr = (TimetableManager)helper.getHibSession().createQuery(
												"from TimetableManager where externalUniqueId = :externalId")
												.setString("externalId", enrl.getApprovedBy())
												.setCacheable(true).setMaxResults(1).uniqueResult();
										if (mgr != null) {
											approval = enrl.getApprovedDate().getTime() + ":" + enrl.getApprovedBy() + ":" + mgr.getName();
										} else {
											DepartmentalInstructor instr = (DepartmentalInstructor)helper.getHibSession().createQuery(
													"from DepartmentalInstructor where externalUniqueId = :externalId and department.session.uniqueId = :sessionId")
        											.setString("externalId", enrl.getApprovedBy())
        											.setLong("sessionId", server.getAcademicSession().getUniqueId())
        											.setCacheable(true).setMaxResults(1).uniqueResult();
											approval = enrl.getApprovedDate().getTime() + ":" + enrl.getApprovedBy() + ":" +
												(instr == null ? enrl.getApprovedBy() : instr.nameLastNameFirst());
										}
                                	}
                                	if (section.getTime() != null && !assignedSections.isEmpty()) {
                                		for (Section other: assignedSections) {
                                			if (other.isOverlapping(section)) {
                            					helper.warn("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): "+
                            							section.getSubpart().getName() + " " + section.getName() + " " + section.getTime().getLongName() +
                            							" overlaps with " + other.getSubpart().getConfig().getOffering().getName() + " " + other.getSubpart().getName() + " " +
                            							other.getName() + " " + other.getTime().getLongName());
                            				}
                                		}
                                	}
                                    assignedSections.add(section);
                                    if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                                    	helper.warn("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                                    }
                                    assignedConfig = section.getSubpart().getConfig();
                                    if (!subparts.add(section.getSubpart().getId())) {
                                    	helper.warn("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                                    }
                                } else {
                                	if (enrl.getCourseOffering().getUniqueId().equals(course.getId()))
                                		helper.warn("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel() + " not known.");
                                }
                            }
                        }
                        courses.addElement(course);
                        if (cr.getClassWaitLists() != null) {
                        	for (ClassWaitList cwl: cr.getClassWaitLists()) {
                        		Section section = course.getOffering().getSection(cwl.getClazz().getUniqueId());
                        		if (section != null)
                        			classSelections.add(section.getChoice());
                        	}
                        }
                    }
                    if (courses.isEmpty())
                    	continue;
                    CourseRequest request = new CourseRequest(
                            cd.getUniqueId(),
                            cd.getPriority(),
                            cd.isAlternative(),
                            student,
                            courses,
                            (cd.isWaitlist() != null && cd.isWaitlist()), // || assignedConfig != null,
                            (cd.getTimestamp() == null ? ts.getTime() : cd.getTimestamp().getTime()));
                    if (!classSelections.isEmpty()) {
                    	request.getSelectedChoices().addAll(classSelections);
            			helper.info("Selections for " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): " + classSelections);
                    }
                    if (assignedConfig != null) {
                        Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections);
                        if (enrollmentTS != null)
                        	enrollment.setTimeStamp(enrollmentTS.getTime());
                        if (approval != null)
                        	enrollment.setApproval(approval);
                        request.setInitialAssignment(enrollment);
                        if (assignedSections.size() != assignedConfig.getSubparts().size()) {
                        	helper.warn("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") wrong number of classes (" +
                        			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + ").");
                        }
                        for (Request r: student.getRequests()) {
                        	if (r.equals(request) || r.getInitialAssignment() == null) continue;
                        	if (r.getInitialAssignment().isOverlapping(request.getInitialAssignment())) {
                        		helper.warn("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): "+
                        				" overlaps with " + r.getName());
                        	}
                        }
                    }
                }
            }
        }
        
        if (!s.getClassEnrollments().isEmpty() || !s.getWaitlists().isEmpty()) {
        	TreeSet<Course> courses = new TreeSet<Course>(new Comparator<Course>() {
        		public int compare(Course c1, Course c2) {
        			return (c1.getSubjectArea() + " " + c1.getCourseNumber()).compareTo(c2.getSubjectArea() + " " + c2.getCourseNumber());
        		}
        	});
        	Map<Long, Long> timeStamp = new Hashtable<Long, Long>();
        	for (Iterator<StudentClassEnrollment> i = s.getClassEnrollments().iterator(); i.hasNext(); ) {
        		StudentClassEnrollment enrl = i.next();
        		if (enrl.getCourseRequest() != null) continue; // already loaded
        		Course course = server.getCourse(enrl.getCourseOffering().getUniqueId());
                if (course==null) {
                    helper.warn("Student " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") requests course " + enrl.getCourseOffering().getCourseName()+" that is not loaded.");
                    continue;
                }
                if (enrl.getTimestamp() != null) timeStamp.put(enrl.getCourseOffering().getUniqueId(), enrl.getTimestamp().getTime());
                courses.add(course);
        	}
        	for (WaitList w: s.getWaitlists()) {
        		Course course = server.getCourse(w.getCourseOffering().getUniqueId());
                if (course==null) {
                	helper.warn("Student " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") requests course " + w.getCourseOffering().getCourseName()+" that is not loaded.");
                    continue;
                }
                if (w.getTimestamp() != null) timeStamp.put(w.getCourseOffering().getUniqueId(), w.getTimestamp().getTime());
                courses.add(course);
        	}
        	for (Course course: courses) {
        		Vector<Course> cx = new Vector<Course>(); cx.add(course);
        		CourseRequest request = null;
        		for (Request r: student.getRequests()) {
        			if (r instanceof CourseRequest && r.getAssignment() == null && ((CourseRequest)r).getCourses().contains(course)) {
        				request = (CourseRequest)r;
        				break;
        			}
        		}
        		if (request == null) {
                    request = new CourseRequest(
                            - course.getId(),
                            student.getRequests().size(),
                            false,
                            student,
                            cx,
                            false,
                            timeStamp.get(course.getId()));
        		}
                HashSet<Section> assignedSections = new HashSet<Section>();
                Config assignedConfig = null;
                HashSet<Long> subparts = new HashSet<Long>();
                Date enrollmentTS = null;
                for (Iterator<StudentClassEnrollment> i = s.getClassEnrollments().iterator(); i.hasNext(); ) {
                	StudentClassEnrollment enrl = i.next();
                	if (course.getId() != enrl.getCourseOffering().getUniqueId()) continue;
                	if (enrollmentTS == null || (enrl.getTimestamp() != null && enrl.getTimestamp().after(enrollmentTS)))
                		enrollmentTS = enrl.getTimestamp();
                	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                    if (section!=null) {
                    	if (section.getTime() != null && !assignedSections.isEmpty()) {
                    		for (Section other: assignedSections) {
                				if (other.isOverlapping(section)) {
                					helper.warn("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): "+
                							section.getSubpart().getName() + " " + section.getName() + " " + section.getTime().getLongName() +
                							" overlaps with " + other.getSubpart().getConfig().getOffering().getName() + " " + other.getSubpart().getName() + " " +
                							other.getName() + " " + other.getTime().getLongName());
                				}
                    		}
                    	}
                        assignedSections.add(section);
                        if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                        	helper.warn("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                        }
                        assignedConfig = section.getSubpart().getConfig();
                        if (!subparts.add(section.getSubpart().getId())) {
                        	helper.warn("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                        }
                    } else {
                    	helper.warn("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel() + " not known.");
                    	Section x = server.getSection(enrl.getClazz().getUniqueId());
                    	if (x != null) {
                    		helper.info("  but a class with the same id is loaded, but under offering " + x.getSubpart().getConfig().getOffering().getName() + " (id is " + x.getSubpart().getConfig().getOffering().getId() + 
                    				", expected " +course.getOffering().getId() + ")");
                    	}
                    }
                }
                if (assignedConfig!=null) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections);
                    if (enrollmentTS != null)
                    	enrollment.setTimeStamp(enrollmentTS.getTime());
                    request.setInitialAssignment(enrollment);
                    if (assignedSections.size() != assignedConfig.getSubparts().size()) {
                    	helper.warn("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): wrong number of classes (" +
                    			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + ").");
                    }
                    for (Request r: student.getRequests()) {
                    	if (r.equals(request) || r.getInitialAssignment() == null) continue;
                    	if (r.getInitialAssignment().isOverlapping(request.getInitialAssignment())) {
                    		helper.warn("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): "+
                    				" overlaps with " + r.getName());
                    	}
                    }
                }
        	}
        }
        
        return student;
    }    

    public static String datePatternName(TimeLocation time, AcademicSessionInfo session) {
    	if (time.getWeekCode().isEmpty()) return time.getDatePatternName();
    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(session.getDatePatternFirstDate());
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
    	cal.setTime(session.getDatePatternFirstDate());
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
        Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
    	return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    }
    
	@Override
    public String name() { return "reload-all"; }
	
}
