package org.unitime.timetable.util;

import java.util.Date;

import org.hibernate.Transaction;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

public class PopulateProjectedDemandSnapshotData {

	public PopulateProjectedDemandSnapshotData() {
		
	}
	
	public Date populateProjectedDemandDataFor(Session acadSession) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		Transaction trans = null;
		Date snapshotDate = null;
		try {
			trans = hibSession.beginTransaction();
			snapshotDate = populateProjectedDemandDataFor( acadSession, hibSession);
			trans.commit();
			
		} catch (Exception e) {
			trans.rollback();
		}
		
		return(snapshotDate);
	}

	public Date populateProjectedDemandDataFor(Session acadSession, org.hibernate.Session hibSession) {
			Date snapshotDate = new Date();
			updateCourseOfferingData(acadSession, snapshotDate, hibSession);
			updateInstructionalOfferingData(acadSession, snapshotDate, hibSession);
			updateClassData(acadSession, snapshotDate, hibSession);
			updateCurriculumProjectionRuleData(acadSession, snapshotDate, hibSession);
			updateCurriculumClassificationData(acadSession, snapshotDate, hibSession);
			updateCurriculumCourseData(acadSession, snapshotDate, hibSession);			
			return(snapshotDate);
	}

	
	private void updateCourseOfferingData(Session acadSession, Date snapshotDate, org.hibernate.Session hibSession){
		StringBuilder courseOfferingUpdateSb = new StringBuilder();
		courseOfferingUpdateSb.append("update CourseOffering as co")
		    .append(" set co.snapshotProjectedDemand = co.projectedDemand,")
		    .append(" co.snapshotProjectedDemandDate = :snapshotDate")
		    .append(" where co.instructionalOffering.uniqueId in ")
		    .append(" ( select io.uniqueId from InstructionalOffering io where io.session.uniqueId = :sessId ) " );
		hibSession
			.createQuery(courseOfferingUpdateSb.toString())
			.setTimestamp("snapshotDate", snapshotDate)
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.executeUpdate();
	}

	private void updateInstructionalOfferingData(Session acadSession, Date snapshotDate,
			org.hibernate.Session hibSession) {
		StringBuilder instructionalOfferingUpdateSb = new StringBuilder();
		instructionalOfferingUpdateSb.append("update InstructionalOffering as io")
		    .append(" set io.snapshotLimit = ( select sum(ioc.limit) from InstrOfferingConfig ioc where ioc.instructionalOffering.uniqueId = io.uniqueId ),")
		    .append(" io.snapshotLimitDate = :snapshotDate")
		    .append(" where io.session.uniqueId = :sessId ");
		hibSession
			.createQuery(instructionalOfferingUpdateSb.toString())
			.setTimestamp("snapshotDate", snapshotDate)
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.executeUpdate();
	}
	
	private void updateClassData(Session acadSession, Date snapshotDate,
			org.hibernate.Session hibSession) {
		StringBuilder classUpdateSb = new StringBuilder();
		classUpdateSb.append("update Class_ as c")
		    .append(" set c.snapshotLimit = c.expectedCapacity,")
		    .append(" c.snapshotLimitDate = :snapshotDate")
		    .append(" where c.schedulingSubpart.uniqueId in ")
	    	.append(" ( select ss.uniqueId from SchedulingSubpart as ss where ")
	    	.append("  ss.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessId ) " );
		hibSession
			.createQuery(classUpdateSb.toString())
			.setTimestamp("snapshotDate", snapshotDate)
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.executeUpdate();
	
	}
	
	private void updateCurriculumProjectionRuleData(Session acadSession, Date snapshotDate, org.hibernate.Session hibSession){

		StringBuilder curriculumProjectionRuleUpdateSb = new StringBuilder();
		curriculumProjectionRuleUpdateSb.append("update CurriculumProjectionRule as cpr")
		    .append(" set cpr.snapshotProjection = cpr.projection,")
		    .append(" cpr.snapshotProjectedDate = :snapshotDate")
		    .append(" where cpr.academicArea.uniqueId in ")
		    .append(" ( select aa.uniqueId from AcademicArea aa where aa.session.uniqueId = :sessId ) " );
		hibSession
			.createQuery(curriculumProjectionRuleUpdateSb.toString())
			.setTimestamp("snapshotDate", snapshotDate)
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.executeUpdate();

	}

	private void updateCurriculumClassificationData(Session acadSession, Date snapshotDate, org.hibernate.Session hibSession){

		StringBuilder curriculumClassificationUpdateSb = new StringBuilder();
		curriculumClassificationUpdateSb.append("update CurriculumClassification as cc")
		    .append(" set cc.snapshotNrStudents = cc.nrStudents,")
		    .append(" cc.snapshotNrStudentsDate = :snapshotDate")
		    .append(" where cc.curriculum.uniqueId in ")
		    .append(" ( select c.uniqueId from Curriculum c where c.academicArea.session.uniqueId = :sessId ) " );
		hibSession
			.createQuery(curriculumClassificationUpdateSb.toString())
			.setTimestamp("snapshotDate", snapshotDate)
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.executeUpdate();

	}

	private void updateCurriculumCourseData(Session acadSession, Date snapshotDate, org.hibernate.Session hibSession){

		StringBuilder curriculumClassificationUpdateSb = new StringBuilder();
		curriculumClassificationUpdateSb.append("update CurriculumCourse as ccrs")
		    .append(" set ccrs.snapshotPercShare = ccrs.percShare,")
		    .append(" ccrs.snapshotPercShareDate = :snapshotDate")
		    .append(" where ccrs.classification.uniqueId in ")
		    .append(" ( select cc.uniqueId from CurriculumClassification cc where cc.curriculum.academicArea.session.uniqueId = :sessId ) " );
		hibSession
			.createQuery(curriculumClassificationUpdateSb.toString())
			.setTimestamp("snapshotDate", snapshotDate)
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.executeUpdate();

	}


}
