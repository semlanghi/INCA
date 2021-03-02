package com.limos.fr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);

//		System.out.println("Sayez ...............................");
		
//		JSONObject o = new JSONObject();
//		List<String> l = new ArrayList<String>();
//		
//		l.add("hello");
//		o.put("a", "bbaab");
//		o.put("b", "b_àbaab");
//		JSONArray ll = new JSONArray(l);
//		ll.put("ab");
//		       
//		JSONObject oo = new JSONObject();
//		oo.put("a", "ccncncn");
//		ll.put(oo); 
//		o.put("c", ll);          
//		      
//		System.out.println(o);
		    
		      
//		System.out.println(Math.round(1.45));
		
//		int first = 10;
//		int total = 20;
//		System.out.println(reduceFraction(first, total));
	}   
	
	
	static String reduceFraction(int first, int total) {
		if ((total%first)==0)
			return "(1/"+(total/first)+")";
		int f = first, t = total;
		for(int i=2; i<=f; i++) {
			while((f%i==0)&&(t%i==0)) {
				f = f/i;
				t = t/i;
			}
		}
		return "("+f+"/"+t+")";
	}

} 