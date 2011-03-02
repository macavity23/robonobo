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

package gnu.iou.dom;

/**
 * <p> DOM builder.</p>
 * 
 * @see gnu.iou.dom.Document#builderEnterSax()
 * @see gnu.iou.dom.Document#builder(boolean)
 * @author jdp
 */
public interface Builder {

    /**
     * <p> An {@link Element} implementing this interface will receive
     * an event on the completion of the construction of itself and
     * its children. </p>
     */
    public interface Post {

	/**
	 * Called on the completion of the construction of an element
	 * and its children.  An exception thrown from this method
	 * will halt the parsing and building process.
	 */
	public void buildpost();
    }

    /**
     * <p> Convenience tools for driving XML parsing using the built-
     * in JAX parser.  Sets- up namespace awareness (etc).  </p>
     * 
     * @see Formatter$Writer
     */
    public static class Parser
	extends org.xml.sax.helpers.DefaultHandler
    {
	private static javax.xml.parsers.SAXParserFactory factory;
	private static javax.xml.parsers.SAXParserFactory SaxFactory(){
	    if (null == factory){
		try {
		    factory = javax.xml.parsers.SAXParserFactory.newInstance();
		    synchronized(factory){
			factory.setNamespaceAware(true);
			factory.setFeature("http://xml.org/sax/features/namespaces",true);
			factory.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
		    }
		}
		catch (javax.xml.parsers.ParserConfigurationException conf){
		    java.lang.RuntimeException rex = new gnu.iou.dom.Error.State("Error configuring JAXP");
		    rex.initCause(conf);
		    throw rex;
		}
		catch (org.xml.sax.SAXException sax){
		    java.lang.RuntimeException rex = new gnu.iou.dom.Error.State("Error configuring SAX");
		    rex.initCause(sax);
		    throw rex;
		}
	    }
	    return factory;
	}
	public final static javax.xml.parsers.SAXParser New(){
	    try {
		return SaxFactory().newSAXParser();
	    }
	    catch (javax.xml.parsers.ParserConfigurationException conf){
		java.lang.RuntimeException rex = new gnu.iou.dom.Error.State("Error configuring JAXP");
		rex.initCause(conf);
		throw rex;
	    }
	    catch (org.xml.sax.SAXException sax){
		java.lang.RuntimeException rex = new gnu.iou.dom.Error.State("Error configuring SAX");
		rex.initCause(sax);
		throw rex;
	    }
	}
	public final static void Parse(java.io.InputStream stream, 
				       gnu.iou.dom.Document doc,
				       java.lang.String src)
	    throws java.io.IOException,
		   org.xml.sax.SAXException
	{
	    org.xml.sax.ContentHandler handler = doc.builderEnterSax();
	    Parse(stream,handler,src);
	}
	public final static void Parse(java.io.InputStream stream, 
				       org.xml.sax.ContentHandler handler,
				       java.lang.String src)
	    throws java.io.IOException,
		   org.xml.sax.SAXException
	{
	    Parse(New(),stream,handler,src);
	}
	public final static void Parse(javax.xml.parsers.SAXParser parser,
				       java.io.InputStream stream, 
				       org.xml.sax.ContentHandler handler, 
				       java.lang.String src)
	    throws java.io.IOException,
		   org.xml.sax.SAXException
	{
	    org.xml.sax.XMLReader reader = parser.getXMLReader();
	    //
            reader.setContentHandler(handler);
	    if (handler instanceof org.xml.sax.EntityResolver)
		reader.setEntityResolver( (org.xml.sax.EntityResolver)handler);
	    if (handler instanceof org.xml.sax.ErrorHandler)
		reader.setErrorHandler( (org.xml.sax.ErrorHandler)handler);
	    if (handler instanceof org.xml.sax.DTDHandler)
		reader.setDTDHandler( (org.xml.sax.DTDHandler)handler);
	    if (handler instanceof org.xml.sax.ext.LexicalHandler)
		reader.setProperty("http://xml.org/sax/properties/lexical-handler",handler);
	    //
	    org.xml.sax.InputSource ins = new org.xml.sax.InputSource(stream);
	    ins.setSystemId(src);
	    reader.parse(ins);
	}
    }

    /**
     * <p> SAX parse events for builder </p>
     * 
     * @author jdp
     */
    public interface Sax 
	extends Builder,
		org.xml.sax.ContentHandler,
		org.xml.sax.ext.LexicalHandler 
    {

	public void destroy();

	public org.xml.sax.Locator locator();

	public org.xml.sax.Locator locator(boolean exc);

    }


    /**
     * <p> Nodes (including Documents) implementing this interface can
     * influence the binding of their children.  Binding occurs in
     * node instantiation, mapping a node namespace and name onto a
     * java class, and can be manipulated via these interfaces in DOM
     * building from parsing. </p>
     * 
     * @see Document#create(short,java.lang.String,java.lang.String)
     * 
     * @author jdp
     */
    public interface Binding
	extends org.w3c.dom.Node 
    {
	/**
	 * <p> Nodes implementing this interface can map bindings for
	 * nodes among their children.  The map binding has precedence
	 * over the override, normal and special bindings. </p>
	 * 
	 * @author jdp
	 */
	public interface Map extends Binding {
	    /**
	     * @param name Node name as
	     * projected from name and namespace
	     * @return Classname may be null for no map
	     */
	    public String map(Name name);
	}
	/**
	 * <p> Nodes implementing this interface can override the
	 * binding of their children.  The override binding has
	 * precedence over the normal and special binding, and is
	 * preceeded by the map binding.  </p>
	 * 
	 * @author jdp
	 */
	public interface Override extends Binding {
	    /**
	     * @return Classname prefix, eg,
	     * <code>"package.name."</code> or
	     * <code>"package.ClassName$"</code>.
	     */
	    public String overridePrefix();
	}
	/**
	 * <p> Nodes implementing this interface can provide special
	 * bindings for unbound nodes among their children.  The
	 * special binding is preceeded by the map, override and
	 * normal bindings.  </p>
	 * 
	 * @author jdp
	 */
	public interface Special extends Binding {
	    /**
	     * @return Classname prefix, eg,
	     * <code>"package.name."</code> or
	     * <code>"package.ClassName$"</code>.
	     */
	    public String specialPrefix();
	}
    }


    public Builder cloneBuilder(Document newp);

    public Document document();

    public void destroy();

    public Element buildCurrent(boolean exc);

    public Node buildLast();

    public Node buildParent();

    public Element push(Element elem);

    public Element pop(String ns, String qn);

    public Binding lastBinding();

    public Binding prevBinding();

    public Binding popBinding();

}
