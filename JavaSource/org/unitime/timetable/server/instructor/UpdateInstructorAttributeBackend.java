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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.PositionInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.UpdateInstructorAttributeRequest;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;
import org.unitime.timetable.model.dao.InstructorAttributeTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(UpdateInstructorAttributeRequest.class)
public class UpdateInstructorAttributeBackend implements GwtRpcImplementation<UpdateInstructorAttributeRequest, AttributeInterface> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public AttributeInterface execute(UpdateInstructorAttributeRequest request, SessionContext context) {
		Transaction tx = null;
		AttributeInterface a = null;
        try {
            org.hibernate.Session hibSession = InstructorAttributeDAO.getInstance().getSession();
            tx = hibSession.beginTransaction();

            InstructorAttribute attribute = null;
            if (request.hasAttribute()) {
            	attribute = createOrUpdateAttribute(request.getAttribute(), request.getAddInstructors(), request.getDropInstructors(), context.getUser().getCurrentAcademicSessionId(), hibSession, context, false);
            } else if (request.getDeleteAttributeId() != null) {
            	dropAttribute(request.getDeleteAttributeId(), context.getUser().getCurrentAcademicSessionId(), hibSession, context, false);
            } else {
            	throw new GwtRpcException("Bad request.");
            }
            
            if (attribute != null) {
            	a = new AttributeInterface();
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
    			NameFormat instructorNameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));
    			boolean sortByLastName = CommonValues.SortByLastName.eq(UserProperty.SortNames.get(context.getUser()));
    			for (DepartmentalInstructor instructor: attribute.getInstructors()) {
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
    				i.setTeachingPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), true));
    				i.setMaxLoad(instructor.getMaxLoad());
    				a.addInstructor(i);
    			}
            }

            tx.commit();
            
            return a;
        } catch (Exception e) {
        	e.printStackTrace();
            if (tx != null) tx.rollback();
            if (e instanceof GwtRpcException) throw (GwtRpcException) e;
            throw new GwtRpcException(e.getMessage());
        }
	}
	
	protected Department lookuDepartment(org.hibernate.Session hibSession, DepartmentInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			return Department.findByDeptCode(original.getDeptCode(), sessionId, hibSession);
		} else {
			return DepartmentDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected InstructorAttribute lookupAttribute(org.hibernate.Session hibSession, AttributeInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			if (original.isDepartmental())
				return (InstructorAttribute)hibSession.createQuery(
					"select f from InstructorAttribute f, InstructorAttribute o where o.uniqueId = :originalId and f.department.session.uniqueId = :sessionId " +
					"and f.code = o.code and f.department.deptCode = o.department.deptCode")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
			else
				return (InstructorAttribute)hibSession.createQuery(
					"select f from InstructorAttribute f, InstructorAttribute o where o.uniqueId = :originalId and f.session.uniqueId = :sessionId " +
					"and f.code = o.code and f.department is null")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
		} else {
			return InstructorAttributeDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected InstructorAttribute lookupAttribute(org.hibernate.Session hibSession, Long attributeId, boolean future, Long sessionId) {
		if (attributeId == null) return null;
		if (future) {
			InstructorAttribute attribute = (InstructorAttribute)hibSession.createQuery(
					"select f from InstructorAttribute f, InstructorAttribute o where o.uniqueId = :originalId and f.department.session.uniqueId = :sessionId " +
					"and f.code = o.code and f.department.deptCode = o.department.deptCode")
					.setLong("sessionId", sessionId).setLong("originalId", attributeId).setCacheable(true).setMaxResults(1).uniqueResult();
			if (attribute == null)
				attribute = (InstructorAttribute)hibSession.createQuery(
					"select f from InstructorAttribute f, InstructorAttribute o where o.uniqueId = :originalId and f.session.uniqueId = :sessionId " +
					"and f.code = o.code and f.department is null")
					.setLong("sessionId", sessionId).setLong("originalId", attributeId).setCacheable(true).setMaxResults(1).uniqueResult();
			return attribute;
		} else {
			return InstructorAttributeDAO.getInstance().get(attributeId, hibSession);
		}
	}
	
	protected Collection<DepartmentalInstructor> lookupInstructors(org.hibernate.Session hibSession, List<Long> ids, boolean future, Long sessionId) {
		if (ids == null || ids.isEmpty()) return new ArrayList<DepartmentalInstructor>();
		if (future) {
			return (List<DepartmentalInstructor>)hibSession.createQuery(
					"select f from DepartmentalInstructor f, DepartmentalInstructor o where o.uniqueId in :ids and " +
					"o.externalUniqueId is not null and f.externalUniqueId = o.externalUniqueId and " +
					"f.department.deptCode = o.department.deptCode and f.department.session.uniqueId = :sessionId")
					.setParameterList("ids", ids).setLong("sessionId", sessionId).list();
		} else {
			return (List<DepartmentalInstructor>)hibSession.createQuery("from DepartmentalInstructor where uniqueId in :ids").setParameterList("ids", ids).list();
		}
	}

	protected InstructorAttribute createOrUpdateAttribute(AttributeInterface attribute, List<Long> add, List<Long> drop, Long sessionId, org.hibernate.Session hibSession, SessionContext context, boolean future) {
		Department d = attribute.isDepartmental() ? lookuDepartment(hibSession, attribute.getDepartment(), future, sessionId) : null;
		if (attribute.isDepartmental() && d == null) return null;

		InstructorAttribute ia = (attribute.getId() == null ? null : lookupAttribute(hibSession, attribute, future, sessionId));

		boolean edit = true;
		if (ia == null) {
			if (!future && attribute.getId() != null)
				throw new GwtRpcException(MESSAGES.errorInstructorAttributeDoesNotExist(attribute.getId()));
    		if (d == null) {
    			context.checkPermission(Right.InstructorGlobalAttributeEdit);
    			ia = new InstructorAttribute();
    			ia.setSession(SessionDAO.getInstance().get(sessionId));
    		} else {
    			context.checkPermission(d, Right.InstructorAttributeAdd);
    			ia = new InstructorAttribute();
    			ia.setDepartment(d);
    			ia.setSession(d.getSession());
    		}
    		ia.setInstructors(new HashSet<DepartmentalInstructor>());
    		ia.setChildAttributes(new HashSet<InstructorAttribute>());
		} else {
			context.checkPermission(ia, Right.InstructorAttributeAssign);
			edit = context.hasPermission(ia, Right.InstructorAttributeEdit);
			if (edit && d == null)
				edit = context.hasPermission(Right.InstructorGlobalAttributeEdit);
			if (edit && d != null)
				ia.setDepartment(d);
		}
    	
		for (Iterator i = InstructorAttribute.getAllGlobalAttributes(sessionId).iterator();i.hasNext();) {
			InstructorAttribute x = (InstructorAttribute)i.next();
			if ((x.getName().equalsIgnoreCase(attribute.getName()) || x.getCode().equalsIgnoreCase(attribute.getCode())) && !x.getUniqueId().equals(ia.getUniqueId()))
				throw new GwtRpcException(MESSAGES.errorInstructorAttributeAlreadyExists(attribute.getName(), SessionDAO.getInstance().get(sessionId).getLabel()));
		}
		
		if (ia.getDepartment() != null) {
			for (Iterator i = InstructorAttribute.getAllDepartmentalAttributes(d.getUniqueId()).iterator();i.hasNext();) {
				InstructorAttribute x = (InstructorAttribute)i.next();
				if ((x.getName().equalsIgnoreCase(attribute.getName()) || x.getCode().equalsIgnoreCase(attribute.getCode())) && !x.getUniqueId().equals(ia.getUniqueId()))
					throw new GwtRpcException(MESSAGES.errorInstructorAttributeAlreadyExists(attribute.getName(), SessionDAO.getInstance().get(sessionId).getLabel()));
			}
		}
		
		if (edit) {
			ia.setCode(attribute.getCode());
			ia.setName(attribute.getName());
			ia.setType(attribute.getType() == null ? null : InstructorAttributeTypeDAO.getInstance().get(attribute.getType().getId()));
			if (ia.getParentAttribute() != null && !ia.getParentAttribute().getUniqueId().equals(attribute.getParentId())) {
				ia.getParentAttribute().getChildAttributes().remove(ia);
			}
			ia.setParentAttribute(attribute.getParentId() == null ? null : InstructorAttributeDAO.getInstance().get(attribute.getParentId()));
			if (ia.getParentAttribute() != null)
				ia.getParentAttribute().getChildAttributes().add(ia);
		}

		hibSession.saveOrUpdate(ia);
    	
    	if (add != null && !add.isEmpty())
			for (DepartmentalInstructor instructor: lookupInstructors(hibSession, add, future, sessionId)) {
				instructor.getAttributes().add(ia);
				ia.getInstructors().add(instructor);
				hibSession.saveOrUpdate(instructor);
			}

    	if (drop != null && !drop.isEmpty())
    		for (DepartmentalInstructor instructor: lookupInstructors(hibSession, drop, future, sessionId)) {
				instructor.getAttributes().remove(ia);
				ia.getInstructors().remove(instructor);
				hibSession.saveOrUpdate(instructor);
			}
    	
    	hibSession.saveOrUpdate(ia);
    	
        ChangeLog.addChange(
                hibSession, 
                context, 
                ia, 
                ChangeLog.Source.INSTRUCTOR_ATTRIBUTE_EDIT, 
                (attribute.getId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE),
                null,
                ia.getDepartment());
        
        return ia;
	}	

	protected boolean dropAttribute(Long attributeId, Long sessionId, org.hibernate.Session hibSession, SessionContext context, boolean future) {
		InstructorAttribute ia = lookupAttribute(hibSession, attributeId, future, sessionId);
		if (ia == null) {
			if (!future) throw new GwtRpcException(MESSAGES.errorInstructorAttributeDoesNotExist(attributeId));
			return false;
		}
		
		if (ia.getDepartment() == null)
			context.checkPermission(Right.InstructorGlobalAttributeEdit);
		context.checkPermission(ia, Right.InstructorAttributeDelete);
		
        ChangeLog.addChange(
                hibSession, 
                context, 
                ia, 
                ChangeLog.Source.INSTRUCTOR_ATTRIBUTE_EDIT, 
                ChangeLog.Operation.DELETE, 
                null, 
                ia.getDepartment());
        
        for (DepartmentalInstructor instructor: ia.getInstructors()) {
        	instructor.getAttributes().remove(ia);
        	hibSession.saveOrUpdate(instructor);
        }
        if (ia.getParentAttribute() != null)
        	ia.getParentAttribute().getChildAttributes().remove(ia);
        for (InstructorAttribute ch: ia.getChildAttributes()) {
        	ch.setParentAttribute(null);
        	hibSession.saveOrUpdate(ch);
        }
        
        for (InstructorAttributePref p: (List<InstructorAttributePref>)hibSession.createQuery("from InstructorAttributePref p where p.attribute.uniqueId = :id").setLong("id", ia.getUniqueId()).list()) {
        	p.getOwner().getPreferences().remove(p);
        	hibSession.delete(p);
        	hibSession.saveOrUpdate(p.getOwner());
        }
        
        hibSession.delete(ia);
        return true;
	}

}
