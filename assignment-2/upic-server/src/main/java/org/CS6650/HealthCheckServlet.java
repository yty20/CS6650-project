package org.CS6650;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import java.io.PrintWriter;

@WebServlet(name = "HealthCheckServlet", urlPatterns = "/api/*")
public class HealthCheckServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.println("OK");
    }
}