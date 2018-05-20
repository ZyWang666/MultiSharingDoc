package com.wysiwyg;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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

@WebServlet(
        name = "MetadataServlet",
        urlPatterns = {"/documents"}
    )
public class MetadataServlet extends HttpServlet {
    private static String CREATE_DOCUMENT = "name";

    protected MetadataManager metadataManager;

    public MetadataServlet() {
        metadataManager = new MetadataManagerImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<String> documents = new ArrayList<String>();
        for (Document document: metadataManager.listDocument()) {
            documents.add(document.documentId);
        }
        Gson gson = new Gson();
        byte[] ret = gson.toJson(documents).getBytes();
        ServletOutputStream out = resp.getOutputStream();
        out.write(ret);
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Document newDoc = new Document(req.getParameter(CREATE_DOCUMENT));
        metadataManager.addDocument(newDoc);
   }

}
