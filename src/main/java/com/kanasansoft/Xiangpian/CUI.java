package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.util.StringUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

import jline.console.ConsoleReader;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import com.kanasansoft.Xiangpian.Core.CONNECTION_TYPE;
import com.kanasansoft.Xiangpian.Core.MESSAGE_TYPE;
import com.kanasansoft.Xiangpian.Core.SendData;

class CUI implements MessageListener {

	private CommandLineOption options = null;
	private ConsoleReader cons = null;
	private static String commandStartString = ";;";

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

	HashMap<COMMAND_LIST,String> commandHelpList=new HashMap<COMMAND_LIST, String>(){{
		put(COMMAND_LIST.DISPLAY,"display [startLineNumber [length]] | display multilines stack code.");
		put(COMMAND_LIST.CLEAR,"clear | clear multilines stack code.");
		put(COMMAND_LIST.EXECUTE,"execute | execute multilines stack code. (don't execute when parse error)");
		put(COMMAND_LIST.EXECUTE_FORCE,"execute_force | execute multilines stack code by force. (execute even when parse error)");
		put(COMMAND_LIST.INSERT,"insert lineNumber insertCode | insert code into specify point.");
		put(COMMAND_LIST.REPLACE,"replace lineNumber replaceCode | replace code as specify point.");
		put(COMMAND_LIST.REMOVE,"remove lineNumber | remove code from specify point.");
		put(COMMAND_LIST.EXIT,"exit | exit application.");
		put(COMMAND_LIST.HELP,"help | display help.");
	}};

	HashMap<String,String> style=null;

