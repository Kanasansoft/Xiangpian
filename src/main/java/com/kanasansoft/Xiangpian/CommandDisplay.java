package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandDisplay extends Command {

	public CommandDisplay() {
		super();
		name = "display";
		example = "display [startLineNumber [length]]";
		description = "display multilines stack code.";
	}

	@Override
	COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException {
		String args=argsString.replaceAll("\\s+", " ").trim();
		List<String> argsList=new ArrayList<String>();
		if(args.length()!=0){
			String[] argsArray=args.split("\\s+");
			argsList=Arrays.asList(argsArray);
		}
		if(0<=argsList.size()&&argsList.size()<=2){
		}else{
			console.println("many count of arguments.");
			return COMMAND_RESULT.END;
		}
		for(String arg:argsList){
			if(!arg.matches("^\\d+$")){
				console.println("arguments is not number.");
				return COMMAND_RESULT.END;
			}
		}
		int startPos=0;
		if(argsList.size()>=1){
			int num = Integer.parseInt(argsList.get(0),10);
			startPos=Math.min(num, lines.size());
		}
		int endPos=lines.size();
		if(argsList.size()>=2){
			int num = Integer.parseInt(argsList.get(1),10);
			endPos=Math.min(startPos+num, endPos);
		}
		for(int i=startPos;i<endPos;i++){
			String line=lines.get(i);
			console.println(String.format("%4d : %s", i,line));
		}
		return COMMAND_RESULT.END;
	}

}
