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

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.solver.course.ui.ClassInfoModel;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ClassInfoForm extends ActionForm {
	private static final long serialVersionUID = -9085986972061220089L;
	private String iOp;
    private ClassInfoModel iModel;
    private String iMessage;

    private String iMinRoomSize = null, iMaxRoomSize = null;
    private String iRoomFilter = null;
    private boolean iAllowRoomConflict = false;
    private String iRoomOrder = null;
    private RoomBase iRoomBase = RoomBase.Departmental;
    private boolean iKeepConflictingAssignments = false;
    public static String sRoomOrdNameAsc = "Name [asc]";
    public static String sRoomOrdNameDesc = "Name [desc]";
    public static String sRoomOrdSizeAsc = "Size [asc]";
    public static String sRoomOrdSizeDesc = "Size [desc]";
    public static String[] sRoomOrds = new String[] { sRoomOrdNameAsc, sRoomOrdNameDesc, sRoomOrdSizeAsc, sRoomOrdSizeDesc };
	private Long[] iRoomFeatures = null;
	private Long[] iRoomTypes = null;
	private Long[] iRoomGroups = null;
	private Long iSessionId = null;
    
    private String iFilter = null;
    
    public static enum RoomBase {
    	Departmental("Departmental"),
    	Timetabling("Timetabling"),
    	All("All"),
    	;
    	
    	private String iLabel;
    	RoomBase(String label) { iLabel = label; }
    	
    	public String getValue() { return name(); }
    	public String getLabel() { return iLabel; }
    }
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        iOp = null;
        iModel = null;
        iMessage = null;
        iMinRoomSize = null;
        iMaxRoomSize = null;
        iRoomFilter = null;
        iAllowRoomConflict = false;
        iRoomBase = RoomBase.Departmental;
        iRoomOrder = sRoomOrdNameAsc;
        iFilter = null;
        iKeepConflictingAssignments = false;
        iRoomTypes = null;
        iRoomFeatures = null;
        iRoomGroups = null;
        iSessionId = HttpSessionContext.getSessionContext(request.getSession().getServletContext()).getUser().getCurrentAcademicSessionId();
    }
    
    public void load(HttpSession session) {
        iRoomOrder = (String)session.getAttribute("ClassInfo.RoomOrd");
        iMinRoomSize = (String)session.getAttribute("ClassInfo.MinRoomSize");
        iMaxRoomSize = (String)session.getAttribute("ClassInfo.MaxRoomSize");
        iRoomFilter = (String)session.getAttribute("ClassInfo.RoomFilter");
        iAllowRoomConflict = "true".equals(session.getAttribute("ClassInfo.AllowRoomConflict"));
        String base = (String)session.getAttribute("ClassInfo.RoomBase");
        if (base == null || base.isEmpty())
        	iRoomBase = RoomBase.Departmental;
        else
        	iRoomBase = RoomBase.valueOf(base);
        iFilter = (String)session.getAttribute("ClassInfo.Filter");
        iKeepConflictingAssignments = "true".equals(session.getAttribute("ClassInfo.KeepConflictingAssignments"));
        iRoomTypes = (Long[]) session.getAttribute("ClassInfo.RoomTypes");
        iRoomGroups = (Long[]) session.getAttribute("ClassInfo.RoomGroups");
		iRoomFeatures = (Long[]) session.getAttribute("ClassInfo.RoomFeatures");
    }
    
    public void save(HttpSession session) {
        if (iRoomOrder==null)
            session.removeAttribute("ClassInfo.RoomOrd");
        else
            session.setAttribute("ClassInfo.RoomOrd", iRoomOrder);
        if (iMinRoomSize==null)
            session.removeAttribute("ClassInfo.MinRoomSize");
        else
            session.setAttribute("ClassInfo.MinRoomSize", iMinRoomSize);
        if (iMaxRoomSize==null)
            session.removeAttribute("ClassInfo.MaxRoomSize");
        else
            session.setAttribute("ClassInfo.MaxRoomSize", iMaxRoomSize);
        if (iRoomFilter==null)
            session.removeAttribute("ClassInfo.RoomFilter");
        else
            session.setAttribute("ClassInfo.RoomFilter", iRoomFilter);
        session.setAttribute("ClassInfo.AllowRoomConflict", (iAllowRoomConflict?"true":"false"));
        session.setAttribute("ClassInfo.RoomBase", iRoomBase == null ? RoomBase.Departmental.name() : iRoomBase.name());
        if (!iKeepConflictingAssignments)
        	session.removeAttribute("ClassInfo.KeepConflictingAssignments");
        else
        	session.setAttribute("ClassInfo.KeepConflictingAssignments", (iKeepConflictingAssignments?"true":"false"));

        if (iFilter==null)
            session.removeAttribute("ClassInfo.Filter");
        else
            session.setAttribute("ClassInfo.Filter", iFilter);
        
        if (iRoomTypes==null)
        	session.removeAttribute("ClassInfo.RoomTypes");
        else
        	session.setAttribute("ClassInfo.RoomTypes", iRoomTypes);
        if (iRoomGroups==null)
        	session.removeAttribute("ClassInfo.RoomGroups");
        else
        	session.setAttribute("ClassInfo.RoomGroups", iRoomGroups);
        if (iRoomFeatures==null)
        	session.removeAttribute("ClassInfo.RoomFeatures");
        else
        	session.setAttribute("ClassInfo.RoomFeatures", iRoomFeatures);
    }
    
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public ClassInfoModel getModel() { return iModel; }
    public void setModel(ClassInfoModel model) { iModel = model; }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
    
    public String getMinRoomSize() { return iMinRoomSize; }
    public void setMinRoomSize(String minRoomSize) { iMinRoomSize = minRoomSize; }
    public String getMaxRoomSize() { return iMaxRoomSize; }
    public void setMaxRoomSize(String maxRoomSize) { iMaxRoomSize = maxRoomSize; }
    public String getRoomFilter() { return iRoomFilter; }
    public void setRoomFilter(String roomFilter) { iRoomFilter = roomFilter; }
    public boolean getAllowRoomConflict() { return iAllowRoomConflict; }
    public void setAllowRoomConflict(boolean allowRoomConflict) { iAllowRoomConflict = allowRoomConflict; }
    public String getRoomBase() { return iRoomBase == null ? RoomBase.Departmental.name() : iRoomBase.name(); }
    public RoomBase getRoomBaseEnum() { return iRoomBase == null ? RoomBase.Departmental : iRoomBase; }
    public RoomBase[] getRoomBases() { return RoomBase.values(); }
    public void setRoomBase(String base) { iRoomBase = (base == null || base.isEmpty() ? RoomBase.Departmental : RoomBase.valueOf(base)); }
    public String getRoomOrder() { return iRoomOrder; }
    public void setRoomOrder(String ord) { iRoomOrder = ord; }
    public String[] getRoomOrders() { return sRoomOrds; }
    public String getFilter() { return iFilter; }
    public void setFilter(String filter) { iFilter = filter; }

	public boolean getKeepConflictingAssignments() {
		return iKeepConflictingAssignments;
	}

	public void setKeepConflictingAssignments(boolean unassignConflictingAssignments) {
		iKeepConflictingAssignments = unassignConflictingAssignments;
	}
	
    public Long[] getRoomTypes() { return iRoomTypes; }
	public void setRoomTypes(Long[] rts) { iRoomTypes = rts; }

    public Long[] getRoomGroups() { return iRoomGroups; }
    public void setRoomGroups(Long[] rgs) { iRoomGroups= rgs; }

    public Long[] getRoomFeatures() { return iRoomFeatures; }
    public void setRoomFeatures(Long[] rfs) { iRoomFeatures = rfs; }
    
    public Collection<RoomFeatureType> getRoomFeatureTypes() {
    	Set<RoomFeatureType> types = RoomFeatureType.getRoomFeatureTypes(iSessionId, false);
    	if (RoomFeatureType.hasRoomFeatureWithNoType(iSessionId, false)) {
    		RoomFeatureType f = new RoomFeatureType();
    		f.setUniqueId(-1l); f.setReference("Features"); f.setLabel("Room Features");
    		types.add(f);
    	}
    	return types;
    }
    
    public Collection<GlobalRoomFeature> getAllRoomFeatures(String featureType) {
    	return RoomFeature.getAllGlobalRoomFeatures(iSessionId, featureType == null || featureType.isEmpty() ? null : Long.valueOf(featureType));
    }
    
    public Collection<RoomGroup> getAllRoomGroups() {
        return RoomGroup.getAllGlobalRoomGroups(iSessionId);
    }

    public Collection<RoomType> getAllRoomTypes() {
        return RoomType.findAll(iSessionId);
    }
}
