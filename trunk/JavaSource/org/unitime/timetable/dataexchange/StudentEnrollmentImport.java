package org.unitime.timetable.dataexchange;

import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.test.UpdateExamConflicts;

public class StudentEnrollmentImport extends BaseImport {
	TimetableManager manager = null;
	HashMap<String, Class_> classes = new HashMap<String, Class_>();
	HashMap<String, CourseOffering> controllingCourses = new HashMap<String, CourseOffering>();
	boolean trimLeadingZerosFromExternalId = false;
	public StudentEnrollmentImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		String rootElementName = "studentEnrollments";
		String trimLeadingZeros =
	        ApplicationProperties.getProperty("tmtbl.data.exchange.trim.externalId","false");
		if (trimLeadingZeros.equals("true")){
			trimLeadingZerosFromExternalId = true;
		}

        if (!rootElement.getName().equalsIgnoreCase(rootElementName)) {
        	throw new Exception("Given XML file is not a Student Enrollments load file.");
        }
        
        Session session = null;
		try {
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");
	        String created = rootElement.attributeValue("created");
			beginTransaction();
	        session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }
	        loadClasses(session.getUniqueId());
	        info("classes loaded");
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
	            if (trimLeadingZerosFromExternalId){
	            	externalId = (new Integer(externalId)).toString();
	            }
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
        if (session!=null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.finalExam.updateConflicts","false"))) {
            try {
                beginTransaction();
                new UpdateExamConflicts(this).update(session.getUniqueId(), Exam.sExamTypeFinal, getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }
        if (session!=null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.midtermExam.updateConflicts","false"))) {
            try {
                beginTransaction();
                new UpdateExamConflicts(this).update(session.getUniqueId(), Exam.sExamTypeMidterm, getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }
	}
	
	private void elementClass(Element studentElement, Student student) throws Exception{
		String elementName = "class";
		
        for ( Iterator it = studentElement.elementIterator(); it.hasNext(); ) {
            Element classElement = (Element) it.next();
            String externalId = getRequiredStringAttribute(classElement, "externalId", elementName);
            Class_ clazz = classes.get(externalId);
            if (clazz != null){
				StudentClassEnrollment sce = new StudentClassEnrollment();
		    	sce.setStudent(student);
		    	sce.setClazz(clazz);
		    	sce.setCourseOffering(controllingCourses.get(externalId));
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
	
	private void loadClasses(Long sessionId) throws Exception {
 		for (Iterator<?> it = Class_.findAll(sessionId).iterator(); it.hasNext();) {
			Class_ c = (Class_) it.next();
			if (c.getExternalUniqueId() != null){
				classes.put(c.getExternalUniqueId(), c);
				controllingCourses.put(c.getExternalUniqueId(), c.getSchedulingSubpart().getControllingCourseOffering());
			} 
		}
	}

}
