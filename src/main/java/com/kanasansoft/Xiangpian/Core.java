package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class Core {

	enum MESSAGE_TYPE {
		COMMAND("command"),RESULT("result"),CONSOLE("console"),STATUS("status"),ERROR("error");
		private String string;
		private MESSAGE_TYPE(String string){
			this.string=string;
		}
		public String toString(){
			return string;
		}
		public static MESSAGE_TYPE get(String string){
			for(MESSAGE_TYPE type:MESSAGE_TYPE.values()){
				if(type.name().equalsIgnoreCase(string)){
					return type;
				}
			}
			return null;
		}
		public static String getName(){
			return "message_type";
		}
	}

	enum MESSAGE_FORMAT {
		TEXT("text"),JSON("json");
		private String string;
		private MESSAGE_FORMAT(String string){
			this.string=string;
		}
		public String toString(){
			return string;
		}
		public static MESSAGE_FORMAT get(String string){
			for(MESSAGE_FORMAT type:MESSAGE_FORMAT.values()){
				if(type.name().equalsIgnoreCase(string)){
					return type;
				}
			}
			return null;
		}
		public static String getName(){
			return "message_format";
		}
	}

	enum CONNECTION_TYPE {
		LISTENER("listener"),CONTROLLER("controller"),CONTROLLED("controlled");
		private String string;
		private CONNECTION_TYPE(String string){
			this.string=string;
		}
		public String toString(){
			return string.toLowerCase();
		}
		public static CONNECTION_TYPE get(String string){
			for(CONNECTION_TYPE type:CONNECTION_TYPE.values()){
				if(type.name().equalsIgnoreCase(string)){
					return type;
				}
			}
			return null;
		}
		public static String getName(){
			return "connection_type";
		}
	}

	class SendData {
		private MESSAGE_TYPE messageType=null;
		private MESSAGE_FORMAT messageFormat=null;
		private String data=null;
		SendData(MESSAGE_TYPE messageType,MESSAGE_FORMAT messageFormat,String data){
			this.messageType=messageType;
			this.messageFormat=messageFormat;
			this.data=data;
		}
		SendData(String json){
			HashMap<String,String> map=JSON.decode(json,HashMap.class);
			this.messageType=MESSAGE_TYPE.get(map.get(MESSAGE_TYPE.getName()));
			this.messageFormat=MESSAGE_FORMAT.get(map.get(MESSAGE_FORMAT.getName()));
			this.data=map.get("data");
		}
		public MESSAGE_TYPE getMessageType() {
			return messageType;
		}
		public MESSAGE_FORMAT getMessageFormat() {
			return messageFormat;
		}
		public String getData() {
			return data;
		}
		public String toString(){
			HashMap<String, String> map = new HashMap<String,String>();
			map.put(MESSAGE_TYPE.getName(), messageType.toString());
			map.put(MESSAGE_FORMAT.getName(), messageFormat.toString());
			map.put("data", data);
			return JSON.encode(map);
		}
	}

	private WebSocketServletWithConnectionType wsServletWithConnectionType = null;
	private static Set<WebSocketWithConnectionType> clients = new CopyOnWriteArraySet<WebSocketWithConnectionType>();
	private MessageListener listener = null;
	private CommandLineOption options = null;

	public static void main(String[] args) throws Exception {
		new Core(args);
	}

	public Core(String[] args) throws Exception {
		options=Utility.parseCommandLineOption(args);
		runServer();
	}

	public MessageListener getListener() {
		return listener;
	}

	public void setListener(MessageListener listener) {
		this.listener = listener;
	}

	public void clearListener() {
		this.listener = null;
	}

	void runServer() throws Exception {

		Server server = new Server(options.getPortNumber());

		ResourceHandler resourceHandler = new ResourceHandler();
		String htmlPath = this.getClass().getClassLoader().getResource("html").toExternalForm();
		resourceHandler.setResourceBase(htmlPath);

		wsServletWithConnectionType = new WebSocketServletWithConnectionType();
		ServletHolder wsServletHolder = new ServletHolder(wsServletWithConnectionType);
//		wsServletHolder.setInitParameter("bufferSize", Integer.toString(8192*256,10));
		ServletContextHandler wsServletContextHandler = new ServletContextHandler();
		wsServletContextHandler.addServlet(wsServletHolder, "/ws/*");

		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(new Handler[] {resourceHandler, wsServletContextHandler});
		server.setHandler(handlerList);
		server.start();

	}

	void onMessage(String data) {

		SendData sendData = new SendData(MESSAGE_TYPE.COMMAND,MESSAGE_FORMAT.TEXT,data);
		String sendString = sendData.toString();

		if(listener!=null){
			listener.onMessage(CONNECTION_TYPE.LISTENER, sendData);
		}
		wsServletWithConnectionType.sendMessageForConnectionType(CONNECTION_TYPE.CONTROLLER.toString(), sendString);
		wsServletWithConnectionType.sendMessageForConnectionType(CONNECTION_TYPE.CONTROLLED.toString(), sendString);

	}

	void onMessageFromWebSocket(String connectionType, byte frame, String data) {

		SendData sendData=null;

		try {
			sendData=new SendData(data);
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println(data);
			return;
		}

		CONNECTION_TYPE ctype=CONNECTION_TYPE.get(connectionType);
		MESSAGE_TYPE mtype=sendData.getMessageType();

		if(listener!=null){
			listener.onMessage(ctype, sendData);
		}
		wsServletWithConnectionType.sendMessageForConnectionType(CONNECTION_TYPE.CONTROLLER.toString(), data);
		if(		CONNECTION_TYPE.CONTROLLER.equals(ctype)	&&
				MESSAGE_TYPE.COMMAND.equals(mtype)			&&
				!options.isStopExternal()
		){
			wsServletWithConnectionType.sendMessageForConnectionType(CONNECTION_TYPE.CONTROLLED.toString(), data);
		}

	}

	void onMessageFromWebSocket(String connectionType, byte frame, byte[] data, int offset, int length) {
	}

	class WebSocketServletWithConnectionType extends WebSocketServlet {

		private static final long serialVersionUID = 1L;

		WebSocketServletWithConnectionType() {
			super();
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			getServletContext().getNamedDispatcher("default").forward(request, response);
		}

		@Override
		protected WebSocket doWebSocketConnect(HttpServletRequest request, String connectionType) {
			return new WebSocketWithConnectionType(this, connectionType);
		}

		void onConnect(WebSocketWithConnectionType webSocketWithConnectionType) {
			clients.add(webSocketWithConnectionType);
		}

		void onDisconnect(WebSocketWithConnectionType webSocketWithConnectionType) {
			clients.remove(webSocketWithConnectionType);
		}

		void onMessage(WebSocketWithConnectionType webSocketWithConnectionType, byte frame, String data) {
			Core.this.onMessageFromWebSocket(webSocketWithConnectionType.getConnectionType(), frame, data);
		}

		void onMessage(WebSocketWithConnectionType webSocketWithConnectionType, byte frame, byte[] data, int offset, int length) {
			Core.this.onMessageFromWebSocket(webSocketWithConnectionType.getConnectionType(), frame, data, offset, length);
		}

		void sendMessageAll(String data) {
			for(WebSocketWithConnectionType client : clients){
				client.sendMessage(data);
			}
		}

		void sendMessageForConnectionType(String connectionType, String data) {
			for(WebSocketWithConnectionType client : clients){
				if(Utility.equalsWithNull(connectionType, client.getConnectionType())){
					client.sendMessage(data);
				}
			}
		}

	}

	class WebSocketWithConnectionType implements WebSocket {

		private WebSocketServletWithConnectionType servlet = null;
		private Outbound outbound;
		private String connectionType = null;

		WebSocketWithConnectionType(WebSocketServletWithConnectionType servlet, String connectionType) {
			super();
			this.servlet = servlet;
			this.connectionType = connectionType;
		}

		String getConnectionType() {
			return connectionType;
		}

		boolean sendMessage(String data) {
			if(outbound==null){return false;}
			try {
				outbound.sendMessage(data);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		public void onConnect(Outbound outbound) {
			this.outbound = outbound;
			servlet.onConnect(this);
		}

		@Override
		public void onDisconnect() {
			servlet.onDisconnect(this);
		}

		@Override
		public void onMessage(byte frame, String data) {
			servlet.onMessage(this, frame, data);
		}

		@Override
		public void onMessage(byte frame, byte[] data, int offset, int length) {
			servlet.onMessage(this, frame, data, offset, length);
		}

	}

}
