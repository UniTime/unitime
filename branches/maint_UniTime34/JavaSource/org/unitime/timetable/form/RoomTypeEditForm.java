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

