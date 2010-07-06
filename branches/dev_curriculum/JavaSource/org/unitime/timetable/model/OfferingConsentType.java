/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.criterion.Order;
import org.unitime.timetable.model.base.BaseOfferingConsentType;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;




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
    
    /** Consent Type List **/
    private static Vector consentTypeList = null;
    
	/**
	 * Retrieves all consent types in the database
	 * ordered by column label
	 * @param refresh true - refreshes the list from database
	 * @return Vector of ConsentType objects
	 */
    public static synchronized Vector getConsentTypeList(boolean refresh) {
        if(consentTypeList!=null && !refresh)
            return consentTypeList;
        
        OfferingConsentTypeDAO cdao = new OfferingConsentTypeDAO();
        Vector orderList = new Vector();
        orderList.addElement(Order.asc("label"));

        List l = cdao.findAll(orderList);
        consentTypeList = new Vector(l);
        return consentTypeList;
    }
	public static OfferingConsentType getOfferingConsentTypeForReference(String referenceString){
		if (referenceString == null || referenceString.length() == 0){
			return(null);
		}
		OfferingConsentType oct = null;
		for(Iterator it = getConsentTypeList(false).iterator(); it.hasNext(); ){
			oct = (OfferingConsentType) it.next();
			if (referenceString.equals(oct.getReference())){
				return(oct);
			}
		}
		return(null);
	}
	
	public String getAbbv() {
		//FIXME: put abbreviation into DB
		if (getLabel()==null) return null;
		return getLabel().replaceAll("Consent of ", "");
	}
	

}
