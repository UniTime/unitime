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
package org.unitime.timetable.form;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePattern.DatePatternType;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DatePatternEditForm implements UniTimeForm {
	private static final long serialVersionUID = -929558620061783652L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String iOp;
    private String iName;
    private Long iUniqueId;
    private String iType;
    private boolean iIsUsed;
    private boolean iIsDefault;
    private boolean iVisible;
    private List<Long> iDepartmentIds = new ArrayList<Long>();
    private Long iDepartmentId;
    private List<Long> iParentIds = new ArrayList<Long>();
    private Long iParentId;
    private Long iSessionId;
    private String iNumberOfWeeks;
    private Long iPreviousId, iNextId;
    private List<Long> iChildrenIds = new ArrayList<Long>();
    private Long iChildId;
    
    public DatePatternEditForm() {
    	reset();
    }

    @Override
	public void validate(UniTimeAction action) {
		if (iName==null || iName.trim().isEmpty())
			action.addFieldError("form.name", MSG.errorRequiredField(MSG.columnDatePatternName()));
		else {
			try {
				DatePattern pat = DatePattern.findByName(iSessionId,iName);
				if (pat!=null && !pat.getUniqueId().equals(iUniqueId))
					action.addFieldError("form.name", MSG.errorAlreadyExists(iName));
			} catch (Exception e) {
				action.addFieldError("form.name", e.getMessage());	
			}
        }
        
		if (getTypeInt()<0)
			action.addFieldError("form.type", MSG.errorRequiredField(MSG.columnDatePatternType()));
		
		if (getTypeInt()!=DatePatternType.Extended.ordinal() && !iDepartmentIds.isEmpty() && getTypeInt()!=DatePatternType.PatternSet.ordinal())
			action.addFieldError("form.type", MSG.errorOnyExtDatePatternsHaveDepartments());
		
		if (getNumberOfWeeks() != null && !getNumberOfWeeks().isEmpty()) {
			DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(Localization.getJavaLocale()));
			try {
				df.parse(getNumberOfWeeks());
			} catch (ParseException e) {
				action.addFieldError("form.numberOfWeeks", MSG.errorNumberOfWeeksIsNotNumber());
			}
		}
		
		try {
			DatePattern dp = getDatePattern(action.getRequest());
			if (getTypeInt() == DatePatternType.PatternSet.ordinal()) {
				if (dp.size()!=0)
					action.addFieldError("form.type", MSG.errorAltPatternSetCannotHaveDates());
				if (getParentIds() != null && !getParentIds().isEmpty()) {
					action.addFieldError("form.type", MSG.errorAltPatternSetCannotHavePatternSet());
				}
			} else {
				if (dp.size()==0)
					action.addFieldError("form.pattern", MSG.errorRequiredField(MSG.columnDatePatternPattern()));
			}
			if (dp.getPattern().length() > 366)
				action.addFieldError("form.pattern", MSG.errorDatePatternCannotContainMoreThanAYear());
		} catch (Exception e) {}
	}
	
    @Override
	public void reset() {
		iOp = null; iUniqueId = Long.valueOf(-1); iType = DatePatternType.Standard.name(); 
		iIsUsed = false; iVisible = false; iName = ""; iIsDefault = false; iNumberOfWeeks = null;
		iDepartmentId = null; iDepartmentIds.clear(); iParentId = null; iParentIds.clear();
		iPreviousId = null; iNextId = null;
		iChildrenIds.clear(); iChildId = null;
	}
	
	public void load(DatePattern dp) {
		if (dp==null) {
			reset();
			iOp = MSG.actionSaveDatePattern();
			iVisible = true; iIsUsed = false; iIsDefault = false;
		} else {
			setName(dp.getName());
			setVisible(dp.isVisible().booleanValue());
			setIsUsed(dp.isUsed());
			setTypeInt(dp.getType().intValue());
			setUniqueId(dp.getUniqueId());
			setIsDefault(dp.isDefault());
			setSessionId(dp.getSession().getUniqueId());
			DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(Localization.getJavaLocale()));
			setNumberOfWeeks(dp.getNumberOfWeeks() == null ? "" : df.format(dp.getNumberOfWeeks()));
			
			iParentIds.clear();
			TreeSet parents = new TreeSet(dp.getParents());
			for (Iterator i=parents.iterator();i.hasNext();) {
				DatePattern d = (DatePattern)i.next();
				iParentIds.add(d.getUniqueId());
			}
			iDepartmentIds.clear();
			TreeSet depts = new TreeSet(dp.getDepartments());
			for (Iterator i=depts.iterator();i.hasNext();) {
				Department d = (Department)i.next();
				iDepartmentIds.add(d.getUniqueId());
			}
			iChildrenIds.clear();
			for (DatePattern ch: dp.findChildren()) {
				iChildrenIds.add(ch.getUniqueId());
			}
			iOp = MSG.actionUpdateDatePattern();
		}
	}
	
	public void update(DatePattern dp, HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
		dp.setName(getName());
		dp.setVisible(Boolean.valueOf(getVisible()));
		dp.setType(Integer.valueOf(getTypeInt()));
		dp.setPatternAndOffset(request);
		if (getNumberOfWeeks() != null && !getNumberOfWeeks().isEmpty()) {
			try {
				DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(Localization.getJavaLocale()));
				dp.setNumberOfWeeks(df.parse(getNumberOfWeeks()).floatValue());
			} catch (ParseException e) {
				dp.setNumberOfWeeks(null);
			}
		} else {
			dp.setNumberOfWeeks(null);
		}
		
		HashSet oldParents = new HashSet(dp.getParents());
		for (Long parentId: iParentIds) {
			DatePattern d = (new DatePatternDAO()).get(parentId,hibSession);
			if (d==null) continue;
			if (oldParents.remove(d)) {
				//not changed -> do nothing
			} else {
				dp.getParents().add(d);				
				hibSession.saveOrUpdate(dp);				
			}
		}
		for (Iterator i=oldParents.iterator();i.hasNext();) {
			DatePattern d = (DatePattern)i.next();
			dp.getParents().remove(d);			
			hibSession.saveOrUpdate(d);
		}
		
		HashSet oldDepts = new HashSet(dp.getDepartments());
		for (Long departmentId: iDepartmentIds) {
			Department d = (new DepartmentDAO()).get(departmentId,hibSession);
			if (d==null) continue;
			if (oldDepts.remove(d)) {
				//not changed -> do nothing
			} else {
				dp.getDepartments().add(d);
				d.getDatePatterns().add(dp);
				hibSession.saveOrUpdate(d);
			}
		}
		for (Iterator i=oldDepts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			dp.getDepartments().remove(d);
			d.getDatePatterns().remove(dp);
			hibSession.saveOrUpdate(d);
		}
		hibSession.saveOrUpdate(dp);
		
		if (dp.isPatternSet()) {
			List<DatePattern> oldChildren = dp.findChildren(hibSession);
			for (Long childId: iChildrenIds) {
				DatePattern d = (new DatePatternDAO()).get(childId,hibSession);
				if (d==null) continue;
				if (oldChildren.remove(d)) {
					//not changed -> do nothing
				} else {
					d.getParents().add(dp);
					hibSession.saveOrUpdate(d);
				}
			}
			for (DatePattern d: oldChildren) {
				d.getParents().remove(dp);
				hibSession.saveOrUpdate(d);
			}
		} else {
			List<DatePattern> oldChildren = dp.findChildren(hibSession);
			for (DatePattern d: oldChildren) {
				d.getParents().remove(dp);
				hibSession.saveOrUpdate(d);
			}
		}
	}
	
	public DatePattern create(HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
		DatePattern dp = new DatePattern();
		dp.setName(getName());
		dp.setSession(SessionDAO.getInstance().get(getSessionId(), hibSession));
		dp.setVisible(Boolean.valueOf(getVisible()));
		dp.setType(Integer.valueOf(getTypeInt()));
		dp.setPatternAndOffset(request);
		if (getNumberOfWeeks() != null && !getNumberOfWeeks().isEmpty()) {
			try {
				DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(Localization.getJavaLocale()));
				dp.setNumberOfWeeks(df.parse(getNumberOfWeeks()).floatValue());
			} catch (ParseException e) {
				dp.setNumberOfWeeks(null);
			}
		} else {
			dp.setNumberOfWeeks(null);
		}
		
		HashSet newParents = new HashSet();
		for (Long parentId: iParentIds) {
			DatePattern d = (new DatePatternDAO()).get(parentId,hibSession);
			if (d==null) continue;
			newParents.add(d);
		}
		dp.setParents(newParents);
		HashSet newDepts = new HashSet();
		for (Long departmentId: iDepartmentIds) {
			Department d = (new DepartmentDAO()).get(departmentId,hibSession);
			if (d==null) continue;
			newDepts.add(d);
		}
		dp.setDepartments(newDepts);
		hibSession.save(dp);
		for (Iterator i=newDepts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			d.getDatePatterns().add(dp);
			hibSession.saveOrUpdate(d);
		}
		setUniqueId(dp.getUniqueId());
		if (dp.isPatternSet()) {
			for (Long childId: iChildrenIds) {
				DatePattern d = (new DatePatternDAO()).get(childId,hibSession);
				if (d==null) continue;
				d.getParents().add(dp);
				hibSession.saveOrUpdate(d);
			}
		}
		return dp;
	}
	
	public DatePattern saveOrUpdate(HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
		DatePattern dp = null;
		if (getUniqueId().longValue()>=0)
			dp = (new DatePatternDAO()).get(getUniqueId());
		if (dp==null)
			dp = create(request, hibSession);
		else 
			update(dp, request, hibSession);
		return dp;
	}
	
	public void delete(org.hibernate.Session hibSession) throws Exception {
		if (getUniqueId().longValue()<0) return;
		if (getIsUsed()) return;
		DatePattern dp = (new DatePatternDAO()).get(getUniqueId(), hibSession);
		for (Iterator i=dp.getDepartments().iterator();i.hasNext();) {
			Department d = (Department)i.next();
			d.getDatePatterns().remove(dp);
			hibSession.saveOrUpdate(d);
		}
		for (Iterator i=dp.findChildren().iterator();i.hasNext();) {
			DatePattern d = (DatePattern)i.next();
			d.getParents().remove(dp);
			hibSession.saveOrUpdate(d);
		}
		dp.getParents().clear();
		hibSession.saveOrUpdate(dp);
		hibSession.delete(dp);
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
	public String getType() { return iType; }
	public void setType(String type) { iType = type; }
	public List<ComboBoxLookup> getTypes() {
		List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
		for (DatePatternType t: DatePatternType.values())
			ret.add(new ComboBoxLookup(t.getLabel(), t.name()));
		return ret;
	}
	public int getTypeInt() {
		for (int i=0;i<DatePatternType.values().length;i++)
			if (DatePatternType.values()[i].name().equals(iType)) return i;
		return -1;
	}
	public void setTypeInt(int type) { iType = (type<0?"":DatePatternType.values()[type].name()); }
	public boolean getIsUsed() { return iIsUsed; }
	public void setIsUsed(boolean isUsed) { iIsUsed = isUsed; }
	public boolean getVisible() { return iVisible; }
	public void setVisible(boolean visible) { iVisible = visible; }
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public boolean getIsDefault() { return iIsDefault; }
	public void setIsDefault(boolean isDefault) { iIsDefault = isDefault; }
	public List<Long> getDepartmentIds() { return iDepartmentIds; }
	public void setDepartmentIds(List<Long> departmentIds) { iDepartmentIds = departmentIds; }
	public Long getDepartmentIds(int idx) { return iDepartmentIds.get(idx); }
	public void setDepartmentIds(int idx, Long value) { iDepartmentIds.set(idx, value); }
	public Long getDepartmentId() { return iDepartmentId; }
	public void setDepartmentId(Long deptId) { iDepartmentId = deptId; }
	public Long getParentId() {return iParentId;}
	public void setParentId(Long parentId) {iParentId = parentId;}
	public List<Long> getParentIds() {return iParentIds;}
	public void setParentIds(List<Long> parentIds) {iParentIds = parentIds;}
	public Long getParentIds(int idx) { return iParentIds.get(idx); }
	public void setParentIds(int idx, Long value) { iParentIds.set(idx, value); }
	public String getNumberOfWeeks() { return iNumberOfWeeks; }
	public void setNumberOfWeeks(String numberOfWeeks) { iNumberOfWeeks = numberOfWeeks; }
	
	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }
	
	public Long getNextId() { return iNextId; }
	public void setNextId(Long nextId) { iNextId = nextId; }
	public boolean getHasNext() { return iNextId != null && iNextId >= 0; }
	public Long getPreviousId() { return iPreviousId; }
	public void setPreviousId(Long previousId) { iPreviousId = previousId; }
	public boolean getHasPrevious() { return iPreviousId != null && iPreviousId >= 0; }
	
	public Long getChildId() {return iChildId;}
	public void setChildId(Long childId) {iChildId = childId;}
	public List<Long> getChildrenIds() {return iChildrenIds;}
	public void setChildrenIds(List<Long> ChildrenIds) {iChildrenIds = ChildrenIds;}
	public Long getChildrenIds(int idx) { return iChildrenIds.get(idx); }
	public void setChildrenIds(int idx, Long value) { iChildrenIds.set(idx, value); }
	
	public DatePattern getDatePattern(HttpServletRequest request) throws Exception {
		DatePattern dp = null;
		if (getUniqueId()!=null) {
			dp = (new DatePatternDAO()).get(getUniqueId());
			if (dp!=null) dp = (DatePattern)dp.clone();
		}
		if (dp==null) {
			dp = new DatePattern();
		}
		if (dp.getSession()==null) {
			dp.setSession(SessionDAO.getInstance().get(getSessionId()));
		}
		if (request.getParameter("cal_select")!=null) {
			dp.setName(getName());
			dp.setVisible(Boolean.valueOf(getVisible()));
			dp.setType(Integer.valueOf(getTypeInt()));
			dp.setPatternAndOffset(request);
			if (getNumberOfWeeks() != null && !getNumberOfWeeks().isEmpty()) {
				try {
					DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(Localization.getJavaLocale()));
					dp.setNumberOfWeeks(df.parse(getNumberOfWeeks()).floatValue());
				} catch (ParseException e) {
					dp.setNumberOfWeeks(null);
				}
			} else {
				dp.setNumberOfWeeks(null);
			}
		}
		return dp;
	}
}
