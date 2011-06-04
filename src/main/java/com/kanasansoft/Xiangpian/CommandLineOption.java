package com.kanasansoft.Xiangpian;

import uk.co.flamingpenguin.jewel.cli.*;

interface CommandLineOption {

	@Option(shortName = "u", longName = "user-interface", defaultValue="gui", pattern="^(g|c)(ui)?$", description="select user interface [g,gui,c,cui]")
	String getUserInterfaceType();

	@Option(shortName="p",longName="port",defaultValue="40320",description="specify port number")
	int getPortNumber();

    @Option(shortName="s", longName="stop-external", description="stop execute external command from websocket")
    boolean isStopExternal();

    @Option(helpRequest = true, shortName = "h", description = "display help")
	boolean getHelp();

}
