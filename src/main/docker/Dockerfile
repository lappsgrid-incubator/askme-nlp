FROM openjdk:8-jdk
RUN useradd -ms /bin/bash askme
COPY start.sh /home/askme/start.sh
RUN chmod +x /home/askme/start.sh
COPY *.jar /home/askme
USER askme
WORKDIR /home/askme
CMD ["/home/askme/start.sh"]


