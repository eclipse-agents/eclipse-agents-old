/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.acp;

public class AcpClient implements IAcpClient {

	private ContextStore<IAcpServer> store;

	public AcpClient(ContextStore<IAcpServer> store) {
		this.store = store;
	}

	@Override
	public String helloWorld(String firstName, String lastName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getDebuggerRunResult(String notice) {
		// TODO Auto-generated method stub
		
	}

	
}
