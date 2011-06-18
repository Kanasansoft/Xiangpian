package com.kanasansoft.Xiangpian;

import com.kanasansoft.Xiangpian.Core.CONNECTION_TYPE;
import com.kanasansoft.Xiangpian.Core.SendData;

public interface MessageListener {
	void onMessage(CONNECTION_TYPE connectionType, SendData data);
}
