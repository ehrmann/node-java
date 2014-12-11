package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;

import java.util.Objects;

class ScriptableDecorator implements Scriptable {

    protected final Scriptable scriptable;

    public ScriptableDecorator(Scriptable scriptable) {
        this.scriptable = Objects.requireNonNull(scriptable);
    }

    @Override
    public String getClassName() {
        return this.scriptable.getClassName();
    }

    @Override
    public Object get(String name, Scriptable start) {
        return this.scriptable.get(name, start);
    }

    @Override
    public Object get(int index, Scriptable start) {
        return this.scriptable.get(index, start);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return scriptable.has(name, start);
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return this.scriptable.has(index, start);
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        this.scriptable.put(name, start, value);
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        this.scriptable.put(index, start, value);
    }

    @Override
    public void delete(String name) {
        this.scriptable.delete(name);
    }

    @Override
    public void delete(int index) {
        this.scriptable.delete(index);
    }

    @Override
    public Scriptable getPrototype() {
        return this.scriptable.getPrototype();
    }

    @Override
    public void setPrototype(Scriptable prototype) {
        this.scriptable.setPrototype(prototype);
    }

    @Override
    public Scriptable getParentScope() {
        return this.scriptable.getParentScope();
    }

    @Override
    public void setParentScope(Scriptable parent) {
        this.scriptable.setParentScope(parent);
    }

    @Override
    public Object[] getIds() {
        return this.scriptable.getIds();
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        return this.scriptable.getDefaultValue(hint);
    }

    @Override
    public boolean hasInstance(Scriptable instance) {
        return this.scriptable.hasInstance(instance);
    }
}
