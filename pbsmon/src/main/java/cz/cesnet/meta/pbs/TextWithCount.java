package cz.cesnet.meta.pbs;

/**
 * Text a jeho počet, seřaditelné podle počtu.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: TextWithCount.java,v 1.1 2009/05/05 09:24:37 makub Exp $
 */
public class TextWithCount implements Comparable<TextWithCount> {

    private String text;
    private int pocet;

    public TextWithCount(String text, int pocet) {
        this.text = text;
        this.pocet = pocet;
    }

    @Override
    public int compareTo(TextWithCount o) {
        return o.pocet - this.pocet;
    }

    public String getText() {
        return text;
    }

    public int getPocet() {
        return pocet;
    }
}
