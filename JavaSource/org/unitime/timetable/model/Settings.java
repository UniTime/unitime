/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseSettings;
import org.unitime.timetable.model.dao.SettingsDAO;
import org.unitime.timetable.model.dao._RootDAO;


/**
 * @author Tomas Muller
 */
public class Settings extends BaseSettings {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Settings () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Settings (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    /**
     * Retrieves the user setting id /value if exists
     * @param currRole Current Role
     * @param uSettings User Settings Object
     * @param keyId Setting UniqueId
     * @param defaultValue Default Value
     * @return Array of Setting id /value if found, otherwise returns -1 / default value
     */
    public static String[] getSettingValue(String currRole, Set uSettings, Long keyId, String defaultValue) {
        String[] data = new String[2];
        data[0] = "-1";
        data[1] = defaultValue;
        if (uSettings==null) return data;

        org.hibernate.Session hibSession = null;
        
		try {
		    _RootDAO rootDao = new _RootDAO();
			hibSession = rootDao.getSession();
			Iterator i = uSettings.iterator();

	        while (i.hasNext()) {
	            ManagerSettings mgrSettings = (ManagerSettings) i.next();
				hibSession.update(mgrSettings);
				
	            if(mgrSettings.getKey().getUniqueId().intValue()==keyId.intValue()) {
	                data[0] = mgrSettings.getUniqueId().toString();
	                data[1] = mgrSettings.getValue();
	                break;
	            }
	        }
		}
		catch (Exception ex) {
		    Debug.error(ex);
		}
		finally {
			//if (hibSession!=null && hibSession.isOpen()) hibSession.close();
		}
		
        return data;
    }

    /**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	public static Settings getSetting(String key) {
	    Settings settings = null;
		org.hibernate.Session hibSession = null;
	    
        try {
            SettingsDAO sDao = new SettingsDAO();
			hibSession = sDao.getSession();
            
			List settingsList = hibSession.createCriteria(Settings.class)
			.add(Restrictions.eq("key", key))
			.setCacheable(true)
			.list();
			
			if(settingsList.size()!=0) 
			    settings = (Settings) settingsList.get(0);
			    
	    }
	    catch (Exception e) {
			Debug.error(e);
			settings = null;
	    }
	    finally {
	    	//if (hibSession!=null && hibSession.isOpen()) hibSession.close();
	    }
	    
	    return settings;
	}
	
}
