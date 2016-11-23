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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.cpsolver.ifs.util.DistanceMetric;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseLocation;
import org.unitime.timetable.model.dao.ExamLocationPrefDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.rooms.RoomDetailsBackend.UrlSigner;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.webutil.RequiredTimeTable;


/**
 * @author Tomas Muller
 */
public abstract class Location extends BaseLocation implements Comparable {
	public static final CourseMessages MSG = Localization.create(CourseMessages.class); 
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

/*[CONSTRUCTOR MARKER END]*/
	
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
			return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(((Location)o).getUniqueId() == null ? -1 : ((Location)o).getUniqueId());
		}
	}
	
	public abstract String getLabel();
	
	/** Room sharing table with all fields editable (for administrator) */
	public RequiredTimeTable getRoomSharingTable() {
		return new RequiredTimeTable(new RoomSharingModel(this, null, null)); //all editable
	}
	
	public RequiredTimeTable getEventAvailabilityTable() {
		RoomSharingModel model = new RoomSharingModel(this, null, null);
		model.setEventAvailabilityPreference(getEventAvailability());
		return new RequiredTimeTable(model);
	}

	/** Room sharing table with all fields editable (for administrator)
	 * @param departments list of departments (or depatment ids)
	 */
	public RequiredTimeTable getRoomSharingTable(Collection departments) {
		return new RequiredTimeTable(new RoomSharingModel(this, null, departments)); //all editable
	}
	
	/** Room sharing table editable by the given manager 
	 * @param session current academic session
	 * @param editingManager current timetabling manager (the one whose departments should be editable)
	 * @param departments list of departments (or depatment ids)
	 * */
    public RequiredTimeTable getRoomSharingTable(UserContext editingUser, Collection departments) {
    	return new RequiredTimeTable(getRoomSharingModel(editingUser, departments));
    }
    
	/** Room sharing table editable by the given manager 
	 * @param session current academic session
	 * @param editingManager current timetabling manager (the one whose departments should be editable)
	 * */
    public RequiredTimeTable getRoomSharingTable(UserContext editingUser) {
    	return new RequiredTimeTable(getRoomSharingModel(editingUser, null));
    }

    /** Room sharing model with all fields editable (for administrator) */
    public RoomSharingModel getRoomSharingModel() {
    	return new RoomSharingModel(this, null, null);
    }
    
    /** Room sharing model editable by the given manager
	 * @param session current academic session
	 * @param editingManager current timetabling manager (the one whose departments should be editable)
	 * @param departments list of departments (or depatment ids)
	 * */
    public RoomSharingModel getRoomSharingModel(UserContext editingUser) {
    	return getRoomSharingModel(editingUser, null);
    }
    
    /** Room sharing model editable by the given manager
	 * @param session current academic session
	 * @param editingManager current timetabling manager (the one whose departments should be editable)
	 * */
    public RoomSharingModel getRoomSharingModel(UserContext editingUser, Collection departments) {
    	if (editingUser == null || editingUser.getCurrentAuthority() == null || editingUser.getCurrentAuthority().hasRight(Right.DepartmentIndependent))
    		return new RoomSharingModel(this, null, departments);
    	
    	Set<Long> editingDepartments = new HashSet<Long>();
    	for (Qualifiable dept: editingUser.getCurrentAuthority().getQualifiers("Department"))
    		editingDepartments.add((Long)dept.getQualifierId());
    	
    	//check whether one of the editing departments has control over the room
    	for (Iterator i=getRoomDepts().iterator();i.hasNext();) {
    		RoomDept rd = (RoomDept)i.next();
    		if (!rd.isControl().booleanValue()) continue;
    		if (editingDepartments.contains(rd.getDepartment().getUniqueId()))
    			return new RoomSharingModel(this, null, departments);
    	}
    	
    	return new RoomSharingModel(this, editingDepartments, departments);
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
	
	public boolean hasGroup (Long roomGroup) {
		for (Iterator it = getRoomGroups().iterator(); it.hasNext();)
			if (roomGroup.equals(((RoomGroup) it.next()).getUniqueId())) return true;
		return false;
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
	
	public boolean hasFeature (Long roomFeature) {
		for (Iterator it = getFeatures().iterator(); it.hasNext();)
			if (roomFeature.equals(((RoomFeature) it.next()).getUniqueId())) return true;
		return false;
	}

	/**
	 * 
	 * @param sisReference
	 * @return
	 * @throws SmasException
	 */
	public boolean hasGlobalFeature(String sisReference) {
		GlobalRoomFeature grf =	GlobalRoomFeature.featureWithSisReference(getSession(), sisReference);
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
	public TreeSet<GlobalRoomFeature> getGlobalRoomFeatures() {
		TreeSet<GlobalRoomFeature> grfs = new TreeSet<GlobalRoomFeature>();
		for (RoomFeature rf: getFeatures())
			if (rf instanceof GlobalRoomFeature) {
				grfs.add((GlobalRoomFeature)rf);
			}
		return grfs;
	}
	
	public TreeSet<RoomGroup> getGlobalRoomGroups() {
		TreeSet<RoomGroup> grgs = new TreeSet<RoomGroup>();
		for (RoomGroup rg: getRoomGroups()) {
			if (rg.isGlobal()) grgs.add(rg);
		}
		return grgs;
	}
	
	/**
	 * 
	 * @return
	 */

	public TreeSet<DepartmentRoomFeature> getDepartmentRoomFeatures() {
		TreeSet<DepartmentRoomFeature> drfs = new TreeSet<DepartmentRoomFeature>();
		for (RoomFeature rf: getFeatures())
			if (rf instanceof DepartmentRoomFeature)
				drfs.add((DepartmentRoomFeature)rf);
		return drfs;
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
    	DistanceMetric m = new DistanceMetric(DistanceMetric.Ellipsoid.valueOf(ApplicationProperty.DistanceEllipsoid.value()));
    	return m.getDistanceInMeters(getUniqueId(), getCoordinateX(), getCoordinateY(), other.getUniqueId(), other.getCoordinateX(), other.getCoordinateY());
	}
	
	public Department getControllingDepartment() {
		for (Iterator i=getRoomDepts().iterator();i.hasNext();) {
			RoomDept rd = (RoomDept)i.next();
			if (rd.isControl().booleanValue()) return rd.getDepartment();
		}
		return null;
	}
	
	public abstract String getRoomTypeLabel();
	
	public Hashtable<ExamPeriod,PreferenceLevel> getExamPreferences(ExamType examType) {
		return getExamPreferences(examType.getUniqueId());
	}

    public Hashtable<ExamPeriod,PreferenceLevel> getExamPreferences(Long examTypeId) {
        Hashtable<ExamPeriod,PreferenceLevel> ret = new Hashtable();
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (examTypeId.equals(pref.getExamPeriod().getExamType().getUniqueId()))
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
    
    public void clearExamPreferences(ExamType examType) {
    	clearExamPreferences(examType.getUniqueId());
    }
    
    public void clearExamPreferences(Long examTypeId) {
        if (getExamPreferences()==null) setExamPreferences(new HashSet());
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (examTypeId.equals(pref.getExamPeriod().getExamType().getUniqueId())) {
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

    public String getExamPreferencesHtml(ExamType examType) {
        if (examType.getType() == ExamType.sExamTypeMidterm) {
            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(getSession(), examType);
            epx.load(this);
            return epx.toString(true).replaceAll(", ", "<br>");
        }
        StringBuffer ret = new StringBuffer();
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (!examType.equals(pref.getExamPeriod().getExamType())) continue;
            ret.append(
                    "<span style='color:"+PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog())+";'>"+
                    pref.getPrefLevel().getPrefName()+" "+pref.getExamPeriod().getName()+
                    "</span>");
        }
        return ret.toString();
    }
    
    public String getExamPreferencesAbbreviationHtml(ExamType examType) {
    	if (examType.getType() == ExamType.sExamTypeMidterm) {
            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(getSession(), examType);
            epx.load(this);
            return epx.toString(true).replaceAll(", ", "<br>");
        }
        StringBuffer ret = new StringBuffer();
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (!examType.equals(pref.getExamPeriod().getExamType())) continue;
            if(ret.length()>0){
            	ret.append("<br>");
            }
            ret.append(
                    "<span title='"+pref.getPrefLevel().getPrefName()+" "+pref.getExamPeriod().getName()+"' style='color:"+PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog())+";'>"+
                    pref.getExamPeriod().getAbbreviation()+
                    "</span>");
        }
        return ret.toString();
    }
    
    public String getExamPreferencesAbbreviation(ExamType examType) {
    	if (examType.getType() == ExamType.sExamTypeMidterm) {
            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(getSession(), examType);
            epx.load(this);
            return epx.toString(false).replaceAll(", ", "\n");
        }
        StringBuffer ret = new StringBuffer();
        for (Iterator i=getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (!examType.equals(pref.getExamPeriod().getExamType())) continue;
            if (ret.length()>0) ret.append("\n");
            ret.append(pref.getPrefLevel().getAbbreviation()+" "+pref.getExamPeriod().getAbbreviation());
        }
        return ret.toString();
    }
    
    public static TreeSet findAllExamLocations(Long sessionId, ExamType examType) {
    	return findAllExamLocations(sessionId, examType == null ? null : examType.getUniqueId());
    }

    public static TreeSet findAllExamLocations(Long sessionId, Long examTypeId) {
    	if (examTypeId == null) {
    		return new TreeSet(
                    (new LocationDAO()).getSession()
                    .createQuery("select room from Location as room where room.session.uniqueId = :sessionId and room.examTypes is not empty")
                    .setLong("sessionId", sessionId).setCacheable(true).list());
    	} else {
    		return new TreeSet(
                    (new LocationDAO()).getSession()
                    .createQuery("select room from Location as room inner join room.examTypes as type where room.session.uniqueId = :sessionId and type.uniqueId = :typeId")
                    .setLong("sessionId", sessionId).setLong("typeId", examTypeId).setCacheable(true).list());
    	}
    }
    
    public static TreeSet findNotAvailableExamLocations(Long periodId) {
        return new TreeSet(
                (new LocationDAO()).getSession()
                .createQuery("select distinct r from Exam x inner join x.assignedRooms r where x.assignedPeriod.uniqueId=:periodId")
                .setLong("periodId",periodId)
                .setCacheable(true).list());
    }
    
    public static Hashtable<Long, Set<Long>> findExamLocationTable(Long periodId) {
        Hashtable<Long,Set<Long>> table = new Hashtable();
        for (Iterator i = (new LocationDAO()).getSession()
                    .createQuery("select distinct r.uniqueId, x.uniqueId from Exam x inner join x.assignedRooms r where x.assignedPeriod.uniqueId=:periodId")
                    .setLong("periodId",periodId)
                    .setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            Set<Long> exams = table.get((Long)o[0]);
            if (exams == null) { exams = new HashSet<Long>(); table.put((Long)o[0], exams); }
            exams.add((Long)o[1]);
        }
        return table;
    }
    
    public Collection<Assignment> getCommitedAssignments() {
    	return new LocationDAO().getSession().createQuery(
                "select a from Assignment a inner join a.rooms r where " +
                "a.solution.commited=true and r.uniqueId=:locationId")
                .setLong("locationId", getUniqueId())
                .setCacheable(true).list();
    }
    
    public Collection<Assignment> getAssignments(Solution solution) {
    	List<Assignment> ret = new ArrayList<Assignment>(new LocationDAO().getSession().createQuery(
    			"select a from Assignment a inner join a.rooms r where " +
    			"r.uniqueId = :locationId and a.solution.uniqueId = :solutionId")
    			.setLong("locationId", getUniqueId())
    			.setLong("solutionId", solution.getUniqueId())
    			.setCacheable(true).list());
    	ret.addAll(new LocationDAO().getSession().createQuery(
    			"select a from Assignment a, Solution x inner join a.rooms r where " +
                "r.uniqueId = :locationId and a.solution.commited = true and " +
    			"x.uniqueId = :solutionId and a.solution.owner != x.owner")
    			.setLong("locationId", getUniqueId())
    			.setLong("solutionId", solution.getUniqueId())
                .setCacheable(true).list());
    	return ret;
    }
    
    public Collection<Assignment> getAssignments(Collection<Long> solutionId) {
    	if (solutionId.isEmpty()) return getCommitedAssignments();
    	List<Assignment> ret = new ArrayList<Assignment>(new LocationDAO().getSession().createQuery(
    			"select a from Assignment a inner join a.rooms r where " +
    			"r.uniqueId = :locationId and a.solution.uniqueId in (:solutionIds)")
    			.setLong("locationId", getUniqueId())
    			.setParameterList("solutionIds", solutionId, new LongType())
    			.setCacheable(true).list());
    	ret.addAll(new LocationDAO().getSession().createQuery(
    			"select a from Assignment a inner join a.rooms r where " +
                "r.uniqueId = :locationId and a.solution.commited = true and " +
    			"a.solution.owner.uniqueId not in (select s.owner.uniqueId from Solution s where s.uniqueId in (:solutionIds))")
    			.setLong("locationId", getUniqueId())
    			.setParameterList("solutionIds", solutionId, new LongType())
                .setCacheable(true).list());
    	return ret;
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
    
    public List<Exam> getExams(Long periodId) {
        return new LocationDAO().getSession().createQuery(
                "select x from Exam x inner join x.assignedRooms r where "+
                "x.assignedPeriod.uniqueId=:periodId and r.uniqueId=:locationId")
                .setLong("periodId",periodId)
                .setLong("locationId",getUniqueId())
                .setCacheable(true).list();
    }
    
    public boolean isExamEnabled(ExamType examType) {
    	return getExamTypes() != null && getExamTypes().contains(examType);
    }
    
    public boolean isExamEnabled(Long examTypeId) {
    	if (getExamTypes() == null) return false;
    	for (ExamType type: getExamTypes())
    		if (type.getUniqueId().equals(examTypeId)) return true;
    	return false;
    }
    
    public boolean hasFinalExamsEnabled() {
    	for (ExamType type: getExamTypes())
    		if (type.getType() == ExamType.sExamTypeFinal) return true;
    	return false;
    }
    
    public boolean hasMidtermExamsEnabled() {
    	for (ExamType type: getExamTypes())
    		if (type.getType() == ExamType.sExamTypeMidterm) return true;
    	return false;
    }
    
    public boolean hasAnyExamsEnabled() {
    	return getExamTypes() != null && !getExamTypes().isEmpty();
    }

    public void setExamEnabled(ExamType examType, boolean enabled) {
    	if (getExamTypes() == null) setExamTypes(new HashSet<ExamType>());
    	if (enabled) 
    		getExamTypes().add(examType);
    	else
    		getExamTypes().remove(examType);
    }
    
    public static List<Location> findAll(Long sessionId) {
        return (List<Location>)new LocationDAO().getSession().createQuery(
                "select l from Location l where l.session.uniqueId=:sessionId"
                ).setLong("sessionId", sessionId).setCacheable(true).list();
    }

    public static List<Location> findAllEventRooms(Long departmentId) {
        return (List<Location>)new LocationDAO().getSession().createQuery(
                "select l from Location l where l.eventDepartment.uniqueId=:departmentId"
                ).setLong("departmentId", departmentId).setCacheable(true).list();
    }

    public static List<Room> findAllRooms(Long sessionId) {
        return (List<Room>)new LocationDAO().getSession().createQuery(
                "select l from Room l where l.session.uniqueId=:sessionId"
                ).setLong("sessionId", sessionId).setCacheable(true).list();
    }
    
    public static List<NonUniversityLocation> findAllNonUniversityLocations(Long sessionId) {
        return (List<NonUniversityLocation>) new LocationDAO().getSession().createQuery(
                "select l from NonUniversityLocation l where l.session.uniqueId=:sessionId"
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
        for (Iterator i = q.setCacheable(true).list().iterator();i.hasNext();) {
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

    public static Hashtable<Long,Set<Long>> findClassLocationTable(Long sessionId, Set<Long> permanentIds, int startSlot, int length, List<Date> dates) {
    	if (permanentIds.isEmpty() || dates.isEmpty()) return new Hashtable<Long,Set<Long>>();
    	EventDateMapping.Class2EventDateMap class2eventMap = EventDateMapping.getMapping(sessionId);
    	String datesStr = "";
    	for (int i=0; i<dates.size(); i++) {
    		if (i>0) datesStr += ", ";
    		datesStr += ":date"+i;
    	}
    	Hashtable<Long,Set<Long>> table = new Hashtable<Long,Set<Long>>();
    	Iterator<Long> permanentIdIterator = permanentIds.iterator();
    	while (permanentIdIterator.hasNext()){
	    	String permIds = "";
	    	Long permanentId;
	    	int cntPermIds = 0;
	    	while(permanentIdIterator.hasNext() && cntPermIds < 1000){
	    		permanentId = permanentIdIterator.next();
	    		if (permIds.length()>0) permIds += ",";
	    		permIds += permanentId;
	    		cntPermIds++;
	    	}
	    	Query q = LocationDAO.getInstance().getSession()
	    	    .createQuery("select distinct m.locationPermanentId, e.clazz.uniqueId from " +
	    	    		"ClassEvent e inner join e.meetings m where " +
	            		"m.locationPermanentId in ("+permIds+") and " +
	            		"m.stopPeriod>:startSlot and :endSlot>m.startPeriod and " + // meeting time within given time period
	            		"m.meetingDate in ("+datesStr+") and m.approvalStatus = 1") // and date
	            .setInteger("startSlot", startSlot)
	            .setInteger("endSlot", startSlot + length);
	    	for (int i=0; i<dates.size(); i++) {
	    		q.setDate("date"+i, class2eventMap.getEventDate(dates.get(i)));
	    	}
	        for (Iterator i = q.setCacheable(true).list().iterator();i.hasNext();) {
	            Object[] o = (Object[])i.next();
	            Set<Long> ids = table.get((Long)o[0]);
	            if (ids==null) {
	            	ids = new HashSet<Long>();
	            	table.put((Long)o[0], ids);
	            }
	            ids.add((Long)o[1]);
	        }
    	}
        return table;
    }

    public static Hashtable<Long,Set<Event>> findEventTable(Long sessionId, Set<Long> permanentIds, int startSlot, int length, List<Date> dates) {
    	if (permanentIds.isEmpty() || dates.isEmpty()) return new Hashtable<Long,Set<Event>>();
    	EventDateMapping.Class2EventDateMap class2eventMap = EventDateMapping.getMapping(sessionId);
    	String datesStr = "";
    	for (int i=0; i<dates.size(); i++) {
    		if (i>0) datesStr += ", ";
    		datesStr += ":date"+i;
    	}

    	Hashtable<Long,Set<Event>> table = new Hashtable<Long,Set<Event>>();
    	Iterator<Long> permanentIdIterator = permanentIds.iterator();
    	while (permanentIdIterator.hasNext()){
	    	String permIds = "";
	    	Long permanentId;
	    	int cntPermIds = 0;
	    	while(permanentIdIterator.hasNext() && cntPermIds < 1000){
	    		permanentId = permanentIdIterator.next();
	    		if (permIds.length()>0) permIds += ",";
	    		permIds += permanentId;
	    		cntPermIds++;
	    	}
	
	    	Query q = LocationDAO.getInstance().getSession()
	    	    .createQuery("select distinct m.locationPermanentId, e from " +
	    	    		"Event e inner join e.meetings m where " +
	    	    		"e.class!=ClassEvent and "+
	            		"m.locationPermanentId in ("+permIds+") and " +
	            		"m.stopPeriod>:startSlot and :endSlot>m.startPeriod and " + // meeting time within given time period
	            		"m.meetingDate in ("+datesStr+") and m.approvalStatus = 1") // and date
	            .setInteger("startSlot", startSlot)
	            .setInteger("endSlot", startSlot + length);
	    	for (int i=0; i<dates.size(); i++) {
	    		q.setDate("date"+i, class2eventMap.getEventDate(dates.get(i)));
	    	}
	        for (Iterator i = q.setCacheable(true).list().iterator();i.hasNext();) {
	            Object[] o = (Object[])i.next();
	            Set<Event> events = table.get((Long)o[0]);
	            if (events==null) {
	            	events = new HashSet<Event>();
	            	table.put((Long)o[0], events);
	            }
	            events.add((Event)o[1]);
	        }
    	}
        return table;
    }
    
    @Deprecated
    public String getHtmlHint() {
    	return getHtmlHint(null);
    }
    
    @Deprecated
    public String getHtmlHint(String preference) {
		try {
			if (!Hibernate.isPropertyInitialized(this, "roomType") || !Hibernate.isInitialized(getRoomType())) {
				return LocationDAO.getInstance().get(getUniqueId()).getHtmlHintImpl(preference);
			} else {
				return getHtmlHintImpl(preference);
			}
		} catch (LazyInitializationException e) {
			return LocationDAO.getInstance().get(getUniqueId()).getHtmlHintImpl(preference);
		}
    }

    @Deprecated
	private String getHtmlHintImpl(String preference) {
    	String hint = (preference == null ? "" : preference + " " ) + getLabel() + (getDisplayName() == null ? " (" + getRoomTypeLabel() + ")" : " (" + getDisplayName() + ")");
    	String minimap = ApplicationProperty.RoomHintMinimapUrl.value();
    	if (minimap != null && getCoordinateX() != null && getCoordinateY() != null) {
    		minimap = minimap
    				.replace("%x", getCoordinateX().toString())
    				.replace("%y", getCoordinateY().toString())
    				.replace("%n", getLabel())
    				.replace("%i", getExternalUniqueId() == null ? "" : getExternalUniqueId());
	    	String apikey = ApplicationProperty.RoomMapStaticApiKey.value();
	    	if (apikey != null && !apikey.isEmpty()) {
	    		minimap += "&key=" + apikey;
		    	String secret = ApplicationProperty.RoomMapStaticSecret.value();
	    		if (secret != null && !secret.isEmpty()) {
	    			try {
	    				minimap += "&signature=" + new UrlSigner(secret).signRequest(minimap);
					} catch (Exception e) {}
	    		}
	    	}
	    	hint += "<br><img src=\\'" + minimap + "\\' border=\\'0\\' style=\\'border: 1px solid #9CB0CE;\\'/>";
    	}
    	hint += "<table width=\\'300px;\\'>";
    	hint += "<tr><td>Capacity:</td><td width=\\'99%\\'>" + getCapacity();
    	if (getExamCapacity() != null && getExamCapacity() > 0 && !getExamCapacity().equals(getCapacity()) && !getExamTypes().isEmpty()) {
    		String type = (getExamTypes().size() == 1 ? getExamTypes().iterator().next().getLabel().toLowerCase() + " " : "");
    		hint += " (" + getExamCapacity() + " for " + type + "examinations)";
    	}
    	hint += "</td></tr>";
    	if (getArea() != null) {
    		hint += "<tr><td>" + MSG.propertyRoomArea() + "</td><td width=\\'99%\\'>" +
    				new DecimalFormat(ApplicationProperty.RoomAreaUnitsFormat.value()).format(getArea()) + " " +
    				(ApplicationProperty.RoomAreaUnitsMetric.isTrue() ? MSG.roomAreaMetricUnitsShort() : MSG.roomAreaUnitsShort()) +
    				"</td></tr>";
    	}
    	Map<String, String> features = new HashMap<String, String>();
    	for (GlobalRoomFeature f: getGlobalRoomFeatures()) {
    		String type = (f.getFeatureType() == null ? "Features" : f.getFeatureType().getReference());
    		String featuresThisType = features.get(type);
    		if (featuresThisType == null) {
    			featuresThisType = "";
    		} else {
    			featuresThisType += ", ";
    		}
    		featuresThisType += f.getLabel();
			features.put(type, featuresThisType);
    	}
    	for (String type: new TreeSet<String>(features.keySet()))
    		hint += "<tr><td>" + type + ":</td><td>" + features.get(type) + "</td></tr>";
    	String groups = "";
    	for (RoomGroup g: getGlobalRoomGroups()) {
    		if (!groups.isEmpty()) groups += ", ";
    		groups += g.getName();
    	}
    	if (!groups.isEmpty()) hint += "<tr><td>Groups:</td><td>" + groups + "</td></tr>";
    	hint += "<tr><td>Events:</td><td><i>" + (getEventDepartment() == null ? "No Event Department" : getEffectiveEventStatus().toString()) + "</i></td></tr>";
    	String message = getEventMessage();
    	if (message != null && !message.isEmpty())
    		hint += "<tr><td colspan=\\'2\\'>" + message.replace("'", "\\'") + "</td></tr>";
    	hint += "</table>";
    	return hint;
    }
    
    public String getLabelWithHint() {
    	return "<span onmouseover=\"showGwtRoomHint(this, '" + getUniqueId() + "');\" onmouseout=\"hideGwtRoomHint();\">" + getLabel() + "</span>";
    }
    
    public boolean isUsed() {
    	Number nrMeetings = (Number)LocationDAO.getInstance().getSession().createQuery(
    			"select count(m) from Meeting m, Location l where " +
    			"l.uniqueId = :locId and m.locationPermanentId = l.permanentId " +
    			"and m.meetingDate >= l.session.eventBeginDate and m.meetingDate <= l.session.eventEndDate") // and m.approvedDate is not null
    			.setLong("locId", getUniqueId())
    			.setCacheable(true).uniqueResult();
    	return nrMeetings.intValue() > 0;
    }
    
    public int getEffectiveBreakTime() {
    	if (getBreakTime() != null)
    		return getBreakTime();
    	if (getEventDepartment() == null)
    		return ApplicationProperty.RoomDefaultBreakTime.intValue(getRoomType().getReference());
    	else
    		return getRoomType().getOption(getEventDepartment()).getBreakTime();
    }
    
    public RoomTypeOption.Status getEffectiveEventStatus() {
    	if (getEventStatus() != null)
    		return RoomTypeOption.Status.values()[getEventStatus()];
    	if (getEventDepartment() == null)
    		return RoomTypeOption.Status.NoEventManagement;
    	else
    		return getRoomType().getOption(getEventDepartment()).getEventStatus();
    }

    public String getEventMessage() {
    	if (getNote() != null && !getNote().isEmpty()) return getNote();
    	if (getEventDepartment() == null)
    		return null;
    	else
    		return getRoomType().getOption(getEventDepartment()).getMessage();
    }

    
    public String getLabelWithCapacity() {
    	return (getCapacity() == null ? getLabel() : MSG.labelLocationLabelWithCapacity(getLabel(), getCapacity()));
    }
    
    public String getLabelWithExamCapacity() {
    	return (getExamCapacity() == null ? getLabelWithCapacity() : MSG.labelLocationLabelWithCapacity(getLabel(), getExamCapacity()));
    }
    
    @Override
    public String getPattern() {
    	String pattern = super.getPattern();
    	if (pattern != null && pattern.length() == 336) {
    		// Old format (1 character per half-hour) -> convert to 1 character per slot
    		StringBuffer p = new StringBuffer();
    		for (int i = 0; i < 2016; i++)
    			p.append(pattern.charAt(i / 6));
    		return p.toString();
    	}
    	return pattern;
    }
    
    public abstract Set<? extends LocationPicture> getPictures();
    
    public static Location findByName(org.hibernate.Session hibSession, Long sessionId, String name) {
    	Room room = (Room)hibSession.createQuery(
    			"from Room r where r.session.uniqueId = :sessionId and (r.buildingAbbv || ' ' || r.roomNumber) = :name"
    			).setLong("sessionId", sessionId).setString("name", name)
    			.setMaxResults(1).setCacheable(true).uniqueResult();
    	if (room != null) return room;
    	return (NonUniversityLocation)hibSession.createQuery(
    			"from NonUniversityLocation l where l.session.uniqueId = :sessionId and l.name = :name"
    			).setLong("sessionId", sessionId).setString("name", name)
    			.setMaxResults(1).setCacheable(true).uniqueResult();
    }
    
    public static List<Location> getFutureLocations(Long locationId) {
    	Location location = LocationDAO.getInstance().get(locationId);
    	return location == null ? null : location.getFutureLocations();
    }
    
    public abstract List<Location> getFutureLocations();

	public static Collection<Location> lookupFutureLocations(org.hibernate.Session hibSession, List<Long> ids, Long sessionId) {
		Map<Long, Location> locations = new HashMap<Long, Location>();
		Set<Long> blacklist = new HashSet<Long>();
		
		for (Object[] o: (List<Object[]>)LocationDAO.getInstance().getSession().createQuery(
    			"select l.uniqueId, f from Room l, Room f where " +
    			"l.uniqueId in :ids and f.session.uniqueId = :sessionId and " +
    			"l.session.academicInitiative = f.session.academicInitiative and l.session.sessionBeginDateTime < f.session.sessionBeginDateTime and " +
    			"((l.permanentId = f.permanentId) or " + // match on permanent ids
    			"(not exists (from Location x where x.permanentId = f.permanentId and x.session = l.session) and " + // no match on permanent id exist
    			"l.roomType = f.roomType and " + // room type match
    			"((length(f.externalUniqueId) > 0 and l.externalUniqueId = f.externalUniqueId) or " + // external id match
    			"((f.externalUniqueId is null or length(f.externalUniqueId) = 0) and (l.externalUniqueId is null or length(l.externalUniqueId) = 0) and " + // no external id match
    			"f.building.abbreviation = l.building.abbreviation and f.roomNumber = l.roomNumber and f.capacity = l.capacity)))) " + // name & capacity match
    			"order by f.session.sessionBeginDateTime"
    			).setParameterList("ids", ids).setLong("sessionId", sessionId).setCacheable(true).list()) {
    		if (locations.put((Long)o[0], (Location)o[1]) != null)
    			blacklist.add((Long)o[0]);
    	}
		
		for (Object[] o: (List<Object[]>)LocationDAO.getInstance().getSession().createQuery(
    			"select l.uniqueId, f from NonUniversityLocation l, NonUniversityLocation f where " +
    			"l.uniqueId in :ids and f.session.uniqueId = :sessionId and " +
    			"l.session.academicInitiative = f.session.academicInitiative and l.session.sessionBeginDateTime < f.session.sessionBeginDateTime and " +
    			"((l.permanentId = f.permanentId) or " + // match on permanent ids
    			"(not exists (from Location x where x.permanentId = f.permanentId and x.session = l.session) and " + // no match on permanent id exist
    			"l.roomType = f.roomType and " + // room type match
    			"((length(f.externalUniqueId) > 0 and l.externalUniqueId = f.externalUniqueId) or " + // external id match
    			"((f.externalUniqueId is null or length(f.externalUniqueId) = 0) and (l.externalUniqueId is null or length(l.externalUniqueId) = 0) and " + // no external id match
    			"f.name = l.name and f.capacity = l.capacity)))) " + // name & capacity match
    			"order by f.session.sessionBeginDateTime"
    			).setParameterList("ids", ids).setLong("sessionId", sessionId).setCacheable(true).list()) {
    		if (locations.put((Long)o[0], (Location)o[1]) != null)
    			blacklist.add((Long)o[0]);
    	}
		
		if (!blacklist.isEmpty())
			for (Long id: blacklist)
				locations.remove(id);
		
		return locations.values();
	}
}
