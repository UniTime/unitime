package org.unitime.timetable.gwt.command.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

public abstract class XsrfBaseWrapper {
	XsrfTokenServiceAsync iXSRF;
	HasRpcToken iTokenHolder;
	Throwable iTokenError = null;
	
	public XsrfBaseWrapper(HasRpcToken tokenHolder) {
		iTokenHolder = tokenHolder;
		iXSRF = (XsrfTokenServiceAsync)GWT.create( XsrfTokenService.class );
		((ServiceDefTarget)iXSRF).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
	}
	
	protected void checkToken(final AsyncCallback<Boolean> callback) {
		if (iTokenHolder.getRpcToken() != null) {
			callback.onSuccess(true);
		} else if (iTokenError != null) {
			callback.onFailure(iTokenError);
		} else {
			iXSRF.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
				@Override
				public void onSuccess(XsrfToken result) {
					iTokenHolder.setRpcToken(result);
					callback.onSuccess(true);
				}
				@Override
				public void onFailure(Throwable caught) {
					iTokenError = caught;
					callback.onFailure(iTokenError);
				}
			});
		}
	}
}
