package org.example.knowledge;

import java.io.Serializable;

public class KnowledgeEntry implements Serializable {
    private String knowledgeId;
    private String knowledgeName;

    public String getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(String knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public String getKnowledgeName() {
        return knowledgeName;
    }

    public void setKnowledgeName(String knowledgeName) {
        this.knowledgeName = knowledgeName;
    }
}
