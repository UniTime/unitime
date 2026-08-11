package org.unitime.timetable.util.queue;

import java.util.List;

import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;

import jakarta.activation.DataSource;

public interface QueueProcessor {
	
	public QueueItem add(QueueItem item);
	
	public QueueItem get(String id);
	
	public QueueItem getByExecutionId(Long id);
	
	public boolean remove(String id);

	public List<QueueItem> getItems(String ownerId, Long sessionId, String type);
	
	public List<QueueItemInterface> getItemsTable(String ownerId, Long sessionId, String type, Integer timeToShow);
	
	public DataSource getFile(String id);
}
