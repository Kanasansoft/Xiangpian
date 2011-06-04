package com.kanasansoft.Xiangpian;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class Core {

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

	void onMessage(String connectionType, byte frame, String data) {
		if(listener!=null){
			listener.onMessage(connectionType, frame, data);
		}
		wsServletWithConnectionType.sendMessageForConnectionType("server_side_command", frame, data);
		wsServletWithConnectionType.sendMessageForConnectionType("client_side", frame, data);
	}

	void onMessage(String connectionType, byte frame, byte[] data, int offset, int length) {
		if(listener!=null){
			listener.onMessage(connectionType, frame, data, offset, length);
		}
		wsServletWithConnectionType.sendMessageForConnectionType("server_side_command", frame, data, offset, length);
		wsServletWithConnectionType.sendMessageForConnectionType("client_side", frame, data, offset, length);
	}

	void onMessageFromWebSocket(String connectionType, byte frame, String data) {
		if(listener!=null){
			listener.onMessage(connectionType, frame, data);
		}
		if("client_side".equals(connectionType)){
			wsServletWithConnectionType.sendMessageForConnectionType("server_side_result", frame, data);
		}
		if("server_side_command".equals(connectionType)&&!options.isStopExternal()){
			wsServletWithConnectionType.sendMessageForConnectionType("server_side_command", frame, data);
			wsServletWithConnectionType.sendMessageForConnectionType("client_side", frame, data);
		}
	}

	void onMessageFromWebSocket(String connectionType, byte frame, byte[] data, int offset, int length) {
		if(listener!=null){
			listener.onMessage(connectionType, frame, data, offset, length);
		}
		if("client_side".equals(connectionType)){
			wsServletWithConnectionType.sendMessageForConnectionType("server_side", frame, data, offset, length);
		}
		if("server_side".equals(connectionType)&&!options.isStopExternal()){
			wsServletWithConnectionType.sendMessageForConnectionType("server_side", frame, data, offset, length);
			wsServletWithConnectionType.sendMessageForConnectionType("client_side", frame, data, offset, length);
		}
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

		void sendMessageAll(byte frame, byte[] data) {
			for(WebSocketWithConnectionType client : clients){
				client.sendMessage(frame, data);
			}
		}

		void sendMessageAll(byte frame, byte[] data, int offset, int length) {
			for(WebSocketWithConnectionType client : clients){
				client.sendMessage(frame, data, offset, length);
			}
		}

		void sendMessageAll(byte frame, String data) {
			for(WebSocketWithConnectionType client : clients){
				client.sendMessage(frame, data);
			}
		}

		void sendMessageAll(String data) {
			for(WebSocketWithConnectionType client : clients){
				client.sendMessage(data);
			}
		}

		void sendMessageForConnectionType(String connectionType, byte frame, byte[] data) {
			for(WebSocketWithConnectionType client : clients){
				if(Utility.equalsWithNull(connectionType, client.getConnectionType())){
					client.sendMessage(frame, data);
				}
			}
		}

		void sendMessageForConnectionType(String connectionType, byte frame, byte[] data, int offset, int length) {
			for(WebSocketWithConnectionType client : clients){
				if(Utility.equalsWithNull(connectionType, client.getConnectionType())){
					client.sendMessage(frame, data, offset, length);
				}
			}
		}

		void sendMessageForConnectionType(String connectionType, byte frame, String data) {
			for(WebSocketWithConnectionType client : clients){
				if(Utility.equalsWithNull(connectionType, client.getConnectionType())){
					client.sendMessage(frame, data);
				}
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

		boolean sendMessage(byte frame, byte[] data) {
			if(outbound==null){return false;}
			try {
				outbound.sendMessage(frame, data);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		boolean sendMessage(byte frame, byte[] data, int offset, int length) {
			if(outbound==null){return false;}
			try {
				outbound.sendMessage(frame, data, offset, length);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		boolean sendMessage(byte frame, String data) {
			if(outbound==null){return false;}
			try {
				outbound.sendMessage(frame, data);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
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
