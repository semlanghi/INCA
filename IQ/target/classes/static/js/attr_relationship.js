
function dragstart_(e){
	//alert(e.target.id)
	//console.log(e.target.id)	
    
    e.dataTransfer.effectAllowed = 'copy';
	e.dataTransfer.setData('text', e.target.id);
	return false;
}

function dragover_(e){
	if (e.preventDefault) e.preventDefault();
	    e.dataTransfer.dropEffect = 'copy'; 
}

function drop_(e, ca){
	if (e.stopPropagation) e.stopPropagation();
	var id = e.dataTransfer.getData('text');
	var theitem = document.getElementById(id);
	theitem.parentNode.removeChild(theitem);  
	theitem.className='itemblurred';
	var y  = document.createElement('div');
	y.innerHTML = theitem.innerHTML;
	y.className="produit";
	y.draggable="true";
	y.addEventListener('dragstart', dragstart_)
	y.id = id;
	y.style="display: inline-block; padding:0.5em; margin-top:0.5em;margin-left:0.5em;background: #ddd; border: 1px solid black; border-radius: 5px;";
    
    if (ca.id=="Attributs_relation")
        ca.appendChild(y);
    else {
        if (ca.children.length==0){
            ca.appendChild(y);
        }
        else
            document.getElementById("Attributs_relation").appendChild(y);
    }
    e.preventDefault(); //
}





opts2 ={
    callbacks: {
        label: function(tooltipItem, data) {
            var key = tooltipItem.xLabel+"_"+tooltipItem.yLabel;
            var label = (data.datasets[tooltipItem.datasetIndex].l)[key]

            //alert(data.datasets[tooltipItem.datasetIndex].l);

            return label;
        }
    }
}

function load_attribut_relations(){

    var attributs = document.getElementById("Attributs_relation")
	var attribute1 = document.getElementById("attribute1")
	var attribute2 = document.getElementById("attribute2")
    var relation = document.getElementById("relation_name").value;

    attributs.innerHTML = ''
	attribute1.innerHTML = ''
    attribute2.innerHTML = ''

    //alert('voir');
    
    var data = "{'relation':'"+relation+"'}";
    var param = {'method':'Post', 'body':data};
    var url = adr+"profiling/attributs/"

    fetch(url,param).then(e => e.json()).then (attrs => {
        attrs.forEach(e => {
            if (e!=""){
                var y  = document.createElement('div');
                y.innerHTML = e;
                y.draggable="true";
                y.addEventListener('dragstart', dragstart_)
                y.id = e;
                y.style="display: inline-block; padding:0.5em; margin-top:0.5em;margin-left:0.5em;background: #ddd; border: 1px solid black; border-radius: 5px;";
                attributs.appendChild(y);
            }
        });
    
    }).catch(e => {alert("Error: This table does not exit")});

    attributs.addEventListener('dragover', e => {dragover_(e); return false;}, false);
	attributs.addEventListener('dragenter', e => false, false);
	attributs.addEventListener('dragleave', e => false, false);
	attributs.addEventListener('drop', e =>  {drop_(e, attributs);return false;}, false);

	attribute1.addEventListener('dragover', e => {dragover_(e); return false;}, false);
	attribute1.addEventListener('dragenter', e => false, false);
	attribute1.addEventListener('dragleave', e => false, false);
	attribute1.addEventListener('drop', e =>  {drop_(e, attribute1);return false;}, false);

	attribute2.addEventListener('dragover', e => {dragover_(e); return false;}, false);
	attribute2.addEventListener('dragenter', e => false, false);
	attribute2.addEventListener('dragleave', e => false, false);
	attribute2.addEventListener('drop', e =>  {drop_(e, attribute2);return false;}, false);
    //clear_constraints_exploration();
}

function show_figure(){
    var attribute1 = document.getElementById("attribute1")
	var attribute2 = document.getElementById("attribute2")
    var relation = document.getElementById("relation_name").value;
    
    var data = "";
    var val1 = ""
    var val2 = ""
  
    attribute1.childNodes.forEach(e => {
        if (e)
            val1=e.innerText;
    })
    attribute2.childNodes.forEach(e => {
        if (e)
            val2 = e.innerText;
    })

    if (attribute1.hasChildNodes() && attribute2.hasChildNodes()){
        data = "{'relation':'"+relation+"', 'a1':'"+val1+"', 'a2':'"+val2+"'}";
        display_fig(data)
    }else{
        if (attribute1.hasChildNodes()){
            data = "{'relation':'"+relation+"', 'a1':'"+val1+"'}";    
            display_fig(data)
        }else
        if (attribute2.hasChildNodes()){
            data = "{'relation':'"+relation+"', 'a2':'"+val2+"'}";
            display_fig(data)
        }
    }
}


function display_fig(data){
    var param = {'method':'Post', 'body':data};
    var url = adr+"profiling/bytable/"
    fetch(url,param).then(e => e.json()).then (e => {
        document.getElementById("attr1_attr2_").innerHTML = '<canvas id="attr1_attr2"></canvas>';
        var ctx = document.getElementById('attr1_attr2').getContext('2d');
        var yLabels = e.js;
        if (yLabels){
            var scales__ = {
                yAxes: [{
                    ticks: {
                        callback: function(value, index, values) {
                            //console.log(index+":"+value+":"+values)
                            return yLabels[value];
                        },
                        maxTicksLimit:yLabels.length
                    }
                }],
                xAxes: [{
                    ticks: {maxTicksLimit:e.xlength}
                }]
            }
            e["options"]["tooltips"] = opts2
            e["options"]["scales"] = scales__;
        }
        var chart = new Chart(ctx, e);
    }).catch(e => {alert(e)});
}