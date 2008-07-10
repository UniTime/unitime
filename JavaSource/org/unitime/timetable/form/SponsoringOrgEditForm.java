package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class SponsoringOrgEditForm extends ActionForm {

	private String iScreen = "edit";
	private String iOrgName;
	private String iOrgEmail;
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		if ("add".equals(request.getAttribute("op"))) {
			iScreen = "add";
		} 
		iOrgName=null;
		iOrgEmail=null;
	}
	
	public String getScreen() {return iScreen;}
	public void setScreen(String screen) {iScreen = screen;}

	public String getOrgName() {return iOrgName;}
	public void setOrgName(String name) {iOrgName = name;}
	
	public String getOrgEmail() {return iOrgEmail;}
	public void setOrgEmail(String email) {iOrgEmail = email;}
	
}
