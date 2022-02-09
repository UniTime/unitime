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
package org.unitime.timetable.server.classinstructors;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.unitime.commons.Debug;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.PageName;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.Record;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class AssignClassInstructorsBackend {
	private static Logger sLog = Logger.getLogger(AssignClassInstructorsBackend.class);

	@GwtRpcImplements(AssignClassInstructorsInterface.GetPageNameRpcRequest.class)
	public static class PageNameBackend implements GwtRpcImplementation<AssignClassInstructorsInterface.GetPageNameRpcRequest, AssignClassInstructorsInterface.PageName> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public PageName execute(AssignClassInstructorsInterface.GetPageNameRpcRequest request, SessionContext context) {
			Debug.info("In PageName execute(AssignClassInstructorsInterface.GetPageNameRpcRequest request");
			return getTable(applicationContext).name();
		}
	}
	
	@GwtRpcImplements(AssignClassInstructorsInterface.LoadDataRpcRequest.class)
	public static class LoadDataBackend implements GwtRpcImplementation<AssignClassInstructorsInterface.LoadDataRpcRequest, AssignClassInstructorsInterface> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public AssignClassInstructorsInterface execute(AssignClassInstructorsInterface.LoadDataRpcRequest request, SessionContext context) {
			@SuppressWarnings("rawtypes")
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				AssignClassInstructorsInterface data = null;
				AssignClassInstructorsTable at = getTable(applicationContext);
				data = at.load(request.getConfigIdStr(), context, hibSession);
				
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
	
	@GwtRpcImplements(AssignClassInstructorsInterface.SaveDataRpcRequest.class)
	public static class SaveDataBackend implements GwtRpcImplementation<AssignClassInstructorsInterface.SaveDataRpcRequest, AssignClassInstructorsInterface> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public AssignClassInstructorsInterface execute(AssignClassInstructorsInterface.SaveDataRpcRequest request, SessionContext context) {
			Debug.info("In AssignClassInstructorsInterface execute(AssignClassInstructorsInterface.SaveDataRpcRequest request, ");
			@SuppressWarnings("rawtypes")
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				AssignClassInstructorsInterface data = request.getData();
				
				AssignClassInstructorsTable at = getTable(applicationContext);
				at.save(request.getData(), context, hibSession);
				
				if (data.isSaveSuccessful()) {
					hibSession.flush();
					tx.commit(); tx = null;
	
					for (Iterator<Record> i = data.getRecords().iterator(); i.hasNext(); )
						if (i.next().getUniqueId() == null) i.remove();
				}
				
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
	
	@GwtRpcImplements(AssignClassInstructorsInterface.SaveDataGoToPreviousRpcRequest.class)
	public static class SaveDataGoToPreviousRpcRequest implements GwtRpcImplementation<AssignClassInstructorsInterface.SaveDataGoToPreviousRpcRequest, AssignClassInstructorsInterface> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public AssignClassInstructorsInterface execute(AssignClassInstructorsInterface.SaveDataGoToPreviousRpcRequest request, SessionContext context) {
			Debug.info("In AssignClassInstructorsInterface execute(AssignClassInstructorsInterface.SaveDataGoToPreviousRpcRequest request, ");
			@SuppressWarnings("rawtypes")
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				AssignClassInstructorsInterface data = request.getData();
				
				AssignClassInstructorsTable at = getTable(applicationContext);
				at.save(request.getData(), context, hibSession);
				
				if (data.isSaveSuccessful()) {
					hibSession.flush();
					tx.commit(); tx = null;
					tx = hibSession.beginTransaction();
					data = at.load(data.getPreviousConfigId().toString(), context, hibSession);
					hibSession.flush();
					tx.commit(); tx = null;
				}				
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
	
	@GwtRpcImplements(AssignClassInstructorsInterface.SaveDataGoToNextRpcRequest.class)
	public static class SaveDataGoToNextRpcRequest implements GwtRpcImplementation<AssignClassInstructorsInterface.SaveDataGoToNextRpcRequest, AssignClassInstructorsInterface> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public AssignClassInstructorsInterface execute(AssignClassInstructorsInterface.SaveDataGoToNextRpcRequest request, SessionContext context) {
			Debug.info("In AssignClassInstructorsInterface execute(AssignClassInstructorsInterface.SaveDataGoToPreviousRpcRequest request, ");
			@SuppressWarnings("rawtypes")
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				AssignClassInstructorsInterface data = request.getData();
				
				AssignClassInstructorsTable at = getTable(applicationContext);
				at.save(request.getData(), context, hibSession);
				
				if (data.isSaveSuccessful()) {
					hibSession.flush();
					tx.commit(); tx = null;
					data = at.load(data.getNextConfigId().toString(), context, hibSession);
				}				
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


	@GwtRpcImplements(AssignClassInstructorsInterface.RemoveAllClassInstructorsDataRpcRequest.class)
	public static class RemoveAllClassInstructorsDataBackend implements GwtRpcImplementation<AssignClassInstructorsInterface.RemoveAllClassInstructorsDataRpcRequest, AssignClassInstructorsInterface> {
		@Autowired ApplicationContext applicationContext;

		@Override
		public AssignClassInstructorsInterface execute(AssignClassInstructorsInterface.RemoveAllClassInstructorsDataRpcRequest request, SessionContext context) {
			@SuppressWarnings("rawtypes")
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				AssignClassInstructorsInterface data = request.getData();
				
				AssignClassInstructorsTable at = getTable(applicationContext);
				at.removeAllInstructors(request.getData(), context, hibSession);
				
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

	
	public static AssignClassInstructorsTable getTable(ApplicationContext context) {
	
		return (AssignClassInstructorsTable) context.getBean("gwtAssignClassInstrs", AssignClassInstructorsTable.class);
	}

}
