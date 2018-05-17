package org.unitime.timetable.util.queue;

import java.util.List;

import javax.activation.DataSource;

public interface QueueProcessor {
	
	public QueueItem add(QueueItem item);
	
	public QueueItem get(String id);
	
	public QueueItem getByExecutionId(Long id);
	
	public boolean remove(String id);

	public List<QueueItem> getItems(String ownerId, Long sessionId, String type);
	
	public DataSource getFile(String id);
}
