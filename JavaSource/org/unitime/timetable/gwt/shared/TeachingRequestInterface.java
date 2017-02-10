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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.Offering;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TeachingRequestInterface implements IsSerializable {
	
	public static abstract class Request implements IsSerializable {
		private float iTeachingLoad;
		private Long iSameCoursePref, iSameCommonPref;
		private Responsibility iTeachingResponsibility;
		private List<Preference> iInstructorPreferences;
		private List<Preference> iAttributePreferences;
		private boolean iAssignCoordinator = false;
		private int iPercentShare = 0;
		
		public Request() {}
		
		public void setTeachingLoad(float teachingLoad) { iTeachingLoad = teachingLoad; }
		public float getTeachingLoad() { return iTeachingLoad; } 
		
		public void setSameCoursePreference(Long prefId) { iSameCoursePref = prefId; }
		public Long getSameCoursePreference() { return iSameCoursePref; }
		
		public void setSameCommonPreference(Long prefId) { iSameCommonPref = prefId; }
		public Long getSameCommonPreference() { return iSameCommonPref; }
		
		public void setTeachingResponsibility(Responsibility responsibility) { iTeachingResponsibility = responsibility; }
		public Responsibility getTeachingResponsibility() { return iTeachingResponsibility; }

		public boolean hasInstructorPrefernces() { return iInstructorPreferences != null && !iInstructorPreferences.isEmpty(); }
		public void addInstructorPreference(Preference p) {
			if (iInstructorPreferences == null) iInstructorPreferences = new ArrayList<Preference>();
			iInstructorPreferences.add(p);
		}
		public List<Preference> getInstructorPreferences() { return iInstructorPreferences; }

		public boolean hasAttributePrefernces() { return iAttributePreferences != null && !iAttributePreferences.isEmpty(); }
		public void addAttributePreference(Preference p) {
			if (iAttributePreferences == null) iAttributePreferences = new ArrayList<Preference>();
			iAttributePreferences.add(p);
		}
		public List<Preference> getAttributePreferences() { return iAttributePreferences; }
		
		public boolean isAssignCoordinator() { return iAssignCoordinator; }
		public void setAssignCoordinator(boolean assign) { iAssignCoordinator = assign; }
		public int getPercentShare() { return iPercentShare; }
		public void setPercentShare(Integer percentShare) { iPercentShare = (percentShare == null ? 0 : percentShare.intValue()); }
	}

	public static class MultiRequest extends Request {
		private List<RequestedClass> iClasses = new ArrayList<RequestedClass>(); 
		private List<IncludeLine> iSubparts = new ArrayList<IncludeLine>();
		
		public MultiRequest() {}
		
		public void addClass(RequestedClass clazz) { iClasses.add(clazz); }
		public List<RequestedClass> getClasses() { return iClasses; }
		public RequestedClass getClass(Long id) {
			if (id == null) return null;
			for (RequestedClass r: iClasses)
				if (id.equals(r.getClassId())) return r;
			return null;
		}
		
		public void addSubpart(IncludeLine subpart) { iSubparts.add(subpart); }
		public List<IncludeLine> getSubparts() { return iSubparts; }
		public IncludeLine getSubpart(Long id) {
			if (id == null) return null;
			for (IncludeLine l: iSubparts)
				if (id.equals(l.getOwnerId())) return l;
			return null;
		}
	}
	
	public static class SingleRequest extends Request {
		private Long iRequestId;
		private List<Long> iInstructorIds = null;
		private List<IncludeLine> iClasses = new ArrayList<IncludeLine>();
		private int iNbrInstructors = 1;
		
		public Long getRequestId() { return iRequestId; }
		public void setRequestId(Long id) { iRequestId = id; }
		
		public void addClass(IncludeLine clazz) { iClasses.add(clazz); }
		public List<IncludeLine> getClasses() { return iClasses; }
		public IncludeLine getClazz(Long id) {
			if (id == null) return null;
			for (IncludeLine l: iClasses)
				if (id.equals(l.getOwnerId())) return l;
			return null;
		}
		
		public boolean hasInstructors() { return iInstructorIds != null && !iInstructorIds.isEmpty(); }
		public void addInstructorId(Long id) {
			if (iInstructorIds == null) iInstructorIds = new ArrayList<Long>();
			iInstructorIds.add(id);
		}
		public List<Long> getInstructorIds() { return iInstructorIds; }
		public void setInstructorIds(List<Long> instructorIds) { iInstructorIds = instructorIds; }
		
		public int getNbrInstructors() { return iNbrInstructors; }
		public void setNbrInstructors(int nbrInstructors) { iNbrInstructors = nbrInstructors; }
	}
	
	public static class RequestedClass implements IsSerializable {
		private Long iClassId;
		private Long iRequestId;
		private List<Long> iInstructorIds = null;
		private int iNbrInstructors = 1;

		public RequestedClass() {}
		
		public Long getClassId() { return iClassId; }
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getRequestId() { return iRequestId; }
		public void setRequestId(Long requestId) { iRequestId = requestId; }
		public boolean hasInstructors() { return iInstructorIds != null && !iInstructorIds.isEmpty(); }
		public void addInstructorId(Long id) {
			if (iInstructorIds == null) iInstructorIds = new ArrayList<Long>();
			iInstructorIds.add(id);
		}
		public List<Long> getInstructorIds() { return iInstructorIds; }
		public void setInstructorIds(List<Long> instructorIds) { iInstructorIds = instructorIds; }
		public int getNbrInstructors() { return iNbrInstructors; }
		public void setNbrInstructors(int nbrInstructors) { iNbrInstructors = nbrInstructors; }
	}
	
	public static class IncludeLine implements IsSerializable {
		private Long iOwnerId;
		private boolean iAssign;
		private int iShare;
		private boolean iLead;
		private boolean iCanOverlap;
		private boolean iCommon;
		
		public IncludeLine() {}
		
		public Long getOwnerId() { return iOwnerId; }
		public void setOwnerId(Long ownerId) { iOwnerId = ownerId; }
		public boolean isAssign() { return iAssign; }
		public void setAssign(boolean assign) { iAssign = assign; }
		public int getShare() { return iShare; }
		public void setShare(int share) { iShare = share; }
		public boolean isLead() { return iLead; }
		public void setLead(boolean lead) { iLead = lead; }
		public boolean isCanOverlap() { return iCanOverlap; }
		public void setCanOverlap(boolean canOverlap) { iCanOverlap = canOverlap; }
		public boolean isCommon() { return iCommon; }
		public void setCommon(boolean common) { iCommon = common; }
	}
	
	public static class Preference implements IsSerializable {
		private Long iOwnerId;
		private Long iPreferenceId;
		
		public Preference() {}
		
		public Long getOwnerId() { return iOwnerId; }
		public void setOwnerId(Long ownerId) { iOwnerId = ownerId; }
		public Long getPreferenceId() { return iPreferenceId; }
		public void setPreferenceId(Long preferenceId) { iPreferenceId = preferenceId; }
	}
	
	public static class Properties implements IsSerializable {
		private Offering iOffering;
		List<PreferenceInterface> iPreferences = new ArrayList<PreferenceInterface>();
		List<InstructorInterface> iInstructors = new ArrayList<InstructorInterface>();
		List<AttributeInterface> iAttributes = new ArrayList<AttributeInterface>();
		List<Responsibility> iResponsibilities = new ArrayList<Responsibility>();
		
		public Properties() {}
		
		public Offering getOffering() { return iOffering; }
		public void setOffering(Offering offering) { iOffering = offering; }
		
		public void addPreference(PreferenceInterface preference) { iPreferences.add(preference); }
		public List<PreferenceInterface> getPreferences() { return iPreferences; }
		public PreferenceInterface getPreference(Long id) {
			if (id == null) return null;
			for (PreferenceInterface p: iPreferences)
				if (id.equals(p.getId())) return p;
			return null;
		}
		
		public void addInstructor(InstructorInterface instructor) { iInstructors.add(instructor); }
		public List<InstructorInterface> getInstructors() { return iInstructors; }
		public InstructorInterface getInstructor(Long id) {
			if (id == null) return null;
			for (InstructorInterface i: iInstructors)
				if (id.equals(i.getId())) return i;
			return null;
		}
		
		public void addAttribute(AttributeInterface attribute) { iAttributes.add(attribute); }
		public List<AttributeInterface> getAttributes() { return iAttributes; }
		public AttributeInterface getAttribute(Long id) {
			if (id == null) return null;
			for (AttributeInterface a: iAttributes)
				if (id.equals(a.getId())) return a;
			return null;
		}
		
		public void addResponsibility(Responsibility responsibility) { iResponsibilities.add(responsibility); }
		public List<Responsibility> getResponsibilities() { return iResponsibilities; }
		public Responsibility getResponsibility(Long id) {
			if (id == null) return null;
			for (Responsibility r: iResponsibilities)
				if (id.equals(r.getId())) return r;
			return null;
		}
	}
	
	public static class Responsibility extends IdName {
		private boolean iCoordinator, iInstructor;
		
		public Responsibility() {}
		
		public void setCoordinator(boolean coordinator) { iCoordinator = coordinator; }
		public boolean isCoordinator() { return iCoordinator; }
		public void setInstructor(boolean instructor) { iInstructor = instructor; }
		public boolean isInstructor() { return iInstructor; }
	}
	
	public static class GetRequestsRpcRequest implements GwtRpcRequest<GetRequestsRpcResponse> {
		private Long iOfferingId;
		
		public GetRequestsRpcRequest() {}
		
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }
	}
	
	public static class GetRequestsRpcResponse extends Properties implements GwtRpcResponse {
		private List<Request> iRequests = new ArrayList<Request>();
		
		public GetRequestsRpcResponse() {}
		
		public void addRequest(Request request) { iRequests.add(request); }
		public List<Request> getRequests() { return iRequests; }
	}
	
	public static class SaveRequestsRpcRequest implements GwtRpcRequest<GwtRpcResponseNull> {
		private Long iOfferingId;
		private List<Request> iRequests = new ArrayList<Request>();
		
		public SaveRequestsRpcRequest() {}
		
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }

		public void addRequest(Request request) { iRequests.add(request); }
		public List<Request> getRequests() { return iRequests; }
	}
}
