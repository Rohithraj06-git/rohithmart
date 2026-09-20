package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.service.ProductService;
import com.rohith.rohithmart.service.ServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Landing page controller.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends BaseServlet {

    private final ProductService productService = ServiceFactory.getProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            List<ProductDTO> featured = productService.searchProducts(null, null, "newest", 1, 8);
            req.setAttribute("featuredProducts", featured);
            forward(req, res, "buyer/home.jsp");
        } catch (Exception e) {
            handleException(req, res, e);
            forward(req, res, "buyer/home.jsp");
        }
    }
}
