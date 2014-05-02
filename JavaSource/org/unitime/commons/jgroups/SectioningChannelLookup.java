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

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.fork.ForkChannel;
import org.jgroups.protocols.FRAG2;
import org.jgroups.protocols.RSVP;
import org.jgroups.stack.ProtocolStack;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
public class SectioningChannelLookup extends UniTimeChannelLookup {
	
	@Override
	public Channel getJGroupsChannel(Properties p) {
		try {
			if (ApplicationProperty.OnlineSchedulingClusterForkChannel.isTrue()) {
				return new ForkChannel(super.getJGroupsChannel(p), "forked-stack", "sectioning-channel",
						true, ProtocolStack.ABOVE, FRAG2.class,
						new RSVP().setValue("timeout", 60000).setValue("resend_interval", 500).setValue("ack_on_delivery", false));
			} else {
				return new JChannel(JGroupsUtils.getConfigurator(ApplicationProperty.OnlineSchedulingClusterConfiguration.value()));
			}
		} catch (Exception e) {
			Debug.error(e.getMessage(), e);
			return null;
		}
	}
}