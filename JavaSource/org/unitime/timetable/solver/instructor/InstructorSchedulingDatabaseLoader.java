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
package org.unitime.timetable.solver.instructor;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.util.ProblemLoader;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.instructor.constraints.SameInstructorConstraint;
import org.cpsolver.instructor.model.Attribute;
import org.cpsolver.instructor.model.Course;
import org.cpsolver.instructor.model.EnrolledClass;
import org.cpsolver.instructor.model.Instructor;
import org.cpsolver.instructor.model.InstructorSchedulingModel;
import org.cpsolver.instructor.model.Preference;
import org.cpsolver.instructor.model.Section;
import org.cpsolver.instructor.model.TeachingAssignment;
import org.cpsolver.instructor.model.TeachingRequest;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
public class InstructorSchedulingDatabaseLoader extends ProblemLoader<TeachingRequest.Variable, TeachingAssignment, InstructorSchedulingModel> {
	private Progress iProgress = null;
	private Set<Long> iSolverGroupId = new HashSet<Long>();
	private String iInstructorFormat;
	private Map<Long, Attribute.Type> iAttributeTypes = new HashMap<Long, Attribute.Type>();
	private Map<Long, Attribute> iAttributes = new HashMap<Long, Attribute>();
	private Map<Long, Attribute> iDepartmentAttribute = new HashMap<Long, Attribute>();
	private Map<Long, Instructor> iInstructors = new HashMap<Long, Instructor>();
	private boolean iHasCommitted = false;
	private String iDefaultSameCourse = null, iDefaultSameCommon = null;
	
    public InstructorSchedulingDatabaseLoader(InstructorSchedulingModel model, Assignment<TeachingRequest.Variable, TeachingAssignment> assignment) {
    	super(model, assignment);
    	iProgress = Progress.getInstance(model);
    	// iSessionId = model.getProperties().getPropertyLong("General.SessionId", (Long)null);
    	for (Long id: model.getProperties().getPropertyLongArry("General.SolverGroupId", null))
    		iSolverGroupId.add(id);
    	iInstructorFormat = getModel().getProperties().getProperty("General.InstructorFormat", NameFormat.LAST_FIRST.reference());
    	iDefaultSameCourse = getModel().getProperties().getProperty("Defaults.SameCourse", "R");
    	iDefaultSameCommon = getModel().getProperties().getProperty("Defaults.SameCommon", "R");
    }
    
    public void load() throws Exception {
    	org.hibernate.Session hibSession = null;
    	Transaction tx = null;
    	try {
    		hibSession = TimetableManagerDAO.getInstance().createNewSession();
    		hibSession.setCacheMode(CacheMode.IGNORE);
    		hibSession.setFlushMode(FlushMode.COMMIT);
    		
    		tx = hibSession.beginTransaction(); 
    		
    		load(hibSession);
    		
    		tx.commit();
    	} catch (Exception e) {
    		iProgress.fatal("Unable to load input data, reason: " + e.getMessage(), e);
    		tx.rollback();
    	} finally {
    		// here we need to close the session since this code may run in a separate thread
    		if (hibSession != null && hibSession.isOpen()) hibSession.close();
    	}
    }
    
    protected void load(org.hibernate.Session hibSession) throws Exception {
    	iProgress.setStatus("Loading input data ...");
    	List<Department> departments = (List<Department>)hibSession.createQuery(
    			"from Department d where d.solverGroup.uniqueId in :solverGroupId"
    			).setParameterList("solverGroupId", iSolverGroupId).list();
    	if (departments.size() > 1) {
    		Attribute.Type dt = new Attribute.Type(-1, "Department", false, false);
    		getModel().addAttributeType(dt);
    		for (Department d: departments)
    			iDepartmentAttribute.put(d.getUniqueId(), new Attribute(-d.getUniqueId(), d.getDeptCode(), dt));
    	}
    	
    	loadInstructors(hibSession);
    	
    	loadRequests(hibSession);
        
    	createAssignment();
        
        getModel().getProperties().setProperty("Save.Commit", !iHasCommitted ? "false" : "true");
    }
    
