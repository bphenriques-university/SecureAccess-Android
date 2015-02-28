SecureAccess-Android
====================

Companion app for https://github.com/bphenriques/SecureAccess-Server

Implements remote authentication using an encrypted channel through bluetooth.

A KEK is established during the configuration phase (next to the server screen that shows a code / qrcode) then it is exchanged a session key encrypted with the KEK. The server pings the client with an encrypted challenge-response.
