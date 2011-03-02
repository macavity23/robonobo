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
 * <p> Base 64 encoder and decoder from RFC 1521, section `5.2', and
 * with CRLF line terminals.</p>
 * 
 * <p> Condensed from the public domain <code>Base64.java
 * v2.0.2</code> by Robert Harder.  Modified for fewer configuration
 * options for I/O in standard RFC 1521 Base64, for essential output-
 * encode, input- decode API.  </p>
 *
 * @author Robert Harder
 * @author John Pritchard
 */
public class b64 {
    
        
    /** Maximum line length (76) of Base64 output. 
     */
    public final static int MAX_LINE_LENGTH = 76;
    
    /** The equals sign (=) as a byte. 
     */
    public final static byte EQUALS_SIGN = (byte)'=';
    
    
    /** The new line CRLF
     */
    public final static byte[] NEW_LINE = {(byte)'\r',(byte)'\n'};
        
    
    /** The 64 valid Base64 values. 
     */
    public final static byte[] ALPHABET = {
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
        (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
        (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', 
        (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
        (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
        (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
        (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', 
        (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', 
        (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/'
    };
    
    /** 
     * Translates a Base64 value to either its 6-bit reconstruction value
     * or a negative number indicating some other meaning.
     */
    public final static byte[] DECODABET =
    {   
        -9,-9,-9,-9,-9,-9,-9,-9,-9,                 // Decimal  0 -  8
        -5,-5,                                      // Whitespace: Tab and Linefeed
        -9,-9,                                      // Decimal 11 - 12
        -5,                                         // Whitespace: Carriage Return
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 14 - 26
        -9,-9,-9,-9,-9,                             // Decimal 27 - 31
        -5,                                         // Whitespace: Space
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,              // Decimal 33 - 42
        62,                                         // Plus sign at decimal 43
        -9,-9,-9,                                   // Decimal 44 - 46
        63,                                         // Slash at decimal 47
        52,53,54,55,56,57,58,59,60,61,              // Numbers zero through nine
        -9,-9,-9,                                   // Decimal 58 - 60
        -1,                                         // Equals sign at decimal 61
        -9,-9,-9,                                      // Decimal 62 - 64
        0,1,2,3,4,5,6,7,8,9,10,11,12,13,            // Letters 'A' through 'N'
        14,15,16,17,18,19,20,21,22,23,24,25,        // Letters 'O' through 'Z'
        -9,-9,-9,-9,-9,-9,                          // Decimal 91 - 96
        26,27,28,29,30,31,32,33,34,35,36,37,38,     // Letters 'a' through 'm'
        39,40,41,42,43,44,45,46,47,48,49,50,51,     // Letters 'n' through 'z'
        -9,-9,-9,-9                                 // Decimal 123 - 126
        /*,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 127 - 139
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 140 - 152
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 153 - 165
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 166 - 178
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 179 - 191
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 192 - 204
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 205 - 217
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 218 - 230
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 231 - 243
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9         // Decimal 244 - 255 */
    };
    
    public final static byte BAD_ENCODING    = -9; // Indicates error in encoding
    public final static byte WHITE_SPACE_ENC = -5; // Indicates white space in encoding
    public final static byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in encoding


    /**
     * Encodes the first three bytes of array <var>threeBytes</var>
     * and returns a four-byte array in Base64 notation.
     *
     * @param threeBytes the array to convert
     * @return four byte array in Base64 notation.
     * @since 1.3
     */
    public final static byte[] encode3to4( byte[] threeBytes){   
        return encode3to4( threeBytes, 3 );
    }
    /**
     * Encodes up to the first three bytes of array <var>threeBytes</var>
     * and returns a four-byte array in Base64 notation.
     * The actual number of significant bytes in your array is
     * given by <var>numSigBytes</var>.
     * The array <var>threeBytes</var> needs only be as big as
     * <var>numSigBytes</var>.
     *
     * @param threeBytes the array to convert
     * @param numSigBytes the number of significant bytes in your array
     * @return four byte array in Base64 notation.
     * @since 1.3
     */
    public final static byte[] encode3to4( byte[] threeBytes, int numSigBytes){  
        byte[] dest = new byte[4];
        encode3to4( threeBytes, 0, numSigBytes, dest, 0 );
        return dest;
    }
    
     /**
     * Encodes up to the first three bytes of array <var>threeBytes</var>
     * and returns a four-byte array in Base64 notation.
     * The actual number of significant bytes in your array is
     * given by <var>numSigBytes</var>.
     * The array <var>threeBytes</var> needs only be as big as
     * <var>numSigBytes</var>.
     * Code can reuse a byte array by passing a four-byte array as <var>b4</var>.
     *
     * @param b4 A reusable byte array to reduce array instantiation
     * @param threeBytes the array to convert
     * @param numSigBytes the number of significant bytes in your array
     * @return four byte array in Base64 notation.
     * @since 1.5.1
     */
    public final static byte[] encode3to4( byte[] b4, byte[] threeBytes, int numSigBytes ){
        encode3to4( threeBytes, 0, numSigBytes, b4, 0 );
        return b4;
    }
    
    /**
     * Encodes up to three bytes of the array <var>source</var>
     * and writes the resulting four Base64 bytes to <var>destination</var>.
     * The source and destination arrays can be manipulated
     * anywhere along their length by specifying 
     * <var>srcOffset</var> and <var>destOffset</var>.
     * This method does not check to make sure your arrays
     * are large enough to accomodate <var>srcOffset</var> + 3 for
     * the <var>source</var> array or <var>destOffset</var> + 4 for
     * the <var>destination</var> array.
     * The actual number of significant bytes in your array is
     * given by <var>numSigBytes</var>.
     *
     * @param source the array to convert
     * @param srcOffset the index where conversion begins
     * @param numSigBytes the number of significant bytes in your array
     * @param destination the array to hold the conversion
     * @param destOffset the index where output will be put
     * @return the <var>destination</var> array
     * @since 1.3
     */
    public final static byte[] encode3to4( byte[] source, int srcOffset, int numSigBytes,
				      byte[] destination, int destOffset)
    {
        //           1         2         3  
        // 01234567890123456789012345678901 Bit position
        // --------000000001111111122222222 Array position from threeBytes
        // --------|    ||    ||    ||    | Six bit groups to index ALPHABET
        //          >>18  >>12  >> 6  >> 0  Right shift necessary
        //                0x3f  0x3f  0x3f  Additional AND
        
        // Create buffer with zero-padding if there are only one or two
        // significant bytes passed in the array.
        // We have to shift left 24 in order to flush out the 1's that appear
        // when Java treats a value as negative that is cast from a byte to an int.
        int inBuff =   ( numSigBytes > 0 ? ((source[ srcOffset     ] << 24) >>>  8) : 0 )
                     | ( numSigBytes > 1 ? ((source[ srcOffset + 1 ] << 24) >>> 16) : 0 )
                     | ( numSigBytes > 2 ? ((source[ srcOffset + 2 ] << 24) >>> 24) : 0 );

        switch( numSigBytes){
            case 3:
                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
                destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
                destination[ destOffset + 3 ] = ALPHABET[ (inBuff       ) & 0x3f ];
                return destination;
                
            case 2:
                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
                destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
                destination[ destOffset + 3 ] = EQUALS_SIGN;
                return destination;
                
            case 1:
                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
                destination[ destOffset + 2 ] = EQUALS_SIGN;
                destination[ destOffset + 3 ] = EQUALS_SIGN;
                return destination;
                
            default:
                return destination;
        }
    }

    /**
     * <p> Encodes a byte array into Base64 notation.</p>
     *
     * @param source The data to convert
     */
    public final static byte[] encode( byte[] source ){
	if (null == source)
	    return null;
	else
	    return encode( source, 0, source.length);
    }

    /**
     * <p> This method does not close either stream.  Reads input to
     * exhaustion, writes code to output.</p>
     * @return Number of bytes read from input
     */
    public final static int encode ( java.io.InputStream in, java.io.OutputStream out) throws java.io.IOException {
	Encoder enc = new Encoder(out);
	int read, buflen = 512, acc = 0;
	byte[] buf = new byte[buflen];
	while (0 < (read = in.read(buf,0,buflen))){
	    enc.write(buf,0,read);
	    acc += read;
	}
	enc.flush();
	return acc;
    }
    /**
     * <p>Encodes a byte array into Base64 notation.</p>
     *
     * @param source The data to convert
     * @param off Offset in array where conversion should begin
     * @param len Length of data to convert
     */
    public final static byte[] encode( byte[] source, int off, int len){
            
	int expansion = ((len * 4)/3);
	int buflen = expansion;
	buflen += (((len % 3) > 0)?(4):(0));         /*(padding)*/
	buflen += (((expansion*2)/MAX_LINE_LENGTH)); /*(crlf)*/
	byte[] outbuf = new byte[buflen];
	int srcp = 0;
	int outp = 0;
	int len2 = len - 2;
	int linelen = 0;
	while (srcp < len2){
	    encode3to4( source, (srcp+off), 3, outbuf, outp);
	    srcp += 3;
	    outp += 4;
	    linelen += 4;
	    if ( linelen == MAX_LINE_LENGTH){   
		outbuf[outp++] = NEW_LINE[0];
		outbuf[outp++] = NEW_LINE[1];
		linelen = 0;
	    }
	}
	
	if (srcp < len){
	    encode3to4( source, (srcp+off), (len-srcp), outbuf, outp);
	    outp += 4;
	}

	if (outp < outbuf.length){
	    byte[] re = new byte[outp];
	    System.arraycopy(outbuf,0,re,0,outp);
	    return re;
	}
	else
	    return outbuf;
    }
    /**
     * Decodes the first four bytes of array <var>fourBytes</var>
     * and returns an array up to three bytes long with the
     * decoded values.
     *
     * @param fourBytes the array with Base64 content
     * @return array with decoded values
     * @since 1.3
     */
    public final static byte[] decode4to3( byte[] fourBytes){
        byte[] outbuf1 = new byte[3];
        int    count    = decode4to3( fourBytes, 0, outbuf1, 0 );
        byte[] outbuf2 = new byte[ count ];
        
        for( int i = 0; i < count; i++ )
            outbuf2[i] = outbuf1[i];
        
        return outbuf2;
    }
    
    /**
     * Decodes four bytes from array <var>source</var>
     * and writes the resulting bytes (up to three of them)
     * to <var>destination</var>.
     * The source and destination arrays can be manipulated
     * anywhere along their length by specifying 
     * <var>srcOffset</var> and <var>destOffset</var>.
     * This method does not check to make sure your arrays
     * are large enough to accomodate <var>srcOffset</var> + 4 for
     * the <var>source</var> array or <var>destOffset</var> + 3 for
     * the <var>destination</var> array.
     * This method returns the actual number of bytes that 
     * were converted from the Base64 encoding.
     * 
     *
     * @param source the array to convert
     * @param srcOffset the index where conversion begins
     * @param destination the array to hold the conversion
     * @param destOffset the index where output will be put
     * @return the number of decoded bytes converted
     */
    public final static int decode4to3( byte[] source, int srcOffset, byte[] destination, int destOffset )
    {
        // Example: "Dk=="
        if( source[ srcOffset + 2] == EQUALS_SIGN )
        {
            // Two ways to do the same thing. Don't know which way I like best.
            //int outbuf =   ( ( DECODABET[ source[ srcOffset    ] ] << 24 ) >>>  6 )
            //              | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
            int outbuf =   ( ( DECODABET[ source[ srcOffset    ] ] & 0xFF ) << 18 )
                          | ( ( DECODABET[ source[ srcOffset + 1] ] & 0xFF ) << 12 );
            
            destination[ destOffset ] = (byte)( outbuf >>> 16 );
            return 1;
        }
        
        // Example: "DkL="
        else if( source[ srcOffset + 3 ] == EQUALS_SIGN )
        {
            // Two ways to do the same thing. Don't know which way I like best.
            //int outbuf =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
            //              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            //              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
            int outbuf =   ( ( DECODABET[ source[ srcOffset     ] ] & 0xFF ) << 18 )
                          | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
                          | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6 );
            
            destination[ destOffset     ] = (byte)( outbuf >>> 16 );
            destination[ destOffset + 1 ] = (byte)( outbuf >>>  8 );
            return 2;
        }
        
        // Example: "DkLE"
        else
        {
            try{
            // Two ways to do the same thing. Don't know which way I like best.
            //int outbuf =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
            //              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            //              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
            //              | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
            int outbuf =   ( ( DECODABET[ source[ srcOffset     ] ] & 0xFF ) << 18 )
                          | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
                          | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6)
                          | ( ( DECODABET[ source[ srcOffset + 3 ] ] & 0xFF )      );

            
            destination[ destOffset     ] = (byte)( outbuf >> 16 );
            destination[ destOffset + 1 ] = (byte)( outbuf >>  8 );
            destination[ destOffset + 2 ] = (byte)( outbuf       );

            return 3;
            }catch( Exception e){
                System.out.println(""+source[srcOffset]+ ": " + ( DECODABET[ source[ srcOffset     ] ]  ) );
                System.out.println(""+source[srcOffset+1]+  ": " + ( DECODABET[ source[ srcOffset + 1 ] ]  ) );
                System.out.println(""+source[srcOffset+2]+  ": " + ( DECODABET[ source[ srcOffset + 2 ] ]  ) );
                System.out.println(""+source[srcOffset+3]+  ": " + ( DECODABET[ source[ srcOffset + 3 ] ]  ) );
                return -1;
            }
        }
    }

    /**
     * <p> This method does not close either stream.  Reads input to
     * exhaustion, writes plain text to output.</p>
     * @return Number of bytes written to output
     */
    public final static int decode ( java.io.InputStream in, java.io.OutputStream out) throws java.io.IOException {
	Decoder dec = new Decoder(in);
	int read, buflen = 512, acc = 0;
	byte[] buf = new byte[buflen];
	while (0 < (read = dec.read(buf,0,buflen))){
	    acc += read;
	    out.write(buf,0,read);
	}
	return acc;
    }
    
    public final static byte[] decode( byte[] bytes){
	if (null == bytes)
	    return null;
	else
	    return decode(bytes,0,bytes.length);
    }
    
    /**
     * <p> Decode Base64 text.</p>
     *
     * @param source The Base64 encoded data
     * @param off    The offset of where to begin decoding
     * @param len    The length of characters to decode
     * @return Decoded data
     */
    public final static byte[] decode( byte[] source, int off, int len ){
        int    len34   = len * 3 / 4;
        byte[] outbuf = new byte[ len34 ]; // Upper limit on size of output
        int    outbufPosn = 0;
        
        byte[] b4        = new byte[4];
        int    b4Posn    = 0;
        int    i         = 0;
        byte   sbiCrop   = 0;
        byte   sbiDecode = 0;
        for( i = off; i < off+len; i++ )
        {
            sbiCrop = (byte)(source[i] & 0x7f); // Only the low seven bits
            sbiDecode = DECODABET[ sbiCrop ];
            
            if ( sbiDecode >= WHITE_SPACE_ENC ){ // White space, Equals sign or better
                if ( sbiDecode >= EQUALS_SIGN_ENC ){
                    b4[ b4Posn++ ] = sbiCrop;
                    if ( b4Posn > 3 ){
                        outbufPosn += decode4to3( b4, 0, outbuf, outbufPosn );
                        b4Posn = 0;
                        if ( sbiCrop == EQUALS_SIGN )
                            break;
                    }
                }
            }
            else
                throw new IllegalStateException( "Byte value 0x" + Integer.toHexString(source[i])+" at offset " + i );
        }
        byte[] out = new byte[ outbufPosn ];
        System.arraycopy( outbuf, 0, out, 0, outbufPosn ); 
        return out;
    }

    
    /**
     * <p> Decode Base64 text on the fly.</p>
     *
     * @see b64
     * @author Robert Harder
     * @author John Pritchard
     */
    public static class Decoder extends java.io.FilterInputStream {

        private final static int bufferLength = 3;

        private int position = -1;
        private byte[] buffer = new byte[bufferLength];
        private int numSigBytes;
        
        /**
         */
        public Decoder( java.io.InputStream in){   
            super(in);
        }
        
        /**
         * <p> Reads enough of the input stream to convert
         * from Base64 and returns the next byte.</p>
         */
        public int read() throws java.io.IOException { 
            if ( 0 > this.position){
		byte[] b4 = new byte[4];
		int cc = 0;
		for ( cc = 0; cc < 4; cc++ ){
		    // Read four "meaningful" bytes
		    int b = 0;
		    do {
			b = this.in.read(); 
		    }
		    while( b >= 0 && DECODABET[ b & 0x7f ] <= WHITE_SPACE_ENC );
		    
		    if ( b < 0 )
			break;
		    else
			b4[cc] = (byte)b;
		}
		
		if ( cc == 4 ){
		    this.numSigBytes = decode4to3( b4, 0, this.buffer, 0);
		    this.position = 0;
		}
		else if (0 == cc)
		    return -1;
		else 
		    throw new java.io.IOException( "Improperly padded Base64 input." );
            }
	    //
            if ( -1 < this.position){
                if ( this.numSigBytes <= this.position)
                    return -1;
                else {
                    int b = this.buffer[this.position++];
                    if ( this.position >= this.bufferLength )
                        this.position = -1;
                    return (b & 0xff);
                }
            }
            else
                throw new java.io.IOException( "Error in Base64 code reading stream." );
        }
        
        
        /**
         * <p> Calls {@link #read()} repeatedly until the end of
         * stream is reached or <var>len</var> bytes are read.</p>
         *
         * @param dest array to hold values
         * @param off offset for array
         * @param len max number of bytes to read into array
         * @return bytes read into array or -1 if end of stream is encountered.
         */
        public int read( byte[] dest, int off, int len ) throws java.io.IOException {
            int cc;
            int ch;
            for( cc = 0; cc < len; cc++ ){
                ch = read();
                if(-1 < ch)
                    dest[off+cc] = (byte)ch;
                else if (0 == cc)
                    return -1;
                else
                    break;
            }
            return cc;
        }
    }
    
    /**
     * <p> Encode Base64 text on the fly.</p>
     *
     * @see b64
     * @author Robert Harder
     * @author John Pritchard
     */
    public static class Encoder extends java.io.FilterOutputStream {

        private final static int bufferLength = 3;

        private int     position = 0;
        private byte[]  buffer = new byte[ bufferLength ];
        private int     linelen = 0;
        private byte[]  b4 = new byte[4];

	/**
	 */
        public Encoder( java.io.OutputStream out){   
            super(out);
        }
        /**
         * <p> Three bytes are buffered for encoding, before the
         * target stream actually gets a <code>write()</code>
         * call.</p>
         */
        public void write(int bb) throws java.io.IOException {
	    this.buffer[this.position++] = (byte)bb;
	    if ( bufferLength <= this.position){
		this.out.write( encode3to4( this.b4, this.buffer, bufferLength));
		this.linelen += 4;
		if( linelen >= MAX_LINE_LENGTH ){
		    this.out.write( NEW_LINE );
		    this.linelen = 0;
		}
		this.position = 0;
	    }
	}
        /**
         * <p> Calls {@link #write(int)} repeatedly until
         * <var>len</var> bytes are written.</p>
         */
        public void write( byte[] bbs, int off, int len ) throws java.io.IOException {
            for( int cc = 0; cc < len; cc++)
                this.write( bbs[off+cc]);
        }        
        /**
         * <p> Pads the buffer without closing the stream.</p>
         */
        public void flush() throws java.io.IOException {
            if (0 < this.position){
		this.out.write( encode3to4( this.b4, this.buffer, this.position));
		this.position = 0;
	    }
	    super.flush();
        }
    }

  //

    private final static void usage( java.io.PrintStream out){
	out.println();
	out.println(" Usage: b64 (-e|-d)");
	out.println();
	out.println("\tEncode or Decode stdin to stdout.");
	out.println();
	System.exit(1);
    }
    public static void main( String[] argv){
	try {
	    boolean fwd = true;

	    if ( null != argv){
		int alen = argv.length;
		if ( 0 < alen){
		    String arg;
		    int arglen, ch;
		    for ( int argc = 0; argc < alen; argc++){

			arg = argv[argc];

			arglen = arg.length();

			if ( '-' == arg.charAt(0)){
			    if ( 1 < arglen){
				ch = arg.charAt(1);

				if ( 'e' == ch){

				    fwd = true;
				}
				else if ( 'd' == ch){

				    fwd = false;
				}
				else
				    usage(System.err);
			    }
			    else
				usage(System.err);
			}
			else 
			    usage(System.err);
		    }
		}
		else
		    usage(System.err);
	    }
	    else 
		usage(System.err);

	    if (fwd)
		encode(System.in,System.out);
	    else
		decode(System.in,System.out);

	    System.err.println();

	    System.exit(0);
	}
	catch ( IllegalArgumentException ilarg){
	    System.err.println("Error: "+ilarg.getMessage());
	}
	catch ( Exception exc){
	    exc.printStackTrace();
	    System.exit(1);
	}

    }	
}
