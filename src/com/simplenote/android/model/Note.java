package com.simplenote.android.model;

public class Note {
	private final String title;
	private final String body;
	private final String key;
	private final String dateModified;
	private int newlines = 0;

	public Note(String title, String body, String key, String dateModified) {
		this.title = title;
		this.body = body;
		this.key = key;
		this.dateModified = dateModified;
		this.newlines = 2;
	}

	public Note(String titleAndBody, String key, String dateModified) {
		int idxNewline = titleAndBody.indexOf("\n");
		if (idxNewline != -1) {
			this.title = titleAndBody.substring(0,idxNewline);
			String tmp = titleAndBody.substring(idxNewline);
			idxNewline = tmp.indexOf("\n");
			while (idxNewline == 0) {
				tmp = tmp.substring(1);
				idxNewline = tmp.indexOf("\n");
				newlines++;
			}
			this.body = tmp;
		} else {
			this.title = titleAndBody;
			this.body = "";
		}
		this.key = key;
		this.dateModified = dateModified;
	}

	public final String getTitle() {
		return title;
	}

	public final String getBody() {
		return body;
	}

	public final String getKey() {
		return key;
	}

	public final String getDateModified() {
		return dateModified;
	}

	public final String getTitleAndBody() {
		StringBuilder builder = new StringBuilder(title);
		for (int i = 0; i < newlines; i++) {
			builder.append("\n");
		}
		builder.append(body);
		return builder.toString();
	}
}
