function initial(){
	var protocol=(location.protocol=="https:")?"wss":"ws";
	var host=location.host;
	var wsurl=protocol+"://"+host+"/ws/";
	var jsurl=location.protocol+"//"+host+"/execute.js"
	var bookmarklets=[
		'(function(){',
		'var script=document.createElement("script");',
		'script.setAttribute("type","text/javascript");',
		'script.setAttribute("src","'+jsurl+'");',
		'script.setAttribute("data-xiangpian-execute-url","'+wsurl+'");',
		'script.setAttribute("data-xiangpian-execute_from","bookmarklet");',
		'document.body.appendChild(script);',
		'})();'
	];
	var bookmarklet="javascript:"+encodeURIComponent(bookmarklets.join(""));
	document.getElementById("bookmarklet").href=bookmarklet;
	document.getElementById("bookmarklet_for_ios").href="?Xiangpian&-----&"+bookmarklet;
}
window.addEventListener("load",initial,false);
