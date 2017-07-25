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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseEventServiceProvider;
import org.unitime.timetable.model.dao.EventServiceProviderDAO;

public class EventServiceProvider extends BaseEventServiceProvider {
	private static final long serialVersionUID = 1L;
	
	public static enum Option {
		;
				
		public int toggle() { return 1 << ordinal(); }
		
		public boolean contains(Integer options) {
			return options != null && (options & toggle()) != 0;
		}
	}

	public EventServiceProvider() {
		super();
	}
	
	public boolean hasOption(Option option) {
		return getOptions() != null && (getOptions() & option.toggle()) != 0;
	}
	
	public void addOption(Option option) {
		if (!hasOption(option)) setOptions((getOptions() == null ? 0 : getOptions()) + option.toggle());
	}

	public void removeOption(Option option) {
		if (hasOption(option)) setOptions(getOptions() - option.toggle());
	}
	
	public static EventServiceProvider getEventServiceProvider(String reference, org.hibernate.Session hibSession) {
		if (reference == null || reference.isEmpty()) return null;
		return (EventServiceProvider)hibSession.createQuery(
				"from EventServiceProvider where reference = :reference")
				.setString("reference", reference).setMaxResults(1).setCacheable(true).uniqueResult();
	}

	public static boolean hasOption(Option option, String reference, org.hibernate.Session hibSession) {
		EventServiceProvider provider = getEventServiceProvider(reference, hibSession);
		return provider != null && provider.hasOption(option);
	}
	
	public boolean isUsed() {
		if (((Number)EventServiceProviderDAO.getInstance().getSession().createQuery("select count(e) from Event e inner join e.requestedServices p where p.uniqueId = :providerId")
			.setLong("providerId", getUniqueId()).uniqueResult()).intValue() > 0) return true;
		return false;
	}


}
