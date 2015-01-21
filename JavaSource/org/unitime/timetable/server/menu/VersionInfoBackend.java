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
package org.unitime.timetable.server.menu;

import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.MenuInterface.VersionInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.VersionInfoRpcRequest;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(VersionInfoRpcRequest.class)
public class VersionInfoBackend implements GwtRpcImplementation<VersionInfoRpcRequest, VersionInfoInterface> {
	@Override
	public VersionInfoInterface execute(VersionInfoRpcRequest request, SessionContext context) {
		VersionInfoInterface ret = new VersionInfoInterface();
		ret.setVersion(Constants.getVersion());
		ret.setBuildNumber(Constants.getBuildNumber());
		ret.setReleaseDate(Constants.getReleaseDate());
		return ret;
	}

}
