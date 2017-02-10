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
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
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
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.OfferingCoordinator;
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
		boolean commit = Department.isInstructorSchedulingCommitted(offering.getDepartment().getUniqueId());
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
					for (TeachingClassRequest tcr: tr.getClassRequests())
						hibSession.saveOrUpdate(tcr);
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
						tr.setPercentShare(0);
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
						for (TeachingClassRequest cr: remains) {
							cr.getTeachingClass().getTeachingRequests().remove(cr);
							tr.getClassRequests().remove(cr);
							hibSession.delete(cr);
						}						
						hibSession.saveOrUpdate(tr);
						for (TeachingClassRequest tcr: tr.getClassRequests())
							hibSession.saveOrUpdate(tcr);
					}
				}
			}
			Set<DepartmentalInstructor> updatedInstructors = new HashSet<DepartmentalInstructor>();
			for (TeachingRequest tr: requests.values()) {
				tr.getOffering().getTeachingRequests().remove(tr);
				for (Iterator<TeachingClassRequest> i = tr.getClassRequests().iterator(); i.hasNext(); ) {
					TeachingClassRequest cr = i.next();
					for (Iterator<ClassInstructor> j = cr.getTeachingClass().getClassInstructors().iterator(); j.hasNext(); ) {
						ClassInstructor ci = j.next();
						if (tr.equals(ci.getTeachingRequest())) {
							Debug.info(cr.getTeachingClass().getClassLabel(hibSession) + ": UNASSIGN " + ci.getInstructor().getNameLastFirst());
							updatedInstructors.add(ci.getInstructor());
							ci.getInstructor().getClasses().remove(ci);
							hibSession.delete(ci);
							j.remove();
						}
					}
					cr.getTeachingClass().getTeachingRequests().remove(cr);
					i.remove();
					hibSession.delete(cr);
				}
				for (Iterator<Preference> i = tr.getPreferences().iterator(); i.hasNext(); ) {
					Preference p = i.next();
					hibSession.delete(p); i.remove();
				}
				for (Iterator<OfferingCoordinator> i = tr.getOffering().getOfferingCoordinators().iterator(); i.hasNext(); ) {
					OfferingCoordinator oc = i.next();
					if (tr.equals(oc.getTeachingRequest())) {
						Debug.info(tr.getOffering().getCourseName() + ": UNASSIGN " + oc.getInstructor().getNameLastFirst());
						updatedInstructors.add(oc.getInstructor());
						oc.getInstructor().getOfferingCoordinators().remove(oc);
						hibSession.delete(oc);
						i.remove();
					}
				}
				hibSession.delete(tr);
			}
			for (Iterator<OfferingCoordinator> i = offering.getOfferingCoordinators().iterator(); i.hasNext(); ) {
				OfferingCoordinator oc = i.next();
				if (oc.getTeachingRequest() != null && !oc.getTeachingRequest().isAssignCoordinator()) {
					Debug.info(offering.getCourseName() + ": UNASSIGN " + oc.getInstructor().getNameLastFirst());
					updatedInstructors.add(oc.getInstructor());
					oc.getInstructor().getOfferingCoordinators().remove(oc);
					hibSession.delete(oc);
					i.remove();
				}
			}
			for (InstrOfferingConfig config: offering.getInstrOfferingConfigs())
				for (SchedulingSubpart subpart: config.getSchedulingSubparts())
					for (Class_ clazz: subpart.getClasses())
						for (Iterator<ClassInstructor> i = clazz.getClassInstructors().iterator(); i.hasNext(); ) {
							ClassInstructor ci = i.next();
							if (ci.getTeachingRequest() != null) {
								TeachingClassRequest support = null;
								for (TeachingClassRequest tcr: ci.getTeachingRequest().getClassRequests()) {
									if (tcr.getTeachingClass().equals(clazz) && tcr.isAssignInstructor()) { support = tcr; break; }
								}
								if (support == null) {
									Debug.info(clazz.getClassLabel(hibSession) + ": UNASSIGN " + ci.getInstructor().getNameLastFirst());
									updatedInstructors.add(ci.getInstructor());
									ci.getInstructor().getClasses().remove(ci);
									hibSession.delete(ci);
									i.remove();
								} else if (!ci.getPercentShare().equals(support.getPercentShare()) || !ci.isLead().equals(support.getLead()) ||
										!ToolBox.equals(ci.getResponsibility(), support.getTeachingRequest().getResponsibility())) {
									Debug.info(clazz.getClassLabel(hibSession) + ": UPDATE " + ci.getInstructor().getNameLastFirst());
									ci.setPercentShare(support.getPercentShare());
									ci.setLead(support.getLead());
									ci.setResponsibility(support.getTeachingRequest().getResponsibility());
									hibSession.saveOrUpdate(ci);
								}
							}
						}
			if (commit) {
				for (TeachingRequest tr: offering.getTeachingRequests()) {
					if (tr.getAssignedInstructors() == null || tr.getAssignedInstructors().isEmpty()) continue;
					if (tr.isAssignCoordinator()) {
						for (DepartmentalInstructor instructor: tr.getAssignedInstructors()) {
							OfferingCoordinator support = null;
							for (OfferingCoordinator oc: offering.getOfferingCoordinators()) {
								if (instructor.equals(oc.getInstructor()) && tr.equals(oc.getTeachingRequest())) {
									support = oc; break;
								}
							}
							if (support == null) {
								Debug.info(offering.getCourseName() + ": ASSIGN " + instructor.getNameLastFirst());
								OfferingCoordinator oc = new OfferingCoordinator();
								oc.setInstructor(instructor);
								oc.setOffering(offering);
								oc.setResponsibility(tr.getResponsibility());
								oc.setPercentShare(tr.getPercentShare());
								oc.setTeachingRequest(tr);
								offering.getOfferingCoordinators().add(oc);
								hibSession.save(oc);
							}
						}
					}
					for (TeachingClassRequest cr: tr.getClassRequests()) {
						if (cr.isAssignInstructor()) {
							for (DepartmentalInstructor instructor: tr.getAssignedInstructors()) {
								ClassInstructor support = null;
								for (ClassInstructor ci: cr.getTeachingClass().getClassInstructors()) {
									if (instructor.equals(ci.getInstructor()) && tr.equals(ci.getTeachingRequest())) {
										support = ci; break;
									}
								}
								if (support == null) {
									Debug.info(cr.getTeachingClass().getClassLabel(hibSession) + ": ASSIGN " + instructor.getNameLastFirst());
									ClassInstructor ci = new ClassInstructor();
									ci.setClassInstructing(cr.getTeachingClass());
									ci.setInstructor(instructor);
									ci.setLead(cr.isLead());
									ci.setPercentShare(cr.getPercentShare());
									ci.setResponsibility(tr.getResponsibility());
									ci.setTeachingRequest(tr);
									cr.getTeachingClass().getClassInstructors().add(ci);
									instructor.getClasses().add(ci);
									hibSession.saveOrUpdate(ci);
								}
							}
						}
					}
				}
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
		tr.setPercentShare(r.getPercentShare());
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
