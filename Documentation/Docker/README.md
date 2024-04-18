## How to use

- Download the latest release of UniTime 4.8.126 or later from [builds.unitime.org](https://builds.unitime.org/) or [github.com/UniTime/unitime/releases](https://github.com/UniTime/unitime/releases/latest)

- Unzip the downloaded distribution, and go to the `docker` folder

```
unzip unitime-4.8_bld126.zip
cd docker
```

- Build and deploy docker images using the following command

```
docker-compose build && docker-compose up
```

- Once the project has been built and deployed, UniTime should become available at [localhost:8888](http://localhost:8888)

- Log in using `admin` as both user name and password. Other available credentials are listed at [demo.unitime.org](https://demo.unitime.org)

## Notes

- This is a simple installation much like the [online demo](https://demo.unitime.org), with only one web-server and no dedicated solver server.

- All components (Java, Tomcat, and MySQL) are included in one container.

- No HTTPS included, but a reverse-proxy can be used to provide SSL layer.

- Inspired by [vlatka-sinisa/docker-unitime](https://github.com/vlatka-sinisa/docker-unitime)
