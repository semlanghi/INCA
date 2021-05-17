
var from = ""; 
var where = "";


function dragstart(e){
	e.dataTransfer.effectAllowed = 'copy';
	e.dataTransfer.setData('text', e.target.id);
	e.dataTransfer.setData('border', e.target.style.border);
	return false;
}

function dragover(e){
	if (e.preventDefault) e.preventDefault();
	e.dataTransfer.dropEffect = 'copy'; 
} 

function drop(e, ca){
	if (e.stopPropagation) e.stopPropagation();
	var id = e.dataTransfer.getData('text');
	var border = e.dataTransfer.getData('border');
	var theitem = document.getElementById(id);
	theitem.parentNode.removeChild(theitem);  
	theitem.className='itemblurred';
	var y  = document.createElement('div');
	y.innerHTML = theitem.innerHTML;
	y.className="produit";
	y.draggable="true";
	y.addEventListener('dragstart', dragstart)
	y.id = id;
	y.style="font-size:1.5em;font-weight: bold;display: inline-block; color:black; padding:0.5em; margin-top:0.5em;margin-left:0.5em;background: #ddd; border-radius: 5px; border: "+border+";";
	//y.class="size__1"
	ca.appendChild(y);
	e.preventDefault(); 
}

function nextColor(tab){
	rr = (tab[0]*2)%255;
	gg = (tab[1]*3)%255;
	bb = (tab[2]*5)%255;
	tt = []
	tt.push(rr);tt.push(gg);tt.push(bb)
	return tt;
}

function selectedSource_(){	

	selectedSource();

	var x = document.getElementById("Constraint_Selected_");
	var val = x.options[x.selectedIndex].value;

	for(var po=0; po<constraints.length; po++){
		var ee = constraints[po]
		if (ee.id==val){
			from = ee.f;
			where = ee.w;
			break;
		}
	}
	from = from.split(/[\s]*,[\s]*/)
	where = where.replaceAll(' ', '').split(',')
	var attrs = {};//new Set([]); 

	for(var i=0; i<where.length; i++){
		var b = where[i].includes("<>") || where[i].includes(">=") || where[i].includes("<=") || (where[i].includes(">") && !where[i].includes("=")) || (where[i].includes("<") && !where[i].includes("="))
		var c = (where[i].includes("=") && !where[i].includes("<") && !where[i].includes(">")) 
		if (b){
			w = where[i].split(/<>|=|<=|>=|<|>/)
			//attrs.add(w[0]);
			//attrs.add(w[1]);
			attrs[w[0]] = 'blue'
			attrs[w[1]] = 'blue' 
		}else{
			if (c){
				w = where[i].split(/=/)
				attrs[w[0]] = 'green'
				attrs[w[1]] = 'green' 
			}
		}
	}

	var attributs = document.getElementById("attributs")
	var attribut_to_show = document.getElementById("attribut_to_show")
	var attribut_to_group = document.getElementById("attribut_to_group")
	//var attribut_to_count = document.getElementById("attribut_to_count")

	attributs.innerHTML = ''
	attribut_to_show.innerHTML = ''
	attribut_to_group.innerHTML = ''
	//attribut_to_count.innerHTML = ''

	attributsToJson = JSON.parse(JSON.stringify(attrs))

	for(var e in attributsToJson){
		if (e!=""){
			var y  = document.createElement('div');
			y.innerHTML = e;
			y.draggable="true";
			y.addEventListener('dragstart', dragstart)
			y.id = e;
			y.style="font-size:1.5em;font-weight: bold;display: inline-block; color:black; padding:0.5em; margin-top:0.5em;margin-left:0.5em;background: #ddd; border-radius: 5px; border: 3px solid "+attributsToJson[e]+";";
			y.class="size__1"
			attributs.appendChild(y);
		}
	}
	clear_constraints_exploration_lite();
}

function show_details(){
	var x = document.getElementById("Constraint_Selected_");
	var val = x.options[x.selectedIndex].value;
	if (val){
		//alert(val)
		show_description_query(val);
	}
}

function clear_constraints_exploration_lite(){
	var container = document.getElementById('constraints_exploration_container');
	container.innerHTML = '';
}


function clear_constraints_exploration(){
	var container = document.getElementById('constraints_exploration_container');
	//alert(container);
	container.innerHTML = '<img src="img/attente3.gif"/>';
}

function set_constraints_exploration(){
	var container = document.getElementById('constraints_exploration_container');
	container.innerHTML = '<canvas id="constraints_exploration"></canvas>';
}


function exploration_by_constraint(){

	var attribut_to_show = document.getElementById("attribut_to_show")
	var attribut_to_group = document.getElementById("attribut_to_group")
	//var attribut_to_count = document.getElementById("attribut_to_count")

    var children_attribut_to_show = attribut_to_show.childNodes;
    var children_attribut_to_group = attribut_to_group.childNodes;
    //var children_attribut_to_count = attribut_to_count.childNodes;

	// && attribut_to_count.hasChildNodes())
	if (attribut_to_show.hasChildNodes() && attribut_to_group.hasChildNodes()){	
		clear_constraints_exploration();
		var limit = document.getElementById("limit").value;
		group = []; select = []; count = [];
		attribut_to_show.childNodes.forEach(e => {
			if (e)
				select.push('"'+e.innerText+'"')
		})
		attribut_to_group.childNodes.forEach(e => {
			if (e)
				group.push('"'+e.innerText+'"')
		})
		//attribut_to_count.childNodes.forEach(e => {
		//	if (e)
		//		count.push('"'+e.innerText+'"')
		//})

		var data = "{'from':'"+from+"', 'where':'  "+where+" ', 'limit':' "+limit+" ', 'select':["+select+"], 'group':["+group+"]}";// , 'count':["+count+"]}";
		//var param = {'method':'Post', 'body': content};
		var param = {'method':'Post', 'body':data};
		var url = adr+"exploration/by/constraints"

		fetch(url,param).then(e => e.json()).then(e => {			
			//alert('ici 2')
			set_constraints_exploration();
			var canvas = document.getElementById('constraints_exploration');
			var ctx = canvas.getContext('2d');
			var chart = new Chart(ctx, {
				type: 'bar',
				data: e,
				options: {legend: {display: false,labels: {display: false, fontColor: "black", fontStyle: "bold", fontSize: size__}},
					scales: {
						xAxes: [{stacked: true, ticks: {display: false,fontColor: "black", fontStyle: "bold", fontSize: size__}}],
						yAxes: [{stacked: true, ticks: {display: false,beginAtZero: true,fontColor: "black", fontStyle: "bold", fontSize: size__},type: 'logarithmic'}]
					}
				}
			});

		}).catch(e => {alert(e);clear_constraints_exploration_lite();});
	}
}