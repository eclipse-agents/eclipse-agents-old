
How to install the Eclipse MCP feature:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. Place the <patch>.zip in a reachable directory.  
 For example to:
 Windows: c:\temp\<patch>.zip
 Linux and UNIX: /temp/<patch>.zip

2. Open the Eclipse instance you'd like to patch.  
2.a Use Eclipse for 4.31 or newer
2.b Tested with 2025-09 Eclipse for Java and Web Developers
   
3. Navigate to Help > Install New Software...

4. Click "Add..." > "Archive..." and navigate to where the <patch>.zip was placed, then click "Open"

5. Toggle the checkbox for the patch that shows up in the view

6. Click "Next" until "Finish"

7. Click "OK" to the security warning and allow the patch to install

8. When prompted to restart, click "Yes"

How to uninstall the test fix:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
1. Navigate to Help > About IBM Developer for z/OS

2. Click on "Installation Details"

3. Scroll through the Installed Software list to find the patch that was installed; double check it's the proper feature

4. Select the patch and click "Uninstall..."

5. Click "Finish" in the Uninstall Details panel if it looks correct

6. When prompted to restart, click "Yes"