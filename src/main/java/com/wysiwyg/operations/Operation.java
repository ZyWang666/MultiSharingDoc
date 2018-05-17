package com.wysiwyg.operations;

import com.wysiwyg.structs.Mutation;

public interface Operation {
    boolean insert(Mutation mutation);
    boolean delete(Mutation mutation);
}