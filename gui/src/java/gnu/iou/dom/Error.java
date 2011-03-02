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
 * <p> Exceptions from <code>gnu.iou.dom</code>. </p>
 * 
 * @author John Pritchard (jdp@syntelos.com)
 */
public class Error
    extends java.lang.IllegalStateException
{
    protected final static Throwable THR_NIL = null;
    protected final static String STR_NIL = null;
    protected final static Element ELE_NIL = null;

    /**
     * <p> Unintended state, incorrect operation. </p>
     */
    public static class Bug 
	extends Error
    {
	public Bug(){
	    super();
	}
	public Bug(String msg){
	    super(msg);
	}
	public Bug(Throwable thrown){
	    super(thrown);
	}
	public Bug(Throwable thrown, String msg){
	    super(thrown,msg);
	}
	public Bug(Node re){
	    super(re);
	}
	public Bug(Node re, String msg){
	    super(re,msg);
	}
	public Bug(Node re, Throwable thrown){
	    super(re,thrown);
	}
	public Bug(Node re, Throwable thrown, String msg){
	    super(re,thrown,msg);
	}
    }

    /**
     * <p> Input class file format error. </p>
     */
    public static class Format 
	extends Error
    {
	public Format(){
	    super();
	}
	public Format(String msg){
	    super(msg);
	}
	public Format(Throwable thrown){
	    super(thrown);
	}
	public Format(Throwable thrown, String msg){
	    super(thrown,msg);
	}
	public Format(Node re){
	    super(re);
	}
	public Format(Node re, String msg){
	    super(re,msg);
	}
	public Format(Node re, Throwable thrown){
	    super(re,thrown);
	}
	public Format(Node re, Throwable thrown, String msg){
	    super(re,thrown,msg);
	}
    }

    /**
     * <p> Usage error, or other conflict violates operational
     * requirements. </p>
     */
    public static class State
	extends Error
    {
	public State(){
	    super();
	}
	public State(String msg){
	    super(msg);
	}
	public State(Throwable thrown){
	    super(thrown);
	}
	public State(Throwable thrown, String msg){
	    super(thrown,msg);
	}
	public State(Node re){
	    super(re);
	}
	public State(Node re, String msg){
	    super(re,msg);
	}
	public State(Node re, Throwable thrown){
	    super(re,thrown);
	}
	public State(Node re, Throwable thrown, String msg){
	    super(re,thrown,msg);
	}
    }

    /**
     * <p> Usage error violates input requirements. </p>
     */
    public static class Argument
	extends java.lang.IllegalArgumentException
    {
	private final String re_sid, re_qname;
	private final int re_lno, re_cno;

	public Argument(){
	    this(ELE_NIL,THR_NIL,STR_NIL);
	}
	public Argument(String msg){
	    this(ELE_NIL,THR_NIL,msg);
	}
	public Argument(Throwable thrown){
	    this(ELE_NIL,thrown,STR_NIL);
	}
	public Argument(Throwable thrown, String msg){
	    this(ELE_NIL,thrown,msg);
	}
	public Argument(Node re){
	    this(re,THR_NIL,STR_NIL);
	}
	public Argument(Node re, String msg){
	    this(re,THR_NIL,msg);
	}
	public Argument(Node re, Throwable thrown){
	    this(re,thrown,STR_NIL);
	}
	public Argument(Node re, Throwable thrown, String msg){
	    super(msg);
	    if (null == re){
		this.re_sid = null;
		this.re_qname = null;
		this.re_lno = 0;
		this.re_cno = 0;
	    }
	    else {
		Element elem;
		if (re instanceof Element)
		    elem = (Element)re;
		else {
		    re = re.getParentNode2();
		    if (re instanceof Element)
			elem = (Element)re;
		    else
			elem = null;
		}
		if (null != elem){
		    this.re_sid = elem.getLocSystemId();
		    this.re_qname = elem.getNodeName();
		    this.re_lno = elem.getLocLineNumber();
		    this.re_cno = elem.getLocColumnNumber();
		}
		else {
		    this.re_sid = null;
		    this.re_qname = null;
		    this.re_lno = 0;
		    this.re_cno = 0;
		}
	    }
	    if (null != thrown)
		this.initCause(thrown);
	}

	public boolean hasReference(){
	    return (null != this.re_sid);
	}
	public String getReferenceSystemId(){
	    return this.re_sid;
	}
	public int getReferenceLine(){
	    return this.re_lno;
	}
	public int getReferenceColumn(){
	    return this.re_cno;
	}
    }

    private final String re_sid, re_qname, to_string;
    private final int re_lno, re_cno;

    public Error(){
	this(ELE_NIL,THR_NIL,STR_NIL);
    }
    public Error(String msg){
	this(ELE_NIL,THR_NIL,msg);
    }
    public Error(Throwable thrown){
	this(ELE_NIL,thrown,STR_NIL);
    }
    public Error(Throwable thrown, String msg){
	this(ELE_NIL,thrown,msg);
    }
    public Error(Node re){
	this(re,THR_NIL,STR_NIL);
    }
    public Error(Node re, String msg){
	this(re,THR_NIL,msg);
    }
    public Error(Node re, Throwable thrown){
	this(re,thrown,STR_NIL);
    }
    public Error(Node re, Throwable thrown, String msg){
	super(msg);
	if (null == re){
	    this.re_sid = null;
	    this.re_qname = null;
	    this.re_lno = 0;
	    this.re_cno = 0;
	    this.to_string = super.toString();
	}
	else {
	    Element elem;
	    if (re instanceof Element)
		elem = (Element)re;
	    else {
		re = re.getParentNode2();
		if (re instanceof Element)
		    elem = (Element)re;
		else
		    elem = null;
	    }
	    if (null != elem){
		this.re_sid = elem.getLocSystemId();
		this.re_qname = elem.getNodeName();
		this.re_lno = elem.getLocLineNumber();
		this.re_cno = elem.getLocColumnNumber();
		java.io.PrintStream ps = new gnu.iou.bpo();
		ps.println(super.toString());
		ps.print("\t[qna] ");
		ps.println(this.re_qname);
		ps.print("\t[sid] ");
		ps.println(this.re_sid);
		ps.print("\t[lno] ");
		ps.println(this.re_lno);
		this.to_string = ps.toString();
	    }
	    else {
		this.re_sid = null;
		this.re_qname = null;
		this.re_lno = 0;
		this.re_cno = 0;
		this.to_string = super.toString();
	    }
	}
	if (null != thrown)
	    this.initCause(thrown);
    }

    public boolean hasReference(){
	return (null != this.re_sid);
    }
    public String getReferenceSystemId(){
	return this.re_sid;
    }
    public int getReferenceLine(){
	return this.re_lno;
    }
    public int getReferenceColumn(){
	return this.re_cno;
    }
    public java.lang.String toString(){
	return this.to_string;
    }
}
