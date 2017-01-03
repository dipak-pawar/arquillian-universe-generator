# arquillian-universe-generator

Pre-Requisites:
* Generate personal access token for your github account from [here](https://help.github.com/articles/creating-an-access-token-for-command-line-use/).
Set it as Environment variable by Name `AUTH_TOKEN`

Steps to Run:

1.Clone Repository - https://github.com/dipak-pawar/arquillian-universe-generator

2.Run Command from root dir of project.
 > `mvn package`

3.Look for `update-pom.jar` inside target dir which is generated due to above command.

4.Now if you set `AUTH_TOKEN` as environment variable. Run
> `java -jar target/update-pom.jar -u username`

OR

 If you don't want to set environment variable. Run
> `java -jar target/update-pom.jar -u username -t personal_access_token`

5.After successful run of above command, see for generated pull request at [here](https://github.com/arquillian/arquillian-universe-bom/pulls).
