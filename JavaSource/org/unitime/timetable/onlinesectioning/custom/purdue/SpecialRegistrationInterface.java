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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.util.List;

import org.joda.time.DateTime;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationInterface {
	
	public static class SpecialRegistrationRequest {
		public String requestId;
		public String studentId;
		public String term;
		public String campus;
		public String status;
		public List<Change> changes;
		public DateTime dateCreated;
	}
	
	public static enum RequestStatus {
		 mayEdit, mayNotEdit, maySubmit, newRequest;
	}
	
	public static class SpecialRegistrationResponse {
		public SpecialRegistrationRequest data;
		public ResponseStatus status;
		public String message;
	}

	public static class SpecialRegistrationResponseList {
		public List<SpecialRegistrationRequest> data;
		public String status;
		public String message;
	}

	public static enum ResponseStatus {
		success, failure;
	}
	
	public static class Change {
		public String subject;
		public String courseNbr;
		public String crn;
		public ChangeOperation operation;
		public List<ChangeError> errors;
	}
	
	public static enum ChangeOperation {
		ADD, DROP;
	}
	
	public static class ChangeError {
		String code;
		String message;
	}

}
