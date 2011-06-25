package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandClear extends Command {

	public CommandClear() {
		super();
		name = "clear";
		example = "clear";
		description = "clear multilines stack code.";
	}

	@Override
	COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException {
		String args=argsString.replaceAll("\\s+", "");
		if(args.length()!=0){
			console.println("no need arguments.");
			return COMMAND_RESULT.END;
		}
		console.println("do you clear really? y/n");
		console.flush();
		int virtualKey = console.readVirtualKey();
		if(virtualKey==89||virtualKey==121){
			lines.clear();
			console.println("clear all.");
		}else{
			console.println("cancel clear.");
		}
		return COMMAND_RESULT.END;
	}

}
