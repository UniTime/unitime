package org.unitime.timetable.model;

import java.util.Hashtable;
import java.util.List;

import org.unitime.timetable.model.base.BaseCurriculumProjectionRule;
import org.unitime.timetable.model.dao.CurriculumProjectionRuleDAO;



public class CurriculumProjectionRule extends BaseCurriculumProjectionRule {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculumProjectionRule () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculumProjectionRule (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CurriculumProjectionRule (
			java.lang.Long uniqueId,
			org.unitime.timetable.model.AcademicArea academicArea,
			org.unitime.timetable.model.AcademicClassification academicClassification,
			java.lang.Float projection) {

		super (
			uniqueId,
			academicArea,
			academicClassification,
			projection);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static List<CurriculumProjectionRule> findAll(Long sessionId) {
	    return CurriculumProjectionRuleDAO.getInstance().getSession()
	        .createQuery("select r from CurriculumProjectionRule r where r.academicArea.session.uniqueId=:sessionId")
	        .setLong("sessionId", sessionId)
	        .setCacheable(true).list();
	}

    public static List<CurriculumProjectionRule> findByAcademicArea(Long acadAreaId) {
        return CurriculumProjectionRuleDAO.getInstance().getSession()
            .createQuery("select r from CurriculumProjectionRule r where r.academicArea.uniqueId=:acadAreaId")
            .setLong("acadAreaId", acadAreaId)
            .setCacheable(true).list();
    }
    
    public static Hashtable<String, Float> getProjections(Long acadAreaId, Long acadClasfId) {
    	Hashtable<String, Float> ret = new Hashtable<String, Float>();
    	for (CurriculumProjectionRule r: (List<CurriculumProjectionRule>)CurriculumProjectionRuleDAO.getInstance().getSession()
    			.createQuery("select r from CurriculumProjectionRule r where r.academicArea.uniqueId=:acadAreaId and r.academicClassification.uniqueId=:acadClasfId")
    			.setLong("acadAreaId", acadAreaId)
    			.setLong("acadClasfId", acadClasfId)
    			.setCacheable(true).list()) {
    		ret.put(r.getMajor() == null ? "" : r.getMajor().getCode(), r.getProjection());
    	}
    	return ret;
    }

}