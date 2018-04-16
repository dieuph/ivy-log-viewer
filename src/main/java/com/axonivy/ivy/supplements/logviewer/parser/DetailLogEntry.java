package com.axonivy.ivy.supplements.logviewer.parser;

public class DetailLogEntry {

	private MainLogEntry parentEntry;
	private String detailText;
	
	public DetailLogEntry(MainLogEntry parentEntry, String detailText) {
		this.parentEntry = parentEntry;
		this.detailText = detailText;
	}

	public MainLogEntry getParentEntry() {
		return parentEntry;
	}

	public String getDetailText() {
		return detailText;
	}
	
	@Override
	public String toString() {
		return parentEntry.getOriginalTitleLine().concat("\n").concat(getDetailText());
	}
}