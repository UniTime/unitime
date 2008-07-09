package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseSponsoringOrganization;



public class SponsoringOrganization extends BaseSponsoringOrganization {
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


}