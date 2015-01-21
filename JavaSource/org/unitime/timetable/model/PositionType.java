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
import java.util.TreeSet;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BasePositionType;
import org.unitime.timetable.model.dao.PositionTypeDAO;




/**
 * @author Tomas Muller
 */
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
		if (uid == null){
			return(null);
		}
		
		PositionTypeDAO pdao = new PositionTypeDAO();
		List types = pdao.getSession().createCriteria(PositionType.class).add(Restrictions.eq("uniqueId", uid)).list();
		if(types != null && types.size() == 1){
			return((PositionType) types.get(0));
		} else
			return (null);
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
	public static TreeSet findAll() {
		return new TreeSet(
				(new PositionTypeDAO().findAll()));
	}
	
	/**
	 * 
	 */
	public static PositionType findByRef(String ref) throws Exception {
		if (ref == null){
			return(null);
		}
		
		PositionTypeDAO pdao = new PositionTypeDAO();
		List types = pdao.getSession().createCriteria(PositionType.class).add(Restrictions.eq("reference", ref)).list();
		if(types != null && types.size() == 1){
			return((PositionType) types.get(0));
		} else
			return (null);
	}

	/**
	 * Retrieves all position types in the database
	 * ordered by column label
	 * @return List of PositionType objects
	 */
    public static synchronized List<PositionType> getPositionTypeList() {
        return PositionTypeDAO.getInstance().findAll(Order.asc("sortOrder"));
    }

}
