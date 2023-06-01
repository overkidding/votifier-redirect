# Description
Vote redirection, prevents direct connections to the main server thru 8192 port.

# Usage
<ul>
  <li>Start votifier-redirect-1.0-SNAPSHOT.jar</li>
  <li>Edit the config.json and put your main server address in "host", main server votifier port in "port", the default token that you will find in the config.yml of Votifier in "token"</li>
  <li>Enable your firewall</li>
  <li>Add the rule to allow connections to the main server votifier port only from the secondary server</li>
</ul>
