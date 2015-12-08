package com.example.jwbauer3.mapsourcing;

/**
 * created by Nikhil on 11/24/2015.
 */
public enum MenuSelection {
    START("Start"),
    END("End"),
    LOCATE("I'm Here"),
    SEARCH("Go There");


    private final String text;

    /**
     * @param text
     */
    private MenuSelection(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
