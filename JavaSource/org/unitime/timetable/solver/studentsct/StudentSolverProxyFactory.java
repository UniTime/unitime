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
package org.unitime.timetable.solver.studentsct;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.unitime.timetable.solver.remote.RemoteSolverServerProxy;

/**
 * @author Tomas Muller
 */
public class StudentSolverProxyFactory implements InvocationHandler {
	private RemoteSolverServerProxy iProxy; 
	private String iPuid = null;
	
	private StudentSolverProxyFactory(RemoteSolverServerProxy proxy, String puid) {
		iProxy = proxy;
		iPuid = puid;
	}
	
	public static StudentSolverProxy create(RemoteSolverServerProxy proxy, String puid) {
	    StudentSolverProxyFactory handler = new StudentSolverProxyFactory(proxy, puid);
	    RemoteStudentSolverProxy px = (RemoteStudentSolverProxy)Proxy.newProxyInstance(
	            StudentSolverProxyFactory.class.getClassLoader(),
				new Class[] {RemoteStudentSolverProxy.class},
				handler
				);
		return px;
	}
	
	public RemoteSolverServerProxy getServerProxy() { return iProxy; }
	
	public String getHost() {
		return iProxy.getAddress().getHostName()+":"+iProxy.getPort();
    }
	
	public String getPuid() {
	    return iPuid;
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
	
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			return getClass().getMethod(method.getName(),method.getParameterTypes()).invoke(this, args);
		} catch (NoSuchMethodException e) {}
		
		Object[] params = new Object[2*(args==null?0:args.length)+3];
    	params[0] = method.getName();
    	params[1] = "SCT";
    	params[2] = iPuid;
    	if (args!=null) {
    		for (int i=0;i<args.length;i++) {
    			params[2*i+3] = method.getParameterTypes()[i];
    			params[2*i+4] = args[i];
    		}
    	}
    	return iProxy.query(params);
    }
}
