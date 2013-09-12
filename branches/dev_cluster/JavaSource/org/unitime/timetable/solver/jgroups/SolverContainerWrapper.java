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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.Message.Flag;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import net.sf.cpsolver.ifs.util.DataProperties;

public class SolverContainerWrapper<T> implements SolverContainer<T> {
	private static Log sLog = LogFactory.getLog(SolverContainerWrapper.class);
	private SolverServerImplementation iServer;
	private RemoteSolverContainer<T> iContainer;
	private RequestOptions iFirstResponse, iAllResponses;

	public SolverContainerWrapper(SolverServerImplementation server, RemoteSolverContainer<T> container) {
		iServer = server;
		iContainer = container;
		iFirstResponse = new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
		iAllResponses = new RequestOptions(ResponseMode.GET_ALL, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
	}

	@Override
	public Set<String> getSolvers() {
		Set<String> solvers = new HashSet<String>();
		try {
			RspList<Set<String>> ret = iContainer.getDispatcher().callRemoteMethods(null, "getSolvers", new Object[] {}, new Class[] {}, iAllResponses);
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
			RspList<Boolean> ret = iContainer.getDispatcher().callRemoteMethods(null, "hasSolver", new Object[] { user }, new Class[] { String.class }, iAllResponses);
			for (Rsp<Boolean> rsp : ret) {
				if (rsp.getValue())
					return iContainer.createProxy(rsp.getSender(), user);
			}
			return null;
		} catch (Exception e) {
			sLog.error("Failed to retrieve solver " + user + ": " + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public boolean hasSolver(String user) {
		try {
			RspList<Boolean> ret = iContainer.getDispatcher().callRemoteMethods(null, "hasSolver", new Object[] { user }, new Class[] { String.class }, iAllResponses);
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
			SolverServer bestServer = null;
			int bestUsage = 0;
			for (SolverServer server: iServer.getServers(true)) {
				int usage = server.getUsage();
				if (server.getAddress().equals(iServer.getAddress()))
					usage += 500;
				if (bestServer == null || bestUsage > usage) {
	                bestServer = server;
	                bestUsage = usage;
	            }
	        }
			if (bestServer == null)
				throw new RuntimeException("Not enough resources to create a solver instance, please try again later.");
			iContainer.getDispatcher().callRemoteMethod(bestServer.getAddress(), "createRemoteSolver", new Object[] { user, config, iServer.getAddress() }, new Class[] { String.class, DataProperties.class, Address.class }, iFirstResponse);
			return iContainer.createProxy(bestServer.getAddress(), user);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Failed to start the solver: " + e.getMessage(), e);
		}
	}

	@Override
	public int getUsage() {
		int usage = 0;
		try {
			RspList<Integer> ret = iContainer.getDispatcher().callRemoteMethods(null, "getUsage", new Object[] {}, new Class[] {}, iAllResponses);
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
