package service;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import domain.Customer;
import domain.CustomerList;

@Controller
public class CustomerController {

	@RequestMapping(value="/customers", method=RequestMethod.GET)
	public ModelAndView customers(Model model) {
		CustomerList list = new CustomerList();
		list.getCustomer().add(new Customer("1", "John Doe"));
		list.getCustomer().add(new Customer("2", "Jane Doe"));
		return new ModelAndView("customerList", "object", list);
	}

}
