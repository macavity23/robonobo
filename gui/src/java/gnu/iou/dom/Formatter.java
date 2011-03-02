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
 * <p> DOM writer. </p>
 * 
 * @author jdp
 */
public interface Formatter
{
    /**
     * Convenience tool for writing XML to output.
     * 
     * @see Builder$Parser
     */
    public static class Writer {

	/**
	 * <p> Write element to output in XML (UTF-8, CRLF). </p>
	 * 
	 * @param elem Document element in output
	 * @param out Output target requires subsequent flushing and
	 * closing
	 * @exception java.io.IOException An error in the output stream
	 * @exception java.lang.IllegalArgumentException For a null
	 * argument
	 */
	public final static void Write(gnu.iou.dom.Element elem, java.io.OutputStream out)
	    throws java.io.IOException
	{
	    if (null == elem || null == out)
		throw new gnu.iou.dom.Error.Argument();
	    else {
		gnu.iou.dom.Formatter writer = 
		    new gnu.iou.dom.impl.Formatter.Stream(out);
		writer.write(elem);
	    }
	}
	/**
	 * 
	 */
	public final static void Write(org.w3c.dom.Element elem, java.io.OutputStream out)
	    throws java.io.IOException
	{
	    if (elem instanceof Element)
		Write( (Element)elem, out);
	    else {
		Document doc = new gnu.iou.dom.impl.Document();
		Element ele = (Element)doc.importNode(elem,true);
		doc.appendChild(ele);
		Write(ele,out);
	    }
	}
	public final static void Write(org.w3c.dom.Document doc, java.io.OutputStream out)
	    throws java.io.IOException
	{
	    if (!(doc instanceof Document)){
		Document doc2 = new gnu.iou.dom.impl.Document();
		doc = (Document)doc2.importNode(doc,true);
	    }
	    gnu.iou.dom.Formatter writer = 
		new gnu.iou.dom.impl.Formatter.Stream(out);
	    writer.write(doc);
	}
    }


    /**
     * The DOM Walker used internally to implement indent, headline
     * and write element.
     */
    public interface State
    {
	/**
	 * Reset to init state 
	 */
	public void reset();
	/**
	 * Subsequently unusable
	 */
	public void destroy();
	/**
	 * @return Toggle headline
	 */
	public boolean headline();
	/**
	 * @return Current indent level at write (write-x, etc.) call
	 */
	public int indent();
	/**
	 * Convenience resolver calls <code>gnu.iou.dom</code> resolver.
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.Node node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Convenience cast to <code>gnu.iou.dom</code> for call to emitter
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.CDATASection node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Convenience cast to <code>gnu.iou.dom</code> for call to emitter
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.Comment node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Convenience cast to <code>gnu.iou.dom</code> for call to emitter
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.Document node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Convenience cast to <code>gnu.iou.dom</code> for call to emitter
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.Element node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Convenience cast to <code>gnu.iou.dom</code> for call to emitter
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.Entity node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Convenience cast to <code>gnu.iou.dom</code> for call to emitter
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.EntityReference node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Convenience cast to <code>gnu.iou.dom</code> for call to emitter
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.ProcessingInstruction node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Convenience cast to <code>gnu.iou.dom</code> for call to emitter
	 * @return Last character output by this call was a newline
	 */
        public boolean write(org.w3c.dom.Text node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Resolver calls sibling (emitter)
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.Node node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Emit output for node
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.CDATASection node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Emit output for node
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.Comment node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Emit output for node
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.Document node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Emit output for node
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.Element node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Emit output for node
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.Entity node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Emit output for node
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.EntityReference node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Emit output for node
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.ProcessingInstruction node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;
	/**
	 * Emit output for node
	 * @return Last character output by this call was a newline
	 */
        public boolean write(gnu.iou.dom.Text node, gnu.iou.dom.Formatter out)
            throws java.io.IOException;

    }


    /**
     * <p> An element implementing this interface has this method
     * called from {@link Formatter#write(gnu.iou.dom.Element)}
     * instead of writing it.  It will be invisible to output. </p>
     * 
     * <p> Its children are ignored only if {@link
     * WriteX#writeX(gnu.iou.dom.Formatter)} returns FALSE, and in
     * this case the {@link WriteX$Closing} interface is ignored as
     * well.  </p>
     * 
     * <p> If the implementor makes output, it must employ CRLF
     * newlines, it's output must be terminated by a CRLF newline and
     * must be UTF-8 text.  Note that UTF-8 is seven bit transparent,
     * and includes the ASCII plain text character set. </p>
     */
    public interface WriteX
	extends Element
    {

	/**
	 * <p> An element implementing this interface will have {@link
	 * #writeX2(gnu.iou.dom.Formatter) writeX2} called after
	 * {@link #writeX(gnu.iou.dom.Formatter) writeX} when that
	 * (write X) method returns TRUE, and after its children have
	 * been evaluated.  </p>
	 * 
	 * <p> It returns TRUE if the last character output was a
	 * newline, otherwise FALSE. </p>
	 */
	public interface Closing
	    extends WriteX
	{
	    /**
	     * @param out Target output sink
	     * @return True when the last character written by this
	     * call was a newline, otherwise false.
	     */
	    public boolean writeX2(Formatter out)
		throws java.io.IOException;
	}

	/**
	 * @param out Target output sink
	 * @return True to subsequently evaluate element's children 
	 */
	public boolean writeX(Formatter out)
	    throws java.io.IOException;
    }

