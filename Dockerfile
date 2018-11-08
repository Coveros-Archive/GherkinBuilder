FROM nimmis/apache-php5

RUN rm /var/www/html/index.html
COPY public/ /var/www/html/

EXPOSE 80
EXPOSE 443

CMD ["/usr/sbin/apache2ctl", "-D", "FOREGROUND"]