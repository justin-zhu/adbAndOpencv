package com.justin.utils;

import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

/**
 * 重写输出流 并接管System.out.print重定向到JtextArea控件中
 * 
 * @author Administrator
 *
 */
public class MPrintStream extends PrintStream {

	private JTextArea text;

	Logger logger = Logger.getLogger(getClass());

	public MPrintStream(OutputStream out, JTextArea text) {
		super(out);
		this.text = text;
	}

	/**
	 * 在这里重截,所有的打印方法都要调用的方法
	 */
	public void write(byte[] buf, int off, int len) {
		String message = new String(buf, off, len);
		logger.info(message);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				text.append(message);
			}
		});

	}
}