    protected Attribute getAttribute(InstructorAttribute a) {
    	Attribute attribute = iAttributes.get(a.getUniqueId());
    	if (attribute == null) {
    		Attribute.Type type = iAttributeTypes.get(a.getType().getUniqueId());
    		if (type == null) {
    			type = new Attribute.Type(a.getType().getUniqueId(), a.getType().getLabel(), a.getType().isConjunctive(), a.getType().isRequired());
    			iAttributeTypes.put(a.getType().getUniqueId(), type);
    			getModel().addAttributeType(type);
    		}
    		attribute = new Attribute(a.getUniqueId(), a.getName(), type);
    		iAttributes.put(a.getUniqueId(), attribute);
    	}
    	return attribute;
    }
    
    protected void loadDistributionPreferences(Instructor instructor, DistributionPref dp) {
		if ("BTB_TIME".equals(dp.getDistributionType().getReference()) || "BTB".equals(dp.getDistributionType().getReference())) {
			instructor.setBackToBackPreference(Constants.preference2preferenceLevel(dp.getPrefLevel().getPrefProlog()));
		}
    }
    
    protected void loadTimePreferences(Instructor instructor, TimePref tp) {
		TimePatternModel m = tp.getTimePatternModel();
		boolean out[][] = new boolean [m.getNrDays()][m.getNrTimes()];
        for (int i = 0; i < m.getNrDays(); i++)
               for (int j = 0; j < m.getNrTimes(); j++)
            	   out[i][j] = false;
        for (int i = 0; i < m.getNrDays(); i++)
            for (int j = 0; j < m.getNrTimes(); j++) {
         	   if (out[i][j]) continue;
         	   out[i][j] = true;
         	   if (PreferenceLevel.sNeutral.equals(m.getPreference(i, j))) continue;
         	   int endDay = i, endTime = j;
         	   while (endTime + 1 < m.getNrTimes() && !out[i][endTime+1] && m.getPreference(i, endTime+1).equals(m.getPreference(i, j)))
         		   endTime++;
         	   if (i == 0) {
         		   boolean same = true;
         		   for (int k = i; k + 1 < m.getNrDays(); k++) {
             		   for (int x = j; x <= endTime; x++) {
             			   if (!out[k+1][x] && !m.getPreference(i, x).equals(m.getPreference(k+1, x))) {
             				   same = false; break;
             			   }
             			   if (!same) break;
             		   }
             		   if (!same) break;
         		   }
         		   if (same) endDay = m.getNrDays()-1;
         	   }
         	   while (endDay + 1 < m.getNrDays()) {
         		   boolean same = true;
         		   for (int x = j; x <= endTime; x++)
         			   if (!out[endDay+1][x] && !m.getPreference(i, x).equals(m.getPreference(endDay+1, x))) {
         				   same = false; break;
         			   }
         		   if (!same) break;
         		   endDay++;
         	   }
         	   for (int a = i; a <= endDay;a++)
         		   for (int b = j; b <= endTime;b++)
         			   out[a][b] = true;

         	   int dayCode = 0;
         	   for (int a = i; a <= endDay; a++)
         		   dayCode |= m.getDayCode(a);
         	   TimeLocation time =  new TimeLocation(dayCode, m.getStartSlot(j), m.getStartSlot(endTime) - m.getStartSlot(j) + m.getSlotsPerMtg(), 0, 0.0,
         			   null, "", null, m.getBreakTime());
         	   instructor.addTimePreference(new Preference<TimeLocation>(time, Constants.preference2preferenceLevel(m.getPreference(i, j))));
            }
    }
    
    public static String getClassExternalId(CourseOffering course, Class_ clazz) {
    	String section = clazz.getClassSuffix(course == null ? clazz.getSchedulingSubpart().getControllingCourseOffering() : course);
    	if (section != null && !section.isEmpty()) return section;
    	String extId = clazz.getExternalId(course == null ? clazz.getSchedulingSubpart().getControllingCourseOffering() : course);
    	if (extId != null && !extId.isEmpty()) return extId;
    	return clazz.getSectionNumberString();
    }
    
