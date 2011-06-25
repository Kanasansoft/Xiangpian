package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandRemove extends Command {

	public CommandRemove() {
		super();
		name = "remove";
		example = "remove lineNumber";
		description = "remove code from specify point.";
	}

	@Override
	COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException {
		String arg=argsString.trim();
		if(arg.length()==0){
			console.println("no enough count of arguments.");
			return COMMAND_RESULT.END;
		}
		if(!arg.matches("^\\d+$")){
			console.println("first argument is not number.");
			return COMMAND_RESULT.END;
		}
		int pos = Integer.parseInt(arg,10);
		if(pos>=lines.size()){
			console.println("line number out of bound.");
			return COMMAND_RESULT.END;
		}
		lines.remove(pos);
		return COMMAND_RESULT.END;
	}

}
