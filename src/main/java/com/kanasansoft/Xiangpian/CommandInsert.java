package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandInsert extends Command {

	public CommandInsert() {
		super();
		name = "insert";
		example = "insert lineNumber insertCode";
		description = "insert code into specify point.";
	}

	@Override
	COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException {
		String[] argsArray=argsString.split("\\s",2);
		if(argsArray.length!=2){
			console.println("no enough count of arguments.");
			return COMMAND_RESULT.END;
		}
		if(!argsArray[0].matches("^\\d+$")){
			console.println("first argument is not number.");
			return COMMAND_RESULT.END;
		}
		int pos = Integer.parseInt(argsArray[0],10);
		if(pos>lines.size()){
			console.println("line number out of bound.");
			return COMMAND_RESULT.END;
		}
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(lines.subList(0, pos));
		list.add(argsArray[1]);
		list.addAll(lines.subList(pos, lines.size()));
		lines.clear();
		lines.addAll(list);
		return COMMAND_RESULT.END;
	}

}
