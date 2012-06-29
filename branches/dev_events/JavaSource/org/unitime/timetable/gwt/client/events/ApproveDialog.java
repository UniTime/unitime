package org.unitime.timetable.gwt.client.events;

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.events.EventMeetingTable.EventMeetingRow;
import org.unitime.timetable.gwt.client.events.EventMeetingTable.MeetingFilter;
import org.unitime.timetable.gwt.client.events.EventMeetingTable.OperationType;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public abstract class ApproveDialog extends UniTimeDialogBox implements EventMeetingTable.Implementation {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private SimpleForm iForm;
	private TextArea iNotes;
	private EventMeetingTable iTable;
	private ListBox iStandardNotes;
	private UniTimeFileUpload iFileUpload;
	private UniTimeHeaderPanel iFooter;
	
	public ApproveDialog() {
		super(true, false);
		iTable = new EventMeetingTable(EventMeetingTable.Mode.ApprovalOfSingleEventMeetings, false);
		
		iForm = new SimpleForm();
		
		if (iTable instanceof Widget) {
			ScrollPanel scroll = new ScrollPanel((Widget)iTable);
			ToolBox.setMaxHeight(scroll.getElement().getStyle(), "200px");
			iForm.addRow(MESSAGES.propMeetings(), scroll);
		}
		
		iStandardNotes = new ListBox();
		iStandardNotes.setVisibleItemCount(4);
		iStandardNotes.setWidth("480px");
		iForm.addRow(MESSAGES.propStandardNotes(), iStandardNotes);
		iStandardNotes.addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				String text = iNotes.getText();
				if (!text.isEmpty() && !text.endsWith("\n"))
					text += "\n";
				text += iStandardNotes.getItemText(iStandardNotes.getSelectedIndex());
				iNotes.setText(text);
			}
		});
		iStandardNotes.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					String text = iNotes.getText();
					if (!text.isEmpty() && !text.endsWith("\n"))
						text += "\n";
					text += iStandardNotes.getItemText(iStandardNotes.getSelectedIndex());
					iNotes.setText(text);
					event.preventDefault();
					event.stopPropagation();
				}
			}
		});
		
		iNotes = new TextArea();
		iNotes.setStyleName("unitime-TextArea");
		iNotes.setVisibleLines(5);
		iNotes.setCharacterWidth(80);
		iForm.addRow(MESSAGES.propNotes(), iNotes);
		
		iFileUpload = new UniTimeFileUpload();
		iForm.addRow(MESSAGES.propAttachement(), iFileUpload);
		
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("approve", MESSAGES.opApprove(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSubmit(ApproveEventRpcRequest.Operation.APPROVE, iTable.getValue(), iNotes.getText());
				hide();
			}
		});
		iFooter.addButton("inquire", MESSAGES.opInquire(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSubmit(ApproveEventRpcRequest.Operation.INQUIRE, iTable.getValue(), iNotes.getText());
				hide();
			}
		});
		iFooter.addButton("reject", MESSAGES.opReject(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSubmit(ApproveEventRpcRequest.Operation.REJECT, iTable.getValue(), iNotes.getText());
				hide();
			}
		});
		iFooter.addButton("cancel", MESSAGES.onCancel(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iForm.addBottomRow(iFooter);
		
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		
		setWidget(iForm);
	}
	
	protected abstract void onSubmit(ApproveEventRpcRequest.Operation operation, List<EventMeetingRow> items, String message);
	
	public void reset(EventPropertiesRpcResponse properties) {
		iNotes.setText("");
		iStandardNotes.clear();
		iForm.getRowFormatter().setVisible(iForm.getRow(MESSAGES.propNotes()), properties != null && properties.hasStandardNotes());
		if (properties != null && properties.hasStandardNotes()) {
			for (String note: properties.getStandardNotes())
				iStandardNotes.addItem(note);
		}
		iFileUpload.reset();
	}
	
	public void show(List<EventMeetingRow> meetings, ApproveEventRpcRequest.Operation operation) {
		iTable.setValue(meetings);
		switch (operation) {
		case APPROVE: setText(MESSAGES.dialogApprove());
		case REJECT: setText(MESSAGES.dialogReject());
		case INQUIRE: setText(MESSAGES.dialogInquire());
		}
		iFooter.setEnabled("approve", operation == ApproveEventRpcRequest.Operation.APPROVE);
		iFooter.setEnabled("reject", operation == ApproveEventRpcRequest.Operation.REJECT);
		iFooter.setEnabled("inquire", operation == ApproveEventRpcRequest.Operation.INQUIRE);
		iFileUpload.check();
		center();
		if (iStandardNotes.getItemCount() == 0)
			iNotes.setFocus(true);
		else
			iStandardNotes.setFocus(true);
		RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
	}
	
	public String getNote() {
		return iNotes.getText();
	}
	
	@Override
	public void execute(final EventMeetingTable source, OperationType operation, List<EventMeetingRow> selection) {
		switch (source.getMode()) {
		case ListOfEvents:
			iTable.setMode(EventMeetingTable.Mode.ApprovalOfEvents);
			break;
		case ListOfMeetings:
			iTable.setMode(EventMeetingTable.Mode.ApprovalOfMeetings);
			break;
		case MeetingsOfAnEvent:
			iTable.setMode(EventMeetingTable.Mode.ApprovalOfSingleEventMeetings);
			break;
		}
		iTable.setMeetingFilter(new MeetingFilter() {
			@Override
			public boolean filter(MeetingInterface meeting) {
				return !meeting.isCanApprove() || (source.getMeetingFilter() != null && source.getMeetingFilter().filter(meeting));
			}
		});
		switch (operation) {
		case Approve:
			show(selection, ApproveEventRpcRequest.Operation.APPROVE);
			break;
		case Reject:
			show(selection, ApproveEventRpcRequest.Operation.REJECT);
			break;
		case Inquire:
			show(selection, ApproveEventRpcRequest.Operation.INQUIRE);
			break;
		}
	}
}
