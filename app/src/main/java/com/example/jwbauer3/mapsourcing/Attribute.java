package com.example.jwbauer3.mapsourcing;

/**
 * created by Nikhil on 11/24/2015.
 */
public enum Attribute {
    CLICKED("Clicked"),
    PATH("Path"),
    TERMINAL("Terminal");

    private final String text;

    /**
     * @param text
     */
    private Attribute(final String text) {
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
