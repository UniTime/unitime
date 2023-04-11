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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.cpsolver.studentsct.StudentSectioningSaver;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.reservation.LearningCommunityReservation;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLogger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ApiMode;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Problem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.XEBatchSolverSaver.StudentMatcher;
import org.unitime.timetable.solver.studentsct.InMemoryReport;
import org.unitime.timetable.solver.studentsct.StudentSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

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
public class PurdueBatchSolverValidator extends StudentSectioningSaver {
	private static Log sLog = LogFactory.getLog(PurdueBatchSolverValidator.class);
    private String iInitiative = null;
    private String iTerm = null;
    private String iYear = null;
    private String iOwnerId = null;
    private Progress iProgress = null;

    private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	private AcademicSessionInfo iSession;
	private InMemoryReport iCSV;
	private int iNrThreads = 1;
	private boolean iCanContinue = true;
	
	private Hashtable<Long,CourseOffering> iCourses = null;
    private Hashtable<Long,Class_> iClasses = null;
    
    private Query iStudentQuery = null;
    
    private boolean iAutoOverrides = false;
	private Set<String> iAllowedOverrides = new HashSet<String>();
    private boolean iTimeConflictsIgnoreBreakTimes = false;
	private boolean iAutoTimeOverrides = false;
	private boolean iAutoLCOverrides = false;
	
