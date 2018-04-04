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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.model.Model;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.Callback;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.ifs.util.ProblemLoader;
import org.cpsolver.ifs.util.ProblemSaver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.StudentSectioningXMLLoader;
import org.cpsolver.studentsct.StudentSectioningXMLSaver;
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
import org.cpsolver.studentsct.report.SectionConflictTable;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.dom4j.Document;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.sectioning.SectioningReports.ReportTypeInterface;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.match.StudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.solver.AbstractSolver;
import org.unitime.timetable.solver.SolverDisposeListener;
import org.unitime.timetable.util.MemoryCounter;


/**
 * @author Tomas Muller
 */
public class StudentSolver extends AbstractSolver<Request, Enrollment, StudentSectioningModel> implements StudentSolverProxy {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
    private transient Map<Long, XCourse> iCourseInfoCache = null;
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
			if (loaderClass != null && !loaderClass.isEmpty())
				return (ProblemLoader<Request, Enrollment, StudentSectioningModel>) Class.forName(loaderClass).getConstructor(StudentSectioningModel.class, Assignment.class).newInstance(model, assignment);
		} catch (Exception e) {
			iProgress.error("Failed to create a custom database loader: " + e.getMessage(), e);
		}
		return new StudentSectioningDatabaseLoader(model, assignment);
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
        
        return document;
	}

	@Override
	protected void restureCurrentSolutionFromBackup(Document document) {
        getProperties().setProperty("Xml.LoadBest", "true");
        getProperties().setProperty("Xml.LoadInitial", "true");
        getProperties().setProperty("Xml.LoadCurrent", "true");

        new StudentSectioningXMLLoader((StudentSectioningModel)currentSolution().getModel(), currentSolution().getAssignment()).load(document);
	}

	@Override
    protected void disposeNoInherit(boolean unregister) {
		super.disposeNoInherit(unregister);
        clearCourseInfoTable();
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
                        iProgress.warn("WARNING: Section "+s.getName()+" is not available for "+cr.getName());
                        return null;
                    }
                    sections.add(section);
                }
                return cr.createEnrollment(currentSolution().getAssignment(), sections);
            }
        }
        
        private void assign(Enrollment enrollment) {
        	if (!enrollment.getStudent().isAvailable(enrollment)) {
        		iProgress.warn("Unable to assign "+enrollment.variable().getName()+" := "+enrollment.getName() + " (student not available)");
        		return;
        	}
        	Map<Constraint<Request, Enrollment>, Set<Enrollment>> conflictConstraints = currentSolution().getModel().conflictConstraints(currentSolution().getAssignment(), enrollment);
            if (conflictConstraints.isEmpty()) {
            	currentSolution().getAssignment().assign(0, enrollment);
            } else {
                iProgress.warn("Unable to assign "+enrollment.variable().getName()+" := "+enrollment.getName());
                iProgress.warn("&nbsp;&nbsp;Reason:");
                for (Constraint<Request, Enrollment> c: conflictConstraints.keySet()) {
                	Set<Enrollment> vals = conflictConstraints.get(c);
                    for (Enrollment enrl: vals) {
                        iProgress.warn("&nbsp;&nbsp;&nbsp;&nbsp;"+enrl.getRequest().getName()+" = "+enrl.getName());
                    }
                    iProgress.debug("&nbsp;&nbsp;&nbsp;&nbsp;in constraint "+c);
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
                        if (enrollment!=null) assign(enrollment);
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
                        if (enrollment!=null) assign(enrollment);
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
			org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession(); 
			try {
				iSession = new AcademicSessionInfo(SessionDAO.getInstance().get(getSessionId(), hibSession));
				iSession.setSectioningEnabled(false);
			} finally {
				hibSession.close();
			}
		}
		return iSession;
	}

	private DistanceMetric iDistanceMetric = null;
	@Override
	public DistanceMetric getDistanceMetric() {
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
			org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().createNewSession();
			try {
				iCourseInfoCache = new Hashtable<Long, XCourse>();
				for (CourseOffering course: (List<CourseOffering>)hibSession.createQuery(
						"from CourseOffering x where x.subjectArea.session.uniqueId = :sessionId and x.instructionalOffering.notOffered = false"
						).setLong("sessionId", getSessionId()).setCacheable(true).list()) {
					iCourseInfoCache.put(course.getUniqueId(), new XCourse(course));
				}
			} finally {
				hibSession.close();
			}

		}
		return iCourseInfoCache;
	}
	private void clearCourseInfoTable() {
		iCourseInfoCache = null;
	}
	
	
	@Override
	public Collection<XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher) {
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
		return ret;
	}

	@Override
	public Collection<XCourseId> findCourses(CourseMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		List<XCourseId> ret = new ArrayList<XCourseId>();
		for (XCourse c : getCourseInfoTable().values())
			if (matcher.match(c)) ret.add(c);
		return ret;
	}

	@Override
	public Collection<XStudentId> findStudents(StudentMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		List<XStudentId> ret = new ArrayList<XStudentId>();
		for (Student student: ((StudentSectioningModel)currentSolution().getModel()).getStudents()) {
			if (student.isDummy()) continue;
			XStudentId s = new XStudentId(student);
			if (!student.isDummy() && matcher.match(s))
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
		for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings())
			for (Course course: offering.getCourses())
				if (course.getName().equalsIgnoreCase(courseName)) return getCourse(course.getId());
		return null;
	}

	@Override
	public XStudent getStudent(Long studentId) {
		for (Student student: ((StudentSectioningModel)currentSolution().getModel()).getStudents())
			if (!student.isDummy() && student.getId() == studentId)
				return new XStudent(student, currentSolution().getAssignment());
		return null;
	}

	@Override
	public XOffering getOffering(Long offeringId) {
		for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings())
			if (offering.getId() == offeringId)
				return new XOffering(offering, ((StudentSectioningModel)currentSolution().getModel()).getLinkedSections());
		return null;
	}

	@Override
	public <X extends OnlineSectioningAction> X createAction(Class<X> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
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
					h.info("Execution failed: " + e.getMessage());
				} else {
					h.warn("Execution failed: " + e.getMessage(), e.getCause());
				}
			} else {
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
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
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
	public boolean isMaster() {
		return true;
	}
	
	@Override
	public void releaseMasterLockIfHeld() {
	}

	@Override
	public Collection<XCourseRequest> getRequests(Long offeringId) {
		List<XCourseRequest> ret = new ArrayList<XCourseRequest>();
		for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings())
			if (offering.getId() == offeringId) {
				for (Course course: offering.getCourses())
					for (CourseRequest req: course.getRequests()) {
						if (!req.getStudent().isDummy())
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
		for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings())
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
			String name = parameters.getProperty("report", SectionConflictTable.class.getName());
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
		List<Long> ret = new ArrayList<Long>();
		Set<Long> sections = new HashSet<Long>();
		for (Student student: ((StudentSectioningModel)currentSolution().getModel()).getStudents())
			if (instructorExternalId.equals(student.getExternalId()))
				for (Unavailability unavailability: student.getUnavailabilities())
					sections.add(unavailability.getId());
		offerings: for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings()) {
			for (Config config: offering.getConfigs())
				for (Subpart subpart: config.getSubparts())
					for (Section section: subpart.getSections())
						if (sections.contains(section.getId())) {
							ret.add(offering.getId()); continue offerings;
						} else if (section.hasInstructors())
							for (Instructor instructor: section.getInstructors())
								if (instructorExternalId.equals(instructor.getExternalId())) {
									ret.add(offering.getId()); continue offerings;
								}
		}
		return ret;
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
			ret.add(new ReportTypeInterface(report.getReference(), report.getName(), StudentSolver.class.getName(), "reference", report.getReference()));
		return ret;
	}
	
	public void setReport(InMemoryReport report) {
		iReports.put(report.getReference(), report);
	}
	
	public InMemoryReport getReport(String reference) {
		return iReports.get(reference);
	}
}
