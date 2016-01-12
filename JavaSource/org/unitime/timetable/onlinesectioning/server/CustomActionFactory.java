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
package org.unitime.timetable.onlinesectioning.server;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;

/**
 * @author Tomas Muller
 */
public class CustomActionFactory extends SimpleActionFactory {

	@Override
	public <X extends OnlineSectioningAction> X createAction(Class<X> clazz) throws SectioningException {
		String implementation = ApplicationProperty.CustomOnlineSchedulingAction.value(clazz.getSimpleName());
		if (implementation != null && !implementation.isEmpty()) {
			try {
				return (X) Class.forName(implementation).newInstance();
			} catch (ClassNotFoundException e) {
				throw new SectioningException(e.getMessage(), e);
			} catch (InstantiationException e) {
				throw new SectioningException(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new SectioningException(e.getMessage(), e);
			}
		}
		return super.createAction(clazz);
	}

}
