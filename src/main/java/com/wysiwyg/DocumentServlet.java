package com.wysiwyg;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Integer;
import java.util.List;

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
import com.wysiwyg.structs.Mutation;
import com.wysiwyg.structs.Opcode;
import com.wysiwyg.ot.OperationalTransformation;
import com.wysiwyg.operations.Operation;
import com.wysiwyg.operations.OperationImpl;

@WebServlet(
        name = "DocumentServlet",
        urlPatterns = {"/documents/d"}
    )
public class DocumentServlet extends HttpServlet {
    private static final String DOCUMENT_ID         = "documentId";
    private static final String VERSION             = "version";
    private static final String MODIFY_POSITION     = "pos";
    private static final String MODIFY_PAYLOAD      = "payload";
    private static final String OPCODE              = "op";

    protected MetadataManager metadataManager;
    protected OperationalTransformation operationalTransformation;
    public DocumentServlet() {
        metadataManager = new MetadataManagerImpl();
        operationalTransformation = new OperationalTransformation(new OperationImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Document document = metadataManager.getDocument(req.getParameter(DOCUMENT_ID));
        long version = Long.valueOf(req.getParameter(DOCUMENT_ID)).longValue();

        // final AsyncContext asyncCtx = req.startAsync();
        // asyncCtx.start( new Runnable(){
        //         public void run()
        //         {
        //             ServletResponse response = asyncCtx.getResponse();
        //             try {
        //                 response.getWriter().write( waitFor5To15Seconds() );
        //             } catch (Exception e) {
        //                 e.printStackTrace();
        //             }
        //             asyncCtx.complete();
        //         }
        //     });
        // if (document.version <= version) {

        // }

        Gson gson = new Gson();
        byte[] ret = gson.toJson(document).getBytes();
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
        Opcode op = data.get(OPCODE).getAsString().equals("ins") ? Opcode.INSERT : Opcode.DELETE;
        Mutation mutation = new Mutation(op, document.documentId, pos, payload);
        operationalTransformation.enqueueMutation(mutation);
   }

}
