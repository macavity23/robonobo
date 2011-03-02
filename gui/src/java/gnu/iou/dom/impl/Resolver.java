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
 * <p> Simple resolver. 
 * </p>
 * 
 * 
 * @author jdp
 */
public class Resolver 
    extends java.lang.Object
    implements gnu.iou.dom.io.Resolver.WS
{

    public static class Source
	extends java.lang.Object
	implements gnu.iou.dom.io.Source.Byte,
		   gnu.iou.dom.io.Target.Byte,
		   gnu.iou.dom.io.Resolver.WS
    {

	private Resolver resolver;

	private gnu.iou.uri source;

	public Source(Resolver resolver, gnu.iou.uri src){
	    super();
	    if (null == resolver)
		throw new gnu.iou.dom.Error.Argument();
	    else
		this.resolver = resolver;
	    if (null == src)
		throw new gnu.iou.dom.Error.Argument();
	    else
		this.source = src;
	}

	public void destroy(){
	    this.resolver = null;
	    this.source = null;
	}
	public gnu.iou.dom.Document get(gnu.iou.uri uri)
	    throws java.io.IOException
	{
	    return this.resolver.get(uri);
	}
	public void put(gnu.iou.uri uri, gnu.iou.dom.Document doc)
	    throws java.io.IOException
	{
	    this.resolver.put(uri,doc);
	}
	public gnu.iou.dom.Document post(gnu.iou.uri uri, gnu.iou.dom.Document doc)
	    throws java.io.IOException
	{
	    return this.resolver.post(uri,doc);
	}
	public String getSystemIdSource(){
	    return this.source.toString();
	}
	public gnu.iou.uri getSystemIdSource2(){
	    return this.source;
	}
	public String getSystemIdTarget(){
	    return this.source.toString();
	}
	public gnu.iou.uri getSystemIdTarget2(){
	    return this.source;
	}
	public String toString(){
	    return this.source.toString();
	}

    }

    /**
     * <p> Implementors are used by {@link
     * Resolver#newConnection(gnu.iou.uri)}. </p>
     */
    public interface Connection {

	public final static int GET  = 1;
	public final static int POST = 2;
	public final static int PUT  = 3;

	/**
	 * <p> java.net.URLConnection </p>
	 */
	public static class Janet
	    extends java.lang.Object
	    implements Connection
	{
	    protected int method;

	    protected gnu.iou.uri uri;

	    protected java.net.URL url;

	    protected java.net.URLConnection connection;

	    public Janet(gnu.iou.uri uri, int method)
		throws java.io.IOException
	    {
		super();
		if (null != uri){
		    if (uri.isAbsolute()){
			this.uri = uri;
			switch(method){
			case GET:
			case POST:
			case PUT:
			    this.method = method;
			    try {
				this.url = new java.net.URL(uri.toString());
				this.connection = this.url.openConnection();
				if (this.connection instanceof java.net.HttpURLConnection){
				    java.net.HttpURLConnection htp_connection = (java.net.HttpURLConnection)this.connection;
				    switch(method){
				    case GET:
					htp_connection.setRequestMethod("GET");
					return;
				    case POST:
					htp_connection.setRequestMethod("POST");
					return;
				    case PUT:
					htp_connection.setRequestMethod("PUT");
					return;
				    default:
					throw new gnu.iou.dom.Error.Bug();
				    }
				}
				else
				    return;
			    }
			    catch (java.net.MalformedURLException mal){
				throw new gnu.iou.dom.Error.Argument(mal,"Malformed URL '"+uri.toString()+"'.");
			    }
			    //(break);//(not-reached)
			default:
			    throw new gnu.iou.dom.Error.Argument("Unrecognized connection method '"+String.valueOf(method)+"'.");
			}
		    }
		    else
			throw new gnu.iou.dom.Error.Argument("URI has no protocol '"+uri.toString()+"'.");
		}
		else
		    throw new gnu.iou.dom.Error.Argument("URI is null.");
	    }
	    public gnu.iou.uri getUri(){
		return this.uri;
	    }

	    public int getMethod(){
		return this.method;
	    }
	    public java.io.InputStream getInputStream() 
		throws java.io.IOException
	    {
		return this.connection.getInputStream();
	    }
	    public java.io.OutputStream getOutputStream() 
		throws java.io.IOException
	    {
		return this.connection.getOutputStream();
	    }
	    public void destroy(){
		this.method = 0;
		this.uri = null;
		this.url = null;
		this.connection = null;
	    }
	}


	public gnu.iou.uri getUri();

	public int getMethod();

	public java.io.InputStream getInputStream() throws java.io.IOException;

	public java.io.OutputStream getOutputStream() throws java.io.IOException;

	/**
	 * <p> The implementor should never throw an exception. </p>
	 */
	public void destroy();
    }


    protected gnu.iou.objmap uri2connection = new gnu.iou.objmap();

    public Resolver(){
	super();
    }

    protected Connection newConnection(gnu.iou.uri uri, int method)
	throws java.io.IOException
    {
	return new Connection.Janet(uri,method);
    }
    protected Connection openConnection(gnu.iou.uri uri, int method)
	throws java.io.IOException
    {
	Connection connection = (Connection)this.uri2connection.get(uri);
	if (null == connection){
	    connection = this.newConnection(uri,method);
	    this.uri2connection.put(uri,connection);
	}
	return connection;
    }
    protected Connection getConnection(gnu.iou.uri uri)
	throws java.io.IOException
    {
	return (Connection)this.uri2connection.get(uri);
    }
    protected void closeConnection(gnu.iou.uri uri)
	throws java.io.IOException
    {
	Connection connection = (Connection)this.uri2connection.remove(uri);
	if (null != connection){
	    connection.destroy();
	}
    }
    protected Connection connectGET(gnu.iou.uri uri)
	throws java.io.IOException
    {
	return this.openConnection(uri,Connection.GET);
    }
    protected Connection connectPUT(gnu.iou.uri uri)
	throws java.io.IOException
    {
	return this.openConnection(uri,Connection.PUT);
    }
    protected Connection connectPOST(gnu.iou.uri uri)
	throws java.io.IOException
    {
	return this.openConnection(uri,Connection.POST);
    }
    protected java.io.InputStream streamGET(gnu.iou.uri uri)
	throws java.io.IOException
    {
	Connection connection = this.connectGET(uri);
	if (null != connection)
	    return connection.getInputStream();
	else
	    throw new gnu.iou.dom.Error.State(uri.toString());
    }
    protected java.io.OutputStream streamPUT(gnu.iou.uri uri)
	throws java.io.IOException
    {
	Connection connection = this.connectPUT(uri);
	if (null != connection)
	    return connection.getOutputStream();
	else
	    throw new gnu.iou.dom.Error.State(uri.toString());
    }
    protected java.io.OutputStream streamPOST1(gnu.iou.uri uri)
	throws java.io.IOException
    {
	Connection connection = this.connectPOST(uri);
	if (null != connection)
	    return connection.getOutputStream();
	else
	    throw new gnu.iou.dom.Error.State(uri.toString());
    }
    protected java.io.InputStream streamPOST2(gnu.iou.uri uri)
	throws java.io.IOException
    {
	Connection connection = this.getConnection(uri);
	if (null != connection)
	    return connection.getInputStream();
	else
	    throw new gnu.iou.dom.Error.State(uri.toString());
    }
    protected gnu.iou.dom.Document docRead(gnu.iou.uri uri, java.io.InputStream in)
	throws java.io.IOException
    {
	gnu.iou.dom.Document doc = new gnu.iou.dom.impl.Document();
	gnu.iou.dom.io.Source src = new Source(this,uri);
	doc.setSource(src);
	try {
	    gnu.iou.dom.Builder.Parser.Parse(in,doc,src.toString());
	    return doc;
	}
	catch (org.xml.sax.SAXException sax){
	    throw new gnu.iou.dom.Error.State(sax,uri.toString());
	}
    }
    protected void docWrite(gnu.iou.uri uri, 
			java.io.OutputStream out,
			gnu.iou.dom.Document doc)
	throws java.io.IOException
    {
	gnu.iou.dom.Formatter writer = 
	    new gnu.iou.dom.impl.Formatter.Stream(out);
	writer.write(doc);
	out.flush();
    }
    public gnu.iou.dom.Document get(gnu.iou.uri uri)
	throws java.io.IOException
    {
	java.io.InputStream ins = this.streamGET(uri);
	try {
	    return this.docRead(uri,ins);
	}
	finally {
	    this.closeConnection(uri);
	    ins.close();
	}
    }
    public void put(gnu.iou.uri uri, gnu.iou.dom.Document doc)
	throws java.io.IOException
    {
	java.io.OutputStream out = this.streamPUT(uri);
	try {
	    this.docWrite(uri,out,doc);
	}
	finally {
	    this.closeConnection(uri);
	    out.close();
	}
    }
    public gnu.iou.dom.Document post(gnu.iou.uri uri, gnu.iou.dom.Document doc)
	throws java.io.IOException
    {
	java.io.OutputStream out = this.streamPOST1(uri);
	try {
	    this.docWrite(uri,out,doc);
	    java.io.InputStream ins = this.streamPOST2(uri);
	    try {
		return this.docRead(uri,ins);
	    }
	    finally {
		this.closeConnection(uri);
		ins.close();
	    }
	}
	finally {
	    out.close();
	}
    }
}
