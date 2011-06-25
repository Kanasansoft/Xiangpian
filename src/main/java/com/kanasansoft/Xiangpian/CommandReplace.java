package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandReplace extends Command {

	public CommandReplace() {
		super();
		name = "replace";
		example = "replace lineNumber replaceCode";
		description = "replace code as specify point.";
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
		if(pos>=lines.size()){
			console.println("line number out of bound.");
			return COMMAND_RESULT.END;
		}
		lines.set(pos, argsArray[1]);
		return COMMAND_RESULT.END;
	}

}
