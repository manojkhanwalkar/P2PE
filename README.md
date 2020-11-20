# **P2PE Framework**

The code provides a design pattern for application level encryption between services using the JOSE standards.
This can be used as a start point for handling of sensitive data between services on top of using TLS1.2
It uses the Nimbus library for the digital signature and encryption and utilizes JWKS/JWK/JWE/JWS standards.
It uses DropWizard for the services. 

**Setup**
1. KMS - The key management service that hosts the JWKS end point that all services connect to at startup and get the signing and encryption keys.
2. Client and Server communicate with each other using the security apparatus.
3. Server starts and waits for client connection
4. Client starts and gets the keys from KMS 
5. Client initiates keyexchange with the server and sends it its public keys.
6. Server gets its keys from KMS and sends it's public keys to the client
7. At this point setup is complete and both parties have each others public keys.
8. Both sides will use the other's public key and use DiffieHellman to derive the shared secret. 
9. Each message will use an ephemeral keypair and the other parties public key to derive the shared secret.This actual work is done by the Nimbus library.
10. The message sent has the following format JWE<JWS<Message>> and the other party first decrypts the message and then verifies the signature before processing the message.
11. Server implements two Rest end points - one for keyexchange and one for processing the message.

**Exclusions**
The pattern does not cater to the following areas, which would need to be provided by alternate mechanisms.
1. Mutual authentication between services
2. Service discovery 
3. Long term key storage 
4. Does not use certificates for validating public keys
5. Use of a Nonce will provide additional security against replay attacks
