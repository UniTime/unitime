package org.unitime.timetable.dataexchange;

import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.dom4j.Element;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimetableManager;

public class StudentEnrollmentImport extends BaseImport {
	TimetableManager manager = null;
	HashMap<String, Class_> classes = new HashMap<String, Class_>();
	public void loadXml(Element rootElement, HttpServletRequest request) throws Exception {
		HttpSession httpSession = request.getSession();
        String userId = (String)httpSession.getAttribute("authUserExtId");
        User user = Web.getUser(httpSession);
        if (userId!=null) {
        	manager = TimetableManager.findByExternalId(userId);
        }
        if (manager==null && user!=null) {
            Debug.warning("No authenticated user defined, using "+user.getName());
        	manager = TimetableManager.getManager(user);
        }
        
		loadXml(rootElement);
	}
	
	
	public StudentEnrollmentImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		String rootElementName = "studentEnrollments";
        if (!rootElement.getName().equalsIgnoreCase(rootElementName)) {
        	throw new Exception("Given XML file is not a Student Enrollments load file.");
        }
		try {
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");
	        String created = rootElement.attributeValue("created");
	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }
	        loadClasses(session.getUniqueId());
	        info("classes loaded");
			beginTransaction();
	        if (manager == null){
	        	manager = findDefaultManager();
	        }
	        if (created != null) {
				ChangeLog.addChange(getHibSession(), manager, session, session, created, ChangeLog.Source.DATA_IMPORT_STUDENT_ENROLLMENTS, ChangeLog.Operation.UPDATE, null, null);
	        }
         
            /* 
             * If some records of a table related to students need to be explicitly deleted, 
             * hibernate can also be used to delete them. For instance, the following query 
             * deletes all student class enrollments for given academic session:
             *   
             * delete StudentClassEnrollment sce where sce.student.uniqueId in
             *      (select s.uniqueId from Student s where s.session.uniqueId=:sessionId)
             */
            
            getHibSession().createQuery("delete StudentClassEnrollment sce where sce.student.uniqueId in (select s.uniqueId from Student s where s.session.uniqueId=:sessionId)").setLong("sessionId", session.getUniqueId().longValue()).executeUpdate();
            
            flush(true);
            String elementName = "student";
 	        for ( Iterator it = rootElement.elementIterator(); it.hasNext(); ) {
	            Element studentElement = (Element) it.next();
	            String externalId = getRequiredStringAttribute(studentElement, "externalId", elementName);
	            
            	Student student = fetchStudent(externalId, session.getUniqueId());
            	if (student == null){
            		student = new Student();
	                student.setSession(session);
	                String firstName = getOptionalStringAttribute(studentElement, "firstName");
		            student.setFirstName(firstName==null?"Name":firstName);
		            student.setMiddleName(getOptionalStringAttribute(studentElement, "middleName"));
		            String lastName = getOptionalStringAttribute(studentElement, "lastName");
		            student.setLastName(lastName==null?"Unknown":lastName);
		            student.setEmail(getOptionalStringAttribute(studentElement, "email"));
		            student.setExternalUniqueId(externalId);
		            student.setFreeTimeCategory(new Integer(0));
		            student.setSchedulePreference(new Integer(0));
            	}
            	
            	elementClass(studentElement, student);
                getHibSession().save(student);

	            flushIfNeeded(true);
	        }
            
            commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
	}
	
	private void elementClass(Element studentElement, Student student) throws Exception{
		String elementName = "class";
		
        for ( Iterator it = studentElement.elementIterator(); it.hasNext(); ) {
            Element classElement = (Element) it.next();
            String externalId = getRequiredStringAttribute(classElement, "externalId", elementName);
            Class_ clazz = fetchClassForExternalUniqueId(externalId, student.getSession().getUniqueId());
            if (clazz != null){
				StudentClassEnrollment sce = new StudentClassEnrollment();
		    	sce.setStudent(student);
		    	sce.setClazz(clazz);
		    	sce.setCourseOffering(clazz.getSchedulingSubpart().getControllingCourseOffering());
		    	sce.setTimestamp(new java.util.Date());
		    	student.addToclassEnrollments(sce);
            }
        }
	}

	Student fetchStudent(String externalId, Long sessionId) {
		return (Student) this.
		getHibSession().
		createQuery("select distinct a from Student as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
		setLong("sessionId", sessionId.longValue()).
		setString("externalId", externalId).
		setCacheable(true).
		uniqueResult();
	}
	
	private Class_ fetchClassForExternalUniqueId(String externalUniqueId, Long sessionId){
		if (classes.containsKey(externalUniqueId)){
			return((Class_) classes.get(externalUniqueId));
		} else {
			Class_ c = (Class_) this.
			getHibSession().
			createQuery("select distinct c from Class_ as c where c.externalUniqueId=:externalUniqueId and c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId" ).
			setString("externalUniqueId", externalUniqueId).
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			uniqueResult();
			classes.put(externalUniqueId, c);
			return(c);
		}
	}
	private void loadClasses(Long sessionId) throws Exception {
 		for (Iterator<?> it = Class_.findAll(sessionId).iterator(); it.hasNext();) {
			Class_ c = (Class_) it.next();
			if (c.getExternalUniqueId() != null){
				classes.put(c.getExternalUniqueId(), c);
			} 
		}
	}

}
