package protocol.commands;

public enum MessageTag {
    ANY_TAG;

    private final int tag;

    MessageTag(int tag) {
        this.tag = tag;
    }

    MessageTag() {
        this(0);
    }

    public int getTagValue() {
        return tag;
    }
}