    public static List<EnrolledClass> loadUnavailability(org.hibernate.Session hibSession, DepartmentalInstructor di) {
    	List<EnrolledClass> ret = new ArrayList<EnrolledClass>();
    	if (di.getExternalUniqueId() != null) {
    		List<StudentClassEnrollment> enrollments = (List<StudentClassEnrollment>)hibSession.createQuery(
    				"from StudentClassEnrollment e where e.student.session.uniqueId = :sessionId and e.student.externalUniqueId = :externalId and e.clazz.cancelled = false"
    				).setLong("sessionId", di.getDepartment().getSessionId()).setString("externalId", di.getExternalUniqueId()).setCacheable(true).list();
    		for (StudentClassEnrollment enrollment: enrollments) {
    			org.unitime.timetable.model.Assignment assignment = enrollment.getClazz().getCommittedAssignment();
    			if (assignment != null) {
    				String rooms = null;
    				for (Location loc: assignment.getRooms()) {
    					if (rooms == null) rooms = loc.getLabel();
    					else rooms += ", " + loc.getLabel();
    				}
    				DatePattern datePattern = assignment.getDatePattern();
    				ret.add(new EnrolledClass(
    						enrollment.getCourseOffering().getUniqueId(),
    						enrollment.getClazz().getUniqueId(),
    						enrollment.getCourseOffering().getCourseName(),
    						enrollment.getClazz().getSchedulingSubpart().getItypeDesc().trim(),
    						enrollment.getClazz().getClassLabel(enrollment.getCourseOffering()),
    						getClassExternalId(enrollment.getCourseOffering(), enrollment.getClazz()),
    						assignment.getDays().intValue(),
    						assignment.getStartSlot().intValue(),
    						assignment.getSlotPerMtg(),
    						enrollment.getClazz().getUniqueId(),
    						(datePattern == null ? "" : datePattern.getName()),
    						(datePattern == null ? new BitSet() : datePattern.getPatternBitSet()),
    						assignment.getBreakTime(),
    						rooms,
    						false
    						));
    			}
    		}
    		List<ClassInstructor> classInstructors = (List<ClassInstructor>)hibSession.createQuery(
    				"from ClassInstructor ci where ci.instructor.externalUniqueId = :externalId and ci.instructor.department.session.uniqueId = :sessionId and " +
    				"ci.instructor.department.uniqueId != :departmentId and ci.lead = true and ci.classInstructing.cancelled = false"
    				).setLong("sessionId", di.getDepartment().getSessionId()).setString("externalId", di.getExternalUniqueId()).setLong("departmentId", di.getDepartment().getUniqueId()).setCacheable(true).list();
    		for (ClassInstructor ci: classInstructors) {
        		org.unitime.timetable.model.Assignment assignment = ci.getClassInstructing().getCommittedAssignment();
        		if (assignment != null) {
        			String rooms = null;
    				for (Location loc: assignment.getRooms()) {
    					if (rooms == null) rooms = loc.getLabel();
    					else rooms += ", " + loc.getLabel();
    				}
        			DatePattern datePattern = assignment.getDatePattern();
        			Class_ clazz = ci.getClassInstructing();
        			CourseOffering course = clazz.getSchedulingSubpart().getControllingCourseOffering();
        			ret.add(new EnrolledClass(
        					course.getUniqueId(),
    						clazz.getUniqueId(),
    						course.getCourseName(),
    						clazz.getSchedulingSubpart().getItypeDesc().trim(),
    						clazz.getClassLabel(course),
    						getClassExternalId(course, clazz),
    						assignment.getDays().intValue(),
    						assignment.getStartSlot().intValue(),
    						assignment.getSlotPerMtg(),
    						ci.getClassInstructing().getUniqueId(),
    						(datePattern == null ? "" : datePattern.getName()),
    						(datePattern == null ? new BitSet() : datePattern.getPatternBitSet()),
    						assignment.getBreakTime(),
    						rooms,
    						true
    						));
    			}
    		}
    	}
    	for (ClassInstructor ci: di.getClasses()) {
    		if (!ci.isLead() || ci.getClassInstructing().isCancelled()) continue;
    		if (ci.getTeachingRequest() != null) continue;
    		org.unitime.timetable.model.Assignment assignment = ci.getClassInstructing().getCommittedAssignment();
    		if (assignment != null) {
    			String rooms = null;
				for (Location loc: assignment.getRooms()) {
					if (rooms == null) rooms = loc.getLabel();
					else rooms += ", " + loc.getLabel();
				}
    			DatePattern datePattern = assignment.getDatePattern();
    			Class_ clazz = ci.getClassInstructing();
    			CourseOffering course = clazz.getSchedulingSubpart().getControllingCourseOffering();
    			ret.add(new EnrolledClass(
    					course.getUniqueId(),
						clazz.getUniqueId(),
						course.getCourseName(),
						clazz.getSchedulingSubpart().getItypeDesc().trim(),
						clazz.getClassLabel(course),
						getClassExternalId(course, clazz),
						assignment.getDays().intValue(),
						assignment.getStartSlot().intValue(),
						assignment.getSlotPerMtg(),
						ci.getClassInstructing().getUniqueId(),
						(datePattern == null ? "" : datePattern.getName()),
						(datePattern == null ? new BitSet() : datePattern.getPatternBitSet()),
						assignment.getBreakTime(),
						rooms,
						true
						));
			}
    	}
    	return ret;
    }
    
