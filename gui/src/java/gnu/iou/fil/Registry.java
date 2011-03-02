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

package gnu.iou.fil ;

/**
 * <p> The {@link coder} registry of filters. </p>
 * 
 * @author John Pritchard (jdp@syntelos.com)
 * 
 * @see foo
 * @see fii
 */
public final class Registry
    extends gnu.iou.objmap
{
    private final static java.lang.Class ClassCoder = coder.class;

    private final static Registry Instance = new Registry();

    /**
     * <p> This proceedure replaces any (or no) existing factory for
     * the registry name with a new instance of the argument. </p>
     * 
     * @param jclass A class implementing {@link coder} 
     * 
     * @exception java.lang.IllegalArgumentException The argument is
     * not implementing {@link coder}
     * @exception java.lang.RuntimeException Caused by instantiation or
     * access exceptions from {@link java.lang.Class#newInstance()}.
     */
    public final static void Add(java.lang.Class jclass)
	throws java.lang.IllegalArgumentException,
	       java.lang.RuntimeException
    {
	if (ClassCoder.isAssignableFrom(jclass)){
	    try {
		coder factory = (coder)jclass.newInstance();
		Instance.put(factory.getName(),factory);
	    }
	    catch (java.lang.InstantiationException ins){
		java.lang.RuntimeException rex = new java.lang.IllegalStateException(jclass.getName());
		rex.initCause(ins);
		throw rex;
	    }
	    catch (java.lang.IllegalAccessException acc){
		java.lang.RuntimeException rex = new java.lang.IllegalStateException(jclass.getName());
		rex.initCause(acc);
		throw rex;
	    }
	}
	else
	    throw new java.lang.IllegalArgumentException(jclass.getName());
    }
    public final static boolean Has(java.lang.String name){
	return (null != Instance.get(name));
    }
    public final static boolean HasNot(java.lang.String name){
	return (null == Instance.get(name));
    }
    private final static coder Get(java.lang.String name){
	return (coder)Instance.get(name);
    }
    /**
     * <p> To be done: construct filter chains from compound names. </p>
     * 
     * @param name Required registered name for a {@link coder} factory
     * @param in Required constructor operand
     * @return Null for no such registered name
     * @exception java.lang.IllegalArgumentException For a null operand
     */
    public final static fii Decoder( java.lang.String name, java.io.InputStream in)
	throws java.lang.IllegalArgumentException
    {
	if (null == name || null == in)
	    throw new java.lang.IllegalArgumentException();
	else {
	    coder factory = Get(name);
	    if (null != factory)
		return factory.decoder(in);
	    else
		return null;
	}
    }
    /**
     * <p> To be done: construct filter chains from compound names. </p>
     * 
     * @param name Required registered name for a {@link coder} factory
     * @param buf Required constructor operand
     * @return Null for no such registered name
     * @exception java.lang.IllegalArgumentException For a null operand
     */
    public final static fii Decoder( java.lang.String name, gnu.iou.bbuf buf)
	throws java.lang.IllegalArgumentException
    {
	if (null == name || null == buf)
	    throw new java.lang.IllegalArgumentException();
	else {
	    coder factory = Get(name);
	    if (null != factory)
		return factory.decoder(buf);
	    else
		return null;
	}
    }
    /**
     * <p> To be done: construct filter chains from compound names. </p>
     * 
     * @param name Required registered name for a {@link coder} factory
     * @param out Required constructor operand
     * @return Null for no such registered name
     * @exception java.lang.IllegalArgumentException For a null operand
     */
    public final static foo Encoder( java.lang.String name, java.io.OutputStream out)
	throws java.lang.IllegalArgumentException
    {
	if (null == name || null == out)
	    throw new java.lang.IllegalArgumentException();
	else {
	    coder factory = Get(name);
	    if (null != factory)
		return factory.encoder(out);
	    else
		return null;
	}
    }
    /**
     * <p> To be done: construct filter chains from compound names. </p>
     * 
     * @param name Required registered name for a {@link coder} factory
     * @param buf Required constructor operand
     * @return Null for no such registered name
     * @exception java.lang.IllegalArgumentException For a null operand
     */
    public final static foo Encoder( java.lang.String name, gnu.iou.bbuf buf)
	throws java.lang.IllegalArgumentException
    {
	if (null == name || null == buf)
	    throw new java.lang.IllegalArgumentException();
	else {
	    coder factory = Get(name);
	    if (null != factory)
		return factory.encoder(buf);
	    else
		return null;
	}
    }
    /**
     * <p> Install built- in default coders (in this package), this
     * proceedure may be called any number of times.  Actual
     * registration only occurs once as the registered names are
     * checked before being added. </p>
     */
    public final static void Defaults(){
	if (HasNot(base64.NAME))
	    Add(base64.class);
	if (HasNot(oaep.NAME))
	    Add(oaep.class);
	if (HasNot(nil.NAME))
	    Add(nil.class);
    }

    static {
	Defaults();
    }
}
