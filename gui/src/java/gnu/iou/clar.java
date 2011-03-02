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
package gnu.iou ;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.NoSuchElementException;

/**
 * Clar is a class file rewriter for reading a bytecode class file,
 * manipulating it, and then copying it out for loading (definition in
 * the class loader).  Clar is loosely based on Chuck McManus'
 * "ClassFile" program.
 *
 * <p> The class file constant pool contains (most notably) field and
 * method references, and declared field constant values.  A compiled
 * class can be read as a resource, renamed, have references and
 * values modified, and then can be loaded for a fast dynamic
 * reference.  
 *
 * <p> The overhead of creating this dynamic class is on order of a
 * hundred times greater than getting a method or field using Java
 * reflection, but the resulting runtime performance is on order of a
 * hundred times greater than reflected methods and fields.
 * 
 * <p> <b>Command line</b>
 * 
 * <p> This class includes a simple command line function for printing
 * basic class file data.  The perspective offered describes the class
 * file structure.  
 *
 * <p> At the top of its output, the file magic identifier and version
 * are represented.  On the following lines, Clar prints the
 * identifying attributes of the class, then its fields and methods,
 * and then the constant pool.  (In the class file, the constant pool
 * comes before the class information, after the file magic identifier
 * and version.)
 * 
 * <pre>
 * 	CAFEBABE 3.45
 * 	Class      CLASS {37 org/syntelos/pi/pif/pica}
 * 	Superclass CLASS {38 java/lang/Object}
 * 	Interface  CLASS {39 org/syntelos/pi/pif/pico}
 * 
 * 	Field {12 target_classname}
 * 
 * 	Method {15 &lt;init&gt; @{Code}}
 * 
 * 	1:	METHODREF {10 CLASS {38 java/lang/Object}}{27 NAME&amp;TYPE {15 &lt;init&gt;}{16 ()V}}
 * 	2:	STRING {28 org.syntelos.pi.pif.pica_}
 * 	3:	FIELDREF {9 CLASS {37 org/syntelos/pi/pif/pica}}{29 NAME&amp;TYPE {12 target_classname}{13 Ljava/lang/String;}}
 * 	4:	STRING {30 pica_fun}
 * 	5:	FIELDREF {9 CLASS {37 org/syntelos/pi/pif/pica}}{31 NAME&amp;TYPE {14 target_function}{13 Ljava/lang/String;}}
 * 	6:	METHODREF {32 CLASS {40 org/syntelos/pi/pif/pica_}}{33 NAME&amp;TYPE {30 pica_fun}{20 (Ljava/lang/Object;)Ljava/lang/Object;}}
 * 	7:	METHODREF {34 CLASS {41 java/lang/Class}}{35 NAME&amp;TYPE {42 forName}{43 (Ljava/lang/String;)Ljava/lang/Class;}}
 * 	8:	CLASS {36 java/lang/ClassNotFoundException}
 * 	9:	CLASS {37 org/syntelos/pi/pif/pica}
 * 	10:	CLASS {38 java/lang/Object}
 * 	11:	CLASS {39 org/syntelos/pi/pif/pico}
 * 	12:	target_classname
 * 
 * 	14:	target_function
 * 	15:	&lt;init&gt;
 * 	16:	()V
 * </pre>
 * 
 * <p> 
 * 
 * <p> <b>Usage</b>
 * 
 * <p> Without using Clar, a dynamic function invocation is done using
 * the reflection API, like so.
 * 
 * <pre>
 *   Class cla = Class.forName(clnam)
 *   Method invoker = cla.getDeclaredMethod(metnam,argtypes)
 * </pre>
 * 
 * <p> Using Clar, a class with a generic invocation method invoking a
 * dummy target class and function are defined.  At runtime, the
 * generic class is read as a resource, and the dummy reference
 * replaced with a dynamically configured reference.
 * 
 * <pre>
 *  package foo.bar
 * 
 *  public class generic {
 *
 *    public Object invoke ( Object arg){
 *      return dummy.invoke(arg)
 *    }
 *  }
 *
 *  public class dummy {
 *
 *    public Object invoke ( Object arg){
 *      return null
 *    }
 *  }
 * </pre>
 * 
 * <p> The following is an outline of the method adaptation process
 * using Clar.
 * 
 * <pre>
 *   ClassLoader cl = obj.getClass().getClassLoader()
 *   InputStream rin = cl.getResourceAsStream('/foo/bar/dummy.class')
 * 
 *   clar clr = new clar(rin)
 *   clr.subClass('generated.a123')
 *   clr.renameMethodRef('foo.bar.dummy.invoke','com.pack.cla.funName')
 *   byte[] classfile = clr.write()
 * 
 *   cl.defineClass( classfile)
 * 
 *   Class gencla = Class.forName('generated.a123')
 *   method invoker = gencla.newInstance()
 * </pre>
 * 
 * <p> If the method will only be invoked once or twice, the
 * reflection approach is certainly much more efficient.  Likewise, an
 * infrequently called method benefits very little from a fast
 * invocation.
 * 
 * @author John Pritchard 
 */
public class clar {

    public static final byte TYPE_UTF8        =  1;
    public static final byte TYPE_INTEGER     =  3;
    public static final byte TYPE_FLOAT       =  4;
    public static final byte TYPE_LONG        =  5;
    public static final byte TYPE_DOUBLE      =  6;
    public static final byte TYPE_CLASS       =  7;
    public static final byte TYPE_STRING      =  8;
    public static final byte TYPE_FIELDREF    =  9;
    public static final byte TYPE_METHODREF   = 10;
    public static final byte TYPE_IFMETREF    = 11;
    public static final byte TYPE_NAMEANDTYPE = 12;

    public static final String TYPENAME_CLASS       = "CLASS";
    public static final String TYPENAME_FIELDREF    = "FIELDREF";
    public static final String TYPENAME_METHODREF   = "METHODREF";
    public static final String TYPENAME_IFMETREF    = "IFMETREF";
    public static final String TYPENAME_NAMEANDTYPE = "NAME&TYPE";
    public static final String TYPENAME_STRING      = "STRING";

