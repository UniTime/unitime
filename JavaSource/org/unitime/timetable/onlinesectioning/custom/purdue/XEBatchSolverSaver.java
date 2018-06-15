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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
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
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.model.XStudent;
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
public class XEBatchSolverSaver extends StudentSectioningSaver {
	private static Log sLog = LogFactory.getLog(XEBatchSolverSaver.class);
    private String iInitiative = null;
    private String iTerm = null;
    private String iYear = null;
    private String iOwnerId = null;
    private Progress iProgress = null;

    private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	private AcademicSessionInfo iSession;
	private String iHoldPassword = null;
	private String iRegistrationDate = null;
	private String iActionAdd = null, iActionDrop = null;
	private boolean iConditionalAddDrop = true;
	private InMemoryReport iCSV;
	private boolean iAutoOverrides = false;
	private Set<String> iAllowedOverrides = new HashSet<String>();
	private int iNrThreads = 1;
	private boolean iCanContinue = true;
	
	private Hashtable<Long,CourseOffering> iCourses = null;
    private Hashtable<Long,Class_> iClasses = null;
	private List<XStudent> iUpdatedStudents = new ArrayList<XStudent>();
	
	public XEBatchSolverSaver(Solver solver) {
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
		iHoldPassword = solver.getProperties().getProperty("Save.XE.HoldPassword");
		iRegistrationDate = solver.getProperties().getProperty("Save.XE.RegistrationDate");
		iActionAdd = solver.getProperties().getProperty("Save.XE.ActionAdd", "RE");
		iActionDrop = solver.getProperties().getProperty("Save.XE.ActionDrop", "DDD");
		iConditionalAddDrop = solver.getProperties().getPropertyBoolean("Save.XE.ConditionalAddDrop", true);
		iAutoOverrides = solver.getProperties().getPropertyBoolean("Save.XE.AutoOverrides", false);
		String allowedOverrides = solver.getProperties().getProperty("Save.XE.AllowedOverrides", null);
		if (allowedOverrides != null && !allowedOverrides.isEmpty())
			iAllowedOverrides = new HashSet<String>(Arrays.asList(allowedOverrides.split(",")));
		iNrThreads = solver.getProperties().getPropertyInt("Save.XE.NrSaveThreads", 10);
		iCSV = new InMemoryReport("XE", "Last XE Enrollment Results (" + Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT).format(new Date()) + ")");
		((StudentSolver)solver).setReport(iCSV);
	}

	@Override
	public void save() throws Exception {
		iProgress.setStatus("Saving solution ...");
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		iCSV.setHeader(new CSVField[] {
				new CSVField("PUID"),
				new CSVField("Name"),
				new CSVField("Course"),
				new CSVField("CRN"),
				new CSVField("Request"),
				new CSVField("Status"),
				new CSVField("Message"),
				new CSVField("Used Override")
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
		
            save(session, hibSession);
            
            if (!iUpdatedStudents.isEmpty() && CustomStudentEnrollmentHolder.isCanRequestUpdates()) {
            	CustomStudentEnrollmentHolder.getProvider().requestUpdate((OnlineSectioningServer)getSolver(), new OnlineSectioningHelper(hibSession, getUser()), iUpdatedStudents);
            }
            
            hibSession.flush();
            
            tx.commit(); tx = null;
		} catch (Exception e) {
            iProgress.fatal("Unable to save , reason: "+e.getMessage(), e);
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
	
	public void save(Session session, org.hibernate.Session hibSession) {
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
            
		setPhase("Enrolling students...", getModel().getStudents().size());
		if (iNrThreads <= 1) {
			for (Student student: getModel().getStudents()) {
	            incProgress();
	            if (student.isDummy()) continue;
	            saveStudent(student);
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
				throw new RuntimeException("The save was interrupted.");
		}
	}
	
	protected void saveStudent(Student student) {
		long c0 = OnlineSectioningHelper.getCpuTime();
		OnlineSectioningLog.Action.Builder action = OnlineSectioningLog.Action.newBuilder();
		action.setOperation("batch-enroll");
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
        	enroll(student, getCrns(student), action, csv);
        } catch (Exception e) {
        	if (e instanceof SectioningException) {
				if (e.getCause() == null) {
					iProgress.info("Enrollment failed: " + e.getMessage());
				} else {
					iProgress.warn("Enrollment failed: " + e.getMessage(), e.getCause());
				}
			} else {
				iProgress.error("Enrollment failed: " + e.getMessage(), e);
			}
        	String puid = getBannerId(student);
        	for (String id: getCrns(student)) {
        		if (id == null) continue;
        		csv.add(new CSVField[] {
        				new CSVField(puid),
						new CSVField(student.getName()),
						new CSVField(getCourseNameForCrn(student, id)),
						new CSVField(id),
						new CSVField("Add"),
						new CSVField("Failed"),
						new CSVField(e.getMessage())
        		});
        	}
        	action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
        	if (e.getMessage() != null)
        		action.setApiException(e.getMessage());
			if (e.getCause() != null && e instanceof SectioningException)
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.FATAL)
						.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
			else
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.FATAL)
						.setText(e.getMessage() == null ? "null" : e.getMessage()));
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
            iUpdatedStudents.add(new XStudent(student, getAssignment()));
		}
        OnlineSectioningLogger.getInstance().record(OnlineSectioningLog.Log.newBuilder().addAction(action).build());
	}
	
	protected Set<String> getCrns(Student student) {
		Set<String> crns = new TreeSet<String>();
		for (Request request: student.getRequests()) {
			Enrollment enrollment = getAssignment().getValue(request);
			if (enrollment != null && enrollment.isCourseRequest()) {
				CourseOffering course = iCourses.get(enrollment.getCourse().getId());
				for (Section section: enrollment.getSections()) {
					Class_ clazz = iClasses.get(section.getId());
					if (clazz != null && course != null) crns.add(clazz.getExternalId(course));
				}
			}
		}
		return crns;
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
	
	protected void enroll(Student student, Set<String> crns, OnlineSectioningLog.Action.Builder action, List<CSVField[]> csv) throws IOException {
		iProgress.info("[" + student.getExternalId() + "] " + student.getName() + " " + crns);
		
		ClientResource resource = null;
		try {
			resource = new ClientResource(ApplicationProperties.getProperty("banner.xe.site"));
			resource.setNext(iClient);
			resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, ApplicationProperties.getProperty("banner.xe.admin.user"), ApplicationProperties.getProperty("banner.xe.admin.password"));
			
			String term = iExternalTermProvider.getExternalTerm(iSession);
			String campus = iExternalTermProvider.getExternalCampus(iSession);
			String puid = getBannerId(student);
			
			resource.addQueryParameter("term", term);
		    resource.addQueryParameter("bannerId", puid);
		    resource.addQueryParameter("systemIn", "SB");
		    resource.addQueryParameter("persona", "SB");
		    if (iHoldPassword != null && !iHoldPassword.isEmpty())
		        resource.addQueryParameter("holdPassword", iHoldPassword);
		    action.addOptionBuilder().setKey("term").setValue(term);
			action.addOptionBuilder().setKey("bannerId").setValue(getBannerId(student));
			Gson gson = getGson();
		    
			long t0 = System.currentTimeMillis();
		    XEInterface.RegisterResponse original = null;
		    try {
		    	original = getSchedule(student, resource);
		    } finally {
		    	action.setApiGetTime(System.currentTimeMillis() - t0);
		    }
		    action.addOptionBuilder().setKey("original").setValue(gson.toJson(original));
		    
		    Set<String> noadd = new HashSet<String>();
		    Set<String> nodrop = new HashSet<String>();
		    Set<String> notregistered = new HashSet<String>();
		    Map<String, XEInterface.Registration> registered = new HashMap<String, XEInterface.Registration>();
		    if (original.registrations != null) {
		    	OnlineSectioningLog.Enrollment.Builder previous = OnlineSectioningLog.Enrollment.newBuilder();
		    	previous.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
		    	for (XEInterface.Registration reg: original.registrations) {
		    		if (reg.isRegistered()) {
		    			registered.put(reg.courseReferenceNumber, reg);
		    			if (!reg.can(iActionDrop))
		    				nodrop.add(reg.courseReferenceNumber);
		    			previous.addSectionBuilder()
		    				.setClazz(OnlineSectioningLog.Entity.newBuilder().setName(reg.courseReferenceNumber))
		    				.setCourse(OnlineSectioningLog.Entity.newBuilder().setName(reg.subject + " " + reg.courseNumber))
		    				.setSubpart(OnlineSectioningLog.Entity.newBuilder().setName(reg.scheduleType));
		    		} else {
		    			notregistered.add(reg.courseReferenceNumber);
		    			if (!reg.can(iActionAdd))
		    				noadd.add(reg.courseReferenceNumber);
		    		}
		    	}
		    	action.addEnrollment(previous);
		    }
		    
			action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
		    Set<String> added = new HashSet<String>();
		    XEInterface.RegisterRequest req = new XEInterface.RegisterRequest(term, puid, null, true);
		    if (iHoldPassword != null && !iHoldPassword.isEmpty())
		        req.holdPassword = iHoldPassword;
			if (iRegistrationDate != null && !iRegistrationDate.isEmpty())
		        req.registrationDate = iRegistrationDate;
			if (iConditionalAddDrop)
				req.conditionalAddDrop = "Y";
			
			for (String id: crns) {
				if (id == null) continue;
				if (!registered.containsKey(id) && noadd.contains(id)) {
					csv.add(new CSVField[] {
							new CSVField(puid),
							new CSVField(student.getName()),
							new CSVField(getCourseNameForCrn(student, id)),
							new CSVField(id),
							new CSVField("Add"),
							new CSVField("Failed"),
							new CSVField("Action " + iActionAdd + " is not allowed.")
					});
					iProgress.warn("[" + student.getExternalId() + "] " + id + ": Action " + iActionAdd + " is not allowed.");
					action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
					action.addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.WARN)
							.setText(id + ": Action " + iActionAdd + " is not allowed."));
				} else {
					if (registered.containsKey(id)) {
						if (added.add(id)) keep(req, id);
					} else {
						if (added.add(id)) add(req, id, notregistered.contains(id));
					}
				}
			}
		    Set<String> dropped = new HashSet<String>();
			for (String id: registered.keySet()) {
				if (added.contains(id)) continue;
				XEInterface.Registration reg = registered.get(id);
				if (!campus.equals(reg.campus)) {
					if (added.add(id)) keep(req, id);
				} else if (nodrop.contains(id)) {
					csv.add(new CSVField[] {
							new CSVField(puid),
							new CSVField(student.getName()),
							new CSVField(reg.subject + " " + reg.courseNumber),
							new CSVField(id),
							new CSVField("Drop"),
							new CSVField("Failed"),
							new CSVField("Action " + iActionDrop + " is not allowed.")
					});
					iProgress.warn("[" + student.getExternalId() + "] " + id + ": Action " + iActionDrop + " is not allowed.");
					action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
					action.addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.WARN)
							.setText(id + ": Action " + iActionDrop + " is not allowed."));					
					if (added.add(id)) keep(req, id);
				} else {
					drop(req, id);
					dropped.add(id);
				}
			}
			Map<String, Set<String>> appliedOverrides = new HashMap<String, Set<String>>();
			
			action.addOptionBuilder().setKey("request").setValue(gson.toJson(req));
			long t1 = System.currentTimeMillis();
			XEInterface.RegisterResponse response = null;
			try {
				response = postChanges(resource, req);
			} finally {
				action.setApiPostTime(System.currentTimeMillis() - t1);
			}
			action.addOptionBuilder().setKey("response").setValue(gson.toJson(response));
			
			int index = 1;
			while (iAutoOverrides && response.registrations != null) {
				boolean changed = false;
				for (XEInterface.Registration reg: response.registrations) {
					String id = reg.courseReferenceNumber;
					if (reg.crnErrors != null && "F".equals(reg.statusIndicator)) {
						for (XEInterface.CrnError e: reg.crnErrors) {
							String override = getDefaultOverride(student, id, e.messageType);
							if (override != null && iAllowedOverrides.contains(override)) {
								if (addOverride(student, req, id, override, appliedOverrides)) { changed = true; break; }
							}
						}
					}
					if (!iConditionalAddDrop && dropped.contains(reg.courseReferenceNumber) && ("Deleted".equals(reg.statusDescription) || "Dropped".equals(reg.statusDescription))) {
						removeAction(req, reg.courseReferenceNumber);
					}
				}
				if (!changed) break;
				action.addOptionBuilder().setKey("request-override-" + index).setValue(gson.toJson(req));
				long t2 = System.currentTimeMillis();
				try {
					response = postChanges(resource, req);
				} finally {
					action.setApiPostTime(System.currentTimeMillis() - t2 + action.getApiPostTime());
				}
				action.addOptionBuilder().setKey("response-override-" + index).setValue(gson.toJson(response));
				index ++;
			}
			
			Set<String> checked = new HashSet<String>();
			if (response.registrations != null) {
				OnlineSectioningLog.Enrollment.Builder stored = OnlineSectioningLog.Enrollment.newBuilder();
				stored.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
				for (XEInterface.Registration reg: response.registrations) {
					if ("Registered".equals(reg.statusDescription)) {
						stored.addSectionBuilder()
	    					.setClazz(OnlineSectioningLog.Entity.newBuilder().setName(reg.courseReferenceNumber))
	    					.setCourse(OnlineSectioningLog.Entity.newBuilder().setName(reg.subject + " " + reg.courseNumber))
	    					.setSubpart(OnlineSectioningLog.Entity.newBuilder().setName(reg.scheduleType));
					}
					String id = reg.courseReferenceNumber;
					checked.add(id);
					String op = (added.contains(id) ? "Add" : "Drop");
					if (notregistered.contains(id)) continue;
					String error = null;
					if (reg.crnErrors != null && !reg.crnErrors.isEmpty()) 
						for (XEInterface.CrnError e: reg.crnErrors) {
							if (error == null)
								error = e.messageType + ": " + e.message;
							else
								error += "\n" + e.messageType + ": " + e.message;;
						}
					csv.add(new CSVField[] {
							new CSVField(puid),
							new CSVField(student.getName()),
							new CSVField(reg.subject + " " + reg.courseNumber),
							new CSVField(id),
							new CSVField(op),
							new CSVField(reg.statusDescription),
							new CSVField(error),
							new CSVField(getOverride(req, id, appliedOverrides))
					});
					if (error != null) {
						if ("F".equals(reg.statusIndicator)) {
							iProgress.warn("[" + student.getExternalId() + "] " + id + ": " + error);
							action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
							action.addMessage(OnlineSectioningLog.Message.newBuilder()
									.setLevel(OnlineSectioningLog.Message.Level.WARN)
									.setText(id + ": " + error));
						} else {
							iProgress.info("[" + student.getExternalId() + "] " + id + ": " + error);
							action.addMessage(OnlineSectioningLog.Message.newBuilder()
									.setLevel(OnlineSectioningLog.Message.Level.INFO)
									.setText(id + ": " + error));
						}
					}
				}
				action.addEnrollment(stored);
			}
			
			if (response.failedRegistrations != null) {
				for (XEInterface.FailedRegistration reg: response.failedRegistrations) {
					if (reg.failedCRN == null || reg.failure == null) continue;
					String id = reg.failedCRN;
		            checked.add(id);
		            String op = (added.contains(id) ? "Add" : "Drop");
		            String error = reg.failure;
		            csv.add(new CSVField[] {
		            		new CSVField(puid),
							new CSVField(student.getName()),
							new CSVField(getCourseNameForCrn(student, id)),
							new CSVField(id),
							new CSVField(op),
							new CSVField("Failed"),
							new CSVField(error),
							new CSVField(getOverride(req, id, appliedOverrides))
		            });
		            iProgress.warn("[" + student.getExternalId() + "] " + id + ": " + error);
		            action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
					action.addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.WARN)
							.setText(id + ": " + error));
				}
			}
			
			boolean ex = false;
			for (String id: crns) {
				if (id == null) continue;
				if (checked.contains(id)) continue;
				String op = (added.contains(id) ? "Add" : "Drop");
				ex = true;
				csv.add(new CSVField[] {
						new CSVField(puid),
						new CSVField(student.getName()),
						new CSVField(getCourseNameForCrn(student, id)),
						new CSVField(id),
						new CSVField(op),
						new CSVField("Exception"),
						new CSVField(response.registrationException)
				});
			}
			
			if (response.registrationException != null && !ex) {
				csv.add(new CSVField[] {
						new CSVField(puid),
						new CSVField(student.getName()),
						new CSVField(null),
						new CSVField(null),
						new CSVField(null),
						new CSVField("Exception"),
						new CSVField(response.registrationException)
				});
			}
			
			if (response.registrationException != null) {
				action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.ERROR)
						.setText(response.registrationException));
			}
			
			if (response.registrationException != null)
				iProgress.warn("[" + student.getExternalId() + "] " + response.registrationException);
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	protected boolean addOverride(Student student, XEInterface.RegisterRequest req, String id, String override, Map<String, Set<String>> overrides) {
		if (req.courseReferenceNumbers == null) return false;
		for (XEInterface.CourseReferenceNumber crn: req.courseReferenceNumbers) {
			if (id.equals(crn.courseReferenceNumber)) {
	            iProgress.debug("[" + student.getExternalId() + "] " + "Adding override " + override + " for " + id);
	            crn.courseOverride = override;
	            Set<String> list = overrides.get(id);
	            if (list == null) { list = new TreeSet<String>(); overrides.put(id, list); }
	            return list.add(override);
			}
		}
		iProgress.warn("[" + student.getExternalId() + "] " + "Failed to add override " + override + " for " + id);
		return false;
	}
	
	protected String getOverride(XEInterface.RegisterRequest req, String id, Map<String, Set<String>> overrides) {
		Set<String> list = overrides.get(id);
		if (list != null) {
			String ret = "";
			for (String override: list)
				ret += (ret.isEmpty() ? "" : ",") + override;
			return ret;
		}
		if (req.courseReferenceNumbers != null)
			for (XEInterface.CourseReferenceNumber crn: req.courseReferenceNumbers)
				if (id.equals(crn.courseReferenceNumber))
					return crn.courseOverride;
		return null;
	}
	
	protected XEInterface.RegisterResponse getSchedule(Student student, ClientResource resource) throws IOException {
		try {
			resource.get(MediaType.APPLICATION_JSON);
		} catch (ResourceException e) {
			handleError(resource, e);
		}
		
		List<XEInterface.RegisterResponse> current = new GsonRepresentation<List<XEInterface.RegisterResponse>>(resource.getResponseEntity(), XEInterface.RegisterResponse.TYPE_LIST).getObject();
		XEInterface.RegisterResponse original = null;
	    if (current != null && !current.isEmpty())
	    	original = current.get(0);
	    
	    if (original == null || !original.validStudent) {
	    	String reason = null;
	    	if (original != null && original.failureReasons != null) {
	    		for (String m: original.failureReasons) {
	    			if ("Holds prevent registration.".equals(m) && iHoldPassword != null && !iHoldPassword.isEmpty())
	    				return getHoldSchedule(student, resource);
	    			if ("Invalid or undefined Enrollment Status or date range invalid.".equals(m) && iRegistrationDate != null && !iRegistrationDate.isEmpty())
	    				return getHoldSchedule(student, resource);
	    			if (m != null) reason = m;
		    	}	
	    	}
	    	if (reason != null) throw new SectioningException(reason);
	    	throw new SectioningException("Failed to check student registration status.");
	    }
	    
	    return original;
	}
	
	protected XEInterface.RegisterResponse getHoldSchedule(Student student, ClientResource resource) throws IOException {
		if (iHoldPassword != null && !iHoldPassword.isEmpty())
			iProgress.debug("[" + student.getExternalId() + "] " + "Using hold password...");
		if (iRegistrationDate != null && !iRegistrationDate.isEmpty())
			iProgress.debug("[" + student.getExternalId() + "] " + "Using registration date...");
		
		XEInterface.RegisterRequest req = new XEInterface.RegisterRequest(resource.getQueryValue("term"), resource.getQueryValue("bannerId"), null, true);
		req.empty();
		if (iHoldPassword != null && !iHoldPassword.isEmpty())
	        req.holdPassword = iHoldPassword;
		if (iRegistrationDate != null && !iRegistrationDate.isEmpty())
	        req.registrationDate = iRegistrationDate;
		try {
			resource.post(new GsonRepresentation<XEInterface.RegisterRequest>(req));
		} catch (ResourceException e) {
			handleError(resource, e);
		}

		XEInterface.RegisterResponse response = new GsonRepresentation<XEInterface.RegisterResponse>(resource.getResponseEntity(), XEInterface.RegisterResponse.class).getObject();
		if (response == null)
			throw new SectioningException("Failed to check student registration status.");
		else if (!response.validStudent) {
			String reason = null;
			if (response.failureReasons != null)
				for (String m: response.failureReasons) {
					if (reason == null) reason = m;
					else reason += "\n" + m;
				}
			if (reason != null) throw new SectioningException(reason);
			throw new SectioningException("Failed to check student registration status.");
		}
	    return response;
	}
	
	protected XEInterface.RegisterResponse postChanges(ClientResource resource, XEInterface.RegisterRequest req) throws IOException {
		if (req.isEmpty()) req.empty();
		try {
	        resource.post(new GsonRepresentation<XEInterface.RegisterRequest>(req));
		} catch (ResourceException e) {
			handleError(resource, e);
		}

		XEInterface.RegisterResponse response = new GsonRepresentation<XEInterface.RegisterResponse>(resource.getResponseEntity(), XEInterface.RegisterResponse.class).getObject();

		if (response == null)
	        throw new SectioningException("Failed to enroll student.");
		else if (!response.validStudent) {
			String reason = null;
			if (response.failureReasons != null)
				for (String m: response.failureReasons) {
					if (reason == null) reason = m;
					else reason += "\n" + m;
				}
			if (reason != null) throw new SectioningException(reason);
			throw new SectioningException("Failed to enroll student.");
		}
		
	    return response;
	}
	
	protected void handleError(ClientResource resource, Exception exception) {
		try {
			XEInterface.ErrorResponse response = new GsonRepresentation<XEInterface.ErrorResponse>(resource.getResponseEntity(), XEInterface.ErrorResponse.class).getObject();
			XEInterface.Error error = response.getError();
			if (error != null && error.message != null) {
				throw new SectioningException(error.message);
			} else if (error != null && error.description != null) {
				throw new SectioningException(error.description);
			} else if (error != null && error.errorMessage != null) {
				throw new SectioningException(error.errorMessage);
			} else {
				throw exception;
			}
		} catch (SectioningException e) {
			throw e;
		} catch (Throwable t) {
			throw new SectioningException(exception.getMessage(), exception);
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
						return course.getConsentType();
				}
			}
		}
		return null;
	}
	
	protected Entity getUser() {
		return Entity.newBuilder().setExternalId(iOwnerId).setType(Entity.EntityType.MANAGER).build();
	}
	
	protected void add(XEInterface.RegisterRequest req, String id, boolean changeStatus) {
	    if (iActionAdd == null)
	        req.add(id, changeStatus);
	    else if (changeStatus) {
	    	if (req.actionsAndOptions == null) req.actionsAndOptions = new ArrayList<XEInterface.RegisterAction>();
	    	req.actionsAndOptions.add(new XEInterface.RegisterAction(iActionAdd, id));
	    } else {
	    	if (req.courseReferenceNumbers == null) req.courseReferenceNumbers = new ArrayList<XEInterface.CourseReferenceNumber>();
	    	req.courseReferenceNumbers.add(new XEInterface.CourseReferenceNumber(id, iActionAdd));
	    }
	}
	
	protected void keep(XEInterface.RegisterRequest req, String id) {
		req.keep(id);
	}
	
	protected void drop(XEInterface.RegisterRequest req, String id) {
		if (iActionDrop == null)
			req.drop(id, null);
		else {
			if (req.actionsAndOptions == null) req.actionsAndOptions = new ArrayList<XEInterface.RegisterAction>();
			req.actionsAndOptions.add(new XEInterface.RegisterAction(iActionDrop, id));
		}
	}
	
	protected boolean removeAction(XEInterface.RegisterRequest req, String id) {
		if (req.actionsAndOptions == null) return false;
		for (Iterator<XEInterface.RegisterAction> i = req.actionsAndOptions.iterator(); i.hasNext(); ) {
			XEInterface.RegisterAction action = i.next();
			if (id.equals(action.courseReferenceNumber)) { i.remove(); return true; }
		}
		return false;
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
	
    protected void checkTermination() {
    	if (getTerminationCondition() != null && !getTerminationCondition().canContinue(getSolution()))
    		throw new RuntimeException("The save was interrupted.");
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
			setName("XESaver-" + (1 + index));
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
					saveStudent(student);
			}
			iProgress.debug(getName() + " has finished.");
		}
	}
}
