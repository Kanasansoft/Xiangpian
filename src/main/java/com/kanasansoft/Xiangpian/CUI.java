package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.HashMap;

import jline.console.ConsoleReader;

import org.eclipse.jetty.websocket.WebSocket;

class CUI implements MessageListener {

	private CommandLineOption options = null;
	private ConsoleReader cons = null;

	enum STYLE {COMMAND,EXTERNAL_COMMAND,RESULT,IRREGULAR_MESSAGE,END}

	HashMap<Boolean,HashMap<STYLE,String>> styles=new HashMap<Boolean,HashMap<STYLE,String>>(){{
		put(false,new HashMap<STYLE,String>(){{
			put(STYLE.COMMAND,"");
			put(STYLE.EXTERNAL_COMMAND,"");
			put(STYLE.RESULT,"");
			put(STYLE.IRREGULAR_MESSAGE,"");
			put(STYLE.END,"");
		}});
		put(true,new HashMap<STYLE,String>(){{
			put(STYLE.COMMAND,"\u001B[32m");
			put(STYLE.EXTERNAL_COMMAND,"\u001B[32m");
			put(STYLE.RESULT,"\u001B[31m");
			put(STYLE.IRREGULAR_MESSAGE,"\u001B[41m\u001B[37m");
			put(STYLE.END,"\u001B[0m");
		}});
	}};

	HashMap<STYLE,String> style=null;

	CUI(String[] args) throws Exception {

		super();

		options=Utility.parseCommandLineOption(args);

		style=styles.get(options.isColor());

		Core core = new Core(args);

		core.setListener(this);

		cons = new ConsoleReader();
		cons.setBellEnabled(false);
		cons.setPrompt("% ");

		String line;
		while((line=cons.readLine())!=null){
			if(!"".equals(line)){
				core.onMessage("console", WebSocket.SENTINEL_FRAME, line);
			}
		}

	}

	@Override
	public void onMessage(String connectionType, byte frame, String data) {
		try{
			if("console".equals(connectionType)){
				cons.println(style.get(STYLE.COMMAND)+"command>"+style.get(STYLE.END));
				cons.println(style.get(STYLE.COMMAND)+data+style.get(STYLE.END));
			}else if("server_side_command".equals(connectionType)){
				cons.println();
				cons.println(style.get(STYLE.EXTERNAL_COMMAND)+"external command>"+style.get(STYLE.END));
				cons.println(style.get(STYLE.EXTERNAL_COMMAND)+data+style.get(STYLE.END));
				cons.drawLine();
			}else if("client_side".equals(connectionType)){
				cons.println();
				cons.println(style.get(STYLE.RESULT)+"result>"+style.get(STYLE.END));
				cons.println(style.get(STYLE.RESULT)+data+style.get(STYLE.END));
				cons.drawLine();
			}else{
				cons.println();
				cons.println(style.get(STYLE.IRREGULAR_MESSAGE)+"irregular message(" + connectionType + ")>"+style.get(STYLE.END));
				cons.println(style.get(STYLE.IRREGULAR_MESSAGE)+data+style.get(STYLE.END));
				cons.drawLine();
			}
			cons.flush();
		}catch(IOException e){}
	}

	@Override
	public void onMessage(String connectionType, byte frame, byte[] data, int offset, int length) {
	}
}
