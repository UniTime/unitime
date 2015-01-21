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
package org.unitime.timetable.solver.jgroups;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.SuspectedException;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.CommitedClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.solver.ui.TimetableInfoFileProxy;
import org.unitime.timetable.solver.ui.TimetableInfoUtil;

/**
 * @author Tomas Muller
 */
public class CourseSolverContainerRemote extends CourseSolverContainer implements RemoteSolverContainer<SolverProxy> {
	private static Log sLog = LogFactory.getLog(CourseSolverContainerRemote.class);
	
	private RpcDispatcher iDispatcher;
		
	public CourseSolverContainerRemote(JChannel channel, short scope) {
		iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
	}
	
	@Override
	public RpcDispatcher getDispatcher() { return iDispatcher; }
	
	@Override
	public boolean createRemoteSolver(String user, DataProperties config, Address caller) {
		TimetableSolver solver = (TimetableSolver)super.createSolver(user, config);
		solver.setFileProxy(new FileProxy(caller));
        return true;
	}
	
	@Override
	public Object invoke(String method, String user, Class[] types, Object[] args) throws Exception {
		try {
			SolverProxy solver = iCourseSolvers.get(user);
			if ("exists".equals(method) && types.length == 0)
				return solver != null;
			if (solver == null)
				throw new Exception("Solver " + user + " does not exist.");
			return solver.getClass().getMethod(method, types).invoke(solver, args);
		} catch (InvocationTargetException e) {
			throw (Exception)e.getTargetException();
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public Object dispatch(Address address, String user, Method method, Object[] args) throws Exception {
		try {
			return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), user, method.getParameterTypes(), args }, new Class[] { String.class, String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
		} catch (InvocationTargetException e) {
			throw (Exception)e.getTargetException();
		} catch (Exception e) {
			if ("exists".equals(method.getName()) && e instanceof SuspectedException) return false;
			sLog.error("Excution of " + method.getName() + " on solver " + user + " failed: " + e.getMessage(), e);
			throw e;
		}
	}
	
	public void saveToFile(String name, TimetableInfo info) throws Exception {
		TimetableInfoUtil.getInstance().saveToFile(name, info);
	}
	
	public TimetableInfo loadFromFile(String name) throws Exception  {
		return TimetableInfoUtil.getInstance().loadFromFile(name);
	}
	
	public void deleteFile(String name) throws Exception {
		TimetableInfoUtil.getInstance().deleteFile(name);
	}

	
    private class FileProxy implements TimetableInfoFileProxy {
    	private Address iAddress;
    	private FileProxy(Address address) {
    		iAddress = address;
    	}
    	
    	@Override
    	public void saveToFile(String name, TimetableInfo info) throws Exception {
    		iDispatcher.callRemoteMethod(iAddress, "saveToFile", new Object[] { name, info } , new Class[] { String.class, TimetableInfo.class }, SolverServerImplementation.sFirstResponse);
    	}
    	
    	@Override
    	public TimetableInfo loadFromFile(String name) throws Exception {
    		return iDispatcher.callRemoteMethod(iAddress, "loadFromFile", new Object[] { name } , new Class[] { String.class }, SolverServerImplementation.sFirstResponse);
    	}
    	@Override
        public void deleteFile(String name) throws Exception {
    		iDispatcher.callRemoteMethod(iAddress, "deleteFile", new Object[] { name } , new Class[] { String.class }, SolverServerImplementation.sFirstResponse);
        }
    }
    

	@Override
	public SolverProxy createProxy(Address address, String user) {
		SolverInvocationHandler handler = new SolverInvocationHandler(address, user);
		SolverProxy px = (SolverProxy)Proxy.newProxyInstance(
				SolverProxy.class.getClassLoader(),
				new Class[] {SolverProxy.class, RemoteSolver.class, },
				handler);
		handler.setRemoteSolverProxy(px);
    	return px;
	}
    
    public class SolverInvocationHandler implements InvocationHandler {
    	private Address iAddress;
    	private String iUser;
    	private SolverProxy iRemoteSolverProxy;
    	private CommitedClassAssignmentProxy iCommitedClassAssignmentProxy = null;
    	
    	private SolverInvocationHandler(Address address, String user) {
    		iAddress = address;
    		iUser = user;
    		iCommitedClassAssignmentProxy = new CommitedClassAssignmentProxy();
    	}
    	
    	private void setRemoteSolverProxy(SolverProxy proxy) { iRemoteSolverProxy = proxy; }
    	
    	public String getHost() {
    		return iAddress.toString();
    	}
    	
    	public String getUser() {
    		return iUser;
    	}
    	
    	public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) throws Exception {
    		Department dept = clazz.getManagingDept();
    		if (dept!=null && iRemoteSolverProxy.getDepartmentIds().contains(dept.getUniqueId()))
    			return iRemoteSolverProxy.getAssignmentInfo(clazz.getUniqueId());
    		return iCommitedClassAssignmentProxy.getAssignmentInfo(clazz);
    	}

    	public Assignment getAssignment(Class_ clazz) throws Exception {
    		Department dept = clazz.getManagingDept();
    		if (dept!=null && iRemoteSolverProxy.getDepartmentIds().contains(dept.getUniqueId()))
    			return iRemoteSolverProxy.getAssignment(clazz.getUniqueId());
    		return iCommitedClassAssignmentProxy.getAssignment(clazz);
    	}

        public Hashtable getAssignmentTable(Collection classesOrClassIds) throws Exception {
            Set deptIds = iRemoteSolverProxy.getDepartmentIds();
            Hashtable assignments = new Hashtable();
            Vector solverClassesOrClassIds = new Vector(classesOrClassIds.size());
            for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
                Object classOrClassId = i.next();
                if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
                Class_ clazz = (classOrClassId instanceof Class_ ? (Class_)classOrClassId : (new Class_DAO()).get((Long)classOrClassId));
                if (clazz.getManagingDept()==null || !deptIds.contains(clazz.getManagingDept().getUniqueId())) {
                    Assignment assignment = iCommitedClassAssignmentProxy.getAssignment(clazz);
                    if (assignment!=null)
                        assignments.put(clazz.getUniqueId(), assignment);
                } else {
                    solverClassesOrClassIds.add(clazz.getUniqueId());
                }
            }
            if (!solverClassesOrClassIds.isEmpty())
                assignments.putAll(iRemoteSolverProxy.getAssignmentTable2(solverClassesOrClassIds));
            return assignments;
        }
        
