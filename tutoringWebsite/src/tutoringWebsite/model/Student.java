package tutoringWebsite.model;

public class Student extends User { 
	private String year;
	private String major;
	private int student_id;
	
	public void setYear(String year) {
		this.year = year;
	}
	public String getYear() {
		return this.year;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getMajor() {
		return this.major;
	}
	public int getStudent_id() {
		return student_id;
	}
	public void setStudent_id(int student_id) {
		this.student_id = student_id;
	}

}
