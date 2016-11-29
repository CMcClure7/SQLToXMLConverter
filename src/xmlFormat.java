/*
 * xmlFormat class
 * 
 * xmlFormat class formats the SQL data into proper XML output
 * Class is passed a ResultSet object from a selection query
 * Class is passed an ArrayList of Attribute objects from the SQLParser class
 * XML output is displayed to the console
 * 
 */

//import Java libraries
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class xmlFormat {
	
	//for testing purposes
	public static void main(String[] args) {
		ArrayList<Attribute> testList = new ArrayList<Attribute>();
   		
		Attribute temp = new Attribute();
		temp.name = "id";
		temp.tableName = "person";
			
		Attribute temp2 = new Attribute();
		Group gp = new Group();
		gp.name = "Tacos";
		gp.compTo=temp;
		temp2.compFlag=true;
		temp2.group=gp;
		temp2.name = "name";
		temp2.tableName = "person";
		
		Attribute temp3 = new Attribute();
		temp3.name = "ids";
		temp3.tableName = "PART";
		gp.compTo = null;
		gp.name = "";
		temp3.group=gp;
		
		Attribute temp4 =new Attribute();
		temp4.name = "CITY";
		temp4.tableName="PART";
		temp4.group = gp;
		
			
		testList.add(temp);
		testList.add(temp2);
		//testList.add(temp3);
		//testList.add(temp4);
	   		
		Database parser = new Database("jdbc:sqlite:sample.db","","");
		

		ResultSet adsf=	parser.query("SELECT * FROM person");
		
		XML(adsf,testList);
		parser.close();
	}
	
	static ResultSet rSet;					// make ResultSet global
	static ArrayList<Attribute> aList;		// make ArrayList global
	static ArrayList<String> dList;
	static ArrayList<String> dtdList;
	static ArrayList<String> xsdList;
	
	public static void XML(ResultSet ret, ArrayList<Attribute> lst) {
		
		rSet = ret;								// set the ResultSet to the global variable
		aList =lst;								// set the ArrayList to the global variable
		int colCount = 0; 						// used to keep track of the current column
		int groupCount = 0;						// used to keep track of the group scope
		int length = aList.size();				// used to track the length of the ArrayList
		int tabCnt = 1;							// used to format tab whitespace for console output
		int tagCnt = 0;							// used to track the <A Record> tags
		boolean groupFlag = false;				// used to keep track of grouping
		boolean compressionFlag = false;		// used to keep track of compression
		String[] gNames = new String[5];		// used to keep track of the name of the groups
		String tempGroup = "";					// used to track the name of the current group
		String tempAtname = "";					// used as a temporary holder for the Attribute object
		String prevAtName = "";					// used to track the previous Attribute object
		
		Scanner input = new Scanner(System.in);
		String selection = "";
		
		while (selection != "1" || selection != "2" || selection != "3")
		{
			System.out.println("\n-----------XML MENU------------");
			
			System.out.println("1. Display XML to the console.");
			System.out.println("2. Save XML to a file.");
			System.out.println("3. Do both option 1 and option 2.");
				
			System.out.println("Enter your selection:");
			selection = input.nextLine();

			if (selection != "1" || selection != "2" || selection != "3")
			{
				System.out.println("Invalid selection.  Try again.");
			}
		}
		
		try {		// error handling for SQLException
			
			dList.add("<?xml version ='1.0'?>");
			// display XML output to the console

			//System.out.println("<?xml version ='1.0'?>");
			//System.out.println("<This Query>");

			System.out.println("<?xml version ='1.0'?>");
			
			pDTD(aList);
			
			System.out.println("<This Query>");

			
			while (rSet.next()) {		// move ResultSet to the next data row
				
				if (compressionFlag == false) {		// checks if data is to be compressed
					
					dList.add("<A Record>");
					//System.out.println("<A Record>");	// display XML output to the console
					
					tagCnt++;		// increase the counter for the <A Record> tag
				}
				
				// initialize the variables for the current ResultSet data row
				String tabName = aList.get(colCount).tableName;
				String alias = aList.get(colCount).alias;
				String gName ="";
				
				colLoop: while (colCount < length) {		// move the ResultSet to the next data column
					
					tabName = aList.get(colCount).tableName;			// grabs the current Attribute's table
					alias = aList.get(colCount).alias;					// grabs the current Attribute's alias
					
					if (aList.get(colCount).compFlag == true) {

						if (compressionFlag == false)				// saves previous Attribute's name
							tempGroup = prevAtName;
							
						compressionFlag = true;						// flag to track if compression is processing
						tempAtname = aList.get(colCount-1).name;	// stores the previous column's name
					}
					
					else if (compressionFlag == true  && tempAtname.equals(aList.get(colCount).name)) {

						String f = aList.get(colCount).name;		// stores the current Attribute's name
						String d = rSet.getString(f);				// stores the current ResultSet's name
						
						if (tempGroup.equals(d)) {		// checks if the current group equals the previous group
						
							colCount++;					// increase the column counter for the inner loop
							continue colLoop;			// proceed to the next column
						}
						
						compressionFlag = false;		// mark compression flag; compression completed
					}
					
					if (aList.get(colCount).group != null && groupFlag == false) {		// checks Attribute for a group 

						groupCount++;										// increment the group counter
						groupFlag = true;									// flag to track grouping
						gName = aList.get(colCount).group.name;				// grabs Attribute's group name
						gNames[groupCount] = gName;							// saves the group name to an array
																			// used to close group tags

						dList.add(String.format("%" + (4 * tabCnt) + "s", " ") + "<" + gName + ">");
						// display XML output to the console
						//System.out.print(String.format("%" + (4 * tabCnt) + "s", " "));
						//System.out.println("<" + gName + ">");
						
						tabCnt++;		// increment the tab counter for whitespace format
					}
					
					else if (groupFlag == true && ((aList.get(colCount).group.name==null) || aList.get(colCount).group.name != gName)) {	// entering nested groups
						
						groupCount++;														// increment the group counter
						groupFlag = true;													// flag to track grouping
						gName = aList.get(colCount).group.name;								// grabs Attribute's group name
						gNames[groupCount] = gName;											// saves the group name to an array
																							// used to close group tags
						
						dList.add(String.format("%" + (4 * tabCnt) + "s", " ") + "<" + gName + ">");
						// display XML output to the console
						//System.out.print(String.format("%" + (4 * tabCnt) + "s", " "));
						//System.out.println("<" + gName + ">");
						
						tabCnt++;		// increment the tab counter for whitespace format
					}
					
					String colName = aList.get(colCount).name;			// grab the current Attribute's name
					
					if (alias == null)				// check the current group for an alias
						alias = colName;			// set alias to column name if alias does not exist
					
					prevAtName = rSet.getString(colName);
							
					dList.add(String.format("%" + (4 * tabCnt) + "s", " ") + 
							"<" + alias.toUpperCase() + " table=\""+ tabName + "\" name=\"" + colName +"\">" +
							prevAtName + "</" + alias.toUpperCase() + ">");
					
					// display XML output to the console
					//System.out.print(String.format("%" + (4 * tabCnt) + "s", " "));
					//System.out.print("<" + alias.toUpperCase() + " table=\""+ tabName + "\" name=\"" + colName +"\">");
					//System.out.print(prevAtName = rSet.getString(colName));
					//System.out.println("</" + alias.toUpperCase() + ">");
					
					colCount++;			// increase the column counter for the inner loop
				}	// end column while loop
				
				if (groupFlag == true) {		// checks for grouping
					
					while (groupCount > 0) {	// loop through grouping to display group closing tags
						
						gName = gNames[groupCount];		// set the current group tag to the tag stored in the array		
						
						tabCnt--;		// decrement the tab counter for whitespace format
						
						dList.add(String.format("%" + (4 * tabCnt) + "s", " ") + "</" + gName + ">");
						// display XML output to the console
						//System.out.print(String.format("%" + (4 * tabCnt) + "s", " "));
						//System.out.println("</" + gName + ">");
						
						groupCount--;		// decrement the group counter
					}
					
					groupFlag = false;		// reset the flag for grouping
				}
				
				else {
					
					dList.add("</A Record>");
					// display XML output to the console
					//System.out.println("</A Record>");
					
					tagCnt--;		// decrement the tag counter
				}
				
				colCount = 0;		// reset the column counter for the next ResultSet data row
			}	// end row while loop
			
			while (tagCnt > 0) {	// checks that all <A Record> tags are closed
				
				dList.add("</A Record>");
				// display XML output to the console
				//System.out.println("</A Record>");
				
				tagCnt--;		// decrement the tag counter
			}
			
			dList.add("</This Query>");
			// display XML output to the console
			// System.out.println("</This Query>");
			
			
			DTD(rSet,aList);
		} catch (SQLException e) {		// catch SQLException
			e.printStackTrace();		// display stack trace for thrown exception
		}
		
		if (selection.equals("1"))
		{
			Parray(dList);
		}
		
		else if (selection.equals("2"))
		{
			Sarray(dList);
		}
		
		else
		{
			Parray(dList);
			Sarray(dList);
		}
		
		dtdList = DTD(rSet, aList);
		xsdList = XSD(rSet, aList);
		
		String selXSD = "";
		
		while (selXSD != "1" || selXSD != "2" || selXSD != "3")
		{
			System.out.println("\n-----------XSD/DTD MENU------------");
			
			System.out.println("1. Display XSD and DTD to the console.");
			System.out.println("2. Save XSD and DTD to seperate files.");
			System.out.println("3. Do both option 1 and option 2.");
				
			System.out.println("Enter your selection:");
			selXSD = input.nextLine();

			if (selXSD != "1" || selXSD != "2" || selXSD != "3")
			{
				System.out.println("Invalid selection.  Try again.");
			}
		}
		
		if (selXSD.equals("1"))
		{
			printDTD(dtdList);
			printXSD(xsdList);
		}
		
		else if (selXSD.equals("2"))
		{
			saveDTD(dtdList);
			saveXSD(xsdList);
		}
		
		else
		{
			printDTD(dtdList);
			printXSD(xsdList);
			saveDTD(dtdList);
			saveXSD(xsdList);
	}
	
	public static ArrayList DTD(ResultSet ret, ArrayList<Attribute> lst){		//when this function is called it will print the DTD Information
		
		int counter = 0;
		int STnum = 0;
		int EDnum = 0;
		int length = lst.size();
		boolean flag= false;
		String asdf ="";
	System.out.println(" \n \n \n");
	System.out.println("<?xml version ='1.0'?>");
	
	
	while(!flag){
		asdf =lst.get(counter).tableName;
	System.out.println("<!DOCTYPE "+ asdf+ " [ \n" );
	System.out.print("<!ELEMENT " + asdf+ " (" ) ;
	STnum = counter ;
	System.out.print(lst.get(counter++).name );
	
	 while((counter< length) &&  asdf.equals(lst.get(counter).tableName))
	{
		System.out.print(", " + lst.get(counter++).name);
	}
	System.out.print(")> \n \n");
	EDnum = counter;
	counter = STnum;
	
	while(counter< (EDnum)){
	System.out.println("<!ELEMENT  " + lst.get(counter++).name + " (#PCDATA)> \n");
	}
	
	System.out.println("]> \n");
	
	if(flag == false && ((counter) == length)){
		flag =true;
		counter++;
	}
	
	}
	
	}
	public static void pDTD(ArrayList<Attribute> lst){
	int counter = 0;
	int length = lst.size();
String d = "";
		 while(counter< length)
		 {
			 if(!lst.get(counter).tableName.equals(d))
				 {
				 d = lst.get(counter).tableName;
				 System.out.println("<!DOCTYPE " + d+ " INFORMTATION \""+ d + "_Info.dtd\">");
				
				 }
			counter++;
			}
		 

	}
	
	public static ArrayList XSD (ResultSet ret, ArrayList<Attribute> lst) {		//when this function is called it will print XSD 
		
		int counter = 0;
		int tabCnt = 1;
		String tableName = lst.get(counter).tableName;
		//ResultSetMetaData rsmd = ret.getMetaData();
		
		System.out.println("<?xml version=\"1.0\"?>");
		
		while (counter < lst.size())
		{
			tableName = lst.get(counter).tableName;
			
			System.out.println("<schema xmlns:xsd=\"" + tableName + "XSDnew\" elementFormDefault=\"qualified\" " +
					"attributeFormDefault=\"qualified\">");
			
			System.out.print(String.format("%" + (4 * tabCnt) + "s", " "));
			System.out.println("<xsd:complexType name=\"" + tableName + "\">");
			tabCnt++;
			
			while (counter < lst.size() && tableName.equals((lst.get(counter).tableName)))
			{
				System.out.print(String.format("%" + (4 * tabCnt) + "s", " "));
				System.out.println("<xsd:element name=\"" + lst.get(counter).name + "\" type=\"xsd:" +
						"string\" maxOccurs=\"1\" minOccurs=\"1\" />");
						//rsmd.getColumnType(counter) + "\" maxOccurs=\"1\" minOccurs=\"1\" />");
				
				counter++;
			}
			
			tabCnt--;
			
			System.out.print(String.format("%" + (4 * tabCnt) + "s", " "));
			System.out.println("</xsd:complexType>");
			System.out.println("</schema>");
		}
		
		return xsdList;
	}
	
	public static void Parray(ArrayList<String> alist){			//prints the array of strings 
		int length = alist.size();
		int i = 0;
		
		while (i< length){						//for the length of the array of strings 
			System.out.println(alist.get(i));
			i++;
		}
	}
	public static void Sarray(ArrayList<String> amsd){			//saves the array of Strings
		int i = 0;
		int length = amsd.size();
		try(  PrintWriter mout = new PrintWriter( "XML.txt","UTF-8" )  ){	//creates file xml.txt will overwrite if already exist
			while (i< length){
				mout.println(amsd.get(i));
				i++;
			}
			
			mout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}