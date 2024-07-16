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
package org.unitime.timetable.solver.studentsct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.model.Model;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.termination.TerminationCondition;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.Callback;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.ifs.util.ProblemLoader;
import org.cpsolver.ifs.util.ProblemSaver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ProgressWriter;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.StudentSectioningXMLLoader;
import org.cpsolver.studentsct.StudentSectioningXMLSaver;
import org.cpsolver.studentsct.constraint.LinkedSections;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.online.expectations.NeverOverExpected;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumOverride;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.LearningCommunityReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.cpsolver.studentsct.reservation.UniversalOverride;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMCDATA;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.sectioning.SectioningReports.ReportTypeInterface;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.GroupOverrideReservation;
import org.unitime.timetable.model.SectioningSolutionLog;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.StudentSchedulingRule.Mode;
import org.unitime.timetable.model.dao.LearningCommunityReservationDAO;
import org.unitime.timetable.model.dao.SectioningSolutionLogDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.model.dao.StudentGroupReservationDAO;
import org.unitime.timetable.model.dao.StudentSchedulingRuleDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.match.StudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XClassEnrollment;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRule;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRules;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.server.sectioning.SectioningReportTypesBackend.ReportType;
import org.unitime.timetable.solver.AbstractSolver;
import org.unitime.timetable.solver.SolverDisposeListener;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.MemoryCounter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


/**
 * @author Tomas Muller
 */
public class StudentSolver extends AbstractSolver<Request, Enrollment, StudentSectioningModel> implements StudentSolverProxy {
	private static StudentSectioningMessages SCT_MSG = Localization.create(StudentSectioningMessages.class);
    private transient Map<Long, XCourse> iCourseInfoCache = null;
    private transient Map<Long, XOffering> iOfferingCache = null;
    private transient Map<String, Set<Long>> iInstructedOfferingsCache = null;
    private transient Map<Long, Student> iStudentCache = null;
    private transient Map<String, Student> iStudentExtCache = null;
    private Map<String, Object> iOnlineProperties = new HashMap<String, Object>();
    private Map<String, InMemoryReport> iReports = new HashMap<String, InMemoryReport>();
    
    public StudentSolver(DataProperties properties, SolverDisposeListener disposeListener) {
        super(properties, disposeListener);
    }
    
	@Override
	protected ProblemSaver<Request, Enrollment, StudentSectioningModel> getDatabaseSaver(Solver<Request, Enrollment> solver) {
		try {
			String saverClass = getProperties().getProperty("General.DatabaseSaver", StudentSectioningDatabaseSaver.class.getName());
			if (saverClass != null && !saverClass.isEmpty())
				return (ProblemSaver<Request, Enrollment, StudentSectioningModel>) Class.forName(saverClass).getConstructor(Solver.class).newInstance(solver);
		} catch (Exception e) {
			iProgress.error("Failed to create a custom database saver: " + e.getMessage(), e);
		}
		return new StudentSectioningDatabaseSaver(solver);
	}

	@Override
	protected ProblemLoader<Request, Enrollment, StudentSectioningModel> getDatabaseLoader(StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
		try {
			String loaderClass = getProperties().getProperty("General.DatabaseLoader", StudentSectioningDatabaseLoader.class.getName());
			if (loaderClass != null && !loaderClass.isEmpty()) {
				try {
					return (ProblemLoader<Request, Enrollment, StudentSectioningModel>) Class.forName(loaderClass).getConstructor(StudentSolver.class, StudentSectioningModel.class, Assignment.class).newInstance(this, model, assignment);
				} catch (NoSuchMethodException e) {
					return (ProblemLoader<Request, Enrollment, StudentSectioningModel>) Class.forName(loaderClass).getConstructor(StudentSectioningModel.class, Assignment.class).newInstance(model, assignment);
				}
			}
		} catch (Exception e) {
			iProgress.error("Failed to create a custom database loader: " + e.getMessage(), e);
		}
		return new StudentSectioningDatabaseLoader(this, model, assignment);
	}
	
