FROM ubuntu:20.04

RUN sed -i 's@archive.ubuntu.com@mirror.kakao.com@g' /etc/apt/sources.list

RUN apt-get -y update && apt-get -y dist-upgrade
RUN apt-get install -y apt-utils dialog libpq-dev

RUN apt-get install -y python3-pip python3-dev

# 우분투에 mysqlclient를 설치하기 위함
RUN apt-get install -y libssl-dev

# ARG DEBIAN_FRONTEND=noninteractive
# RUN apt-get install -y mysql-server
# RUN apt-get install -y mysql-client
# RUN apt-get install -y libmysqlclient-dev

# add cron for auto day completion
RUN apt-get install -y cron && touch ./cron.log

RUN pip3 install --upgrade pip
RUN pip3 install --upgrade setuptools

RUN mkdir /config
ADD /config/requirements.txt /config/
RUN pip3 install -r /config/requirements.txt

RUN mkdir /moment
WORKDIR /moment
