package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

class CommandExit extends Command {

	public CommandExit() {
		super();
		name = "exit";
		example = "exit";
		description = "exit application.";
	}

	@Override
	COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException {
		System.exit(0);
		return COMMAND_RESULT.END;
	}

}
