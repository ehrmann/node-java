package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

public class CharArrayAdapter extends ScriptableDecorator implements Adapted<char[]>  {

    private final char[] adapted;

    public CharArrayAdapter(Context context, Scriptable parentScope, char[] adapted) {
        super(ScriptRuntime.toObject(context, parentScope, new String(adapted)));
        this.adapted = adapted;
    }

    @Override
    public char[] getAdapted() {
        return adapted;
    }
}
