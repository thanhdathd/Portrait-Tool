package filter;

import userpackage.Noitifier;

public class FStack {
    public int len;
    private FilterProperties[] fp;
    private String name;
    private boolean echo;

    public FStack(int length) {
        this.fp = new FilterProperties[length];
        this.len = 0;
        this.name = "FStack";
    }

    public FStack(int length, String name) {
        this.fp = new FilterProperties[length];
        this.len = 0;
        this.name = name;
    }

    public FStack(int length, String name, boolean echo) {
        this.fp = new FilterProperties[length];
        this.len = 0;
        this.name = name;
        this.echo = echo;
    }

    public boolean isFull() {
        return this.len == this.fp.length;
    }

    public boolean isEmpty() {
        return this.len == 0;
    }

    public void setEcho(boolean b) {
        this.echo = b;
    }

    public void push(FilterProperties f) {
        if (!this.isFull()) {
            this.fp[this.len] = f;
            if (this.echo) {
                Noitifier.printConsole("pushesed at " + this.name + "[" + this.len + "] - fp:" + f);
            }

            ++this.len;
        } else {
            Noitifier.printConsole("Stack " + this.name + " is Full");
        }

    }

    public FilterProperties pop() {
        if (!this.isEmpty()) {
            --this.len;
            FilterProperties f = this.fp[this.len];
            if (this.echo) {
                Noitifier.printConsole("Pop " + this.name + "[" + this.len + "]: " + f);
            }

            return f;
        } else {
            Noitifier.printConsole(this.name + " Empty !!");
            return null;
        }
    }

    public FilterProperties getAt(int i) {
        return this.fp[i];
    }

    public void popAll() {
        this.len = 0;
        if (this.echo) {
            Noitifier.printConsole(this.name + " - popAll");
        }

    }
}