    public static final short ACC_PUBLIC        = 0x0001;
    public static final short ACC_PRIVATE       = 0x0002;
    public static final short ACC_PROTECTED     = 0x0004;
    public static final short ACC_STATIC        = 0x0008;
    public static final short ACC_FINAL         = 0x0010;
    public static final short ACC_SYNCHRONIZED  = 0x0020;
    public static final short ACC_NATIVE        = 0x0100;
    public static final short ACC_ABSTRACT      = 0x0400;
    public static final short ACC_STRICT        = 0x0800;

  //

    private final static short BEshort(byte a[]){

	int a0 = ((a[0])<<8);

	int a1 = a[1];

	return (short)(a0|a1);
    }

  //

    /**
     * Tree node for the class data.
     */
    public static class cpinfo {
	
	protected int type = -1;

	protected short arg1_idx = -1, arg2_idx = -1;

	protected String strValue = null;

	protected int intValue;
	protected long longValue;
	protected float floatValue;
	protected double doubleValue;
	
	public cpinfo(){}

	public cpinfo arg1( cpinfo[] pool){
	    return pool[arg1_idx];
	}

	public cpinfo arg2( cpinfo[] pool){
	    return pool[arg2_idx];
	}

	public void read(DataInputStream din) throws IOException {

	    type = din.readByte(); // CP info tag

	    switch (type){
	    case TYPE_UTF8:
		chbuf strbuf = new chbuf();

		int len = din.readShort();

		while (len-- > 0)
		    strbuf.append((char)din.readByte());

		strValue = strbuf.toString();
		break;
	    case TYPE_INTEGER:
		intValue = din.readInt();
		break;
	    case TYPE_FLOAT:
		floatValue = din.readFloat();
		break;
	    case TYPE_LONG:
		longValue = din.readLong();
		break;
	    case TYPE_DOUBLE:
		doubleValue = din.readDouble();
		break;
	    case TYPE_CLASS:
		arg1_idx = din.readShort();
		break;
	    case TYPE_STRING:
		arg1_idx = din.readShort();
		break;
	    case TYPE_FIELDREF:
		arg1_idx = din.readShort();
		arg2_idx = din.readShort();
		break;
	    case TYPE_METHODREF:
		arg1_idx = din.readShort();
		arg2_idx = din.readShort();
		break;
	    case TYPE_IFMETREF:
		arg1_idx = din.readShort();
		arg2_idx = din.readShort();
		break;
	    case TYPE_NAMEANDTYPE:
		arg1_idx = din.readShort();
		arg2_idx = din.readShort();
		break;
	    default:
		throw new IOException("Bad type `0x"+Integer.toHexString(type)+"'.");
	    }
	}
	public void write(DataOutputStream dout) throws IOException {
	    dout.write(type);
	    
	    switch (type){
	    case TYPE_UTF8:
		dout.writeShort(strValue.length());
		dout.writeBytes(strValue);
		break;
	    case TYPE_INTEGER:
		dout.writeInt(intValue);
		break;
	    case TYPE_FLOAT:
		dout.writeFloat(floatValue);
		break;
	    case TYPE_LONG:
		dout.writeLong(longValue);
		break;
	    case TYPE_DOUBLE:
		dout.writeDouble(doubleValue);
		break;
	    case TYPE_CLASS:
	    case TYPE_STRING:
		dout.writeShort( arg1_idx);
		break;
	    case TYPE_FIELDREF:
	    case TYPE_METHODREF:
	    case TYPE_IFMETREF:
	    case TYPE_NAMEANDTYPE:
		dout.writeShort( arg1_idx);
		dout.writeShort( arg2_idx);
		break;
	    default:
		throw new IOException("Bad type `0x"+Integer.toHexString(type)+"'.");
	    }
	}
	public String toString(){

	    String name = null;

	    switch(type){
		
	    case TYPE_UTF8:
		return strValue;
		
	    case TYPE_INTEGER:
		return Integer.toString(intValue);
		
	    case TYPE_LONG:
		return Long.toString(longValue);
		
	    case TYPE_FLOAT:
		return Float.toString(floatValue);
		
	    case TYPE_DOUBLE:
		return Double.toString(doubleValue);
		
	    case TYPE_CLASS:
		name = TYPENAME_CLASS;
		break;
	    case TYPE_STRING:
		name = TYPENAME_STRING;
		break;
	    case TYPE_FIELDREF:
		name = TYPENAME_FIELDREF;
		break;
	    case TYPE_METHODREF:
		name = TYPENAME_METHODREF;
		break;
	    case TYPE_IFMETREF:
		name = TYPENAME_IFMETREF;
		break;
	    case TYPE_NAMEANDTYPE:
		name = TYPENAME_NAMEANDTYPE;
		break;
	    }
		
	    chbuf strbuf = new chbuf();
		
	    strbuf.append(name);
	    strbuf.append(' ');

	    if ( -1 < arg1_idx){
		
		strbuf.append('{');
		strbuf.append(Integer.toString(arg1_idx));
		strbuf.append('}');
		
		if ( -1 < arg2_idx){
			strbuf.append('{');
			strbuf.append(Integer.toString(arg2_idx));
			strbuf.append('}');
		}
	    }
	    return strbuf.toString();
	}
	public String toString( cpinfo[] pool){

	    String name = null;

	    switch(type){
		
	    case TYPE_UTF8:
		return strValue;
		
	    case TYPE_INTEGER:
		return Integer.toString(intValue);
		
	    case TYPE_LONG:
		return Long.toString(longValue);
		
	    case TYPE_FLOAT:
		return Float.toString(floatValue);
		
	    case TYPE_DOUBLE:
		return Double.toString(doubleValue);
		
	    case TYPE_CLASS:
		name = TYPENAME_CLASS;
		break;
	    case TYPE_STRING:
		name = TYPENAME_STRING;
		break;
	    case TYPE_FIELDREF:
		name = TYPENAME_FIELDREF;
		break;
	    case TYPE_METHODREF:
		name = TYPENAME_METHODREF;
		break;
	    case TYPE_IFMETREF:
		name = TYPENAME_IFMETREF;
		break;
	    case TYPE_NAMEANDTYPE:
		name = TYPENAME_NAMEANDTYPE;
		break;
	    }
		
	    chbuf strbuf = new chbuf();
		
	    strbuf.append(name);
	    strbuf.append(' ');

	    if ( -1 < arg1_idx){
		
		strbuf.append('{');
		strbuf.append(arg1_idx);
		strbuf.append(' ');
		strbuf.append(pool[arg1_idx].toString(pool));
		strbuf.append('}');
		
		if ( -1 < arg2_idx){
			strbuf.append('{');
			strbuf.append(arg2_idx);
			strbuf.append(' ');
			strbuf.append(pool[arg2_idx].toString(pool));
			strbuf.append('}');
		}
	    }
	    return strbuf.toString();
	}
	public boolean equals(cpinfo cp){
	    
	    if (cp == null)
		
		return false;
	    
	    else if (cp.type != type)
		
		return false;
	    
	    else {
		switch (cp.type){
		case TYPE_UTF8:
		    return cp.strValue.equals(strValue);
		case TYPE_INTEGER:
		    return (cp.intValue == intValue);
		case TYPE_FLOAT:
		    return (cp.floatValue == floatValue);
		case TYPE_LONG:
		    return (cp.longValue == longValue);
		case TYPE_DOUBLE:
		    return (cp.doubleValue == doubleValue);
		case TYPE_CLASS:
		case TYPE_STRING:
		    return (arg1_idx == cp.arg1_idx);
		case TYPE_FIELDREF:
		case TYPE_METHODREF:
		case TYPE_IFMETREF:
		case TYPE_NAMEANDTYPE:
		    return ((arg1_idx == cp.arg1_idx) && (arg2_idx == cp.arg2_idx));
		}
		
		return false;
	    }
	}
	public cpinfo indexOf( cpinfo pool[]){

	    cpinfo cpi;

	    for (int cc = 1; cc < pool.length; cc++){

		cpi = pool[cc];

		if (equals(cpi))
		    return cpi;
	    }
	    return null;
	}
      }

