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

import java.util.List;

import org.unitime.timetable.model.base.BaseSponsoringOrganization;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;



/**
 * @author Zuzana Mullerova, Tomas Muller
 */
public class SponsoringOrganization extends BaseSponsoringOrganization implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SponsoringOrganization () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SponsoringOrganization (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int compareTo(Object o) {
		SponsoringOrganization so1 = (SponsoringOrganization) this;
		SponsoringOrganization so2 = (SponsoringOrganization) o;
    	int cmp = so1.getName().compareTo(so2.getName());
    	if (cmp!=0) return cmp;
    	else return so1.getUniqueId().compareTo(so2.getUniqueId());
	}

    public static List<SponsoringOrganization> findAll() {
        return new SponsoringOrganizationDAO().getSession().createQuery(
                "select so from SponsoringOrganization so order by so.name"
                ).setCacheable(true).list();
    }
    
}