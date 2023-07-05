# stockTraceDemo

Enter in procject file:

/stockTraceDemo/stock-trace

run:

```docker compose up```

or run this docker-compose anywhere:
```
version: '3.1'

services:

  front:
    image: marinisz/stock-trace-front:7.0
    ports:
      - "3000:80"
    depends_on:
      - app
    network_mode: "host"

  selenium:
    image: marinisz/stock-trace-back-python:1.0
    ports:
      - "5000:5000"
    network_mode: "host"

  app:
    image: marinisz/stock-trace-back-kotlin:3.0
    ports:
      - "8080:8080"
    depends_on:
      - selenium
    network_mode: "host"

```

The application will be on at: http://localhost:3000/