  //

    /**
     * Attribute info
     */
    public static class atti {

	protected short name_idx ;

	protected byte[] data = null; 

	public atti(){}

	public cpinfo getName( cpinfo pool[]){
	    return pool[name_idx];
	}

	public boolean booleanValue(cpinfo pool[]){

	    cpinfo cpi = pool[BEshort(data)];

	    if ( 0 == cpi.intValue)
		return false;
	    else
		return true;
	}
	public cpinfo cpValue(cpinfo pool[]){

	    return pool[BEshort(data)];
	}
	public void read(DataInputStream din) throws IOException {

	    name_idx = din.readShort(); 

	    int len = din.readInt(), read;

	    data = new byte[len];

	    if (len != (read = din.read(data,0,len)))
		throw new IOException("Classfile truncated ("+read+"/"+len+").");
	}
	public void write(DataOutputStream dout) throws IOException, NoSuchElementException {

	    dout.writeShort( name_idx);

	    dout.writeInt(data.length);

	    dout.write(data, 0, data.length);
	}
	public String toString(){
	    return "@{"+name_idx+"}";
	}
	public String toString( cpinfo[] pool){
	    return "@{"+pool[name_idx].toString(pool)+"}";
	}
    }

  //

    /**
     * Field info
     */
    public static class fldi {
	protected short access_flags;

	protected short name_idx;

	protected short signature_idx;

	protected atti attributes[];

	public fldi(){}

	public cpinfo getName( cpinfo[] pool){
	    return pool[name_idx];
	}

	public cpinfo getSignature( cpinfo[] pool){
	    return pool[signature_idx];
	}

	public void read(DataInputStream din) throws IOException {
	    int count;

	    access_flags = din.readShort();

	    name_idx = din.readShort();

	    signature_idx = din.readShort();

	    count = din.readShort();

	    if (count != 0){

		attributes = new atti[count];

		atti at;

		for (int cc = 0; cc < count; cc++){

		    at = new atti();

		    attributes[cc] = at;

		    at.read(din);
		}
	    }
	}
	public void write(DataOutputStream dout) throws IOException {

	    dout.writeShort( access_flags);

	    dout.writeShort( name_idx);

	    dout.writeShort( signature_idx);

	    int count = (null == attributes)?(0):(attributes.length);

	    dout.writeShort(count);

	    if ( 0 < count){

		for ( int cc = 0; cc < count; cc++)

		    attributes[cc].write(dout);
	    }
	}
	public String toString(){
	    chbuf strbuf = new chbuf();
	    strbuf.append("Field {");
	    strbuf.append(name_idx);
	    if ( null != attributes){
		int len = attributes.length;

		for ( int cc = 0; cc < len; cc++){
		    strbuf.append(' ');
		    strbuf.append(attributes[cc].toString());
		}
	    }
	    strbuf.append('}');
	    return strbuf.toString();
	}
	public String toString( cpinfo[] pool){
	    chbuf strbuf = new chbuf();
	    strbuf.append("Field {");
	    strbuf.append(name_idx);
	    strbuf.append(' ');
	    strbuf.append(pool[name_idx].toString(pool));
	    if ( null != attributes){
		int len = attributes.length;

		for ( int cc = 0; cc < len; cc++){
		    strbuf.append(' ');
		    strbuf.append(attributes[cc].toString(pool));
		}
	    }
	    strbuf.append('}');
	    return strbuf.toString();
	}
    }

  //

    /**
     * Method info
     */
    public static class meti {

	protected short access_flags;

	protected short name_idx ;

	protected short signature_idx ;

	protected atti attributes[];

	public meti(){}

	public cpinfo getName( cpinfo pool[]){
	    return pool[name_idx];
	}

	public cpinfo getSignature( cpinfo pool[]){
	    return pool[signature_idx];
	}

