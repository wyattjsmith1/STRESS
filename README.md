# Apache Project Data
Here, you will find the information to run the program
## Quickstart
You will need:
 1. Java 8
 2. MySQL server
 3. An account with Apache (https://issues.apache.org/jira/login.jsp?os_destination=%2Fsecure%2FDashboard.jspa)
 4. A reCaptcha account with Google. 

To start:
 1. Modify the 'apache.properties' file. Add your Apache credentials, MySQL configuration, and reCaptcha keys.
 2. Import the sql data from the `INSTALL/sql` directory into your database. (Optionally, you can generate a new snapshot, but that takes tremendous time and resources. See below)
 3. Run:
```sh
$ java -jar apache_project_data.jar [flags]
```
Use `--help` to get a list of flags. You will likely want to use the `-a` flag to define the location of the config file. Note that generating another snapshot can require around 150GB disk space and take 30-40 hours for the first run.
 4. Navigate to http://localhost:8080

If you want to use the algorithm in R, you will need to do a few more things:
 1. Install R
 2. Install DBI in R `install.packages("DBI")`
 3. Add the following lines to your P config (default location: `.my.cnf`). You may need more information if your database is remote. See here for more details: https://cran.r-project.org/web/packages/DBI/README.html
```sh
database=<Your schema>
user=<MySQL user>
password=<MySQL pass>
```
Notes:
 1. To build source, run `mvn package`. That will create a jar in `/target`
 2. When importing SQL, the table names must be the same.
 3. Due to a small but that will hopefully be fixed soon, when filtering with a boolean (checkbox), the checkbox must always be enabled. For example, if you want to look for projects that do not use maven, do: "usesMaven? != \[x\]"
 4. Currently, filtering by language stats is not supported. You may view the statistics of the languages, but you can not filter by them.
