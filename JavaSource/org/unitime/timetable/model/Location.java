/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseLocation;
import org.unitime.timetable.model.dao.ExamLocationPrefDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.webutil.RequiredTimeTable;


public abstract class Location extends BaseLocation implements Comparable {
	public static final String AVAILABLE_LOCATIONS_ATTR = "availableLocations";
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Location () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Location (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public Location (
		java.lang.Long uniqueId,
		java.lang.Long permanentId,
		java.lang.Integer capacity,
		java.lang.Integer coordinateX,
		java.lang.Integer coordinateY,
		java.lang.Boolean ignoreTooFar,
		java.lang.Boolean ignoreRoomCheck) {

		super (
			uniqueId,
			permanentId,
			capacity,
			coordinateX,
			coordinateY,
			ignoreTooFar,
			ignoreRoomCheck);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	private static final int sExamLocationTypeNone = 0;
	private static final int sExamLocationTypeFinal = 1;
	private static final int sExamLocationTypeMidterm = 2;
	private static final int sExamLocationTypeBoth = 3;

	public int compareTo(Object o) {
		if (o==null || !(o instanceof Location)) return -1;
		if (this instanceof Room) {
			if (o instanceof Room) {
				Room r1 = (Room)this;
				Room r2 = (Room)o;
		    	int cmp = r1.getBuilding().compareTo(r2.getBuilding());
		    	if (cmp!=0) return cmp;
		    	cmp = r1.getRoomNumber().compareTo(r2.getRoomNumber());
		    	if (cmp!=0) return cmp;
		    	return r1.getUniqueId().compareTo(r2.getUniqueId());
			} else return -1; //rooms first
		} else if (this instanceof NonUniversityLocation) {
			if (o instanceof Room) {
				return 1; //rooms first
			} else if (o instanceof NonUniversityLocation) {
				NonUniversityLocation l1 = (NonUniversityLocation)this;
				NonUniversityLocation l2 = (NonUniversityLocation)o;
				int cmp = l1.getName().compareTo(l2.getName());
				if (cmp!=0) return cmp;
				return l1.getUniqueId().compareTo(l2.getUniqueId());
				
			} else return -1; //all the rest after
		} else {
			return getUniqueId().compareTo(((Location)o).getUniqueId());
		}
	}
	
	public abstract String getLabel();
	
	/** Room sharing table with all fields editable (for administrator) */
	public RequiredTimeTable getRoomSharingTable() {
		return new RequiredTimeTable(getRoomSharingModel(null)); //all editable
	}

	/** Room sharing table with all fields editable (for administrator)
	 * @param departments list of departments (or depatment ids)
	 */
	public RequiredTimeTable getRoomSharingTable(Collection departments) {
		return new RequiredTimeTable(getRoomSharingModel(null, departments)); //all editable
	}
	
	/** Room sharing table editable by the given manager 
	 * @param session current academic session
	 * @param editingManager current timetabling manager (the one whose departments should be editable)
	 * @param departments list of departments (or depatment ids)
	 * */
    public RequiredTimeTable getRoomSharingTable(Session session, User editingUser, Collection departments) {
    	return new RequiredTimeTable(getRoomSharingModel(session, editingUser, departments));
    }
    
	/** Room sharing table editable by the given manager 
	 * @param session current academic session
	 * @param editingManager current timetabling manager (the one whose departments should be editable)
	 * */
    public RequiredTimeTable getRoomSharingTable(Session session, User editingUser) {
    	return new RequiredTimeTable(getRoomSharingModel(session, editingUser, null));
    }

    /** Room sharing model with all fields editable (for administrator) */
    public RoomSharingModel getRoomSharingModel() {
    	return getRoomSharingModel(null);
    }
    
    /** Room sharing model editable by the given manager
	 * @param session current academic session
	 * @param editingManager current timetabling manager (the one whose departments should be editable)
	 * @param departments list of departments (or depatment ids)
	 * */
    public RoomSharingModel getRoomSharingModel(Session session, User editingUser) {
    	return getRoomSharingModel(session, editingUser, null);
    }
    
    /** Room sharing model editable by the given manager
	 * @param session current academic session
	 * @param editingManager current timetabling manager (the one whose departments should be editable)
	 * */
    public RoomSharingModel getRoomSharingModel(Session session, User editingUser, Collection departments) {
    	TimetableManager editingManager = TimetableManager.getManager(editingUser);
    	if (session==null || editingUser==null || editingManager==null || editingUser.isAdmin())
    		return getRoomSharingModel(null,departments);
    	Set editingDepartments = editingManager.departmentsForSession(session.getUniqueId());
    	//check whether one of the editing departments has control over the room
    	for (Iterator i=getRoomDepts().iterator();i.hasNext();) {
    		RoomDept rd = (RoomDept)i.next();
    		if (!rd.isControl().booleanValue()) continue;
    		if (editingDepartments.contains(rd.getDepartment()))
    			return getRoomSharingModel(null,departments);
    	}
    	HashSet editingDepartmentIds = new HashSet();
    	if (editingDepartments!=null) {
        	for (Iterator i=editingDepartments.iterator();i.hasNext();) {
        		Department d = (Department)i.next();
        		editingDepartmentIds.add(d.getUniqueId());
        	}
    	}
    	return getRoomSharingModel(editingDepartmentIds, departments);
    }
    
    
    /** Room sharing model editable by the given manager
     * @param editingDepartmentIds editable departments (null if all)
     */
    public RoomSharingModel getRoomSharingModel(Set editingDepartmentIds) {
    	return getRoomSharingModel(editingDepartmentIds, null);
    }
    
    /** Room sharing model editable by the given manager
     * @param editingDepartmentIds editable departments (null if all)
     * @param departments list of departments (or depatment ids)
     */
    public RoomSharingModel getRoomSharingModel(Set editingDepartmentIds, Collection departments) {
    	return new RoomSharingModel(this, editingDepartmentIds, departments); 
    }
    
    /** Save changes made in the room sharing model back to the room */
    public void setRoomSharingModel(RoomSharingModel model) {
    	if (model==null) {
    		setPattern(null); setManagerIds(null);
    	} else {
    		setPattern(model.getPreferences());
    		setManagerIds(model.getManagerIds());
    	}
    }
    
    public void setRoomSharingTable(RequiredTimeTable table) {
    	setRoomSharingModel((RoomSharingModel)table.getModel());
    }
    
	/**
	 * 
	 * @param roomGroup
	 * @return
	 */
	public boolean hasGroup (RoomGroup roomGroup) {
		boolean b = false;
		for (Iterator it = getRoomGroups().iterator(); it.hasNext();) {
			if (roomGroup.equals((RoomGroup) it.next())) {
				b = true;
				break;
			}
		}
		return b;
	}
	
	/**
	 * 
	 * @param roomDept
	 * @return
	 */
	public boolean hasRoomDept (Department d) {
		boolean b = false;
		for (Iterator it = getRoomDepts().iterator(); it.hasNext();) {
			RoomDept rd = (RoomDept) it.next();
			if (rd.getDepartment().equals(d) && rd.getRoom().equals(this)) {
				b = true;
				break;
			}
		}
		return b;
	}
	
	/**
	 * 
	 * @param roomFeature
	 * @return
	 */
	public boolean hasFeature (RoomFeature roomFeature) {
		boolean b = false;
		for (Iterator it = getFeatures().iterator(); it.hasNext();) {
			if (roomFeature.equals((RoomFeature) it.next())) {
				b = true;
				break;
			}
		}
		return b;
	}
	
	/**
	 * 
	 * @param sisReference
	 * @return
	 * @throws SmasException
	 */
	public boolean hasGlobalFeature(String sisReference) {
		GlobalRoomFeature grf =	GlobalRoomFeature.featureWithSisReference(sisReference);
		if (grf == null) return false;
		return hasFeature(grf);
	}
	
	/**
	 * 
	 * @param roomFeature
	 */
	public void addTofeatures (org.unitime.timetable.model.RoomFeature roomFeature) {
		if (null == getFeatures()) 
			setFeatures(new java.util.HashSet());
		getFeatures().add(roomFeature);
	}

	/**
	 * remove feature from room
	 * @param roomFeature
	 */
	public void removeFromfeatures (org.unitime.timetable.model.RoomFeature roomFeature) {
		if (null == getFeatures()) 
			setFeatures(new java.util.HashSet());
		getFeatures().remove(roomFeature);
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void saveOrUpdate() throws HibernateException {
		(new LocationDAO()).saveOrUpdate(this);
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection getGlobalRoomFeatures() {
		Collection grfs = new HashSet();
		for (Iterator iter = getFeatures().iterator(); iter.hasNext();) {
			RoomFeature rf = (RoomFeature) iter.next();
			if (rf instanceof GlobalRoomFeature) {
				grfs.add(rf);
			}
		}
		return (new TreeSet(grfs));
	}
	
	/**
	 * 
	 * @return
	 */

	public Collection getDepartmentRoomFeatures() {
		Collection drfs = new HashSet();
		for (Iterator iter = getFeatures().iterator(); iter.hasNext();) {
			RoomFeature rf = (RoomFeature) iter.next();
			if (rf instanceof DepartmentRoomFeature) {
				drfs.add(rf);
			}
		}
		return (new TreeSet(drfs));
	}
	
	/**
	 * 
	 * @param department
	 * @return
	 */
	public PreferenceLevel getRoomPreferenceLevel(Department department) {
		if (department==null) return PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral);
		for (Iterator i=department.getRoomPreferences().iterator();i.hasNext(); ) {
			RoomPref rp = (RoomPref)i.next();
			if (rp.getRoom().equals(this)) return rp.getPrefLevel();
		}
		return PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral);
	}
	
	/**
	 * 
	 * @param department
	 * @return
	 */
	public RoomPref getRoomPreference(Department department) {
		for (Iterator i=department.getRoomPreferences().iterator();i.hasNext(); ) {
			RoomPref rp = (RoomPref)i.next();
			if (rp.getRoom().equals(this)) return rp;
		}
		return null;
	}
	
	public void removedFromDepartment(Department department, org.hibernate.Session hibSession) {
		for (Iterator iter = getFeatures().iterator(); iter.hasNext();) {
			RoomFeature rf = (RoomFeature) iter.next();
			if (!(rf instanceof DepartmentRoomFeature)) continue;
			DepartmentRoomFeature drf = (DepartmentRoomFeature)rf;
			if (department.equals(drf.getDepartment())) {
				drf.getRooms().remove(this);
				iter.remove();
				hibSession.saveOrUpdate(drf);
			}
		}
		for (Iterator iter = getRoomGroups().iterator(); iter.hasNext();) {
			RoomGroup rg = (RoomGroup) iter.next();
			if (rg.isGlobal().booleanValue()) continue;
			if (department.equals(rg.getDepartment())) {
				rg.getRooms().remove(this);
				iter.remove();
				hibSession.saveOrUpdate(rg);
			}
		}
		for (Iterator iter = department.getPreferences().iterator(); iter.hasNext();) {
			Preference p = (Preference)iter.next();
           	if (p instanceof RoomPref && ((RoomPref)p).getRoom().equals(this)) {
           		hibSession.delete(p);
           		iter.remove();
            }
		}
		hibSession.saveOrUpdate(department);
		List roomPrefs = hibSession.
			createQuery("select distinct rp from RoomPref rp where rp.room.uniqueId=:locationId").
			setInteger("locationId", getUniqueId().intValue()).
			list();
		for (Iterator i=roomPrefs.iterator();i.hasNext();) {
			RoomPref rp = (RoomPref)i.next();
			if (rp.getOwner() instanceof Class_) {
				Class_ c = (Class_)rp.getOwner();
				if (department.equals(c.getManagingDept())) {
					c.getPreferences().remove(rp);
					hibSession.delete(rp);
					hibSession.saveOrUpdate(c);
				}
			}
			if (rp.getOwner() instanceof SchedulingSubpart) {
				SchedulingSubpart s = (SchedulingSubpart)rp.getOwner();
				if (department.equals(s.getManagingDept())) {
					s.getPreferences().remove(rp);
					hibSession.delete(rp);
					hibSession.saveOrUpdate(s);
				}
			}
			if (rp.getOwner() instanceof DepartmentalInstructor) {
				DepartmentalInstructor d = (DepartmentalInstructor)rp.getOwner();
				if (department.equals(d.getDepartment())) {
					d.getPreferences().remove(rp);
					hibSession.delete(rp);
					hibSession.saveOrUpdate(d);
				}
			}
		}
		
		if (this instanceof Room) {
			Building bldg = ((Room)this).getBuilding();
			List bldgPrefs = hibSession.
			createQuery("select distinct bp from BuildingPref bp where bp.building.uniqueId=:bldgId").
			setInteger("bldgId", bldg.getUniqueId().intValue()).
			list();
			for (Iterator i=bldgPrefs.iterator();i.hasNext();) {
				BuildingPref bp = (BuildingPref)i.next();
				if (bp.getOwner() instanceof Class_) {
					Class_ c = (Class_)bp.getOwner();
					if (!c.getAvailableBuildings().contains(bldg) && department.equals(c.getManagingDept())) {
						c.getPreferences().remove(bp);
						hibSession.delete(bp);
						hibSession.saveOrUpdate(c);
					}
				}
				if (bp.getOwner() instanceof SchedulingSubpart) {
					SchedulingSubpart s = (SchedulingSubpart)bp.getOwner();
					if (!s.getAvailableBuildings().contains(bldg) && department.equals(s.getManagingDept())) {
						s.getPreferences().remove(bp);
						hibSession.delete(bp);
						hibSession.saveOrUpdate(s);
					}
				}
				if (bp.getOwner() instanceof DepartmentalInstructor) {
					DepartmentalInstructor d = (DepartmentalInstructor)bp.getOwner();
					if (!d.getAvailableBuildings().contains(bldg) && department.equals(d.getDepartment())) {
						d.getPreferences().remove(bp);
						hibSession.delete(bp);
						hibSession.saveOrUpdate(d);
					}
				}
			}
		}
	}
    
	public double getDistance(Location other) {
    	if (getUniqueId().equals(other.getUniqueId())) return 0.0;
    	if (this instanceof Location && isIgnoreTooFar()!=null && isIgnoreTooFar().booleanValue()) return 0.0;
    	if (other instanceof Location && other.isIgnoreTooFar()!=null && other.isIgnoreTooFar().booleanValue()) return 0.0;
    	int x1 = (getCoordinateX()==null?-1:getCoordinateX().intValue());
    	int y1 = (getCoordinateY()==null?-1:getCoordinateY().intValue());
    	int x2 = (other.getCoordinateX()==null?-1:other.getCoordinateX().intValue());
    	int y2 = (other.getCoordinateY()==null?-1:other.getCoordinateY().intValue());
    	if (x1<0 || x2<0 || y1<0 || y2<0) return 10000.0;
		long x = x1-x2;
		long y = y1-y2;
		return Math.sqrt((x*x)+(y*y));
	}
	
	public Department getControllingDepartment() {
		for (Iterator i=getRoomDepts().iterator();i.hasNext();) {
			RoomDept rd = (RoomDept)i.next();
			if (rd.isControl().booleanValue()) return rd.getDepartment();
		}
		return null;
	}
	
	public abstract String getRoomTypeLabel();

    public Hashtable<ExamPeriod,PreferenceLevel> getExamPreferences(int examType) {
        Hashtable<ExamPeriod,PreferenceLevel> ret = new Hashtable();
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (examType==pref.getExamPeriod().getExamType())
                ret.put(pref.getExamPeriod(),pref.getPrefLevel());
        }
        return ret;
    }
    
    public PreferenceLevel getExamPreference(ExamPeriod period) {
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (pref.getExamPeriod().equals(period)) return pref.getPrefLevel();
        }
        return PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral);
    }
    
    public void clearExamPreferences(int examType) {
        if (getExamPreferences()==null) setExamPreferences(new HashSet());
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (examType==pref.getExamPeriod().getExamType()) {
                new ExamLocationPrefDAO().getSession().delete(pref);
                i.remove();
            }
        }
    }
    
    public void setExamPreference(ExamPeriod period, PreferenceLevel preference) {
        if (getExamPreferences()==null) setExamPreferences(new HashSet());
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (pref.getExamPeriod().equals(period)) {
                if (PreferenceLevel.sNeutral.equals(preference.getPrefProlog())) {
                    new ExamLocationPrefDAO().getSession().delete(pref);
                    i.remove();
                } else {
                    pref.setPrefLevel(preference);
                    new ExamLocationPrefDAO().getSession().update(pref);
                }
                return; 
            }
        }
        if (PreferenceLevel.sNeutral.equals(preference.getPrefProlog())) return;
        ExamLocationPref pref = new ExamLocationPref();
        pref.setExamPeriod(period);
        pref.setPrefLevel(preference);
        pref.setLocation(this);
        getExamPreferences().add(pref);
        new ExamLocationPrefDAO().getSession().save(pref);
    }
    
    public void addExamPreference(ExamPeriod period, PreferenceLevel preference) {
        if (PreferenceLevel.sNeutral.equals(preference.getPrefProlog())) return;
        ExamLocationPref pref = new ExamLocationPref();
        pref.setExamPeriod(period);
        pref.setPrefLevel(preference);
        pref.setLocation(this);
        getExamPreferences().add(pref);
        new ExamLocationPrefDAO().getSession().save(pref);
    }

    public String getExamPreferencesHtml(int examType) {
        if (examType==Exam.sExamTypeMidterm) {
            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(getSession());
            epx.load(this);
            return epx.toString(true).replaceAll(", ", "<br>");
        }
        StringBuffer ret = new StringBuffer();
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (examType!=pref.getExamPeriod().getExamType()) continue;
            ret.append(
                    "<span style='color:"+PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog())+";'>"+
                    pref.getPrefLevel().getPrefName()+" "+pref.getExamPeriod().getName()+
                    "</span>");
        }
        return ret.toString();
    }
    
    public String getExamPreferencesAbbreviationHtml(int examType) {
        if (examType==Exam.sExamTypeMidterm) {
            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(getSession());
            epx.load(this);
            return epx.toString(true).replaceAll(", ", "<br>");
        }
        StringBuffer ret = new StringBuffer();
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (examType!=pref.getExamPeriod().getExamType()) continue;
            ret.append(
                    "<span title='"+pref.getPrefLevel().getPrefName()+" "+pref.getExamPeriod().getName()+"' style='color:"+PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog())+";'>"+
                    pref.getExamPeriod().getAbbreviation()+
                    "</span>");
        }
        return ret.toString();
    }
    
    public String getExamPreferencesAbbreviation(int examType) {
        if (examType==Exam.sExamTypeMidterm) {
            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(getSession());
            epx.load(this);
            return epx.toString(true).replaceAll(", ", "\n");
        }
        StringBuffer ret = new StringBuffer();
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (examType!=pref.getExamPeriod().getExamType()) continue;
            if (ret.length()>0) ret.append("\n");
            ret.append(PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.getExamPeriod().getAbbreviation());
        }
        return ret.toString();
    }

    public static TreeSet findAllExamLocations(Long sessionId, int examType) {
        switch (examType) {
        case Exam.sExamTypeFinal :
            return new TreeSet(
                    (new LocationDAO()).getSession()
                    .createQuery("select room from Location as room where room.session.uniqueId = :sessionId and room.examType in ("+sExamLocationTypeFinal+","+sExamLocationTypeBoth+")")
                    .setLong("sessionId", sessionId).setCacheable(true).list());
        case Exam.sExamTypeMidterm :
            return new TreeSet(
                    (new LocationDAO()).getSession()
                    .createQuery("select room from Location as room where room.session.uniqueId = :sessionId and room.examType in ("+sExamLocationTypeMidterm+","+sExamLocationTypeBoth+")")
                    .setLong("sessionId", sessionId).setCacheable(true).list());
        default :
            return new TreeSet(
                    (new LocationDAO()).getSession()
                    .createQuery("select room from Location as room where room.session.uniqueId = :sessionId and room.examType != "+sExamLocationTypeNone)
                    .setLong("sessionId", sessionId).setCacheable(true).list());
        }
    }
    
    public static TreeSet findNotAvailableExamLocations(Long periodId) {
        return new TreeSet(
                (new LocationDAO()).getSession()
                .createQuery("select distinct r from Exam x inner join x.assignedRooms r where x.assignedPeriod.uniqueId=:periodId")
                .setLong("periodId",periodId)
                .setCacheable(true).list());
    }
    
    public static Hashtable<Long,Long> findExamLocationTable(Long periodId) {
        Hashtable<Long,Long> table = new Hashtable();
        for (Iterator i = (new LocationDAO()).getSession()
                    .createQuery("select distinct r.uniqueId, x.uniqueId from Exam x inner join x.assignedRooms r where x.assignedPeriod.uniqueId=:periodId")
                    .setLong("periodId",periodId)
                    .setCacheable(true).iterate();i.hasNext();) {
            Object[] o = (Object[])i.next();
            table.put((Long)o[0],(Long)o[1]);
        }
        return table;
    }

    public Exam getExam(Long periodId) {
        return (Exam)new LocationDAO().getSession().createQuery(
                "select x from Exam x inner join x.assignedRooms r where " +
                "x.assignedPeriod.uniqueId=:periodId and r.uniqueId=:locationId")
                .setLong("locationId", getUniqueId())
                .setLong("periodId", periodId).setCacheable(true).uniqueResult();
    }
    
    public Collection<Assignment> getCommitedAssignments() {
    	return new LocationDAO().getSession().createQuery(
                "select a from Assignment a inner join a.rooms r where " +
                "a.solution.commited=true and r.uniqueId=:locationId")
                .setLong("locationId", getUniqueId())
                .setCacheable(true).list();
    }
    
    public static TreeSet findAllAvailableExamLocations(ExamPeriod period) {
        TreeSet locations = findAllExamLocations(period.getSession().getUniqueId(),period.getExamType());
        locations.removeAll(findNotAvailableExamLocations(period.getUniqueId()));
        return locations;
    }

    public static double getDistance(Collection rooms1, Collection rooms2) {
        if (rooms1==null || rooms1.isEmpty() || rooms2==null || rooms2.isEmpty()) return 0;
        double maxDistance = 0;
        for (Iterator i1=rooms1.iterator();i1.hasNext();) {
            Object o1 = i1.next();
            Location r1 = null;
            if (o1 instanceof ExamRoomInfo)
                r1 = ((ExamRoomInfo)o1).getLocation();
            else
                r1 = (Location)o1;
            for (Iterator i2=rooms2.iterator();i2.hasNext();) {
                Object o2 = i2.next();
                Location r2 = null;
                if (o2 instanceof ExamRoomInfo)
                    r2 = ((ExamRoomInfo)o2).getLocation();
                else
                    r2 = (Location)o2;
                maxDistance = Math.max(maxDistance, r1.getDistance(r2));
            }
        }
        return maxDistance;
    }
    
    public List getExams(Long periodId) {
        return new LocationDAO().getSession().createQuery(
                "select x from Exam x inner join x.assignedRooms r where "+
                "x.assignedPeriod.uniqueId=:periodId and r.uniqueId=:locationId")
                .setLong("periodId",periodId)
                .setLong("locationId",getUniqueId())
                .setCacheable(true).list();
    }
    
    public boolean isExamEnabled(int examType) {
        if (getExamType()==null) return false;
        switch (examType) {
        case Exam.sExamTypeFinal :
                return sExamLocationTypeFinal==getExamType() || sExamLocationTypeBoth==getExamType();
        case Exam.sExamTypeMidterm :
                return sExamLocationTypeMidterm==getExamType() || sExamLocationTypeBoth==getExamType();
        }
        return false;
    }
    public void setExamEnabled(int examType, boolean enabled) {
        switch (examType) {
        case Exam.sExamTypeFinal :
                setExamType(
                        (isExamEnabled(Exam.sExamTypeMidterm)?sExamLocationTypeMidterm:0)+
                        (enabled?sExamLocationTypeFinal:0));
                break;
        case Exam.sExamTypeMidterm :
            setExamType(
                    (isExamEnabled(Exam.sExamTypeFinal)?sExamLocationTypeFinal:0)+
                    (enabled?sExamLocationTypeMidterm:0));
        }
    }
    
    public static List findAll(Long sessionId) {
        return new LocationDAO().getSession().createQuery(
                "select l from Location l where l.session.uniqueId=:sessionId"
                ).setLong("sessionId", sessionId).setCacheable(true).list();
    }

    public static List findAllRooms(Long sessionId) {
        return new LocationDAO().getSession().createQuery(
                "select l from Room l where l.session.uniqueId=:sessionId"
                ).setLong("sessionId", sessionId).setCacheable(true).list();
    }
    
    public abstract RoomType getRoomType();
    public abstract void setRoomType(RoomType roomType);
    
    public static Hashtable<Long,Set<Long>> findClassLocationTable(Long sessionId, int startSlot, int length, Vector<Date> dates) {
    	Hashtable<Long,Set<Long>> table = new Hashtable();
    	String datesStr = "";
    	for (int i=0; i<dates.size(); i++) {
    		if (i>0) datesStr += ", ";
    		datesStr += ":date"+i;
    	}
    	Query q = LocationDAO.getInstance().getSession()
    	    .createQuery("select distinct r.uniqueId, e.clazz.uniqueId from " +
    	    		"ClassEvent e inner join e.meetings m, Location r where " +
            		"r.session.uniqueId=:sessionId and r.permanentId=m.locationPermanentId and " + // link Location r with Meeting m
            		"m.stopPeriod>:startSlot and :endSlot>m.startPeriod and " + // meeting time within given time period
            		"m.meetingDate in ("+datesStr+")")
            .setLong("sessionId",sessionId)
            .setInteger("startSlot", startSlot)
            .setInteger("endSlot", startSlot + length);
    	for (int i=0; i<dates.size(); i++) {
    		q.setDate("date"+i, dates.elementAt(i));
    	}
        for (Iterator i = q.setCacheable(true).iterate();i.hasNext();) {
            Object[] o = (Object[])i.next();
            Set<Long> ids = table.get((Long)o[0]);
            if (ids==null) {
            	ids = new HashSet<Long>();
            	table.put((Long)o[0], ids);
            }
            ids.add((Long)o[1]);
        }
        return table;
    }
    
    public Set<Long> findClassLocationTable(int startSlot, int length, Vector<Date> dates) {
    	String datesStr = "";
    	for (int i=0; i<dates.size(); i++) {
    		if (i>0) datesStr += ", ";
    		datesStr += ":date"+i;
    	}
    	Query q = LocationDAO.getInstance().getSession()
    	    .createQuery("select distinct e.clazz.uniqueId from " +
    	    		"ClassEvent e inner join e.meetings m where " +
            		"m.locationPermanentId=:permanentId and " +
            		"m.stopPeriod>:startSlot and :endSlot>m.startPeriod and " + // meeting time within given time period
            		"m.meetingDate in ("+datesStr+")") // and date
            .setLong("permanentId",getPermanentId())
            .setInteger("startSlot", startSlot)
            .setInteger("endSlot", startSlot + length);
    	for (int i=0; i<dates.size(); i++) {
    		q.setDate("date"+i, dates.elementAt(i));
    	}
    	return new HashSet<Long>(q.setCacheable(true).list());
    }

}
