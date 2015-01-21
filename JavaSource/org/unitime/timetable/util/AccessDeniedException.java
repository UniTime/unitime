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
package org.unitime.timetable.util;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.SecurityMessages;

/**
 * @author Tomas Muller
 */
public class AccessDeniedException extends org.springframework.security.access.AccessDeniedException {
	private static final long serialVersionUID = 2565632362237022274L;
	private static final SecurityMessages MSG = Localization.create(SecurityMessages.class);

	public AccessDeniedException(String message) {
		super(message);
	}
	
	public AccessDeniedException() {
		super(MSG.accessDenied());
	}
}
