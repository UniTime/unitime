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
package org.unitime.timetable.gwt.client.offerings;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.shared.FilterInterface;

public class OfferingsInterface {
	
	public static class OfferingsFilterRequest implements GwtRpcRequest<OfferingsFilterResponse> {}
	
	public static class OfferingsFilterResponse extends ClassesFilterResponse {
		private static final long serialVersionUID = 1L;
		private boolean iCanAdd = false;
		private boolean iCanWorksheet = false;
		
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }

		public boolean isCanWorksheet() { return iCanWorksheet; }
		public void setCanWorksheet(boolean canWorksheet) { iCanWorksheet = canWorksheet; }
	}
	
	public static class ClassesFilterRequest implements GwtRpcRequest<ClassesFilterResponse> {}
	
	public static class ClassesFilterResponse extends FilterInterface {
		private static final long serialVersionUID = 1L;
		private boolean iSticky = false;
		private boolean iCanExport = false;
		private Integer iMaxSubjectsToSearchAutomatically = null;
		private Long iSessionId = null;
		
		public boolean isSticky() { return iSticky; }
		public void setSticky(boolean sticky) { iSticky = sticky; }
		
		public boolean isCanExport() { return iCanExport; }
		public void setCanExport(boolean canExport) { iCanExport = canExport; }

		public Integer getMaxSubjectsToSearchAutomatically() { return iMaxSubjectsToSearchAutomatically; }
		public void setMaxSubjectsToSearchAutomatically(Integer max) { iMaxSubjectsToSearchAutomatically = max; }
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
	}
	
	public static class OfferingsRequest implements GwtRpcRequest<GwtRpcResponseList<TableInterface>> {
		private FilterInterface iFilter;
		private String iBackId, iBackType;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
		
		public String getBackId() { return iBackId; }
		public void setBackId(String backId) { iBackId = backId; }
		public String getBackType() { return iBackType; }
		public void setBackType(String backType) { iBackType = backType; }
	}
	
	public static class ClassesRequest implements GwtRpcRequest<GwtRpcResponseList<TableInterface>> {
		private FilterInterface iFilter;
		private String iBackId, iBackType;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
		
		public String getBackId() { return iBackId; }
		public void setBackId(String backId) { iBackId = backId; }
		public String getBackType() { return iBackType; }
		public void setBackType(String backType) { iBackType = backType; }
	}
	
	public static class OfferingDetailsRequest implements GwtRpcRequest<OfferingDetailsResponse> {
		private Long iOfferingId;
		
		public OfferingDetailsRequest() {}
		
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }
	}
	
	public static class OfferingDetailsResponse implements GwtRpcResponse {
		private List<InstrOfferingConfigurationInterface> iConfigurations;
		
		public OfferingDetailsResponse() {}
		
		public List<InstrOfferingConfigurationInterface> getConfigurations() { return iConfigurations; }
		public void addConfiguration(InstrOfferingConfigurationInterface configuration) {
			if (iConfigurations == null) iConfigurations = new ArrayList<InstrOfferingConfigurationInterface>();
			iConfigurations.add(configuration);
		}
	}
	
	public static class InstrOfferingConfigurationInterface extends TableInterface {
		
	}

}
