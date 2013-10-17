/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.dom4j.Element;
import org.unitime.timetable.model.PreferenceLevel;

import net.sf.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import net.sf.cpsolver.coursett.constraint.GroupConstraint;
import net.sf.cpsolver.coursett.constraint.InstructorConstraint;
import net.sf.cpsolver.coursett.criteria.BrokenTimePatterns;
import net.sf.cpsolver.coursett.criteria.TooBigRooms;
import net.sf.cpsolver.coursett.criteria.UselessHalfHours;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.preference.PreferenceCombination;
import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.solver.Solver;

/**
 * @author Tomas Muller
 */
public class AssignmentPreferenceInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 4L;
	public static int sVersion = 4; // to be able to do some changes in the future
	private double iNormalizedTimePreference = 0.0;
	private double iBestNormalizedTimePreference = 0.0;
	private int iTimePreference = 0;
	private Hashtable iRoomPreference = new Hashtable();
	private int iBestRoomPreference = 0;
	private int iNrStudentConflicts = 0;
	private int iNrHardStudentConflicts = 0;
	private int iNrDistanceStudentConflicts = 0;
	private int iNrCommitedStudentConflicts = 0;
	private int iNrTimeLocations = 0;
	private int iNrRoomLocations = 0;
	private int iNrSameRoomPlacementsNoConf = 0;
    private int iNrSameTimePlacementsNoConf = 0;
	private int iNrPlacementsNoConf = 0;
	private int iBtbInstructorPreference = 0;
	private boolean iIsInitial = false;
	private String iInitialAssignment = null;
	private boolean iHasInitialSameTime = false;
	private boolean iHasInitialSameRoom = false;
	private double iPerturbationPenalty = 0.0;
	private int iTooBigRoomPreference = 0;
	private long iMinRoomSize = 0;
	private int iUselessHalfHours = 0;
	private double iDeptBalancPenalty = 0;
	private double iSpreadPenalty = 0;
	private int iMaxDeptBalancPenalty = 0;
	private double iMaxSpreadPenalty = 0;
	private int iGroupConstraintPref = 0;
	private int iDatePatternPref = 0;
	
	public AssignmentPreferenceInfo() {
		super();
	}
	
	public AssignmentPreferenceInfo(Solver solver, Placement placement) {
		this(solver, placement, false, false);
	}
	
	public AssignmentPreferenceInfo(Solver solver, Placement placement, boolean includeConflictInfo) {
		this(solver, placement, includeConflictInfo, false);
	}
	
	public AssignmentPreferenceInfo(Solver solver, Placement placement, boolean includeConflictInfo, boolean ofTheSameProblem) {
		super();
		Lecture lecture=(Lecture)placement.variable();
		setBestNormalizedTimePreference(lecture.getBestTimePreference());
		setNormalizedTimePreference(placement.getTimeLocation().getNormalizedPreference());
		setBestRoomPreference(lecture.getBestRoomPreference());
		if (ofTheSameProblem) {
			setNrStudentConflicts(lecture.countStudentConflictsOfTheSameProblem(placement));
			setNrHardStudentConflicts(lecture.countHardStudentConflictsOfTheSameProblem(placement));
			setNrCommitedStudentConflicts(lecture.countCommittedStudentConflictsOfTheSameProblem(placement));
			setNrDistanceStudentConflicts(lecture.countDistanceStudentConflictsOfTheSameProblem(placement));
		} else {
			setNrStudentConflicts(lecture.countStudentConflicts(placement)+lecture.getCommitedConflicts(placement));
			setNrHardStudentConflicts(lecture.countHardStudentConflicts(placement));
			setNrCommitedStudentConflicts(lecture.getCommitedConflicts(placement) + lecture.countCommittedStudentConflicts(placement));
			setNrDistanceStudentConflicts(lecture.countDistanceStudentConflicts(placement));
		}
		setNrRoomLocations(lecture.nrRoomLocations());
		setNrTimeLocations(lecture.nrTimeLocations());
		if (lecture.nrTimeLocations()==1 && placement.getTimeLocation().getPreference()==0)
			setTimePreference(PreferenceLevel.sIntLevelRequired);
		else
			setTimePreference(placement.getTimeLocation().getPreference());
		if (placement.isMultiRoom()) {
			for (RoomLocation r: placement.getRoomLocations()) {
				if (lecture.nrRoomLocations()==lecture.getNrRooms() && r.getPreference()==0)
					setRoomPreference(r.getId(),PreferenceLevel.sIntLevelRequired);
				else
					setRoomPreference(r.getId(),r.getPreference());
			}
		} else {
			if (lecture.nrRoomLocations()==1 && placement.getRoomLocation().getPreference()==0)
				setRoomPreference(placement.getRoomLocation().getId(),PreferenceLevel.sIntLevelRequired);
			else
				setRoomPreference(placement.getRoomLocation().getId(),placement.getRoomLocation().getPreference());
		}
		if (includeConflictInfo) {
			int nrSameRoomPlacementsNoConf = 0;
			int nrSameTimePlacementsNoConf = 0;
			int nrPlacementsNoConf = 0;
			for (Placement p: lecture.values()) {
				if (p.isHard()) continue;
				if (p.equals(placement)) continue;
				if (!lecture.getModel().conflictValues(p).isEmpty()) continue;
				if (p.getTimeLocation().equals(placement.getTimeLocation())) {
					nrSameTimePlacementsNoConf++;
				}
				if (p.sameRooms(placement))
					nrSameRoomPlacementsNoConf++;
				nrPlacementsNoConf++;
			}
			setNrPlacementsNoConf(nrPlacementsNoConf);
			setNrSameRoomPlacementsNoConf(nrSameRoomPlacementsNoConf);
			setNrSameTimePlacementsNoConf(nrSameTimePlacementsNoConf);
		}
		int btbInstructorPref = 0;
       	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
       		btbInstructorPref += ic.getPreferenceCombination(placement);
       	}
       	setBtbInstructorPreference(btbInstructorPref);
		if (lecture.getInitialAssignment()!=null) {
			setInitialAssignment(lecture.getInitialAssignment().getName());
			setIsInitial(placement.equals(lecture.getInitialAssignment()));
			setHasInitialSameTime(placement.getTimeLocation().equals(((Placement)lecture.getInitialAssignment()).getTimeLocation()));
			setHasInitialSameRoom(placement.sameRooms((Placement)lecture.getInitialAssignment()));
		}
		iTooBigRoomPreference = TooBigRooms.getTooBigRoomPreference(placement);
		iMinRoomSize = lecture.minRoomSize();
		iUselessHalfHours = placement.variable().getModel() == null ? 0 : (int)Math.round(
				placement.variable().getModel().getCriterion(UselessHalfHours.class).getValue(placement, null) + 
				placement.variable().getModel().getCriterion(BrokenTimePatterns.class).getValue(placement, null));
		DepartmentSpreadConstraint deptConstraint = null;
		for (Constraint c: lecture.constraints()) {
			if (c instanceof DepartmentSpreadConstraint)
				deptConstraint = (DepartmentSpreadConstraint)c;
			if (c instanceof GroupConstraint)
				iGroupConstraintPref += ((GroupConstraint)c).getCurrentPreference(placement);
		}
		if (deptConstraint!=null) {
			iDeptBalancPenalty = ((double)deptConstraint.getPenalty(placement))/12.0;
			iMaxDeptBalancPenalty = deptConstraint.getMaxPenalty(placement);
		}
		iSpreadPenalty = ((double)placement.getSpreadPenalty())/12.0;
		iMaxSpreadPenalty = ((double)placement.getMaxSpreadPenalty())/12.0;
		if (solver!=null && solver.getPerturbationsCounter()!=null)
			setPerturbationPenalty(solver.getPerturbationsCounter().getPerturbationPenalty(lecture.getModel(), placement, new Vector(0)));
		setDatePatternPref(placement.getTimeLocation().getDatePatternPreference());
	}
	
	public double getNormalizedTimePreference() { return iNormalizedTimePreference; }
	public void setNormalizedTimePreference(double normalizedTimePreference) { iNormalizedTimePreference = normalizedTimePreference; }
	public double getBestNormalizedTimePreference() { return iBestNormalizedTimePreference; }
	public void setBestNormalizedTimePreference(double bestNormalizedTimePreference) { iBestNormalizedTimePreference = bestNormalizedTimePreference; }
	public int getTimePreference() { return iTimePreference; }
	public void setTimePreference(int timePreference) { iTimePreference = timePreference; }
	public int getRoomPreference(Long roomId) { 
		Integer pref = (Integer)iRoomPreference.get(roomId);
		return (pref==null?0:pref.intValue());
	}
	public int sumRoomPreference() {
		int ret = 0;
		for (Iterator i=iRoomPreference.values().iterator();i.hasNext();)
			ret += ((Integer)i.next()).intValue();
	    return ret;
	}
	public int combineRoomPreference() {
		PreferenceCombination p = PreferenceCombination.getDefault();  
		for (Iterator i=iRoomPreference.values().iterator();i.hasNext();)
			p.addPreferenceInt(((Integer)i.next()).intValue());
	    return p.getPreferenceInt();
	}
	public void setRoomPreference(Long roomId, int roomPreference) { iRoomPreference.put(roomId,new Integer(roomPreference)); }
	public int getBestRoomPreference() { return iBestRoomPreference; }
	public void setBestRoomPreference(int bestRoomPreference) { iBestRoomPreference = bestRoomPreference; }
	public int getNrStudentConflicts() { return iNrStudentConflicts; }
	public void setNrStudentConflicts(int nrStudentConflicts) { iNrStudentConflicts = nrStudentConflicts; }
	public int getNrHardStudentConflicts() { return iNrHardStudentConflicts; }
	public void setNrHardStudentConflicts(int nrHardStudentConflicts) { iNrHardStudentConflicts = nrHardStudentConflicts; }
	public int getNrDistanceStudentConflicts() { return iNrDistanceStudentConflicts; }
	public void setNrDistanceStudentConflicts(int nrDistanceStudentConflicts) { iNrDistanceStudentConflicts = nrDistanceStudentConflicts; }
	public int getNrCommitedStudentConflicts() { return iNrCommitedStudentConflicts; }
	public void setNrCommitedStudentConflicts(int nrCommitedStudentConflicts) { iNrCommitedStudentConflicts = nrCommitedStudentConflicts; }
	public int getNrTimeLocations() { return iNrTimeLocations; }
	public void setNrTimeLocations(int nrTimeLocations) { iNrTimeLocations = nrTimeLocations; }
	public int getNrRoomLocations() { return iNrRoomLocations; }
	public void setNrRoomLocations(int nrRoomLocations) { iNrRoomLocations = nrRoomLocations; }
	public int getNrSameRoomPlacementsNoConf() { return iNrSameRoomPlacementsNoConf; }
	public void setNrSameRoomPlacementsNoConf(int nrSameRoomPlacementsNoConf) { iNrSameRoomPlacementsNoConf = nrSameRoomPlacementsNoConf; }
	public int getNrSameTimePlacementsNoConf() { return iNrSameTimePlacementsNoConf; }
	public void setNrSameTimePlacementsNoConf(int nrSameTimePlacementsNoConf) { iNrSameTimePlacementsNoConf = nrSameTimePlacementsNoConf; }
	public int getNrPlacementsNoConf() { return iNrPlacementsNoConf; }
	public void setNrPlacementsNoConf(int nrPlacementsNoConf) { iNrPlacementsNoConf = nrPlacementsNoConf; }
	public int getBtbInstructorPreference() { return iBtbInstructorPreference; }
	public void setBtbInstructorPreference(int btbInstructorPreference) { iBtbInstructorPreference = btbInstructorPreference; }
	public boolean getIsInitial() { return iIsInitial; }
	public void setIsInitial(boolean isInitial) { iIsInitial = isInitial; }
	public String getInitialAssignment() { return iInitialAssignment; }
	public void setInitialAssignment(String initialAssignment) { iInitialAssignment = initialAssignment; }
	public boolean getHasInitialSameTime() { return iHasInitialSameTime; }
	public void setHasInitialSameTime(boolean hasInitialSameTime) { iHasInitialSameTime = hasInitialSameTime; }
	public boolean getHasInitialSameRoom() { return iHasInitialSameRoom; }
	public void setHasInitialSameRoom(boolean hasInitialSameRoom) { iHasInitialSameRoom = hasInitialSameRoom; }
	public double getPerturbationPenalty() { return iPerturbationPenalty; }
	public void setPerturbationPenalty(double perturbationPenalty) { iPerturbationPenalty = perturbationPenalty; }
	public int getTooBigRoomPreference() { return iTooBigRoomPreference; } 
	public void setTooBigRoomPreference(int tooBigRoomPreference) { iTooBigRoomPreference = tooBigRoomPreference; }
	public long getMinRoomSize() { return iMinRoomSize; } 
	public void setMinRoomSize(long minRoomSize) { iMinRoomSize = minRoomSize; }
	public int getUselessHalfHours() { return iUselessHalfHours; }
	public void setUselessHalfHours(int uselessHalfHours) { iUselessHalfHours = uselessHalfHours; }
	public double getDeptBalancPenalty() { return iDeptBalancPenalty; }
	public void setDeptBalancPenalty(double deptBalancPenalty) { iDeptBalancPenalty = deptBalancPenalty; }
	public int getMaxDeptBalancPenalty() { return iMaxDeptBalancPenalty; }
	public void setMaxDeptBalancPenalty(int deptBalancPenalty) { iMaxDeptBalancPenalty = deptBalancPenalty; }
	public int getGroupConstraintPref() { return iGroupConstraintPref; }
	public void setGroupConstraintPref(int groupConstraintPref) { iGroupConstraintPref = groupConstraintPref; }
	public double getSpreadPenalty() { return iSpreadPenalty; }
	public void setSpreadPenalty(double spreadPenalty) { iSpreadPenalty = spreadPenalty; }
	public double getMaxSpreadPenalty() { return iMaxSpreadPenalty; }
	public void setMaxSpreadPenalty(double spreadPenalty) { iMaxSpreadPenalty = spreadPenalty; }
	public int getDatePatternPref() { return iDatePatternPref; }
	public void setDatePatternPref(int datePatternPref) { iDatePatternPref = datePatternPref; }
	
	public void load(Element root) throws Exception {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==sVersion) {
			iNormalizedTimePreference = Double.parseDouble(root.elementText("normTimePref"));
			iBestNormalizedTimePreference = Double.parseDouble(root.elementText("bestNormTimePref"));
			iTimePreference = Integer.parseInt(root.elementText("timePref"));
			for (Iterator i=root.elementIterator("roomPref");i.hasNext();) {
				Element e = (Element)i.next();
				iRoomPreference.put(Long.valueOf(e.attributeValue("id")),Integer.valueOf(e.getText()));
			}
			iBestRoomPreference = Integer.parseInt(root.elementText("bestRoomPref"));
			iNrStudentConflicts = Integer.parseInt(root.elementText("nrStudentConf"));
			iNrHardStudentConflicts = Integer.parseInt(root.elementText("nrHardStudentConf"));
			iNrDistanceStudentConflicts = Integer.parseInt(root.elementText("nrDistanceStudentConf"));
			iNrCommitedStudentConflicts = Integer.parseInt(root.elementText("nrCommitedStudentConf"));
			iNrTimeLocations = Integer.parseInt(root.elementText("nrTimeLoc"));
			iNrRoomLocations = Integer.parseInt(root.elementText("nrRoomLoc"));
			iNrSameRoomPlacementsNoConf = Integer.parseInt(root.elementText("nrSameRoomPlacNoConf"));
			iNrSameTimePlacementsNoConf = Integer.parseInt(root.elementText("nrSameTimePlacNoConf"));
			iNrPlacementsNoConf = Integer.parseInt(root.elementText("nrPlacNoConf"));
			iBtbInstructorPreference = Integer.parseInt(root.elementText("btbInstrPref"));
			iIsInitial = Boolean.valueOf(root.elementText("isInitial")).booleanValue();
			iInitialAssignment = root.elementText("iniAssign");
			iHasInitialSameTime = Boolean.valueOf(root.elementText("hasIniSameTime")).booleanValue();
			iHasInitialSameRoom = Boolean.valueOf(root.elementText("hasIniSameRoom")).booleanValue();
			iPerturbationPenalty = Double.parseDouble(root.elementText("pertPenalty"));
			iTooBigRoomPreference = Integer.parseInt(root.elementText("tooBig"));
			iMinRoomSize = Long.parseLong(root.elementText("minSize"));
			iUselessHalfHours = Integer.parseInt(root.elementText("uselessHalfHours"));
			iDeptBalancPenalty = Double.parseDouble(root.elementText("deptBalanc"));
			iGroupConstraintPref = Integer.parseInt(root.elementText("groupConstr"));
			if (root.elementText("spread")!=null)
				iSpreadPenalty = Double.parseDouble(root.elementText("spread"));
			if (root.elementText("maxSpread")!=null)
				iMaxSpreadPenalty = Double.parseDouble(root.elementText("maxSpread"));
			else
				iMaxSpreadPenalty = iSpreadPenalty;
			if (root.elementText("maxDeptBalanc")!=null)
				iMaxDeptBalancPenalty = Integer.parseInt(root.elementText("maxDeptBalanc"));
			else
				iMaxDeptBalancPenalty = (int)iDeptBalancPenalty;
			if (root.elementText("datePref") != null)
				iDatePatternPref = Integer.parseInt(root.elementText("datePref"));
		}
	}
	
	public void save(Element root) throws Exception {
		root.addAttribute("version", String.valueOf(sVersion));
		root.addElement("normTimePref").setText(String.valueOf(iNormalizedTimePreference));
		root.addElement("bestNormTimePref").setText(String.valueOf(iBestNormalizedTimePreference));
		root.addElement("timePref").setText(String.valueOf(iTimePreference));
		for (Iterator i=iRoomPreference.entrySet().iterator();i.hasNext();) {
			Map.Entry entry = (Map.Entry)i.next();
			root.addElement("roomPref").addAttribute("id",entry.getKey().toString()).setText(entry.getValue().toString());
		}
		root.addElement("bestRoomPref").setText(String.valueOf(iBestRoomPreference));
		root.addElement("nrStudentConf").setText(String.valueOf(iNrStudentConflicts));
		root.addElement("nrHardStudentConf").setText(String.valueOf(iNrHardStudentConflicts));		
		root.addElement("nrCommitedStudentConf").setText(String.valueOf(iNrCommitedStudentConflicts));		
		root.addElement("nrDistanceStudentConf").setText(String.valueOf(iNrDistanceStudentConflicts));		
		root.addElement("nrTimeLoc").setText(String.valueOf(iNrTimeLocations));
		root.addElement("nrRoomLoc").setText(String.valueOf(iNrRoomLocations));
		root.addElement("nrSameRoomPlacNoConf").setText(String.valueOf(iNrSameRoomPlacementsNoConf));
		root.addElement("nrSameTimePlacNoConf").setText(String.valueOf(iNrSameTimePlacementsNoConf));
		root.addElement("nrPlacNoConf").setText(String.valueOf(iNrPlacementsNoConf));
		root.addElement("btbInstrPref").setText(String.valueOf(iBtbInstructorPreference));
		root.addElement("isInitial").setText(String.valueOf(iIsInitial));
		if (iInitialAssignment!=null)
			root.addElement("iniAssign").setText(iInitialAssignment);
		root.addElement("hasIniSameTime").setText(String.valueOf(iHasInitialSameTime));
		root.addElement("hasIniSameRoom").setText(String.valueOf(iHasInitialSameRoom));
		root.addElement("pertPenalty").setText(String.valueOf(iPerturbationPenalty));
		root.addElement("tooBig").setText(String.valueOf(iTooBigRoomPreference));
		root.addElement("minSize").setText(String.valueOf(iMinRoomSize));
		root.addElement("uselessHalfHours").setText(String.valueOf(iUselessHalfHours));
		root.addElement("deptBalanc").setText(String.valueOf(iDeptBalancPenalty));
		root.addElement("maxDeptBalanc").setText(String.valueOf(iMaxDeptBalancPenalty));
		root.addElement("groupConstr").setText(String.valueOf(iGroupConstraintPref));
		root.addElement("spread").setText(String.valueOf(iSpreadPenalty));
		root.addElement("maxSpread").setText(String.valueOf(iMaxSpreadPenalty));
		root.addElement("datePref").setText(String.valueOf(iDatePatternPref));
	}
	
	public boolean saveToFile() {
		return false;
	}
    
    public String toString() {
        return "AssignmentPreferneceInfo{\n"+
            "  normTimePref="+iNormalizedTimePreference+"\n"+
            "  bestNormTimePref = "+iBestNormalizedTimePreference+"\n"+
            "  timePref = "+iTimePreference+"\n"+
            "  roomPreference = "+iRoomPreference+"\n"+
            "  bestRoomPref = "+iBestRoomPreference+"\n"+
            "  nrStudentConf = "+iNrStudentConflicts+"\n"+
            "  nrHardStudentConf = "+iNrHardStudentConflicts+"\n"+      
            "  nrCommitedStudentConf = "+iNrCommitedStudentConflicts+"\n"+      
            "  nrDistanceStudentConf = "+iNrDistanceStudentConflicts+"\n"+      
            "  nrTimeLoc = "+iNrTimeLocations+"\n"+
            "  nrRoomLoc = "+iNrRoomLocations+"\n"+
            "  nrSameRoomPlacNoConf = "+iNrSameRoomPlacementsNoConf+"\n"+
            "  nrSameTimePlacNoConf = "+iNrSameTimePlacementsNoConf+"\n"+
            "  nrPlacNoConf = "+iNrPlacementsNoConf+"\n"+
            "  btbInstrPref = "+iBtbInstructorPreference+"\n"+
            "  isInitial = "+iIsInitial+"\n"+
            "  iniAssign = "+iInitialAssignment+"\n"+
            "  hasIniSameTime = "+iHasInitialSameTime+"\n"+
            "  hasIniSameRoom = "+iHasInitialSameRoom+"\n"+
            "  pertPenalty = "+iPerturbationPenalty+"\n"+
            "  tooBig = "+iTooBigRoomPreference+"\n"+
            "  minSize = "+iMinRoomSize+"\n"+
            "  uselessHalfHours = "+iUselessHalfHours+"\n"+
            "  deptBalanc = "+iDeptBalancPenalty+"\n"+
            "  maxDeptBalanc = "+iMaxDeptBalancPenalty+"\n"+
            "  groupConstr = "+iGroupConstraintPref+"\n"+
            "  spread = "+iSpreadPenalty+"\n"+
            "  maxSpread = "+iMaxSpreadPenalty+"\n"+
            "  datePatterPref = "+iDatePatternPref+"\n"+
            "}";
    }
}
