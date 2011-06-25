package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kanasansoft.Xiangpian.CUI.COMMAND_RESULT;

import jline.console.ConsoleReader;

abstract class Command {

	protected String name;
	protected String example;
	protected String description;
	private static String prefix = null;
	private static List<Command> commandList = new ArrayList<Command>();

	Command() {
		commandList.add(this);
	}

	String getName() {
		return name;
	}

	String getExample() {
		return example;
	}

	String getDescription() {
		return description;
	}

	abstract COMMAND_RESULT execute(ConsoleReader console, String argsString, List<String> lines) throws IOException;

	static void setCommandPrefix(String commandPrefix){
		prefix = commandPrefix;
	}

	static String getCommandPrefix(){
		return prefix;
	}

	static List<Command> getCommandList() {
		return commandList;
	}

	static void setCommandList(List<Command> commandList) {
		Command.commandList = commandList;
	}

}
