/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.jgroups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class SolverContainerWrapper<T> implements SolverContainer<T> {
	private static Log sLog = LogFactory.getLog(SolverContainerWrapper.class);
	private RpcDispatcher iDispatcher;
	private RemoteSolverContainer<T> iContainer;
	private boolean iCheckLocal = true;

	public SolverContainerWrapper(RpcDispatcher dispatcher, RemoteSolverContainer<T> container, boolean checkLocal) {
		iDispatcher = dispatcher;
		iContainer = container;
		iCheckLocal = checkLocal;
	}

	@Override
	public Set<String> getSolvers() {
		Set<String> solvers = new HashSet<String>(iContainer.getSolvers());
		try {
			RspList<Set<String>> ret = iContainer.getDispatcher().callRemoteMethods(null, "getSolvers", new Object[] {}, new Class[] {}, SolverServerImplementation.sAllResponses);
			for (Rsp<Set<String>> rsp : ret) {
				solvers.addAll(rsp.getValue());
			}
		} catch (Exception e) {
			sLog.error("Failed to retrieve solvers: " + e.getMessage(), e);
		}
		return solvers;
	}

	@Override
	public T getSolver(String user) {
		try {
			if (iCheckLocal) {
				T solver = iContainer.getSolver(user);
				if (solver != null) return solver;				
			}

			RspList<Boolean> ret = iContainer.getDispatcher().callRemoteMethods(null, "hasSolver", new Object[] { user }, new Class[] { String.class }, SolverServerImplementation.sAllResponses);
			List<Address> senders = new ArrayList<Address>();
			for (Rsp<Boolean> rsp : ret) {
				if (rsp != null && rsp.getValue())
					senders.add(rsp.getSender());
			}
			if (senders.isEmpty())
				return null;
			else if (senders.size() == 1)
				return iContainer.createProxy(senders.get(0), user);
			else if (iContainer instanceof ReplicatedSolverContainer)
				return ((ReplicatedSolverContainer<T>)iContainer).createProxy(senders, user);
			else
				return iContainer.createProxy(ToolBox.random(senders), user);
		} catch (Exception e) {
			sLog.error("Failed to retrieve solver " + user + ": " + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public boolean hasSolver(String user) {
		try {
			if (iContainer.hasSolver(user)) return true;

			RspList<Boolean> ret = iContainer.getDispatcher().callRemoteMethods(null, "hasSolver", new Object[] { user }, new Class[] { String.class }, SolverServerImplementation.sAllResponses);
			for (Rsp<Boolean> rsp : ret)
				if (rsp.getValue()) return true;
			return false;
		} catch (Exception e) {
			sLog.error("Failed to check solver " + user + ": " + e.getMessage(), e);
		}
		return false;
	}

	@Override
	public T createSolver(String user, DataProperties config) {
		try {
			Address bestAddress = null;
			int bestUsage = 0;
			RspList<Boolean> ret = iDispatcher.callRemoteMethods(null, "isAvailable", new Object[] {}, new Class[] {}, SolverServerImplementation.sAllResponses);
			for (Rsp<Boolean> rsp : ret) {
				if (Boolean.TRUE.equals(rsp.getValue())) {
					int usage = iDispatcher.callRemoteMethod(rsp.getSender(), "getUsage", new Object[] {}, new Class[] {}, SolverServerImplementation.sFirstResponse);
					if (bestAddress == null || bestUsage > usage) {
						bestAddress = rsp.getSender();
		                bestUsage = usage;
		            }
				}
			}
				
			if (bestAddress == null)
				throw new RuntimeException("Not enough resources to create a solver instance, please try again later.");
			
			if (bestAddress.equals(iDispatcher.getChannel().getAddress()))
				return iContainer.createSolver(user, config);
			
			iContainer.getDispatcher().callRemoteMethod(bestAddress, "createRemoteSolver", new Object[] { user, config, iDispatcher.getChannel().getAddress() }, new Class[] { String.class, DataProperties.class, Address.class }, SolverServerImplementation.sFirstResponse);
			return iContainer.createProxy(bestAddress, user);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Failed to start the solver: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void unloadSolver(String user) {
		try {
			if (iContainer.hasSolver(user))
				iContainer.unloadSolver(user);
			
			RspList<Boolean> ret = iContainer.getDispatcher().callRemoteMethods(null, "hasSolver", new Object[] { user }, new Class[] { String.class }, SolverServerImplementation.sAllResponses);
			for (Rsp<Boolean> rsp : ret) {
				if (rsp.getValue())
					iContainer.getDispatcher().callRemoteMethod(rsp.getSender(), "unloadSolver", new Object[] { user }, new Class[] { String.class }, SolverServerImplementation.sFirstResponse);
			}
		} catch (Exception e) {
			sLog.error("Failed to unload solver " + user + ": " + e.getMessage(), e);
		}
	}

	@Override
	public int getUsage() {
		int usage = 0;
		try {
			RspList<Integer> ret = iContainer.getDispatcher().callRemoteMethods(null, "getUsage", new Object[] {}, new Class[] {}, SolverServerImplementation.sAllResponses);
			for (Rsp<Integer> rsp : ret)
				usage += rsp.getValue();
		} catch (Exception e) {
			sLog.error("Failed to check solver server usage: " + e.getMessage(), e);
		}
		return usage;
	}

	@Override
	public void start() {
		throw new RuntimeException("Method start is not supported on the solver container wrapper.");
	}

	@Override
	public void stop() {
		throw new RuntimeException("Method stop is not supported on the solver container wrapper.");
	}
	

}
