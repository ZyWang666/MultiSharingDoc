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
import com.wysiwyg.structs.DocumentOutput;
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

    protected MetadataManager metadataManager;

    public DocumentServlet() {
        metadataManager = new MetadataManagerImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Document document = metadataManager.getDocument(req.getParameter(DOCUMENT_ID));
        if (document == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        DocumentOutput documentOutput = new DocumentOutput(document.documentId, 
                                                        document.documentRope.toString(), 
                                                        document.ver);

        Gson gson = new Gson();
        byte[] ret = gson.toJson(documentOutput).getBytes();
        ServletOutputStream out = resp.getOutputStream();
        out.write(ret);
        out.flush();
        out.close();
   }

}
