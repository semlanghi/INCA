package com.limos.fr.mod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.limos.fr.exceptions.DataLoadingError;
import com.limos.fr.queries.PreprocessService;

@RestController
public class Preprocess {

	@Autowired
	private PreprocessService service;
	
	@RequestMapping(value = "/preprocess", method = RequestMethod.POST)
	public String exploration_by_constraints(@RequestBody String param) {
		try { 
			System.out.println(param);
			String res = service.doPreprocess(param);
			System.out.println(res);
			return res;
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}
}
