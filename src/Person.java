public class Person {

	private String url;
	private String email;
	private String room;
	private String phone;
	private String firstname;
	private String lastname;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	
	public Person(String email, String url, String room, String phone){
		this.email = email;
		this.url = url;
		this.room =room;
		this.phone = phone;
	}
	
	private void getName(String input) {
		if(input.contains("@")) {
			String[] parts = input.split("@");
			String name = parts[0].replace(".", " ").toLowerCase();
			String[] nameParts = name.split(" ");

			if (nameParts.length == 1) {				
				this.firstname = "";
				this.lastname = nameParts[0];
			}

			else{
				this.firstname = nameParts[0];
				this.lastname = nameParts[1];
			}
		}
		else {
			this.firstname = "";
			this.lastname = "";
		}
		}
	
	public String toSql() {
		getName(getEmail());
		return "INSERT INTO pers_core_data VALUES (NULL, '" + this.firstname + "', '"+ this.lastname + "', '" + getEmail() + "', '" 
															+ getUrl() + "', '"+ getRoom()+ "', '"+ getPhone()+"')";		
	}
}