package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;

public class SponsoringOrgEditForm extends ActionForm {

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
