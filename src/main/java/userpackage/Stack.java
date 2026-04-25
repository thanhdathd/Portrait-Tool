package userpackage;

import java.io.File;

public class Stack {
    public SPoint[] s;
    public File[] f;
    public int len;
    private String name = null;
    private boolean echo = true;
    public static final int FILE = 1;
    public static final int SPOINT = 0;

    public Stack(int lenS) {
        this.s = new SPoint[lenS];
        this.len = 0;
        this.name = "s";
    }

    public Stack(int lenS, int type) {
        if (type == 1) {
            this.f = new File[lenS];
            this.s = new SPoint[lenS];
            this.len = 0;
            this.name = "f";
        } else {
            this.s = new SPoint[lenS];
            this.len = 0;
            this.name = "f";
        }

    }

    public Stack(int lenS, int type, String name) {
        if (type == 1) {
            this.f = new File[lenS];
            this.s = new SPoint[lenS];
            this.len = 0;
            this.name = name;
        } else {
            this.s = new SPoint[lenS];
            this.len = 0;
            this.name = name;
        }

    }

    public Stack(int lenS, String name) {
        this.s = new SPoint[lenS];
        this.len = 0;
        this.name = name;
    }

    public void push(SPoint p) {
        if (this.isFull()) {
            Noitifier.printConsole("Stack " + this.name + " Full");
        } else {
            this.s[this.len] = p;
            if (this.echo) {
                Noitifier.printConsole("pushesed at " + this.name + "[" + this.len + "] - id:" + p.id + "; X:" + p.X + "; Y:" + p.Y + "; Direction:" + p.dr);
            }

            ++this.len;
        }

    }

    public void push(File f) {
        if (this.isFull()) {
            Noitifier.printConsole("Stack " + this.name + " full");
        } else {
            this.f[this.len] = f;
            this.s[this.len] = new SPoint();
            if (this.echo) {
                Noitifier.printConsole("pushesed at " + this.name + "[" + this.len + "] - file:" + f);
            }

            ++this.len;
        }

    }

    public SPoint pop() {
        new SPoint();
        --this.len;
        SPoint p = this.s[this.len];
        if (this.echo) {
            Noitifier.printConsole(this.name + " poped - id:" + p.id + "; X:" + p.X + "; Y:" + p.Y + "; Direction:" + p.dr + "; len:" + this.len);
        }

        return p;
    }

    public File pop(int type) {
        if (type == 1) {
            new File("empty.txt");
            --this.len;
            File f = this.f[this.len];
            if (this.echo) {
                Noitifier.printConsole(this.name + " poped file:" + f);
            }

            return f;
        } else {
            return new File("empty.txt");
        }
    }

    public boolean isFull() {
        return this.len == this.s.length;
    }

    public boolean isEmpty() {
        return this.len == 0;
    }

    public void display() {
        for(int i = 0; i < this.len - 1; ++i) {
            if (this.echo) {
                System.out.print("id:" + this.s[i].id + "\t X:" + this.s[i].X + "\t Y:" + this.s[i].Y + "\t Dr:" + this.s[i].dr + "\n");
            }
        }

    }

    public SPoint getAt(int i) {
        return this.s[i];
    }

    public void popAll() {
        this.len = 0;
        if (this.echo) {
            Noitifier.printConsole(this.name + " - popAll");
        }

    }

    public void setSize(int stackSize) {
        if (this.len > 0 && this.len < stackSize) {
            Stack temp = new Stack(this.len);

            for(int i = 0; i < this.len; ++i) {
                temp.push(this.s[i]);
            }

            this.s = new SPoint[stackSize];

            for(int i = 0; i < temp.len; ++i) {
                this.s[i] = temp.getAt(i);
            }
        } else {
            if (this.len != 0) {
                Stack temp = new Stack(stackSize);

                for(int i = 0; i < stackSize; ++i) {
                    temp.push(this.s[i]);
                }

                this.s = new SPoint[stackSize];

                for(int i = 0; i < temp.len; ++i) {
                    this.s[i] = temp.getAt(i);
                    this.len = i + 1;
                }
            }

            if (this.len == 0) {
                this.s = new SPoint[stackSize];
            }
        }

    }

    public void setEcho(boolean e) {
        this.echo = e;
    }
}
