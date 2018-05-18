package com.knoldus.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.transport.Method.GET;

public interface NotificationService extends Service {

    ServiceCall<NotUsed, PSequence<User>> getUser(String uid);
    ServiceCall<NotUsed, PSequence<Vehicle>> getVehicle(String vid);
    ServiceCall<NotUsed, PSequence<Rc>> getRc(String rcNumber);
    ServiceCall<NotUsed, PSequence<EmailAddress>> checkExpiry(String rcNumber);

    @Override
    default Descriptor descriptor() {
        Descriptor descriptor = named("notification")
                .withCalls(
                        restCall(GET, "/api/user/:uid", this::getUser),
                        restCall(GET, "/api/vehicle/:vid", this::getVehicle),
                        restCall(GET, "/api/rc/:rcNumber", this::getRc),
                        restCall(GET, "/api/check/:rcNumber", this::checkExpiry)
                        )
                .withAutoAcl(true);
        return descriptor;
    }
}
