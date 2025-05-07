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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.model.base.BasePositionType;
import org.unitime.timetable.model.dao.PositionTypeDAO;


/**
 * @author Tomas Muller
 */
@Entity
@Table(name = "position_type")
public class PositionType extends BasePositionType implements Comparable{
	private static final long serialVersionUID = 1L;
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public PositionType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public PositionType (Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
    /** Request attribute name for available position typess **/
    public static String POSTYPE_ATTR_NAME = "posTypeList";  
    
	/**
	 * 
	 */
	public static PositionType findById(Long uid) throws Exception {
		if (uid == null) return null;
		return PositionTypeDAO.getInstance().getSession()
				.createQuery("from PositionType where uniqueId = :uid", PositionType.class)
				.setParameter("uid", uid)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
        if(o == null | !(o instanceof PositionType))
            return(-1);
        PositionType p = (PositionType) o;
        if (this.getUniqueId() != null && p.getUniqueId() != null){
        	if (this.getUniqueId().equals(p.getUniqueId())){
        		return(0);
        	} else {
        		return (this.getLabel().compareTo(p.getLabel()));
        	}
        } else {
        	return (-1);
        }
	}
	
	/**
	 * 
	 * @return
	 */
	public static TreeSet<PositionType> findAll() {
		return new TreeSet<PositionType>(
				(PositionTypeDAO.getInstance().findAll()));
	}
	
	/**
	 * 
	 */
	public static PositionType findByRef(String ref) throws Exception {
		if (ref == null) return null;
		return PositionTypeDAO.getInstance().getSession()
				.createQuery("from PositionType where reference = :ref", PositionType.class)
				.setParameter("ref", ref)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
	}

	/**
	 * Retrieves all position types in the database
	 * ordered by column label
	 * @return List of PositionType objects
	 */
	@Transient
    public static synchronized List<PositionType> getPositionTypeList() {
		return PositionTypeDAO.getInstance().getSession().createQuery(
				"from PositionType order by sortOrder", PositionType.class)
				.setCacheable(true).list();
    }
}
