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
package org.unitime.timetable.form;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;

/**
 * @author Zuzana Mullerova, Tomas Muller
 */
public class SponsoringOrgEditForm extends ActionForm {

	private static final long serialVersionUID = 5587107210035732698L;

	private String iScreen = "edit";
	private String iOrgName;
	private String iOrgEmail;
	private String iOp;
	private SponsoringOrganization iOrg;
	private Long iId;
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();
		
		if (iOrgName==null || iOrgName.length()==0) {
			errors.add("orgName", new ActionMessage("errors.generic", "Please enter the name of the organization."));
		} else {
			for (Iterator i=SponsoringOrganization.findAll().iterator(); i.hasNext();) {
				SponsoringOrganization so2 = (SponsoringOrganization) i.next();
				if (iOrgName.compareToIgnoreCase(so2.getName())==0) {
					if (iId != null) {
						if (iId.compareTo(so2.getUniqueId())!=0) {
							errors.add("orgNameExists", new ActionMessage("errors.generic", "Another organization with this name already exists."));
							break;
						}
					} else {
						errors.add("orgNameExists", new ActionMessage("errors.generic", "Another organization with this name already exists."));
						break;
					}
				}
			}
		}
			
		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		if ("add".equals(request.getAttribute("op"))) {
			iScreen = "add";
			iOrg = null;
		} else if (request.getParameter("id")!=null && request.getParameter("id").length()>0) {
			iId = Long.valueOf(request.getParameter("id"));
			iOrg = SponsoringOrganizationDAO.getInstance().get(iId);
		}
		iOrgName=(iOrg==null?"":iOrg.getName());
		iOrgEmail=(iOrg==null?"":iOrg.getEmail());
	}
	
	public String getScreen() {return iScreen;}
	public void setScreen(String screen) {iScreen = screen;}

	public String getOrgName() {return iOrgName;}
	public void setOrgName(String name) {iOrgName = name;}
	
	public String getOrgEmail() {return iOrgEmail;}
	public void setOrgEmail(String email) {iOrgEmail = email;}
	
	public String getOp() {return iOp;}
	public void setOp(String op) {iOp = op;}
	
	public Long getId() {return iId;}
	public void setId(Long id) {iId = id;}
	
	public SponsoringOrganization getOrg() {return iOrg;}
	public void setOrg(SponsoringOrganization org) {iOrg = org;}
	
}
