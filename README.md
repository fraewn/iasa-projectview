# Overview
...

# Run the application
To run the app you need to provide the following keys in the ``application.properties`` file for Spring:
- ``spring.datasource.url``
- ``spring.datasource.username``
- ``spring.datasource.password``
- ``spring.jpa.hibernate.ddl-auto``=validate <- leave this on validate
- ``spring.jpa.properties.hibernate.dialect``
- ``spring.jpa.open-in-view``
- ``application.security.bcrypt.strength``
- ``application.security.jwt.secret``
- ``application.security.jwt.expiration.seconds``
