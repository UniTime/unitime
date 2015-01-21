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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.dao.RoomTypeDAO;


/** 
 * @author Tomas Muller
 */
public class RoomTypeEditForm extends ActionForm {
	private static final long serialVersionUID = 3139971302727896389L;
	private String iOp;
    private Long iUniqueId;
    private String iReference;
    private String iLabel;
    private boolean iCanEdit = false;
    private int iType = 0;
    private int iOrder = -1;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
        
        if(iReference==null || iReference.trim().length()==0)
            errors.add("reference", new ActionMessage("errors.required", ""));
		else {
			try {
				RoomType rt = RoomType.findByReference(iReference);
				if (rt!=null && !rt.getUniqueId().equals(iUniqueId))
					errors.add("reference", new ActionMessage("errors.exists", iReference));
			} catch (Exception e) {
				errors.add("reference", new ActionMessage("errors.generic", e.getMessage()));
			}
        }
        
        if(iLabel==null || iLabel.trim().length()==0)
            errors.add("label", new ActionMessage("errors.required", ""));
        
		return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = "List"; iUniqueId = new Long(-1);
        iReference = null; iLabel = null;
        iCanEdit = false; iType = 0;
        iOrder = RoomType.findAll().size();
	}
    
    public void setOp(String op) { iOp = op; }
    public String getOp() { return iOp; }
    public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
    public Long getUniqueId() { return iUniqueId; }
    public void setReference(String reference) { iReference = reference; }
    public String getReference() { return iReference; }
    public void setLabel(String label) { iLabel = label; }
    public String getLabel() { return iLabel; }
    public void setOrder(int order) { iOrder = order; }
    public int getOrder() { return iOrder; }
    public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }
    public boolean getCanEdit() { return iCanEdit; }
    public void setType(int type) { iType = type; }
    public int getType() { return iType; }
	
	public void load(RoomType t) {
		if (t==null) {
			reset(null, null);
			setCanEdit(true);
			setOp("Save");
		} else {
            setUniqueId(t.getUniqueId());
            setReference(t.getReference());
            setLabel(t.getLabel());
            setType(t.isRoom()?0:1);
            setCanEdit(t.countRooms()==0);
            setOrder(t.getOrd());
            setOp("Update");
		}
	}
	
	public RoomType saveOrUpdate(org.hibernate.Session hibSession) throws Exception {
	    RoomType t = null;
		if (getUniqueId().intValue()>=0)
			t = RoomTypeDAO.getInstance().get(getUniqueId());
		if (t==null) 
            t = new RoomType();
        t.setReference(getReference());
        t.setLabel(getLabel());
        t.setRoom(getType()==0);
        if (t.getOrd()==null) t.setOrd(RoomType.findAll().size());
        hibSession.saveOrUpdate(t);
        setUniqueId(t.getUniqueId());
        return t;
	}
	
	public void delete(org.hibernate.Session hibSession) throws Exception {
		if (getUniqueId().intValue()<0) return;
        RoomType t = RoomTypeDAO.getInstance().get(getUniqueId());
        if (t!=null) {
            for (RoomType other: RoomTypeDAO.getInstance().findAll(hibSession)) {
            	if (other.getOrd() > t.getOrd()) {
            		other.setOrd(other.getOrd() - 1);
                	hibSession.saveOrUpdate(other);
            	}
            }
        	hibSession.delete(t);
        }
	}
}