    protected void loadInstructors(org.hibernate.Session hibSession) throws Exception {
    	List<DepartmentalInstructor> list = (List<DepartmentalInstructor>)hibSession.createQuery(
    			"select distinct i from DepartmentalInstructor i, SolverGroup g inner join g.departments d where " +
    			"g.uniqueId in :solverGroupId and i.department = d and i.teachingPreference.prefProlog != :prohibited and i.maxLoad > 0.0"
    			).setParameterList("solverGroupId", iSolverGroupId).setString("prohibited", PreferenceLevel.sProhibited).list();
    	iProgress.setPhase("Loading instructors...", list.size());
    	for (DepartmentalInstructor i: list) {
    		Instructor instructor = new Instructor(i.getUniqueId(), i.getExternalUniqueId(), i.getName(iInstructorFormat),
    				Constants.preference2preferenceLevel(i.getTeachingPreference().getPrefProlog()), i.getMaxLoad());
    		for (InstructorAttribute a: i.getAttributes())
    			instructor.addAttribute(getAttribute(a));
    		if (!iDepartmentAttribute.isEmpty())
    			instructor.addAttribute(iDepartmentAttribute.get(i.getDepartment().getUniqueId()));
    		for (org.unitime.timetable.model.Preference p: i.getPreferences()) {
    			if (p instanceof InstructorCoursePref) {
    				InstructorCoursePref cp = (InstructorCoursePref)p;
    				instructor.addCoursePreference(new Preference<Course>(new Course(cp.getCourse().getUniqueId(), cp.getCourse().getCourseName()), Constants.preference2preferenceLevel(cp.getPrefLevel().getPrefProlog())));
    			} else if (p instanceof DistributionPref) {
    				loadDistributionPreferences(instructor, (DistributionPref)p);
    			} else if (p instanceof TimePref) {
    				loadTimePreferences(instructor, (TimePref)p);
    			}
    		}
    		for (EnrolledClass ec: loadUnavailability(hibSession, i))
    			instructor.addTimePreference(new Preference<TimeLocation>(ec, Constants.sPreferenceLevelProhibited));
    		getModel().addInstructor(instructor);
    		iInstructors.put(i.getUniqueId(), instructor);
    		iProgress.incProgress();
    	}
    }
    
    protected Section getSection(TeachingClassRequest req) {
    	Class_ clazz = req.getTeachingClass();
		CourseOffering course = clazz.getSchedulingSubpart().getControllingCourseOffering();
		String room = null;
		TimeLocation time = null;
		org.unitime.timetable.model.Assignment assignment = clazz.getCommittedAssignment();
		if (assignment != null) {
			time = assignment.getTimeLocation();
			for (Location location: assignment.getRooms()) {
				if (room == null) room = location.getLabel();
				else room += ", " + location.getLabel();
			}
		}
		return new Section(clazz.getUniqueId(), getClassExternalId(course, clazz),
				clazz.getSchedulingSubpart().getItypeDesc().trim(), clazz.getClassLabel(course),
				time, room, req.isCanOverlap(), req.isCommon());
    }
    
    protected String toHtml(Class_ clazz) {
    	return "<A href='classDetail.do?cid="+clazz.getUniqueId()+"'>"+clazz.getClassLabel()+"</A>";
    }
    
    protected String toHtml(DepartmentalInstructor instructor) {
    	return "<a href='instructorDetail.do?instructorId=" + instructor.getUniqueId() + "&deptId=" + instructor.getDepartment().getUniqueId() + "'>" + instructor.getName(iInstructorFormat) + "</a>";
    }
    
