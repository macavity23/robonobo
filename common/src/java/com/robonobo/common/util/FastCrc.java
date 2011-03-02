package com.robonobo.common.util;
/*
 * Robonobo Common Utils
 * Copyright (C) 2008 Will Morton (macavity@well.com) & Ray Hilton (ray@wirestorm.net)

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


/**
 * This class implements the fast crc algorithm (that is, it takes whole
 * bytes at once).
 * @author Brautigam Robert
 * @version CVS Revision: $Revision: 1.2 $
 */
public class FastCrc
{
   private int[] crcTable;  // Hold the pre-computed values

   /**
    * Construct, and initialize the fast crc algorithm with the given
    * generator polynomial.
    * @param generator The generator polynomial.
    */
   public FastCrc(int generator)
   {
      init(generator);
   }

   /**
    * Initialize the crc table using the given generator.
    * @param generator The generator polynomial.
    */
   private void init(int generator)
   {
      crcTable=new int[256];
      for ( int i=0; i<256; i++ )
      {
         int result = (i<<24);
         for ( int j=0; j<8; j++ )
         {
            boolean bit = (result & 0x80000000) != 0;  // Is bit set?
            result <<= 1;                              // Step to next bit
            if ( bit )                                 // If bit set, exp
               result ^= generator;
         }
         crcTable[i]=result;
      }
   }

   /**
    * Generate crc code for given byte array.
    * @param b The byte array.
    * @param offset The offset to start from.
    * @param length The length of data.
    */
   public int crc(byte[] b, int offset, int length)
   {
      int result = 0;
      for ( int i=offset; i<offset+length; i++ )
      {
         int top = (result >> 24) & 0xff;
         top ^= ((int) b[i] & 0xff);
         result = (result<<8) ^ crcTable[top];
      }
      return result;
   }

   /**
    * Generate crc code. Same as <code>crc(b,0,b.length)</code>.
    * @param b The byte data.
    */
   public int crc(byte[] b)
   {
      return crc(b,0,b.length);
   }
}


