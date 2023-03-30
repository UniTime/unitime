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

import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.dao.ItypeDescDAO;

/**
 * @author Tomas Muller
 */
public class ItypeDescEditForm implements UniTimeForm {
	private static final long serialVersionUID = 3555401912530970356L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	private Integer iUniqueId = null;
    private String iId = null;
    private String iReference = null;
    private String iName = null;
    private String iAbbreviation = null;
    private int iType = 1;
    private boolean iOrganized = false;
    private Integer iParent = null;

    @Override
	public void validate(UniTimeAction action) {
        try {
            if (iAbbreviation==null || iAbbreviation.trim().length()==0)
            	action.addFieldError("form.abbreviation", MSG.errorRequiredField(MSG.fieldAbbreviation()));
            
            if (iName==null || iName.trim().length()==0)
                action.addFieldError("form.name", MSG.errorRequiredField(MSG.fieldName()));
            
            try {
                if (iId==null || iId.trim().length()==0) {
                    action.addFieldError("form.id", MSG.errorRequiredField(MSG.fieldIType()));
                } else {
                    Integer id = Integer.valueOf(iId);
                    ItypeDesc itype = new ItypeDescDAO().get(id);
                    if (itype!=null && (iUniqueId==null || iUniqueId<0 || itype.equals(iUniqueId)))
                        action.addFieldError("form.id", MSG.errorAlreadyExists(iId));
                    
                    itype = (ItypeDesc)ItypeDescDAO.getInstance().getSession().createQuery(
                    		"from ItypeDesc x where x.abbv = :abbv and x.id != :id")
                    		.setParameter("abbv", iAbbreviation, org.hibernate.type.StringType.INSTANCE).setParameter("id", id, org.hibernate.type.IntegerType.INSTANCE).setMaxResults(1).uniqueResult();
                    if (itype != null)
                    	action.addFieldError("abbreviation", MSG.errorAlreadyExists(iAbbreviation));
                    		
                    itype = (ItypeDesc)ItypeDescDAO.getInstance().getSession().createQuery(
                    		"from ItypeDesc x where x.desc = :name and x.id != :id")
                    		.setParameter("name", iName, org.hibernate.type.StringType.INSTANCE).setParameter("id", id, org.hibernate.type.IntegerType.INSTANCE).setMaxResults(1).uniqueResult();
                    if (itype != null)
                    	action.addFieldError("form.name", MSG.errorAlreadyExists(iName));
                }
            } catch (NumberFormatException e) {
                action.addFieldError("form.id", MSG.errorNotNumber(iId));
            }
        } catch (Exception e) {
            Debug.error(e);
            action.addFieldError("form.id", e.getMessage());
        }
    }

    @Override
	public void reset() {
        iId = null; iUniqueId = -1;
        iAbbreviation = null; iReference = null; iName = null;
        iType = 1; iOrganized = false;
	}
	
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
