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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.PageNames;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameRpcRequest;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(PageNameRpcRequest.class)
public class PageNameBackend implements GwtRpcImplementation<PageNameRpcRequest, PageNameInterface> {
	private static PageNames sPageNames = Localization.create(PageNames.class);
	
	@Override
	public PageNameInterface execute(PageNameRpcRequest request, SessionContext context) {
		String name = request.getName().trim().replace(' ', '_').replace("(", "").replace(")", "").replace(':', '_');
		PageNameInterface ret = new PageNameInterface();
		if (ApplicationProperty.PageHelpEnabled.isTrue() && ApplicationProperty.PageHelpUrl.value() != null)
			ret.setHelpUrl(ApplicationProperty.PageHelpUrl.value() + name);
		ret.setName(sPageNames.translateMessage(name, request.getName()));
		return ret;
	}

}
