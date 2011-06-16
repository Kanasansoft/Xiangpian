var parameters;
var commandWS;
var resultWS;
var elemReceiveMessages;
var elemCode;

function getParameters(){
	var data={};
	var params=location.search.replace(/^\?/,"").split("&");
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
}
function sendCode(){
	if(elemCode.value!=""){
		commandWS.send(elemCode.value);
		elemCode.value="";
	}
	elemCode.focus();
}
function reflectOldCommand(){
	var value=elemCode.value;
	var text=this.textContent;
	if(value.length!=0&&!(/(\r|\n)/.test(value.slice(-1)))){
		text="\n"+text;
	}
	if(!(/(\r|\n)/.test(text.slice(-1)))){
		text=text+"\n";
	}
	elemCode.value+=text;
	elemCode.focus();
}
function clearMessages(){
	while(elemReceiveMessages.hasChildNodes()){
		elemReceiveMessages.removeChild(elemReceiveMessages.firstChild);
	}
}
function onControlEnter(evt){
	if(evt.ctrlKey&&!evt.shiftKey&&!evt.altKey&&!evt.metaKey&&evt.keyCode==13){
		evt.preventDefault();
		sendCode();
	}
}
function onOpenCommand(){
	document.getElementById("btn_code_send").addEventListener("click",sendCode,false);
	document.getElementById("btn_messages_clear").addEventListener("click",clearMessages,false);
	elemCode.addEventListener("keydown",onControlEnter,false);
}
function onMessageCommand(evt){
	var message=evt.data;
	var elem=document.createElement("div");
	elem.className="receive_command";
	elem.textContent=message;
	elem.style.cursor="pointer";
	elem.addEventListener("click",reflectOldCommand,false);
	elemReceiveMessages.appendChild(elem);
}
function onMessageResult(evt){
	var message=evt.data;
	var elem=document.createElement("div");
	elem.className="receive_result";
	elem.textContent=message;
	elemReceiveMessages.appendChild(elem);
}
function onUnloadWindow(){
	commandWS.close();
	resultWS.close();
}
function initial(){
	elemReceiveMessages=document.getElementById("receive_messages");
	elemCode=document.getElementById("code");
	var protocol=(location.protocol=="https:")?"wss":"ws";
	var host=location.host;
	commandWS=new WebSocket(protocol+"://"+host+"/ws/","server_side_command");
	resultWS=new WebSocket(protocol+"://"+host+"/ws/","server_side_result");
	commandWS.addEventListener("open",onOpenCommand,false);
	commandWS.addEventListener("message",onMessageCommand,false);
	resultWS.addEventListener("message",onMessageResult,false);
	window.addEventListener("unload",onUnloadWindow,false);
}

window.addEventListener("load",initial,false);
