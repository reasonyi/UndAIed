FROM python:3.12.8

WORKDIR /AI

COPY ./requirements.txt /AI/requirements.txt

RUN pip install --no-cache-dir --upgrade -r /AI/requirements.txt

COPY . /AI

EXPOSE 8000

CMD ["uvicorn", "app:app", "--port", "8000", "--reload"]
