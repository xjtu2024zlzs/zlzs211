FROM python:3.11-slim

WORKDIR /app

COPY mock_adapters/requirements.txt /app/mock_adapters/requirements.txt
RUN pip install --no-cache-dir -r /app/mock_adapters/requirements.txt

COPY mock_adapters /app/mock_adapters

ENV PYTHONPATH=/app
ENV ADAPTER_SYSTEM=plm
ENV ADAPTER_PORT=9000

CMD ["sh", "-c", "uvicorn mock_adapters.${ADAPTER_SYSTEM}.main:app --host 0.0.0.0 --port ${ADAPTER_PORT}"]
