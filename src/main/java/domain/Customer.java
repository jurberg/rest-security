package domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "id",
    "name"
})
@XmlRootElement(name = "Customer")
public class Customer {

    @XmlElement(required = true)
	private String id;
	
	@XmlElement(required = true)
	private String name;
	
	public Customer(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Customer() {
		super();
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