	@Override
	protected ProblemSaver<Request, Enrollment, StudentSectioningModel> getCustomValidator(Solver<Request, Enrollment> solver) {
		try {
			String validatorClass = getProperties().getProperty("General.CustomValidator", null);
			if (validatorClass != null && !validatorClass.isEmpty())
				return (ProblemSaver<Request, Enrollment, StudentSectioningModel>) Class.forName(validatorClass).getConstructor(Solver.class).newInstance(solver);
		} catch (Exception e) {
			iProgress.error("Failed to create a custom validator: " + e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public boolean isCanValidate() {
		String validatorClass = getProperties().getProperty("General.CustomValidator", null);
		return validatorClass != null && !validatorClass.isEmpty();
	}

	@Override
	protected StudentSectioningModel createModel(DataProperties properties) {
		return new StudentSectioningModel(properties);
	}
	
	@Override
    public void setInitalSolution(Model<Request, Enrollment> model) {
		setInitalSolution(new Solution(model, new DefaultSingleAssignment<Request, Enrollment>()));
	}

	@Override
	protected Document createCurrentSolutionBackup(boolean anonymize, boolean idconv) {
        getProperties().setProperty("Xml.SaveBest", "true");
        getProperties().setProperty("Xml.SaveInitial", "true");
        getProperties().setProperty("Xml.SaveCurrent", "true");
        if (anonymize) {
            getProperties().setProperty("Xml.ConvertIds", idconv ? "true" : "false");
            getProperties().setProperty("Xml.SaveOnlineSectioningInfo", "true");
            getProperties().setProperty("Xml.SaveStudentInfo", "false");
            getProperties().setProperty("Xml.ShowNames", "false");
        }

        Document document = new StudentSectioningXMLSaver(this).saveDocument();
        
        if (anonymize) {
            getProperties().setProperty("Xml.ConvertIds", "false");
            getProperties().setProperty("Xml.SaveOnlineSectioningInfo", "true");
            getProperties().setProperty("Xml.SaveStudentInfo", "true");
            getProperties().setProperty("Xml.ShowNames", "true");
        }
        
        saveReports(document);
        
        return document;
	}

	@Override
	protected void restureCurrentSolutionFromBackup(Document document) {
        getProperties().setProperty("Xml.LoadBest", "true");
        getProperties().setProperty("Xml.LoadInitial", "true");
        getProperties().setProperty("Xml.LoadCurrent", "true");

        new StudentSectioningXMLLoader(getModel(), currentSolution().getAssignment()).load(document);
        
        readReports(document);
	}
	
	private void clearCachedData() {
		clearCourseInfoTable();
        clearOfferingCache();
        clearInstructedOfferingsCache();
        clearStudentCache();
	}

	@Override
    protected void disposeNoInherit(boolean unregister) {
		super.disposeNoInherit(unregister);
		clearCachedData();
    }
	
	@Override
	protected void afterLoad() {
		super.afterLoad();
		clearCachedData();
    }
	
	@Override
	public void setInitalSolution(Solution<Request, Enrollment> solution) {
		super.setInitalSolution(solution);
		clearCachedData();
	}
	
	@Override
    public Callback getReloadingDoneCallback() {
        return new ReloadingDoneCallback();
    }

    public class ReloadingDoneCallback implements Callback {
        Map<Long, Map<Long, Enrollment>> iCurrentAssignmentTable = new Hashtable<Long, Map<Long,Enrollment>>();
        Map<Long, Map<Long, Enrollment>> iBestAssignmentTable = new Hashtable<Long, Map<Long,Enrollment>>();
        Map<Long, Map<Long, Enrollment>> iInitialAssignmentTable = new Hashtable<Long, Map<Long,Enrollment>>();
        String iSolutionId = null;
        Progress iProgress = null;
        
        public ReloadingDoneCallback() {
            iSolutionId = getProperties().getProperty("General.SolutionId");
            for (Request request: currentSolution().getModel().variables()) {
            	Enrollment enrollment = currentSolution().getAssignment().getValue(request);
            	if (enrollment != null) {
                	Map<Long, Enrollment> assignments = iCurrentAssignmentTable.get(request.getStudent().getId());
                	if (assignments == null) {
                		assignments = new Hashtable<Long, Enrollment>();
                		iCurrentAssignmentTable.put(request.getStudent().getId(), assignments);
                	}
                	assignments.put(request.getId(), enrollment);
            	}
            	if (request.getBestAssignment() != null) {
                	Map<Long, Enrollment> assignments = iBestAssignmentTable.get(request.getStudent().getId());
                	if (assignments == null) {
                		assignments = new Hashtable<Long, Enrollment>();
                		iBestAssignmentTable.put(request.getStudent().getId(), assignments);
                	}
                	assignments.put(request.getId(), request.getBestAssignment());
            	}
            	if (request.getInitialAssignment() != null) {
                	Map<Long, Enrollment> assignments = iInitialAssignmentTable.get(request.getStudent().getId());
                	if (assignments == null) {
                		assignments = new Hashtable<Long, Enrollment>();
                		iInitialAssignmentTable.put(request.getStudent().getId(), assignments);
                	}
                	assignments.put(request.getId(), request.getInitialAssignment());
            	}
            }
        }
        
        private Enrollment getEnrollment(Request request, Enrollment enrollment) {
            if (request instanceof FreeTimeRequest) {
                return ((FreeTimeRequest)request).createEnrollment();
            } else {
                CourseRequest cr = (CourseRequest)request;
                Set<Section> sections = new HashSet<Section>();
                for (Section s: enrollment.getSections()) {
                    Section section = cr.getSection(s.getId());
                    if (section == null) {
                        iProgress.warn("Section "+s.getName()+" is not available for "+cr.getName());
                        return null;
                    }
                    sections.add(section);
                }
                return cr.createEnrollment(currentSolution().getAssignment(), sections);
            }
        }
        
        private void assign(Enrollment enrollment, boolean warn) {
        	if (!enrollment.getStudent().isAvailable(enrollment)) {
        		if (warn)
        			iProgress.warn("There is a problem assigning " + enrollment.getName() + " to " + enrollment.getStudent().getName() + " (" + enrollment.getStudent().getExternalId() + "): Student not available.");
        		else
        			iProgress.info("There is a problem assigning " + enrollment.getName() + " to " + enrollment.getStudent().getName() + " (" + enrollment.getStudent().getExternalId() + "): Student not available.");
        		return;
        	}
        	Map<Constraint<Request, Enrollment>, Set<Enrollment>> conflictConstraints = currentSolution().getModel().conflictConstraints(currentSolution().getAssignment(), enrollment);
            if (conflictConstraints.isEmpty()) {
            	currentSolution().getAssignment().assign(0, enrollment);
            } else {
            	if (warn)
            		iProgress.warn("There is a problem assigning " + enrollment.getName() + " to " + enrollment.getStudent().getName() + " (" + enrollment.getStudent().getExternalId() + ")");
            	else
            		iProgress.info("There is a problem assigning " + enrollment.getName() + " to " + enrollment.getStudent().getName() + " (" + enrollment.getStudent().getExternalId() + ")");
                for (Constraint<Request, Enrollment> c: conflictConstraints.keySet()) {
                	Set<Enrollment> vals = conflictConstraints.get(c);
                    for (Enrollment enrl: vals) {
                    	iProgress.info("    conflicts with " + enrl.getName() +
                    			(enrl.getRequest().getStudent().getId() != enrollment.getStudent().getId() ? " of a different student (" + enrl.getRequest().getStudent().getExternalId() + ")" : "") +
                    			" due to " + c.getClass().getSimpleName());
                    }
                }
            }
        }
        
        private void unassignAll() {
            for (Request request: currentSolution().getModel().variables()) {
            	currentSolution().getAssignment().unassign(0l, request);
            }
        }
        
        public void execute() {
            iProgress = Progress.getInstance(currentSolution().getModel());
            
            Map<Long, Map<Long, Request>> requests = new Hashtable<Long, Map<Long,Request>>();
            for (Request request: currentSolution().getModel().variables()) {
            	Map<Long, Request> r = requests.get(request.getStudent().getId());
            	if (r == null) {
            		r = new Hashtable<Long, Request>();
            		requests.put(request.getStudent().getId(), r);
            	}
            	r.put(request.getId(), request);
            }
            
            if (!iBestAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating best assignment ...", iBestAssignmentTable.size());
                unassignAll();
                for (Map.Entry<Long, Map<Long, Enrollment>> e1: iBestAssignmentTable.entrySet()) {
                	Map<Long, Request> r = requests.get(e1.getKey());
                    iProgress.incProgress();
                	if (r == null) continue;
                	for (Map.Entry<Long, Enrollment> e2: e1.getValue().entrySet()) {
                		Request request = r.get(e2.getKey());
                		if (request == null) continue;
                		Enrollment enrollment = getEnrollment(request, e2.getValue());
                        if (enrollment!=null) assign(enrollment, false);
                	}
                }
                currentSolution().saveBest();
            }
            if (!iInitialAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating initial assignment ...", iInitialAssignmentTable.size());
                for (Map.Entry<Long, Map<Long, Enrollment>> e1: iInitialAssignmentTable.entrySet()) {
                	Map<Long, Request> r = requests.get(e1.getKey());
                    iProgress.incProgress();
                	if (r == null) continue;
                	for (Map.Entry<Long, Enrollment> e2: e1.getValue().entrySet()) {
                		Request request = r.get(e2.getKey());
                		if (request == null) continue;
                		Enrollment enrollment = getEnrollment(request, e2.getValue());
                        if (enrollment!=null) request.setInitialAssignment(enrollment);
                	}
                }
            }
            if (!iCurrentAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating current assignment ...", iCurrentAssignmentTable.size());
                unassignAll();
                for (Map.Entry<Long, Map<Long, Enrollment>> e1: iCurrentAssignmentTable.entrySet()) {
                	Map<Long, Request> r = requests.get(e1.getKey());
                    iProgress.incProgress();
                	if (r == null) continue;
                	for (Map.Entry<Long, Enrollment> e2: e1.getValue().entrySet()) {
                		Request request = r.get(e2.getKey());
                		if (request == null) continue;
                		Enrollment enrollment = getEnrollment(request, e2.getValue());
                        if (enrollment!=null) assign(enrollment, true);
                	}
                }
            }
            iCurrentAssignmentTable.clear();
            iBestAssignmentTable.clear();
            iInitialAssignmentTable.clear();
            iProgress = null;
            
            if (iSolutionId!=null)
                getProperties().setProperty("General.SolutionId",iSolutionId);

            iLoadedDate = new Date();
            iWorking = false;
            afterLoad();
            Progress.getInstance(currentSolution().getModel()).setStatus("Awaiting commands ...");
        }
    }
    
    private AcademicSessionInfo iSession = null;
	@Override
	public AcademicSessionInfo getAcademicSession() {
		if (iSession == null) {
			try {
				org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession(); 
				try {
					iSession = new AcademicSessionInfo(SessionDAO.getInstance().get(getSessionId(), hibSession));
					iSession.setSectioningEnabled(false);
					iRules = new XSchedulingRules(iSession, hibSession);
				} finally {
					hibSession.close();
				}
			} catch (Exception e) {
				iSession = new AcademicSessionInfo(getSessionId(),
						getConfig().getProperty("Data.Year"),
						getConfig().getProperty("Data.Term"),
						getConfig().getProperty("Data.Initiative"));
			}
		}
		return iSession;
	}

	private DistanceMetric iDistanceMetric = null;
	@Override
	public DistanceMetric getDistanceMetric() {
		if (iDistanceMetric == null)
			iDistanceMetric = getModel().getDistanceMetric();
		if (iDistanceMetric == null) {
			iDistanceMetric = new DistanceMetric(getProperties());
			TravelTime.populateTravelTimes(iDistanceMetric);
		}
		return iDistanceMetric;
	}

	@Override
	public DataProperties getConfig() {
		return getProperties();
	}

	private Map<Long, XCourse> getCourseInfoTable() {
		if (iCourseInfoCache == null) {
			iCourseInfoCache = new Hashtable<Long, XCourse>();
			for (Offering offering: getModel().getOfferings())
				for (Course course: offering.getCourses())
					if (course != null)
						iCourseInfoCache.put(course.getId(), new XCourse(course));
		}
		return iCourseInfoCache;
	}
	private void clearCourseInfoTable() {
		iCourseInfoCache = null;
	}
	
	private Map<Long, XOffering> getOfferingCache() {
		if (iOfferingCache == null) {
			iOfferingCache = new Hashtable<Long, XOffering>();
			List<LinkedSections> links = getModel().getLinkedSections();
			for (Offering offering: getModel().getOfferings())
				iOfferingCache.put(offering.getId(), new XOffering(offering, links));
		}
		return iOfferingCache;
	}
	private void clearOfferingCache() {
		iOfferingCache = null;
	}
	
	private Map<String, Set<Long>> getInstructedOfferingsCache() {
		if (iInstructedOfferingsCache == null) {
			iInstructedOfferingsCache = new Hashtable<String, Set<Long>>();
			for (Offering offering: getModel().getOfferings()) {
				for (Config config: offering.getConfigs())
					for (Subpart subpart: config.getSubparts())
						for (Section section: subpart.getSections())
							if (section.hasInstructors())
								for (Instructor instructor: section.getInstructors())
									if (instructor.getExternalId() != null) {
										Set<Long> offerings = iInstructedOfferingsCache.get(instructor.getExternalId());
										if (offerings == null) {
											offerings = new HashSet<Long>();
											iInstructedOfferingsCache.put(instructor.getExternalId(), offerings);
										}
										offerings.add(offering.getId());
									}
			}
			for (Student student: getModel().getStudents()) {
				if (student.isDummy() || student.getExternalId() == null) continue;
				unavailbilities: for (Unavailability unavailability: student.getUnavailabilities())
					for (Offering offering: getModel().getOfferings()) {
						for (Config config: offering.getConfigs())
							for (Subpart subpart: config.getSubparts())
								for (Section section: subpart.getSections())
									if (section.getId() == unavailability.getId()) {
										Set<Long> offerings = iInstructedOfferingsCache.get(student.getExternalId());
										if (offerings == null) {
											offerings = new HashSet<Long>();
											iInstructedOfferingsCache.put(student.getExternalId(), offerings);
										}
										offerings.add(offering.getId());
										continue unavailbilities;
									}
					}
			}
		}
		return iInstructedOfferingsCache;
	}
	
	private void clearInstructedOfferingsCache() {
		iInstructedOfferingsCache = null;
	}
	
	private Map<Long, Student> getStudentCache() {
		if (iStudentCache == null) {
			iStudentCache = new Hashtable<Long, Student>();
			for (Student student: getModel().getStudents())
				if (!student.isDummy())
					iStudentCache.put(student.getId(), student);
		}
		return iStudentCache;
	}
	
	private void clearStudentCache() {
		iStudentCache = null;
		iStudentExtCache = null;
	}
	
	private Map<String, Student> getStudentExtCache() {
		if (iStudentExtCache == null) {
			iStudentExtCache = new HashMap<>();
			for (Student student: getModel().getStudents())
				if (!student.isDummy() && student.getExternalId() != null && !student.getExternalId().isEmpty())
					iStudentExtCache.put(student.getExternalId(), student);
		}
		return iStudentExtCache;
	}
	
	@Override
	public Collection<XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher) {
		return findCourses(query, limit, matcher, null);
	}
	
	@Override
	public Collection<XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher, Comparator<XCourseId> cmp) {
		if (matcher != null) matcher.setServer(this);
		List<XCourseId> ret = new ArrayList<XCourseId>(limit == null || limit < 0 ? 100 : limit);
		String queryInLowerCase = query.toLowerCase();
		for (XCourse c : getCourseInfoTable().values()) {
			if (c.matchCourseName(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
			if (limit != null && limit > 0 && ret.size() >= limit) return ret;
		}
		if (queryInLowerCase.length() > 2) {
			for (XCourse c : getCourseInfoTable().values()) {
				if (c.matchTitle(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
				if (limit != null && limit > 0 && ret.size() >= limit) return ret;
			}
		}
		if (cmp != null)
			Collections.sort(ret, cmp);
		return ret;
	}

	@Override
	public Collection<XCourseId> findCourses(CourseMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		List<XCourseId> ret = new ArrayList<XCourseId>();
		for (XCourse c : getCourseInfoTable().values())
			if (matcher == null || matcher.match(c)) ret.add(c);
		return ret;
	}

	@Override
	public Collection<XStudentId> findStudents(StudentMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		List<XStudentId> ret = new ArrayList<XStudentId>();
		for (Student student: getModel().getStudents()) {
			if (student.isDummy()) continue;
			XStudentId s = new XStudentId(student);
			if (!student.isDummy() && (matcher == null || matcher.match(s)))
				ret.add(s);
		}
		return ret;
	}

	@Override
	public XCourse getCourse(Long courseId) {
		return getCourseInfoTable().get(courseId);
	}
	
	@Override
	public XCourse getCourse(String courseName) {
		for (Offering offering: getModel().getOfferings())
			for (Course course: offering.getCourses())
				if (course.getName().equalsIgnoreCase(courseName)) return getCourse(course.getId());
		return null;
	}

	@Override
	public XStudent getStudent(Long studentId) {
		Student student = getStudentCache().get(studentId);
		return (student == null ? null : new XStudent(student, currentSolution().getAssignment()));
	}

	@Override
	public XOffering getOffering(Long offeringId) {
		return getOfferingCache().get(offeringId);
	}

	@Override
	public <X extends OnlineSectioningAction> X createAction(Class<X> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (SecurityException e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}

	@Override
	public <E> E execute(OnlineSectioningAction<E> action, Entity user) throws SectioningException {
		long c0 = OnlineSectioningHelper.getCpuTime();
		OnlineSectioningHelper h = new OnlineSectioningHelper(user);
		try {
			h.addMessageHandler(new OnlineSectioningHelper.DefaultMessageLogger(LogFactory.getLog(action.getClass().getName() + "." + action.name() + "[" + getAcademicSession().toCompactString() + "]")));
			h.addAction(action, getAcademicSession());
			E ret = action.execute(this, h);
			if (h.getAction() != null && !h.getAction().hasResult()) {
				if (ret == null)
					h.getAction().setResult(OnlineSectioningLog.Action.ResultType.NULL);
				else if (ret instanceof Boolean)
					h.getAction().setResult((Boolean)ret ? OnlineSectioningLog.Action.ResultType.TRUE : OnlineSectioningLog.Action.ResultType.FALSE);
				else
					h.getAction().setResult(OnlineSectioningLog.Action.ResultType.SUCCESS);
			}
			return ret;
		} catch (Exception e) {
			if (e instanceof SectioningException) {
				if (e.getCause() == null) {
					sLog.info("Execution failed: " + e.getMessage());
					h.info("Execution failed: " + e.getMessage());
				} else {
					sLog.warn("Execution failed: " + e.getMessage(), e);
					h.warn("Execution failed: " + e.getMessage(), e.getCause());
				}
			} else {
				sLog.error("Execution failed: " + e.getMessage(), e);
				h.error("Execution failed: " + e.getMessage(), e);
			}
			if (h.getAction() != null) {
				h.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
				if (e.getCause() != null && e instanceof SectioningException)
					h.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
				else
					h.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getMessage() == null ? "null" : e.getMessage()));
			}
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(SCT_MSG.exceptionUnknown(e.getMessage()), e);
		} finally {
			if (h.getAction() != null)
				h.getAction().setEndTime(System.currentTimeMillis()).setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
			sLog.debug("Executed: " + h.getLog() + " (" + h.getLog().toByteArray().length + " bytes)");
		}
	}

	@Override
	public <E> void execute(OnlineSectioningAction<E> action, Entity user, ServerCallback<E> callback) throws SectioningException {
		try {
			callback.onSuccess(execute(action, user));
		} catch (Throwable t) {
			callback.onFailure(t);
		}
	}

	@Override
	public void clearAll() {
	}

	@Override
	public void clearAllStudents() {
	}

	@Override
	public Lock readLock() {
		return new NoLock();
	}

	@Override
	public Lock writeLock() {
		return new NoLock();
	}

	@Override
	public Lock lockAll() {
		return new NoLock();
	}

	@Override
	public Lock lockStudent(Long studentId, Collection<Long> offeringIds, String actionName) {
		return new NoLock();
	}

	@Override
	public Lock lockOffering(Long offeringId, Collection<Long> studentIds, String actionName) {
		return new NoLock();
	}

	@Override
	public Lock lockRequest(CourseRequestInterface request, String actionName) {
		return new NoLock();
	}

	@Override
	public boolean isOfferingLocked(Long offeringId) {
		return false;
	}

	@Override
	public void lockOffering(Long offeringId) {
	}

	@Override
	public void unlockOffering(Long offeringId) {
	}

	@Override
	public Collection<Long> getLockedOfferings() {
		return null;
	}

	@Override
	public void releaseAllOfferingLocks() {	
	}

	@Override
	public void persistExpectedSpaces(Long offeringId) {
	}

	@Override
	public List<Long> getOfferingsToPersistExpectedSpaces(long minimalAge) {
		return null;
	}

	@Override
	public void unload() {
	}
	
	public static class NoLock implements Lock {
		@Override
		public void release() {
		}
	}

	@Override
	public boolean needPersistExpectedSpaces(Long offeringId) {
		return false;
	}
	
	@Override
	public void reload() {
	}

	@Override
	public Collection<XCourseRequest> getRequests(Long offeringId) {
		List<XCourseRequest> ret = new ArrayList<XCourseRequest>();
		Set<Long> reqIds = new HashSet<Long>();
		for (Offering offering: getModel().getOfferings())
			if (offering.getId() == offeringId) {
				for (Course course: offering.getCourses())
					for (CourseRequest req: course.getRequests()) {
						if (!req.getStudent().isDummy() && reqIds.add(req.getId()))
							ret.add(new XCourseRequest(req, currentSolution().getAssignment().getValue(req)));
					}
				break;
			}
		return ret;
	}

	@Override
	public XEnrollments getEnrollments(Long offeringId) {
		return new XEnrollments(offeringId, getRequests(offeringId));
	}

	@Override
	public XExpectations getExpectations(Long offeringId) {
		for (Offering offering: getModel().getOfferings())
			if (offering.getId() == offeringId)
				return new XExpectations(offering);
		return null;
	}

	@Override
	public void update(XExpectations expectations) {
	}

	@Override
	public void remove(XStudent student) {
	}

	@Override
	public void update(XStudent student, boolean updateRequests) {
	}

	@Override
	public void remove(XOffering offering) {
	}

	@Override
	public void update(XOffering offering) {
	}

	@Override
	public XCourseRequest assign(XCourseRequest request, XEnrollment enrollment) {
		return request;
	}

	@Override
	public XCourseRequest waitlist(XCourseRequest request, boolean waitlist) {
		return request;
	}

	@Override
	public boolean checkDeadline(Long courseId, XTime sectionTime, Deadline type) {
		return true;
	}

	@Override
	public String getCourseDetails(Long courseId, CourseDetailsProvider provider) {
		XCourse course = getCourse(courseId);
		return course == null ? null : course.getDetails(getAcademicSession(), provider);
	}

	@Override
	public boolean isReady() {
		return true;
	}
	
	@Override
	public long getMemUsage() {
		return new MemoryCounter().estimate(this);
	}

	@Override
	public <E> E getProperty(String name, E defaultValue) {
		E ret = (E)iOnlineProperties.get(name);
		return ret == null ? defaultValue : ret;
	}

	@Override
	public <E> void setProperty(String name, E value) {
		if (value == null)
			iOnlineProperties.remove(name);
		else
			iOnlineProperties.put(name, value);
	}
	
	@Override
	public CSVFile getReport(DataProperties parameters) {
		try {
			String name = parameters.getProperty("report", null);
			if (name == null) {
				String reference = parameters.getProperty("name");
				if (reference == null)
					return null;
				else if (iReports.containsKey(reference))
					return iReports.get(reference);
				else 
					name = ReportType.valueOf(reference).getImplementation();
			}
			if (name == null || name.isEmpty()) return null;
			if (StudentSolver.class.getName().equals(name)) {
				String reference = parameters.getProperty("reference");
				return (reference == null ? null : iReports.get(reference));
			}
			Class<StudentSectioningReport> clazz = (Class<StudentSectioningReport>) Class.forName(name);
			StudentSectioningReport report = clazz.getConstructor(StudentSectioningModel.class).newInstance(currentSolution().getModel());
			return report.create(currentSolution().getAssignment(), parameters);
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}

	@Override
	public OverExpectedCriterion getOverExpectedCriterion() {
		return new NeverOverExpected(getConfig());
	}
	
	@Override
	public SolverType getType() {
		return SolverType.STUDENT;
	}

	@Override
	public XCourseId getCourse(Long courseId, String courseName) {
		if (courseId != null) return getCourse(courseId);
		if (courseName != null) return getCourse(courseName);
		return null;
	}

	@Override
	public Collection<Long> getInstructedOfferings(String instructorExternalId) {
		return getInstructedOfferingsCache().get(instructorExternalId);
	}
	
	@Override
	public Set<Long> getRequestedCourseIds(Long studentId) {
		for (Student student: getModel().getStudents())
			if (!student.isDummy() && student.getId() == studentId) {
				Set<Long> courseIds = new HashSet<Long>();
				for (Request request: student.getRequests()) {
					if (request instanceof CourseRequest)
						for (Course course: ((CourseRequest)request).getCourses())
							courseIds.add(course.getId());
				}
				return courseIds;
			}
		return null;
	}

	@Override
	public boolean isRunning() {
		if (super.isRunning()) return true;
		if (iWorking && iWorkThread != null && iWorkThread instanceof InterruptibleThread && iWorkThread.isAlive() && !iWorkThread.isInterrupted())
			return true;
		return false;
	}
	
	@Override
	public void stopSolver() {
		if (super.isRunning()) super.stopSolver();
		if (iWorking && iWorkThread != null && iWorkThread instanceof InterruptibleThread && iWorkThread.isAlive() && !iWorkThread.isInterrupted()) {
			try {
				iWorkThread.interrupt();
				iWorkThread.join();
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public Collection<ReportTypeInterface> getReportTypes() {
		List<ReportTypeInterface> ret = new ArrayList<ReportTypeInterface>();
		for (InMemoryReport report: new TreeSet<InMemoryReport>(iReports.values()))
			ret.add(new ReportTypeInterface(report.getReference(), report.getName(), StudentSolver.class.getName(), false, "reference", report.getReference()));
		return ret;
	}
	
	public void setReport(InMemoryReport report) {
		iReports.put(report.getReference(), report);
	}
	
	public InMemoryReport getReport(String reference) {
		return iReports.get(reference);
	}

	@Override
	public byte[] backupXml() {
		java.util.concurrent.locks.Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            ByteArrayOutputStream ret = new ByteArrayOutputStream();
            GZIPOutputStream gz = new GZIPOutputStream(ret);
            
            Document document = createCurrentSolutionBackup(false, false);
            saveProperties(document);
            
            new XMLWriter(gz, OutputFormat.createCompactFormat()).write(document);
            
            gz.flush(); gz.close();

            return ret.toByteArray();
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            return null;
        } finally {
        	lock.unlock();
        }
	}

	@Override
	public boolean restoreXml(byte[] data) {
		StudentSectioningModel model = null;
        try {
            if (isRunning()) stopSolver();
            disposeNoInherit(false);

            model = createModel(getProperties());
            Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
            setInitalSolution(model);
            initSolver();

            Document document = (new SAXReader()).read(new GZIPInputStream(new ByteArrayInputStream(data)));
            // readProperties(document);

            restureCurrentSolutionFromBackup(document);
            if (isPublished()) {
            	Progress.getInstance(model).setStatus(SCT_MSG.statusPublished());
                model.clearBest();
            } else {
            	Progress.getInstance(model).setStatus(MSG.statusReady());
            }
            
            clearCachedData();
            
            return true;
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            if (model!=null) Progress.removeInstance(model);
            return false;
        }
	}

	@Override
	public boolean isPublished() {
		return getProperties().getProperty("StudentSct.Published") != null;
	}
	
	@Override
    public Map<String,String> currentSolutionInfo() {
		String published = getProperties().getProperty("StudentSct.Published");
		if (published != null) {
			Map<String,String> info = currentSolution().getModel().getExtendedInfo(currentSolution().getAssignment());
			info.put(" "+SCT_MSG.infoPublished(), Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP).format(new Date(Long.valueOf(published))));
			return info;
		} else {
			return super.currentSolutionInfo();
		}
	}
	
	@Override
	public boolean canPassivate() {
		return super.canPassivate() && !isPublished();
	}
	
	@Override
    public boolean restore(File folder, String puid, boolean removeFiles) {
		if (super.restore(folder, puid, removeFiles)) {
			if (isPublished())
				Progress.getInstance(currentSolution().getModel()).setStatus(SCT_MSG.statusPublished());
			return true;
		}
		return false;
	}
	
	protected Gson getGson() {
		GsonBuilder builder = new GsonBuilder()
		.registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
			@Override
			public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
			}
		})
		.registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
			@Override
			public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return new DateTime(json.getAsJsonPrimitive().getAsString(), DateTimeZone.UTC);
			}
		})
		.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(src));
			}
		})
		.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				try {
					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getAsJsonPrimitive().getAsString());
				} catch (ParseException e) {
					throw new JsonParseException(e.getMessage(), e);
				}
			}
		});
		builder.setPrettyPrinting();
		return builder.create();
	}
	
	@Override
    protected void finishBeforeSave() {
		if (getProperties().getPropertyBoolean("General.Validate",false) && isCanValidate()) {
			ProblemSaver<Request, Enrollment, StudentSectioningModel> saver = getCustomValidator(this); 
			java.util.concurrent.locks.Lock lock = currentSolution().getLock().readLock();
			saver.setTerminationCondition(new TerminationCondition<Request, Enrollment>() {
				@Override
				public boolean canContinue(Solution<Request, Enrollment> currentSolution) {
					return !isStop();
				}
			});
            lock.lock();
            try {
                saver.save();
            } catch (Exception e) {
            	sLog.error("Failed to validate the problem: " + e.getMessage(), e);
            } finally {
            	lock.unlock();
            }
		}
		if (getProperties().getPropertyBoolean("General.Publish",false)) {
			byte[] data = backupXml();
        	SectioningSolutionLog log = new SectioningSolutionLog();
        	log.setData(data);
        	log.setInfo(getGson().toJson(currentSolutionInfo()));
        	log.setTimeStamp(new Date());
        	log.setSession(SessionDAO.getInstance().get(getSessionId()));
        	String mgrId = getProperties().getProperty("General.OwnerPuid");
        	log.setOwner(TimetableManager.findByExternalId(mgrId));
        	Long configId = getProperties().getPropertyLong("General.SettingsId", null);
        	if (configId != null) {
        		SolverPredefinedSetting config = SolverPredefinedSettingDAO.getInstance().get(configId);
        		if (config != null)
        			log.setConfig(config.getDescription());
        	}
        	SectioningSolutionLogDAO.getInstance().getSession().persist(log);
        	SectioningSolutionLogDAO.getInstance().getSession().flush();
        	Long publishId = log.getUniqueId();
        	if (SolverServerImplementation.getInstance() != null) {
    			SolverServerImplementation.getInstance().unloadSolver(getType(), "PUBLISHED_" + getSessionId());
    			DataProperties config = new DataProperties(getProperties().toMap());
    			config.setProperty("StudentSct.Published", String.valueOf((new Date()).getTime()));
    			config.setProperty("StudentSct.PublishId", publishId.toString());
    			config.setProperty("General.OwnerPuid", "PUBLISHED_" + config.getProperty("General.SessionId"));
    			StudentSolverProxy solver = SolverServerImplementation.getInstance().getStudentSolverContainer().createSolver("PUBLISHED_" + config.getProperty("General.SessionId"), config);
    			if (!solver.restoreXml(data))
    				solver.dispose();
        	}
		}
    }
	
    protected void saveReports(Document document) {
    	Element reports = document.getRootElement().addElement("reports");
    	for (InMemoryReport r: iReports.values()) {
    		try {
    			StringWriter sw = new StringWriter();
    			r.save(sw);
    			reports.addElement("report").addAttribute("reference", r.getReference()).addAttribute("name", r.getName()).add(new DOMCDATA(sw.toString()));
    		} catch (Exception ex) {}
    	}
    }
    
    protected void readReports(Document document) {
    	iReports.clear();
    	Element reports = document.getRootElement().element("reports");
    	if (reports != null)
    		for (Iterator i = reports.elementIterator("report"); i.hasNext(); ) {
    			Element e = (Element)i.next();
    			InMemoryReport r = new InMemoryReport(e.attributeValue("reference"), e.attributeValue("name"));
    			try {
    				r.load(new StringReader(e.getText()));
        			iReports.put(r.getReference(), r);
    			} catch (Exception ex) {}
    		}
    }
    
    @Override
    public void clear() {
    	java.util.concurrent.locks.Lock lock = currentSolution().getLock().writeLock();
        lock.lock();
        try {
            for (Request request: currentSolution().getModel().variables()) {
            	if (request instanceof CourseRequest && ((CourseRequest)request).isFixed()) continue;
            	currentSolution().getAssignment().unassign(0, request);
            }
            currentSolution().clearBest();
        } finally {
        	lock.unlock();
        }
    }

	@Override
	public CourseDeadlines getCourseDeadlines(Long courseId) {
		return new CourseDeadlines() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override
			public boolean checkDeadline(XTime sectionTime, Deadline type) {
				return true;
			}
		};
	}
	
	private ReservationInterface convert(Reservation reservation, Assignment<Request, Enrollment> assignment) {
		ReservationInterface r = null;
		Course co = null;
		for (Course c: reservation.getOffering().getCourses()) {
			if (co == null || reservation.getOffering().getName().equals(c.getName()))
				co = c;
		}
		if (reservation instanceof LearningCommunityReservation) {
			org.unitime.timetable.model.LearningCommunityReservation lcr = LearningCommunityReservationDAO.getInstance().get(reservation.getId());
			if (lcr != null) {
				r = new ReservationInterface.LCReservation();
				IdName g = new IdName();
				g.setAbbv(lcr.getGroup().getGroupAbbreviation());
				g.setName(lcr.getGroup().getGroupName());
				g.setLimit(lcr.getGroup().getStudents().size());
				((ReservationInterface.LCReservation)r).setGroup(g);
				
				co = ((LearningCommunityReservation) reservation).getCourse();
				ReservationInterface.Course course = new ReservationInterface.Course();
				course.setId(co.getId());
				course.setAbbv(co.getName());
				course.setControl(reservation.getOffering().getName().equals(co.getName()));
				course.setLimit(co.getLimit() < 0 ? null : co.getLimit());
				((ReservationInterface.LCReservation) r).setCourse(course);
			}
		} else if (reservation instanceof GroupReservation) {
			StudentGroupReservation sgr = StudentGroupReservationDAO.getInstance().get(reservation.getId());
			if (sgr != null) {
				r = new ReservationInterface.GroupReservation();
				IdName g = new IdName();
				g.setAbbv(sgr.getGroup().getGroupAbbreviation());
				g.setName(sgr.getGroup().getGroupName());
				g.setLimit(sgr.getGroup().getStudents().size());
				((ReservationInterface.GroupReservation)r).setGroup(g);
				r.setOverride(sgr instanceof GroupOverrideReservation);
			}
		}
		
		if (r != null) {
			// do nothing
		} else if (reservation instanceof CourseReservation) {
			co = ((CourseReservation) reservation).getCourse();
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(co.getId());
			course.setAbbv(co.getName());
			course.setControl(reservation.getOffering().getName().equals(co.getName()));
			course.setLimit(co.getLimit() < 0 ? null : co.getLimit());
			r = new ReservationInterface.CourseReservation();
			r.setOverride(reservation.mustBeUsed() != CourseReservation.DEFAULT_MUST_BE_USED ||
					reservation.canAssignOverLimit() != CourseReservation.DEFAULT_CAN_ASSIGN_OVER_LIMIT ||
					reservation.isAllowDisabled() != CourseReservation.DEFAULT_ALLOW_OVERLAP);
			((ReservationInterface.CourseReservation) r).setCourse(course);
			r.setProjection(co.getProjected());
			r.setEnrollment(co.getContext(assignment).getEnrollments().size());
		} else if (reservation instanceof CurriculumReservation) {
			CurriculumReservation cr = (CurriculumReservation) reservation;
			r = new ReservationInterface.CurriculumReservation();
			r.setOverride(reservation.mustBeUsed() != CurriculumReservation.DEFAULT_MUST_BE_USED ||
					reservation.canAssignOverLimit() != CurriculumReservation.DEFAULT_CAN_ASSIGN_OVER_LIMIT ||
					reservation.isAllowDisabled() != CurriculumReservation.DEFAULT_ALLOW_OVERLAP);
			ReservationInterface.Areas curriculum = new ReservationInterface.Areas();
			long areaId = 0;
			for (String area: cr.getAcademicAreas()) {
				ReservationInterface.IdName aa = new ReservationInterface.IdName();
				aa.setId(areaId++);
				aa.setAbbv(area);
				aa.setName(area);
				curriculum.getAreas().add(aa);
			}
			long clasfId = 0;
			for (String classification: cr.getClassifications()) {
				ReservationInterface.IdName clasf = new ReservationInterface.IdName();
				clasf.setId(clasfId++);
				clasf.setAbbv(classification);
				clasf.setName(classification);
				curriculum.getClassifications().add(clasf);
			}
			long majorId = 0, concId = 0;
			for (String major: cr.getMajors()) {
				ReservationInterface.IdName mj = new ReservationInterface.IdName();
				mj.setId(majorId);
				mj.setAbbv(major);
				mj.setName(major);
				curriculum.getMajors().add(mj);
				if (cr.getConcentrations(major) != null)
					for (String conc: cr.getConcentrations(major)) {
						ReservationInterface.IdName cc = new ReservationInterface.IdName();
						cc.setId(concId++);
						cc.setAbbv(conc);
						cc.setParentId(majorId);
						cc.setName(conc);
						curriculum.getConcentrations().add(cc);
					}
				majorId ++;
			}
			for (String minor: cr.getMinors()) {
				ReservationInterface.IdName mn = new ReservationInterface.IdName();
				mn.setAbbv(minor);
				mn.setName(minor);
				curriculum.getMinors().add(mn);
			}
			if (curriculum.getAreas().size() > 1)
				Collections.sort(curriculum.getAreas(), new Comparator<ReservationInterface.IdName>() {
					@Override
					public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
						int cmp = s1.getAbbv().compareTo(s2.getAbbv());
						if (cmp != 0) return cmp;
						cmp = s1.getName().compareTo(s2.getName());
						if (cmp != 0) return cmp;
						return s1.getId().compareTo(s2.getId());
					}
				});
			Collections.sort(curriculum.getMajors(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getAbbv().compareTo(s2.getAbbv());
					if (cmp != 0) return cmp;
					cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getId().compareTo(s2.getId());
				}
			});
			Collections.sort(curriculum.getClassifications(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getAbbv().compareTo(s2.getAbbv());
					if (cmp != 0) return cmp;
					cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getId().compareTo(s2.getId());
				}
			});
			Collections.sort(curriculum.getMinors(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getAbbv().compareTo(s2.getAbbv());
					if (cmp != 0) return cmp;
					cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getId().compareTo(s2.getId());
				}
			});
			Collections.sort(curriculum.getConcentrations(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getAbbv().compareTo(s2.getAbbv());
					if (cmp != 0) return cmp;
					cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getId().compareTo(s2.getId());
				}
			});
			((ReservationInterface.CurriculumReservation) r).setCurriculum(curriculum);
		} else if (reservation instanceof IndividualReservation) {
			r = new ReservationInterface.IndividualReservation();
			r.setOverride(reservation instanceof ReservationOverride || !reservation.mustBeUsed());
			if (reservation instanceof ReservationOverride) {
				r = new ReservationInterface.OverrideReservation(
						reservation.canAssignOverLimit() && reservation.isAllowOverlap() && reservation.canBreakLinkedSections() ? OverrideType.AllowOverLimitTimeConflictLink :
						reservation.canAssignOverLimit() && reservation.canBreakLinkedSections() ? OverrideType.AllowOverLimitLink :
						reservation.isAllowOverlap() && reservation.canBreakLinkedSections() ? OverrideType.AllowTimeConflictLink :
						reservation.canAssignOverLimit() && reservation.isAllowOverlap() ? OverrideType.AllowOverLimitTimeConflict :
						reservation.canAssignOverLimit() ? OverrideType.AllowOverLimit :
						reservation.isAllowOverlap() ? OverrideType.AllowTimeConflict :
						reservation.canBreakLinkedSections() ? OverrideType.CoReqOverride : OverrideType.Other);
			} else {
				r.setOverride(reservation.mustBeUsed() != IndividualReservation.DEFAULT_MUST_BE_USED ||
						reservation.canAssignOverLimit() != IndividualReservation.DEFAULT_CAN_ASSIGN_OVER_LIMIT ||
						reservation.isAllowDisabled() != IndividualReservation.DEFAULT_ALLOW_OVERLAP);
			}
			
			for (Student student: getModel().getStudents()) {
				if (((IndividualReservation)reservation).getStudentIds().contains(student.getId())) {
					ReservationInterface.IdName s = new ReservationInterface.IdName();
					s.setId(student.getId());
					s.setAbbv(student.getExternalId());
					s.setName(student.getName());
					((ReservationInterface.IndividualReservation) r).getStudents().add(s);
				}
			}
			Collections.sort(((ReservationInterface.IndividualReservation) r).getStudents(), new Comparator<ReservationInterface.IdName>() {
				@Override
				public int compare(ReservationInterface.IdName s1, ReservationInterface.IdName s2) {
					int cmp = s1.getName().compareTo(s2.getName());
					if (cmp != 0) return cmp;
					return s1.getAbbv().compareTo(s2.getAbbv());
				}
			});
		} else if (reservation instanceof UniversalOverride) {
			r = new ReservationInterface.UniversalReservation();
			((ReservationInterface.UniversalReservation)r).setFilter(((UniversalOverride)reservation).getFilter());
		} else {
			return null;
		}
		r.setEnrollment(reservation.getContext(assignment).getEnrollments().size());
		
		ReservationInterface.Offering offering = new ReservationInterface.Offering();
		offering.setAbbv(co.getName());
		offering.setId(reservation.getOffering().getId());
		offering.setOffered(true);
		r.setOffering(offering);
		for (Course cx: reservation.getOffering().getCourses()) {
			ReservationInterface.Course course = new ReservationInterface.Course();
			course.setId(cx.getId());
			course.setAbbv(cx.getName());
			course.setControl(reservation.getOffering().getName().equals(cx.getName()));
			course.setLimit(cx.getLimit() < 0 ? null : cx.getLimit());
			offering.getCourses().add(course);
		}
		for (Config ioc: reservation.getOffering().getConfigs()) {
			if (reservation.getConfigs().contains(ioc)) {
				boolean hasSection = false;
				for (Subpart subpart: ioc.getSubparts()) {
					Set<Section> sections = reservation.getSections(subpart);
					if (sections != null)
						for (Section c: sections) {
							ReservationInterface.Clazz clazz = new ReservationInterface.Clazz();
							clazz.setId(c.getId());
							clazz.setAbbv(c.getName(c.getId()));
							clazz.setName(subpart.getName() + " " + c.getName(c.getId()));
							clazz.setLimit(c.getLimit() < 0 ? null : c.getLimit());
							r.getClasses().add(clazz);
							hasSection = true;
						}
				}
				if (!hasSection) {
					ReservationInterface.Config config = new ReservationInterface.Config();
					config.setId(ioc.getId());
					config.setName(ioc.getName());
					config.setAbbv(ioc.getName());
					config.setLimit(ioc.getLimit() < 0 ? null : ioc.getLimit());
					r.getConfigs().add(config);
				}
			} else {
				for (Subpart subpart: ioc.getSubparts()) {
					Set<Section> sections = reservation.getSections(subpart);
					if (sections != null)
						for (Section c: sections) {
							ReservationInterface.Clazz clazz = new ReservationInterface.Clazz();
							clazz.setId(c.getId());
							clazz.setAbbv(c.getName(c.getId()));
							clazz.setName(subpart.getName() + " " + c.getName(c.getId()));
							clazz.setLimit(c.getLimit() < 0 ? null : c.getLimit());
							r.getClasses().add(clazz);
						}
				}
			}
		}
		r.setExpired(reservation.isExpired());
		r.setLimit(reservation.getReservationLimit() < 0 ? null : (int)Math.round(reservation.getReservationLimit()));
		r.setInclusive(reservation.areRestrictionsInclusive());
		r.setId(reservation.getId());
		r.setAllowOverlaps(reservation.isAllowOverlap());
		r.setMustBeUsed(reservation.mustBeUsed());
		r.setAlwaysExpired(reservation instanceof ReservationOverride || reservation instanceof CurriculumOverride);
		r.setOverLimit(reservation.canAssignOverLimit());
		return r;
	}
	
	@Override
	public List<ReservationInterface> getReservations(Long offeringId) {
		Assignment<Request, Enrollment> assignment = currentSolution().getAssignment();
		for (Offering offering: getModel().getOfferings())
			if (offeringId.equals(offering.getId())) {
				List<ReservationInterface> ret = new ArrayList<ReservationInterface>();
				for (Reservation r: offering.getReservations()) {
					ReservationInterface res = convert(r, assignment);
					if (res != null)
						ret.add(res);
				}
				Collections.sort(ret);
				return ret;
			}
		return null;
	}

	
	private XSchedulingRules iRules = null;
	@Override
	public void setSchedulingRules(XSchedulingRules rules) {
		iRules = rules;
	}

	@Override
	public XSchedulingRule getSchedulingRule(XStudent student, Mode mode, boolean isAdvisor, boolean isAdmin) {
		if (iRules != null)
			return iRules.getRule(student, mode, this, isAdvisor, isAdmin);
		StudentSchedulingRule rule = StudentSchedulingRule.getRule(
				new org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher(student, getAcademicSession().getDefaultSectioningStatus(), this, false),
				getAcademicSession(),
				isAdvisor,
				isAdmin,
				mode,
				StudentSchedulingRuleDAO.getInstance().getSession());
		return (rule == null ? null : new XSchedulingRule(rule));
	}
	
	@Override
	public XSchedulingRule getSchedulingRule(Long studentId, Mode mode, boolean isAdvisor, boolean isAdmin) {
		XStudent student = getStudent(studentId);
		if (student == null) return null;
		return getSchedulingRule(student, mode, isAdvisor, isAdmin);
	}

	@Override
	public Collection<XClassEnrollment> getStudentSchedule(String studentExternalId) {
		XStudent student = getStudentForExternalId(studentExternalId);
		if (student == null) return null;
		List<XClassEnrollment> ret = new ArrayList<>();
		for (XRequest request: student.getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				XEnrollment e = cr.getEnrollment();
				if (e != null) {
					XOffering offering = getOffering(e.getOfferingId());
					XEnrollments enrl = getEnrollments(e.getOfferingId());
					for (XSection section: offering.getSections(e)) {
						XClassEnrollment ce = new XClassEnrollment(e, section);
						if (section.getParentId() != null)
							ce.setParentSectionName(offering.getSection(section.getParentId()).getName(e.getCourseId()));
						if (enrl != null) ce.setEnrollment(enrl.countEnrollmentsForSection(section.getSectionId()));
						XSubpart subpart = offering.getSubpart(section.getSubpartId());
						ce.setCredit(subpart.getCredit(e.getCourseId()));
						Float creditOverride = section.getCreditOverride(e.getCourseId());
						if (creditOverride != null) ce.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
						ret.add(ce);
					}
				}
			}
		}
		return ret;
	}

	@Override
	public XStudent getStudentForExternalId(String externalUniqueId) {
		if (externalUniqueId == null || externalUniqueId.isEmpty()) return null;
		Student student = getStudentExtCache().get(externalUniqueId);
		return (student == null ? null : new XStudent(student, currentSolution().getAssignment()));
	}
}
