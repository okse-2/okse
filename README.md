# Okse
[![Build Status](https://travis-ci.org/okse-2/okse.svg?branch=master)](https://travis-ci.org/okse-2/okse)
[![Coverage Status](https://coveralls.io/repos/github/okse-2/okse/badge.svg?branch=mqtt)](https://coveralls.io/github/okse-2/okse?branch=mqtt)
## (Overordnet KommunikasjonsSystem for Etteretning)

Bachelors degree project for FFI (Norwegian Defence Research Establishment)

OKSE is a publish-subscribe message broker with support for WS-Notification and AMQP (MQTT support is flagged as upcoming in later release).

OKSE functions as a completely protocol agnostic communication relay, allowing messages published on a WS-Notification topic to be relayed to the corresponding AMQP queue.

The main components of the OKSE brokering system are its CoreService, MessageService, TopicService and SubscriptionService. Extending the application with additional services is done with ease by extending the appropriate abstract class and registering it in `Application.java`. All services adhere to the singleton pattern using static references, so some of the fields and getInstance methods must be implemented manually for each new service.

Extending with additional protocols is also possible, by extending the appropriate abstract class and implementing the needed static fields and invocation methods. Then its just a matter of registering the new protocol in `Application.java`

