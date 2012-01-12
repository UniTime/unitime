/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.solver.CommitedClassAssignmentProxy;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;


/**
 * @author Tomas Muller
 */
public class RemoteSolverProxyFactory implements InvocationHandler {
	private RemoteSolverServerProxy iProxy; 
	private String iPuid;
	private RemoteSolverProxy iRemoteSolverProxy;
	private CommitedClassAssignmentProxy iCommitedClassAssignmentProxy = null;
	
	private RemoteSolverProxyFactory(RemoteSolverServerProxy proxy, String puid) {
		iProxy = proxy;
		iPuid = puid;
		iCommitedClassAssignmentProxy = new CommitedClassAssignmentProxy();
	}
	
	public void setRemoteSolverProxy(RemoteSolverProxy proxy) {
		iRemoteSolverProxy = proxy;
	}
	
	public static RemoteSolverProxy create(RemoteSolverServerProxy proxy, String puid) {
		RemoteSolverProxyFactory handler = new RemoteSolverProxyFactory(proxy, puid);
		RemoteSolverProxy px = (RemoteSolverProxy)Proxy.newProxyInstance(
				RemoteSolverProxyFactory.class.getClassLoader(),
				new Class[] {RemoteSolverProxy.class},
				handler
				);
		handler.setRemoteSolverProxy(px);
		return px;
	}
	
	public String getPuid() { return iPuid; }
	public RemoteSolverServerProxy getServerProxy() { return iProxy; }
	
	public String getHost() {
		return iProxy.getAddress().getHostName()+":"+iProxy.getPort();
    }

	public String getHostLabel() {
		String hostName = iProxy.getAddress().getHostName();
		if (hostName.indexOf('.')>=0) hostName = hostName.substring(0,hostName.indexOf('.'));
		try {
			Integer.parseInt(hostName);
			//hostName is an IP address -> return that IP address 
			hostName = iProxy.getAddress().getHostName();
		} catch (NumberFormatException x) {}
		return hostName+":"+iProxy.getPort();
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

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			return getClass().getMethod(method.getName(),method.getParameterTypes()).invoke(this, args);
		} catch (NoSuchMethodException e) {}
		
		Object[] params = new Object[2*(args==null?0:args.length)+2];
    	params[0] = method.getName();
    	params[1] = iPuid;
    	if (args!=null) {
    		for (int i=0;i<args.length;i++) {
    			params[2*i+2] = method.getParameterTypes()[i];
    			params[2*i+3] = args[i];
    		}
    	}
    	return iProxy.query(params);
    }
}
