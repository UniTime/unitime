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
package org.unitime.timetable.gwt.client.exams;

import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingsRequest;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

public class ExamsInterface {
	public static class ExamsFilterRequest implements GwtRpcRequest<ExamsFilterResponse> {}
	
	public static class ExamsFilterResponse extends ClassesFilterResponse {
		private static final long serialVersionUID = 1L;
		private boolean iCanAdd = false;
		
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
	}

	public static class ExamsRequest extends OfferingsRequest {}
	
	public static class ExamDetailRequest implements GwtRpcRequest<ExamDetailReponse> {
		private Long iExamId;
		private Action iAction;

		public static enum Action {
			DELETE,
		}
		
		public void setExamId(Long examId) { iExamId = examId; }
		public Long getExamId() { return iExamId; }
		public Action getAction() { return iAction; }
		public void setAction(Action action) { iAction = action; }
	}
	
	public static class ExamDetailReponse implements GwtRpcResponse {
		private Long iExamId, iPreviousId, iNextId;
		private String iExamName;
		private String iBackUrl, iBackTitle;
		private String iUrl;
		private boolean iConfirms;
		private Set<String> iOperations;
		
		private TableInterface iProperties;
		private TableInterface iOwners;
		private TableInterface iAssignment;
		private TableInterface iPreferences;
		private TableInterface iDistributions;
		
		public void setExamId(Long examId) { iExamId = examId; }
		public Long getExamId() { return iExamId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		public Long getPreviousId() { return iPreviousId; }
		public void setNextId(Long id) { iNextId = id; }
		public Long getNextId() { return iNextId; }
		public String getExamName() { return iExamName; }
		public void setExamName(String name) { iExamName = name; }
		
		public boolean hasBackUrl() { return iBackUrl != null && !iBackUrl.isEmpty(); }
		public void setBackUrl(String backUrl) { iBackUrl = backUrl; }
		public String getBackUrl() { return iBackUrl; }
		public boolean hasBackTitle() { return iBackTitle != null && !iBackTitle.isEmpty(); }
		public void setBackTitle(String backTitle) { iBackTitle = backTitle; }
		public String getBackTitle() { return iBackTitle; }

		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }

		public boolean isConfirms() { return iConfirms; }
		public void setConfirms(boolean confirms) { iConfirms = confirms; }
		
		public boolean hasOperation(String operation) { return iOperations != null && iOperations.contains(operation); }
		public void addOperation(String operation) {
			if (iOperations == null) iOperations = new HashSet<String>();
			iOperations.add(operation);
		}
		
		public boolean hasProperties() { return iProperties != null && !iProperties.hasProperties(); }
		public void addProperty(PropertyInterface property) {
			if (iProperties == null) iProperties = new TableInterface();
			iProperties.addProperty(property);
		}
		public TableInterface getProperties() { return iProperties; }
		public CellInterface addProperty(String text) {
			PropertyInterface p = new PropertyInterface();
			p.setName(text);
			p.setCell(new CellInterface());
			addProperty(p);
			return p.getCell();
		}
		public void setProperties(TableInterface properties) { iProperties = properties; }
		
		public boolean hasOwners() { return iOwners != null; }
		public TableInterface getOwners() { return iOwners; }
		public void setOwners(TableInterface conflicts) { iOwners = conflicts; }

		public boolean hasAssignment() { return iAssignment != null; }
		public TableInterface getAssignment() { return iAssignment; }
		public void setAssignment(TableInterface conflicts) { iAssignment = conflicts; }

		public boolean hasPreferences() { return iPreferences != null; }
		public TableInterface getPreferences() { return iPreferences; }
		public void setPreferences(TableInterface preferences) { iPreferences = preferences; }

		public boolean hasDistributions() { return iDistributions != null; }
		public TableInterface getDistributions() { return iDistributions; }
		public void setDistributions(TableInterface distributions) { iDistributions = distributions; }
	}
	
}
