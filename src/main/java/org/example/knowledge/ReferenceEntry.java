package org.example.knowledge;

import java.io.Serializable;
import java.util.List;

public class ReferenceEntry implements Serializable {
    private List<Chunk> chunks;

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    class Chunk implements Serializable{
        private String content_with_weight;
        private String docnm_kwd;
        private String neid;
        private String nsid;

        public String getContent_with_weight() {
            return content_with_weight;
        }

        public void setContent_with_weight(String content_with_weight) {
            this.content_with_weight = content_with_weight;
        }

        public String getDocnm_kwd() {
            return docnm_kwd;
        }

        public void setDocnm_kwd(String docnm_kwd) {
            this.docnm_kwd = docnm_kwd;
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
}
