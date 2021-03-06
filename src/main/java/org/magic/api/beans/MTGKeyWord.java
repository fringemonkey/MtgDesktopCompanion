package org.magic.api.beans;

import java.io.Serializable;

public class MTGKeyWord implements Serializable{

	public enum TYPE {
		ABILITIES, ACTION, WORD
	}

	public enum SUBTYPE {
		EVERGREEN, OTHER
	}

	public enum EVENT {
		SPELL, STATIC, ACTIVATED, TRIGGERED, ACTION, ABILITY
	}

	private String keyword;
	private String description;

	private TYPE type;
	private SUBTYPE subtype;
	private EVENT event;

	private MTGKeyWord() {

	}

	@Override
	public String toString() {
		return getKeyword();
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public SUBTYPE getSubtype() {
		return subtype;
	}

	public void setSubtype(SUBTYPE subtype) {
		this.subtype = subtype;
	}

	public EVENT getEvent() {
		return event;
	}

	public void setEvent(EVENT action) {
		this.event = action;
	}

}
