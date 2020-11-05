package com.justin.mavenTest;

import java.io.File;
import java.util.List;

import com.justin.utils.Tools;

import cn.hutool.core.util.RuntimeUtil;

public class JavaRunPyton {	
	
	static File file;
	static {
		file = new File("");
	}

	public  void executeSend(List<String> list) {
		Tools.println(list.toString());			
		String commonds = "python " + getPy() + " " + list.toString();
		String string = RuntimeUtil.execForStr(commonds);
		Tools.println(string);
	}
	public String getPy() {			
		return file.getAbsolutePath()+"\\CAN.py";
	}
}
