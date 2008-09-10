package org.unitime.timetable.model;

import java.util.List;

import org.unitime.timetable.model.base.BaseCurricula;
import org.unitime.timetable.model.dao.CurriculaDAO;



public class Curricula extends BaseCurricula {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Curricula () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Curricula (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public Curricula (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Department department,
		java.lang.String abbv,
		java.lang.String name) {

		super (
			uniqueId,
			department,
			abbv,
			name);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static List<Curricula> findAll(Long sessionId) {
	    return CurriculaDAO.getInstance().getSession()
	        .createQuery("select c from Curricula c where c.department.session.uniqueId=:sessionId")
	        .setLong("sessionId", sessionId)
	        .setCacheable(true).list();
	}

    public static List<Curricula> findByDepartment(Long deptId) {
        return CurriculaDAO.getInstance().getSession()
            .createQuery("select c from Curricula c where c.department.uniqueId=:deptId")
            .setLong("deptId", deptId)
            .setCacheable(true).list();
    }
}