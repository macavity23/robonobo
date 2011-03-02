 /* 
  *  `gnu.iou.dom' 
  *  Copyright (C) 2006 John Pritchard.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License as
  *  published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
  *  02111-1307 USA
  */

package gnu.iou.dom.impl;

/**
 * <p> Command line pretty printer (basic DOM I/O test).  Run with no
 * args for usage message.  </p>
 * 
 * @see Builder
 * @see Formatter
 * 
 * @author jdp
 */
public class Pretty 
    extends java.lang.Object
{

    public static void usage(){
	System.err.println("Usage");
	System.err.println();
	System.err.println("   gnu.iou.dom.impl.Pretty 'pp' < in.xml > out.xml");
	System.err.println("   gnu.iou.dom.impl.Pretty 'pp'   in.xml   out.xml");
	System.err.println();
	System.err.println("Description");
	System.err.println();
	System.err.println("   A pretty printer, run with command 'pp'.");
	System.err.println();
	System.exit(1);
    }

    public static void main (String[] argv){
	if (null == argv || 1 > argv.length)
	    usage();
	else {
	    int argc = argv.length;
	    String call = argv[0], src = "stdio:in", dst = "stdio:out";
	    java.io.InputStream in = System.in;
	    java.io.OutputStream out = System.out;
	    if (1 < argc){
		String test = argv[1];
		try {
		    java.io.File test_file = new java.io.File(test);
		    in = new java.io.FileInputStream(test_file);
		    src = "file:"+test;
		}
		catch (java.io.IOException iox){
		    System.err.println("Error opening input file '"+test+"'.");
		    System.exit(1);
		}
		if (2 < argc){
		    test = argv[2];
		    try {
			java.io.File test_file = new java.io.File(test);
			out = new java.io.FileOutputStream(test_file);
			dst = "file:"+test;
		    }
		    catch (java.io.IOException iox){
			System.err.println("Error opening output file '"+test+"'.");
			System.exit(1);
		    }
		}
	    }
	    if ("pp".equals(call)){
		Document doc = new Document();
		try {
		    /*
		     * Read
		     */
		    gnu.iou.dom.Builder.Parser.Parse(in,doc,src);
		}
		catch (java.io.IOException iox){
		    System.err.println("Error reading input from '"+src+"': "+iox.getMessage());
		    try {
			in.close();
			out.close();
		    }
		    catch (java.io.IOException ignore){}
		    System.exit(1);
		}
		catch (org.xml.sax.SAXException sax){
		    System.err.println("Error parsing input from '"+src+"': "+sax.getMessage());
		    try {
			in.close();
			out.close();
		    }
		    catch (java.io.IOException ignore){}
		    System.exit(1);
		}
		try {
		    /*
		     * Write
		     */
		    gnu.iou.dom.Formatter writer = new Formatter.Stream(out);
		    writer.write(doc.getDocumentElement());
		}
		catch (java.io.IOException iox){
		    System.err.println("Error writing output to '"+dst+"': "+iox.getMessage());
		    try {
			in.close();
			out.close();
		    }
		    catch (java.io.IOException ignore){}
		    System.exit(1);
		}
		finally {
		    try {
			out.flush();
			in.close();
			out.close();
		    }
		    catch (java.io.IOException ignore){
		    }
		}
		System.exit(0);
	    }
	    else
		usage();
	}
    }

}
