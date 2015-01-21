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

import org.hibernate.FlushMode;
import org.unitime.timetable.model.base.BaseEventContact;
import org.unitime.timetable.model.dao.EventContactDAO;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;



/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
public class EventContact extends BaseEventContact implements NameInterface {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public EventContact () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public EventContact (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static EventContact findByExternalUniqueId(String externalUniqueId) {
	    return (EventContact)new EventContactDAO().getSession().
	        createQuery("select c from EventContact c where c.externalUniqueId=:externalUniqueId").
	        setString("externalUniqueId", externalUniqueId).
	        setFlushMode(FlushMode.MANUAL).
	        uniqueResult();
	}

	public static EventContact findByEmail(String email) {
	    List<EventContact> ec = (List<EventContact>)new EventContactDAO().getSession().
	        createQuery("select c from EventContact c where c.emailAddress=:emailAddress").
	        setString("emailAddress", email).list();
	    if (ec.isEmpty()) return null; 
	    else return ec.get(0);
	}
	
    public String getShortName() {
        StringBuffer sb = new StringBuffer();
        if (getFirstName()!=null && getFirstName().length()>0) {
            sb.append(getFirstName().substring(0,1).toUpperCase());
            sb.append(". ");
        }
        if (getLastName()!=null && getLastName().length()>0) {
            sb.append(getLastName().substring(0,1).toUpperCase());
            sb.append(getLastName().substring(1,Math.min(10,getLastName().length())).toLowerCase().trim());
        }
        return sb.toString();
    }

    public String getName() {
        return ((getLastName() == null ? "" : getLastName().trim()) + ", "+ 
                (getFirstName() == null ? "" : getFirstName().trim()) + " "+
                (getMiddleName() == null ? "" : getMiddleName().trim()));
    }
    
    public String getName(String nameFormat) {
    	return NameFormat.fromReference(nameFormat).format(this);
    }
}
