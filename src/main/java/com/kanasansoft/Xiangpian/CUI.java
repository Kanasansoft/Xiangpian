package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jetty.util.StringUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import com.kanasansoft.Xiangpian.Core.CONNECTION_TYPE;
import com.kanasansoft.Xiangpian.Core.MESSAGE_TYPE;
import com.kanasansoft.Xiangpian.Core.MESSAGE_FORMAT;
import com.kanasansoft.Xiangpian.Core.SendData;

class CUI implements MessageListener {

	private CommandLineOption options = null;
	private ConsoleReader cons = null;
	private List<Command> commands = new ArrayList<Command>(){
		private static final long serialVersionUID = 1L;
		{
			add(new CommandDisplay());
			add(new CommandClear());
			add(new CommandExecute());
			add(new CommandExecuteForce());
			add(new CommandInsert());
			add(new CommandReplace());
			add(new CommandRemove());
			add(new CommandExit());
			add(new CommandHelp());
		}
	};

	@SuppressWarnings("serial")
	HashMap<Boolean,HashMap<String,String>> styles=new HashMap<Boolean,HashMap<String,String>>(){{
		put(false,new HashMap<String,String>(){{
			put(MESSAGE_TYPE.COMMAND.toString(),"");
			put(MESSAGE_TYPE.RESULT.toString(),"");
			put(MESSAGE_TYPE.CONSOLE.toString(),"");
			put(MESSAGE_TYPE.STATUS.toString(),"");
			put(MESSAGE_TYPE.ERROR.toString(),"");
			put("irregular_message","");
			put("end","");
		}});
		put(true,new HashMap<String,String>(){{
			put(MESSAGE_TYPE.COMMAND.toString(),"\u001B[32m");
			put(MESSAGE_TYPE.RESULT.toString(),"\u001B[31m");
			put(MESSAGE_TYPE.CONSOLE.toString(),"\u001B[31m");
			put(MESSAGE_TYPE.STATUS.toString(),"\u001B[31m");
			put(MESSAGE_TYPE.ERROR.toString(),"\u001B[31m");
			put("irregular_message","\u001B[41m\u001B[37m");
			put("end","\u001B[0m");
		}});
	}};

	enum COMMAND_LIST {
		DISPLAY("display"),CLEAR("clear"),EXECUTE("execute"),EXECUTE_FORCE("execute_force"),INSERT("insert"),REPLACE("replace"),REMOVE("remove"),EXIT("exit"),HELP("remove");
		private String string;
		private COMMAND_LIST(String string){
			this.string=string;
		}
		public String toString(){
			return string;
		}
		public static COMMAND_LIST get(String string){
			for(COMMAND_LIST type:COMMAND_LIST.values()){
				if(type.name().equalsIgnoreCase(string)){
					return type;
				}
			}
			return null;
		}
		public static String getName(){
			return "command_list";
		}
	}

	enum COMMAND_RESULT {
		EXECUTE,EXECUTE_FORCE,END,NO_COMMAND;
	}

	HashMap<String,String> style=null;

	CUI(String[] args) throws Exception {

		super();

		options=Utility.parseCommandLineOption(args);

		Command.setCommandPrefix(options.getCommandPrefix());

		style=styles.get(options.isColor());

		Core core = new Core(args);

		core.setListener(this);

		cons = new ConsoleReader();
		cons.setBellEnabled(false);
		cons.setPrompt("% ");

		executeCommand(cons, Command.getCommandPrefix()+"help", null);

		ArrayList<String> candidates = new ArrayList<String>();
		for(Command command : commands){
			candidates.add(Command.getCommandPrefix()+command.getName());
		}
		StringsCompleter stringsCompleter = new StringsCompleter(candidates);
		cons.addCompleter(stringsCompleter);

		Context cx = Context.enter();

		ArrayList<String> multilines = new ArrayList<String>();
		String line;

		while((line=cons.readLine())!=null){

			if(!"".equals(line)){

				COMMAND_RESULT result = COMMAND_RESULT.EXECUTE;

				//command
				if(line.startsWith(options.getCommandPrefix())){
					result = executeCommand(cons, line, multilines);
					cons.flush();
				}else{
					multilines.add(line);
				}

				switch (result) {
				case EXECUTE:											break;
				case EXECUTE_FORCE:										break;
				case END:												continue;
				case NO_COMMAND:	cons.println("unknown command.");	continue;
				default:			cons.println("command error.");		break;
				}

				//execute
				String lines=Utility.joinString(multilines,StringUtil.CRLF);
				try {
					if(result!=COMMAND_RESULT.EXECUTE_FORCE){
						cx.compileString(lines, "", 1, null);
					}
					core.onMessage(lines);
					multilines.clear();
				} catch (EvaluatorException e) {
				}

			}

		}

	}

	private COMMAND_RESULT executeCommand(ConsoleReader cons,String commandline,List<String> lines) throws IOException{

		//get command name and arguments string
		String commandAndArgsString=commandline.substring(Command.getCommandPrefix().length());
		String[] commandAndArgsArray = commandAndArgsString.split("\\s+",2);
		List<String> commandAndArgsList = Arrays.asList(commandAndArgsArray);
		String commandName = commandAndArgsList.get(0);
		String argsString = commandAndArgsList.size()==1?"":commandAndArgsList.get(1);

		for(Command command : commands){
			if(command.getName().equals(commandName)){
				return command.execute(cons, argsString, lines);
			}
		}
		return COMMAND_RESULT.NO_COMMAND;

	}

	@Override
	public void onMessage(CONNECTION_TYPE connectionType, SendData sendData) {

		String mtype=null;
		String mformat=null;
		String console=null;
		String data=null;
		if(connectionType==null||sendData==null||sendData.getMessageType()==null||sendData.getMessageFormat()==null||sendData.getData()==null){
			mtype="irregular_message";
			console="irregular message"+
				"(connection type:"+(connectionType==null?"null":connectionType.toString())+")"+
				"(message type:"+((sendData==null||sendData.getMessageType()==null)?"null":sendData.getMessageType().toString())+")"+
				"(message format:"+((sendData==null||sendData.getMessageFormat()==null)?"null":sendData.getMessageFormat().toString())+")";
			data=(sendData==null||sendData.getData()==null)?"sendData is null":sendData.getData();
		}else{
			mtype=sendData.getMessageType().toString();
			mformat=sendData.getMessageFormat().toString();
			console=mtype.toLowerCase();
			if(MESSAGE_FORMAT.JSON.toString().equalsIgnoreCase(mformat)){
				try {
					data=JSON.encode(JSON.decode(sendData.getData()),true);
				} catch (JSONException e) {
					data=sendData.getData();
				}
			}else{
				data=sendData.getData();
			}
		}

		try{
			if(!CONNECTION_TYPE.LISTENER.equals(connectionType)){
				cons.println();
			}
			cons.println(style.get(mtype)+console+">"+style.get("end"));
			cons.println(style.get(mtype)+data+style.get("end"));
			if(!CONNECTION_TYPE.LISTENER.equals(connectionType)){
				cons.drawLine();
			}
			cons.flush();
		}catch(IOException e){}catch (Exception e) {
			System.out.println(sendData.getData());
		}

	}

}
