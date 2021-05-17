var shown = true;
var shown2 = true;

function show_database_param(){	
	var e = document.getElementById("param_database");
	if (shown){
		e.style="display: block;";
		shown = false;
	}else {
		e.style="display: none;";
		shown = true;
	}	
	changeBackground("input_database")
}

function load_database(){

var pass = document.getElementById("pass").value;
var userName = document.getElementById("user_name").value;
var port = document.getElementById("database_port").value;
var host = document.getElementById("database_host").value;
var type = document.getElementById("database_type").value;
var databaseName = document.getElementById("database_name").value;

var data = "{'passWord':'"+pass+"', 'userName':'"+userName+"', 'port':'"+port+"', 'host': '"+host+"', 'type': '"+type+"', 'databaseName': '"+databaseName+"'}";

var param = {'method':'Post', 'body':data}
//'headers': {'Accept':'application/json'},
var url = adr+'load/database';

var style = "display: block; padding:0.25em; text-align: center; font-size:1em;"
var boite = document.getElementById("boite_message"); 

fetch(url, param).then(resp => {

	if (resp.ok){
	
		style = style+"color: #73AD21;";
		boite.style= style;
		boite.innerHTML = "Database is loaded";
		setTimeout(function(){boite.style="display: none;"}, 3000);
	
	} else {
	
		style = style+"color: red;"
		boite.style= style;
		boite.innerHTML = "Error arisen in database information";
		setTimeout(function(){boite.style="display: none;"}, 3000);
	
	}
}
).catch(error => {
 	
 	style = style+"background: red;"
	boite.innerHTML = "Error arisen in database information";
	boite.style= style;
	setTimeout(function(){boite.style="display: none;"}, 3000);	

 });	
	

	
}


function show_profiling(){
	var e = document.getElementById("profiling_param");
	if (shown2){
		e.style="display: block;";
		shown2 = false;
	} else {
		e.style="display: none;";
		shown2 = true;
	}
	 changeBackground("data_i_profiling");
}

var part = adr+"others/menu.html"
fetch(part).then(e => e.text()).then(e => {
	//alert(e);
	var content = document.getElementById('menu');
	content.innerHTML = e;
}).catch(ef => {});	


function Data_Exploration(){
	var part = adr+"others/dataexploration.html"
	fetch(part).then(e => e.text()).then(e => {
		changeBackground("data_exploration")
		var content = document.getElementById('content');
		content.innerHTML = e;	
		start_data_exploration();	
	}).catch(ef => {});	
	
}

function correlated_violations(){
	var part = adr+"others/profiling2.html"
	fetch(part).then(e => e.text()).then(e => {
		//alert(e);
		changeBackground("Data_exp_constraint")
		var content = document.getElementById('content');
		content.innerHTML = e;		
		loading_by_constraints();
		loadConstraints_forCS();
	}).catch(ef => {});	
}

function loadConstraints_forCS(){
	fetch(adr+"getConstraints").then(e => e.json()).then(e => {
		constraints = e;
		//var x = document.getElementById("Constraint_Selected");
		var x1 = document.getElementById("Constraint_Selected_");
		
		//var v = ''
		var v1 = ''
		for (var j = 0; j < e.length; j++) {
			var val = e[j];
			//v+='<option value="'+val.id+'">'+val.id+'</option>'
			v1+='<option value="'+val.id+'"> <span class="size__1 commun_text">'+val.id+'</span></option>'
		}
		//x.innerHTML = v;
		x1.innerHTML = v1;

		//selectedSource();
		selectedSource_();
		
	}).catch(e => alert(e));
}



function loading_by_constraints(){
	var attributs = document.getElementById("attributs");
	var attribut_to_show = document.getElementById("attribut_to_show")
	var attribut_to_group = document.getElementById("attribut_to_group")
	//var attribut_to_count = document.getElementById("attribut_to_count")
	
	attributs.addEventListener('dragover', e => {dragover(e); return false;}, false);
	attributs.addEventListener('dragenter', e => false, false);
	attributs.addEventListener('dragleave', e => false, false);
	attributs.addEventListener('drop', e =>  {drop(e, attributs);return false;}, false);

	attribut_to_show.addEventListener('dragover', e => {dragover(e); return false;}, false);
	attribut_to_show.addEventListener('dragenter', e => false, false);
	attribut_to_show.addEventListener('dragleave', e => false, false);
	attribut_to_show.addEventListener('drop', e =>  {drop(e, attribut_to_show);return false;}, false);

	attribut_to_group.addEventListener('dragover', e => {dragover(e); return false;}, false);
	attribut_to_group.addEventListener('dragenter', e => false, false);
	attribut_to_group.addEventListener('dragleave', e => false, false);
	attribut_to_group.addEventListener('drop', e =>  {drop(e, attribut_to_group);return false;}, false);

	//attribut_to_count.addEventListener('dragover', e => {dragover(e); return false;}, false);
	//attribut_to_count.addEventListener('dragenter', e => false, false);
	//attribut_to_count.addEventListener('dragleave', e => false, false);
	//attribut_to_count.addEventListener('drop', e =>  {drop(e, attribut_to_count);return false;}, false);

}

/*
input_database :: show_database_param()
data_annotation :: doPreprocess()
data_i_profiling :: show_profiling()
simple_statistics_data :: simpleStatistics()
Data_exp_constraint :: correlated_violations()
data_exploration :: data_Exploration()
query_exploration :: QueryProcess()*/

function changeBackground(id_){

	var A = document.getElementById("input_database");
	var B = document.getElementById("data_annotation");
	var C = document.getElementById("data_i_profiling");
	var D = document.getElementById("simple_statistics_data_");
	var E = document.getElementById("Data_exp_constraint");
	var F = document.getElementById("data_exploration");
	var G = document.getElementById("query_exploration");

	A.style = "background: #aaa;";
	B.style = "background: #aaa;";
	C.style = "background: #aaa;";
	D.style = "background: #aaa;";
	E.style = "background: #aaa;";
	F.style = "background: #aaa;";
	G.style = "background: #aaa;";

	var H = document.getElementById(id_);
	H.style = "background: green;";

}