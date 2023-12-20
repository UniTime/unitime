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
package org.unitime.timetable.server.admin;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.server.admin.AdminTable.HasFilter;
import org.unitime.timetable.server.admin.AdminTable.HasLazyFields;

/**
 * @author Tomas Muller
 */
public class AdminBackend {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Log sLog = LogFactory.getLog(AdminBackend.class);

	@GwtRpcImplements(SimpleEditInterface.GetPageNameRpcRequest.class)
	public static class PageNameBackend implements GwtRpcImplementation<SimpleEditInterface.GetPageNameRpcRequest, SimpleEditInterface.PageName> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public PageName execute(SimpleEditInterface.GetPageNameRpcRequest request, SessionContext context) {
			return getTable(applicationContext, request.getType()).name();
		}
	}
	
	@GwtRpcImplements(SimpleEditInterface.LoadDataRpcRequest.class)
	public static class LoadDataBackend implements GwtRpcImplementation<SimpleEditInterface.LoadDataRpcRequest, SimpleEditInterface> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public SimpleEditInterface execute(SimpleEditInterface.LoadDataRpcRequest request, SessionContext context) {
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				SimpleEditInterface data = null;
				AdminTable at = getTable(applicationContext, request.getType());
				if (request.getFilter() != null && at instanceof HasFilter) {
					data = ((HasFilter)at).load(request.getFilter(), context, hibSession);
				} else {
					data = at.load(context, hibSession);
				}
				
				if (!data.hasConfirmDelete() && CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.ConfirmationDialogs)))
					data.setConfirmDelete(MESSAGES.confirmDeleteItem(at.name().singular().toLowerCase()));
				
				hibSession.flush();
				tx.commit(); tx = null;

				return data;
			} catch (PageAccessException e) {
				throw e;
			} catch (GwtRpcException e) {
				throw e;
			} catch (AccessDeniedException e) {
				throw new PageAccessException(e.getMessage(), e);
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new GwtRpcException(e.getMessage(), e);
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
			}
		}
	}
	
	@GwtRpcImplements(SimpleEditInterface.SaveDataRpcRequest.class)
	public static class SaveDataBackend implements GwtRpcImplementation<SimpleEditInterface.SaveDataRpcRequest, SimpleEditInterface> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public SimpleEditInterface execute(SimpleEditInterface.SaveDataRpcRequest request, SessionContext context) {
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				SimpleEditInterface data = request.getData();
				
				AdminTable at = getTable(applicationContext, request.getType());
				if (request.getFilter() != null && at instanceof HasFilter) {
					((HasFilter)at).save(request.getFilter(), request.getData(), context, hibSession);
				} else {
					at.save(request.getData(), context, hibSession);
				}
				
				hibSession.flush();
				tx.commit(); tx = null;

				for (Iterator<Record> i = data.getRecords().iterator(); i.hasNext(); )
					if (i.next().getUniqueId() == null) i.remove();
				
				return data;
			} catch (PageAccessException e) {
				throw e;
			} catch (GwtRpcException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new GwtRpcException(e.getMessage(), e);
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
			}
		}
	}
	
	@GwtRpcImplements(SimpleEditInterface.SaveRecordRpcRequest.class)
	public static class SaveRecordBackend implements GwtRpcImplementation<SimpleEditInterface.SaveRecordRpcRequest, SimpleEditInterface.Record> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public SimpleEditInterface.Record execute(SimpleEditInterface.SaveRecordRpcRequest request, SessionContext context) {
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				if (request.getRecord().getUniqueId() == null)
					getTable(applicationContext, request.getType()).save(request.getRecord(), context, hibSession);
				else
					getTable(applicationContext, request.getType()).update(request.getRecord(), context, hibSession);
				
				hibSession.flush();
				tx.commit(); tx = null;
				
				return request.getRecord();
			} catch (PageAccessException e) {
				throw e;
			} catch (GwtRpcException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new GwtRpcException(e.getMessage(), e);
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
			}
		}
	}
	
	@GwtRpcImplements(SimpleEditInterface.DeleteRecordRpcRequest.class)
	public static class DeleteRecordBackend implements GwtRpcImplementation<SimpleEditInterface.DeleteRecordRpcRequest, SimpleEditInterface.Record> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public SimpleEditInterface.Record execute(SimpleEditInterface.DeleteRecordRpcRequest request, SessionContext context) {
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				getTable(applicationContext, request.getType()).delete(request.getRecord(), context, hibSession);
				
				hibSession.flush();
				tx.commit(); tx = null;
				
				return request.getRecord();
			} catch (PageAccessException e) {
				throw e;
			} catch (GwtRpcException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new GwtRpcException(e.getMessage(), e);
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
			}
		}
	}
	
	@GwtRpcImplements(SimpleEditInterface.LoadRecordRpcRequest.class)
	public static class LoadRecordBackend implements GwtRpcImplementation<SimpleEditInterface.LoadRecordRpcRequest, SimpleEditInterface.Record> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public SimpleEditInterface.Record execute(SimpleEditInterface.LoadRecordRpcRequest request, SessionContext context) {
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				AdminTable at = getTable(applicationContext, request.getType());
				if (at instanceof HasLazyFields)
					((HasLazyFields)at).load(request.getRecord(), context, hibSession);
				
				hibSession.flush();
				tx.commit(); tx = null;
				
				return request.getRecord();
			} catch (PageAccessException e) {
				throw e;
			} catch (GwtRpcException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new GwtRpcException(e.getMessage(), e);
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
			}
		}
	}
	
	@GwtRpcImplements(SimpleEditInterface.GetFilterRpcRequest.class)
	public static class GetFilterBackend implements GwtRpcImplementation<SimpleEditInterface.GetFilterRpcRequest, SimpleEditInterface.Filter> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public SimpleEditInterface.Filter execute(SimpleEditInterface.GetFilterRpcRequest request, SessionContext context) {
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			SimpleEditInterface.Filter ret = null;
			try {
				tx = hibSession.beginTransaction();
				
				AdminTable at = getTable(applicationContext, request.getType());
				if (at instanceof HasFilter)
					ret = ((HasFilter)at).getFilter(context, hibSession);
				
				hibSession.flush();
				tx.commit(); tx = null;
				
				return ret;
			} catch (PageAccessException e) {
				throw e;
			} catch (GwtRpcException e) {
				throw e;
			} catch (Exception e) {
				sLog.error(e.getMessage(), e);
				throw new GwtRpcException(e.getMessage(), e);
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
			}
		}
	}
	
	public static AdminTable getTable(ApplicationContext context, String type) {
		return (AdminTable) context.getBean("gwtAdminTable[type="+type+"]", AdminTable.class);
	}

}
