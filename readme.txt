1) Steps to run application:


2)Technologies & Tools
-> Spring Boot
-> Rsocket protocol
-> Spring scheduler
-> Rest service used in client service

Reason behind to use Rsocket:
It build on top of TCP, WebSockets
-> Single, shared long-lived connection.
-> communicate back pressure
-> supports cancelling/resuming stream
-> it has 4 common interaction models
    -> Request-Response(1 to 1) ---> Using in our task
    -> Fire and Forget(1 to 0)
    -> Request stream (1 to n) ---> Using in our task
    -> Request channel(n to n)

