package com.bt.rsqe.comparator;

import com.bt.rsqe.customerrecord.SiteDTO;

import java.util.Comparator;

public class SiteDTOComparator implements Comparator<SiteDTO> {

    @Override
    public int compare(SiteDTO o1, SiteDTO o2) {
        if (!(o1 instanceof SiteDTO) || !(o2 instanceof SiteDTO)) {
            return 0;
        }
        return new SiteNameComparator().compare(o1.name, o2.name);
    }

    private static class SiteNameComparator implements Comparator<String>  {

         @Override
        public int compare(String string1, String string2) {
            int thisMarker = 0;
            int thatMarker = 0;
            int s1Length = string1.length();
            int s2Length = string2.length();

            while (thisMarker < s1Length && thatMarker < s2Length) {
                String thisChunk = getChunk(string1, s1Length, thisMarker);
                thisMarker += thisChunk.length();

                String thatChunk = getChunk(string2, s2Length, thatMarker);
                thatMarker += thatChunk.length();

                // If both chunks contain numeric characters, sort them numerically
                int result = 0;
                boolean bothCharsAreDigits = isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0));
                if (bothCharsAreDigits) {
                    // Simple chunk comparison by length.
                    int thisChunkLength = thisChunk.length();
                    result = thisChunkLength - thatChunk.length();
                    // If equal, the first different number counts
                    if (result == 0) {
                        for (int i = 0; i < thisChunkLength; i++) {
                            result = thisChunk.charAt(i) - thatChunk.charAt(i);
                            if (result != 0) {
                                return result;
                            }
                        }
                    }
                } else {
                    result = thisChunk.compareTo(thatChunk);
                }

                if (result != 0) {
                    return result;
                }
            }

            return s1Length - s2Length;
        }

        /**
         * Length of string is passed in for improved efficiency (only need to calculate it once) *
         */
        private final String getChunk(String s, int slength, int marker) {
            int i = marker;
            StringBuilder chunk = new StringBuilder();
            char c = s.charAt(i);
            chunk.append(c);
            i++;
            if (isDigit(c)) {
                while (i < slength) {
                    c = s.charAt(i);
                    if (!isDigit(c)) {
                        break;
                    }
                    chunk.append(c);
                    i++;
                }
            } else {
                while (i < slength) {
                    c = s.charAt(i);
                    if (isDigit(c)) {
                        break;
                    }
                    chunk.append(c);
                    i++;
                }
            }
            return chunk.toString();
        }

        private final boolean isDigit(char ch) {
            return ch >= 48 && ch <= 57;
        }

    }

}
