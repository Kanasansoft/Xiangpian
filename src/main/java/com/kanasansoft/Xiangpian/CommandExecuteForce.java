package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandExecuteForce extends Command {

	public CommandExecuteForce() {
		super();
		name = "execute_force";
		example = "execute_force";
		description = "execute multilines stack code by force. (execute even when parse error)";
	}

	@Override
	COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException {
		String args=argsString.replaceAll("\\s+", "");
		if(args.length()!=0){
			console.println("no need arguments.");
			return COMMAND_RESULT.END;
		}
		return COMMAND_RESULT.EXECUTE_FORCE;
	}

}
