#!/bin/sh

mvn clean deploy -P release

#<settings>
#  <profiles>
#    <profile>
#      <id>ossrh</id>
#      <activation>
#        <activeByDefault>true</activeByDefault>
#      </activation>
#      <properties>
#        <gpg.executable>gpg2</gpg.executable>
#        <gpg.passphrase>my_passphrase</gpg.passphrase>
#      </properties>
#    </profile>
#  </profiles>
#  <servers>
#    <server>
#      <id>ossrh</id>
#      <username>my_username</username>
#      <password>my_password</password>
#    </server>
#  </servers>
#</settings>