	public void read(DataInputStream din) throws IOException {

	    access_flags = din.readShort();

	    name_idx = din.readShort();

	    signature_idx = din.readShort();

	    int count = din.readShort();

	    if (0 < count){

		attributes = new atti[count];

		atti at;

		for (int cc = 0; cc < count; cc++){

		    at = new atti(); // function bytecode 

		    attributes[cc] = at;

		    at.read(din);
		}
	    }
	}
	public void write(DataOutputStream dout) throws IOException {
	    dout.writeShort(access_flags);

	    dout.writeShort( name_idx);

	    dout.writeShort( signature_idx);

	    int count = (null == attributes)?(0):(attributes.length);

	    dout.writeShort(count);

	    if ( 0 < count){

		for ( int cc = 0; cc < count; cc++)

		    attributes[cc].write(dout);
	    }
	}
	public String toString(){
	    chbuf strbuf = new chbuf();
	    strbuf.append("Method {");
	    strbuf.append(name_idx);
	    if ( null != attributes){
		int len = attributes.length;

		for ( int cc = 0; cc < len; cc++){
		    strbuf.append(' ');
		    strbuf.append(attributes[cc].toString());
		}
	    }
	    strbuf.append('}');
	    return strbuf.toString();
	}
	public String toString( cpinfo[] pool){
	    chbuf strbuf = new chbuf();
	    strbuf.append("Method {");
	    strbuf.append(name_idx);
	    strbuf.append(' ');
	    strbuf.append(pool[name_idx].toString(pool));
	    if ( null != attributes){
		int len = attributes.length;

		for ( int cc = 0; cc < len; cc++){
		    strbuf.append(' ');
		    strbuf.append(attributes[cc].toString(pool));
		}
	    }
	    strbuf.append('}');
	    return strbuf.toString();
	}
    }

  // class file 

    private int magic; // 0xCAFEBABE

    private short version_major;

    private short version_minor;

    private cpinfo[] constant_pool = null;

    private short access_flags;

    private short class_this_idx;

    private short class_super_idx;

    private short[] interface_idxs = null;

    private fldi[] fields = null;

    private meti[] methods = null;

    private atti[] attributes = null;

    private boolean _ok = false;

    public clar(){
	super();
    }

    /**
     * Read a class file from the input stream
     * 
     * @param in Class file
     *
     * @exception IOException Bad classfile format.
     */
    public clar( InputStream in) throws IOException {
	super();
	read(in);
    }

    /**
     * Read a class file from the input stream
     * 
     * @param in Class file
     *
     * @exception IOException Bad classfile format.
     */
    public clar( byte[] in) throws IOException {
	super();
	read(new DataInputStream(new ByteArrayInputStream(in)));
    }

    /**
     * Read the named class as a resource.
     *
     * @param rn Resource name for class file, eg, relative to this
     * package: <tt>`pica.class'</tt>; or absolute:
     * <tt>`/tld/dom/pkg/pic2.class'</tt>.
     * 
     * @exception IllegalArgumentException For null argument, or
     * missing class binary, or bad classfile format.
     */
    public clar ( String rn){
	super();
	if ( null == rn)
	    throw new IllegalArgumentException("Null resource-name argument to `clar' constructor.");
	else {

	    InputStream rin = null;

	    try {
		Class cla = getClass();

		rin = cla.getResourceAsStream(rn);

		read(rin);
	    }
	    catch ( IllegalArgumentException ilarg){
		throw new IllegalArgumentException("Class file resource not found ("+rn+").");
	    }
	    catch ( NullPointerException npx){
		throw new IllegalArgumentException("Class file not found ("+rn+").");
	    }
	    catch ( IOException iox){
		iox.printStackTrace();
		throw new IllegalArgumentException("Class file format error in ("+rn+") is ("+iox.getMessage()+").");
	    }
	    finally {
		if ( null != rin) try{rin.close();}catch(IOException iox){}
	    }
	}
    }

  //

    /**
     * Class name
     */
    public String getName(){
    	return constant_pool[cpClass().arg1_idx].strValue.replace('/','.');
    }

    /**
     * Super class name
     */
    public String getNameSuper(){
    	return constant_pool[cpSuperClass().arg1_idx].strValue.replace('/','.');
    }

    /**
     * Rename this class
     */
    public void renameClass ( String classname){
	constant_pool[cpClass().arg1_idx].strValue = classname.replace('.','/');
    }

    /**
     * Rename super class
     */
    public void renameSuper ( String classname){
	constant_pool[cpSuperClass().arg1_idx].strValue = classname.replace('.','/');
    }

    /**
     * Rename class as a subclass of the current class.
     */
    public void subClass ( String classname){

	cpinfo cla = cpClass(), supcla = cpSuperClass();

	constant_pool[supcla.arg1_idx].strValue = constant_pool[cla.arg1_idx].strValue;

	constant_pool[cla.arg1_idx].strValue = classname.replace('.','/');

    }

    // "rename" lookup consts
    private final static int REN_FROM_CN = 1;
    private final static int REN_FROM_CR = 2;
    private final static int REN_FROM_MN = 3;
    private final static Integer REN_FROM_CN_I = new Integer(REN_FROM_CN);
    private final static Integer REN_FROM_CR_I = new Integer(REN_FROM_CR);
    private final static Integer REN_FROM_MN_I = new Integer(REN_FROM_MN);

  // 