    protected String toHtml(TeachingAssignment assignment) {
    	return "<a href='instructorDetail.do?instructorId=" + assignment.getInstructor().getInstructorId() + "'>" + assignment.getInstructor().getName() + "</a>";
    }
    
    protected String toHtml(org.unitime.timetable.model.TeachingRequest request) {
    	String sections = "";
    	for (TeachingClassRequest tcr: new TreeSet<TeachingClassRequest>(request.getClassRequests())) {
    		sections += (sections.isEmpty() ? "" : ", ") + (tcr.isAssignInstructor() ? "" : "<i>") + toHtml(tcr.getTeachingClass()) + (tcr.isAssignInstructor() ? "" : "</i>");
    	}
    	return "<a href='instructionalOfferingDetail.do?io=" + request.getOffering().getUniqueId() + "&requestId=" + request.getUniqueId() + "&op=view#instructors'>" + request.getOffering().getCourseName() + "</a> " + sections;
    }
    
    protected String toHtml(TeachingRequest request) {
    	return "<a href='instructionalOfferingDetail.do?co=" + request.getCourse().getCourseId() + "&requestId=" + request.getRequestId() + "&op=view#instructors'>" + request.getCourse().getCourseName() + (request.getSections().isEmpty() ? "" : " " + request.getSections()) + "</a>";
    }
    
    protected String toHtml(TeachingRequest.Variable variable) {
    	return "<a href='instructionalOfferingDetail.do?co=" + variable.getCourse().getCourseId() + "&requestId=" + variable.getRequest().getRequestId() + "&op=view#instructors'>" + variable.getRequest().getCourse().getCourseName() +
    			(variable.getRequest().getNrInstructors() != 1 ? "/" + (1 + variable.getInstructorIndex()) : "") + (variable.getRequest().getSections().isEmpty() ? "" : " " + variable.getRequest().getSections()) + "</a>";
    }
    
