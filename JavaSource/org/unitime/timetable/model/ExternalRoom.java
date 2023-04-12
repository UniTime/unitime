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



import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

import org.unitime.timetable.model.base.BaseExternalRoom;
import org.unitime.timetable.model.dao.ExternalRoomDAO;




/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "external_room")
public class ExternalRoom extends BaseExternalRoom {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExternalRoom () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExternalRoom (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static ExternalRoom findExternalRoomForSession(String externalUniqueId, Session session){
		return ExternalRoomDAO.getInstance().getSession()
				.createQuery("from ExternalRoom where externalUniqueId = :externalUniqueId and building.session.uniqueId = :sessionId", ExternalRoom.class)
				.setParameter("externalUniqueId", externalUniqueId)
				.setParameter("sessionId", session.getUniqueId())
				.setCacheable(true)
				.setMaxResults(1)
                .uniqueResult();
	}
    
    public static List findAll(Long sessionId) {
        return new ExternalRoomDAO().getSession().createQuery(
                "select r from ExternalRoom r where r.building.session.uniqueId=:sessionId").
                setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).
                setCacheable(true).
                list();
    }
    
    public static ExternalRoom findByBldgAbbvRoomNbr(Long sessionId, String bldgAbbv, String roomNbr) {
        return (ExternalRoom)new ExternalRoomDAO().getSession().createQuery(
                "select r from ExternalRoom r where r.building.session.uniqueId=:sessionId and " +
                "r.building.abbreviation=:bldgAbbv and r.roomNumber=:roomNbr").
                setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).
                setParameter("bldgAbbv", bldgAbbv, org.hibernate.type.StringType.INSTANCE).
                setParameter("roomNbr", roomNbr, org.hibernate.type.StringType.INSTANCE).
                uniqueResult();
    }
	
}