    /**
     * For two methods of identical type signatures ("from" and "to"),
     * redirect the invocation of "from" to the invocation of "to".
     * 
     * <pre>
     *  from = "org.syntelos.pi.pif.pica_.pica_fun"
     *  to   = "net.pico.brazil.picoFun"
     *
     *  For:
     *
     *    package org.syntelos.pi
     *    class pica_ { 
     *      public static Object pica_fun ( Object arg)
     *    }
     *
     *  And:
     *
     *    package net.pico
     *    class brazil { 
     *      public static Object picoFun ( Object arg)
     *    } 
     * </pre>
     * 
     * <p> When either method name parameter ("from" or "to") is not
     * qualified with a class name, <i>this</i> class name is used as a
     * default.  Valid unqualified method names include both
     * ".methodname" and "methodname".
     * 
     * @param from Fully- qualified class- method name
     * 
     * @param to Fully- qualified class- method name 
     * 
     * @exception NoSuchElementException For method reference "from" not found.  
     * 
     * @returns Number of principal constants modified.  */
    public int renameMethodRef ( String from, String to){

	cpinfo metrefs[] = cpMethodRefs();

	if ( null == metrefs)
	    return 0;
	else {
	    Hashtable lookup = new Hashtable();

	    String from_cn, from_cnr, from_mn,
		to_cn, to_cnr, to_mn;

	    int ix = from.lastIndexOf('.');

	    if ( 0 > ix){
		from = getName()+"."+from;
		ix = from.lastIndexOf('.');
	    }
	    else if ( 0 == ix){
		from = getName()+from;
		ix = from.lastIndexOf('.');
	    }

	    from_cn = from.substring(0,ix);
	    from_cnr = from_cn.replace('.','/');
	    from_mn = from.substring(ix+1);

	    lookup.put( from_cn, REN_FROM_CN_I);
	    lookup.put( from_cnr, REN_FROM_CR_I);
	    lookup.put( from_mn, REN_FROM_MN_I);


	    ix = to.lastIndexOf('.');

	    if ( 0 > ix){
		to = getName()+"."+to;
		ix = to.lastIndexOf('.');
	    }
	    else if ( 0 == ix){
		to = getName()+to;
		ix = to.lastIndexOf('.');
	    }

	    to_cn = to.substring(0,ix);
	    to_cnr = to_cn.replace('.','/');
	    to_mn = to.substring(ix+1);

	    String mr_cn, mr_mn;

	    cpinfo metref, mrcla, mrfld, pool[] = constant_pool;

	    int len = metrefs.length, changes = 0;

	    Integer lv;

	    for ( short cc = 0; cc < len; cc++){

		metref = metrefs[cc];

		mrcla = pool[pool[metref.arg1_idx].arg1_idx];

		mr_cn = mrcla.strValue;

		if ( null != mr_cn ){

		    mrfld = pool[pool[metref.arg2_idx].arg1_idx];

		    mr_mn = mrfld.strValue;

		    if ( null != (lv = (Integer)lookup.get(mr_cn))){
		
			switch(lv.intValue()){
		    
			case REN_FROM_CR:

			    mrcla.strValue = to_cnr;

			    changes += 1;

			    break;

			case REN_FROM_CN:

			    mrcla.strValue = to_cn;

			    changes += 1;

			    break;

			default:
			    throw new IllegalStateException("BBBUGGG in `clar.renameMethodRef' lookup types, found type ("+lv+").");
			}
		    }

		    if ( null != (lv = (Integer)lookup.get(mr_mn))){
		
			switch(lv.intValue()){
		    
			case REN_FROM_MN:

			    mrfld.strValue = to_mn;
			
			    changes += 1;

			    break;

			default:
			    throw new IllegalStateException("BBBUGGG in `clar.renameMethodRef' lookup types, found type ("+lv+").");
			}
		    }
		}
	    }

	    if ( 1 > changes)
		throw new NoSuchElementException("Method reference ("+from+") not found.");
	    else
		return changes;
	}
    }

  // 

    /**
     * For two fields of identical types, redirect the reference of
     * "from" to the reference of "to".
     * 
     * <p> When either field name parameter ("from" or "to") is not
     * qualified with a class name, this class name is used as a
     * default.  Valid unqualified field names include both
     * ".fieldname" and "fieldname".
     * 
     * @param from Fully- qualified class- field name, eg,
     * <tt>"org.syntelos.pi.pif.clar.constant_pool"</tt>.
     * 
     * @param to Fully- qualified class- field name 
     * 
     * @exception NoSuchElementException For field reference "from" not found.  
     * 
     * @returns Number of principal constants modified.  */
    public int renameFieldRef ( String from, String to){

	cpinfo fldrefs[] = cpFieldRefs();

	if ( null == fldrefs)
	    return 0;
	else {
	    Hashtable lookup = new Hashtable();

	    String from_cn, from_cnr, from_fn,
		to_cn, to_cnr, to_fn;

	    int ix = from.lastIndexOf('.');

	    if ( 0 > ix){
		from = getName()+"."+from;
		ix = from.lastIndexOf('.');
	    }
	    else if ( 0 == ix){
		from = getName()+from;
		ix = from.lastIndexOf('.');
	    }

	    from_cn = from.substring(0,ix);
	    from_cnr = from_cn.replace('.','/');
	    from_fn = from.substring(ix+1);

	    lookup.put( from_cn, REN_FROM_CN_I);
	    lookup.put( from_cnr, REN_FROM_CR_I);
	    lookup.put( from_fn, REN_FROM_MN_I);


	    ix = to.lastIndexOf('.');

	    if ( 0 > ix){
		to = getName()+"."+to;
		ix = to.lastIndexOf('.');
	    }
	    else if ( 0 == ix){
		to = getName()+to;
		ix = to.lastIndexOf('.');
	    }

	    to_cn = to.substring(0,ix);
	    to_cnr = to_cn.replace('.','/');
	    to_fn = to.substring(ix+1);

	    String fr_cn, fr_fn;

	    cpinfo fldref, frcla, frfld, pool[] = constant_pool;

	    int len = fldrefs.length, changes = 0;

	    Integer lv;

	    for ( short cc = 0; cc < len; cc++){

		fldref = fldrefs[cc];

		frcla = pool[pool[fldref.arg1_idx].arg1_idx];

		frfld = pool[pool[fldref.arg2_idx].arg1_idx];

		fr_cn = frcla.strValue;

		fr_fn = frfld.strValue;

		if ( null != (lv = (Integer)lookup.get(fr_cn))){
		
		    switch(lv.intValue()){
		    
		    case REN_FROM_CR:

			frcla.strValue = to_cnr;

			changes += 1;

			break;

		    case REN_FROM_CN:

			frcla.strValue = to_cn;

			changes += 1;

			break;

		    default:
			throw new IllegalStateException("BBBUGGG in `clar.renameMethodRef' lookup types, found type ("+lv+").");
		    }
		}

		if ( null != (lv = (Integer)lookup.get(fr_fn))){
		
		    switch(lv.intValue()){
		    
		    case REN_FROM_MN:
			
			frfld.strValue = to_fn;
			
			changes += 1;

			break;

		    default:
			throw new IllegalStateException("BBBUGGG in `clar.renameMethodRef' lookup types, found type ("+lv+").");
		    }
		}
	    }

	    if ( 1 > changes)
		throw new NoSuchElementException("Field reference ("+from+") not found.");
	    else
		return changes;
	}
    }

