package com.wysiwyg;

import java.io.IOException;
import java.util.List;

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
        name = "UserServlet",
        urlPatterns = {"/users"}
    )
public class UserServlet extends HttpServlet {
    private static final String ADD_USER = "user";
    protected MetadataManager metadataManager;

    public UserServlet() {
        metadataManager = new MetadataManagerImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<String> users = metadataManager.listUser();
        Gson gson = new Gson();
        byte[] ret = gson.toJson(users).getBytes();

        ServletOutputStream out = resp.getOutputStream();
        out.write(ret);
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String newUser = req.getParameter(ADD_USER);
        metadataManager.addUser(newUser);
   }

}
