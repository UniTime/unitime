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

import java.lang.reflect.Method;


import org.cpsolver.ifs.util.DataProperties;
import org.jgroups.Address;
import org.jgroups.blocks.RpcDispatcher;

/**
 * @author Tomas Muller
 */
public interface RemoteSolverContainer<T> extends SolverContainer<T> {
	public boolean createRemoteSolver(String user, DataProperties config, Address caller);
	
	public RpcDispatcher getDispatcher();
	
	public Object dispatch(Address address, String user, Method method, Object[] args) throws Exception;
	
	public Object invoke(String method, String user, Class[] types, Object[] args) throws Exception;
	
	public T createProxy(Address address, String user);
}
