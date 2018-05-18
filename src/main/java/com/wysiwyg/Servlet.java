package com.wysiwyg;

import java.io.IOException;
import java.util.List;

import com.cedarsoftware.util.io.JsonWriter;
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
        name = "Servlet",
        urlPatterns = {"/documents"}
    )
public class Servlet extends HttpServlet {
    private static String CREATE_DOCUMENT = "name";

    protected MetadataManager metadataManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Document> documents = metadataManager.listDocument();
        byte[] ret = JsonWriter.objectToJson(documents).getBytes();

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
