package org.example.knowledge;

import java.io.Serializable;

public class KnowledgeResultEntry implements Serializable {
    private String answer;
//    private ReferenceEntry reference;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

//    public ReferenceEntry getReference() {
//        return reference;
//    }
//
//    public void setReference(ReferenceEntry reference) {
//        this.reference = reference;
//    }
}