	public PurdueBatchSolverValidator(Solver solver) {
        super(solver);
        iInitiative = solver.getProperties().getProperty("Data.Initiative");
        iYear = solver.getProperties().getProperty("Data.Year");
        iTerm = solver.getProperties().getProperty("Data.Term");
        iOwnerId = solver.getProperties().getProperty("General.OwnerPuid");
        iProgress = Progress.getInstance(getModel());
		try {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty())
				iExternalTermProvider = new BannerTermProvider();
			else
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external term provider, using the default one instead.", e);
			iExternalTermProvider = new BannerTermProvider();
		}
		iNrThreads = solver.getProperties().getPropertyInt("Save.XE.NrSaveThreads", 10);
		iCSV = new InMemoryReport("VALIDATION", "Last Validation Results (" + Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT).format(new Date()) + ")");
		((StudentSolver)solver).setReport(iCSV);
		
		String query = solver.getProperties().getProperty("Save.StudentQuery", null);
        if (query != null && !query.isEmpty()) {
        	iStudentQuery = new Query(query);
        	iProgress.info("Student filter: " + iStudentQuery); 
        }
        
        iAutoOverrides = solver.getProperties().getPropertyBoolean("Save.XE.AutoOverrides", false);
		String allowedOverrides = solver.getProperties().getProperty("Save.XE.AllowedOverrides", null);
		if (allowedOverrides != null && !allowedOverrides.isEmpty())
			iAllowedOverrides = new HashSet<String>(Arrays.asList(allowedOverrides.split(",")));
		iAutoTimeOverrides = solver.getProperties().getPropertyBoolean("Save.XE.AutoTimeOverrides", iAutoOverrides && iAllowedOverrides.contains("TIME-CNFLT"));
		iAutoLCOverrides = solver.getProperties().getPropertyBoolean("Save.XE.AutoLCOverrides", false);
		iTimeConflictsIgnoreBreakTimes = solver.getProperties().getPropertyBoolean("Save.XE.TimeConflictsIgnoreBreakTimes", false);
	}

	@Override
	public void save() throws Exception {
		iProgress.setStatus("Validating solution ...");
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		Context cx = new Context();
		cx.getParameters().add("readTimeout", getSpecialRegistrationApiReadTimeout());
		iClient.setContext(cx);
		iCSV.setHeader(new CSVField[] {
				new CSVField("PUID"),
				new CSVField("Name"),
				new CSVField("Area"),
				new CSVField("Clasf"),
				new CSVField("Major"),
				new CSVField("Course"),
				new CSVField("CRN"),
				new CSVField("Code"),
				new CSVField("Message"),
				new CSVField("Override")
		});
		org.hibernate.Session hibSession = null;
		Transaction tx = null;
		try {
			hibSession = SessionDAO.getInstance().getSession();
            hibSession.setCacheMode(CacheMode.IGNORE);
            hibSession.setHibernateFlushMode(FlushMode.MANUAL);
            
            tx = hibSession.beginTransaction();
            
            Session session = Session.getSessionUsingInitiativeYearTerm(iInitiative, iYear, iTerm);
            if (session == null) throw new Exception("Session "+iInitiative+" "+iTerm+iYear+" not found!");
            ApplicationProperties.setSessionId(session.getUniqueId());
            iSession = new AcademicSessionInfo(session);
		
            validate(session, hibSession);
            
            hibSession.flush();
            
            tx.commit(); tx = null;
		} catch (Exception e) {
            iProgress.fatal("Unable to validate, reason: "+e.getMessage(), e);
            sLog.error(e.getMessage(),e);
            if (tx != null) tx.rollback();
        } finally {
            if (hibSession!=null && hibSession.isOpen()) hibSession.close();
			try {
				iClient.stop();
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
			}
		}
	}
	
	public void validate(Session session, org.hibernate.Session hibSession) {
		setPhase("Loading classes...", 1);
		iClasses = new Hashtable<Long, Class_>();
        for (Class_ clazz: (List<Class_>)hibSession.createQuery(
        		"select distinct c from Class_ c where " +
        		"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
        		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            iClasses.put(clazz.getUniqueId(),clazz);
        }
        incProgress();
        
        iCourses = new Hashtable<Long, CourseOffering>();
        setPhase("Loading courses...", 1);
        for (CourseOffering course: (List<CourseOffering>)hibSession.createQuery(
        		"select distinct c from CourseOffering c where c.subjectArea.session.uniqueId = :sessionId")
        		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            iCourses.put(course.getUniqueId(), course);
        }
        incProgress();
            
        List<Student> students = new ArrayList<Student>(getModel().getStudents().size());
		for (Student student: getModel().getStudents()) {
            if (student.isDummy()) continue;
            if (iStudentQuery != null && !iStudentQuery.match(new StudentMatcher(student, iSession, getAssignment()))) continue;
            students.add(student);
		}
		setPhase("Validating students...", students.size());
		if (iNrThreads <= 1) {
			for (Student student: students) {
	            incProgress();
	            if (student.isDummy()) continue;
	            validateStudent(student);
	        }
		} else {
			List<Worker> workers = new ArrayList<Worker>();
			Iterator<Student> studentsIterator = students.iterator();
			for (int i = 0; i < iNrThreads; i++)
				workers.add(new Worker(i, studentsIterator));
			for (Worker worker: workers) worker.start();
			for (Worker worker: workers) {
				try {
					worker.join();
				} catch (InterruptedException e) {
					iCanContinue = false;
					try { worker.join(); } catch (InterruptedException x) {}
				}
			}
			if (!iCanContinue)
				throw new RuntimeException("The validate was interrupted.");
		}
	}
	
	protected void validateStudent(Student student) {
		long c0 = OnlineSectioningHelper.getCpuTime();
		OnlineSectioningLog.Action.Builder action = OnlineSectioningLog.Action.newBuilder();
		action.setOperation("batch-validate");
		action.setSession(OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(iSession.getUniqueId())
    			.setName(iSession.toCompactString())
    			);
    	action.setStartTime(System.currentTimeMillis());
    	action.setUser(getUser());
    	action.setStudent(
    			OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(student.getId())
    			.setExternalId(student.getExternalId())
    			.setName(student.getName())
    			.setType(OnlineSectioningLog.Entity.EntityType.STUDENT)
    			);
    	OnlineSectioningLog.Enrollment.Builder requested = OnlineSectioningLog.Enrollment.newBuilder();
    	requested.setType(OnlineSectioningLog.Enrollment.EnrollmentType.REQUESTED);
    	for (Request request: student.getRequests()) {
    		action.addRequest(OnlineSectioningHelper.toProto(request));
    		if (request instanceof CourseRequest) {
    			Enrollment e = getAssignment().getValue(request);
    			if (e != null)
    				for (Section section: e.getSections())
    					requested.addSection(OnlineSectioningHelper.toProto(section, e));
    		}
    	}
    	action.addEnrollment(requested);
    	List<CSVField[]> csv = new ArrayList<CSVField[]>();
        try {
        	validate(student, action, csv);
        } finally {
        	action.setEndTime(System.currentTimeMillis()).setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
		}
        StringBuffer table = new StringBuffer();
        synchronized (iCSV) {
        	for (CSVField[] line: csv) {
        		if (table.length() > 0) table.append("\n");
        		table.append(iCSV.addLine(line));
        	}
        	action.addOptionBuilder().setKey("table").setValue(table.toString());
		}
        OnlineSectioningLogger.getInstance().record(OnlineSectioningLog.Log.newBuilder().addAction(action).build());
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
		});
		return builder.create();
	}
	
	protected String getSpecialRegistrationApiReadTimeout() {
		return ApplicationProperties.getProperty("purdue.specreg.readTimeout", "60000");
	}
	
	protected String getSpecialRegistrationApiSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site");
	}
	
	protected String getSpecialRegistrationApiValidationSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site.validation", getSpecialRegistrationApiSite() + "/checkRestrictions");
	}
	
	protected String getSpecialRegistrationApiKey() {
		return ApplicationProperties.getProperty("purdue.specreg.apiKey");
	}
	
	protected ApiMode getSpecialRegistrationApiMode() {
		return ApiMode.valueOf(ApplicationProperties.getProperty("purdue.specreg.mode.batch", "PREREG"));
	}
	
	protected Set<String> getLCCrns(Student student) {
		Set<String> crns = new TreeSet<String>();
		if (!iAutoLCOverrides) return crns;
		for (Request request: student.getRequests()) {
			Enrollment enrollment = getAssignment().getValue(request);
			if (enrollment != null && enrollment.isCourseRequest() && enrollment.getReservation() != null && enrollment.getReservation() instanceof LearningCommunityReservation) {
				CourseOffering course = iCourses.get(enrollment.getCourse().getId());
				for (Section section: enrollment.getSections()) {
					Class_ clazz = iClasses.get(section.getId());
					if (clazz != null && course != null) crns.add(clazz.getExternalId(course));
				}
			}
		}
		return crns;
	}
	
	protected static String defaultOverrides[] = new String[] {
		    "CAMP", "CAMPUS",
		    "CLAS", "CLASS",
		    "CLOS", "CLOSED",
		    "COLL", "COLLEGE",
		    "CORQ", "CO-REQ",
		    "DEGR", "DEGREE",
		    "DEPT", "DPT-PERMIT",
		    "DUPL", "DUP-CRSE",
		    "LEVL", "LEVEL",
		    "MAJR", "MAJOR",
		    "PREQ", "PRE-REQ",
		    "PROG", "PROGRAM",
		    "TIME", "TIME-CNFLT",
		    "CHRT", "COHORT",
		    "REPH", "REPEATMHRS",
		    };
	
	protected String getDefaultOverride(Student student, String crn, String messageType) {
	    String override = null;
	    if ("DEPT".equals(messageType) || "SAPR".equals(messageType)) {
	    	OfferingConsentType consent = getConsent(student, crn);
	    	if (consent != null && "IN".equals(consent.getReference())) {
	    		override = "INST-PERMT";
	    	} else if (consent != null && "DP".equals(consent.getReference())) {
	    		override = "DPT-PERMIT";
	    	} else {
	    		override = "HONORS";
	    	}
	    } else {
		    for (int i = 0; i < defaultOverrides.length; i+= 2) {
		    	if (messageType.equals(defaultOverrides[i])) override = defaultOverrides[i + 1];
		    }
	    }
	    return getSolver().getProperties().getProperty("Save.XE.Override." + messageType, override);
	}
	
	protected boolean shareHoursIgnoreBreakTime(TimeLocation t1, TimeLocation t2) {
    	int s1 = t1.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    	int e1 = (t1.getStartSlot() + t1.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - t1.getBreakTime();
    	int s2 = t2.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    	int e2 = (t2.getStartSlot() + t2.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - t2.getBreakTime();
    	return e1 > s2 && e2 > s1;
    }
	
	protected String getCrn(Enrollment enrollment, Section section) {
		CourseOffering course = iCourses.get(enrollment.getCourse().getId());
		Class_ clazz = iClasses.get(section.getId());
		if (clazz != null && course != null) return clazz.getExternalId(course);
		return null;
	}
	
	protected boolean inConflict(SctAssignment a1, SctAssignment a2) {
        if (a1.getTime() == null || a2.getTime() == null) return false;
        if (iTimeConflictsIgnoreBreakTimes) {
        	TimeLocation t1 = a1.getTime();
        	TimeLocation t2 = a2.getTime();
        	return t1.shareDays(t2) && shareHoursIgnoreBreakTime(t1, t2) && t1.shareWeeks(t2);
        } else {
        	return a1.getTime().hasIntersection(a2.getTime());
        }
    }
	
	protected Set<String> getTimeConflicts(Student student) {
		Set<String> crns = new TreeSet<String>();
		for (int i1 = 0; i1 < student.getRequests().size(); i1++) {
			Request r1 = student.getRequests().get(i1);
			Enrollment e1 = getAssignment().getValue(r1);
			if (e1 != null && e1.isCourseRequest()) {
				for (int i2 = i1 + 1; i2 < student.getRequests().size(); i2++) {
					Request r2 = student.getRequests().get(i2);
					Enrollment e2 = getAssignment().getValue(r2);
					if (e2 != null && e2.isCourseRequest()) {
						for (Section s1 : e1.getSections()) {
				            for (Section s2 : e2.getSections()) {
				                if (inConflict(s1, s2)) {
				                	if (s1.isAllowOverlap()) {
				                		String crn = getCrn(e1, s1);
				                		if (crn != null) crns.add(crn);
				                	} else if (s2.isAllowOverlap()) {
				                		String crn = getCrn(e2, s2);
				                		if (crn != null) crns.add(crn);
				                	}
				                }
				            }
						}	
					}
				}
			}
		}
		return crns;
	}
	
	protected void validate(Student student, OnlineSectioningLog.Action.Builder action, List<CSVField[]> csv) {
		iProgress.info("[" + student.getExternalId() + "] " + student.getName());
		
		String term = iExternalTermProvider.getExternalTerm(iSession);
		String campus = iExternalTermProvider.getExternalCampus(iSession);
		String puid = getBannerId(student);
		
		Set<String> lcCrns = getLCCrns(student);
		Set<String> timeCrns = (iAutoTimeOverrides ? getTimeConflicts(student) : null);

		CheckRestrictionsRequest req = new CheckRestrictionsRequest();
		req.studentId = puid;
		req.term = term;
		req.campus = campus;
		req.mode = getSpecialRegistrationApiMode();
		
		for (Request request: student.getRequests()) {
			Enrollment enrollment = getAssignment().getValue(request);
			if (enrollment != null && enrollment.isCourseRequest()) {
				CourseOffering course = iCourses.get(enrollment.getCourse().getId());
				if (course != null) {
					for (Section section: enrollment.getSections()) {
						Class_ clazz = iClasses.get(section.getId());
						if (clazz != null)
							SpecialRegistrationHelper.addCrn(req, clazz.getExternalId(course));
					}
				}
			}
		}
		if (req.changes == null) {
			action.addOptionBuilder().setKey("validation_request").setValue(getGson().toJson(req));
			action.setResult(OnlineSectioningLog.Action.ResultType.NULL);
			return;
		}
		
		CheckRestrictionsResponse resp = null;
		ClientResource resource = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiValidationSite());
			resource.setNext(iClient);
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			Gson gson = getGson();
			action.addOptionBuilder().setKey("validation_request").setValue(gson.toJson(req));
			long t1 = System.currentTimeMillis();
			
			resource.post(new GsonRepresentation<CheckRestrictionsRequest>(req));
			
			action.setApiPostTime(System.currentTimeMillis() - t1);
			
			resp = (CheckRestrictionsResponse)new GsonRepresentation<CheckRestrictionsResponse>(resource.getResponseEntity(), CheckRestrictionsResponse.class).getObject();
			action.addOptionBuilder().setKey("validation_response").setValue(gson.toJson(resp));
			
			if (ResponseStatus.success != resp.status)
				throw new SectioningException(resp.message == null || resp.message.isEmpty() ? "Failed to check student eligibility (" + resp.status + ")." : resp.message);
		} catch (Exception e) {
			action.setApiException(e.getMessage());
			action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			action.addMessage(OnlineSectioningLog.Message.newBuilder()
					.setLevel(OnlineSectioningLog.Message.Level.FATAL)
					.setText(e.getClass().getSimpleName() + ": " + e.getMessage()));
			iProgress.error("[" + student.getExternalId() + "] Failed to validate: " + e.getMessage(), e);
			String area = "", clasf = "", major = "";
			for (AreaClassificationMajor acm: student.getAreaClassificationMajors()) {
				area += (area.isEmpty() ? "" : "\n") + (acm.getArea() == null ? "" : acm.getArea());
				clasf += (clasf.isEmpty() ? "" : "\n") + (acm.getClassification() == null ? "" : acm.getClassification());
				major += (major.isEmpty() ? "" : "\n") + (acm.getMajor() == null ? "" : acm.getMajor());
			}
			csv.add(new CSVField[] {
					new CSVField(puid),
					new CSVField(student.getName()),
					new CSVField(area),
					new CSVField(clasf),
					new CSVField(major),
					new CSVField(""),
					new CSVField(""),
					new CSVField("FAIL"),
					new CSVField(e.getMessage())
			});
			return;
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
		
		action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
		if (resp != null && resp.outJson != null && resp.outJson.problems != null) {
			String area = "", clasf = "", major = "";
			for (AreaClassificationMajor acm: student.getAreaClassificationMajors()) {
				area += (area.isEmpty() ? "" : "\n") + (acm.getArea() == null ? "" : acm.getArea());
				clasf += (clasf.isEmpty() ? "" : "\n") + (acm.getClassification() == null ? "" : acm.getClassification());
				major += (major.isEmpty() ? "" : "\n") + (acm.getMajor() == null ? "" : acm.getMajor());
			}
			for (Problem p: resp.outJson.problems) {
				String grantedOverride = null;
				if (p.crn == null) {
					iProgress.warn("[" + student.getExternalId() + "] " + p.message);
					csv.add(new CSVField[] {
							new CSVField(puid),
							new CSVField(student.getName()),
							new CSVField(area),
							new CSVField(clasf),
							new CSVField(major),
							new CSVField(""),
							new CSVField(""),
							new CSVField(p.code),
							new CSVField(p.message),
					});
					action.addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.WARN)
							.setText(p.message));
				} else {
					if ("TIME".equals(p.code) && timeCrns != null) {
						if (timeCrns.contains(p.crn)) {
							grantedOverride = "TIME-CNFLT";
						} else if (p.message != null && p.message.startsWith("Time conflict with CRN ")) {
							String crn = p.message.substring("Time conflict with CRN ".length());
							if (timeCrns.contains(crn))
								grantedOverride = "TIME-CNFLT for " + crn;
						}
					} else if (iAutoOverrides) {
						String override = getDefaultOverride(student, p.crn, p.code);
						if (override != null && (iAllowedOverrides.contains(override) || lcCrns.contains(p.crn))) {
							grantedOverride = override;
						}
					}
					csv.add(new CSVField[] {
							new CSVField(puid),
							new CSVField(student.getName()),
							new CSVField(area),
							new CSVField(clasf),
							new CSVField(major),
							new CSVField(getCourseNameForCrn(student, p.crn)),
							new CSVField(p.crn),
							new CSVField(p.code),
							new CSVField(p.message),
							new CSVField(grantedOverride),
					});
					if (grantedOverride == null) {
						iProgress.warn("[" + student.getExternalId() + "] " + p.crn + ": " + p.message);
						action.addMessage(OnlineSectioningLog.Message.newBuilder()
								.setLevel(OnlineSectioningLog.Message.Level.WARN)
								.setText(p.crn + ": " + p.message));
					} else {
						iProgress.info("[" + student.getExternalId() + "] " + p.crn + ": " + p.message + " -- " + grantedOverride);
						action.addMessage(OnlineSectioningLog.Message.newBuilder()
								.setLevel(OnlineSectioningLog.Message.Level.INFO)
								.setText(p.crn + ": " + p.message + " (" + grantedOverride + " override will be requested)"));
					}
				}
				if (grantedOverride == null)
					action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
			}
		}
	}
	protected String getBannerId(Student student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	public String getCourseNameForCrn(Student student, String crn) {
		for (Request request: student.getRequests()) {
			Enrollment enrollment = getAssignment().getValue(request);
			if (enrollment != null && enrollment.isCourseRequest()) {
				CourseOffering course = iCourses.get(enrollment.getCourse().getId());
				for (Section section: enrollment.getSections()) {
					Class_ clazz = iClasses.get(section.getId());
					if (clazz != null && course != null && crn.equals(clazz.getExternalId(course)))
						return course.getCourseName();
				}
			}
		}
		return null;
	}
	
	public OfferingConsentType getConsent(Student student, String crn) {
		for (Request request: student.getRequests()) {
			Enrollment enrollment = getAssignment().getValue(request);
			if (enrollment != null && enrollment.isCourseRequest()) {
				CourseOffering course = iCourses.get(enrollment.getCourse().getId());
				for (Section section: enrollment.getSections()) {
					Class_ clazz = iClasses.get(section.getId());
					if (clazz != null && course != null && crn.equals(clazz.getExternalId(course)))
						course.getConsentType();
				}
			}
		}
		return null;
	}
	
	protected Entity getUser() {
		return Entity.newBuilder().setExternalId(iOwnerId).setType(Entity.EntityType.MANAGER).build();
	}
	
    protected void checkTermination() {
    	if (getTerminationCondition() != null && !getTerminationCondition().canContinue(getSolution()))
    		throw new RuntimeException("The validate was interrupted.");
    }
    
    protected void setPhase(String phase, long progressMax) {
    	checkTermination();
    	iProgress.setPhase(phase, progressMax);
    }
    
    protected void incProgress() {
    	checkTermination();
    	iProgress.incProgress();
    }
	
	protected class Worker extends Thread {
		private Iterator<Student> iStudents;
		
		public Worker(int index, Iterator<Student> students) {
			setName("XEValidator-" + (1 + index));
			iStudents = students;
		}
		
		@Override
	    public void run() {
			iProgress.debug(getName() + " has started.");
			try {
				ApplicationProperties.setSessionId(iSession.getUniqueId());
				while (true) {
					Student student = null;
					synchronized (iStudents) {
						if (!iCanContinue) {
							iProgress.debug(getName() + " has stopped.");
							return;
						}
						if (!iStudents.hasNext()) break;
						student = iStudents.next();
						iProgress.incProgress();
					}
					if (!student.isDummy())
						validateStudent(student);
				}
				iProgress.debug(getName() + " has finished.");
			} catch (Exception e) {
				iProgress.error(getName() + " has failed: " + e.getMessage(), e);
			} finally {
				ApplicationProperties.setSessionId(null);
				HibernateUtil.closeCurrentThreadSessions();
			}
		}
	}
}
