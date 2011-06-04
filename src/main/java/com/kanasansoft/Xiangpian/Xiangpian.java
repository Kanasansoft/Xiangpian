package com.kanasansoft.Xiangpian;

public class Xiangpian {

	private CommandLineOption options = null;

	public static void main(String[] args) throws Exception{
		new Xiangpian(args);
	}
	public Xiangpian(String[] args) throws Exception {

		super();

		options=Utility.parseCommandLineOption(args);

		String userInterfaceType = options.getUserInterfaceType();

		if("c".equals(userInterfaceType)||"cui".equals(userInterfaceType)){
			new CUI(args);
		}else if("g".equals(userInterfaceType)||"gui".equals(userInterfaceType)){
			new GUI(args);
		}
	}

}
