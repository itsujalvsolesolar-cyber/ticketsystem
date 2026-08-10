package com.sujal.itsm.ticketing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.ticketing.model.Comment;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.CommentRepository;

@Service
@Transactional
public class TicketCommentService {

  private final TicketService ticketService;
  private final CommentRepository commentRepository;

  public TicketCommentService(TicketService ticketService, CommentRepository commentRepository) {
    this.ticketService = ticketService;
    this.commentRepository = commentRepository;
  }

  public void addComment(Long ticketId, Comment formComment) {
    Ticket ticket = ticketService.getTicketDetails(ticketId);

    String author = formComment.getAuthorName();
    if (author == null || author.trim().isEmpty()) {
      author = "IT Support";
    }

    Comment cleanComment =
        Comment.builder()
            .message(formComment.getMessage())
            .authorName(author)
            .ticket(ticket)
            .build();

    commentRepository.save(cleanComment);
  }
}