    protected void loadRequest(org.hibernate.Session hibSession, org.unitime.timetable.model.TeachingRequest r) {
    	Course course = new Course(r.getOffering().getControllingCourseOffering().getUniqueId(), r.getOffering().getCourseName());
    	List<Section> sections = new ArrayList<Section>();
    	for (TeachingClassRequest cr: new TreeSet<TeachingClassRequest>(r.getClassRequests())) {
    		sections.add(getSection(cr));
    	}
		TeachingRequest request = new TeachingRequest(r.getUniqueId(), r.getNbrInstructors(), course, r.getTeachingLoad(), sections,
				Constants.preference2preferenceLevel(r.getSameCoursePreference() == null ? iDefaultSameCourse : r.getSameCoursePreference().getPrefProlog()),
				Constants.preference2preferenceLevel(r.getSameCommonPart() == null ? iDefaultSameCommon : r.getSameCommonPart().getPrefProlog())
				);
		for (Iterator it = r.getPreferences(InstructorPref.class).iterator(); it.hasNext(); ) {
			InstructorPref p = (InstructorPref)it.next();
			Instructor instructor = iInstructors.get(p.getInstructor().getUniqueId());
			if (instructor != null) {
				request.addInstructorPreference(new Preference<Instructor>(instructor, Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog())));
			}
		}
		for (Iterator it = r.getPreferences(InstructorAttributePref.class).iterator(); it.hasNext(); ) {
			InstructorAttributePref p = (InstructorAttributePref)it.next();
			request.addAttributePreference(new Preference<Attribute>(getAttribute(p.getAttribute()), Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog())));
		}
		if (!iDepartmentAttribute.isEmpty()) {
			request.addAttributePreference(new Preference<Attribute>(iDepartmentAttribute.get(r.getOffering().getControllingCourseOffering().getSubjectArea().getDepartment().getUniqueId()), Constants.sPreferenceLevelRequired));
		}
		getModel().addRequest(request);
		int index = 0;
		List<DepartmentalInstructor> instructors = new ArrayList<DepartmentalInstructor>(r.getAssignedInstructors());
		Collections.sort(instructors);
		for (DepartmentalInstructor di: instructors) {
			Instructor instructor = iInstructors.get(di.getUniqueId());
			if (instructor == null || index >= r.getNbrInstructors()) {
				iProgress.warn("Instructor " + toHtml(di) + " is assigned to " + toHtml(r) + ", but not allowed for automatic assignment.");
			} else {
				request.getVariable(index).setInitialAssignment(new TeachingAssignment(request.getVariable(index), instructor));
				index++;
			}
    	}
		if (request.getNrInstructors() > 1) {
    		SameInstructorConstraint diffInstructor = new SameInstructorConstraint(r.getUniqueId(), r.getOffering().getCourseName(), Constants.sPreferenceProhibited);
    		for (TeachingRequest.Variable var: request.getVariables())
    			diffInstructor.addVariable(var);
    		getModel().addConstraint(diffInstructor);
    	}
    }
    
    protected void loadRequests(org.hibernate.Session hibSession) throws Exception {
    	List<org.unitime.timetable.model.TeachingRequest> requests = (List<org.unitime.timetable.model.TeachingRequest>)hibSession.createQuery(
    			"select r from TeachingRequest r inner join r.offering.courseOfferings co where co.isControl = true and co.subjectArea.department.solverGroup.uniqueId in :solverGroupId")
    			.setParameterList("solverGroupId", iSolverGroupId).list();
    	iProgress.setPhase("Loading requests...", requests.size());
    	for (org.unitime.timetable.model.TeachingRequest request: requests) {
    		iProgress.incProgress();
    		if (!request.isCancelled()) loadRequest(hibSession, request);
    	}
    }
    
    protected void createAssignment() {
    	iProgress.setPhase("Creating initial assignment...", getModel().variables().size());
    	requests: for (TeachingRequest.Variable request: getModel().variables()) {
    		iProgress.incProgress();
    		TeachingAssignment assignment = request.getInitialAssignment();
    		if (assignment == null) continue;
			if (assignment.getInstructor().getTimePreference(request.getRequest()).isProhibited()) {
				iProgress.warn("Unable to assign " + toHtml(request) +" &larr; " + toHtml(assignment) + ": instructor is not available.");
				continue;
			}
			if (assignment.getInstructor().getCoursePreference(request.getCourse()).isProhibited()) {
				iProgress.warn("Unable to assign " + toHtml(request) +" &larr; " + toHtml(assignment) + ": course " + request.getCourse().getCourseName() + " is prohibited.");
				continue;
			}
			if (request.getRequest().getInstructorPreference(assignment.getInstructor()).isProhibited()) {
				iProgress.warn("Unable to assign " + toHtml(request) +" &larr; " + toHtml(assignment) + ": instructor " + assignment.getInstructor().getName() + " is prohibited.");
				continue;
			}
			for (Attribute.Type type: getModel().getAttributeTypes()) {
				int pref = request.getRequest().getAttributePreference(assignment.getInstructor(), type);
				if (Constants.sPreferenceProhibited.equals(Constants.preferenceLevel2preference(pref))) {
					iProgress.warn("Unable to assign " + toHtml(request) +" &larr; " + toHtml(assignment) + ": probibited by attribute type " + type.getTypeName() + ".");
					continue requests;
				}
			}
			if (!assignment.getInstructor().canTeach(request.getRequest()) || request.getRequest().getAttributePreference(assignment.getInstructor()).isProhibited()) {
				iProgress.warn("Unable to assign " + toHtml(request) +" &larr; " + toHtml(assignment) + ": assignment not valid.");
				continue;
			}
			getModel().weaken(getAssignment(), assignment);
			Map<Constraint<TeachingRequest.Variable, TeachingAssignment>, Set<TeachingAssignment>> conflictConstraints = getModel().conflictConstraints(getAssignment(), assignment);
        	if (conflictConstraints.isEmpty()) {
        		getAssignment().assign(0, assignment);
	        } else {
                String warn = "Unable to assign " + toHtml(request) +" &larr; " + toHtml(assignment);
                warn += "<br>&nbsp;&nbsp;Reason:";
                for (Constraint<TeachingRequest.Variable, TeachingAssignment> c: conflictConstraints.keySet()) {
                	Set<TeachingAssignment> vals = conflictConstraints.get(c);
                	for (TeachingAssignment v: vals) {
                        warn += "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + toHtml(v.variable()) + " = " + toHtml(v);
            	    }
                    warn += "<br>&nbsp;&nbsp;&nbsp;&nbsp;    in constraint " + c;
                    iProgress.warn(warn);
    	        }
	        }
    	}
    }
}