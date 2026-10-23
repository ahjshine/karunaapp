package com.medsentinel.config;

public class SequenceGeneratorService {

    public long generateSequence(String seqName) {
        return MongoDBManager.getInstance().getNextSequence(seqName);
    }
}