  // 

    /**
     * Replace all strings matching "from" with "to".
     * 
     * @param from Existing string value 
     * 
     * @param to Replacement (new) string value
     * 
     * @returns Number of principal constants modified, zero for no
     * mods.
     */
    public int replaceString ( String from, String to){

	cpinfo cpi, pool[] = constant_pool;

	int len = pool.length, changes = 0;

	for ( short cc = 0; cc < len; cc++){

	    cpi = pool[cc];

	    if ( TYPE_STRING == cpi.type){

		cpi = pool[cpi.arg1_idx];

		if ( from.equals(cpi.strValue)){

		    cpi.strValue = to;

		    changes += 1;
		}
	    }
	}

	return changes;
    }

    /**
     * @param mn Method name
     * 
     * @returns If the method is static 
     * 
     * @exception NoSuchElementException No method
     */
    public boolean isMethodStatic ( String mn){
	meti met = method(mn);

	if ( null == met)
	    throw new NoSuchElementException("Method ("+mn+") not found.");

	else if ( ACC_STATIC == (met.access_flags & ACC_STATIC))
	    
	    return true;
	else
	    return false;
    }

  // 

    /**
     * @returns This CLASS cpi
     */
    protected cpinfo cpClass(){
	return constant_pool[class_this_idx];
    }

    /**
     * @returns Super CLASS cpi
     */
    protected cpinfo cpSuperClass(){
	return constant_pool[class_super_idx];
    }

    /**
     * @returns METHODREF and IFMETREF cpi's
     */
    protected cpinfo[] cpMethodRefs(){

	cpinfo cpi, pool[] = constant_pool, ret[] = null, copier[];

	int poolen = pool.length, rix = 0, tt;

	for ( short cc = 0; cc < poolen; cc++){

	    cpi = pool[cc];

	    tt = cpi.type;

	    if ( TYPE_METHODREF == tt || TYPE_IFMETREF == tt){
		if ( null == ret)
		    ret = new cpinfo[1];
		else {
		    copier = new cpinfo[rix+1];
		    System.arraycopy(ret,0,copier,0,rix);
		    ret = copier;
		}
		ret[rix++] = cpi;
	    }
	}
	return ret;
    }

    /**
     * @param mn Method name
     *
     * @returns First METHODREF or IFMETREF matching the argument method name.
     */
    protected cpinfo cpMethodRef( String mn){

	cpinfo cp1, cp2, pool[] = constant_pool;

	int poolen = pool.length, tt;

	for ( short cc = 0; cc < poolen; cc++){

	    cp1 = pool[cc];

	    tt = cp1.type;

	    if ( TYPE_METHODREF == tt || TYPE_IFMETREF == tt){

		cp2 = pool[pool[cp1.arg2_idx].arg1_idx]; // name

		if ( mn.equals(cp2.strValue))
		    return cp1;
	    }
	}
	return null;
    }

    /**
     * @param mn Method name
     *
     * @returns First method matching the argument method name.
     */
    protected meti method( String mn){

	if ( null == methods)
	    return null;
	else {

	    meti met, mets[] = methods;

	    int len = mets.length;

	    for ( short cc = 0; cc < len; cc++){

		met = mets[cc];

		if ( mn.equals( met.getName(constant_pool).strValue))
		    return met;
	    }
	}
	return null;
    }

    /**
     * @returns FIELDREF cpi's
     */
    protected cpinfo[] cpFieldRefs(){

	cpinfo cpi, pool[] = constant_pool, ret[] = null, copier[];

	int poolen = pool.length, rix = 0;

	for ( short cc = 0; cc < poolen; cc++){

	    cpi = pool[cc];

	    if ( TYPE_FIELDREF == cpi.type){
		if ( null == ret)
		    ret = new cpinfo[1];
		else {
		    copier = new cpinfo[rix+1];
		    System.arraycopy(ret,0,copier,0,rix);
		    ret = copier;
		}
		ret[rix++] = cpi;
	    }
	}
	return ret;
    }

    /**
     * @param fn Field name
     *
     * @returns First FIELDREF matching the argument field name.
     */
    protected cpinfo cpFieldRef( String fn){

	cpinfo cp1, cp2, pool[] = constant_pool;

	int poolen = pool.length;

	for ( short cc = 0; cc < poolen; cc++){

	    cp1 = pool[cc];

	    if ( TYPE_FIELDREF == cp1.type){

		cp2 = pool[pool[cp1.arg2_idx].arg1_idx]; // name

		if ( fn.equals(cp2.strValue))
		    return cp1;
	    }
	}
	return null;
    }

  //

