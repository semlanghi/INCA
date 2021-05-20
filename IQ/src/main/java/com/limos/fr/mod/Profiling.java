package com.limos.fr.mod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.limos.fr.exceptions.DataLoadingError;
import com.limos.fr.queries.ProfilingService;
import com.limos.fr.queries.QueryService;

@RestController
public class Profiling {

	@Autowired
	private ProfilingService service;
	@Autowired
	private QueryService queryService;
	
	@RequestMapping(value = "/percentVio", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String getPercentVioAndNoVio() {
		try {
			String res = service.getVioAndNoVio();
			System.out.println("vio and no vio: "+res);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataLoadingError();
		}
	}

	@RequestMapping(value = "/distributionviolations", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String getdistributionViolation() {
		try {
			String res = service.distributionViolation();
			System.out.println(res);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataLoadingError();
		}
	}

	@RequestMapping(value = "/distributionviolationssubset", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String distributionviolationssubset() {
		try {
			String res = service.distributionviolationssubset();
			System.out.println(res);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataLoadingError();
		}
	}            
         
	@RequestMapping(value = "/distributionviolationsbyconstraint", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String distributionviolationsbyconstraint() {
		try {
			String res = service.distributionviolationsbyconstraint();
			return res; 
		} catch (Exception e) {
			throw new DataLoadingError();
		}
	}
	
	@RequestMapping(value = "/getConstraints", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String getConstraints() {
		try {
			String res = service.getConstraints();
			System.out.println(res);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataLoadingError();
		}		
	}

	//----------------------------------------------------------------------------------------------------------------------------------
	
	@RequestMapping(value = "/consider/percentVio", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String getPercentVioAndNoVio(@RequestParam(name = "considered") long considered) {
		try {
			String res = service.getVioAndNoVio(considered);
//			System.out.println(res);
			return res;
		} catch (Exception e) {
			System.out.println("error");
			throw new DataLoadingError();
		}
	}
	
	@RequestMapping(value = "/consider/distributionviolations", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String getdistributionViolation(@RequestParam(name = "considered") long considered) {
		try {
			return service.distributionViolation(considered);
		} catch (Exception e) {
			throw new DataLoadingError();
		}
	}
	
	@RequestMapping(value = "/consider/distributionviolationssubset", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String distributionviolationssubset(@RequestParam(name = "considered") long considered) {
		try {
			return service.distributionviolationssubset(considered);
		} catch (Exception e) {
			throw new DataLoadingError();
		} 
	}

	@RequestMapping(value = "/consider/distributionviolationsbyconstraint", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE})
	public String distributionviolationsbyconstraint(@RequestParam(name = "considered") long considered) {
		try {
			String res = service.distributionviolationsbyconstraint(considered);
//			System.out.println(res);
			return res;
		} catch (Exception e) {
			throw new DataLoadingError();
		}
	}
	
	
	@RequestMapping(value = "/exploration/by/constraints", method = RequestMethod.POST)
	public String exploration_by_constraints(@RequestBody String param) {
		try { 
			 
//			System.out.println(param);
			String res = service.explorationByConstraints(param);
//			System.out.println("---------------------------------------------- \n\n" + res +" \n\n -------------------------");
			
			return res;
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}
	 
	@RequestMapping(value = "/constraints/correlation", method = RequestMethod.POST)
	public String constraints_correlation(@RequestBody String param) {
		try {
//			System.out.println(param);
			String res = service.getConstraintsCorrelation(param);
//			System.out.println(res);
			return res;
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}     
	       
	@RequestMapping(value = "/constraint/details/", method = RequestMethod.POST)
	public String constraint_details(@RequestBody String param) {
		try {
//			System.out.println(param);
			String res = service.getConstraintDetail(param);
//			System.out.println(res);
			
			return res;
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}
	 
	
	@RequestMapping(value = "/exploration/tuple/proportion/", method = RequestMethod.POST)
	public String tuple_proportion(@RequestBody String param) {
		try {
//			System.out.println(param);
			String res = service.gettupleProportion(param);
//			System.out.println(res);
			
			return res;
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}	  
	 
	@RequestMapping(value = "/exploration/tuple/violations/", method = RequestMethod.POST)
	public String tuple_violations(@RequestBody String param) {
		try {
//			System.out.println(param);
			String res = service.gettupleViolations(param);
//			System.out.println(res);
			return res; 
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}	    
	
	
	
	@RequestMapping(value = "/query/execution/", method = RequestMethod.POST)
	public String query_execution(@RequestBody String param) {
		try {
//			String res = service.getQueryExecution(param);
			String res = queryService.runQuery(param);
			
//			System.out.println(res);
			
			return res; 
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}
	
	
	

	@RequestMapping(value = "/profiling/bytable/", method = RequestMethod.POST)
	public String profiling_by_table(@RequestBody String param) {
		try {
			String res = service.byTable(param);
			
//			System.out.println(res);
			
			return res;  
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}	 

	
	@RequestMapping(value = "/profiling/attributs/", method = RequestMethod.POST)
	public String profiling_attributs(@RequestBody String param) {
		try {
			String res = service.profilingAttributs(param);
			 
//			System.out.println(res);
			
			return res;  
		}catch(Exception e) {e.printStackTrace(); throw new DataLoadingError();}
	}	 
	  
	
	
}
