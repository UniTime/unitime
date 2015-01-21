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
import org.unitime.timetable.solver.exam.ui.ExamInfoModel;

/**
 * @author Tomas Muller
 */
public class ExamInfoForm extends ActionForm {
	private static final long serialVersionUID = 424087977258798931L;
	private String iOp;
    private ExamInfoModel iModel;
    private String iMessage;

    private String iMinRoomSize = null, iMaxRoomSize = null;
    private String iRoomFilter = null;
    private boolean iAllowRoomConflict = false;
    private String iRoomOrder = null;
    public static String sRoomOrdNameAsc = "Name [asc]";
    public static String sRoomOrdNameDesc = "Name [desc]";
    public static String sRoomOrdSizeAsc = "Size [asc]";
    public static String sRoomOrdSizeDesc = "Size [desc]";
    public static String[] sRoomOrds = new String[] { sRoomOrdNameAsc, sRoomOrdNameDesc, sRoomOrdSizeAsc, sRoomOrdSizeDesc };
    
    private boolean iComputeSuggestions = false;
    private String iFilter = null;
    private int iDepth = 2;
    private long iTimeout = 5000;
    private int iLimit = 30;
    
	private Long[] iRoomFeatures = null;
	private Long[] iRoomTypes = null;
	private Long[] iRoomGroups = null;
	private Long iSessionId = null;
	private Long iExamTypeId = null;


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
        iRoomOrder = sRoomOrdNameAsc;
        iComputeSuggestions = false;
        iFilter = null;
        iDepth = 2;
        iTimeout = 5000;
        iLimit = 30;
        iRoomTypes = null;
        iRoomFeatures = null;
        iRoomGroups = null;
    }
    
    public void load(HttpSession session) {
        iRoomOrder = (String)session.getAttribute("ExamInfo.RoomOrd");
        iMinRoomSize = (String)session.getAttribute("ExamInfo.MinRoomSize");
        iMaxRoomSize = (String)session.getAttribute("ExamInfo.MaxRoomSize");
        iRoomFilter = (String)session.getAttribute("ExamInfo.RoomFilter");
        iAllowRoomConflict = "true".equals(session.getAttribute("ExamInfo.AllowRoomConflict"));
        iFilter = (String)session.getAttribute("ExamInfo.Filter");
        if (session.getAttribute("ExamInfo.Limit")!=null)
            iLimit = (Integer)session.getAttribute("ExamInfo.Limit");
        if (session.getAttribute("ExamInfo.Depth")!=null)
            iDepth = (Integer)session.getAttribute("ExamInfo.Depth");
        if (session.getAttribute("ExamInfo.Timeout")!=null)
            iTimeout = (Long)session.getAttribute("ExamInfo.Timeout");
        if (session.getAttribute("ExamInfo.ComputeSuggestions")!=null)
            iComputeSuggestions = (Boolean)session.getAttribute("ExamInfo.ComputeSuggestions");
        iRoomTypes = (Long[]) session.getAttribute("ExamInfo.RoomTypes");
        iRoomGroups = (Long[]) session.getAttribute("ExamInfo.RoomGroups");
		iRoomFeatures = (Long[]) session.getAttribute("ExamInfo.RoomFeatures");
    }
    
    public void save(HttpSession session) {
        if (iRoomOrder==null)
            session.removeAttribute("ExamInfo.RoomOrd");
        else
            session.setAttribute("ExamInfo.RoomOrd", iRoomOrder);
        if (iMinRoomSize==null)
            session.removeAttribute("ExamInfo.MinRoomSize");
        else
            session.setAttribute("ExamInfo.MinRoomSize", iMinRoomSize);
        if (iMaxRoomSize==null)
            session.removeAttribute("ExamInfo.MaxRoomSize");
        else
            session.setAttribute("ExamInfo.MaxRoomSize", iMaxRoomSize);
        if (iRoomFilter==null)
            session.removeAttribute("ExamInfo.RoomFilter");
        else
            session.setAttribute("ExamInfo.RoomFilter", iRoomFilter);
        session.setAttribute("ExamInfo.AllowRoomConflict", (iAllowRoomConflict?"true":"false"));
        if (iFilter==null)
            session.removeAttribute("ExamInfo.Filter");
        else
            session.setAttribute("ExamInfo.Filter", iFilter);
        session.setAttribute("ExamInfo.Depth", iDepth);
        session.setAttribute("ExamInfo.Timeout", iTimeout);
        session.setAttribute("ExamInfo.Limit", iLimit);
        session.setAttribute("ExamInfo.ComputeSuggestions", iComputeSuggestions);
        if (iRoomTypes==null)
        	session.removeAttribute("ExamInfo.RoomTypes");
        else
        	session.setAttribute("ExamInfo.RoomTypes", iRoomTypes);
        if (iRoomGroups==null)
        	session.removeAttribute("ExamInfo.RoomGroups");
        else
        	session.setAttribute("ExamInfo.RoomGroups", iRoomGroups);
        if (iRoomFeatures==null)
        	session.removeAttribute("ExamInfo.RoomFeatures");
        else
        	session.setAttribute("ExamInfo.RoomFeatures", iRoomFeatures);
    }
    
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public ExamInfoModel getModel() { return iModel; }
    public void setModel(ExamInfoModel model) { iModel = model; }
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
    public String getRoomOrder() { return iRoomOrder; }
    public void setRoomOrder(String ord) { iRoomOrder = ord; }
    public String[] getRoomOrders() { return sRoomOrds; }
    public boolean getComputeSuggestions() { return iComputeSuggestions; }
    public void setComputeSuggestions(boolean computeSuggestions) { iComputeSuggestions = computeSuggestions; }
    public int getLimit() { return iLimit; }
    public void setLimit(int limit) { iLimit = limit; }
    public int getDepth() { return iDepth; }
    public void setDepth(int depth) { iDepth = depth; }
    public long getTimeout() { return iTimeout; }
    public void setTimeout(long timeout) { iTimeout = timeout; }
    public String getFilter() { return iFilter; }
    public void setFilter(String filter) { iFilter = filter; }
    
    public Long[] getRoomTypes() { return iRoomTypes; }
	public void setRoomTypes(Long[] rts) { iRoomTypes = rts; }

    public Long[] getRoomGroups() { return iRoomGroups; }
    public void setRoomGroups(Long[] rgs) { iRoomGroups= rgs; }

    public Long[] getRoomFeatures() { return iRoomFeatures; }
    public void setRoomFeatures(Long[] rfs) { iRoomFeatures = rfs; }
    
    public Collection<RoomFeatureType> getRoomFeatureTypes() {
    	Set<RoomFeatureType> types = RoomFeatureType.getRoomFeatureTypes(iSessionId, iExamTypeId);
    	if (RoomFeatureType.hasRoomFeatureWithNoType(iSessionId, iExamTypeId)) {
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
    
    public Long getSessionId() { return iSessionId; }
    public void setSessionId(Long sessionId) { iSessionId = sessionId; }
    
    public Long getExamTypeId() { return iExamTypeId; }
    public void setExamTypeId(long examTypeId) { iExamTypeId = examTypeId; }
}
