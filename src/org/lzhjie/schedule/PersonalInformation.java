package org.lzhjie.schedule;

class PersonalInformation {
	private String name = null;
	private String tel = null;
	PersonalInformation(String name, String tel) {
		this.name = name.trim();
		this.tel = tel;
	}
	public String name() {
		return name;
	}
	public String tel() {
		return tel;
	}
}
