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
    networks:
      - my-net

  selenium:
    image: marinisz/stock-trace-back-python:1.0
    ports:
      - "5000:5000"
    networks:
      - my-net

  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    depends_on:
      - selenium
    networks:
      - my-net

networks:
  my-net:

```
