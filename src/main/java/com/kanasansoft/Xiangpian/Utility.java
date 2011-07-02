package com.kanasansoft.Xiangpian;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

class Utility {

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

	static String joinString(String[] strs){
		return joinString(strs,",");
	}

	static String joinString(List<String> strs){
		return joinString(strs,",");
	}

	static String joinString(String[] strs,String separator){
		return joinString(Arrays.asList(strs),",");
	}

	static String joinString(List<String> strs,String separator){
		StringBuffer buffer = new StringBuffer();
		Iterator<String> iterator = strs.iterator();
		if(iterator.hasNext()){
			buffer.append(iterator.next());
		}
		while (iterator.hasNext()) {
			buffer.append(separator);
			buffer.append(iterator.next());
		}
		return buffer.toString();
	}

}
