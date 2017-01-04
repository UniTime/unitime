package org.unitime.timetable.util;

import java.util.Date;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class ProgressTracker {
	
	
	private String label;
	private int total;
	private int count;
	private int percentInterval = 5;
	private int lastPrintedPercent;
	private Date timeOfLastPrint;
	private Class<?> cls;
	private long previousElapsedTime;
	private TreeMap<Integer, Long> elapseTimeLog = new TreeMap<Integer, Long>();

	public ProgressTracker(String label, int total, Class<?> cls) {
		super();
		this.label = label;
		this.total = total;
		this.cls = cls;
		this.count = 0;
		this.timeOfLastPrint = new Date();
	}
	
	public ProgressTracker(String label, int total, int percentInterval, Class<?> cls) {
		super();
		this.label = label;
		this.total = total;
		this.cls = cls;
		this.percentInterval = percentInterval;
		this.count = 0;
		this.timeOfLastPrint = new Date();
	}

	private String elapsedTimeString(long millisecondsElapsed){
		long ms = millisecondsElapsed % 1000;
		long totalSeconds = millisecondsElapsed / 1000;
		long seconds = totalSeconds % 60;
		long totalMinutes = totalSeconds / 60;
		long minutes = totalMinutes % 60;
		long hours = totalMinutes / 60;
		StringBuilder sb = new StringBuilder();
		String comma = "";
		if (millisecondsElapsed == 0) {
			sb.append("0 Milliseconds");
		}
		if(hours > 0){
			sb.append(hours)
			  .append(" Hours")
			  ;
			comma = ", ";
		}
		if (minutes > 0) {
			sb.append(comma)
			  .append(minutes)
			  .append(" Minutes");
			comma = ", ";			
		}
		if (seconds > 0 ) {
			sb.append(comma)
			  .append(seconds)
			  .append(" Seconds");
			comma = ", ";						
		}
		if (ms > 0 ) {
			sb.append(comma)
			  .append(ms)
			  .append(" Milliseconds");
		}
        return(sb.toString());
	}
	
	private String calculateElapsedTimeSinceLastProgressPrint(Date timeNow){
		long millisecondsElapsed = timeNow.getTime() - timeOfLastPrint.getTime();
		StringBuilder sb = new StringBuilder();
		sb.append("Time Since Last Message:  ");
		sb.append(elapsedTimeString(millisecondsElapsed));
		sb.append(";  Time Between Reports has " );
		if (previousElapsedTime < millisecondsElapsed) {
			sb.append("increased.");
		} else if (previousElapsedTime == millisecondsElapsed){
			sb.append("not changed.");
		} else {
			sb.append("decreased.");
		}
		return(sb.toString());
	}

	public void logProgressIfNeeded(){
		String progressString = getProgressStringIfNeeded();
		if (progressString != null) {
			Logger.getLogger(cls).info(progressString);	
		}
	}
	
	public String getProgressStringIfNeeded(){
		String progressString = null;
		count++;
		int printPct = count * 100 / this.total;
		Date timeNow = new Date();
		if ((printPct % percentInterval == 0 && this.lastPrintedPercent < printPct) || count == this.total)
		{
			progressString = (timeNow.toString() + "  Processing " + this.label + " - progress:  " + printPct + "% (" +  count + " of " + this.total +"), " + calculateElapsedTimeSinceLastProgressPrint(timeNow));	
			this.lastPrintedPercent = printPct;		
			this.previousElapsedTime = timeNow.getTime() - timeOfLastPrint.getTime();
			this.timeOfLastPrint = timeNow;
			this.elapseTimeLog.put(new Integer(printPct), new Long(previousElapsedTime));
		}
	
		return(progressString);
	}
	
	public String totalTimeToProcess() {
		StringBuilder sb = new StringBuilder();
		long totalTime = 0;
		for(Integer pct : this.elapseTimeLog.keySet()){
			totalTime += this.elapseTimeLog.get(pct).longValue();
		}
		sb.append("Total Time to Process ")
		  .append(this.label)
		  .append(":  ")
		  .append(elapsedTimeString(totalTime))
		  .append("\n");
		return(sb.toString());
	}
	
	public String totalTimeToProcessInMilliseconds() {
		StringBuilder sb = new StringBuilder();
		long totalTime = 0;
		for(Integer pct : this.elapseTimeLog.keySet()){
			totalTime += this.elapseTimeLog.get(pct).longValue();
		}
		sb.append("Total Time to Process in Milliseconds ")
		  .append(this.label)
		  .append(":  ")
		  .append(Long.toString(totalTime))
		  .append("\n");
		return(sb.toString());
	}
	
	public String getElapsedTimeAnalysisString(){

		if (count == 0){
			return(null);
		}
		StringBuilder sb = new StringBuilder();
		long lastElapsed = 0;
		boolean prevIncreased = true;
		boolean prevDecreased = false;
		boolean prevSame = false;
		long totalIncreases = 0;
		long totalDecreases = 0;
		

		sb.append("\n\n\n")
		  .append(totalTimeToProcess())
		  .append("\n")
		  .append("Summary of Time Between Status Reports\n")
		  .append("\t")
		  .append("0% - ");
		long cnt = 0;
		long sumOfElapsedDifferences = 0;
		for(Integer pct : this.elapseTimeLog.keySet()){
			if (lastElapsed < this.elapseTimeLog.get(pct).longValue()){
				if (prevDecreased){
					sb.append(pct)
					  .append("% elapsed time decreased.  Average change = ")
					  .append(elapsedTimeString(sumOfElapsedDifferences / cnt))
					  .append(".\n")
					  .append("\t")
					  .append(pct)
					  .append("% - ");
					prevIncreased = true;
					prevDecreased = false;
					cnt = 0;
					sumOfElapsedDifferences = this.elapseTimeLog.get(pct).longValue() - lastElapsed;
				} else if (prevSame){
					sb.append(pct)
					  .append("% elapsed time stayed the same.\n")
					  .append("\t")
					  .append(pct)
					  .append("% - ");
					prevIncreased = true;
					prevSame = false;
					cnt = 0;
					sumOfElapsedDifferences = this.elapseTimeLog.get(pct).longValue() - lastElapsed;
				}
				else {
					sumOfElapsedDifferences += (this.elapseTimeLog.get(pct).longValue() - lastElapsed);
				}
				totalIncreases += (this.elapseTimeLog.get(pct).longValue() - lastElapsed);
			} else if (lastElapsed > this.elapseTimeLog.get(pct).longValue()){
				if (prevIncreased){
					sb.append(pct)
					  .append("% elapsed time increased.  Average change = ")
					  .append(elapsedTimeString(sumOfElapsedDifferences / cnt))
					  .append(".\n")
					  .append("\t")
					  .append(pct)
					  .append("% - ");
					prevIncreased = false;
					prevDecreased = true;
					cnt = 0;
					sumOfElapsedDifferences = lastElapsed - this.elapseTimeLog.get(pct).longValue();		
				} else if (prevSame){
					sb.append(pct)
					  .append("% elapsed time stayed the same.\n")
					  .append("\t")
					  .append(pct)
					  .append("% - ");
					prevDecreased = true;
					prevSame = false;
					cnt = 0;
					sumOfElapsedDifferences = lastElapsed - this.elapseTimeLog.get(pct).longValue();		
				} else {
					sumOfElapsedDifferences += (lastElapsed - this.elapseTimeLog.get(pct).longValue());		
				}
				totalDecreases += (lastElapsed - this.elapseTimeLog.get(pct).longValue());
			} else {
				if (prevIncreased){
					sb.append(pct)
					  .append("% elapsed time increased.  Average change = ")
					  .append(elapsedTimeString(sumOfElapsedDifferences / cnt))
					  .append(".\n")
					  .append("\t")
					  .append(pct)
					  .append("% - ");
					prevIncreased = false;
					prevSame = true;
					cnt = 0;
					sumOfElapsedDifferences = 0;					
				} else if (prevDecreased) {
					sb.append(pct)
					  .append("% elapsed time decreased.  Average change = ")
					  .append(elapsedTimeString(sumOfElapsedDifferences / cnt))
					  .append(".\n")
					  .append("\t")
					  .append(pct)
					  .append("% - ");
					prevSame = true;
					prevDecreased = false;
					cnt = 0;
					sumOfElapsedDifferences = 0;
				} else {
					sumOfElapsedDifferences = 0;					
				}
			}
			cnt++;
			lastElapsed = this.elapseTimeLog.get(pct).longValue();
		}
		if (prevIncreased){
			sb.append(100)
			  .append("% elapsed time increased.  Average change = ")
			  .append(elapsedTimeString(sumOfElapsedDifferences / cnt))
			  .append(".\n");
			
		} else if (prevDecreased){
			sb.append(100)
			  .append("% elapsed time decreased.  Average change = ")
			  .append(elapsedTimeString(sumOfElapsedDifferences / cnt))
			  .append(".\n");
			
		} else {
			sb.append(100)
			  .append("% elapsed time stayed the same.\n");
		}
		sb.append("Total increased time:")
		  .append(elapsedTimeString(totalIncreases))
		  .append("\n")
		  .append("Total decreased time:")
		  .append(elapsedTimeString(totalDecreases))
		  .append(".\n\n");	
		
		return(sb.toString());
	
	}

	public void logElapsedTimeAnalysis(){

		String analysis = getElapsedTimeAnalysisString();
		if (analysis != null) {
			Logger.getLogger(cls).info(analysis);	
		}
	}

}
