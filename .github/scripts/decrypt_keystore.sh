#!/bin/sh

# Decrypt the file
mkdir $HOME/secrets
# --batch to prevent interactive command --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$KEYSTORE_PASSPHRASE" --output ./app/keystore ./.github/keystore.gpg
gpg --quiet --batch --yes --decrypt --passphrase="$KEYSTORE_PASSPHRASE" --output ./app/keystore.properties ./.github/keystore.properties.gpg
gpg --quiet --batch --yes --decrypt --passphrase="$KEYSTORE_PASSPHRASE" --output ./local.properties ./.github/local.properties.gpg
