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

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public class AdminBackend {
	private static Logger sLog = Logger.getLogger(AdminBackend.class);

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
				
				SimpleEditInterface data = getTable(applicationContext, request.getType()).load(context, hibSession);
				
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
				
				getTable(applicationContext, request.getType()).save(request.getData(), context, hibSession);
				
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
	
	public static AdminTable getTable(ApplicationContext context, String type) {
		return (AdminTable) context.getBean("gwtAdminTable[type="+type+"]", AdminTable.class);
	}

}
