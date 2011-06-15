function initial(){
	var protocol=(location.protocol=="https:")?"wss":"ws";
	var host=location.host;
	var wsurl=protocol+"://"+host+"/ws/";
	var jsurl=location.protocol+"//"+host+"/execute.js"
	var bookmarklets=[
		'(function(){',
		'var script=document.createElement("script");',
		'script.type="text/javascript";',
		'script.src="'+jsurl+'";',
		'script.setAttribute("data-execute-xiangpian-url","'+wsurl+'");',
		'document.body.appendChild(script);',
		'})();'
	];
	var bookmarklet="javascript:"+encodeURIComponent(bookmarklets.join(""));
	document.getElementById("bookmarklet").href=bookmarklet;
	document.getElementById("bookmarklet_for_ios").href="?Xiangpian&-----&"+bookmarklet;
}
window.addEventListener("load",initial,false);