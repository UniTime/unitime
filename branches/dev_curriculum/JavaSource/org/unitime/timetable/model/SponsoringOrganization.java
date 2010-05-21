package org.unitime.timetable.model;

import java.util.List;

import org.unitime.timetable.model.base.BaseSponsoringOrganization;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;



public class SponsoringOrganization extends BaseSponsoringOrganization implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SponsoringOrganization () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SponsoringOrganization (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public SponsoringOrganization (
		java.lang.Long uniqueId,
		java.lang.String name) {

		super (
			uniqueId,
			name);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int compareTo(Object o) {
		SponsoringOrganization so1 = (SponsoringOrganization) this;
		SponsoringOrganization so2 = (SponsoringOrganization) o;
    	int cmp = so1.getName().compareTo(so2.getName());
    	if (cmp!=0) return cmp;
    	else return so1.getUniqueId().compareTo(so2.getUniqueId());
	}

    public static List findAll() {
        return new SponsoringOrganizationDAO().getSession().createQuery(
                "select so from SponsoringOrganization so order by so.name"
                ).setCacheable(true).list();
    }
    
}