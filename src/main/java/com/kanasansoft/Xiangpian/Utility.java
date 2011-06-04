package com.kanasansoft.Xiangpian;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class Utility {

	static boolean equalsWithNull(Object a, Object b) {
		if(a==null&&b==null){
			return true;
		}else if(a==null||b==null){
			return false;
		}else if(a.equals(b)){
			return true;
		}
		return false;
	}

	static CommandLineOption parseCommandLineOption(String[] args){
		CommandLineOption result = null;
		try {
			result = CliFactory.parseArguments(CommandLineOption.class, args);
		} catch (ArgumentValidationException e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(1);
		}
		return result;
	}

}
