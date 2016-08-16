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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.ReservationServlet;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.PositionInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.GetRequestsRpcRequest;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.GetRequestsRpcResponse;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.IncludeLine;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.MultiRequest;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.RequestedClass;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.Responsibility;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.SingleRequest;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetRequestsRpcRequest.class)
public class GetTeachingRequestBackend implements GwtRpcImplementation<GetRequestsRpcRequest, GetRequestsRpcResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;

	@Override
	public GetRequestsRpcResponse execute(GetRequestsRpcRequest request, SessionContext context) {
		InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(request.getOfferingId());
		if (offering == null)
			throw new GwtRpcException(MESSAGES.errorOfferingDoesNotExist(request.getOfferingId().toString()));
		context.checkPermission(offering.getDepartment(), Right.InstructorAssignmentPreferences);
		GetRequestsRpcResponse response = new GetRequestsRpcResponse();
		response.setOffering(ReservationServlet.convert(offering, InstructionalOfferingDAO.getInstance().getSession(), null, context, classAssignmentService.getAssignment()));
		
		for (InstructorAttribute attribute: (List<InstructorAttribute>)InstructorAttributeDAO.getInstance().getSession().createQuery(
				"from InstructorAttribute a where a.session.uniqueId = :sessionId and (a.department is null or a.department.uniqueId = :departmentId) order by a.name"
				).setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).setLong("departmentId", offering.getDepartment().getUniqueId()).setCacheable(true).list()) {
			AttributeInterface a = new AttributeInterface();
			a.setId(attribute.getUniqueId());
			a.setParentId(attribute.getParentAttribute() == null ? null : attribute.getParentAttribute().getUniqueId());
			a.setParentName(attribute.getParentAttribute() == null ? null : attribute.getParentAttribute().getName());
			a.setCode(attribute.getCode());
			a.setName(attribute.getName());
			a.setCanDelete(context.hasPermission(attribute, Right.InstructorAttributeDelete));
			a.setCanEdit(context.hasPermission(attribute, Right.InstructorAttributeEdit));
			a.setCanAssign(context.hasPermission(attribute, Right.InstructorAttributeAssign));
			a.setCanChangeType(attribute.getChildAttributes().isEmpty());
			if (attribute.getType() != null) {
				AttributeTypeInterface t = new AttributeTypeInterface();
				t.setId(attribute.getType().getUniqueId());
				t.setAbbreviation(attribute.getType().getReference());
				t.setLabel(attribute.getType().getLabel());
				t.setConjunctive(attribute.getType().isConjunctive());
				t.setRequired(attribute.getType().isRequired());
				a.setType(t);
			}
			if (attribute.getDepartment() != null) {
				DepartmentInterface d = new DepartmentInterface();
				d.setId(attribute.getDepartment().getUniqueId());
				d.setAbbreviation(attribute.getDepartment().getAbbreviation());
				d.setDeptCode(attribute.getDepartment().getDeptCode());
				d.setLabel(attribute.getDepartment().getName());
				d.setTitle(attribute.getDepartment().getLabel());
				a.setDepartment(d);
			}
			response.addAttribute(a);
		}
		
		NameFormat instructorNameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));
		boolean sortByLastName = CommonValues.SortByLastName.eq(UserProperty.SortNames.get(context.getUser()));
		for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor i where i.department.uniqueId = :departmentId").setLong("departmentId", offering.getDepartment().getUniqueId()).setCacheable(true).list()) {
			InstructorInterface i = new InstructorInterface();
			i.setId(instructor.getUniqueId());
			i.setFirstName(instructor.getFirstName());
			i.setMiddleName(instructor.getMiddleName());
			i.setLastName(instructor.getLastName());
			i.setFormattedName(instructorNameFormat.format(instructor));
			if (sortByLastName)
				i.setOrderName(instructor.nameLastNameFirst());
			i.setExternalId(instructor.getExternalUniqueId());
			if (instructor.getPositionType() != null) {
				PositionInterface p = new PositionInterface();
				p.setId(instructor.getPositionType().getUniqueId());
				p.setAbbreviation(instructor.getPositionType().getReference());
				p.setLabel(instructor.getPositionType().getLabel());
				p.setSortOrder(instructor.getPositionType().getSortOrder());
				i.setPosition(p);
			}
			PreferenceLevel pref = instructor.getTeachingPreference();
			if (pref == null) pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited);
			i.setTeachingPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2color(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), true));
			i.setMaxLoad(instructor.getMaxLoad());
			response.addInstructor(i);
		}
		
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
			response.addPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), true));
		}
		
		Map<Long, Responsibility> responsibilities = new HashMap<Long, Responsibility>();
		for (TeachingResponsibility responsibility: (List<TeachingResponsibility>)TeachingResponsibilityDAO.getInstance().getSession().createQuery(
				"from TeachingResponsibility order by label").setCacheable(true).list()) {
			Responsibility r = new Responsibility();
			r.setId(responsibility.getUniqueId());
			r.setAbbv(responsibility.getAbbreviation());
			r.setName(responsibility.getLabel());
			r.setCoordinator(responsibility.isCoordinator());
			r.setInstructor(responsibility.isInstructor());
			response.addResponsibility(r);
			responsibilities.put(responsibility.getUniqueId(), r);
		}
		
		Map<TeachingRequest, MultiRequest> requests = new HashMap<TeachingRequest, MultiRequest>();
		for (TeachingRequest r: offering.getTeachingRequests()) {
			TeachingClassRequest master = r.getMasterRequest(true);
			if (r.isStandard(master) && !r.isAssignCoordinator()) {
				MultiRequest req = null;
				for (Map.Entry<TeachingRequest, MultiRequest> e: requests.entrySet()) {
					if (e.getKey().canCombine(r)) {
						req = e.getValue(); break;
					}
				}
				if (req == null) {
					req = new MultiRequest();
					req.setAssignCoordinator(false);
					req.setSameCommonPreference(r.getSameCommonPart() == null ? null : r.getSameCommonPart().getUniqueId());
					req.setSameCoursePreference(r.getSameCoursePreference() == null ? null : r.getSameCoursePreference().getUniqueId());
					req.setTeachingLoad(r.getTeachingLoad());
					req.setTeachingResponsibility(r.getResponsibility() == null ? null : responsibilities.get(r.getResponsibility().getUniqueId()));
					response.addRequest(req);
					Set<SchedulingSubpart> subparts = new HashSet<SchedulingSubpart>();
					for (TeachingClassRequest cr: r.getClassRequests()) {
						if (subparts.add(cr.getTeachingClass().getSchedulingSubpart())) {
							IncludeLine include = new IncludeLine();
							include.setAssign(cr.isAssignInstructor());
							include.setCommon(cr.isCommon());
							include.setCanOverlap(cr.isCanOverlap());
							include.setLead(cr.isLead());
							include.setShare(cr.getPercentShare() == null ? 0 : cr.getPercentShare().intValue());
							include.setOwnerId(cr.getTeachingClass().getSchedulingSubpart().getUniqueId());
							req.addSubpart(include);
						}
					}
					for (Preference pref: new TreeSet<Preference>(r.getPreferences())) {
						if (pref instanceof InstructorPref) {
							TeachingRequestInterface.Preference p = new TeachingRequestInterface.Preference();
							p.setPreferenceId(pref.getPrefLevel().getUniqueId());
							p.setOwnerId(((InstructorPref)pref).getInstructor().getUniqueId());
							req.addInstructorPreference(p);
						} else if (pref instanceof InstructorAttributePref) {
							TeachingRequestInterface.Preference p = new TeachingRequestInterface.Preference();
							p.setPreferenceId(pref.getPrefLevel().getUniqueId());
							p.setOwnerId(((InstructorAttributePref)pref).getAttribute().getUniqueId());
							req.addAttributePreference(p);
						}
					}
					requests.put(r, req);
				}
				RequestedClass rc = new RequestedClass();
				rc.setNbrInstructors(r.getNbrInstructors());
				rc.setClassId(master.getTeachingClass().getUniqueId());
				rc.setInstructorIds(toInstructorIds(r));
				rc.setRequestId(r.getUniqueId());
				req.addClass(rc);
			} else {
				SingleRequest req = new SingleRequest();
				req.setRequestId(r.getUniqueId());
				req.setInstructorIds(toInstructorIds(r));
				req.setAssignCoordinator(r.isAssignCoordinator());
				req.setSameCommonPreference(r.getSameCommonPart() == null ? null : r.getSameCommonPart().getUniqueId());
				req.setSameCoursePreference(r.getSameCoursePreference() == null ? null : r.getSameCoursePreference().getUniqueId());
				req.setTeachingLoad(r.getTeachingLoad());
				req.setTeachingResponsibility(r.getResponsibility() == null ? null : responsibilities.get(r.getResponsibility().getUniqueId()));
				for (TeachingClassRequest cr: r.getClassRequests()) {
					IncludeLine include = new IncludeLine();
					include.setAssign(cr.isAssignInstructor());
					include.setCommon(cr.isCommon());
					include.setCanOverlap(cr.isCanOverlap());
					include.setLead(cr.isLead());
					include.setShare(cr.getPercentShare() == null ? 0 : cr.getPercentShare().intValue());
					include.setOwnerId(cr.getTeachingClass().getUniqueId());
					req.addClass(include);
				}
				for (Preference pref: new TreeSet<Preference>(r.getPreferences())) {
					if (pref instanceof InstructorPref) {
						TeachingRequestInterface.Preference p = new TeachingRequestInterface.Preference();
						p.setPreferenceId(pref.getPrefLevel().getUniqueId());
						p.setOwnerId(((InstructorPref)pref).getInstructor().getUniqueId());
						req.addInstructorPreference(p);
					} else if (pref instanceof InstructorAttributePref) {
						TeachingRequestInterface.Preference p = new TeachingRequestInterface.Preference();
						p.setPreferenceId(pref.getPrefLevel().getUniqueId());
						p.setOwnerId(((InstructorAttributePref)pref).getAttribute().getUniqueId());
						req.addAttributePreference(p);
					}
				}
				response.addRequest(req);
			}
		}
		
		return response;
	}
	
	public static List<Long> toInstructorIds(TeachingRequest request) {
		if (request.getAssignedInstructors() == null || request.getAssignedInstructors().isEmpty()) return null;
		List<DepartmentalInstructor> instructors = new ArrayList<DepartmentalInstructor>(request.getAssignedInstructors());
		Collections.sort(instructors);
		List<Long> ret = new ArrayList<Long>(instructors.size());
		for (DepartmentalInstructor instructor: instructors)
			ret.add(instructor.getUniqueId());
		return ret;
	}

}
