package com.knoldus.impl;

import akka.NotUsed;
import com.google.inject.Inject;
import com.knoldus.api.*;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.jdbc.JdbcSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class NotificationImpl implements NotificationService {

    private final JdbcSession jdbcSession;

    @Inject
    public NotificationImpl(final JdbcSession jdbcSession) {
        this.jdbcSession = jdbcSession;

    }

    @Override
    public ServiceCall<NotUsed, PSequence<User>> getUser(String id) {
        return (response) -> {
            return jdbcSession.withConnection(connection -> {
                try (PreparedStatement ps = connection.prepareStatement("SELECT id, email FROM person where id = '" + id + "'")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        PSequence<User> userList = TreePVector.empty();
                        while (rs.next()) {
                            userList = userList.plus(new User(rs.getString("id"), rs.getString("email"))
                            );
                        }
                        return userList;
                    }
                }
            });
        };
    }

    @Override
    public ServiceCall<NotUsed, PSequence<Vehicle>> getVehicle(String vid) {
        return (response) -> {
            return jdbcSession.withConnection(connection -> {
                try (PreparedStatement ps = connection.prepareStatement
                        ("SELECT vid, car_number, rc_number, puc_number, chasis_number, insurance, id FROM vehicle where vid = '" + vid + "'")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        PSequence<Vehicle> vehiclePSequence = TreePVector.empty();
                        while (rs.next()) {
                            vehiclePSequence = vehiclePSequence.plus(
                                    new Vehicle(
                                            rs.getInt("vid"),
                                            rs.getString("car_number"),
                                            rs.getString("rc_number"),
                                            rs.getString("puc_number"),
                                            rs.getString("chasis_number"),
                                            rs.getString("insurance"),
                                            rs.getString("id"))
                            );
                        }
                        return vehiclePSequence;
                    }
                }
            });
        };
    }

    @Override
    public ServiceCall<NotUsed, PSequence<Rc>> getRc(String rcNumber) {
        return (response) -> {
            return jdbcSession.withConnection(connection -> {
                try (PreparedStatement ps = connection.prepareStatement
                        ("SELECT rc_number, date_of_expiry, vid FROM rc where rc_number = '" + rcNumber + "'")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        PSequence<Rc> rcPSequence = TreePVector.empty();
                        while (rs.next()) {
                            rcPSequence = rcPSequence.plus(
                                    new Rc(
                                            rs.getString("rc_number"),
                                            rs.getString("date_of_expiry"),
                                            rs.getInt("vid"))
                            );
                        }
                        return rcPSequence;
                    }
                }
            });
        };
    }

    @Override
    public ServiceCall<NotUsed, PSequence<EmailAddress>> checkExpiry(String rcNumber) {
        return (response) -> {
            return jdbcSession.withConnection(connection -> {
                try (PreparedStatement ps = connection.prepareStatement
                        ("SELECT rc_number, date_of_expiry, vid FROM rc where rc_number = '" + rcNumber + "'")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        PSequence<EmailAddress> rcPSequence1 = TreePVector.empty();
                        PSequence<Rc> rcPSequence = TreePVector.empty();
                        while (rs.next()) {
                            rcPSequence = rcPSequence.plus(
                                    new Rc(
                                            rs.getString("rc_number"),
                                            rs.getString("date_of_expiry"),
                                            rs.getInt("vid"))
                            );
                        }
                        if (getDate(rcPSequence.get(0).getDate_of_expiry()) <= 30) {
                            try (PreparedStatement ps1 = connection.prepareStatement("select email from person p where exists(select vid from vehicle v where v.id=p.id AND exists(select rc_number from rc where '" + rcNumber + "' = v.rc_number))")) {
                                try (ResultSet resultSet = ps1.executeQuery()) {
                                    while (resultSet.next()) {
                                        rcPSequence1 = rcPSequence1.plus(new EmailAddress(resultSet.getString("email"))
                                        );
                                    }
                                    resultSet.close();
                                    return rcPSequence1;
                                }
                            }
                        }
                        return rcPSequence1;
                    }
                }
            });
        };
    }

    private void sendEmail(String email) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "localhost");
        Session session = Session.getDefaultInstance(properties);

        //compose the message
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("charmygarg07@gmail.com"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Ping");
            message.setText("Hello, this is example of sending email  ");

            // Send message
            Transport.send(message);

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    private Long getDate(String date) {
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        Long dd = 0l;
        try {
            Date date1 = myFormat.parse(date);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDateTime now = LocalDateTime.now();
            Date date2 = myFormat.parse(dtf.format(now));
            long diff = date1.getTime() - date2.getTime();
            dd = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dd;
    }

}
