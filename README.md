Project name: Live Chat Support

Summary: Connects a client who needs help to an agent, through an intake form, a claimable ticket queue, and a live real time chat. Being able to connect two users to interact with each other.

Technical summary: A client server project that was built with Java, Spring Boot, MySQL, and React, plus Spring Security for session based role authentication and Spring WebSockets/STOMP for the real time chat, so a client can only listen in on a chat they're actually part of and agents being able to watch a live ticket queue.

Debugging notes:
- when there is a massive conversation, the type a message input and send button are pushed all the way to the bottom. Can we give it some padding from the bottom.