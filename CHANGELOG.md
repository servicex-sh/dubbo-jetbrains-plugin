<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# RSocket plugin Changelog

## [0.4.1]

- Compatible with JetBrains IDEs 2022.*

## [0.4.0]

- Fixed: add double quote for all text
- Added: `X-Args-0`, `X-Args-1` introduced for param with language injection

```
### Dubbbo update description
//@name updateDesc
DUBBO 127.0.0.1:20880/GreetingsService/updateDesc(java.lang.Integer,java.lang.String)
X-Args-0: 1
Content-Type: text/html

<div>Java Developer</div>
```

## [0.3.0]

- Added: Support short type name, and you can use `/GreetingsService/sayHi(String)`
- Short Types: primitive types, such as int, Integer ..., and String

## [0.2.0]

- Added: Intention action to create DUBBO request from Dubbo Service class
- Fixed: Remove 'dubbo://' from 'dubbo' live template

## [0.1.0]

### Added

- Dubbo requests support in HTTP file
- Live templates: dubbo
- Dubbo Endpoint support: Java and Kotlin
- Code completion/navigation for DUBBO request
- Line marker for Dubbo API with request in HTTP file
