var constraint_for_query;

function QueryProcess(){
    var part = adr+"others/queryProcess.html"
    fetch(part).then(e => e.text()).then(e => {
        changeBackground("query_exploration")
        var content = document.getElementById('content');
        content.innerHTML = e;
        loadConstraints_for_query();
    }).catch(ef => {});	
}

function loadConstraints_for_query(){
    fetch(adr+"getConstraints").then(e => e.json()).then(e => {
		constraint_for_query = e;
		var constraints_to_check = document.getElementById("constraints_to_check");
		var res = '';
		for (var j = 0; j < e.length; j++) {
            var val = e[j];
            res += '<div style="display: inline; margin-right: 10px;"><input type="checkbox" id="'+val.id+'" value="'+val.id+'" checked></input><label for="'+val.id+'" class="commun_text" >'+val.id+'</label></div>';
		}
		constraints_to_check.innerHTML = res;
	}).catch(e => alert(e));
}

function simple_statistics_by_constraint_load(){
    var y = document.getElementById("simple_statistics_by_constraint_");
    y.innerHTML = '<canvas id="simple_statistics_by_constraint"></canvas>'
}

function simple_statistics_by_constraint_remove(){
    var y = document.getElementById("simple_statistics_by_constraint_");
    y.innerHTML = ''
}

function simple_statistics_subset_constraints_load(){
    var y = document.getElementById("simple_statistics_subset_constraints_");
    y.innerHTML = '<canvas id="simple_statistics_subset_constraints"></canvas>'
}

function simple_statistics_subset_constraints_remove(){
    var y = document.getElementById("simple_statistics_subset_constraints_");
    y.innerHTML = ''
}

function display_subsets(obj){
    simple_statistics_subset_constraints_remove();
    simple_statistics_subset_constraints_load();    
    var ctx3 = document.getElementById('simple_statistics_subset_constraints').getContext('2d');
    var chart3 = new Chart(ctx3, obj)
}

function display_by_constraint(obj){   
    //alert('hahah')
    simple_statistics_by_constraint_remove();
    simple_statistics_by_constraint_load();    
    var ctx3 = document.getElementById('simple_statistics_by_constraint').getContext('2d');
    var chart3 = new Chart(ctx3, obj);
}

function getSelectedConstraints_(){
    var pos = 0;
    if (constraint_for_query){
        for (var j = 0; j < constraint_for_query.length; j++) {
            var val = constraint_for_query[j];
            id = val.id
            position = val.position
            var cst = document.getElementById(id);
            if (cst.checked){
                //alert("ConstraintID: "+id+"\n Position: "+position)
                pos += Math.pow(2, position)
            }
        }
    }
    return pos;
}

function validQuery(){
    
    var query = document.getElementById("query").value;
    var operator = getOperateursValue();
    var measure = getMeasuresValue();
    var filter_value = document.getElementById("filter_value").value;
    var selectedConstraints = getSelectedConstraints_();
    var content = '';

    //alert(measure)

    query = query.replaceAll("'", "\\'");

    if (operator=='top-k' || operator=='top-k_'){
        if (operator=='top-k_')
            filter_value = "-"+filter_value;
        operator='top-k';
        filter_value = parseInt(filter_value); 
        if (filter_value){
            content = "{'query':'"+query+"', 'operator':'"+operator+"', 'measure':["+measure+"], 'filterValue':"+filter_value+", 'selectedConstraints':"+selectedConstraints+"}"; 
            display_figs(content, measure);
        }else
            alert("The filter value most be an integer");
    }

    if (operator=='threshold'){
        var start = filter_value.replaceAll(" ", "");
        filter_value = start[0]; 
        if (filter_value){
            switch (filter_value) {
                case '=':
                    operator = '='
                    filter_value = parseInt(start.substring(1));
                    break;
                case '<':
                    if (start[1]=='='){
                        operator = '<='
                        filter_value = parseInt(start.substring(2));
                    }else {
                        operator = '<'
                        filter_value = parseInt(start.substring(1));
                    }
                    break;
                case '>':
                    if (start[1]=='='){
                        operator = '>='
                        filter_value = parseInt(start.substring(2));
                    }else {
                        operator = '>'
                        filter_value = parseInt(start.substring(1));
                    }
                    break;
                default:
                  operator='NaN';
            }
        }
        if (filter_value && operator != 'NaN'){
            content = "{'query':'"+query+"', 'operator':'"+operator+"', 'measure':'"+measure+"', 'filterValue':"+filter_value+", 'selectedConstraints':"+selectedConstraints+"}"; 
            display_figs(content, measure);
        }else
            alert("The filter value most be for example: =4 or <4 or >4 or <=4 or >=4 with 4 the theshold");
    }
    if (operator=='all'){
        content = "{'query':'"+query+"', 'operator':'"+operator+"', 'measure':'"+measure+"', 'filterValue':"+filter_value+", 'selectedConstraints':"+selectedConstraints+"}"; 
        display_figs(content, measure);
    }
}

