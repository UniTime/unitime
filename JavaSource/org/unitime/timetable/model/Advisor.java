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

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.PersonInterface.LookupRequest;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.model.base.BaseAdvisor;
import org.unitime.timetable.server.lookup.PeopleLookupBackend;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;

public class Advisor extends BaseAdvisor implements NameInterface, Comparable<Advisor> {
	private static final long serialVersionUID = 1L;

	public Advisor() {
		super();
	}
	
	public boolean hasName() {
		return getLastName() != null && !getLastName().isEmpty();
	}
	
	public String getName(String instructorNameFormat) {
    	return NameFormat.fromReference(instructorNameFormat).format(this);
    }
    
    public int compareTo(Advisor advisor) {
        int cmp = NameFormat.LAST_FIRST.format(this).compareTo(NameFormat.LAST_FIRST.format(advisor));
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(advisor.getUniqueId() == null ? -1 : advisor.getUniqueId());
    }
    
    public boolean lookupDetails() {
    	if (getExternalUniqueId() == null) return false;
    	ExternalUidTranslation translation = null;
        if (ApplicationProperty.ExternalUserIdTranslation.value()!=null) {
            try {
                translation = (ExternalUidTranslation)Class.forName(ApplicationProperty.ExternalUserIdTranslation.value()).getConstructor().newInstance();
            } catch (Exception e) {}
        }
        String query = getExternalUniqueId();
        if (translation != null) query = translation.translate(getExternalUniqueId(), Source.Staff, Source.LDAP);
        
        LookupRequest request = new LookupRequest(query, "mustHaveExternalId,session=" + getSession().getUniqueId() + ",source=ldap:instructors:staff");
        GwtRpcResponseList<PersonInterface> response = new PeopleLookupBackend().execute(request, null);
        if (response != null) {
        	for (PersonInterface person: response)
        		if (getExternalUniqueId().equals(person.getId())) {
        			setFirstName(person.getFirstName());
        			setLastName(person.getLastName());
        			setMiddleName(person.getMiddleName());
        			setAcademicTitle(person.getAcademicTitle());
        			setEmail(person.getEmail());
        			return true;
        		}
        }
        if (!query.equals(getExternalUniqueId()))
        	setLastName(query);
        return false;
    }

}
