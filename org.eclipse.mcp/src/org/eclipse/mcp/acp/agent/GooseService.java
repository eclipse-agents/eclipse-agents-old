package org.eclipse.mcp.acp.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GooseService extends AbstractService {

	
	public GooseService() {
		
	}

	@Override
	public String getName() {
		return "Goose";
	}

	@Override
	public Process createProcess() throws IOException {
		String goose = "/Users/jflicke/.local/bin/goose"; 
	
		List<String> commandAndArgs = new ArrayList<String>();
		commandAndArgs.add(goose);
		commandAndArgs.add("acp");
		
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		return pb.start();
		
	}

}
