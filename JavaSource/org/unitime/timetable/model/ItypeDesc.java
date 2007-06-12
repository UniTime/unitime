/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseItypeDesc;
import org.unitime.timetable.model.dao.ItypeDescDAO;




public class ItypeDesc extends BaseItypeDesc {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ItypeDesc () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ItypeDesc (java.lang.Integer itype) {
		super(itype);
	}

/*[CONSTRUCTOR MARKER END]*/

    // Static list of Itypes
    private static HashMap itypes = null;
    private static Vector itypesList = null;
    
    /** Request attribute name for available itypes **/
    public static String ITYPE_ATTR_NAME = "itypesList";
    
    /**
     * Loads Itypes from SIQ
     */
    public static synchronized void load(boolean reload) throws Exception {
        
        if(itypes!=null && !reload)
            return;
        
        itypesList = new Vector();
        itypes = new HashMap();
	    ItypeDescDAO itypeDao = new ItypeDescDAO();
	    ItypeDesc itype = null;
	    
	    try {
		    Iterator it = itypeDao.getQuery("FROM ItypeDesc where basic=1").list().iterator();
	        while (it.hasNext()) {
	        	itype = (ItypeDesc) it.next();
	            Integer code = itype.getItype();
	            
	            itypes.put(code, itype);
	            itypesList.addElement(itype);
	        }
	        Debug.debug("Loaded " + itypes.size() + " itypes ...");	        
	    }
	    catch (Exception e) {	  
	        Debug.error(e);
		    throw (e);
	    }
    }

    /**
     * @return Returns the itypes.
     */
    public static HashMap getItypes() throws Exception{
        load(false);
        return itypes;
    }

    /**
     * Retrieves list of itypes
	 * @param refresh true - refreshes the list from database
     * @return Collection of Itype Objects
     * @throws Exception
     */
    public static Vector getItypesList(boolean refresh) throws Exception{
        load(refresh);
        return itypesList;
    }

}