/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseRoomGroup;
import org.unitime.timetable.model.dao.RoomGroupDAO;




public class RoomGroup extends BaseRoomGroup implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RoomGroup (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public RoomGroup (
		java.lang.Long uniqueId,
		java.lang.String name,
		java.lang.Boolean global,
		java.lang.Boolean defaultGroup) {

		super (
			uniqueId,
			name,
			global,
			defaultGroup);
	}

/*[CONSTRUCTOR MARKER END]*/
	
    /** Request attribute name for available room groups **/
    public static String GROUP_LIST_ATTR_NAME = "roomGroupsList";
	
	public static Collection getAllRoomGroups() throws HibernateException {
		return (new RoomGroupDAO()).findAll(Order.asc("name"));
	}
	
	public static Collection getAllGlobalRoomGroups() throws HibernateException {
		return (new RoomGroupDAO()).
		getSession().
		createCriteria(RoomGroup.class).
		add(Restrictions.eq("global",Boolean.TRUE)).
		addOrder(Order.asc("name")).
		setCacheable(true).
		list();
	}
	
	/**
	 * @param session
	 * @return Collection of RoomGroups for a session
	 * @throws HibernateException
	 */
	public static Collection getAllRoomGroupsForSession(Session session) throws HibernateException {
		RoomGroupDAO rgDao = new RoomGroupDAO();
		String query = "from RoomGroup rg where rg.session.uniqueId = " + session.getUniqueId().toString();
		query += " or rg.global = 1";
		return (rgDao.getQuery(query).list());
	}
	/**
	 * Gets the default global room group. Only one exists hence only one
	 * record is returned. If more than one exists then it returns the first 
	 * one in the list
\	 * @return Room Group if found, null otherwise
	 */
	public static RoomGroup getGlobalDefaultRoomGroup() {
	    String sql = "select rg " +
	    			   "from RoomGroup as rg " +
	    			   "where rg.global = true and rg.defaultGroup = true";
	    org.hibernate.Session hibSession = new RoomGroupDAO().getSession();
	    Query query = hibSession.createQuery(sql);
	    List l = query.list();
	    if (l!=null && l.size()>0)
	        return (RoomGroup) l.get(0);
	    else 
	        return null;	     
	}
	
	public static Collection getAllDepartmentRoomGroups(Department dept) throws HibernateException {
		if (dept==null) return null;
		return (new RoomGroupDAO()).
			getSession().
			createCriteria(RoomGroup.class).
			add(Restrictions.eq("global",Boolean.FALSE)).
			add(Restrictions.eq("department.uniqueId",dept.getUniqueId())).
			addOrder(Order.asc("name")).
			setCacheable(true).list();
	}
    
    public int compareTo(Object o) {
    	if (o==null || !(o instanceof RoomGroup)) return -1;
    	RoomGroup rg = (RoomGroup)o;
    	int cmp = (isGlobal().booleanValue() == rg.isGlobal().booleanValue() ? 0 : (isGlobal().booleanValue() ? -1 : 1));
    	if (cmp!=0) return cmp;
    	cmp = getName().compareTo(rg.getName());
    	if (cmp!=0) return cmp;
    	return getUniqueId().compareTo(rg.getUniqueId());
    }
    
	public boolean hasLocation (Location location) {
		return getRooms().contains(location);
	}
    
	public String htmlLabel() {
		return "<span "+
			(isGlobal().booleanValue()?"":"style='color:#"+getDepartment().getRoomSharingColor(null)+";font-weight:bold;' ")+
			"title='"+getName()+
			" ("+(isGlobal().booleanValue()?"global":getDepartment().isExternalManager().booleanValue()?getDepartment().getExternalMgrLabel():getDepartment().getName())+")'>"+
			getName() +
			"</span>";
	}
	
	public String getNameWithTitle() {
		return getName()+(isGlobal()!=null && isGlobal().booleanValue()?"":" (Department)");
	}
    
    public String toString() {
        return getName();
    }

	public Object clone(){
		RoomGroup newRoomGroup = new RoomGroup();
		newRoomGroup.setDefaultGroup(isDefaultGroup());
		newRoomGroup.setDepartment(getDepartment());
		newRoomGroup.setDescription(getDescription());
		newRoomGroup.setGlobal(isGlobal());
		newRoomGroup.setName(getName());
		newRoomGroup.setSession(getSession());
		return(newRoomGroup);
	}

	public RoomGroup findSameRoomGroupInSession(Session session) {
		if(session == null){
			return(null);
		}
		Department d = getDepartment().findSameDepartmentInSession(session);
		if (d != null){
			List l = (new RoomGroupDAO()).
				getSession().
				createCriteria(RoomGroup.class).
				add(Restrictions.eq("global",Boolean.FALSE)).
				add(Restrictions.eq("department.uniqueId", d.getUniqueId())).
				add(Restrictions.eq("name", getName())).
				addOrder(Order.asc("name")).
				setCacheable(true).list();
			if (l.size() == 1){
				return((RoomGroup) l.get(0));
			}
		}
		return null;
	}
    
    public String getAbbv() {
        if (super.getAbbv()!=null && super.getAbbv().trim().length()>0) return super.getAbbv();
        StringBuffer sb = new StringBuffer();
        for (StringTokenizer stk = new StringTokenizer(getName()," ");stk.hasMoreTokens();) {
            String word = stk.nextToken();
            if ("and".equalsIgnoreCase(word))
                sb.append("&amp;");
            else if (word.replaceAll("[a-zA-Z\\.]*", "").length()==0) {
                for (int i=0;i<word.length();i++) {
                    if (i==0)
                        sb.append(word.substring(i,i+1).toUpperCase());
                    else if ((i==1 && word.length()>3) || (word.charAt(i)>='A' && word.charAt(i)<='Z'))
                        sb.append(word.charAt(i));
                }
            } else
                sb.append(word);
        }
        return sb.toString();
    }
    
}