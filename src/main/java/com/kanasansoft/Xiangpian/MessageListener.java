package com.kanasansoft.Xiangpian;

public interface MessageListener {
	void onMessage(String connectionType, byte frame, String data);
	void onMessage(String connectionType, byte frame, byte[] data, int offset, int length);
}
