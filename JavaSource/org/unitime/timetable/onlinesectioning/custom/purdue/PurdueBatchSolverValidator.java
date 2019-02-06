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
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.cpsolver.studentsct.StudentSectioningSaver;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
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
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Problem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.solver.studentsct.InMemoryReport;
import org.unitime.timetable.solver.studentsct.StudentSolver;
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
	}

	@Override
	public void save() throws Exception {
		iProgress.setStatus("Validating solution ...");
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		iCSV.setHeader(new CSVField[] {
				new CSVField("PUID"),
				new CSVField("Name"),
				new CSVField("Course"),
				new CSVField("CRN"),
				new CSVField("Code"),
				new CSVField("Message")
		});
		org.hibernate.Session hibSession = null;
		Transaction tx = null;
		try {
			hibSession = SessionDAO.getInstance().getSession();
            hibSession.setCacheMode(CacheMode.IGNORE);
            hibSession.setFlushMode(FlushMode.MANUAL);
            
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
        		.setLong("sessionId", session.getUniqueId()).list()) {
            iClasses.put(clazz.getUniqueId(),clazz);
        }
        incProgress();
        
        iCourses = new Hashtable<Long, CourseOffering>();
        setPhase("Loading courses...", 1);
        for (CourseOffering course: (List<CourseOffering>)hibSession.createQuery(
        		"select distinct c from CourseOffering c where c.subjectArea.session.uniqueId = :sessionId")
        		.setLong("sessionId", session.getUniqueId()).list()) {
            iCourses.put(course.getUniqueId(), course);
        }
        incProgress();
            
		setPhase("Validating students...", getModel().getStudents().size());
		if (iNrThreads <= 1) {
			for (Student student: getModel().getStudents()) {
	            incProgress();
	            if (student.isDummy()) continue;
	            validateStudent(student);
	        }
		} else {
			List<Worker> workers = new ArrayList<Worker>();
			Iterator<Student> students = getModel().getStudents().iterator();
			for (int i = 0; i < iNrThreads; i++)
				workers.add(new Worker(i, students));
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
	
	protected String getSpecialRegistrationApiSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site");
	}
	
	protected String getSpecialRegistrationApiValidationSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site.validation", getSpecialRegistrationApiSite() + "/checkRestrictionsForSTAR");
	}
	
	protected String getSpecialRegistrationApiKey() {
		return ApplicationProperties.getProperty("purdue.specreg.apiKey");
	}
	
	protected String getSpecialRegistrationApiMode() {
		return ApplicationProperties.getProperty("purdue.specreg.mode.batch", "PREREG");
	}
	
	protected void validate(Student student, OnlineSectioningLog.Action.Builder action, List<CSVField[]> csv) {
		iProgress.info("[" + student.getExternalId() + "] " + student.getName());
		
		String term = iExternalTermProvider.getExternalTerm(iSession);
		String campus = iExternalTermProvider.getExternalCampus(iSession);
		String puid = getBannerId(student);

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
							req.add(clazz.getExternalId(course));
					}
				}
			}
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
			
			if (!ResponseStatus.success.name().equals(resp.status))
				throw new SectioningException(resp.message == null || resp.message.isEmpty() ? "Failed to check student eligibility (" + resp.status + ")." : resp.message);
		} catch (Exception e) {
			action.setApiException(e.getMessage());
			action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			action.addMessage(OnlineSectioningLog.Message.newBuilder()
					.setLevel(OnlineSectioningLog.Message.Level.FATAL)
					.setText(e.getClass().getSimpleName() + ": " + e.getMessage()));
			iProgress.error("[" + student.getExternalId() + "] Failed to validate: " + e.getMessage(), e);
			csv.add(new CSVField[] {
					new CSVField(puid),
					new CSVField(student.getName()),
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
			for (Problem p: resp.outJson.problems) {
				if (p.crn == null) {
					iProgress.warn("[" + student.getExternalId() + "] " + p.message);
					csv.add(new CSVField[] {
							new CSVField(puid),
							new CSVField(student.getName()),
							new CSVField(""),
							new CSVField(""),
							new CSVField(p.code),
							new CSVField(p.message)
					});
					action.addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.WARN)
							.setText(p.message));
				} else {
					csv.add(new CSVField[] {
							new CSVField(puid),
							new CSVField(student.getName()),
							new CSVField(getCourseNameForCrn(student, p.crn)),
							new CSVField(p.crn),
							new CSVField(p.code),
							new CSVField(p.message)
					});
					iProgress.warn("[" + student.getExternalId() + "] " + p.crn + ": " + p.message);
					action.addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.WARN)
							.setText(p.crn + ": " + p.message));
				}
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
		}
	}
}
