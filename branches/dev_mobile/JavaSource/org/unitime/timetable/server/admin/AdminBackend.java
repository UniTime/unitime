/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.server.admin;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
