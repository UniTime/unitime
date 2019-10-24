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
import org.unitime.commons.Debug;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.dao.ItypeDescDAO;

/** 
 * 
 * @author Tomas Muller
 * 
 */
public class ItypeDescEditForm extends ActionForm {
	private static final long serialVersionUID = -238147307633027599L;
	private Integer iUniqueId = null;
    private String iId = null;
	private String iOp = null;
    private String iReference = null;
    private String iName = null;
    private String iAbbreviation = null;
    private int iType = 1;
    private boolean iOrganized = false;
    private Integer iParent = null;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        try {
            if (iAbbreviation==null || iAbbreviation.trim().length()==0)
                errors.add("abbreviation", new ActionMessage("errors.required", ""));
            
            if (iName==null || iName.trim().length()==0)
                errors.add("name", new ActionMessage("errors.required", ""));
            
            try {
                if (iId==null || iId.trim().length()==0) {
                    errors.add("id", new ActionMessage("errors.required", ""));
                } else {
                    Integer id = Integer.valueOf(iId);
                    ItypeDesc itype = new ItypeDescDAO().get(id);
                    if (itype!=null && (iUniqueId==null || iUniqueId<0 || itype.equals(iUniqueId)))
                        errors.add("id", new ActionMessage("errors.exists", iId));
                    
                    itype = (ItypeDesc)ItypeDescDAO.getInstance().getSession().createQuery(
                    		"from ItypeDesc x where x.abbv = :abbv and x.id != :id")
                    		.setString("abbv", iAbbreviation).setInteger("id", id).setMaxResults(1).uniqueResult();
                    if (itype != null)
                    	errors.add("abbreviation", new ActionMessage("errors.exists", iAbbreviation));
                    		
                    itype = (ItypeDesc)ItypeDescDAO.getInstance().getSession().createQuery(
                    		"from ItypeDesc x where x.desc = :name and x.id != :id")
                    		.setString("name", iName).setInteger("id", id).setMaxResults(1).uniqueResult();
                    if (itype != null)
                    	errors.add("name", new ActionMessage("errors.exists", iName));
                }
            } catch (NumberFormatException e) {
                errors.add("id", new ActionMessage("errors.numeric", iId));
            }
        } catch (Exception e) {
            Debug.error(e);
            errors.add("id", new ActionMessage("errors.generic", e.getMessage()));
        }
        
        return errors;
    }

	public void reset(ActionMapping mapping, HttpServletRequest request) {
        iId = null; iOp = null; iUniqueId = -1;
        iAbbreviation = null; iReference = null; iName = null;
        iType = 1; iOrganized = false;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
    public Integer getUniqueId() { return iUniqueId; }
    public void setUniqueId(Integer uniqueId) { iUniqueId = uniqueId; }
    public String getId() { return iId; }
    public void setId(String id) { iId = id; }
    public String getAbbreviation() { return iAbbreviation; }
    public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }
    public String getName() { return iName; }
    public void setName(String name) { iName = name; }
    public String getReference() { return iReference; }
    public void setReference(String reference) { iReference = reference; }
    public String getType() { return ItypeDesc.sBasicTypes[iType]; }
    public void setType(String type) { 
        for (int i=0;i<ItypeDesc.sBasicTypes.length;i++)
            if (ItypeDesc.sBasicTypes[i].equals(type)) iType = i;
    }
    public int getBasicType() { return iType; }
    public void setBasicType(int type) { iType = type; }
    public String[] getTypes() { return ItypeDesc.sBasicTypes; }
    public boolean getOrganized() { return iOrganized; }
    public void setOrganized(boolean organized) { iOrganized = organized; }
    public Integer getParent() { return iParent; }
    public void setParent(Integer parent) { iParent = parent; }
    
    public void load(ItypeDesc itype) {
        setOp("Update");
        setId(itype.getItype().toString());
        setUniqueId(itype.getItype());
        setAbbreviation(itype.getAbbv());
        setName(itype.getDesc());
        setReference(itype.getSis_ref());
        setBasicType(itype.getBasic() ? 1 : 0);
        setParent(itype.getParent()==null?null:itype.getParent().getItype());
        setOrganized(itype.isOrganized());
    }
    
    public void saveOrUpdate(org.hibernate.Session hibSession) throws Exception {
        ItypeDesc itype = null;
        if (getUniqueId()!=null) itype = new ItypeDescDAO().get(getUniqueId());
        if (itype==null) itype = new ItypeDesc();
        itype.setItype(Integer.valueOf(getId()));
        itype.setAbbv(getAbbreviation());
        itype.setDesc(getName());
        itype.setSis_ref(getReference());
        itype.setBasic(getBasicType() == 1);
        itype.setParent(getParent()==null?null:new ItypeDescDAO().get(getParent()));
        itype.setOrganized(getOrganized());
        hibSession.saveOrUpdate(itype);
    }
    
    public void delete(org.hibernate.Session hibSession) {
        ItypeDesc itype = new ItypeDescDAO().get(getUniqueId());
        if (itype!=null) hibSession.delete(itype);
    }
}

