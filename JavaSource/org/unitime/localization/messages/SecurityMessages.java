/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.localization.messages;

/**
 * @author Tomas Muller
 */
public interface SecurityMessages extends Messages {
	@DefaultMessage("Access denied.")
	String accessDenied();

	@DefaultMessage("Access denied for {0}: user is not authenticated.")
	String noAuthentication(String right);
	
	@DefaultMessage("Access denied for {0}: user has no active role / academic session selected.")
	String noAuthority(String right);
	
	@DefaultMessage("Access denied for {0}: user has no matching role / academic session.")
	String noMatchingAuthority(String right);
	
	@DefaultMessage("Access denied: unknown or no permission provided.")
	String noRight();

	@DefaultMessage("Access denied for {0}: required permission is not granted.")
	String missingRight(String right);

	@DefaultMessage("Access denied for {0}: no {1} provided.")
	String noDomainObject(String right, String type);
	
	@DefaultMessage("Access denied for {0}: wrong domain object provided (provided: {1}, expected:{2}).")
	String wrongDomainObject(String right, String provided, String expected);
	
	@DefaultMessage("Access denied for {0}: permission check failed for {1}.")
	String permissionCheckFailed(String right, String domainObject);

	@DefaultMessage("Access denied for {0}: permission check failed for {1} ({2}).")
	String permissionCheckFailedException(String right, String domainObject, String exception);
	
	@DefaultMessage("Access denied for {0}: {1} not found.")
	String domainObjectNotExists(String right, String type);
	
	@DefaultMessage("Access denied for {0}: user has no department.")
	String noDepartment(String right);

	@DefaultMessage("Access denied for {0}: user has no subject area.")
	String noSubject(String right);
	
	@DefaultMessage("Access denied for {0}: user has no solver group.")
	String noSolverGroup(String right);
	
	@DefaultMessage("Access denied for {0}: academic session check failed for {1}.")
	String sessionCheckFailed(String right, String domainObject);

	@DefaultMessage("Access denied for {0}: department check failed for {1}.")
	String departmentCheckFailed(String right, String domainObject);

}
