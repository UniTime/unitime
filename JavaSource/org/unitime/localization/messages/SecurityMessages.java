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
	
	@DefaultMessage("Access denied for {0}: user is no instructor.")
	String noDepartmentalInstructor(String right);
	
	@DefaultMessage("Access denied for {0}: academic session check failed for {1}.")
	String sessionCheckFailed(String right, String domainObject);

	@DefaultMessage("Access denied for {0}: department check failed for {1}.")
	String departmentCheckFailed(String right, String domainObject);

}