function display_figs(data, measure){

    var param = {'method':'Post', 'body':data};
    var url = adr+"query/execution/"
    //if (measure=='CBS' || measure == 'CBM'){
        fetch(url,param).then(e => e.json()).then(e => {			
            
            //display_subsets(e.sub_vio.X, e.sub_vio.Y);
            //display_by_constraint(e.vio_dist.X, e.vio_dist.Y)
            //display_data(e.data)

            display_subsets(e.sub_vio);
            display_by_constraint(e.vio_dist);
            display_data(e.data);

        }).catch(e => {alert(e+': Error in query')});
    //}else {
    //    alert(measure+" is not supported !")
    //}


}

function display_data(d){
    var tab = document.getElementById("result");
    var res = '';

    //alert(JSON.stringify(d));

    var class_ = 'col-md-12 col-sm-12 col-xs-12'

    if (d.length == 2)
        class_ = 'col-md-6 col-sm-6 col-xs-6';
    if (d.length == 3)
        class_ = 'col-md-4 col-sm-4 col-xs-4';
    if (d.length == 4)
        class_ = 'col-md-3 col-sm-3 col-xs-3';
    

    for(var i=0;i<d.length; i++){
        var data = d[i]
        var attrs = data["attrs"]
        var datas = data["data"]
        //start
        res += '<div class="'+class_+'" style="padding: 0.5em;"> <table style="width: 100%">'
        res += '<tr style="background: #aaa; color: white; padding:10em;">';//'<tr> <th>Selection</th> <th>Constraint ID</th> <th>Description</th>   </tr>';
        attrs.forEach(e => {res += '<th>'+e+'</th>';});
        res += '</tr>';
        datas.forEach(el => {var t='<tr>'; el.forEach(e => {t += '<th>'+e+'</th>';}); t+='</tr>'; res+=t;});
        res +="</table> </div>"
    }
    tab.innerHTML = '<div class="row">'+res+"</div>";
}

function getOperateursValue(){
	return document.querySelector('input[name="operateurs"]:checked').value;
}

function getMeasuresValue(){
    var res = [];
    var cbm = document.getElementById("CBM");
    var cbs = document.getElementById("CBS");
    var csm = document.getElementById("CSM");
    var css = document.getElementById("CSS");
    
    if (cbm.checked)
        res.push("'CBM'");
    if (cbs.checked)
        res.push("'CBS'");
    if (csm.checked)
        res.push("'CSM'");
    if (css.checked)
        res.push("'CSS'");
    return res;
	//return document.querySelector('input[name="measures"]:checked').value;
}


function top_k(){
    document.getElementById("filter_value").style = "display: block;";
    document.getElementById("filter_value").placeholder = 'Filter value';
    document.querySelectorAll(".with_treshold").forEach(e => {e.style="display: none;"});
}
function threshold(){
    //alert('threshold')
    document.getElementById("filter_value").style = "display: block;";
    document.getElementById("filter_value").placeholder = 'op Filter value (op: = | <= | >= | < | >)';
    document.querySelectorAll(".with_treshold").forEach(e => {e.style="display: block;"});
}
function all_(){
    //alert('all')
    document.getElementById("filter_value").style = "display: none;";
    document.querySelectorAll(".with_treshold").forEach(e => {e.style="display: none;"});
}


function selectedQuery(){
	var x = document.getElementById("workload");
	var val = x.options[x.selectedIndex].value;
	if (val != ''){
		var query = document.getElementById("query");
		query.value = val;
	}
}


function generateInterval(){
    var min = document.getElementById("min");
    var max = document.getElementById("max");

    //test
    min.innerText = "Min:: CBS: 10, CBM: 12"
    max.innerText = "Max:: CBS: 10, CBM: 12"

}