        public Hashtable getAssignmentInfoTable(Collection classesOrClassIds) throws Exception {
            Set deptIds = iRemoteSolverProxy.getDepartmentIds();
            Hashtable infos = new Hashtable();
            Vector solverClassesOrClassIds = new Vector(classesOrClassIds.size());
            for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
                Object classOrClassId = i.next();
                if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
                Class_ clazz = (classOrClassId instanceof Class_ ? (Class_)classOrClassId : (new Class_DAO()).get((Long)classOrClassId));
                if (clazz.getManagingDept()==null || !deptIds.contains(clazz.getManagingDept().getUniqueId())) {
                    AssignmentPreferenceInfo info = iCommitedClassAssignmentProxy.getAssignmentInfo(clazz);
                    if (info!=null)
                        infos.put(clazz.getUniqueId(), info);
                } else {
                    solverClassesOrClassIds.add(clazz.getUniqueId());
                }
            }
            if (!solverClassesOrClassIds.isEmpty())
                infos.putAll(iRemoteSolverProxy.getAssignmentInfoTable2(solverClassesOrClassIds));
            return infos;
        }
    	
    	@Override
    	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    		try {
    			return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
    		} catch (NoSuchMethodException e) {}
    		return dispatch(iAddress, iUser, method, args);
        }
    }
}
