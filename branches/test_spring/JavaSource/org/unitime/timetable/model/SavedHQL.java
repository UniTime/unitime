/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseSavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;

public class SavedHQL extends BaseSavedHQL {
	private static final long serialVersionUID = 2532519378106863655L;

	public SavedHQL() {
		super();
	}
	
	public static enum Flag {
		APPEARANCE_COURSES("Appearance: Courses", true),
		APPEARANCE_EXAMS("Appearance: Examinations", true),
		APPEARANCE_SECTIONING("Appearance: Student Sectioning", true),
		APPEARANCE_EVENTS("Appearance: Events", true),
		APPEARANCE_ADMINISTRATION("Appearance: Administration", true),
		ADMIN_ONLY("Restrictions: Administrator Only", false)
		;
		private String iDescription;
		private boolean iAppearance;
		Flag(String desc, boolean appearance) { iDescription = desc; iAppearance = appearance; }
		public int flag() { return 1 << ordinal(); }
		public boolean isSet(int type) { return (type & flag()) != 0; }
		public String description() { return iDescription; }
		public boolean isAppearance() { return iAppearance; }
	}
	
	private static interface OptionImplementation {
		public Map<Long, String> getValues(User user);
		
	}
	
	public static enum Option {
		SESSION("Academic Session", false, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(User user) {
				Session session = Session.getCurrentAcadSession(user);
				if (session == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				ret.put(session.getUniqueId(), session.getLabel());
				return ret;
			}
		}),
		DEPARTMENT("Department", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(User user) {
				Session session = Session.getCurrentAcadSession(user);
				if (session == null) return null;
				TimetableManager manager = TimetableManager.getManager(user);
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				if (user.isAdmin()) {
					for (Department d: (Set<Department>)Department.findAll(session.getUniqueId()))
						ret.put(d.getUniqueId(), d.htmlLabel());
				} else {
					for (Department d: (Set<Department>)Department.findAllOwned(session.getUniqueId(), manager, true))
						ret.put(d.getUniqueId(), d.htmlLabel());
				}
				return ret;
			}
		}),
		DEPARTMENTS("Departments", true, true, DEPARTMENT.iImplementation),
		SUBJECT("Subject Area", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(User user) {
				Map<Long, String> ret = new Hashtable<Long, String>();
				try {
					for (SubjectArea s: (Set<SubjectArea>)TimetableManager.getSubjectAreas(user)) {
						ret.put(s.getUniqueId(), s.getSubjectAreaAbbreviation());
					}
				} catch (Exception e) { return null; }
				return ret;
			}
		}),
		SUBJECTS("Subject Areas", true, true, SUBJECT.iImplementation),
		BUILDING("Buildings", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(User user) {
				Session session = Session.getCurrentAcadSession(user);
				if (session == null) return null;
				TimetableManager manager = TimetableManager.getManager(user);
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Building b: (List<Building>)Building.findAll(session.getUniqueId()))
					ret.put(b.getUniqueId(), b.getAbbrName());
				return ret;
			}
		}),
		BUILDINGS("Buildings", true, true, BUILDING.iImplementation),
		ROOM("Room", true, false, new OptionImplementation() {
			@Override
			public Map<Long, String> getValues(User user) {
				Session session = Session.getCurrentAcadSession(user);
				if (session == null) return null;
				TimetableManager manager = TimetableManager.getManager(user);
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Room r: (List<Room>)Room.findAllRooms(session.getUniqueId())){
					ret.put(r.getUniqueId(), r.getLabel());
				}
				return ret;
			}
		}),
		ROOMS("Rooms", true, true, ROOM.iImplementation)
		;
		
		String iName;
		OptionImplementation iImplementation;
		boolean iAllowSelection, iMultiSelect;
		Option(String name, boolean allowSelection, boolean multiSelect ,OptionImplementation impl) {
			iName = name;
			iAllowSelection = allowSelection; iMultiSelect = multiSelect;
			iImplementation = impl;
		}
		
		public String text() { return iName; }
		public boolean allowSingleSelection() { return iAllowSelection; }
		public boolean allowMultiSelection() { return iAllowSelection && iMultiSelect; }
		public Map<Long, String> values(User user) { return iImplementation.getValues(user); }
	}
	
	public static void main(String args[]) {
		for (Flag f: Flag.values()) {
			System.out.println(f.name() + ": " + f.flag() + " (" + f.isSet(0xFF) + ")");
		}
	}
	
	public boolean isSet(Flag f) {
		return getType() != null && f.isSet(getType());
	}
	
	public void set(Flag f) {
		if (!isSet(f))
			setType((getType() == null ? 0 : getType()) + f.flag());
	}
	
	public void clear(Flag f) {
		if (isSet(f)) setType(getType() - f.flag());
	}
	
	public static List<SavedHQL> listAll(org.hibernate.Session hibSession, Flag appearance, boolean admin) {
		synchronized (sHasQueriesCache) {
			List<SavedHQL> ret = new ArrayList<SavedHQL>();
			for (SavedHQL hql: (List<SavedHQL>)(hibSession == null ? SavedHQLDAO.getInstance().getSession() : hibSession).createQuery(
				"from SavedHQL order by name").setCacheable(true).list()) {
				if (!appearance.isSet(hql.getType())) continue;
				if (!admin && Flag.ADMIN_ONLY.isSet(hql.getType())) continue;
				ret.add(hql);
			}
			sHasQueriesCache.put(appearance.flag() | (admin ? Flag.ADMIN_ONLY.flag() : 0), !ret.isEmpty());
			return ret;
		}
	}
	
	private static Hashtable<Integer, Boolean> sHasQueriesCache = new Hashtable<Integer, Boolean>();
	public static boolean hasQueries(Flag appearance, boolean admin) {
		synchronized (sHasQueriesCache) {
			Boolean ret = sHasQueriesCache.get(appearance.flag() | (admin ? Flag.ADMIN_ONLY.flag() : 0));
			if (ret == null)
				ret = !listAll(null, appearance, admin).isEmpty();
			return ret;
		}
	}

}
