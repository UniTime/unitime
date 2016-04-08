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
package org.unitime.timetable.form;

/**
 * @author Tomas Muller
 */
public interface InstructionalOfferingListFormInterface {
	public Boolean getDivSec();
	public Boolean getDemand();
	public Boolean getProjectedDemand();
	public Boolean getMinPerWk();
	public Boolean getLimit();
	public Boolean getRoomLimit();
	public Boolean getManager();
	public Boolean getDatePattern();
	public Boolean getTimePattern();
	public Boolean getPreferences();
	public Boolean getInstructor();
	public Boolean getTimetable();
	public Boolean getCredit();
	public Boolean getSubpartCredit();
	public Boolean getSchedulePrintNote();
	public Boolean getNote();
	public Boolean getConsent();
	public Boolean getTitle();
	public Boolean getExams();
	public Boolean getInstructorAssignment();
	public String getSortBy();
}
