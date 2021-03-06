package com.oocl.trello.ticket.controller;

import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Label;
import com.oocl.trello.ticket.service.EmailBAFollowUpService;
import com.oocl.trello.ticket.view.EmailDialogWindow;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EmailDialogController{
    private EmailDialogWindow emailWindowView;
    private Board board;

    public EmailDialogController(EmailDialogWindow emailWindowView, Board board){
        this.emailWindowView = emailWindowView;
        this.board = board;

        initController();
    }

    private void initController() {

        this.emailWindowView.getButtonCancel().addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        this.emailWindowView.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.emailWindowView.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        this.emailWindowView.getRecipientsJTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                emailWindowView.getRecipientsJTextField().setText("");
            }
        });

        // call onCancel() on ESCAPE
        this.emailWindowView.getContentPane().registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        List<Card> cards = getCardsThatHasBAFollowUp(board);
        StringBuilder emailContent = getEmailBody(cards);

        this.emailWindowView.requestFocusInWindow();
        this.emailWindowView.getEmailContentPanel().setText(emailContent.toString());
        this.emailWindowView.getEmailContentPanel().requestFocusInWindow();

        this.emailWindowView.getButtonSend().addActionListener(e -> onSend(emailContent.toString(), this.emailWindowView.getRecipientsJTextField().getText()));
    }

    private StringBuilder getEmailBody(List<Card> cards) {
        StringBuilder emailBody = new StringBuilder();

        emailBody.append("Hi BAs,");
        emailBody.append(System.getProperty("line.separator"));
        emailBody.append("Kindly please help to check the following cards:");
        emailBody.append(System.getProperty("line.separator"));
        emailBody.append(System.getProperty("line.separator"));

        for(Card card : cards){
            emailBody.append(card.getName());
            emailBody.append(System.getProperty("line.separator"));
            emailBody.append(card.getShortUrl());
        }

        String note = "\n\n\nThis email was generated by TrelloTicket App";

        emailBody.append(note);
        return emailBody;
    }

    private List<Card> getCardsThatHasBAFollowUp(Board board) {
        List<Card> cards = new LinkedList<>(board.fetchCards());

        for (int cardIter = 0; cardIter < cards.size(); cardIter++){
            if (!hasBAFollowUpLabel(cards.get(cardIter))) {
                cards.remove(cardIter);
            }
        }
        return cards;
    }

    private boolean hasBAFollowUpLabel(Card card) {
        boolean hasFollowUpLabel = false;
        for (Label label : card.getLabels()){
            if(label.getName().equals("BA Follow up")){
                hasFollowUpLabel = true;
            }
        }
        return hasFollowUpLabel;
    }

    private void onSend(String emailContent, String recipients) {
        System.out.println("SEND EMAIL");
        EmailBAFollowUpService.sendEmail(this.emailWindowView.getEmailContentPanel().getText(), recipients);
        this.emailWindowView.dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        this.emailWindowView.dispose();
    }
}
