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
package org.unitime.timetable.server.instructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.IncludeLine;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.MultiRequest;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.Request;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.RequestedClass;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.SaveRequestsRpcRequest;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.SingleRequest;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SaveRequestsRpcRequest.class)
public class SaveTeachingRequestsBackend implements GwtRpcImplementation<SaveRequestsRpcRequest, GwtRpcResponseNull> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public GwtRpcResponseNull execute(SaveRequestsRpcRequest request, SessionContext context) {
		org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
		InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(request.getOfferingId(), hibSession);
		if (offering == null)
			throw new GwtRpcException(MESSAGES.errorOfferingDoesNotExist(request.getOfferingId().toString()));
		context.checkPermission(offering.getDepartment(), Right.InstructorAssignmentPreferences);
		Transaction tx = hibSession.beginTransaction();
		try {
			Map<Long, TeachingRequest> requests = new HashMap<Long, TeachingRequest>();
			for (TeachingRequest r: offering.getTeachingRequests())
				requests.put(r.getUniqueId(), r);
			for (Request r: request.getRequests()) {
				if (r instanceof SingleRequest) {
					SingleRequest single = (SingleRequest)r;
					TeachingRequest tr = null;
					if (single.getRequestId() != null)
						tr = requests.remove(single.getRequestId());
					if (tr == null) {
						tr = new TeachingRequest();
						tr.setOffering(offering);
						offering.getTeachingRequests().add(tr);
						tr.setClassRequests(new HashSet<TeachingClassRequest>());
						tr.setPreferences(new HashSet<Preference>());
					} else {
						for (Iterator<Preference> i = tr.getPreferences().iterator(); i.hasNext(); ) {
							Preference p = i.next();
							hibSession.delete(p); i.remove();
						}
					}
					tr.setNbrInstructors(single.getNbrInstructors());
					fillRequestInfo(r, tr, hibSession);
					List<TeachingClassRequest> remains = new ArrayList<TeachingClassRequest>(tr.getClassRequests());
					for (IncludeLine line: single.getClasses()) {
						TeachingClassRequest cr = null;
						for (Iterator<TeachingClassRequest> i = remains.iterator(); i.hasNext(); ) {
							TeachingClassRequest adept = i.next();
							if (adept.getTeachingClass().getUniqueId().equals(line.getOwnerId())) {
								cr = adept; i.remove(); break;
							}
						}
						if (cr == null) {
							cr = new TeachingClassRequest();
							cr.setTeachingClass(Class_DAO.getInstance().get(line.getOwnerId(), hibSession));
							if (cr.getTeachingClass() == null) continue;
							cr.getTeachingClass().getTeachingRequests().add(cr);
							cr.setTeachingRequest(tr);
							tr.getClassRequests().add(cr);
						}
						cr.setAssignInstructor(line.isAssign());
						cr.setCommon(line.isCommon());
						cr.setCanOverlap(line.isCanOverlap());
						cr.setLead(line.isLead());
						cr.setPercentShare(line.getShare());
					}
					for (TeachingClassRequest cr: remains) {
						cr.getTeachingClass().getTeachingRequests().remove(cr);
						tr.getClassRequests().remove(cr);
						hibSession.delete(cr);
					}
					hibSession.saveOrUpdate(tr);
				} else {
					MultiRequest multi = (MultiRequest)r;
					for (RequestedClass rc: multi.getClasses()) {
						Class_ clazz = Class_DAO.getInstance().get(rc.getClassId(), hibSession);
						if (clazz == null) continue;
						TeachingRequest tr = null;
						if (rc.getRequestId() != null)
							tr = requests.remove(rc.getRequestId());
						if (tr == null) {
							tr = new TeachingRequest();
							tr.setOffering(offering);
							offering.getTeachingRequests().add(tr);
							tr.setClassRequests(new HashSet<TeachingClassRequest>());
							tr.setPreferences(new HashSet<Preference>());
						} else {
							for (Iterator<Preference> i = tr.getPreferences().iterator(); i.hasNext(); ) {
								Preference p = i.next();
								hibSession.delete(p); i.remove();
							}
						}
						tr.setNbrInstructors(rc.getNbrInstructors());
						fillRequestInfo(r, tr, hibSession);
						tr.setAssignCoordinator(false);
						List<TeachingClassRequest> remains = new ArrayList<TeachingClassRequest>(tr.getClassRequests());
						for (IncludeLine line: multi.getSubparts()) {
							for (Class_ c: getClasses(clazz, line.getOwnerId(), hibSession)) {
								TeachingClassRequest cr = null;
								for (Iterator<TeachingClassRequest> i = remains.iterator(); i.hasNext(); ) {
									TeachingClassRequest adept = i.next();
									if (c.equals(adept.getTeachingClass())) {
										cr = adept; i.remove(); break;
									}
								}
								if (cr == null) {
									cr = new TeachingClassRequest();
									cr.setTeachingClass(c);
									if (cr.getTeachingClass() == null) continue;
									cr.getTeachingClass().getTeachingRequests().add(cr);
									cr.setTeachingRequest(tr);
									tr.getClassRequests().add(cr);
								}
								cr.setAssignInstructor(line.isAssign());
								cr.setCommon(line.isCommon());
								cr.setCanOverlap(line.isCanOverlap());
								cr.setLead(line.isLead());
								cr.setPercentShare(line.getShare());
							}
						}
						hibSession.saveOrUpdate(tr);
					}
					if (r.isAssignCoordinator()) {
						TeachingRequest tr = new TeachingRequest();
						tr.setOffering(offering);
						offering.getTeachingRequests().add(tr);
						tr.setClassRequests(new HashSet<TeachingClassRequest>());
						tr.setPreferences(new HashSet<Preference>());
						tr.setNbrInstructors(1);
						fillRequestInfo(r, tr, hibSession);
						hibSession.saveOrUpdate(tr);
					}
				}
			}
			for (TeachingRequest tr: requests.values()) {
				tr.getOffering().getTeachingRequests().remove(tr);
				for (Iterator<TeachingClassRequest> i = tr.getClassRequests().iterator(); i.hasNext(); ) {
					TeachingClassRequest cr = i.next();
					cr.getTeachingClass().getTeachingRequests().remove(cr);
					i.remove();
					hibSession.delete(cr);
				}
				for (Iterator<Preference> i = tr.getPreferences().iterator(); i.hasNext(); ) {
					Preference p = i.next();
					hibSession.delete(p); i.remove();
				}
				hibSession.delete(tr);
			}
			hibSession.saveOrUpdate(offering);
			
			tx.commit(); tx = null;
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			throw new GwtRpcException(e.getMessage(), e);
		}
		return new GwtRpcResponseNull();
	}
	
	protected void fillRequestInfo(Request r, TeachingRequest tr, org.hibernate.Session hibSession) {
		tr.setAssignCoordinator(r.isAssignCoordinator());
		tr.setTeachingLoad(r.getTeachingLoad());
		tr.setResponsibility(r.getTeachingResponsibility() == null ? null : TeachingResponsibilityDAO.getInstance().get(r.getTeachingResponsibility().getId(), hibSession));
		tr.setSameCoursePreference(r.getSameCoursePreference() == null ? null : PreferenceLevelDAO.getInstance().get(r.getSameCoursePreference(), hibSession));
		tr.setSameCommonPart(r.getSameCommonPreference() == null ? null : PreferenceLevelDAO.getInstance().get(r.getSameCommonPreference(), hibSession));
		if (r.hasInstructorPrefernces())
			for (TeachingRequestInterface.Preference p: r.getInstructorPreferences()) {
				InstructorPref pref = new InstructorPref();
				pref.setPrefLevel(PreferenceLevelDAO.getInstance().get(p.getPreferenceId(), hibSession));
				pref.setInstructor(DepartmentalInstructorDAO.getInstance().get(p.getOwnerId(), hibSession));
				if (pref.getPrefLevel() == null || pref.getInstructor() == null) continue;
				pref.setOwner(tr);
				tr.getPreferences().add(pref);
			}
		if (r.hasAttributePrefernces())
			for (TeachingRequestInterface.Preference p: r.getAttributePreferences()) {
				InstructorAttributePref pref = new InstructorAttributePref();
				pref.setPrefLevel(PreferenceLevelDAO.getInstance().get(p.getPreferenceId(), hibSession));
				pref.setAttribute(InstructorAttributeDAO.getInstance().get(p.getOwnerId(), hibSession));
				if (pref.getPrefLevel() == null || pref.getAttribute() == null) continue;
				pref.setOwner(tr);
				tr.getPreferences().add(pref);
			}
	}
	
	public Collection<Class_> getClasses(Class_ clazz, Long subpartId, org.hibernate.Session hibSession) {
		Collection<Class_> ret = new ArrayList<Class_>();
		// same subpart
		if (clazz.getSchedulingSubpart().getUniqueId().equals(subpartId)) {
			ret.add(clazz); return ret;
		}
		SchedulingSubpart subpart = SchedulingSubpartDAO.getInstance().get(subpartId, hibSession);
		if (subpart == null) return ret;
		// parent subpart
		if (subpart.isParentOf(clazz.getSchedulingSubpart())) {
			Class_ parent = clazz.getParentClass();
			while (parent != null && !parent.getSchedulingSubpart().equals(subpart))
				parent = parent.getParentClass();
			if (parent != null) ret.add(parent);
			return ret;
		}
		// child subpart
		if (clazz.getSchedulingSubpart().isParentOf(subpart)) {
			for (Class_ c: subpart.getClasses()) {
				if (clazz.isParentOf(c)) ret.add(c);
			}
			return ret;
		}
		// same parent?
		SchedulingSubpart parent = clazz.getSchedulingSubpart().getParentSubpart();
		while (parent != null && !parent.isParentOf(subpart)) parent = parent.getParentSubpart();
		if (parent != null) {
			Class_ parentClazz = clazz.getParentClass();
			while (parentClazz != null && !parentClazz.getSchedulingSubpart().equals(parent))
				parentClazz = parentClazz.getParentClass();
			if (parentClazz != null) {
				for (Class_ c: subpart.getClasses()) {
					if (parentClazz.isParentOf(c)) ret.add(c);
				}
				return ret;
			}
		}
		// all classes of the subpart
		return subpart.getClasses();
	}
}
