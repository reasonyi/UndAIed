FROM python:3.12.8

WORKDIR /AI

COPY ./requirements.txt /AI/requirements.txt

RUN pip install --no-cache-dir --upgrade -r /AI/requirements.txt

COPY . /AI

EXPOSE 8000

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]

# uvicorn app:app --host 0.0.0.0 --port 8000 --reload