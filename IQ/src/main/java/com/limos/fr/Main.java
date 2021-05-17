package com.limos.fr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.limos.fr.queries.topk.QueryEval;

@SpringBootApplication
public class Main {

	public static void main(String[] args) throws Exception{
		SpringApplication.run(Main.class, args);
	}   
	 
	
	
	static void test() throws Exception{
		String query = "SELECT a1.A, a2.B FROM R1 a1, R2 a2 WHERE a1.B = a2.B +=20";
		QueryEval ev = QueryEval.getInstance();
		ev.decomposeQuery(query);
		
		System.out.println("K= "+ev.getK());
		System.out.println("Query= "+ev.getQuery());
		System.out.println("Most= "+ev.isMost());
		System.out.println("alias= "+ev.getAlias());
		
		Long [] a = {12l, 11l};
		System.out.println("pattern= "+ev.doPatern(a));
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