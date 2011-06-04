package com.kanasansoft.Xiangpian;

import java.io.IOException;

import jline.console.ConsoleReader;

import org.eclipse.jetty.websocket.WebSocket;

class CUI implements MessageListener {

	private CommandLineOption options = null;
	private ConsoleReader cons = null;

	CUI(String[] args) throws Exception {

		super();

		options=Utility.parseCommandLineOption(args);

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
				cons.println("command> " + data);
			}else if("server_side_command".equals(connectionType)){
				cons.println();
				cons.println("external command> " + data);
				cons.drawLine();
			}else if("client_side".equals(connectionType)){
				cons.println();
				cons.println("result> " + data);
				cons.drawLine();
			}else{
				cons.println();
				cons.println("irregular message(" + connectionType + ")> " + data);
				cons.drawLine();
			}
			cons.flush();
		}catch(IOException e){}
	}

	@Override
	public void onMessage(String connectionType, byte frame, byte[] data, int offset, int length) {
	}
}
