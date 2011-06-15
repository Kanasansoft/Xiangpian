(function(){

	var prefix="_xiangpian_";
	var paramUrlName="execute_xiangpian_url";
	var dataUrlName="data-xiangpian-execute-url";
	var dataFromName="data-xiangpian-execute_from";

	//get websocket url in url parameter
	var paramUrl=(function(){
		var getParameters=function(){
			var data={};
			var params=window.location.search.replace(/^\?/,"").split("&");
			for(var i=0;i<params.length;i++){
				var keyvalue=params[i].split("=");
				if(keyvalue.length!=2){continue;}
				var key=decodeURIComponent(keyvalue[0]);
				var value=decodeURIComponent(keyvalue[1]);
				if(!data.hasOwnProperty(key)){
					data[key]=[];
				}
				data[key].push(value);
			}
			return data;
		};
		var params=getParameters();
		return (paramUrlName in params)?params[paramUrlName][0]:void(0);
	})();

	//get websocket url in custom data attribute
	var dataUrl=(function(){
		var scripts=document.getElementsByTagName("script");
		if(scripts.length=0){return void(0);}
		var script=scripts[scripts.length-1];
		return script.getAttribute(dataUrlName);
	})();

	var wsUrl=paramUrl||dataUrl;

	//remove script tag (case execute from bookmarklet)
	(function(){
		var scripts=document.getElementsByTagName("script");
		for(var i=scripts.length-1;0<=i;i--){
			var script=scripts[i];
			if(script.getAttribute(dataFromName)=="bookmarklet"){
				script.parentNode.removeChild(script);
			}
		}
	})();

	var firstFlag=true;

	//make console wrapper
	(function(){
		var re=new RegExp("^"+prefix);
		var keys=[];
		for(var key in console){
			//check made wrapper
			if(re.test(key)){
				firstFlag=false;
				return;
			}
			keys.push(key);
		}
		var handler=function(key){
			return function(){
				console[prefix+key].apply(console,arguments);
				ws.send(JSON.stringify(
						{
							"method name":key,
							"arguments":Array.prototype.slice.apply(arguments,[])
						}
				));
			};
		};
		keys.forEach(function(key){
			console[prefix+key]=console[key];
			console[key]=handler(key);
		});
	})();

	if(!firstFlag){
		return;
	}

	//setting websocket
	function onOpenWebSocket(){
		ws.send("open");
	}
	function onMessageWebSocket(evt){
		var command=evt.data;
		try{
			eval.call(window,command);
		}catch(e){
			ws.send(e);
		}
	}
	function onUnloadWindow(){
		ws.close();
	}
	var ws=new WebSocket(wsUrl,"client_side");
	ws.addEventListener("open",onOpenWebSocket,false);
	ws.addEventListener("message",onMessageWebSocket,false);
	window.addEventListener("unload",onUnloadWindow,false);

})();
