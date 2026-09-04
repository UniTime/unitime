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
package org.unitime.timetable.server.administration.other;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.StatusTypesPage.StatusTypeEditRequest;
import org.unitime.timetable.gwt.client.admin.StatusTypesPage.StatusTypeEditResponse;
import org.unitime.timetable.gwt.client.admin.StatusTypesPage.StatusTypeInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(StatusTypeEditRequest.class)
public class StatusTypeEditBackend implements GwtRpcImplementation<StatusTypeEditRequest, StatusTypeEditResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static GwtMessages GMSG = Localization.create(GwtMessages.class);
	
	@Override
	public StatusTypeEditResponse execute(StatusTypeEditRequest request, SessionContext context) {
		context.checkPermission(Right.StatusTypes);
		
		StatusTypeInterface ret = null;
		
		switch(request.getOperation()) {
		case UP:
			ret = moveUp(request.getUniqueId());
			break;
		case DOWN:
			ret = moveDown(request.getUniqueId());
			break;
		case ADD:
			ret = new StatusTypeInterface();
			break;
		case EDIT:
			ret = toInterface(DepartmentStatusTypeDAO.getInstance().get(request.getUniqueId()));
			break;
		case DELETE:
			delete(request.getUniqueId());
			break;
		case SAVE:
			DepartmentStatusType s = DepartmentStatusType.findByRef(request.getStatusType().getReference());
			if (request.getUniqueId() == null && s != null)
				throw new GwtRpcException(GMSG.errorMustBeUnique(MSG.fieldReference()));
			if (request.getUniqueId() != null && s != null && !request.getUniqueId().equals(s.getUniqueId()))
				throw new GwtRpcException(GMSG.errorMustBeUnique(MSG.fieldReference()));
			
			saveOrUpdate(request.getStatusType());
			ret = request.getStatusType();
			break;
		}
		
		StatusTypeEditResponse response = new StatusTypeEditResponse();
		response.setStatusType(ret);
		response.setCanDelete(response.getUniqueId() != null);
		return response;
	}
	
	protected StatusTypeInterface toInterface(DepartmentStatusType s) {
		if (s == null) return null;
		StatusTypeInterface ret = new StatusTypeInterface();
		ret.setUniqueId(s.getUniqueId());
		ret.setReference(s.getReference());
		ret.setLabel(s.getLabel());
		ret.setApplyTo(s.getApply());
		ret.setStatus(s.getStatus());
		return ret;
	}

	
	protected StatusTypeInterface moveUp(Long id) {
		Transaction tx = null;
		org.hibernate.Session hibSession = DepartmentStatusTypeDAO.getInstance().getSession();
        try {
            tx = hibSession.beginTransaction();
            
            DepartmentStatusType curStatus = DepartmentStatusTypeDAO.getInstance().get(id);
            boolean found = false;
            for (DepartmentStatusType s: DepartmentStatusType.findAll()) {
                if (s.getOrd()+1==curStatus.getOrd()) {
                    s.setOrd(s.getOrd()+1); 
                    hibSession.merge(s);
                    found = true;
                }
            }
            if (found) {
                curStatus.setOrd(curStatus.getOrd()-1);
                hibSession.merge(curStatus);
            }
            
            tx.commit();
            return toInterface(curStatus);
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected StatusTypeInterface moveDown(Long id) {
		Transaction tx = null;
		org.hibernate.Session hibSession = DepartmentStatusTypeDAO.getInstance().getSession();
        try {
            tx = hibSession.beginTransaction();
            
            DepartmentStatusType curStatus = DepartmentStatusTypeDAO.getInstance().get(id);
            boolean found = false;
            for (DepartmentStatusType s: DepartmentStatusType.findAll()) {
            	if (s.getOrd()-1==curStatus.getOrd()) {
                    s.setOrd(s.getOrd()-1); 
                    hibSession.merge(s);
                    found = true;
                }
            }
            if (found) {
            	curStatus.setOrd(curStatus.getOrd()+1);
                hibSession.merge(curStatus);
            }
            
            tx.commit();
            return toInterface(curStatus);
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected void delete(Long id) {
		Transaction tx = null;
		org.hibernate.Session hibSession = DepartmentStatusTypeDAO.getInstance().getSession();
        try {
            tx = hibSession.beginTransaction();
            
            DepartmentStatusType s = DepartmentStatusTypeDAO.getInstance().get(id);
            
            for (Session session: hibSession.createQuery(
                    "select s from Session s where s.statusType.uniqueId=:id", Session.class).
                    setParameter("id", id).list()) {
                DepartmentStatusType other = null;
                for (DepartmentStatusType x: DepartmentStatusType.findAll()) {
                    if (!x.getUniqueId().equals(id) && x.applySession()) {
                        other = x; break;
                    }
                }
                if (other==null)
                    throw new GwtRpcException("Unable to delete session status " + s.getReference() + ", no other session status available.");
                session.setStatusType(other);
                hibSession.merge(session);
            }
            for (Department dept: hibSession.createQuery(
                    "select d from Department d where d.statusType.uniqueId=:id",Department.class).
                    setParameter("id", id).list()) {
                dept.setStatusType(null);
                hibSession.merge(dept);
            }
            
            for (DepartmentStatusType x: DepartmentStatusType.findAll()) {
                if (x.getOrd() > s.getOrd()) {
                    x.setOrd(x.getOrd()-1); 
                    hibSession.merge(x);
                }
            }
            hibSession.remove(s);
            
            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected void saveOrUpdate(StatusTypeInterface form) {
		Transaction tx = null;
		org.hibernate.Session hibSession = DepartmentStatusTypeDAO.getInstance().getSession();
        try {
            tx = hibSession.beginTransaction();
            
            DepartmentStatusType s = null;
            if (form.getUniqueId() != null)
            	s = DepartmentStatusTypeDAO.getInstance().get(form.getUniqueId());
            if (s == null)
            	s = new DepartmentStatusType();
            
            s.setReference(form.getReference());
            s.setLabel(form.getLabel());
            s.setApply(form.getApplyTo());
            if (s.getOrd()==null) s.setOrd(DepartmentStatusType.findAll().size());
            s.setStatus(form.getStatus());
            if (s.getUniqueId() != null)
            	hibSession.persist(s);
            else
            	hibSession.merge(s);
            form.setUniqueId(s.getUniqueId());

            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
}