	CUI(String[] args) throws Exception {

		super();

		options=Utility.parseCommandLineOption(args);

		style=styles.get(options.isColor());

		Core core = new Core(args);

		core.setListener(this);

		cons = new ConsoleReader();
		cons.setBellEnabled(false);
		cons.setPrompt("% ");

		executeCommandHelp(null, cons, null);

		Context cx = Context.enter();

		ArrayList<String> multilines = new ArrayList<String>();
		String line;

		while((line=cons.readLine())!=null){

			if(!"".equals(line)){

				COMMAND_RESULT result = COMMAND_RESULT.EXECUTE;

				//command
				if(line.startsWith(commandStartString)){
					result = executeCommand(line, cons, multilines,commandStartString);
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

	private COMMAND_RESULT executeCommand(String commandline,ConsoleReader cons,List<String> lines,String commandStartString) throws IOException{

		//get command name and arguments string
		String commandAndArgsString=commandline.substring(2);
		String[] commandAndArgsArray = commandAndArgsString.split("\\s+",2);
		List<String> commandAndArgsList = Arrays.asList(commandAndArgsArray);
		COMMAND_LIST command = COMMAND_LIST.get(commandAndArgsList.get(0));
		String argsString = commandAndArgsList.size()==1?"":commandAndArgsList.get(1);

		if(command==null){
			return COMMAND_RESULT.NO_COMMAND;
		}
		switch(command){
		case DISPLAY:		return executeCommandDisplay(argsString, cons, lines);
		case CLEAR:			return executeCommandClear(argsString, cons, lines);
		case EXECUTE:		return executeCommandExecute(argsString, cons, lines);
		case EXECUTE_FORCE:	return executeCommandExecuteForce(argsString, cons, lines);
		case INSERT:		return executeCommandInsert(argsString, cons, lines);
		case REPLACE:		return executeCommandReplace(argsString, cons, lines);
		case REMOVE:		return executeCommandRemove(argsString, cons, lines);
		case EXIT:			return executeCommandExit(argsString, cons, lines);
		case HELP:			return executeCommandHelp(argsString, cons, lines);
		default:			return COMMAND_RESULT.NO_COMMAND;
		}

	}

	private COMMAND_RESULT executeCommandDisplay(String argsString,ConsoleReader cons,List<String> lines) throws IOException{
		String args=argsString.replaceAll("\\s+", " ").trim();
		List<String> argsList=new ArrayList<String>();
		if(args.length()!=0){
			String[] argsArray=args.split("\\s+");
			argsList=Arrays.asList(argsArray);
		}
		if(0<=argsList.size()&&argsList.size()<=2){
		}else{
			cons.println("many count of arguments.");
			return COMMAND_RESULT.END;
		}
		for(String arg:argsList){
			if(!arg.matches("^\\d+$")){
				cons.println("arguments is not number.");
				return COMMAND_RESULT.END;
			}
		}
		int startPos=0;
		if(argsList.size()>=1){
			int num = Integer.parseInt(argsList.get(0),10);
			startPos=Math.min(num, lines.size());
		}
		int endPos=lines.size();
		if(argsList.size()>=2){
			int num = Integer.parseInt(argsList.get(1),10);
			endPos=Math.min(startPos+num, endPos);
		}
		for(int i=startPos;i<endPos;i++){
			String line=lines.get(i);
			cons.println(String.format("%4d : %s", i,line));
		}
		return COMMAND_RESULT.END;
	}

	private COMMAND_RESULT executeCommandClear(String argsString,ConsoleReader cons,List<String> lines) throws IOException{
		String args=argsString.replaceAll("\\s+", "");
		if(args.length()!=0){
			cons.println("no need arguments.");
			return COMMAND_RESULT.END;
		}
		cons.println("do you clear really? y/n");
		cons.flush();
		int virtualKey = cons.readVirtualKey();
		if(virtualKey==89||virtualKey==121){
			lines.clear();
			cons.println("clear all.");
		}else{
			cons.println("cancel clear.");
		}
		return COMMAND_RESULT.END;
	}

	private COMMAND_RESULT executeCommandExecute(String argsString,ConsoleReader cons,List<String> lines) throws IOException{
		String args=argsString.replaceAll("\\s+", "");
		if(args.length()!=0){
			cons.println("no need arguments.");
			return COMMAND_RESULT.END;
		}
		return COMMAND_RESULT.EXECUTE;
	}

	private COMMAND_RESULT executeCommandExecuteForce(String argsString,ConsoleReader cons,List<String> lines) throws IOException{
		String args=argsString.replaceAll("\\s+", "");
		if(args.length()!=0){
			cons.println("no need arguments.");
			return COMMAND_RESULT.END;
		}
		return COMMAND_RESULT.EXECUTE_FORCE;
	}

	private COMMAND_RESULT executeCommandInsert(String argsString,ConsoleReader cons,List<String> lines) throws IOException{
		String[] argsArray=argsString.split("\\s",2);
		if(argsArray.length!=2){
			cons.println("no enough count of arguments.");
			return COMMAND_RESULT.END;
		}
		if(!argsArray[0].matches("^\\d+$")){
			cons.println("first argument is not number.");
			return COMMAND_RESULT.END;
		}
		int pos = Integer.parseInt(argsArray[0],10);
		if(pos>lines.size()){
			cons.println("line number out of bound.");
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

	private COMMAND_RESULT executeCommandReplace(String argsString,ConsoleReader cons,List<String> lines) throws IOException{
		String[] argsArray=argsString.split("\\s",2);
		if(argsArray.length!=2){
			cons.println("no enough count of arguments.");
			return COMMAND_RESULT.END;
		}
		if(!argsArray[0].matches("^\\d+$")){
			cons.println("first argument is not number.");
			return COMMAND_RESULT.END;
		}
		int pos = Integer.parseInt(argsArray[0],10);
		if(pos>=lines.size()){
			cons.println("line number out of bound.");
			return COMMAND_RESULT.END;
		}
		lines.set(pos, argsArray[1]);
		return COMMAND_RESULT.END;
	}

	private COMMAND_RESULT executeCommandRemove(String argsString,ConsoleReader cons,List<String> lines) throws IOException{
		String arg=argsString.trim();
		if(arg.length()==0){
			cons.println("no enough count of arguments.");
			return COMMAND_RESULT.END;
		}
		if(!arg.matches("^\\d+$")){
			cons.println("first argument is not number.");
			return COMMAND_RESULT.END;
		}
		int pos = Integer.parseInt(arg,10);
		if(pos>=lines.size()){
			cons.println("line number out of bound.");
			return COMMAND_RESULT.END;
		}
		lines.remove(pos);
		return COMMAND_RESULT.END;
	}

	private COMMAND_RESULT executeCommandExit(String argsString, ConsoleReader cons, List<String> lines) throws IOException{
		System.exit(0);
		return COMMAND_RESULT.END;
	}

	private COMMAND_RESULT executeCommandHelp(String argsString, ConsoleReader cons, List<String> lines) throws IOException{
		cons.println("  '"+commandStartString+"' is necessary for the command as the prefix.");
		 List<COMMAND_LIST> keys = Arrays.asList(commandHelpList.keySet().toArray(new COMMAND_LIST[]{}));
		Collections.sort(keys);
		for(COMMAND_LIST key : keys){
			cons.println("    "+commandStartString+commandHelpList.get(key));
		}
		return COMMAND_RESULT.END;
	}

	@Override
	public void onMessage(CONNECTION_TYPE connectionType, SendData sendData) {

		String mtype=null;
		String console=null;
		String data=null;
		if(connectionType==null||sendData==null||sendData.getMessageType()==null||sendData.getData()==null){
			mtype="irregular_message";
			console="irregular message"+
				"(connection type:"+(connectionType==null?"null":connectionType.toString())+")"+
				"(message type:"+((sendData==null||sendData.getMessageType()==null)?"null":sendData.getMessageType().toString())+")";
			data=(sendData==null||sendData.getData()==null)?"sendData is null":sendData.getData();
		}else{
			mtype=sendData.getMessageType().toString();
			console=mtype.toLowerCase();
			if(MESSAGE_TYPE.RESULT.toString().equalsIgnoreCase(mtype)){
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
