# [MINOA Research Challenge](https://minoa-itn.fau.de/?page_id=921)

The MINOA Research Challenge is about solving one specific version of the problem of planning trips and vehicles simultaneously, developed by M.A.I.O.R, and called the (non-periodic) Integrated Time Table and Vehicle Scheduling Problem. With respect to the original version, the problem of the challenge requires to take into account the extra complexity due to use of Electric Vehicles, that have shorter ranges and need longer recharge times than ordinary Internal Combustion Engine ones [link](https://minoa-itn.fau.de/?page_id=921)
.


This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `minoa-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/minoa-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/minoa-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.


## Example

```shell script
curl -d "@/home/marco/project/optaplanner/minoa/src/main/resources/examples/Small_Input_P.json" -H "Content-Type: application/json" -X POST http://localhost:8080/vsp/solveJson
```