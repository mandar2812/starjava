package uk.ac.starlink.topcat;

import java.util.BitSet;

/**
 * A RowSubset which maintains the inclusion status of each row as
 * a separate flag.
 *
 * @author    Mark Taylor (Starlink)
 */
public class BitsRowSubset implements RowSubset {

     private BitSet bits;
     private boolean invert;
     private String name;

     /**
      * Constructs a new row subset with a given BitSet, name and sense.
      * The <tt>invert</tt> argument indicates whether the sense of the
      * bit set is to be reversed prior to interpretation.
      *
      * @param   name  subset name
      * @param   bits  flag vector
      * @param   inver  whether to invert the bits from the BitSet
      */
     public BitsRowSubset( String name, BitSet bits, boolean invert ) {
         this.name = name;
         this.bits = bits;
         this.invert = invert;
     }

     /**
      * Constructs a new row subset with a given BitSet and name.
      * Same as <tt>BitsRowSubset(name,bits,false)</tt>
      *
      * @param   name  subset name
      * @param   bits  flag vector
      */
     public BitsRowSubset( String name, BitSet bits ) {
         this( name, bits, false );
     }


     /**
      * Returns the <tt>BitSet</tt> object used to store the inclusion
      * status flags.
      *
      * @return  flag vector
      */
     public BitSet getBitSet() {
         return bits;
     }

     /**
      * Returns the inversion sense of the inclusion flags represented by
      * this subset relative to the bit set.
      *
      * @return  true iff bitset bits are inverted to give inclusion flag
      */
     public boolean getInvert() {
         return invert;
     }

     public String getName() {
         return name;
     }

     public boolean isIncluded( long lrow ) {
         return bits.get( (int) lrow ) ^ invert;
     }

     public String toString() {
         return name;
     }

}
