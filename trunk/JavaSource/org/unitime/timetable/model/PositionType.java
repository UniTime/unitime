/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import java.util.List;
import java.util.TreeSet;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BasePositionType;
import org.unitime.timetable.model.dao.PositionTypeDAO;




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
