package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandExecute extends Command {

	public CommandExecute() {
		super();
		name = "execute";
		example = "execute";
		description = "execute multilines stack code. (don't execute when parse error)";
	}

	@Override
	COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException {
		String args=argsString.replaceAll("\\s+", "");
		if(args.length()!=0){
			console.println("no need arguments.");
			return COMMAND_RESULT.END;
		}
		return COMMAND_RESULT.EXECUTE;
	}

}
