 /* 
  *  `gnu.iou' 
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
package gnu.iou;

/**
 * <p> Parse absolute or relative URIs into an immutable object.  URI
 * syntax has been covered by RFCs 1630, 1737, 1738, 1808, 2396 and
 * others. </p>
 * 
 * <p> At most one component of each of a set of principal types will
 * be recognized.  Where multiple of the same type might be included
 * in the source URI string, only the first encountered in (linear)
 * parsing will be available (known) via (most of) this API. </p>
 *  
 * <h3>Description</h3>
 * 
 * <p> A relative URI has a subset of the parts of an absolute URI.
 * It has neither scheme nor host parts. </p>
 * 
 * <p> An absolute URI has a scheme, optionally followed by one or
 * more of the principal parts including host, path, intern, query,
 * fragment or terminal. 
 * 
 * <dl>
 * 
 * <dt><code>scheme<b>:</b></code></dt>
 * <dd> One or more schemes are recognized as subcomponents of the
 * scheme class of component. </dd>
 * 
 * <dt><code><b>//</b>host<b>/</b></code></dt>
 * <dd> The host class of component may have email, password,
 * hostname and port number subcomponents. </dd>
 * 
 * <dt><code>path</code></dt>
 * <dd> The path class of component is recognized after a scheme
 * without a host, after a host, or as the first component of a
 * relative URI without a scheme. </dd>
 * 
 * <dt><code><b>!</b>intern</code></dt>
 * <dd> The intern class component is a kind of path expression that
 * is typically used immediately after a path expression as in the
 * following example.
 * <pre>
 * scheme:path/filename.zip!inside/zip/file.name
 * </pre>
 * 
 * <dt><code><b>?</b>query</code></dt> <dd> The query class component
 * is typically used as a series of name- value pairs, each of which
 * are parsed as subcomponents.  The query parser maintains the
 * equivalence of '?' and '&amp;' in the usual query string syntax.
 * See also {@link uri$Query}.  </dd>
 * 
 * <dt><code><b>#</b>fragment</code></dt>
 * <dd> The fragment component is a kind of path expression, its
 * subcomponents are parsed as subcomponents of a path expresion. </dd>
 * 
 * <dt><code><b>;</b>terminal</code></dt>
 * <dd> The terminal component is a subclass of query and supports the
 * same syntax for the parsing of its subcomponents. </dd>
 * 
 * </dl>
 * 
 * The path component is uniquely naked of regular syntactic features.
 * A relative URI begins with a path component, while an absolute URI
 * (having a scheme part) has a path component following the scheme or
 * host parts.  </p>
 * 
 * <p> Each of these principal component parts of a URI as described
 * above may have one or more subcomponent parts.  
 * 
 * <dl>
 * 
 * <dt><code>scheme<b>:</b></code></dt>
 * <dd>Ex. "<code>a:b:</code>"</dd>
 * 
 * <dt><code><b>//</b>host</code></dt>
 * <dd>Ex. "<code>//user:pass@hostname:portnum</code>"</dd>
 * 
 * <dt><code>path</code></dt>
 * <dd>Ex. "<code>/a/b/c</code>" or "<code>a/b</code>"</dd>
 * 
 * <dt><code><b>!</b>intern</code></dt>
 * <dd>Ex. "<code>!/a/b/c</code>" or "<code>!a/b</code>"</dd>
 * 
 * <dt><code><b>?</b>query</code></dt>
 * <dd>Ex. "<code>?a=b&amp;c=d</code>" or equivalently "<code>?a=b?c=d</code>"</dd>
 * 
 * <dt><code><b>#</b>fragment</code></dt>
 * <dd>Ex. "<code>#a/b/c</code>" or "<code>#a/b</code>"</dd>
 * 
 * <dt><code><b>;</b>terminal</code></dt>
 * <dd>Ex. "<code>;a=b&amp;c=d</code>" or equivalently "<code>;a=b?c=d</code>"</dd>
 * 
 * </dl>
 * </p>
 * 
 * <h4>Examples</h4>
 * 
 * <pre>
 * path
 * a:b:c:path
 * a:b:c:path!intern?query
 * scheme://usrn:pass@hostn:pno/path!intern?query#frag;terminal
 * http://www.syntelos.com/src/org/syntelos/syx/System.java
 * file:src/org/syntelos/syx/System.java
 * </pre>
 * 
 * <h4>Sublanguages</h4>
 * 
 * <h5>Path and Query</h5>
 * 
 * <p> The query and terminal share the same subsyntax, as implemented
 * in query.  The path, intern and fragment share the same subsyntax,
 * as implemented in path. </p>
 * 
 * <h5>URL Code</h5>
 * 
 * <p> The final stage of the parsing process performs {@link
 * url#decode(java.lang.String) URL Decoding} on each of the component
 * and subcomponent parts.  This decoding approach has been designed
 * to permit URI structures and other conflicting syntaces (like JVM
 * type signatures) to be URL encoded within URI structures and
 * exposed by the user API for subsequent parsing. </p>
 * 
 * <p> The original input source URI as available from {@link
 * #toString()} and {@link #getString()} remains encoded.  Likewise
 * the {@link #hashCode()} and {@link #equals(java.lang.Object)} hash
 * key behavior of this class is based on the input source or external
 * representation. </p>
 * 
 * <h3>Usage</h3>
 * 
 * <p> This class presents the parsed URI with an interface for
 * counting, indexing and typing its principal and their sub
 * components.  </p>
 * 
 * <h4>Types</h4>
 * 
 * <p> This class implements a segmented, absolute addressing scheme
 * it calls types.  A type is a descriptor specific to one component
 * or its subcomponent.  </p>
 * 
 * <p> The <i>use of types <b>must</b> adhere</i> to the static use of
 * the <code>TYPE_*</code> constants, or the dynamic use of the
 * <code>Type</code> functions including {@link uri#Type(int,int)},
 * {@link uri#TypeBase(int)}, and {@link uri#TypePart(int)} to ensure
 * (guarantee) compatibility with future versions.  Future versions
 * could tighten the semantics of the type descriptor value in support
 * of expanding the feature set of this class.  </p>
 * 
 * <h4>API</h4>
 * 
 * <p> The principal get component methods (without parameters) return
 * the entire subcomponent including its recognizable suffix or prefix
 * characters, e.g. <code>"http:"</code> or <code>"!intern"</code>.
 * Conversely the subcomponent accessors return the plain tokens,
 * e.g., <code>"http"</code> or <code>"intern"</code>. </p>
 * 
 * <h3>Lifecycle</h3>
 * 
 * <p> An instance of this class is immutable, although subclasses are
 * able to manipulate its behavior as a hash key --- to segment
 * subclasses into distinct key classes.  It will be garbage collected
 * without explicit destruction (no internal cyclic references).  </p>
 * 
 * 
 * @author John Pritchard (john@syntelos.org)
 */
