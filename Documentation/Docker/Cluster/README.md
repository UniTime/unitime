# Docker Cluster Example

This is an example clustered installation much like the [online demo](https://demo.unitime.org), but with two web-servers and a single solver server running within the same Docker.

## How to use

- Download the latest release of UniTime 4.9.170 or later from [builds.unitime.org](https://builds.unitime.org/) or [github.com/UniTime/unitime/releases](https://github.com/UniTime/unitime/releases/latest)

- Unzip the downloaded distribution, and go to the `docker/cluster` folder

```
unzip unitime-4.9_bld170.zip
cd docker/cluster
```

- Build and deploy docker images using the following command

```
docker-compose build && docker-compose up
```

- Once the project has been built and deployed, UniTime should become available at [localhost](http://localhost)

- Log in using `admin` as both user name and password. Other available credentials are listed at [demo.unitime.org](https://demo.unitime.org)

## Notes

- Ideally, each web server and solver server would run on a separate machine, not within the same Docker instance.

- No HTTPS included, but the Nginx (which is used for load balancing) can be used to provide SSL layer.