    public String toString(){
	return toString(false);
    }
    public String toString( boolean nesting){

	chbuf strbuf = new chbuf();

	strbuf.append("\tCAFEBABE ");
	strbuf.append(Integer.toString(version_major));
	strbuf.append('.');
	strbuf.append(Integer.toString(version_minor));

	strbuf.append("\n\tClass      ");
	if (nesting)
	    strbuf.append(constant_pool[class_this_idx].toString(constant_pool));
	else
	    strbuf.append(constant_pool[class_this_idx].toString());

	strbuf.append("\n\tSuperclass ");
	if (nesting)
	    strbuf.append(constant_pool[class_super_idx].toString(constant_pool));
	else
	    strbuf.append(constant_pool[class_super_idx].toString());

	if ( null != interface_idxs){

	    for ( int cc = 0; cc < interface_idxs.length; cc++){

		strbuf.append("\n\tInterface  ");
		if (nesting)
		    strbuf.append(constant_pool[interface_idxs[cc]].toString(constant_pool));
		else
		    strbuf.append(constant_pool[interface_idxs[cc]].toString());
	    }
	}
	if ( null != fields){

	    for ( int cc = 0; cc < fields.length; cc++){

		strbuf.append("\n\t");
		if (nesting)
		    strbuf.append(fields[cc].toString(constant_pool));
		else
		    strbuf.append(fields[cc].toString());
	    }
	}
	if ( null != methods){

	    for ( int cc = 0; cc < methods.length; cc++){

		strbuf.append("\n\t");
		if (nesting)
		    strbuf.append(methods[cc].toString(constant_pool));
		else
		    strbuf.append(methods[cc].toString());
	    }
	}

	strbuf.append('\n');

	return strbuf.toString();
    }

    public short indexOf( cpinfo cpi) throws NoSuchElementException {

	cpinfo[] pool = constant_pool;

	int poolen = pool.length;

	for ( short cc = 0; cc < poolen; cc++){

	    if (cpi == pool[cc])
		return cc;
	}
	throw new NoSuchElementException("CP ("+cpi+") not found.");
    }

  //

    /**
     * Read a class.
     * @returns Success or failure
     * @exception Format or I/O error
     */
    public void read(InputStream in) throws IOException {
	
	_ok = false;

	DataInputStream din;

	if (in instanceof DataInputStream)
	    din = (DataInputStream)in;
	else
	    din = new DataInputStream(in);

	/*
	 * Head
	 */
	magic = din.readInt();

	if (magic != (int) 0xCAFEBABE) throw new IOException("Input not a valid java class file.");
	
	version_major = din.readShort();

	version_minor = din.readShort();

    	int cc, count;

	/*
	 * Read constant pool
	 */
	count = din.readShort();

	cpinfo cpi;

	constant_pool = new cpinfo[count];

	constant_pool[0] = new cpinfo(); // natively indexed from `1'

	for ( cc = 1; cc < constant_pool.length; cc++){

	    constant_pool[cc] = cpi = new cpinfo();

	    cpi.read(din);

	    if (cpi.type == TYPE_LONG || cpi.type == TYPE_DOUBLE) cc++; // 2 slots
	}

	/*
	 * Flags
	 */
	access_flags = din.readShort();

	/*
	 * Identity
	 */
	class_this_idx = din.readShort();

	class_super_idx = din.readShort();

	/*
	 * Interfaces
	 */
	count = din.readShort();

	if ( 0 < count){

	    interface_idxs = new short[count];

	    for ( cc = 0; cc < count; cc++){

		interface_idxs[cc] = din.readShort();

	    }
	}

    	/*
    	 * Fields
    	 */
    	count = din.readShort();

    	if ( 0 < count){

	    fldi fi ;

	    fields = new fldi[count];

	    for ( cc = 0; cc < count; cc++){

		fields[cc] = fi = new fldi();

		fi.read(din);
	    }
	}

	/*
	 * Methods
	 */
	count = din.readShort();

	if ( 0 < count){

	    meti mi;

	    methods = new meti[count];

	    for ( cc = 0; cc < count; cc++){

		methods[cc] = mi = new meti();

		mi.read(din);
	    }
    	}

    	/*
    	 * Attributes
    	 */
    	count = din.readShort();

    	if ( 0 < count){

	    atti at;

    	    attributes = new atti[count];

    	    for ( cc = 0; cc < count; cc++){

		at = new atti();

		attributes[cc] = at;

		at.read(din);
    	    }
    	}

	_ok = true;
    }

  //

    /**
     * 
     * @returns Classfile bytes for class loader.  
     * 
     * @see #getName
     */
    public byte[] write() throws IOException {
	bbo ous = new bbo(1024);
	write(ous);
	return ous.toByteArray();
    }

    public void write(OutputStream out)	throws IOException {

	if (!_ok)
	    throw new IOException("Can't write class file, bad format.");
	else {
	    int cc, count;

	    DataOutputStream dout;

	    if ( out instanceof DataOutputStream)
		dout = (DataOutputStream)out;
	    else
		dout = new DataOutputStream(out);

	    /*
	     * Head
	     */
	    dout.writeInt(magic);
	    dout.writeShort(version_major);
	    dout.writeShort(version_minor);

	    /*
	     * Constant pool
	     */
	    count = constant_pool.length;

	    dout.writeShort(count);

	    if ( 0 < count){
		cpinfo cpi;

		for ( cc = 1; cc < count; cc++){

		    cpi = constant_pool[cc];

		    cpi.write(dout);
		}
	    }

	    /*
	     * Flags
	     */
	    dout.writeShort(access_flags);

	    /*
	     * Identity
	     */
	    dout.writeShort( class_this_idx);

	    dout.writeShort( class_super_idx);

	    /*
	     * Interfaces
	     */
	    count = (null == interface_idxs)?(0):(interface_idxs.length);

	    dout.writeShort(count);

	    if ( 0 < count){

		for ( cc = 0; cc < count; cc++){

		    dout.writeShort( interface_idxs[cc]);
		}
	    }

	    /*
	     * Fields
	     */
	    count = (null == fields)?(0):(fields.length);

	    dout.writeShort(count);

	    if ( 0 < count){

		for ( cc = 0; cc < count; cc++)
		    fields[cc].write(dout);
	    }

	    /*
	     * Methods
	     */
	    count = (null == methods)?(0):(methods.length);

	    dout.writeShort(count);
	    
	    if ( 0 < count){
		for ( cc = 0; cc < count; cc++)
		    methods[cc].write(dout);
	    }

	    /*
	     * Attributes
	     */
	    count = (null == attributes)?(0):(attributes.length);

	    dout.writeShort(count);
	    
	    if ( 0 < count){

		for ( cc = 0; cc < count; cc++)
		    attributes[cc].write(dout);
	    }
	}
    }

  //

