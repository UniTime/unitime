/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import java.util.List;

import org.hibernate.criterion.Order;
import org.unitime.timetable.model.base.BaseOfferingConsentType;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;




/**
 * @author Tomas Muller
 */
public class OfferingConsentType extends BaseOfferingConsentType {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public OfferingConsentType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public OfferingConsentType (Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    /** Request attribute name for available consent typess **/
    public static String CONSENT_TYPE_ATTR_NAME = "consentTypeList";  
    
	/**
	 * Retrieves all consent types in the database
	 * ordered by column label
	 * @return List of ConsentType objects
	 */
    public static List<OfferingConsentType> getConsentTypeList() {
    	return OfferingConsentTypeDAO.getInstance().findAll(Order.asc("label"));
    }
    
	public static OfferingConsentType getOfferingConsentTypeForReference(String referenceString) {
		if (referenceString == null || referenceString.isEmpty()) return null;
		return (OfferingConsentType)OfferingConsentTypeDAO.getInstance().getSession().createQuery(
				"from OfferingConsentType where reference = :reference")
				.setString("reference", referenceString).setMaxResults(1).setCacheable(true).uniqueResult();
	}
	
}
