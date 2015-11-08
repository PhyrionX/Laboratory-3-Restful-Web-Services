package rest.addressbook;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A person entry in an address book
 *
 */
public class Person {

	private String name;
	private int id;
	private String email;
	private URI href;
	private List<PhoneNumber> phoneList = new ArrayList<PhoneNumber>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<PhoneNumber> getPhoneList() {
		return phoneList;
	}

	public void setPhoneList(List<PhoneNumber> phones) {
		this.phoneList = phones;
	}

	public void addPhone(PhoneNumber phone) {
		getPhoneList().add(phone);
	}

	public boolean hasEmail() {
		return getEmail() != null;
	}

	public void setHref(URI href) {
		this.href = href;
	}
	
	public URI getHref() {
		return href;
	}
}
