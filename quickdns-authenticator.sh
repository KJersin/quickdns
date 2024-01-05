#!/usr/bin/bash

{
    echo "CERTBOT_DOMAIN                : '$CERTBOT_DOMAIN'" 
    echo "CERTBOT_VALIDATION            : '$CERTBOT_VALIDATION'"
    echo "CERTBOT_TOKEN                 : '$CERTBOT_TOKEN'"
    echo "CERTBOT_REMAINING_CHALLENGES  : '$CERTBOT_REMAINING_CHALLENGES'"
    echo "CERTBOT_ALL_DOMAINS           : '$CERTBOT_ALL_DOMAINS'"
    echo "CERTBOT_AUTH_OUTPUT           : '$CERTBOT_AUTH_OUTPUT'"
} >&2
echo "CREATED:aældfjæa"

exit 0