package com.wysiwyg;

public class Data {
  private String documentId;
  private int pos;
  private String payload;
  private String opcode;
  private String uid;
  private int version;

  Data(String documentId, int pos, String payload, String opcode,
       String uid, int version)
  {
    this.documentId = documentId;
    this.pos = pos;
    this.payload = payload;
    this.opcode = opcode;
    this.uid = uid;
    this.version = version;
  }
}