    /**
     * @param cla Print clar
     *
     * @param out Destination
     */
    public final static void PrintCP ( clar cla, PrintStream out){
	PrintCP(cla,out,false);
    }
    /**
     * @param cla Print clar
     *
     * @param out Destination
     * 
     * @param nesting When true, print tree nodes in- line.
     */
    public final static void PrintCP ( clar cla, PrintStream out, boolean nesting){

	out.println(cla.toString(nesting));

	cpinfo cpi, cp[] = cla.constant_pool;

	int count = cp.length;

	for ( int cc = 1; cc < count; cc++){
	    cpi = cp[cc];

	    out.write('\t');

	    out.print(Integer.toString(cc));

	    out.write(':');

	    out.write('\t');

	    if (nesting)
		out.println(cpi.toString(cp));
	    else
		out.println(cpi.toString());
	}
    }

    private final static void usage ( PrintStream out){
	out.println();
	out.println(" Usage");
	out.println();
	out.println("\tclar -c classname [-i]");
	out.println("\tclar -f filename.class [-i]");
	out.println("\tclar [ -c ... | -f ... ] -r from to ");
	out.println();
	out.println(" Description");
	out.println();
	out.println("\tTest `Clar'.  The first two forms print the");
	out.println("\tconstant pool table for visualizing what's ");
	out.println("\tgoing on there.");
	out.println();
	out.println("\tThe third form will rename a method and class");
	out.println("\treferences as `renameMethodRef', then save a");
	out.println("\tclass file in this directory or with `file'.");
	out.println();
	out.println("\tThe `-i' option turns- on inline tree nodes ");
	out.println("\tfor printing (no `-r').");
	out.println();
    }

    /**
     * Command line test- tool.
     * 
     * <pre>
     *  Usage
     * 
     *         clar -c classname [-i]
     *         clar -f filename.class [-i]
     *         clar [ -c ... | -f ... ] -r from to 
     * 
     *  Description
     * 
     *         Test `Clar'.  The first two forms print the
     *         constant pool table for visualizing what's 
     *         going on there.
     * 
     *         The third form will rename a method and class
     *         references as `renameMethodRef', then save a
     *         class file in this directory or with `file'.
     * 
     *         The `-i' option turns- on inline tree nodes 
     *         for printing (no `-r').
     * 
     * </pre>
     */
    public final static void main ( String[] argv){
	try {
	    boolean print = false, inline = false;

	    File file = null;
	    String rsrc = null;

	    String from = null, to = null;

	    { 
		String arg;
		int alen = argv.length;

		for ( int argc = 0; argc < alen; argc++){

		    arg = argv[argc];

		    switch(arg.charAt(0)){

		    case '-':
		    case '/':
			switch(arg.charAt(1)){

			case 'h':
			case 'H':
			case '?':
			    throw new IllegalArgumentException();

			case 'i':

			    if (print)
				inline = true;
			    else
				throw new IllegalArgumentException("Option `-i' must follow a print option, one of either `-f' or `-c'.");

			    break;

			case 'f':
			    argc += 1;

			    if ( argc < alen){

				arg = argv[argc];

				file = new File(arg);

				print = true;
			    
				break;
			    }
			    else 
				throw new IllegalArgumentException("Option `-f' requires filename argument.");
			case 'c':
			    argc += 1;

			    if ( argc < alen){

				arg = argv[argc];

				rsrc = arg;

				print = true;
			    
				break;
			    }
			    else 
				throw new IllegalArgumentException("Option `-p' requires classname argument.");

			case 'r':

			    print = false;

			    argc += 1;

			    if ( argc < alen){

				from = argv[argc];

				argc += 1;

				if ( argc < alen){

				    to = argv[argc];

				    break;
				}
				else 
				    throw new IllegalArgumentException("Option `-r' requires 'to' argument.");
			    }
			    else 
				throw new IllegalArgumentException("Option `-r' requires 'from' argument.");


			default:
			    throw new IllegalArgumentException("Unrecognized option `"+arg+"'.");
			}
			break;
		    default:
			throw new IllegalArgumentException("Unrecognized option `"+arg+"'.");
		    }
		}
	    }

	    long start; 

	    clar clr = null;
	    
	    if ( null != file){

		FileInputStream fin = new FileInputStream(file);

		start = System.currentTimeMillis();

		clr = new clar(fin);

		System.err.println("Pica setup "+(System.currentTimeMillis()-start)+" ms -- using `new clar(InputStream)'.");

		fin.close();
	    }
	    else if ( null != rsrc){

		start = System.currentTimeMillis();

		clr = new clar(rsrc);

		System.err.println("Pica setup "+(System.currentTimeMillis()-start)+" ms -- using `new clar(String)'.");
	    }
	    else
		throw new IllegalArgumentException();

	    if ( print)

		PrintCP( clr, System.out, inline);
	    
	    else if ( null != from && null != to){

		String cn = to.substring(0,to.lastIndexOf('.'));

		cn = cn.substring(cn.lastIndexOf('.')+1);

		cn = "pica/t_"+cn;

		if ( null != file)
		    file = new File( file.getParent()+"/"+ cn+".class");
		else
		    file = new File( cn.replace('/','.')+".class");

		FileOutputStream fout = new FileOutputStream (file);

		start = System.currentTimeMillis();

		clr.renameMethodRef( from, to);

		clr.subClass( cn);

		System.err.println("Pica rename & subclass "+(System.currentTimeMillis()-start)+" ms.");

		start = System.currentTimeMillis();

		clr.write(fout);

		System.err.println("Pica write "+(System.currentTimeMillis()-start)+" ms.");

		System.out.println("Wrote "+file.getAbsolutePath());

		fout.close();
	    }
	    else
		throw new IllegalArgumentException();

	    System.exit(0);
	}
	catch ( IllegalArgumentException ilarg){

	    String msg = ilarg.getMessage();

	    if ( null == msg)

		usage(System.err);
	    else 
		System.err.println(msg);

	    System.exit(1);
	}
	catch ( Exception exc){

	    exc.printStackTrace();

	    System.exit(1);
	}
    }
}
