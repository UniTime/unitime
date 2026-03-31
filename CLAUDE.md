# UniTime — Project Context for Claude

## Build
- Package (skip tests): `mvn package -DskipTests` → generates `target/UniTime.war`
- Full build with tests: `mvn package`
- Clean build: `mvn clean package -DskipTests`

## Docker Deployment
Docker files are in `../New folder/` (one level up from this repo root).

Steps after a new build:
1. Copy WAR: `cp target/UniTime.war "../New folder/web/UniTime.war"`
2. Rebuild and restart (from inside `New folder/`):
   ```
   docker-compose down
   docker-compose build unitime-web
   docker-compose up -d
   ```
3. Verify at: http://localhost:8888 → Help → About → confirm version

## Source Layout
- Hibernate entities:   `JavaSource/org/unitime/timetable/model/`
- Struts 2 actions:     `JavaSource/org/unitime/timetable/action/`
- REST API endpoints:   `JavaSource/org/unitime/timetable/api/`
- GWT client code:      `JavaSource/org/unitime/timetable/gwt/`
- JSP pages:            `WebContent/`
- Spring config:        `WebContent/WEB-INF/applicationContext.xml`
- Struts config:        `WebContent/WEB-INF/struts.xml`
- Security config:      `WebContent/WEB-INF/securityContext*.xml`
- Hibernate mappings:   `*.hbm.xml` files alongside entity classes

## Conventions
- New entities: add `.java` in `model/` + matching `.hbm.xml` mapping file
- New Struts actions: follow existing patterns in `action/`, register in `struts.xml`
- New REST endpoints: follow patterns in `api/` directory
- Follow existing code style — no new abstractions unless the task requires it
- ORM is Hibernate — use existing DAO patterns, not raw JDBC

## Remotes
- `origin`   → https://github.com/imad7654/UniTime.git (personal fork)
- `upstream` → https://github.com/UniTime/unitime.git (official)
