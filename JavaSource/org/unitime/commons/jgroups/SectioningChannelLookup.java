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