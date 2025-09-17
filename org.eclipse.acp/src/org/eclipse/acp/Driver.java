package org.eclipse.acp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Driver {

	public static void main(String[] args) throws IOException {
		List<String> commandAndArgs = new ArrayList<String>();
		commandAndArgs.add("gemini");
		commandAndArgs.add("--experimental-acp");
		
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		Process agentProcess = pb.start();
		InputStream in = agentProcess.getInputStream();
		OutputStream out = agentProcess.getOutputStream();
		
		

		agentProcess.onExit().thenRun(new Runnable() {
			@Override
			public void run() {
				int exitValue = agentProcess.exitValue();
				String output = null;
				String errorString = null;

				System.out.println("Gemini Exit:" + exitValue);
			}
		});;
	}

}
