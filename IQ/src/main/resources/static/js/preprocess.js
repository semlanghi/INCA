function doPreprocess(){
	var part = adr+"others/preprocess.html"
	fetch(part).then(e => e.text()).then(e => {
		//alert(e);
		var content = document.getElementById('content');
		content.innerHTML = e;		
		//figsSimpleStatistics();
		changeBackground("data_annotation");
	}).catch(ef => {});	
 }
 //doPreprocess();

 function load_constraint(){
	alert('load constraint')
 }

 function preprocess(){
	 //alert('preprocess')
	 
	var dbName = document.getElementById("database_name_p").value;
	var dbType = document.getElementById("database_type_p").value;
	var dbHost = document.getElementById("database_host_p").value;
	var dbPort = document.getElementById("database_port_p").value;
	var usrName = document.getElementById("user_name_p").value;
	var pass = document.getElementById("pass_p").value;
	var constraints = document.getElementById("constraints").value;
	
	var res = {};
	res.dbname = dbName;
	res.dbtype = dbType;
	
	res.dbhost = dbHost;
	res.dbport = dbPort;
	
	res.username = usrName;
	res.pass = pass;
	
	
	var constrs = [];
	
	var cstrs = constraints.split('(\s|\t|\n)*;(\s|\t|\n)*');
	
	for(var i=0; i<cstrs.length; i++){
		if (cstrs[i]!=''){
			constrs.push(cstrs[i]);
		}
	}
	res.constraints =  constrs;
	
	var value = JSON.stringify(res);
	
    var param = {"method":"Post", "body":value}
    var url = adr+"preprocess";
    
    wait();
    
    fetch(url, param).then(e=>e.json()).then(e => {
			end();
            if (e.result=='success'){
            	successMessage(e.message);
            }else {
            	failedMessage(e.message);
            }
	}).catch(e => {
			end();
           	failedMessage("Error: the preprocessing is not done !\n"+e);
 	});
	
 }
 
 function  failedMessage(message){
 	var e = document.getElementById("message");
 	e.style = "background: white; display:block;color:red; text-align:center; padding:0.5em; font-size:1.5em;";
 	e.innerHTML = message;
	setTimeout(function(){ e.style="display: none"; }, 10000);
 }
 
 function  successMessage(message){
 	var e = document.getElementById("message");
 	e.style = "background: white; display:block;color:green; text-align:center; padding:0.5em; font-size:1.5em;";
	e.innerHTML = message;
	
	setTimeout(function(){ e.style="display: none"; }, 10000);	
 }
 
 function wait(){
 	var e = document.getElementById("attente");
 	e.style = "background: transparent; display:block; margin-left: 40%;"; 	
 }
 
 function end(){
 	var e = document.getElementById("attente");
 	e.style = "display:none;"; 	
 }
 
 
 