public class uri
    extends url
{
    private final static java.lang.String EMPTY_STRING = new java.lang.String();

    /**
     * Number of bits in the base component of a type value.
     */
    public final static int TYPE_BASE_SIZE = 8;
    /**
     * Basic component type mask is "0xff".  This number (type &amp;
     * mask-base) is the component type including TYPE_SCHEME,
     * TYPE_HOST, TYPE_PATH, TYPE_INTERN, TYPE_FRAGMENT, TYPE_QUERY
     * and TYPE_TERMINAL.
     */
    public final static int TYPE_MASK_BASE   = 0x00ff;
    /**
     * Part component type mask produces the part number using ((type
     * &amp; mask-part) &gt;&gt;&gt; TYPE_BASE_SIZE).  Parts count
     * from one, not zero, so that a part is positively known as such
     * in its type.
     */
    public final static int TYPE_MASK_PART   = 0xffffff00;

    /**
     * Maximum part number value
     */
    public final static int TYPE_PART_Z   = 0xffffff;

    private final static int TYPE_PART_Z_INDEX = (TYPE_PART_Z-1);
    /**
     * Component type NIL is "none" or "error"
     */
    public final static int TYPE_NIL         = 0x0000;
    /**
     * Reference of scheme principal component 
     */
    public final static int TYPE_SCHEME      = 0x0001;
    /**
     * Reference first subcomponent of scheme
     * @see #Type(int,int)
     */
    public final static int TYPE_SCHEME_1    = 0x0001+0x0100;
    public final static int TYPE_SCHEME_2    = 0x0001+0x0200;
    public final static int TYPE_SCHEME_3    = 0x0001+0x0300;
    public final static int TYPE_SCHEME_4    = 0x0001+0x0400;
    public final static int TYPE_SCHEME_5    = 0x0001+0x0500;
    public final static int TYPE_SCHEME_6    = 0x0001+0x0600;
    /**
     * Reference last subcomponent of scheme
     * @see #Type(int,int)
     */
    public final static int TYPE_SCHEME_Z    = 0x0001+0xffffff00;
    /**
     * Reference host principal component.  Note that Type Host
     * supports only four parts.
     */
    public final static int TYPE_HOST        = 0x0002;
    /**
     * Reference host email subcomponent.  Note that Type Host
     * supports only four parts.
     */
    public final static int TYPE_HOST_USER   = 0x0002+0x0100;
    /**
     * Reference host user authentication token subcomponent.  Note
     * that Type Host supports only four parts.
     */
    public final static int TYPE_HOST_PASS   = 0x0002+0x0200;
    /**
     * Reference host name subcomponent.  Note that Type Host supports
     * only four parts.
     */
    public final static int TYPE_HOST_NAME   = 0x0002+0x0300;
    /**
     * Reference host port number subcomponent.  Note that Type Host
     * supports only four parts.
     */
    public final static int TYPE_HOST_PORT   = 0x0002+0x0400;
    /**
     * Reference path principal component 
     */
    public final static int TYPE_PATH        = 0x0004;
    /**
     * Reference first subcomponent of path
     * @see #Type(int,int)
     */
    public final static int TYPE_PATH_1      = 0x0004+0x0100;
    public final static int TYPE_PATH_2      = 0x0004+0x0200;
    public final static int TYPE_PATH_3      = 0x0004+0x0300;
    public final static int TYPE_PATH_4      = 0x0004+0x0400;
    public final static int TYPE_PATH_5      = 0x0004+0x0500;
    public final static int TYPE_PATH_6      = 0x0004+0x0600;
    /**
     * Reference last subcomponent of path
     * @see #Type(int,int)
     */
    public final static int TYPE_PATH_Z      = 0x0004+0xffffff00;
    /**
     * Reference intern principal component 
     */
    public final static int TYPE_INTERN      = 0x0008;
    /**
     * Reference first subcomponent of the intern-path
     * @see #Type(int,int)
     */
    public final static int TYPE_INTERN_1    = 0x0008+0x0100;
    public final static int TYPE_INTERN_2    = 0x0008+0x0200;
    public final static int TYPE_INTERN_3    = 0x0008+0x0300;
    public final static int TYPE_INTERN_4    = 0x0008+0x0400;
    public final static int TYPE_INTERN_5    = 0x0008+0x0500;
    public final static int TYPE_INTERN_6    = 0x0008+0x0600;
    /**
     * Reference last subcomponent of intern
     * @see #Type(int,int)
     */
    public final static int TYPE_INTERN_Z    = 0x0008+0xffffff00;
    /**
     * Reference fragment principal component 
     */
    public final static int TYPE_FRAGMENT    = 0x0010;
    /**
     * Reference first subcomponent
     * @see #Type(int,int)
     */
    public final static int TYPE_FRAGMENT_1  = 0x0010+0x0100;
    public final static int TYPE_FRAGMENT_2  = 0x0010+0x0200;
    public final static int TYPE_FRAGMENT_3  = 0x0010+0x0300;
    public final static int TYPE_FRAGMENT_4  = 0x0010+0x0400;
    public final static int TYPE_FRAGMENT_5  = 0x0010+0x0500;
    public final static int TYPE_FRAGMENT_6  = 0x0010+0x0600;
    /**
     * Reference last subcomponent of fragment
     * @see #Type(int,int)
     */
    public final static int TYPE_FRAGMENT_Z  = 0x0010+0xffffff00;
    /**
     * Reference query principal component 
     */
    public final static int TYPE_QUERY       = 0x0020;
    /**
     * Reference first subcomponent
     * @see #Type(int,int)
     */
    public final static int TYPE_QUERY_1     = 0x0020+0x0100;
    public final static int TYPE_QUERY_2     = 0x0020+0x0200;
    public final static int TYPE_QUERY_3     = 0x0020+0x0300;
    public final static int TYPE_QUERY_4     = 0x0020+0x0400;
    public final static int TYPE_QUERY_5     = 0x0020+0x0500;
    public final static int TYPE_QUERY_6     = 0x0020+0x0600;
    /**
     * Reference first subcomponent
     * @see #Type(int,int)
     */
    public final static int TYPE_QUERY_Z     = 0x0020+0xffffff00;
    /**
     * Reference terminal component
     */
    public final static int TYPE_TERMINAL    = 0x0040;
    /**
     * Reference first subcomponent
     * @see #Type(int,int)
     */
    public final static int TYPE_TERMINAL_1  = 0x0040+0x0100;
    public final static int TYPE_TERMINAL_2  = 0x0040+0x0200;
    public final static int TYPE_TERMINAL_3  = 0x0040+0x0300;
    public final static int TYPE_TERMINAL_4  = 0x0040+0x0400;
    public final static int TYPE_TERMINAL_5  = 0x0040+0x0500;
    public final static int TYPE_TERMINAL_6  = 0x0040+0x0600;
    public final static int TYPE_TERMINAL_Z  = 0x0040+0xffffff00;

    /**
     * @return Component type number, ie one of TYPE_SCHEME,
     * TYPE_HOST, TYPE_PATH, TYPE_INTERN, TYPE_FRAGMENT, TYPE_QUERY or
     * TYPE_TERMINAL.
     */
    public final static int TypeBase(int type){
	return (type & TYPE_MASK_BASE);
    }
    /**
     * @return Component part number, counting from one
     */
    public final static int TypePart(int type){
	return ((type & TYPE_MASK_PART) >>> TYPE_BASE_SIZE);
    }
    /**
     * <p> Construct a type to identify an arbitrary subcomponent.  If
     * in future the type constants should change to accomodate an
     * expanded URI feature set, this function will still know how to
     * construct types from base constants, and part number
     * values. </p>
     * 
     * @param base Base type is one of TYPE_SCHEME,
     * TYPE_HOST, TYPE_PATH, TYPE_INTERN, TYPE_FRAGMENT, TYPE_QUERY or
     * TYPE_TERMINAL.
     * @param part The part number counts from one, except in
     * TYPE_HOST which has four particular parts and four constants to
     * identify parts that may or may not be present.  This function
     * accepts the null (non) part value zero.
     * 
     * @return Type value identifying the requested base and part
     * 
     * @exception java.lang.IllegalArgumentException For an invalid
     * base or part value.  Base must be one of the defined constants.
     * And the part value must be greater than or equal to zero, and
     * less than or equal to the TYPE_PART_Z value
     */
    public final static int Type(int base, int part){
	if (-1 < part && part <= TYPE_PART_Z){
	    switch(base){
	    case TYPE_SCHEME:
	    case TYPE_HOST:
	    case TYPE_PATH:
	    case TYPE_INTERN:
	    case TYPE_QUERY:
	    case TYPE_FRAGMENT:
	    case TYPE_TERMINAL:
		return (base | (part << TYPE_BASE_SIZE));
	    default:
		throw new java.lang.IllegalArgumentException("Base type value not recognized.");
	    }
	}
	else
	    throw new java.lang.IllegalArgumentException("Part value out of range.");
    }

    /**
     * <p> Facilities common to URI parsing subclasses. </p>
     * 
     * <p> As an instance of {@link gnu.iou.intint} this is used to
     * map character values to indeces for the special separators in
     * each segment of the URI: Scheme, Host, Path, Query and
     * Terminal. </p>
     * 
     * @author jdp
     */
    public abstract static class Parser 
	extends gnu.iou.intint 
    {
	/**
	 * Parser.Exception
	 */
	public static class Exception
	    extends java.lang.IllegalArgumentException
	{
	    /**
	     * <p> The source of this exception is a failure in the
	     * intended operation of this code. </p>
	     */
	    public static class Bug
		extends Exception
	    {
		public Bug(){
		    super();
		}
		public Bug(String msg){
		    super(msg);
		}
		public Bug(Throwable thrown, String msg){
		    super(thrown, msg);
		}
	    }

	    /**
	     * <p> The component parser can't accept its assigned
	     * input.  This is used as part of the normal parsing
	     * process to jump out of a lexical branch.  </p>
	     */
	    public static class Component
		extends Exception
	    {
		public Component(){
		    super();
		}
		public Component(String msg){
		    super(msg);
		}
		public Component(Throwable thrown, String msg){
		    super(thrown, msg);
		}
	    }

	    public Exception(Throwable thrown, String msg){
		super(msg);
		this.initCause(thrown);
	    }
	    public Exception(String msg){
		super(msg);
	    }
	    public Exception(){
		super();
	    }
	}


	protected int parser_ofs_start, parser_ofs_end;
	protected java.lang.String parser_term;
	protected java.lang.String parser_components[];

	protected Parser(){
	    super();
	}
	public void clear(){
	    super.clear();
	    this.parser_ofs_start = 0;
	    this.parser_ofs_end = 0;
	    this.parser_term = null;
	    this.parser_components = null;
	}

	protected final char rch(String uri, int subco_idx, int rchx){
	    if (-1 < subco_idx){
		int subco_count = this.parser_components.length;
		int subco_term = (subco_count-1);
		if (subco_idx < subco_count){
		    int uri_len = uri.length();
		    int uri_start = (0 < subco_idx)?(this.value(subco_idx-1)+1):(this.parser_ofs_start);
		    int uri_end = (subco_idx < subco_term)?(this.value(subco_idx)):(this.parser_ofs_end);

		    int uri_idx;
		    if (0 > rchx)
			uri_idx = (uri_end+rchx+1);
		    else
			uri_idx = (uri_start+rchx);

		    if (-1 < uri_idx && uri_idx < uri_len)
			return uri.charAt(uri_idx);
		    else
			return Zech;
		}
		else
		    return Zech;
	    }
	    else
		return Zech;
	}

	public abstract int type();

	public abstract void parse(Parser previous, char[] src, int len);

	protected int finishCount(){
	    return super.size();
	}
	protected java.lang.String finishString(int cix, int cnt, char[] src, int ofs, int len){
	    if (0 == len)
		return EMPTY_STRING;
	    else
		return decode(src,ofs,len);
	}
	protected final void finish(char[] uri, int len, int last){
	    int ofs = this.parser_ofs_start;
	    if (-1 < last && ofs < len){
		this.parser_ofs_end = last;
		int count = (last-ofs)+1;
		this.parser_term = decode(uri,ofs,count);
		//
		count = this.finishCount();
		int trm = (count-1);
		int start = ofs, end, many;
		java.lang.String comp, components[] = new java.lang.String[count];
		for (int cix = 0; cix < count; cix++){
		    end = super.value(cix);
		    if (start <= end){
			many = (end-start)+1;
			comp = this.finishString(cix,count,uri,start,many);
			components[cix] = comp;
			start = (end+1);
		    }
		    else if (cix == trm){
			end = last;
			many = (end-start)+1;
			comp = this.finishString(cix,count,uri,start,many);
			components[cix] = comp;
			break;
		    }
		    else
			throw new Parser.Exception.Bug();
		}
		this.parser_components = components;
	    }
	    else
		this.parser_ofs_end = this.parser_ofs_start;
	}
	public final boolean parserNotEmpty(){
	    return (null != this.parser_term && 0 < this.parser_term.length());
	}
	public final boolean parserEmpty(){
	    return (null == this.parser_term || 1 > this.parser_term.length());
	}
	public final int parserCountComponents(){
	    java.lang.String[] components = this.parser_components;
	    if (null == components)
		return 0;
	    else 
		return components.length;
	}
	public final java.lang.String parserComponent(int idx){
	    java.lang.String[] components = this.parser_components;
	    if (null == components)
		return null;
	    else if (-1 < idx){
		if ( idx < components.length)
		    return components[idx];
		else if (TYPE_PART_Z_INDEX == idx){/*(for 'Z' types)
						    */
		    idx = (components.length-1);
		    return components[idx];
		}
		else
		    return null;
	    }
	    else
		return null;
	}
	public final java.lang.String toString(){
	    if (null != this.parser_term)
		return this.parser_term;
	    else
		throw new java.lang.IllegalStateException("Dead parser.");
	}
	public final java.lang.String testString(){
	    if (null != this.parser_term){
		gnu.iou.chbuf strbuf = new gnu.iou.chbuf();
		for (int idx = 0, len = this.parserCountComponents(); idx < len; idx++){
		    if (0 < idx)
			strbuf.append(' ');
		    strbuf.append(this.parserComponent(idx));
		}
		return strbuf.toString();
	    }
	    else
		return super.toString();
	}
	public final int hashCode(){
	    return this.toString().hashCode();
	}
	public final boolean equals(java.lang.Object ano){
	    if (this == ano)
		return true;
	    else 
		return (ano.toString().equals(this.toString()));
	}
	public final java.lang.String truncate(String uri, int type_base, int type_part){
	    if (this.type() != type_base || 0 == type_part)
		return this.parser_term;
	    else {
		int subco_count = this.parser_components.length;
		int subco_term = (subco_count-1);
		int subco_idx = (type_part - 1);
		int uri_len = uri.length();
		int uri_start = (0 < subco_idx)?(this.value(subco_idx-1)+1):(this.parser_ofs_start);
		int uri_end = (subco_idx < subco_term)?(this.value(subco_idx)):(this.parser_ofs_end);

		if (0 == uri_start)
		    return uri.substring(uri_end+1);
		else {
		    int term = (uri_end + 1);
		    if (term < uri_len)
			return chbuf.cat(uri.substring(0,uri_start),uri.substring(uri_end+1));
		    else
			return uri.substring(0,uri_start);
		}
	    }
	}
    }
    /**
     * <p> A URI scheme component is recognized as always ending with
     * ':'. </p>
     * 
     * @author jdp
     */
    public final static class Scheme 
	extends Parser
    {
	public Scheme(){
	    super();
	}
	public Scheme(Parser previous, char[] uri, int len){
	    super();
	    this.parse(previous,uri,len);
	}
	public int type(){
	    return TYPE_SCHEME;
	}
	public void parse(Parser previous, char[] uri, int len){
	    /*('previous' is null)
	     */
	    int ofs = 0, idx, last = -1;
	    this.parser_ofs_start = ofs;
	    char ch;
	    parsel:
	    for (idx = ofs; idx < len; idx++){
		ch = uri[idx];
		switch (ch){
		case ':':
		    super.add(':',idx);
		    last = idx;
		    break;
		case '-':
		case '.':
		    break;
		default:
		    if (!java.lang.Character.isUnicodeIdentifierPart(ch))
			break parsel;
		    else
			break;
		}
	    }
	    if (0 < last)
		this.finish(uri,len,last);
	    else
		throw new Parser.Exception.Component();
	}
	protected final java.lang.String finishString(int cix, int cnt, char[] src, int ofs, int len){
	    if (0 == len)
		return EMPTY_STRING;
	    else {
		len -= 1;
		if (1 == len && ':' == src[ofs])
		    return EMPTY_STRING;
		else
		    return decode(src,ofs,len);
	    }
	}
    }

    /**
     * <p> The host component of a URI is recognized by starting with
     * a double slash, "//", and ending with the end of the URI or the
     * beginning of a path component, "/". </p>
     * @author jdp
     */
    public final static class Host 
	extends Parser
    {
	private int host_user_name, host_user_pass, host_host_name, host_host_port;

	public Host(){
	    super();
	}
	public Host(Parser previous, char[] uri, int len){
	    super();
	    this.parse(previous,uri,len);
	}
	public int type(){
	    return TYPE_HOST;
	}
	public void clear(){
	    super.clear();
	    this.host_user_name = -1;
	    this.host_user_pass = -1;
	    this.host_host_name = -1;
	    this.host_host_port = -1;
	}
	public void parse(Parser previous, char[] uri, int len){
	    int ofs;
	    if (null == previous)
		ofs = 0;/*(non case included for completeness)
			 */
	    else
		ofs = (previous.parser_ofs_end+1);
	    this.parser_ofs_start = ofs;
	    if (ofs < len){
		int idx = ofs;
		char ch = uri[idx];
		if ('/' == ch){
		    idx += 1;
		    ch = uri[idx];
		    if ('/' == ch){
			int user = -1;
			int last = -1;
			parsel:
			for (idx += 1; idx < len; idx++){
			    ch = uri[idx];
			    switch (ch){
			    case ':':
				super.add(':',idx);
				break;
			    case '@':
				user = super.size();
				super.add('@',idx);
				break;
			    case '/':
			    case '!':
			    case '?':
			    case ';':
			    case '#':
				last = (idx-1);
				break parsel;
			    default:
				break;
			    }
			}
			if (0 > last)
			    last = (len-1);
			this.finish(uri,len,last);
			//
			if (-1 < user){
			    switch(this.parserCountComponents()){
			    case 0:
			    case 1:
				throw new Parser.Exception.Bug();
			    case 2:
				this.host_user_name =  0;
				this.host_user_pass = -1;
				this.host_host_name =  1;
				this.host_host_port = -1;
				break;
			    case 3:
				switch (user){
				case 0:
				    this.host_user_name =  0;
				    this.host_user_pass = -1;
				    this.host_host_name =  1;
				    this.host_host_port =  2;
				    break;
				case 1:
				    this.host_user_name =  0;
				    this.host_user_pass =  1;
				    this.host_host_name =  2;
				    this.host_host_port = -1;
				    break;
				default:
				    throw new Parser.Exception.Bug();
				}
				break;
			    case 4:
				switch (user){
				case 0:
				    this.host_user_name =  0;
				    this.host_user_pass = -1;
				    this.host_host_name =  1;
				    this.host_host_port =  2;
				    break;
				case 1:
				    this.host_user_name =  0;
				    this.host_user_pass =  1;
				    this.host_host_name =  2;
				    this.host_host_port =  3;
				    break;
				default:
				    throw new Parser.Exception.Bug();
				}
				break;
			    default:
				throw new Parser.Exception.Bug();
			    }
			}
			else {
			    this.host_user_name = -1;
			    this.host_user_pass = -1;
			    switch(this.parserCountComponents()){
			    case 0:
				this.host_host_name = -1;
				this.host_host_port = -1;
				break;
			    case 1:
				this.host_host_name =  0;
				this.host_host_port = -1;
				break;
			    case 2:
				this.host_host_name =  0;
				this.host_host_port =  1;
				break;
			    default:
				throw new Parser.Exception.Bug();
			    }
			}
		    }
		    else
			throw new Parser.Exception.Component();
		}
		else
		    throw new Parser.Exception.Component();
	    }
	    else if (ofs == len)
		throw new Parser.Exception.Component();
	    else
		throw new Parser.Exception.Bug();
	}
	protected final int finishCount(){
	    return super.size()+1;
	}
	protected final java.lang.String finishString(int cix, int cnt, char[] src, int ofs, int len){
	    if (0 == len)
		return EMPTY_STRING;
	    else
		switch (cix){
		case 0:
		    ofs += 2;
		    len -= 2;
		    //(fall)
		default:
		    int trm = (ofs+len)-1;
		    switch (src[trm]){
		    case ':':
		    case '@':
			len -= 1;
			//(fall)
		    default:
			if (0 == len)
			    return EMPTY_STRING;
			else
			    return decode(src,ofs,len);
		    }
		}
	}

	public final java.lang.String getUserName(){
	    return this.parserComponent(this.host_user_name);
	}
	public final java.lang.String getUserPass(){
	    return this.parserComponent(this.host_user_pass);
	}
	public final java.lang.String getHostName(){
	    return this.parserComponent(this.host_host_name);
	}
	public final java.lang.String getHostPort(){
	    return this.parserComponent(this.host_host_port);
	}
    }
    /**
     * 
     * @author jdp
     */
    public static class Path 
	extends Parser
    {
	protected final static char NIL_PREFIX    = (char)0;
	protected final static char INTERN_PREFIX = '!';
	protected final static char FRAG_PREFIX   = '#';

	public Path(){
	    super();
	}
	public Path(Parser previous, char[] uri, int len){
	    super();
	    this.parse(previous,uri,len);
	}
	public int type(){
	    return TYPE_PATH;
	}
	protected char parsePrefix(){
	    return NIL_PREFIX;
	}
	public final void parse(Parser previous, char[] uri, int len){
	    int ofs;
	    if (null == previous)
		ofs = 0;
	    else if (previous.parserEmpty())
		ofs = previous.parser_ofs_end;
	    else
		ofs = (previous.parser_ofs_end+1);
	    this.parser_ofs_start = ofs;
	    if (ofs < len){
		char prefix = this.parsePrefix();
		int idx = ofs;
		char ch = uri[idx];
		boolean ok = false;
		if (NIL_PREFIX == prefix)
		    ok = true;
		else if (ch == prefix){
		    ok = true;
		    idx += 1;
		}
		//
		if (ok){
		    int last = -1;
		    parsel:
		    for (; idx < len; idx++){
			ch = uri[idx];
			switch (ch){
			case '/':
			    super.add('/',idx);
			    break;
			case '!': //(intern:subclass of path with prefix '!')
			case '?':
			case '#': //(fragment:subclass of path with prefix '#')
			case ';':
			    if (ofs == idx){
				throw new Parser.Exception.Component();
			    }
			    else {
				last = (idx-1);
				break parsel;
			    }

			default:
			    break;
			}
		    }
		    if (0 > last)
			last = (len-1);

		    this.finish(uri,len,last);
		}
		else
		    throw new Parser.Exception.Component();
	    }
	    else if (ofs == len)
		throw new Parser.Exception.Component();
	    else
		throw new Parser.Exception.Bug();
	}
	protected final int finishCount(){
	    return super.size()+1;
	}
	protected final java.lang.String finishString(int cix, int cnt, char[] src, int ofs, int len){
	    if (0 == len)
		return EMPTY_STRING;
	    else if (NIL_PREFIX != this.parsePrefix())
		switch (cix){
		case 0:
		    ofs += 1;
		    len -= 1;
		    //(fall)
		default:
		    if (0 == len)
			return EMPTY_STRING;
		    else
			return decode(src,ofs,len);
		}
	    else
		return decode(src,ofs,len);
	}

	public final java.lang.String cat(int start, int end){
	    java.lang.String[] components = this.parser_components;
	    if (null == components)
		return null;
	    else {
		int count = components.length;
		gnu.iou.chbuf strbuf = new gnu.iou.chbuf();
		for (int idx = java.lang.Math.max(0,start), len = java.lang.Math.min(count,(end+1)); idx < len; idx++)
		    strbuf.append(components[idx]);
		return strbuf.toString();
	    }
	}
	public final java.lang.String head(int N){
	    return this.cat(0,N);
	}
	public final java.lang.String tail(int N){
	    java.lang.String[] components = this.parser_components;
	    if (null == components || 1 > N)
		return null;
	    else {
		int count = components.length;
		if (N >= count)
		    return this.cat(0,count);
		else {
		    int ofs = (count - N - 1);
		    return this.cat(ofs,count);
		}
	    }
	}

    }
    /**
     * 
     * @author jdp
     */
    public final static class Intern
	extends Path
    {
	public Intern(){
	    super();
	}
	public Intern(Parser previous, char[] uri, int len){
	    super();
	    this.parse(previous,uri,len);
	}
	public int type(){
	    return TYPE_INTERN;
	}
	protected final char parsePrefix(){
	    return INTERN_PREFIX;
	}
    }
    /**
     * <p> This class is instantiated in Query or Terminal. </p>
     * 
     * <p> The query parser maintains the equivalence of '?' and
     * '&amp;' in the otherwise standard query string syntax so that
     * the following expressions are equivalent.
     * 
     * <pre>
     *   ?name=value&amp;name2=value2
     *   ?name=value?name2=value2
     * </pre>
     * 
     * As the simpler symmetric syntax in the second case is more
     * appropriate to this micro structure language than the standard,
     * more complex asymmetric syntax in the first case.  Permitting
     * the symmetric syntax in this class affords us the opportunity
     * to use it. </p>
     * 
     * @author jdp
     */
    public static class Query 
	extends Parser
    {
	public static class Map
	    extends objmap
	{
	    private java.lang.String finish_name;

	    public Map(){
		super();
	    }

	    public java.lang.String[] queryKeys(){

		return (java.lang.String[])this.keyary(java.lang.String.class);
	    }
	    public java.lang.String queryLookup(java.lang.String key){

		return (java.lang.String)this.get(key);
	    }
	    protected void finish(int cix, char key, String value){

		switch (key){
		case '=':
		    if (null != this.finish_name){
			this.put(this.finish_name,value);
			this.finish_name = null;
			return;
		    }
		    else { 
			this.put(EMPTY_STRING,value);/*(dubious support)
						      */
			return;
		    }
		case '?':
		case '&':
		default:
		    if (null != this.finish_name){
			this.put(this.finish_name,EMPTY_STRING);
			this.finish_name = null;
		    }
		    this.finish_name = value;
		    return;
		}
	    }
	}


	protected final static char NIL_PREFIX    = (char)0;
	protected final static char QUERY_PREFIX = '?';
	protected final static char TERMINAL_PREFIX   = ';';
	private final static char AntiPrefix(char pr){
	    switch(pr){
	    case QUERY_PREFIX:
		return TERMINAL_PREFIX;
	    case TERMINAL_PREFIX:
		return QUERY_PREFIX;
	    default:
		throw new Parser.Exception.Bug();
	    }
	}

	protected Query.Map map = new Query.Map();

	public Query(){
	    super();
	}
	public Query(Parser previous, char[] uri, int len){
	    super();
	    this.parse(previous,uri,len);
	}

	public java.lang.String[] mapKeys(){
	    return this.map.queryKeys();
	}
	public java.lang.String mapLookup(java.lang.String key){
	    return this.map.queryLookup(key);
	}

	public int type(){
	    return TYPE_QUERY;
	}
	protected char parsePrefix(){
	    return QUERY_PREFIX;
	}
	public void parse(Parser previous, char[] uri, int len){
	    int ofs;
	    if (null == previous)
		ofs = 0;
	    else if (previous.parserEmpty())
		ofs = previous.parser_ofs_end;
	    else
		ofs = (previous.parser_ofs_end+1);
	    this.parser_ofs_start = ofs;
	    if (ofs < len){
		char prefix = this.parsePrefix();
		char anti_prefix = AntiPrefix(prefix);
		int idx = ofs;
		char ch = uri[idx];
		if (prefix == ch){
		    int last = -1;
		    parsel:
		    for (idx += 1; idx < len; idx++){
			ch = uri[idx];
			switch (ch){
			case '?':
			    super.add('?',idx);
			    break;
			case '&':
			    super.add('&',idx);
			    break;
			case '=':
			    super.add('=',idx);
			    break;

			case '!':
			case '#':
			    if (ofs == idx){
				throw new Parser.Exception.Component();
			    }
			    else {
				last = (idx-1);
				break parsel;
			    }

			default:
			    if (prefix == ch)
				super.add(prefix,idx);

			    else if (anti_prefix == ch){

				if (ofs == idx){
				    throw new Parser.Exception.Component();
				}
				else {
				    last = (idx-1);
				    break parsel;
				}
			    }
			    break;
			}
		    }
		    if (0 > last)
			last = (len-1);

		    this.finish(uri,len,last);
		}
		else
		    throw new Parser.Exception.Component();
	    }
	    else if (ofs == len)
		throw new Parser.Exception.Component();
	    else
		throw new Parser.Exception.Bug();
	}
	protected final int finishCount(){
	    return super.size()+1;
	}
	protected final java.lang.String finishString(int cix, int cnt, char[] src, int ofs, int len){
	    if (0 == len)
		return EMPTY_STRING;
	    else
		switch (cix){
		case 0:
		    ofs += 1;
		    len -= 1;
		    //(fall)
		default:
		    int trm = (ofs+len)-1;
		    switch (src[trm]){
		    case '?':
		    case '=':
		    case '&':
			len -= 1;
			//(fall)
		    default:
			String re;
			if (0 == len)
			    re = EMPTY_STRING;
			else
			    re = decode(src,ofs,len);
			char key = (char)super.key(cix-1);
			this.map.finish(cix,key,re);
			return re;
		    }
		}
	}

    }
    /**
     * 
     * @author jdp
     */
    public final static class Fragment
	extends Path
    {
	public Fragment(){
	    super();
	}
	public Fragment(Parser previous, char[] uri, int len){
	    super();
	    this.parse(previous,uri,len);
	}
	public int type(){
	    return TYPE_FRAGMENT;
	}
	protected final char parsePrefix(){
	    return FRAG_PREFIX;
	}
    }
    /**
     * 
     * @author jdp
     */
    public final static class Terminal 
	extends Query
    {
	public Terminal(){
	    super();
	}
	public Terminal(Parser previous, char[] uri, int len){
	    super();
	    this.parse(previous,uri,len);
	}
	public final int type(){
	    return TYPE_TERMINAL;
	}
	protected final char parsePrefix(){
	    return TERMINAL_PREFIX;
	}
    }
    /**
     * <p> Character value zero is a special return value from
     *     {@link #charByIndex(int,int,int) char by index}. 
     * </p>
     * @see #charByIndex(int,int,int)
     */
    public final static char Zech = (char)0;

    /**
     * <p> Function called from truncate constructor. </p>
     */
    protected final static java.lang.String Truncate(uri source, int type){
	if (null == source)
	    throw new java.lang.IllegalArgumentException("Null source URI");
	else 
	    return source.truncate(type);
    }

    protected final java.lang.String uri;

    protected final boolean absolute;

    private int hashcode;

    private intmap components;

    /**
     * Construct an empty URI object that is not absolute because it
     * has no scheme.
     */
    protected uri(){
	this(false);
    }
    /**
     * Construct an empty URI object with an empty string uri and
     * hashcode.
     * @param absolute If true, an empty URI that appears to be an
     * absolute URI
     */
    protected uri(boolean absolute){
	super();
	this.uri = "";
	this.hashcode = uri.hashCode();
	this.absolute = absolute;
	this.components = new intmap();
    }

    /**
     * @param uri The input source uri is trimmed and interned and
     * then that value is hashed, parsed, or returned by {@link
     * #getString()} or {@link #toString()}, et cetera.
     * @see #getString()
     * @see #toString()
     * @see #hashCode()
     */
    public uri (java.lang.String uri){
	super();
	if (null == uri || 1 > uri.length())
	    throw new java.lang.IllegalArgumentException("Null or empty URI argument.");
	else {
	    this.uri = uri.trim().intern();
	    this.hashcode = uri.hashCode();

	    char[] cary = this.uri.toCharArray();
	    int cary_len = cary.length;
	    int parser = 1, components_len, ofs;
	    Parser prev = null, term = null;
	    intmap components = new intmap();
	    parsel: 
	    while (true){
		try {
		    switch (parser){
		    case TYPE_SCHEME:
			term = new Scheme(prev,cary,cary_len);
			break;
		    case TYPE_HOST:
			term = new Host(prev,cary,cary_len);
			break;
		    case TYPE_PATH:
			term = new Path(prev,cary,cary_len);
			break;
		    default:
			if (prev.parserEmpty())
			    ofs = prev.parser_ofs_end;
			else
			    ofs = (prev.parser_ofs_end+1);
			//
			if (ofs >= cary_len)
			    break parsel;
			else 
			    switch (cary[ofs]){
			    case '!':
				term = new Intern(prev,cary,cary_len);
				break;
			    case '?':
				term = new Query(prev,cary,cary_len);
				break;
			    case '#':
				term = new Fragment(prev,cary,cary_len);
				break;
			    case ';':
				term = new Terminal(prev,cary,cary_len);
				break;
			    default:
				/*(with relative uri, following path)
				 */
				break parsel;
			    }
			break;
		    }
		    if (null == components.get(term.type()))
			components.put(term.type(),term);
		    prev = term;
		    parser <<= 1;
		}
		catch (Parser.Exception.Component next){
		    if (TYPE_SCHEME == parser)
			parser <<= 1; //(skip [host] without [scheme])
		    //
		    parser <<= 1;
		    continue parsel;
		}
	    }
	    if (1 > components.size())
		throw new Parser.Exception(this.uri);
	    else {
		this.components = components;
		this.absolute = (null != components.get(TYPE_SCHEME));
	    }
	}
    }
    /**
     * <p> Truncated copy </p>
     * 
     * @param source A source URI
     * @param truncate A type descriptor value to truncate in the copy
     * of source
     * @exception java.lang.IllegalArgumentException For a null
     * 'source', or for 'truncate' not found in 'source'
     */
    public uri( uri source, int truncate){
	this(Truncate(source,truncate));
    }
    /**
     * @return The trimmed and interned source URI string
     * @see #toString()
     * @see #uri(java.lang.String)
     */
    public final java.lang.String getString(){
	return this.uri;
    }
    /**
     * @return Whether this URI has not a scheme part.  If true, then
     * it should start with a path part --- or in other words a host
     * part is not a possible part of a relative URI.
     */
    public final boolean isRelative(){
	return (!this.absolute);
    }
    /**
     * @return Whether this URI has a scheme part
     */
    public final boolean isAbsolute(){
	return (this.absolute);
    }
    /**
     * @return The scheme component including all subcomponents and
     * colon <code>':'</code> suffices.
     */
    public final java.lang.String getScheme(){
	return this.getByType(TYPE_SCHEME);
    }
    /**
     * @param idx Index subcomponents counting from zero for the head
     * subcomponent, ascending through one to the tail subcomponent.   
     * @return Plain subcomponent tokens without colon
     * <code>':'</code> suffices.
     */
    public final java.lang.String getScheme(int idx){
	if (-1 < idx){
	    int addr = Type(TYPE_SCHEME,(idx+1));
	    return this.getByType(addr);
	}
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    /**
     * @return The last component of the scheme
     */
    public final java.lang.String getSchemeTail(){
	return this.getByType(TYPE_SCHEME_Z);
    }
    public final java.lang.String getSchemeHead(){
	return this.getByType(TYPE_SCHEME_1);
    }
    /**
     * @return The host component including all subcomponents
     */
    public final java.lang.String getHost(){
	return this.getByType(TYPE_HOST);
    }
    public final java.lang.String getHostUser(){
	return this.getByType(TYPE_HOST_USER);
    }
    public final java.lang.String getHostPass(){
	return this.getByType(TYPE_HOST_PASS);
    }
    public final java.lang.String getHostName(){
	return this.getByType(TYPE_HOST_NAME);
    }
    public final java.lang.String getHostPort(){
	return this.getByType(TYPE_HOST_PORT);
    }
    /**
     * @return The path component including all subcomponents
     */
    public final java.lang.String getPath(){
	return this.getByType(TYPE_PATH);
    }
    /**
     * @param idx Index subcomponents counting from zero for the head
     * subcomponent, ascending through one to the tail subcomponent
     */
    public final java.lang.String getPath(int idx){
	if (-1 < idx){
	    int addr = Type(TYPE_PATH,(idx+1));
	    return this.getByType(addr);
	}
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    /**
     * @return The last component of the path part when present,
     * otherwise null.
     */
    public final java.lang.String getPathTail(){
	return this.getByType(TYPE_PATH_Z);
    }
    public final java.lang.String getPathHead(){
	return this.getByType(TYPE_PATH_1);
    }
    /**
     * @return The intern component including all subcomponents
     */
    public final java.lang.String getIntern(){
	return this.getByType(TYPE_INTERN);
    }
    /**
     * @param idx Index subcomponents counting from zero for the head
     * subcomponent, ascending through one to the tail subcomponent
     */
    public final java.lang.String getIntern(int idx){
	if (-1 < idx){
	    int addr = Type(TYPE_INTERN,(idx+1));
	    return this.getByType(addr);
	}
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    /**
     * @return The last component of the intern-path part when
     * present, otherwise null.
     */
    public final java.lang.String getInternTail(){
	return this.getByType(TYPE_INTERN_Z);
    }
    public final java.lang.String getInternHead(){
	return this.getByType(TYPE_INTERN_1);
    }
    /**
     * @return The query component including all subcomponents
     */
    public final java.lang.String getQuery(){
	return this.getByType(TYPE_QUERY);
    }
    /**
     * @param idx Index subcomponents counting from zero for the head
     * subcomponent, ascending through one to the tail subcomponent
     */
    public final java.lang.String getQuery(int idx){
	if (-1 < idx){
	    int addr = Type(TYPE_QUERY,(idx+1));
	    return this.getByType(addr);
	}
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final java.lang.String[] getQueryKeys(){
	Query query = (Query)this.components.get(TYPE_QUERY);
	if (null == query)
	    return null;
	else 
	    return query.mapKeys();
    }
    public final java.lang.String getQueryLookup(java.lang.String key){
	Query query = (Query)this.components.get(TYPE_QUERY);
	if (null == query)
	    return null;
	else 
	    return query.mapLookup(key);
    }
    public final java.lang.String getQuery(java.lang.String key){

	return this.getQueryLookup(key);
    }
    /**
     * @return The fragment component including all subcomponents
     */
    public final java.lang.String getFragment(){
	return this.getByType(TYPE_FRAGMENT);
    }
    /**
     * @param idx Index subcomponents counting from zero for the head
     * subcomponent, ascending through one to the tail subcomponent
     */
    public final java.lang.String getFragment(int idx){
	if (-1 < idx){
	    int addr = Type(TYPE_FRAGMENT,(idx+1));
	    return this.getByType(addr);
	}
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final java.lang.String getFragmentHead(){
	return this.getByType(TYPE_FRAGMENT_1);
    }
    public final java.lang.String getFragmentTail(){
	return this.getByType(TYPE_FRAGMENT_Z);
    }
    /**
     * @return The terminal component including all subcomponents
     */
    public final java.lang.String getTerminal(){
	return this.getByType(TYPE_TERMINAL);
    }
    /**
     * @param idx Index subcomponents counting from zero for the head
     * subcomponent, ascending through one to the tail subcomponent
     */
    public final java.lang.String getTerminal(int idx){
	if (-1 < idx){
	    int addr = Type(TYPE_TERMINAL,(idx+1));
	    return this.getByType(addr);
	}
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final java.lang.String getTerminalHead(){
	return this.getByType(TYPE_TERMINAL_1);
    }
    public final java.lang.String getTerminalTail(){
	return this.getByType(TYPE_TERMINAL_Z);
    }
    public final java.lang.String[] getTerminalKeys(){
	Terminal query = (Terminal)this.components.get(TYPE_TERMINAL);
	if (null == query)
	    return null;
	else 
	    return query.mapKeys();
    }
    public final java.lang.String getTerminalLookup(java.lang.String key){
	Terminal query = (Terminal)this.components.get(TYPE_TERMINAL);
	if (null == query)
	    return null;
	else 
	    return query.mapLookup(key);
    }
    /**
     * @return Number of principal components in this URI
     * @see #getByIndex(int)
     */
    public final int count(){
	return this.components.size();
    }
    /**
     * @param type A type value as defined above can address principal
     * or subcomponents.  Note that Type Host supports only four
     * parts.
     * @return Addressed component
     */
    public final java.lang.String getByType(int type){
	int base = TypeBase(type);
	Parser component = (Parser)this.components.get(base);
	if (null == component)
	    return null;
	else {
	    int part = TypePart(type);
	    if (0 < part){
		if (TYPE_HOST == base){
		    Host host = (Host)component;
		    switch(type){
		    case TYPE_HOST_USER:
			return host.getUserName();
		    case TYPE_HOST_PASS:
			return host.getUserPass();
		    case TYPE_HOST_NAME:
			return host.getHostName();
		    case TYPE_HOST_PORT:
			return host.getHostPort();
		    default:
			throw new java.lang.IllegalArgumentException("Type Host supports only four parts.");
		    }
		}
		else {
		    int subindex = (part - 1);
		    return component.parserComponent(subindex);
		}
	    }
	    else
		return component.toString();
	}
    }
    /**
     * @param index URI component index counting from zero and less
     * than the number of components in the original input URI
     * @return The indexed component, or null
     * @see #count()
     */
    public final java.lang.String getByIndex(int index){
	Parser component = (Parser)this.components.value(index);
	if (null == component)
	    return null;
	else
	    return component.toString();
    }
    /**
     * @param index URI component index counting from zero and less
     * than the number of components in the original input URI
     * @return The type of the indexed component, ie one of
     * TYPE_SCHEME, TYPE_HOST, TYPE_PATH, TYPE_INTERN, TYPE_QUERY,
     * TYPE_FRAGMENT, TYPE_TERMINAL, or TYPE_NIL (zero)
     * @see #count()
     */
    public final int typeOfByIndex(int index){
	Parser component = (Parser)this.components.value(index);
	if (null == component)
	    return TYPE_NIL;
	else
	    return component.type();
    }
    /**
     * @param index URI component index counting from zero and less
     * than the number of components in the original input URI
     * @return The number of subcomponents in the indexed component,
     * or zero.
     * @see #count()
     */
    public final int countByIndex(int index){
	Parser component = (Parser)this.components.value(index);
	if (null == component)
	    return 0;
	else
	    return component.parserCountComponents();
    }
    /**
     * @param index URI component index counting from zero and less
     * than the number of components in the original input URI
     * @param subindex The subcomponent index counting from zero and
     * less than the number of subcomponents in the indexed component
     * @return The indexed subcomponent from the indexed component, or
     * null.
     * @see #count()
     * @see #countByIndex(int)
     */
    public final java.lang.String getByIndex(int index, int subindex){
	Parser component = (Parser)this.components.value(index);
	if (null == component)
	    return null;
	else
	    return component.parserComponent(subindex);
    }
    /**
     * @param index URI component index counting from zero and less
     * than the number of components in the original input URI
     * @param subindex The subcomponent index counting from zero and
     * less than the number of subcomponents in the indexed component
     * @return The type of the indexed subcomponent, including the
     * part number, or TYPE_NIL (zero).  The returned type value can
     * be used in the {@link #getByType(int)} method to lookup the
     * same subcomponent.
     * @see #count()
     * @see #countByIndex(int)
     */
    public final int typeOfByIndex(int index, int subindex){
	Parser component = (Parser)this.components.value(index);
	if (null == component)
	    return TYPE_NIL;
	else {
	    int type = component.type();
	    if (-1 < subindex && subindex < component.parserCountComponents())
		return (type & ((subindex+1)<<TYPE_BASE_SIZE));
	    else
		return TYPE_NIL;
	}
    }
    /**
     * <p> Access characters of a component relative to a
     * subcomponent.  This may be used to inspect a character of the
     * component that has been stripped from the subcomponent. </p>
     * @param index Component index
     * @param subindex Subcomponent index within the referenced component
     * @param rchix Relative character index into the component with
     * respect to the source start and end offsets of the
     * subcomponent.  A zero- positive "rchix" is relative to the
     * start of the indexed subcomponent, while a negative "rchix" is
     * relative to the end of the indexed subcomponent so that
     * negative one is the last character in the source subcomponent
     * (which may be stripped from some parsed subcomponents).
     * @return Zero for indexed position not found
     * @see #Zech
     */
    public final char charByIndex(int index, int subindex, int rchix){
	Parser component = (Parser)this.components.value(index);
	if (null == component)
	    return Zech;
	else 
	    return component.rch(this.uri,subindex,rchix);
    }
    /**
     * <p> This has private access because parser objects have a
     * vulnerable state and should not be exposed.  Also this class
     * has been defined as immutable, so these mutable objects cannot
     * be exposed according to this definition. </p>
     */
    private final Parser[] getComponents(){
	return (Parser[])this.components.valary(Parser.class);
    }
    public final int[] getComponentTypes(){
	return this.components.keyary();
    }
    private Parser getComponentByType(int type){
    	int base = TypeBase(type);
	if (base != type)
	    throw new java.lang.IllegalArgumentException("Type not principal");
	else 
	    return (Parser)this.components.get(base);
    }
    /**
     * <p> Truncate a component or subcomponent from the original
     * source URI string. </p>
     * 
     * @param type Component with optional subcomponent part descriptor
     * @return This uri with the addressed component or subcomponent
     * truncated from the original source string
     */
    public String truncate(int type){
	int type_base = TypeBase(type);
	int type_part = TypePart(type);
	Parser component = this.getComponentByType(type_base);
	if (null == component)
	    return this.uri;
	else
	    return component.truncate(uri,type_base,type_part);
    }
    /**
     * @return The trimmed and interned source URI string
     * @see #getString()
     * @see #uri(java.lang.String)
     * @see #hashCode()
     */
    public final java.lang.String toString(){
	return this.uri;
    }
    /**
     * @return Hash of the trimmed and interned input string
     * @see #uri(java.lang.String)
     * @see #toString()
     * @see #getString()
     * @see #hashCodeXor()
     */
    public final int hashCode(){
	return this.hashcode;
    }
    /** 
     * Call this from a subclass constructor to define the hash code
     * value.
     * @param value Modify the hashcode of this class by setting it to
     * the operand value
     * @see #hashCode()
     * @see #hashCodeXor()
     * @see #hashCodeXor(java.lang.String)
     * @see #hashCodeXor(int)
     */
    protected final void hashCodeSet(int value){
	this.hashcode = value;
    }
    /**
     * Modify the hashcode of this class by XOR'ing it with the string
     * name of this class.  Call this from a subclass constructor to
     * segment that subclass into a unique hash key space where only
     * instances of the subclass will get or put one and the other in
     * a hash table.
     * @see #hashCode()
     * @see #hashCodeXor(java.lang.String)
     * @see #hashCodeXor(int)
     */
    protected final void hashCodeXor(){
	this.hashCodeXor(this.getClass().getName());
    }
    /** 
     * Call this from a subclass constructor to segment that subclass
     * into a unique hash key space where only instances of the
     * subclass will get or put one and the other in a hash table.
     * @param string Modify the hashcode of this class by XOR'ing it
     * with the hash code of the operand string.
     * @see #hashCode()
     * @see #hashCodeXor()
     * @see #hashCodeXor(int)
     */
    protected final void hashCodeXor(java.lang.String string){
	this.hashCodeXor(string.hashCode());
    }
    /**
     * Call this from a subclass constructor to segment that subclass
     * into a unique hash key space where only instances of the
     * subclass will get or put one and the other in a hash table.
     * @param string Modify the hashcode of this class by XOR'ing it
     * with the operand value.
     * @see #hashCode()
     * @see #hashCodeXor()
     * @see #hashCodeXor(java.lang.String)
     */
    protected final void hashCodeXor(int with){
	this.hashcode ^= with;
    }
    /**
     * @param another An instance of this class, a string, or a class
     * whose string may match to this uri original source input string
     * @return True for the identical object to this, a string
     * identical to the trimmed and interned source uri string, or an
     * equivalent string.
     * @see #uri(java.lang.String)
     */
    public final boolean equals(java.lang.Object another){
	if (another == this)
	    return true;
	else {
	    java.lang.String uri = this.uri;
	    if (another == uri)
		return true;
	    else if (another instanceof String)
		return uri.equals(another);
	    else
		return uri.equals(another.toString());
	}
    }
    public final java.lang.String testString(){
	Parser components[] = this.getComponents(), component;
	if (null == components)
	    return null;
	else {
	    gnu.iou.chbuf strbuf = new gnu.iou.chbuf();
	    for (int cc = 0, len = components.length, typ; cc < len; cc++){
		component = components[cc];
		typ = component.type();
		if (0 < cc)
		    strbuf.append(' ');
		switch(typ){
		case TYPE_SCHEME:
		    strbuf.append("[scheme ");
		    strbuf.append(component.testString());
		    strbuf.append(']');
		    break;
		case TYPE_HOST:
		    strbuf.append("[host ");
		    strbuf.append(component.testString());
		    strbuf.append(']');
		    break;
		case TYPE_PATH:
		    strbuf.append("[path ");
		    strbuf.append(component.testString());
		    strbuf.append(']');
		    break;
		case TYPE_INTERN:
		    strbuf.append("[intern ");
		    strbuf.append(component.testString());
		    strbuf.append(']');
		    break;
		case TYPE_QUERY:
		    strbuf.append("[query ");
		    strbuf.append(component.testString());
		    strbuf.append(']');
		    break;
		case TYPE_FRAGMENT:
		    strbuf.append("[frag ");
		    strbuf.append(component.testString());
		    strbuf.append(']');
		    break;
		case TYPE_TERMINAL:
		    strbuf.append("[terminal ");
		    strbuf.append(component.testString());
		    strbuf.append(']');
		    break;
		default:
		    throw new Parser.Exception.Bug();
		}
	    }
 	    return strbuf.toString();
	}
    }


    static void usage(java.io.PrintStream out){
	out.println();
	out.println("usage:  gnu.iou.uri file");
	out.println();
	out.println("  Plain text input 'file' contains a test vector of URIs,");
	out.println("  one per line.  Each line is parsed and output in 'test ");
	out.println("  format'.  Line comments are supported in column one with");
	out.println("  the pound '#' character. ");
	out.println();
	java.lang.System.exit(1);
    }
    /**
     * Simple command line test tool, run with no arguments for a help
     * text.
     */
    public static void main(String[] argv){
	if (null == argv)
	    usage(java.lang.System.err);
	else {
	    int argc = argv.length;
	    if (1 != argc)
		usage(java.lang.System.err);
	    else {
		java.io.File finput = new java.io.File(argv[0]);
		if (! finput.isFile())
		    usage(java.lang.System.err);
		else {
		    java.io.PrintStream err = java.lang.System.err;
		    java.io.PrintStream out = java.lang.System.out;
		    try {
			java.lang.String line;
			uri test ;
			java.io.DataInputStream fin = new java.io.DataInputStream(new java.io.FileInputStream(finput));
			while (null != (line = fin.readLine())){
			    line = line.trim();
			    if (!line.startsWith("#"))
				try {
				    test = new uri(line);
				    out.println("ok "+line+"  "+test.testString());
				}
				catch (Exception ila){
				    out.println("er "+line+"  "+bpo.atStack(ila));
				}
			}
			java.lang.System.exit(0);
		    }
		    catch (java.io.IOException iox){
			iox.printStackTrace();
			usage(err);
		    }
		}
	    }
	}
    }
}
