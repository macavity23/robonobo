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
 * <p> This class parses a string into a list of tokens and their
 * infix separator characters, one of '.', '/', or '$'.  The infix
 * separator character is presented as the suffix of one token or the
 * prefix of the next token in this interface that indexes tokens by
 * count from zero in string order. </p>
 * 
 * @author John Pritchard (john@syntelos.org)
 */
public class cname
    extends java.lang.Object
{
    public final static class token 
	extends java.lang.Object
    {
	protected final java.lang.String string;
	protected final char suffix;

	protected token(char[] cary, int cary_len, int start, int end){
	    super();
	    int len = (end-start);
	    if (end == cary_len)
		len -= 1;
	    //
	    this.string = new java.lang.String(cary,start,len);

	    if (end == cary_len)
		this.suffix = (char)0;
	    else
		this.suffix = cary[end];
	}

	public boolean hasSuffix(){
	    return (0 < this.suffix);
	}
	public char getSuffix(){
	    return this.suffix;
	}
	public int getLength(){
	    return this.string.length();
	}
	public java.lang.String getString(){
	    return this.string;
	}
	public java.lang.String toString(){
	    return this.string;
	}
	public int hashCode(){
	    return this.string.hashCode();
	}
	public boolean equals(java.lang.Object another){
	    if (this == another)
		return true;
	    else if (this.string == another)
		return true;
	    else if (another instanceof java.lang.String)
		return this.string.equals(another);
	    else
		return this.string.equals(another.toString());
	}
    }

    private final java.lang.String string;

    private final token[] list;

    private final int list_len;

    public cname(java.lang.String name){
	super();
	if (null == name)
	    throw new java.lang.IllegalArgumentException("Null string name");
	else {
	    this.string = name;
	    char[] cary = name.toCharArray();
	    int cary_len = cary.length;
	    if (0 < cary_len){
		char ch;
		token list[] = null, tok, copier[];
		int start = 0, end, list_len;
		for (int cc = 0; cc < cary_len; cc++){
		    ch = cary[cc];
		    switch (ch){
		    case '.':
		    case '$':
		    case '/':
			end = cc;
			tok = new token(cary,cary_len,start,end);
			if (null == list)
			    list = new token[]{tok};
			else {
			    list_len = list.length;
			    copier = new token[list_len+1];
			    java.lang.System.arraycopy(list,0,copier,0,list_len);
			    copier[list_len] = tok;
			    list = copier;
			}
			start = (cc+1);
			break;
		    default:
			break;
		    }
		}
		tok = new token(cary,cary_len,start,cary_len);
		if (null == list)
		    list = new token[]{tok};
		else {
		    list_len = list.length;
		    copier = new token[list_len+1];
		    java.lang.System.arraycopy(list,0,copier,0,list_len);
		    copier[list_len] = tok;
		    list = copier;
		}
		this.list = list;
		this.list_len = list.length;
	    }
	    else
		throw new java.lang.IllegalArgumentException("Empty string name");
	}
    }

    public final int count(){
	return this.list_len;
    }
    public final token get(int idx){
	if (-1 < idx && idx < this.list_len)
	    return this.list[idx];
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final int countTokens(){
	return this.list_len;
    }
    public final java.lang.String getToken(int idx){
	if (-1 < idx && idx < this.list_len)
	    return this.list[idx].getString();
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final int getTokenLength(int idx){
	if (-1 < idx && idx < this.list_len)
	    return this.list[idx].getLength();
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final char getTokenSuffix(int idx){
	if (-1 < idx && idx < this.list_len)
	    return this.list[idx].getSuffix();
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final boolean hasTokenSuffix(int idx){
	if (-1 < idx && idx < this.list_len)
	    return this.list[idx].hasSuffix();
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final char getTokenPrefix(int idx){
	if (-1 < idx && idx < this.list_len){
	    int pidx = (idx-1);
	    if (-1 < pidx)
		return this.list[pidx].getSuffix();
	    else
		return (char)0;
	}
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }
    public final boolean hasTokenPrefix(int idx){
	if (-1 < idx && idx < this.list_len){
	    int pidx = (idx-1);
	    if (-1 < pidx)
		return this.list[pidx].hasSuffix();
	    else
		return false;
	}
	else
	    throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(idx));
    }

    public final int getLength(){
	return this.string.length();
    }
    public final java.lang.String getString(){
	return this.string;
    }
    public final java.lang.String toString(){
	return this.string;
    }
    public final int hashCode(){
	return this.string.hashCode();
    }
    public final boolean equals(java.lang.Object another){
	if (this == another)
	    return true;
	else if (this.string == another)
	    return true;
	else if (another instanceof java.lang.String)
	    return this.string.equals(another);
	else
	    return this.string.equals(another.toString());
    }
}
