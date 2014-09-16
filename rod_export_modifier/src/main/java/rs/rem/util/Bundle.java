package rs.rem.util;

public enum Bundle {
	LABEL_SHORTCUTS("labelShortcuts");
	
	private String name;
	
	private Bundle(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
