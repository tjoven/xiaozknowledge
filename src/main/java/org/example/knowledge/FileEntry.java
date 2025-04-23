package org.example.knowledge;

public class FileEntry {
    private String name;
    private String neid;
    private String nsid;

    @Override
    public String toString() {
        return "FileEntry{" +
                "name='" + name + '\'' +
                ", neid='" + neid + '\'' +
                ", nsid='" + nsid + '\'' +
                '}';
    }

    public FileEntry(String name, String neid, String nsid) {
        this.name = name;
        this.neid = neid;
        this.nsid = nsid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNeid() {
        return neid;
    }

    public void setNeid(String neid) {
        this.neid = neid;
    }

    public String getNsid() {
        return nsid;
    }

    public void setNsid(String nsid) {
        this.nsid = nsid;
    }
}
