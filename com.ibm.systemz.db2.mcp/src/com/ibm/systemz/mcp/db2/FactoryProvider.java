package com.ibm.systemz.mcp.db2;

import org.eclipse.mcp.IFactoryProvider;
import org.eclipse.mcp.IMCPServices;
import org.eclipse.mcp.resource.IResourceTemplate;


public class FactoryProvider implements IFactoryProvider {

	
	public FactoryProvider() {}

	@Override
	public IResourceTemplate<?, ?>[] createResourceTemplates() {
		return new IResourceTemplate[0];
	}

	@Override
	public Object[] getAnnotatedObjects() {
		return new Object[] {
			new Tools()
		};
	}


	@Override
	public void initialize(IMCPServices services) {
	}

}
