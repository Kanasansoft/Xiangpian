package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandHelp extends Command {

	public CommandHelp() {
		super();
		name = "help";
		example = "help";
		description = "display help.";
	}

	@Override
	COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException {
		console.println("  '"+Command.getCommandPrefix()+"' is necessary for the command as the prefix.");
		int commandExampleLength = 0;
		for(Command command : getCommandList()) {
			commandExampleLength = Math.max(commandExampleLength, command.getExample().length());
		}
		for(Command command : getCommandList()) {
			console.println(String.format("    "+Command.getCommandPrefix()+"%-"+commandExampleLength+"s | %s", command.getExample(), command.getDescription()));
		}
		return COMMAND_RESULT.END;
	}

}
