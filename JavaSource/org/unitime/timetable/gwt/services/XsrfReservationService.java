package org.unitime.timetable.gwt.services;

import java.util.List;

import org.unitime.timetable.gwt.command.client.XsrfBaseWrapper;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.ReservationException;
import org.unitime.timetable.gwt.shared.ReservationInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;

public class XsrfReservationService extends XsrfBaseWrapper implements ReservationServiceAsync  {
	ReservationServiceAsync iRPC;
	
	public XsrfReservationService(ReservationServiceAsync rpc) {
		super((HasRpcToken) rpc);
		iRPC = rpc;
	}
	

	@Override
	public void getOffering(final Long offeringId, final Long courseId, final AsyncCallback<ReservationInterface.Offering> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getOffering(offeringId, courseId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getOfferingByCourseName(final String course, final AsyncCallback<ReservationInterface.Offering> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getOfferingByCourseName(course, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getAreas(final AsyncCallback<List<ReservationInterface.Area>> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getAreas(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getStudentGroups(final AsyncCallback<List<ReservationInterface.IdName>> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getStudentGroups(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getCurricula(final Long offeringId, final AsyncCallback<List<ReservationInterface.Curriculum>> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getCurricula(offeringId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}


	@Override
	public void getReservation(final Long reservationId, final AsyncCallback<ReservationInterface> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getReservation(reservationId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getReservations(final Long offeringId, final AsyncCallback<List<ReservationInterface>> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getReservations(offeringId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void save(final ReservationInterface reservation, final AsyncCallback<Long> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.save(reservation, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void delete(final Long reservationId, final AsyncCallback<Boolean> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.delete(reservationId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void canAddReservation(final AsyncCallback<Boolean> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.canAddReservation(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void lastReservationFilter(final AsyncCallback<String> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.lastReservationFilter(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void findReservations(final ReservationInterface.ReservationFilterRpcRequest filter, final AsyncCallback<List<ReservationInterface>> callback)  throws ReservationException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.findReservations(filter, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
}
