package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.HashMap;

import jline.console.ConsoleReader;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import com.kanasansoft.Xiangpian.Core.CONNECTION_TYPE;
import com.kanasansoft.Xiangpian.Core.MESSAGE_TYPE;
import com.kanasansoft.Xiangpian.Core.SendData;

class CUI implements MessageListener {

	private CommandLineOption options = null;
	private ConsoleReader cons = null;

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

		String line;
		while((line=cons.readLine())!=null){
			if(!"".equals(line)){
				core.onMessage(line);
			}
		}

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
