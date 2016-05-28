# Okse (Overordnet KommunikasjonsSystem for Etteretning)
[![Build Status](https://travis-ci.org/okse-2/okse.svg?branch=master)](https://travis-ci.org/okse-2/okse)
[![Coverage Status](https://coveralls.io/repos/github/okse-2/okse/badge.svg?branch=master)](https://coveralls.io/github/okse-2/okse?branch=master)

This software has been developed for [FFI](http://www.ffi.no/) by two separate
teams as Bachelor's degree projects at [NTNU](https://www.ntnu.edu/), first
during the spring of 2015 and then during the spring of 2016. For specific
credits, see the AUTHORS file at the base of the project.

OKSE is a polyglot topic-based publish-subscribe message broker written in
Java. OKSE functions as a protocol agnostic communication relay between clients
of its supported protocols. Our broker additionally sports a topic mapping
system. Through its configuration interface, the broker can be configured to
distribute messages inbound on a given topic to a different one. Lastly, the
broker contains support for registering as a subscriber on a remote
WS-Notification broker and relaying messages to local subscribers.

Currently supported protocols and libraries used to implement them are as
follows:

| Protocol | Upstream | Fork |
|-----------------|---|---|
| AMQP 0.9.1      | [Apache Qpid Proton](https://qpid.apache.org/proton/) | N/A |
| AMQP 1.0        | [Joram](http://joram.ow2.org/)                        | [Customised fork](https://github.com/okse-2/joram)    |
| MQTT            | [Moquette](https://github.com/andsel/moquette)        | [Customised fork](https://github.com/okse-2/moquette) |
| STOMP           | [Stampy](https://github.com/mrstampy/Stampy)          | [Customised fork](https://github.com/okse-2/Stampy)   |
| WS-Notification | [WS-Nu](https://github.com/tOgg1/WS-Nu)               | [Customised fork](https://github.com/okse-2/WS-Nu)    |

The OKSE message broker has been written with extensibility in mind. Every
protocol is integrated independently, only communicating with the core layer of
OKSE. Implementing support for a new protocol comes down to implementing a
shared protocol server interface and hooking the protocol server up to
necessary functionality in the OKSE core.

For further information, please consult our user manual and developer guide.
