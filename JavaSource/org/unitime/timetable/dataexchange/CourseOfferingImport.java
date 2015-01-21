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
package org.unitime.timetable.dataexchange;

import org.dom4j.Element;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;

/**
 * @author Stephanie Schluttenhofer
 *
 */
public class CourseOfferingImport extends BaseCourseOfferingImport {

	/**
	 * 
	 */
	public CourseOfferingImport() {
		super();
		rootElementName = "offerings";
	}

	@Override
	protected boolean handleCustomCourseChildElements(CourseOffering courseOffering,
			Element courseOfferingElement) {
		// Core UniTime does not have any child elements for the course offering element
		return(false);
	}

	@Override
	protected boolean handleCustomClassChildElements(Element classElement,
			InstrOfferingConfig ioc, Class_ clazz) {
		// Core UniTime does not have any child elements for the class element
		return false;
	}

	@Override
	protected void postLoadAction() {
		// Core UniTime does not implement the post load action
		
	}

	@Override
	protected void preLoadAction() {
		// Core UniTime does not implement the pre load action
	}

	@Override
	protected boolean handleCustomInstrOffrConfigChildElements(
			InstrOfferingConfig instrOfferingConfig,
			Element instrOfferingConfigElement) throws Exception {
		// Core UniTime does not have any child elements for the instructional offering config element
		return false;
	}

}
