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
package org.unitime.commons.jgroups;

import java.util.Properties;

import org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
public class UniTimeChannelLookup implements JGroupsChannelLookup {
	private static JChannel sChannel = null;

	@Override
	public Channel getJGroupsChannel(Properties p) {
		try {
			if (sChannel == null)
				sChannel = new JChannel(JGroupsUtils.getConfigurator(ApplicationProperty.SolverClusterConfiguration.value()));
			return sChannel;
		} catch (Exception e) {
			Debug.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public boolean shouldConnect() {
		return true;
	}

	@Override
	public boolean shouldDisconnect() {
		return true;
	}

	@Override
	public boolean shouldClose() {
		return true;
	}
}
