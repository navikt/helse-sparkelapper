#!/bin/zsh

# Henter skjemaet for leesah-topicet til PDL. Krever at man er tilkoblet naisdevice og er i riktig cluster.
# Tar to parametere, navnet på en aiven-secret og ønsket versjon av skjemaet

# Eksempel på bruk:
#   ./hent_pdl_leesah_schema.sh aiven-spinnvill-a1b2c3d4-2024-15 3

set -eo pipefail

# inputs
SECRET_NAME=$1
VERSION=$2

function read_value_from_secret {
  VALUE=$1
  kubectl get secret "$SECRET_NAME" -o jsonpath="{.data.${VALUE}}" | base64 -D
}

function main {
  echo Henter KAFKA_SCHEMA_REGISTRY
  URL=$(read_value_from_secret KAFKA_SCHEMA_REGISTRY)

  echo Henter KAFKA_SCHEMA_REGISTRY_USER
  REGISTRY_USER=$(read_value_from_secret KAFKA_SCHEMA_REGISTRY_USER)

  echo Henter KAFKA_SCHEMA_REGISTRY_PASSWORD
  REGISTRY_PASSWORD=$(read_value_from_secret KAFKA_SCHEMA_REGISTRY_PASSWORD)

  # Ta vare på output i ei fil, for å skille det fra output fra curl :p
  TEMP_FIL=$(mktemp)
  curl -v -u "$REGISTRY_USER:$REGISTRY_PASSWORD" "$URL/subjects/pdl.leesah-v1-value/versions/$VERSION" > "$TEMP_FIL"
  printf "\nHer er schema:\n\n"
  cat "$TEMP_FIL"; rm "$TEMP_FIL"
}

function validate {
   # shellcheck disable=SC2296
   test -n "${(P)1}" || (echo "${1}" er påkrevet && exit 1)
}

function check_context {
   if [[ "$(kubectl config current-context)" =~ prod.* ]]; then
     printf "\n**************************************************************\n"
     printf "Du er i prod, da må du være koblet til aiven-prod i naisdevice"
     printf "\n**************************************************************\n\n"
   fi
}

validate SECRET_NAME
validate VERSION

check_context

main
