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
package org.unitime.timetable.interfaces;

import java.util.Map;

/**
 * Interface to generate external links
 * 
 * @author Heston Fernandes
 */
public interface ExternalLinkLookup {
	
	/** Attribute for the link label */
	public final String LINK_LABEL = "label";

	/** Attribute for the link location */
	public final String LINK_LOCATION = "href";
	
	/**
	 * Generate the link based on the attributes of the object
	 * @param obj object whose attributes may be used in constructing the link
	 * @return Map object containing two elements LINK_LABEL and LINK LOCATION
	 */
	public Map getLink(Object obj) throws Exception;
	
	/**
	 * Sets the error message (if any)
	 * @return
	 */
	public String getErrorMessage();

}
