package com.simplenote.android.model;

import java.util.Date;

import junit.framework.TestCase;

public class NoteTest extends TestCase {
	public void testNoNewlines() {
		String txt = "This is a test note without any newlines in it";
		Note note = new Note(txt, "1", new Date().toString());
		assertEquals(txt,note.getTitle());
		assertEquals(txt,note.getTitleAndBody());
	}
	public void testNewline() {
		String txt = "This is a test\nnote with\nnewlines in it";
		Note note = new Note(txt, "1", new Date().toString());
		assertFalse(txt.equals(note.getTitle()));
		assertEquals("This is a test",note.getTitle());
		assertEquals(txt,note.getTitleAndBody());
	}
	public void testMultipleNewlines() {
		String txt = "This is a test\n\n\nnote with\nnewlines in it";
		Note note = new Note(txt, "1", new Date().toString());
		assertFalse(txt.equals(note.getTitle()));
		assertEquals("This is a test",note.getTitle());
		assertEquals(txt,note.getTitleAndBody());
	}
}