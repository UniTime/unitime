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
package org.unitime.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.ExternalUidLookup;


/**
 * Authenticates a user through I2A2
 * Contains all attributes of the logged in user.
 * 
 * @author Heston Fernandes 
 */

public class User {

	private String iId = null;
    private String iLogin = null;
    private String iName = null;
	private String iRole = null;
    private boolean iIsAdmin = false;

	private Vector roles = null;
    private Vector iDepartments = new Vector();

    private Properties otherAttributes = new Properties();

    public User() { }


	/**
	 * Set the ID of the user
	 * @param name PUID
	 */
	public void setId(String id) {
		iId = id;
	}

	/**
	 * Retrieve ID of current user
	 * @return id
	 */
	public String getId() {
		return iId;
	}

    /**
     * Set the full name of the user
     * @param name Full Name
     */
    public void setName(String name) {
        iName = name;
    }

	/**
	 * Retrieve full name of current user
	 * @return String {first} [{middle}] {last}
	 */
    public String getName() {
        return iName;
    }

	/**
	 * Set the username of the logged in user
	 * @param login Login id
	 */
    public void setLogin(String login) {
        iLogin = login.toLowerCase();
    }

	/**
	 * Retrieve login id of the current user
	 * @return Login Id String
	 */
    public String getLogin() {
        return iLogin;
    }

	/**
	 * Set the role assigned to the user
	 * Typically "Administrator" or "Schedule Deputy"
	 * @param name Role Name
	 */
	public void setRole(String role) {
		iRole = role;
	}

	/**
	 * Retrieve role of current user
	 * @return Role Name
	 */
	public String getRole() {
		return iRole;
	}

	/**
	 * Checks if current user has the specified role
	 * @param role String
	 * @return true if the user has the specified role
	 */
	public boolean hasRole(String role) {

		return getRole().equalsIgnoreCase(role);
	}

	/**
	 * Checks if current user is an administrator
	 * @return true if admin, false otherwise
	 */
    public boolean isAdmin() {
        return iIsAdmin;
    }

	/**
	 * Set the current user as an administrator
	 * @param isAdmin true indicates current user is admin, false otherwise
	 */
    public void setAdmin(boolean isAdmin) {
        iIsAdmin = isAdmin;
    }

	/**
	 * Retrieves all departments that a user belongs to
	 * @return Vector of department numbers
	 */
    public Vector getDepartments() {
        return iDepartments;
    }

	/**
	 * Set the user department(s)
	 * @param _depts Vector of department numbers
	 */
	public void setDepartments( Vector _depts) {
		iDepartments = _depts;
	}

	/**
	 * @return Current Role
	 */
	public String getCurrentRole() {
	    return iRole;
	}

	/**
     * Gets all roles that a user may possess
     * @return Vector of Role Names
     * @see ADMIN_ROLE, etc
	 */
    public Vector getRoles() {
        return roles;
    }

    /**
     * Sets all roles that a user may possess
     * @param roles Vector of Role Names
     * @see ADMIN_ROLE, etc
     */
    public void setRoles(Vector roles) {
        this.roles = roles;
    }

    /**
     * @return Returns other attributes in a Properties object.
     */
    public Properties getOtherAttributes() {
        return otherAttributes;
    }

    /**
     * Enables other application to store other user attributes
     * @param otherAttributes Store other attributes via a properties object
     */
    public void setOtherAttributes(Properties otherAttributes) {
        this.otherAttributes = otherAttributes;
    }

    /**
     * Looks up other attribute name
     * @param attributeName Attribute name
     * @return Object representing the attribute value. Null if not found
     */
    public Object getAttribute(String attributeName) {
        return otherAttributes.get(attributeName);
    }

    /**
     * Store additional user attribute
     * @param attributeName Attribute name
     * @param attributeName Attribute value
     */
    public void setAttribute(String attributeName, Object attributeValue) {
        otherAttributes.put(attributeName, attributeValue);
    }
    
    public static boolean canIdentify() {
        return "true".equals(ApplicationProperties.getProperty("tmtbl.instructor.externalId.lookup.enabled","false"));
    }
	
    public static User identify(String externalId) {
        if (externalId==null || externalId.trim().length()==0) return null;
        if (canIdentify()) {
            try {
                HashMap attributes = new HashMap();
                attributes.put(ExternalUidLookup.SEARCH_ID, externalId);
                
                String className = ApplicationProperties.getProperty("tmtbl.manager.externalId.lookup.class");
                ExternalUidLookup lookup = (ExternalUidLookup)(Class.forName(className).newInstance());
                Map results = lookup.doLookup(attributes);
                if (results==null) return null;
                User user = new User();
                user.setId((String)results.get(ExternalUidLookup.EXTERNAL_ID));
                user.setLogin((String)results.get(ExternalUidLookup.USERNAME));
                user.setName(
                        (String)results.get(ExternalUidLookup.FIRST_NAME)+" "+
                        (String)results.get(ExternalUidLookup.MIDDLE_NAME)+" "+
                        (String)results.get(ExternalUidLookup.LAST_NAME));
                return user;
            } catch (Exception e) {
                Debug.error(e);
            }
        }
        return null;
    }
}
