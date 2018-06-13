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
import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.wysiwyg.meta.MetadataManager;
import com.wysiwyg.meta.MetadataManagerImpl;
import com.wysiwyg.meta.ConditionVariableSingleton;
import com.wysiwyg.structs.Document;
import com.wysiwyg.structs.Mutation;
import com.wysiwyg.structs.Opcode;
import com.wysiwyg.ot.OperationalTransformation;
import com.wysiwyg.operations.Operation;
import com.wysiwyg.operations.OperationImpl;


@WebServlet(
        name = "MutationServlet",
        urlPatterns = {"/documents/op"},
        asyncSupported = true
    )
public class MutationServlet extends HttpServlet {
    private static final String OPCODE              = "opcode";
    private static final String DOCUMENT_ID         = "documentId";
    private static final String MODIFY_POSITION     = "pos";
    private static final String MODIFY_PAYLOAD      = "payload";
    private static final String UID                 = "uid";
    private static final String VERSION             = "version";
    private static final String MUTATION_DELETE     = "DELETE";
    private static final String MUTATION_INSERT     = "INSERT";

    protected final MetadataManager metadataManager;
    protected OperationalTransformation operationalTransformation;
    private BackMgr _backend;
    
    public MutationServlet() {
        metadataManager = new MetadataManagerImpl();
        operationalTransformation = new OperationalTransformation(new OperationImpl());
        _backend = new BackMgr(operationalTransformation);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        final AsyncContext asyncCtx = req.startAsync(req, resp);
        asyncCtx.start( new Runnable() {
            public void run() {
                ServletRequest request = asyncCtx.getRequest();
                ServletResponse response = asyncCtx.getResponse();
                Document document = metadataManager.getDocument(request.getParameter(DOCUMENT_ID));
                if (document == null) {
                    return;
                }
                if (document.ver <= Integer.valueOf(request.getParameter(VERSION)).intValue()) {
                    try {
                        metadataManager.await();
                    } catch (InterruptedException e) {
                        // deal with exception
                    }
                }
                List<Mutation> mutationHistory = metadataManager.getMutationHistory(request.getParameter(DOCUMENT_ID));
                List<Mutation> mutationDiff = new ArrayList<Mutation>();
                for (int i = Integer.valueOf(request.getParameter(VERSION)).intValue(); 
                        i < Math.min(document.ver, Integer.valueOf(request.getParameter(VERSION)).intValue()+1); 
                        i++) {
                    // since indexInMutationHistory describes the 0-based index at the historyQueue
                    // so it is alwasy one smaller than the version returned to the client. 
                    // for example, indexInMutationHistory = 0, then version returned to client should be 1
                    // because next time client request will be version 1, which naturally fits in position 1 in historyQueue.
                    Mutation mutation = mutationHistory.get(i);
                    mutation.version = mutation.indexInMutationHistory+1;
                    mutationDiff.add(mutation);
                }
                Gson gson = new Gson();
                String ret = gson.toJson(mutationDiff);
                try {
                    // System.out.println(ret);
                    response.getWriter().write(ret);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                asyncCtx.complete();
            }
        });
   }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        JsonObject data = new Gson().fromJson(req.getReader(), JsonObject.class);
        // System.out.println(data);
        Document document = metadataManager.getDocument(data.get(DOCUMENT_ID).getAsString());
        int pos = Integer.valueOf(data.get(MODIFY_POSITION).getAsString()).intValue();
        String payload = data.get(MODIFY_PAYLOAD).getAsString();
        String opcodeAsString = data.get(OPCODE).getAsString();
        Opcode opcode = null;
        if (opcodeAsString.equals("INSERT")) {
            opcode = Opcode.INSERT;
        } else if (opcodeAsString.equals("DELETE")) {
            opcode = Opcode.DELETE;
        } else {
            opcode = Opcode.IDENTITY;
        }
        int version = Integer.valueOf(data.get(VERSION).getAsString()).intValue();
        String uid = data.get(UID).getAsString();
        Mutation mutation = new Mutation(opcode, document.documentId, pos, payload, uid, version);
        operationalTransformation.enqueueMutation(mutation);
        // _backend.bcast(mutation);
   }
}
