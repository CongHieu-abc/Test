package com.swp.myleague.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.Comment;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.service.CommentService;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.blogservice.BlogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping(value = {"/news", "/news/"})
public class NewsController {
    
    @Autowired BlogService blogService;

    @Autowired CommentService commentService;

    @Autowired UserService userService;

    @GetMapping("")
    public String getNews(Model model) {
        model.addAttribute("blogs", blogService.getAll());
        return "News";
    }
    
    @GetMapping("/{blogId}")
    public String getDetailBlog(@PathVariable(name = "blogId") String blogId, Model model) {
        Blog blog = blogService.getById(blogId);
        model.addAttribute("blog", blog);
        model.addAttribute("relatedNews", blogService.getAll().stream().filter(blg -> blg.getBlogCategory().equals(blog.getBlogCategory()) || blg.getBlogTitle().compareTo(blog.getBlogTitle()) < 100).toList());
        model.addAttribute("comments", commentService.getAllCommentsByBlogId(blogId));
        return "DetailNews";
    }
    
    @PostMapping("/comment/{blogId}")
    public String postComment(@RequestParam(name = "commentContent") String commentContent, @PathVariable(name = "blogId") String blogId, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Comment comment = new Comment();
        comment.setBlog(blogService.getById(blogId));
        comment.setCommentContent(commentContent);
        comment.setUser(user);
        comment.setCommentDateCreated(LocalDateTime.now());
        commentService.save(comment);
        
        return "redirect:/news/" + blogId;
    }

}
