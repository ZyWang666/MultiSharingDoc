package com.wysiwyg;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Integer;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wysiwyg.meta.MetadataManager;
import com.wysiwyg.meta.MetadataManagerImpl;
import com.wysiwyg.structs.Document;
import com.wysiwyg.structs.DocumentOutput;
import com.wysiwyg.structs.Mutation;
import com.wysiwyg.structs.Opcode;
import com.wysiwyg.ot.OperationalTransformation;
import com.wysiwyg.operations.Operation;
import com.wysiwyg.operations.OperationImpl;

@WebServlet(
        name = "OperationServlet",
        urlPatterns = {"/documents/op"}
    )
public class OperationServlet extends HttpServlet {
    private static final String DOCUMENT_ID         = "documentId";
    private static final String VERSION             = "ver";
    private static final String MODIFY_POSITION     = "pos";
    private static final String MODIFY_PAYLOAD      = "payload";
    private static final String OPCODE              = "op";
    private static final String UID                 = "uid";

    protected MetadataManager metadataManager;
    protected OperationalTransformation operationalTransformation;
    
    public OperationServlet() {
        metadataManager = new MetadataManagerImpl();
        operationalTransformation = new OperationalTransformation(new OperationImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Mutation> mutationHistory = metadataManager.getMutationHistory(req.getParameter(DOCUMENT_ID));
        List<Mutation> mutationDiff = new ArrayList<Mutation>();
        for (int i = Integer.valueOf(req.getParameter(VERSION)).intValue(); i < mutationHistory.size(); i++) {
            mutationDiff.add(mutationHistory.get(i));
        }

        Gson gson = new Gson();
        byte[] ret = gson.toJson(mutationDiff).getBytes();
        ServletOutputStream out = resp.getOutputStream();
        out.write(ret);
        out.flush();
        out.close();
   }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        JsonObject data = new Gson().fromJson(req.getReader(), JsonObject.class);
        Document document = metadataManager.getDocument(data.get(DOCUMENT_ID).getAsString());
        int pos = Integer.valueOf(data.get(MODIFY_POSITION).getAsString()).intValue();
        String payload = data.get(MODIFY_PAYLOAD).getAsString();
        String opcodeAsString = data.get(OPCODE).getAsString();
        Opcode opcode = null;
        if (opcodeAsString.equals("ins")) {
            opcode = Opcode.INSERT;
        } else if (opcodeAsString.equals("del")) {
            opcode = Opcode.DELETE;
        } else {
            opcode = Opcode.IDENTITY;
        }
        int version = Integer.valueOf(data.get(VERSION).getAsString()).intValue();
        String uid = req.getParameter(UID);
        Mutation mutation = new Mutation(opcode, document.documentId, pos, payload, uid, version);
        operationalTransformation.enqueueMutation(mutation);
   }
}