    /**
     * <p> An element implementing this interface (and not {@link
     * Formatter$WriteX}) will have this event called immediately
     * before the inspection of its attributes and children for
     * writing.  The implementor must not emit any output, but may
     * otherwise use or effect modes or appropriate properties of
     * output as necessary.  The implementor will be written to output
     * immediately following this event. </p>
     * 
     * <p> This event should not throw an exception unless it intends
     * to halt writing entirely.   </p>
     * 
     * <p> This event is intended to permit elements to finish the
     * state or organization of their attributes and children prior to
     * emission, like an incremental compilation control.  In such
     * cases the children would not implement this interface unless
     * their implementation of this interface accounts for multiple
     * (indempotent) calls. </p>
     * 
     * <p> This event is called after the node name has been retrieved
     * from the Element by the Formatter, after the node has been
     * checked for {@link Formatter$WriteX}, and before any emission
     * to output on behalf of the Element.  As is true generally
     * throughout the DOM, it is not appropriate for events on nodes
     * to cause them to change their own names. </p>
     */
    public interface Prewrite
	extends Element
    {
	/**
	 * @param out The implementor must not emit any output, but
	 * may otherwise use or effect modes or other appropriate
	 * properties of output.
	 */
	public void prewrite(Formatter.State out);
    }

    /**
     * <p> An element implementing this interface (and not {@link
     * Formatter$WriteX}) will have this event called immediately
     * after its attributes, children and any closing tag have been
     * written to output.  The implementor must not emit any output,
     * but may otherwise use or effect modes or appropriate properties
     * of output as necessary.  </p>
     * 
     * <p> This event should not throw an exception unless it intends
     * to halt writing entirely.   </p>
     * 
     * <p> This event is intended to permit Elements to reverse
     * effects on the Formatter that it performed with the {@link
     * Formatter$Prewrite} event. </p>
     */
    public interface Postwrite
	extends Element
    {
	/**
	 * @param out The implementor must not emit any output, but
	 * may otherwise use or effect modes or other appropriate
	 * properties of output.
	 */
	public void postwrite(Formatter.State out);
    }


    /**
     * @return Current indent level (node depth)
     */
    public int indent();

    /**
     * @return The character being used for indenting, default space,
     * typically space or tab.
     */
    public char getCharIndent();

    /**
     * @param ch The character to be used for indenting, typically
     * space or tab but will accept anything including null (value
     * 0x00).  This can only be set before any elements have been
     * written.
     */
    public void setCharIndent(char ch);

    /**
     * Toggle headline output.  This can only be set before any
     * elements have been written.
     * @return State of headline output after this toggle
     */
    public boolean headline();

    /**
     * Raw write
     */
    public void write(int ch)
	throws java.io.IOException;

    /**
     * Raw write
     */
    public void write(byte[] bary, int ofs, int len)
	throws java.io.IOException;

    /**
     * Write to format, element and its descendants.  Emit format
     * headline on first call.
     */
    public void write(Element node)
	throws java.io.IOException;

    /**
     * Write to format.  Emit format headline on first call.
     */
    public void write(Document node)
	throws java.io.IOException;

    /**
     * Convenience method casts to call for <code>gnu.iou.dom</code>.
     */
    public void write(org.w3c.dom.Element node)
	throws java.io.IOException;

    /**
     * Convenience method casts to call for <code>gnu.iou.dom</code>.
     */
    public void write(org.w3c.dom.Document node)
	throws java.io.IOException;

    /**
     * Write to format
     * @param ch Output character to format encoding
     */
    public void print(char ch)
	throws java.io.IOException;

    /**
     * @param ch Output character to format encoding
     * @param many Repeat output this many times
     */
    public void nprint(char ch, int many)
	throws java.io.IOException;

    /**
     * Write to format
     * @param string Output data to format encoding
     */
    public void print(String string)
	throws java.io.IOException;

    /**
     * Write to format
     * @param indent Indentation level
     * @param ch Output character to format encoding
     */
    public void print(int indent, char ch)
	throws java.io.IOException;

    /**
     * Write to format
     * @param indent Indentation level
     * @param string Output data to format encoding
     */
    public void print(int indent, String string)
	throws java.io.IOException;

    /**
     * Write to format
     * @param ch Output character to format encoding 
     */
    public void println(char ch)
	throws java.io.IOException;

    /**
     * Write newline
     */
    public void println()
	throws java.io.IOException;

    /**
     * Write to format
     * @param string Output data to format encoding with newline
     */
    public void println(String string)
	throws java.io.IOException;

    /**
     * Write to format
     * @param indent Indentation level
     * @param string Output data to format encoding
     */
    public void println(int indent, String string)
	throws java.io.IOException;

    /**
     * Print without violating format (XML safe encodes XML special characters)
     */
    public void printSafe(String string)
	throws java.io.IOException;

    /**
     * Print to quoted format (XML attribute value with double quotes)
     */
    public void printSafeQuoted(String string)
	throws java.io.IOException;

    /**
     * Stream flush may have no effect on plain in- memory buffers, or
     * may write buffers to their ultimate target output sink, etc.,
     * as usual.
     */
    public void flush()
	throws java.io.IOException;

    /**
     * Stream close may discard buffers, etc., as usual.  Does not
     * imply {@link #flush()} which must be called before calling
     * {@link #close()} (for users calling close).  Close is typically
     * called (only) by the ultimate end user that is responsible for
     * creating (and then managing) the stream.
     */
    public void close()
	throws java.io.IOException;

    

}
